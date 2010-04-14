package name.kghost.oauth.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class OAuthProxyFilter implements Filter {
	private String schema;
	private String host;
	private int port;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		req.setAttribute("schema", schema);
		req.setAttribute("host", host);
		req.setAttribute("port", port);
		req.setAttribute("fullhost", schema + "://" + host + ":" + port);
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig conf) throws ServletException {
		schema = conf.getInitParameter("schema");
		host = conf.getInitParameter("host");
		port = Integer.parseInt(conf.getInitParameter("port"));
	}

	@Override
	public void destroy() {
		return;
	}
}
