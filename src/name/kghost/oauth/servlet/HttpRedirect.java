package name.kghost.oauth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpRedirect extends HttpServlet {
	private static final long serialVersionUID = -1020787791973989022L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String host = (String) req.getAttribute("fullhost");
		String url = host + req.getRequestURI();
		url += HttpUtil.addQuery(req);

		resp.sendRedirect(url);
	}
}
