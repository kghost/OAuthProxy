package name.kghost.oauth.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class OAuthMessage {
	@SuppressWarnings("unchecked")
	public OAuthMessage(HttpServletRequest req)
			throws UnsupportedEncodingException {
		this.method = req.getMethod();
		scheme = req.getScheme();
		port = req.getServerPort();
		server = req.getServerName();
		uri = req.getRequestURI();

		this.parameters = new HashMap<String, Pair<String, String>>();
		Map<String, String[]> a = req.getParameterMap();
		for (Map.Entry<String, String[]> p : a.entrySet()) {
			String k = p.getKey();
			String v = p.getValue()[0];
			addParameter(k, v);
		}

		String header_s = req.getHeader("Authorization");
		if (header_s == null)
			header_s = req.getHeader("WWW-Authenticate");
		if (header_s != null) {
			parseHeader(header_s);
		}
	}

	private void parseHeader(String header_s)
			throws UnsupportedEncodingException {
		header_s = header_s.trim();
		if (!header_s.startsWith("OAuth "))
			return;
		header_s = header_s.substring(6);
		String[] params = header_s.split(",");
		HashMap<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			param = param.trim();
			String[] ps = param.split("=");
			if (ps.length != 2) {
				return;
			}
			if (ps[0].length() <= 0 || ps[1].length() <= 2) {
				return;
			}
			if (ps[1].charAt(0) == '\"'
					&& ps[1].charAt(ps[1].length() - 1) == '\"') {
				ps[1] = ps[1].substring(1, ps[1].length() - 1);
			}
			map.put(ps[0], URLDecoder.decode(ps[1], "UTF-8"));
		}
		for (Map.Entry<String, String> m : map.entrySet()) {
			this.addParameter(m.getKey(), m.getValue());
		}
	}

	public final String method;
	private String scheme;
	private int port;
	private String server;
	private String uri;
	private final Map<String, Pair<String, String>> parameters;
	private String O_nonce;
	private String O_signature;
	private String O_timestamp;
	private String O_method;
	private String O_consumer;
	private String O_version;
	private String O_token;

	@Override
	public String toString() {
		return "OAuthMessage(" + getUrl() + ", " + parameters + ")";
	}

	public void addParameter(String key, String value) {
		if (key.equals(OAuth.OAUTH_NONCE)) {
			O_nonce = value;
		} else if (key.equals(OAuth.OAUTH_SIGNATURE)) {
			O_signature = value;
		} else if (key.equals(OAuth.OAUTH_SIGNATURE_METHOD)) {
			O_method = value;
		} else if (key.equals(OAuth.OAUTH_TIMESTAMP)) {
			O_timestamp = value;
		} else if (key.equals(OAuth.OAUTH_CONSUMER_KEY)) {
			O_consumer = value;
		} else if (key.equals(OAuth.OAUTH_TOKEN)) {
			O_token = value;
		} else if (key.equals(OAuth.OAUTH_VERSION)) {
			O_version = value;
		}
		parameters.put(key, new Pair<String, String>(key, value));
	}

	public Collection<Pair<String, String>> getParameters() {
		return parameters.values();
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

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUrl() {
		StringBuffer url = new StringBuffer();
		url.append(scheme);
		url.append("://");
		url.append(server);
		if (port > 0
				&& ((scheme.equalsIgnoreCase("http") && port != 80) || (scheme
						.equalsIgnoreCase("https") && port != 443))) {
			url.append(':');
			url.append(port);
		}
		url.append(uri);
		return url.toString();
	}
}
