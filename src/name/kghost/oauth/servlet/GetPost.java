package name.kghost.oauth.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetPost {
	public GetPost() {
		errorCode = 0;
	}

	public void setErrorCode(int i) {
		errorCode = i;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String doPost(HttpServletRequest req, String url,
			Map<String, String> headers, ServletInputStream post,
			HttpServletResponse resp) throws IOException {
		HttpURLConnection conn = null;
		String postdata = HttpUtil.buildPostData(req);
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				if (!key.toLowerCase().equals("authorization")) {
					conn.setRequestProperty(key, header.getValue());
				} else {
					conn.setRequestProperty(key,
							HttpUtil.buildAuthHeader(req, header.getValue()));
				}
			}
			conn.setRequestProperty("Context-Type",
					"application/x-www-form-urlencoded");
			byte[] b2 = postdata.getBytes("UTF-8");
			conn.setRequestProperty("Content-Length", String.valueOf(b2.length));
			OutputStream output = conn.getOutputStream();
			output.write(b2);
			byte[] b = new byte[512];
			int i = 0;
			while ((i = post.read(b)) != -1)
				output.write(b, 0, i);
			output.close();
		} catch (Exception exception) {
			return HttpUtil.getMessage(url, exception);
		}
		return doResponse(resp, conn);
	}

	public String doGet(HttpServletRequest req, String url,
			Map<String, String> headers, HttpServletResponse resp)
			throws IOException {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setUseCaches(false);
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				if (!key.toLowerCase().equals("authorization")) {
					conn.setRequestProperty(key, header.getValue());
				} else {
					conn.setRequestProperty(key,
							HttpUtil.buildAuthHeader(req, header.getValue()));
				}
			}
		} catch (Exception exception) {
			return HttpUtil.getMessage(url, exception);
		}
		return doResponse(resp, conn);
	}

	private String doResponse(HttpServletResponse resp, HttpURLConnection conn)
			throws IOException {
		int code = conn.getResponseCode();
		setErrorCode(code);
		resp.setStatus(code);
		String contentType = conn.getContentType();
		if (contentType != null)
			resp.setContentType(contentType);
		HttpUtil.rewriteResponseHeaders(conn, resp);
		InputStream input = null;
		OutputStream output = null;
		try {
			String contentEnconding = conn.getContentEncoding();
			if (contentEnconding == null)
				contentEnconding = "";
			if (contentEnconding.indexOf("gzip") >= 0)
				input = new GZIPInputStream(conn.getInputStream());
			else
				input = conn.getInputStream();
			output = resp.getOutputStream();
			byte[] b = new byte[512];
			int i;
			while ((i = input.read(b)) >= 0)
				output.write(b, 0, i);
		} finally {
			if (output != null) {
				output.flush();
			}
			if (input != null) {
				input.close();
			}
		}
		return null;
	}

	private int errorCode;
}
