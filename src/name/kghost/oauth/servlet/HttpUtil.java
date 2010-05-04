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

@SuppressWarnings("deprecation")
public class HttpUtil {
	@SuppressWarnings("unchecked")
	static Map<String, String> getRequestHeaders(HttpServletRequest req) {
		Map<String, String> headers = new HashMap<String, String>();
		Enumeration<String> h = req.getHeaderNames();
		while (h.hasMoreElements()) {
			String s = h.nextElement();
			headers.put(s, req.getHeader(s));
		}
		return headers;
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
			Map<String, String> hs = HttpUtil.getOverwriteParams(req);
			for (Map.Entry<String, String[]> p : ps.entrySet()) {
				String k = p.getKey();
				if (hs.containsKey(k)) {
					String v = hs.get(k);
					addQueryParam(sb, k, v);
				} else {
					for (String v : p.getValue()) {
						addQueryParam(sb, k, v);
					}
				}
			}
			return sb.toString();
		}
	}

	private static void addQueryParam(StringBuilder sb, String k, String v)
			throws UnsupportedEncodingException {
		if (sb.length() == 0) {
			sb.append("?").append(k).append("=").append(
					URLEncoder.encode(v, "UTF-8"));
		} else {
			sb.append("&").append(k).append("=").append(
					URLEncoder.encode(v, "UTF-8"));
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

	static void rewriteResponseHeaders(URLConnection conn,
			HttpServletResponse resp) {
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
		Map<String, String> hs = HttpUtil.getOverwriteParams(req);
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String[]> param : map.entrySet()) {
			String k = param.getKey();
			if (!hs.containsKey(k)) {
				for (String v : param.getValue()) {
					addPostParam(sb, k, v);
				}
			} else {
				addPostParam(sb, k, hs.get(k));
			}
		}
		String postdata = sb.toString();
		return postdata;
	}

	private static void addPostParam(StringBuilder sb, String k, String v)
			throws UnsupportedEncodingException {
		if (sb.length() > 0)
			sb.append("&");
		sb.append(k).append("=").append(URLEncoder.encode(v, "UTF-8"));
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getOverwriteParams(HttpServletRequest req) {
		Map<String, String> headers = (Map<String, String>) req
				.getAttribute("OverwriteMap");
		if (headers == null) {
			headers = new HashMap<String, String>();
			req.setAttribute("OverwriteMap", headers);
		}
		return headers;
	}
}
