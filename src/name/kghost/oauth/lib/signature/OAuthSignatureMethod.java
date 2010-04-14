/*
 * Copyright 2007 Netflix, Inc.
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

package name.kghost.oauth.lib.signature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import name.kghost.oauth.lib.OAuth;
import name.kghost.oauth.lib.OAuthAccessor;
import name.kghost.oauth.lib.OAuthConsumer;
import name.kghost.oauth.lib.OAuthException;
import name.kghost.oauth.lib.OAuthMessage;
import name.kghost.oauth.lib.OAuthProblemException;
import name.kghost.oauth.lib.Pair;
import name.kghost.oauth.lib.SimpleOAuthValidator;

/**
 * A pair of algorithms for computing and verifying an OAuth digital signature.
 * <p>
 * Static methods of this class implement a registry of signature methods. It's
 * pre-populated with the standard OAuth algorithms. Appliations can replace
 * them or add new ones.
 * 
 * @author John Kristian
 */
public abstract class OAuthSignatureMethod {
	public String getSignature(OAuthMessage message) throws OAuthException,
			IOException, URISyntaxException {
		String baseString = getBaseString(message.URL, message.method, message
				.getParameters());
		String signature = getSignature(baseString);
		return signature;
	}

	/**
	 * @throws OAuthException
	 */
	protected void initialize(String name, OAuthAccessor accessor)
			throws OAuthException {
		String secret = accessor.consumer.consumerSecret;
		if (name.endsWith(_ACCESSOR)) {
			// This code supports the 'Accessor Secret' extensions
			// described in http://oauth.pbwiki.com/AccessorSecret
			final String key = OAuthConsumer.ACCESSOR_SECRET;
			Object accessorSecret = accessor.getProperty(key);
			if (accessorSecret == null) {
				accessorSecret = accessor.consumer.getProperty(key);
			}
			if (accessorSecret != null) {
				secret = accessorSecret.toString();
			}
		}
		if (secret == null) {
			secret = "";
		}
		setConsumerSecret(secret);
	}

	public static final String _ACCESSOR = "-Accessor";

	public void validate(OAuthMessage message) throws IOException,
			OAuthException, URISyntaxException {
		String signature = message.getSignature();
		String baseString = getBaseString(message);
		if (!isValid(signature, baseString)) {
			OAuthProblemException problem = new OAuthProblemException(
					"signature_invalid");
			problem.setParameter("oauth_signature", signature);
			problem.setParameter("oauth_signature_base_string", baseString);
			problem.setParameter("oauth_signature_method", message
					.getSignatureMethod());
			throw problem;
		}
	}

	/** Compute the signature for the given base string. */
	protected abstract String getSignature(String baseString)
			throws OAuthException;

	/** Decide whether the signature is valid. */
	protected abstract boolean isValid(String signature, String baseString)
			throws OAuthException;

	private String consumerSecret;
	private String tokenSecret;

	protected String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public String getBaseString(String url, String method,
			Collection<Pair<String, String>> parameters)
			throws URISyntaxException, IOException {
		return OAuth.percentEncode(method.toUpperCase()) + '&'
				+ OAuth.percentEncode(normalizeUrl(url)) + '&'
				+ OAuth.percentEncode(normalizeParameters(parameters));
	}

	private String getBaseString(OAuthMessage message)
			throws URISyntaxException, IOException {
		return getBaseString(message.URL, message.method, message
				.getParameters());
	}

	protected static String normalizeUrl(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String scheme = uri.getScheme().toLowerCase();
		String authority = uri.getAuthority().toLowerCase();
		boolean dropPort = (scheme.equals("http") && uri.getPort() == 80)
				|| (scheme.equals("https") && uri.getPort() == 443);
		if (dropPort) {
			// find the last : in the authority
			int index = authority.lastIndexOf(":");
			if (index >= 0) {
				authority = authority.substring(0, index);
			}
		}
		String path = uri.getRawPath();
		if (path == null || path.length() <= 0) {
			path = "/"; // conforms to RFC 2616 section 3.2.2
		}
		// we know that there is no query and no fragment here.
		return scheme + "://" + authority + path;
	}

	private class PairComparator implements Comparator<Pair<String, String>> {
		@Override
		public int compare(Pair<String, String> o1, Pair<String, String> o2) {
			String k1 = o1.getFirst();
			String k2 = o2.getFirst();
			return k1.compareTo(k2);
		}
	}

