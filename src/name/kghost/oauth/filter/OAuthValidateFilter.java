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

import name.kghost.oauth.config.persistent.OAuthConsumer;
import name.kghost.oauth.config.persistent.OAuthUser;
import name.kghost.oauth.lib.OAuthException;
import name.kghost.oauth.lib.OAuthMessage;
import name.kghost.oauth.lib.SimpleOAuthValidator;

public class OAuthValidateFilter implements Filter {
	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse resp = (HttpServletResponse) sresp;
		OAuthMessage m = new OAuthMessage(req);

		if (m.getSignature() != null) {
			String consumer = m.getConsumerKey();
			if (consumer == null) {
				resp.sendError(401, "No Consumer Key");
				return;
			}
			String token = m.getToken();
			OAuthConsumer c;
			OAuthUser u;
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				c = pm.getObjectById(OAuthConsumer.class, consumer);
				if (!c.getMethod().equals(m.getSignatureMethod())) {
					resp.sendError(401, "Sign method mismatch");
					return;
				}
				if (token != null) {
					// token is null when requesting token
					u = pm.getObjectById(OAuthUser.class, token);
				} else {
					u = new OAuthUser(null, null);
				}
			} catch (javax.jdo.JDOObjectNotFoundException e) {
				resp.sendError(401, e.getLocalizedMessage());
				return;
			} finally {
				pm.close();
			}

			try {
				new SimpleOAuthValidator().validateMessage(m, c, u);
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
		return;
	}

	@Override
	public void destroy() {
		return;
	}
}
