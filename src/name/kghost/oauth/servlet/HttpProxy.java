package name.kghost.oauth.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpProxy extends HttpServlet {
	private static final long serialVersionUID = 5758996657152761961L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String host = (String) req.getAttribute("fullhost");
		String url = host + req.getRequestURI();
		url += HttpUtil.addQuery(req);
		Map<String, String> hashtable = HttpUtil.getHeaders(req);
		hashtable.remove("host");
		hashtable.put("Host", HttpUtil.getHostInfo(host));
		GetPost getpost = new GetPost();
		String s3 = getpost.doGet(url, hashtable, resp);
		if (s3 != null) {
			int i = getpost.getErrorCode();
			if (i > 0)
				resp.sendError(i, s3);
			else
				resp.sendError(503, s3);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String host = (String) req.getAttribute("fullhost");
		String url = host + req.getRequestURI();
		url += HttpUtil.addQuery(req);
		Map<String, String> headers = HttpUtil.getHeaders(req);
		headers.remove("host");
		headers.put("Host", (String) req.getAttribute("fullhost"));
		GetPost getpost = new GetPost();
		String s4 = getpost.doPost(req, url, headers, req.getInputStream(),
				resp);
		if (s4 != null) {
			int j = getpost.getErrorCode();
			if (j > 0)
				resp.sendError(j, s4);
			else
				resp.sendError(503, s4);
		}
	}
}