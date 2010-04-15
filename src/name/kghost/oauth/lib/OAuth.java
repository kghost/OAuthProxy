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

package name.kghost.oauth.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Miscellaneous constants, methods and types.
 * 
 * @author John Kristian
 */
public class OAuth {

	public static final String VERSION_1_0 = "1.0";

	/** The encoding used to represent characters as bytes. */
	public static final String ENCODING = "UTF-8";

	/** The MIME type for a sequence of OAuth parameters. */
	public static final String FORM_ENCODED = "application/x-www-form-urlencoded";

	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	public static final String OAUTH_CONSUMER_SECRET = "oauth_consumer_secret";
	public static final String OAUTH_TOKEN = "oauth_token";
	public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	public static final String OAUTH_SIGNATURE = "oauth_signature";
	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	public static final String OAUTH_NONCE = "oauth_nonce";
	public static final String OAUTH_VERSION = "oauth_version";
	public static final String OAUTH_CALLBACK = "oauth_callback";
	public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	public static final String OAUTH_VERIFIER = "oauth_verifier";

	public static final String HMAC_SHA1 = "HMAC-SHA1";
	public static final String RSA_SHA1 = "RSA-SHA1";

	/**
	 * Strings used for <a href="http://wiki.oauth.net/ProblemReporting">problem
	 * reporting</a>.
	 */
	public static class Problems {
		public static final String VERSION_REJECTED = "version_rejected";
		public static final String PARAMETER_ABSENT = "parameter_absent";
		public static final String PARAMETER_REJECTED = "parameter_rejected";
		public static final String TIMESTAMP_REFUSED = "timestamp_refused";
		public static final String NONCE_USED = "nonce_used";
		public static final String SIGNATURE_METHOD_REJECTED = "signature_method_rejected";
		public static final String SIGNATURE_INVALID = "signature_invalid";
		public static final String CONSUMER_KEY_UNKNOWN = "consumer_key_unknown";
		public static final String CONSUMER_KEY_REJECTED = "consumer_key_rejected";
		public static final String CONSUMER_KEY_REFUSED = "consumer_key_refused";
		public static final String TOKEN_USED = "token_used";
		public static final String TOKEN_EXPIRED = "token_expired";
		public static final String TOKEN_REVOKED = "token_revoked";
		public static final String TOKEN_REJECTED = "token_rejected";
		public static final String ADDITIONAL_AUTHORIZATION_REQUIRED = "additional_authorization_required";
		public static final String PERMISSION_UNKNOWN = "permission_unknown";
		public static final String PERMISSION_DENIED = "permission_denied";
		public static final String USER_REFUSED = "user_refused";

		public static final String OAUTH_ACCEPTABLE_VERSIONS = "oauth_acceptable_versions";
		public static final String OAUTH_ACCEPTABLE_TIMESTAMPS = "oauth_acceptable_timestamps";
		public static final String OAUTH_PARAMETERS_ABSENT = "oauth_parameters_absent";
		public static final String OAUTH_PARAMETERS_REJECTED = "oauth_parameters_rejected";
		public static final String OAUTH_PROBLEM_ADVICE = "oauth_problem_advice";

		/**
		 * A map from an <a
		 * href="http://wiki.oauth.net/ProblemReporting">oauth_problem</a> value
		 * to the appropriate HTTP response code.
		 */
		public static final Map<String, Integer> TO_HTTP_CODE = mapToHttpCode();

		private static Map<String, Integer> mapToHttpCode() {
			Integer badRequest = new Integer(400);
			Integer unauthorized = new Integer(401);
			Integer serviceUnavailable = new Integer(503);
			Map<String, Integer> map = new HashMap<String, Integer>();

			map.put(Problems.VERSION_REJECTED, badRequest);
			map.put(Problems.PARAMETER_ABSENT, badRequest);
			map.put(Problems.PARAMETER_REJECTED, badRequest);
			map.put(Problems.TIMESTAMP_REFUSED, badRequest);
			map.put(Problems.SIGNATURE_METHOD_REJECTED, badRequest);

			map.put(Problems.NONCE_USED, unauthorized);
			map.put(Problems.TOKEN_USED, unauthorized);
			map.put(Problems.TOKEN_EXPIRED, unauthorized);
			map.put(Problems.TOKEN_REVOKED, unauthorized);
			map.put(Problems.TOKEN_REJECTED, unauthorized);
			map.put("token_not_authorized", unauthorized);
			map.put(Problems.SIGNATURE_INVALID, unauthorized);
			map.put(Problems.CONSUMER_KEY_UNKNOWN, unauthorized);
			map.put(Problems.CONSUMER_KEY_REJECTED, unauthorized);
			map.put(Problems.ADDITIONAL_AUTHORIZATION_REQUIRED, unauthorized);
			map.put(Problems.PERMISSION_UNKNOWN, unauthorized);
			map.put(Problems.PERMISSION_DENIED, unauthorized);

			map.put(Problems.USER_REFUSED, serviceUnavailable);
			map.put(Problems.CONSUMER_KEY_REFUSED, serviceUnavailable);
			return Collections.unmodifiableMap(map);
		}

	}

	public static String percentEncode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, ENCODING)
					// OAuth encodes some characters differently:
					.replace("+", "%20").replace("*", "%2A")
					.replace("%7E", "~");
			// This could be done faster with more hand-crafted code.
		} catch (UnsupportedEncodingException wow) {
			throw new RuntimeException(wow.getMessage(), wow);
		}
	}
}