	protected String normalizeParameters(
			Collection<Pair<String, String>> parameters) throws IOException {
		if (parameters == null) {
			return "";
		}
		List<Pair<String, String>> p = new LinkedList<Pair<String, String>>();
		for (Pair<String, String> parameter : parameters) {
			if (!OAuth.OAUTH_SIGNATURE.equals(parameter.getFirst())) {
				p.add(parameter);
			}
		}
		Collections.sort(p, new PairComparator());
		return SimpleOAuthValidator.formEncode(p);
	}

	/**
	 * Determine whether the given strings contain the same sequence of
	 * characters. The implementation discourages a <a
	 * href="http://codahale.com/a-lesson-in-timing-attacks/">timing attack</a>.
	 */
	public static boolean equals(String x, String y) {
		if (x == null)
			return y == null;
		else if (y == null)
			return false;
		else if (y.length() <= 0)
			return x.length() <= 0;
		char[] a = x.toCharArray();
		char[] b = y.toCharArray();
		char diff = (char) ((a.length == b.length) ? 0 : 1);
		int j = 0;
		for (int i = 0; i < a.length; ++i) {
			diff |= a[i] ^ b[j];
			j = (j + 1) % b.length;
		}
		return diff == 0;
	}

	/**
	 * Determine whether the given arrays contain the same sequence of bytes.
	 * The implementation discourages a <a
	 * href="http://codahale.com/a-lesson-in-timing-attacks/">timing attack</a>.
	 */
	public static boolean equals(byte[] a, byte[] b) {
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else if (b.length <= 0)
			return a.length <= 0;
		byte diff = (byte) ((a.length == b.length) ? 0 : 1);
		int j = 0;
		for (int i = 0; i < a.length; ++i) {
			diff |= a[i] ^ b[j];
			j = (j + 1) % b.length;
		}
		return diff == 0;
	}

	public static byte[] decodeBase64(String s) {
		return BASE64.decode(s.getBytes());
	}

	public static String base64Encode(byte[] b) {
		return new String(BASE64.encode(b));
	}

	private static final Base64 BASE64 = new Base64();

	public static OAuthSignatureMethod newSigner(String method,
			OAuthAccessor accessor) throws OAuthException {
		OAuthSignatureMethod signer = newMethod(method, accessor);
		signer.setTokenSecret(accessor.tokenSecret);
		return signer;
	}

	/** The factory for signature methods. */
	@SuppressWarnings("unchecked")
	public static OAuthSignatureMethod newMethod(String name,
			OAuthAccessor accessor) throws OAuthException {
		try {
			Class methodClass = NAME_TO_CLASS.get(name);
			if (methodClass != null) {
				OAuthSignatureMethod method = (OAuthSignatureMethod) methodClass
						.newInstance();
				method.initialize(name, accessor);
				return method;
			}
			OAuthProblemException problem = new OAuthProblemException(
					OAuth.Problems.SIGNATURE_METHOD_REJECTED);
			StringBuilder p = new StringBuilder();
			for (String v : NAME_TO_CLASS.keySet()) {
				if (p.length() > 0) {
					p.append("&");
				}
				String s = OAuth.percentEncode(v);
				p.append(s);
			}
			String acceptable = p.toString();
			if (acceptable.length() > 0) {
				problem.setParameter("oauth_acceptable_signature_methods",
						acceptable.toString());
			}
			throw problem;
		} catch (InstantiationException e) {
			throw new OAuthException(e);
		} catch (IllegalAccessException e) {
			throw new OAuthException(e);
		}
	}

	/**
	 * Subsequently, newMethod(name) will attempt to instantiate the given
	 * class, with no constructor parameters.
	 */
	@SuppressWarnings("unchecked")
	public static void registerMethodClass(String name, Class clazz) {
		if (clazz == null)
			unregisterMethod(name);
		else
			NAME_TO_CLASS.put(name, clazz);
	}

	/**
	 * Subsequently, newMethod(name) will fail.
	 */
	public static void unregisterMethod(String name) {
		NAME_TO_CLASS.remove(name);
	}

	@SuppressWarnings("unchecked")
	private static final Map<String, Class> NAME_TO_CLASS = new ConcurrentHashMap<String, Class>();
	static {
		registerMethodClass("HMAC-SHA1", HMAC_SHA1.class);
		registerMethodClass("PLAINTEXT", PLAINTEXT.class);
		registerMethodClass("RSA-SHA1", RSA_SHA1.class);
		registerMethodClass("HMAC-SHA1" + _ACCESSOR, HMAC_SHA1.class);
		registerMethodClass("PLAINTEXT" + _ACCESSOR, PLAINTEXT.class);
	}
}
