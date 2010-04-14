package name.kghost.oauth.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import name.kghost.oauth.lib.OAuth;

@SuppressWarnings("deprecation")
public class HttpUtil {
	@SuppressWarnings("unchecked")
	static Map<String, String> getHeaders(HttpServletRequest req) {
		Map<String, String> headers = new HashMap<String, String>();
		Enumeration<String> h = req.getHeaderNames();
		while (h.hasMoreElements()) {
			String s = h.nextElement();
			headers.put(s, req.getHeader(s));
		}
		return headers;
	}

	static String getHostInfo(String s) {
		String s1 = s;
		int i = s1.indexOf("://");
		if (i > 0)
			s1 = s1.substring(i + 3);
		i = s1.indexOf("/");
		if (i > 0)
			s1 = s1.substring(0, i);
		i = s1.indexOf("?");
		if (i > 0)
			s1 = s1.substring(0, i);
		i = s1.indexOf("#");
		if (i > 0)
			s1 = s1.substring(0, i);
		i = s1.indexOf(";");
		if (i > 0)
			s1 = s1.substring(0, i);
		return s1;
	}

	@SuppressWarnings( { "unchecked" })
	static String addQuery(HttpServletRequest req)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		String query = req.getQueryString();
		if (query == null) {
			return "";
		} else {
			Hashtable<String, String[]> ps = HttpUtils.parseQueryString(query);
			String sig = (String) req
					.getAttribute(name.kghost.oauth.lib.OAuth.OAUTH_SIGNATURE);
			if (sig != null)
				ps.put(name.kghost.oauth.lib.OAuth.OAUTH_SIGNATURE,
						new String[] { URLEncoder.encode(sig, "UTF-8") });
			for (Map.Entry<String, String[]> p : ps.entrySet()) {
				for (String v : p.getValue())
					if (sb.length() == 0) {
						sb.append("?").append(p.getKey()).append("=").append(v);
					} else {
						sb.append("&").append(p.getKey()).append("=").append(v);
					}
			}
			return sb.toString();
		}
	}

	static String getMessage(String url, Exception exception) {
		String s1 = exception.getClass().getName();
		int i = s1.lastIndexOf('.');
		s1 = s1.substring(i + 1);
		StringWriter stringwriter = new StringWriter();
		PrintWriter printwriter = new PrintWriter(stringwriter);
		exception.printStackTrace(printwriter);
		return (new StringBuilder()).append("Request: ").append(url).append(
				"\nException: ").append(s1).append(": ").append(
				exception.getMessage()).append("\n").append(
				stringwriter.getBuffer().toString()).toString();
	}

	static void rewriteHeaders(URLConnection conn, HttpServletResponse resp) {
		Map<String, List<String>> map = conn.getHeaderFields();
		if (map != null) {
			for (Map.Entry<String, List<String>> header : map.entrySet()) {
				if (!header.getKey().equals("Content-Type")) {
					StringBuilder s1 = new StringBuilder();
					for (String v : header.getValue()) {
						if (s1.length() > 0)
							s1.append(", ").append(v);
						else
							s1.append(v);
					}
					resp.setHeader(header.getKey(), s1.toString());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	static String buildPostData(HttpServletRequest req)
			throws UnsupportedEncodingException {
		Map<String, String[]> map = req.getParameterMap();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String[]> param : map.entrySet()) {
			if (!param.getKey().equals(OAuth.OAUTH_SIGNATURE)) {
				for (String v : param.getValue()) {
					if (sb.length() > 0)
						sb.append("&");
					sb.append(param.getKey()).append("=");
					sb.append(URLEncoder.encode(v, "UTF-8"));
				}
			} else {
				if (sb.length() > 0)
					sb.append("&");
				sb.append(OAuth.OAUTH_SIGNATURE).append("=").append(
						URLEncoder.encode((String) req
								.getAttribute(OAuth.OAUTH_SIGNATURE), "UTF-8"));
			}
		}
		String postdata = sb.toString();
		return postdata;
	}
}
