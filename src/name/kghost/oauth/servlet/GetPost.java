package name.kghost.oauth.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
			HttpServletResponse resp) throws UnsupportedEncodingException {
		HttpURLConnection conn = null;
		String postdata = HttpUtil.buildPostData(req);
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			for (Entry<String, String> header : headers.entrySet()) {
				conn.setRequestProperty(header.getKey(), header.getValue());
			}
			conn.setRequestProperty("Context-Type",
					"application/x-www-form-urlencoded");
			byte[] b2 = postdata.getBytes("UTF-8");
			conn
					.setRequestProperty("Content-Length", String
							.valueOf(b2.length));
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
		return doResponse(url, resp, conn);
	}

	public String doGet(String url, Map<String, String> headers,
			HttpServletResponse resp) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setUseCaches(false);
			for (Entry<String, String> header : headers.entrySet()) {
				conn.setRequestProperty(header.getKey(), header.getValue());
			}
		} catch (Exception exception) {
			return HttpUtil.getMessage(url, exception);
		}
		return doResponse(url, resp, conn);
	}

	private String doResponse(String url, HttpServletResponse resp,
			HttpURLConnection conn) {
		try {
			setErrorCode(conn.getResponseCode());
			resp.setStatus(conn.getResponseCode());
			String contentType = conn.getContentType();
			if (contentType != null)
				resp.setContentType(contentType);
			String contentEnconding = conn.getContentEncoding();
			HttpUtil.rewriteHeaders(conn, resp);
			if (contentEnconding == null)
				contentEnconding = "";
			InputStream input = null;
			BufferedOutputStream output = null;
			try {
				if (contentEnconding.indexOf("gzip") >= 0)
					input = new GZIPInputStream(conn.getInputStream());
				else
					input = new BufferedInputStream(conn.getInputStream());
				output = new BufferedOutputStream(resp.getOutputStream());
				byte[] b = new byte[512];
				int i;
				while ((i = input.read(b)) >= 0)
					output.write(b, 0, i);
			} catch (Exception exception2) {
				return HttpUtil.getMessage(url, exception2);
			} finally {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.flush();
					output.close();
				}
			}
		} catch (Exception exception1) {
			return HttpUtil.getMessage(url, exception1);
		}
		return null;
	}

	private int errorCode;
}
