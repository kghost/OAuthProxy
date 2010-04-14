/*
 * Copyright 2007, 2008 Netflix, Inc.
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * A request or response message used in the OAuth protocol.
 * <p>
 * The parameters in this class are not percent-encoded. Methods like
 * OAuthClient.invoke and OAuthResponseMessage.completeParameters are
 * responsible for percent-encoding parameters before transmission and decoding
 * them after reception.
 * 
 * @author John Kristian
 */
public class OAuthMessage {
	@SuppressWarnings("unchecked")
	public OAuthMessage(HttpServletRequest req) {
		this(req.getMethod(), req.getRequestURL().toString(), req
				.getParameterMap());
	}

	public OAuthMessage(String method, String url,
			Map<String, String[]> parameters) {
		this.method = method;
		this.URL = url;
		this.parameters = new LinkedList<Pair<String, String>>();
		for (Map.Entry<String, String[]> p : parameters.entrySet()) {
			if (p.getKey().equals(OAuth.OAUTH_NONCE)) {
				O_nonce = p.getValue()[0];
			} else if (p.getKey().equals(OAuth.OAUTH_SIGNATURE)) {
				O_signature = p.getValue()[0];
			} else if (p.getKey().equals(OAuth.OAUTH_SIGNATURE_METHOD)) {
				O_method = p.getValue()[0];
			} else if (p.getKey().equals(OAuth.OAUTH_TIMESTAMP)) {
				O_timestamp = p.getValue()[0];
			} else if (p.getKey().equals(OAuth.OAUTH_CONSUMER_KEY)) {
				O_consumer = p.getValue()[0];
			} else if (p.getKey().equals(OAuth.OAUTH_TOKEN)) {
				O_token = p.getValue()[0];
			} else if (p.getKey().equals(OAuth.OAUTH_VERSION)) {
				O_version = p.getValue()[0];
			}
			this.parameters.add(new Pair<String, String>(p.getKey(), p
					.getValue()[0]));
		}
	}

	public final String method;
	public final String URL;
	private final Collection<Pair<String, String>> parameters;
	private String O_nonce;
	private String O_signature;
	private String O_timestamp;
	private String O_method;
	private String O_consumer;
	private String O_version;
	private String O_token;

	@Override
	public String toString() {
		return "OAuthMessage(" + URL + ", " + parameters + ")";
	}

	public void addParameter(String key, String value) {
		parameters.add(new Pair<String, String>(key, value));
	}

	public Collection<Pair<String, String>> getParameters() {
		return parameters;
	}

	public String getNonce() {
		return O_nonce;
	}

	public String getConsumerKey() {
		return O_consumer;
	}

	public String getToken() {
		return O_token;
	}

	public String getSignatureMethod() {
		return O_method;
	}

	public String getTimestamp() {
		return O_timestamp;
	}

	public String getVersion() {
		return O_version;
	}

	public String getSignature() {
		return O_signature;
	}
}
