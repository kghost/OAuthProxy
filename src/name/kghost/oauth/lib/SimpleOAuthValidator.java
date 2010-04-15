/*
 * Copyright 2008 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.kghost.oauth.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import name.kghost.oauth.config.OAuthConsumer;
import name.kghost.oauth.config.OAuthUser;
import name.kghost.oauth.lib.signature.OAuthSignatureMethod;

//TODO: move this class into oauth-provider
/**
 * A simple OAuthValidator, which checks the version, whether the timestamp is
 * close to now, the nonce hasn't been used before and the signature is valid.
 * Each check may be overridden.
 * <p>
 * This implementation is less than industrial strength:
 * <ul>
 * <li>Duplicate nonces won't be reliably detected by a service provider running
 * in multiple processes, since the used nonces are stored in memory.</li>
 * <li>The collection of used nonces is a synchronized choke point</li>
 * <li>The used nonces may occupy lots of memory, although you can minimize this
 * by calling releaseGarbage periodically.</li>
 * <li>The range of acceptable timestamps can't be changed, and there's no
 * system for increasing the range smoothly.</li>
 * <li>Correcting the clock backward may allow duplicate nonces.</li>
 * </ul>
 * For a big service provider, it might be better to store used nonces in a
 * database.
 * 
 * @author Dirk Balfanz
 * @author John Kristian
 */
public class SimpleOAuthValidator implements OAuthValidator {
	/** The default maximum age of timestamps is 5 minutes. */
	public static final long DEFAULT_MAX_TIMESTAMP_AGE = 5 * 60 * 1000L;
	public static final long DEFAULT_TIMESTAMP_WINDOW = DEFAULT_MAX_TIMESTAMP_AGE;

	/**
	 * Names of parameters that may not appear twice in a valid message. This
	 * limitation is specified by OAuth Core <a
	 * href="http://oauth.net/core/1.0#anchor7">section 5</a>.
	 */
	private static final Set<String> SINGLE_PARAMETERS = constructSingleParameters();

	private static Set<String> constructSingleParameters() {
		Set<String> s = new HashSet<String>();
		for (String p : new String[] { OAuth.OAUTH_CONSUMER_KEY,
				OAuth.OAUTH_TOKEN, OAuth.OAUTH_TOKEN_SECRET,
				OAuth.OAUTH_CALLBACK, OAuth.OAUTH_SIGNATURE_METHOD,
				OAuth.OAUTH_SIGNATURE, OAuth.OAUTH_TIMESTAMP,
				OAuth.OAUTH_NONCE, OAuth.OAUTH_VERSION }) {
			s.add(p);
		}
		return Collections.unmodifiableSet(s);
	}

	/**
	 * Construct a validator that rejects messages more than five minutes old or
	 * with a OAuth version other than 1.0.
	 */
	public SimpleOAuthValidator() {
		this(DEFAULT_TIMESTAMP_WINDOW, Double.parseDouble(OAuth.VERSION_1_0));
	}

	/**
	 * Public constructor.
	 * 
	 * @param maxTimestampAgeMsec
	 *            the range of valid timestamps, in milliseconds into the past
	 *            or future. So the total range of valid timestamps is twice
	 *            this value, rounded to the nearest second.
	 * @param maxVersion
	 *            the maximum valid oauth_version
	 */
	public SimpleOAuthValidator(long maxTimestampAgeMsec, double maxVersion) {
		this.maxTimestampAgeMsec = maxTimestampAgeMsec;
		this.maxVersion = maxVersion;
	}

	protected final double minVersion = 1.0;
	protected final double maxVersion;
	protected final long maxTimestampAgeMsec;
	private final Set<UsedNonce> usedNonces = new TreeSet<UsedNonce>();

	/**
	 * Allow objects that are no longer useful to become garbage.
	 * 
	 * @return the earliest point in time at which another call will release
	 *         some garbage, or null to indicate there's nothing currently
	 *         stored that will become garbage in future. This value may change,
	 *         each time releaseGarbage or validateNonce is called.
	 */
	public Date releaseGarbage() {
		return removeOldNonces(currentTimeMsec());
	}

