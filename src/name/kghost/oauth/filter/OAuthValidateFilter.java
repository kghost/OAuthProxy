package name.kghost.oauth.filter;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.jdo.PersistenceManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.kghost.oauth.lib.OAuthAccessor;
import name.kghost.oauth.lib.OAuthConsumer;
import name.kghost.oauth.lib.OAuthException;
import name.kghost.oauth.lib.OAuthMessage;
import name.kghost.oauth.lib.SimpleOAuthValidator;

public class OAuthValidateFilter implements Filter {
	private OAuthConsumer c;

	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse resp = (HttpServletResponse) sresp;

		if (req.getParameter(name.kghost.oauth.lib.OAuth.OAUTH_SIGNATURE) != null) {
			OAuthAccessor a = new OAuthAccessor(c);
			String token = req.getParameter(name.kghost.oauth.lib.OAuth.OAUTH_TOKEN);
			if (token != null) {
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					OAuthUser u = pm.getObjectById(OAuthUser.class, token);
					a.accessToken = u.OAuthToken;
					a.tokenSecret = u.OAuthTokenSecret;
				} catch (javax.jdo.JDOObjectNotFoundException e) {
					resp.sendError(401, e.getLocalizedMessage());
					return;
				} finally {
					pm.close();
				}
			}

			OAuthMessage m = new OAuthMessage(req);
			try {
				new SimpleOAuthValidator().validateMessage(m, a);
			} catch (OAuthException e) {
				resp.sendError(401, e.getLocalizedMessage());
				return;
			} catch (URISyntaxException e) {
				resp.sendError(500, e.getLocalizedMessage());
				return;
			}
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig conf) throws ServletException {
		c = new OAuthConsumer(conf.getInitParameter("token"), conf
				.getInitParameter("secret"));
	}

	@Override
	public void destroy() {
		return;
	}
}