	/**
	 * Remove usedNonces with timestamps that are too old to be valid.
	 */
	private Date removeOldNonces(long currentTimeMsec) {
		UsedNonce next = null;
		UsedNonce min = new UsedNonce(
				(currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L);
		synchronized (usedNonces) {
			// Because usedNonces is a TreeSet, its iterator produces
			// elements from oldest to newest (their natural order).
			for (Iterator<UsedNonce> iter = usedNonces.iterator(); iter
					.hasNext();) {
				UsedNonce used = iter.next();
				if (min.compareTo(used) <= 0) {
					next = used;
					break; // all the rest are also new enough
				}
				iter.remove(); // too old
			}
		}
		if (next == null)
			return null;
		return new Date((next.getTimestamp() * 1000L) + maxTimestampAgeMsec
				+ 500);
	}

	@Override
	public void validateMessage(OAuthMessage message, OAuthConsumer consumer,
			OAuthUser user) throws OAuthException, IOException,
			URISyntaxException {
		checkSingleParameters(message);
		validateVersion(message);
		validateTimestampAndNonce(message);
		validateSignature(message, consumer, user);
	}

	/** Throw an exception if any SINGLE_PARAMETERS occur repeatedly. */
	private void checkSingleParameters(OAuthMessage message)
			throws IOException, OAuthException {
		// Check for repeated oauth_ parameters:
		boolean repeated = false;
		Map<String, Collection<String>> nameToValues = new HashMap<String, Collection<String>>();
		for (Pair<String, String> parameter : message.getParameters()) {
			String name = parameter.getFirst();
			if (SINGLE_PARAMETERS.contains(name)) {
				Collection<String> values = nameToValues.get(name);
				if (values == null) {
					values = new LinkedList<String>();
					nameToValues.put(name, values);
				} else {
					repeated = true;
				}
				values.add(parameter.getSecond());
			}
		}
		if (repeated) {
			Collection<Pair<String, String>> rejected = new LinkedList<Pair<String, String>>();
			for (Map.Entry<String, Collection<String>> p : nameToValues
					.entrySet()) {
				String name = p.getKey();
				Collection<String> values = p.getValue();
				if (values.size() > 1) {
					for (String value : values) {
						rejected.add(new Pair<String, String>(name, value));
					}
				}
			}
			OAuthProblemException problem = new OAuthProblemException(
					OAuth.Problems.PARAMETER_REJECTED);
			problem.setParameter(OAuth.Problems.OAUTH_PARAMETERS_REJECTED,
					SimpleOAuthValidator.formEncode(rejected));
			throw problem;
		}
	}

	protected void validateVersion(OAuthMessage message) throws OAuthException {
		String versionString = message.getVersion();
		if (versionString != null) {
			double version = Double.parseDouble(versionString);
			if (version < minVersion || maxVersion < version) {
				OAuthProblemException problem = new OAuthProblemException(
						OAuth.Problems.VERSION_REJECTED);
				problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_VERSIONS,
						minVersion + "-" + maxVersion);
				throw problem;
			}
		}
	}

	/**
	 * Throw an exception if the timestamp is out of range or the nonce has been
	 * validated previously.
	 */
	protected void validateTimestampAndNonce(OAuthMessage message)
			throws OAuthProblemException {
		long timestamp = Long.parseLong(message.getTimestamp());
		long now = currentTimeMsec();
		validateTimestamp(timestamp, now);
		validateNonce(message, timestamp, now);
	}

	/** Throw an exception if the timestamp [sec] is out of range. */
	protected void validateTimestamp(long timestamp, long currentTimeMsec)
			throws OAuthProblemException {
		long min = (currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L;
		long max = (currentTimeMsec + maxTimestampAgeMsec + 500) / 1000L;
		if (timestamp < min || max < timestamp) {
			OAuthProblemException problem = new OAuthProblemException(
					OAuth.Problems.TIMESTAMP_REFUSED);
			problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_TIMESTAMPS,
					min + "-" + max);
			throw problem;
		}
	}

	/**
	 * Throw an exception if the nonce has been validated previously.
	 * 
	 * @return the earliest point in time at which a call to releaseGarbage will
	 *         actually release some garbage, or null to indicate there's
	 *         nothing currently stored that will become garbage in future.
	 */
	protected Date validateNonce(OAuthMessage message, long timestamp,
			long currentTimeMsec) throws OAuthProblemException {
		UsedNonce nonce = new UsedNonce(timestamp, message.getNonce(), message
				.getConsumerKey(), message.getToken());
		/*
		 * The OAuth standard requires the token to be omitted from the stored
		 * nonce. But I include it, to harmonize with a Consumer that generates
		 * nonces using several independent computers, each with its own token.
		 */
		boolean valid = false;
		synchronized (usedNonces) {
			valid = usedNonces.add(nonce);
		}
		if (!valid) {
			throw new OAuthProblemException(OAuth.Problems.NONCE_USED);
		}
		return removeOldNonces(currentTimeMsec);
	}

	protected void validateSignature(OAuthMessage message,
			OAuthConsumer consumer, OAuthUser user) throws OAuthException,
			IOException, URISyntaxException {
		OAuthSignatureMethod.newSigner(consumer, user).validate(message);
	}

	/** Get the number of milliseconds since midnight, January 1, 1970 UTC. */
	protected long currentTimeMsec() {
		return System.currentTimeMillis();
	}

	/**
	 * Write a form-urlencoded document into the given stream, containing the
	 * given sequence of name/value pairs.
	 */
	private static void formEncode(Collection<Pair<String, String>> parameters,
			OutputStream into) throws IOException {
		if (parameters != null) {
			boolean first = true;
			for (Pair<String, String> parameter : parameters) {
				if (first) {
					first = false;
				} else {
					into.write('&');
				}
				into
						.write(OAuth.percentEncode(parameter.getFirst())
								.getBytes());
				into.write('=');
				into.write(OAuth.percentEncode(parameter.getSecond())
						.getBytes());
			}
		}
	}

	/**
	 * Construct a form-urlencoded document containing the given sequence of
	 * name/value pairs. Use OAuth percent encoding (not exactly the encoding
	 * mandated by HTTP).
	 */
	public static String formEncode(Collection<Pair<String, String>> parameters)
			throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		SimpleOAuthValidator.formEncode(parameters, b);
		return new String(b.toByteArray());
	}

	/**
	 * Selected parameters from an OAuth request, in a form suitable for
	 * detecting duplicate requests. The implementation is optimized for the
	 * comparison operations (compareTo, equals and hashCode).
	 * 
	 * @author John Kristian
	 */
	private static class UsedNonce implements Comparable<UsedNonce> {
		/**
		 * Construct an object containing the given timestamp, nonce and other
		 * parameters. The order of parameters is significant.
		 */
		UsedNonce(long timestamp, String... nonceEtc) {
			StringBuilder key = new StringBuilder(String.format("%20d", Long
					.valueOf(timestamp)));
			// The blank padding ensures that timestamps are compared as
			// numbers.
			for (String etc : nonceEtc) {
				key.append("&").append(
						etc == null ? " " : OAuth.percentEncode(etc));
				// A null value is different from "" or any other String.
			}
			sortKey = key.toString();
		}

		private final String sortKey;

		long getTimestamp() {
			int end = sortKey.indexOf("&");
			if (end < 0)
				end = sortKey.length();
			return Long.parseLong(sortKey.substring(0, end).trim());
		}

		/**
		 * Determine the relative order of <code>this</code> and
		 * <code>that</code>, as specified by Comparable. The timestamp is most
		 * significant; that is, if the timestamps are different, return 1 or
		 * -1. If <code>this</code> contains only a timestamp (with no nonce
		 * etc.), return -1 or 0. The treatment of the nonce etc. is murky,
		 * although 0 is returned only if they're all equal.
		 */
		public int compareTo(UsedNonce that) {
			return (that == null) ? 1 : sortKey.compareTo(that.sortKey);
		}

		@Override
		public int hashCode() {
			return sortKey.hashCode();
		}

		/**
		 * Return true iff <code>this</code> and <code>that</code> contain equal
		 * timestamps, nonce etc., in the same order.
		 */
		@Override
		public boolean equals(Object that) {
			if (that == null)
				return false;
			if (that == this)
				return true;
			if (that.getClass() != getClass())
				return false;
			return sortKey.equals(((UsedNonce) that).sortKey);
		}

		@Override
		public String toString() {
			return sortKey;
		}
	}
}
