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
import name.kghost.oauth.lib.signature.OAuthSignatureMethod;

public class OAuthSignFilter implements Filter {
	private OAuthConsumer c;

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse resp = (HttpServletResponse) sresp;

		if (req.getParameter(name.kghost.oauth.lib.OAuth.OAUTH_SIGNATURE) != null) {
			String token = req.getParameter(name.kghost.oauth.lib.OAuth.OAUTH_TOKEN);
			if (token == null
					&& req.getParameter(name.kghost.oauth.lib.OAuth.OAUTH_CONSUMER_KEY) == null) {
				resp.sendError(401, "Token not found.");
				return;
			}

			OAuthAccessor a = new OAuthAccessor(c);
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

			try {
				String url = (String) req.getAttribute("fullhost")
						+ req.getRequestURI();
				OAuthMessage m = new OAuthMessage(req.getMethod(), url, req
						.getParameterMap());
				OAuthSignatureMethod o = OAuthSignatureMethod.newSigner(req
						.getParameter(name.kghost.oauth.lib.OAuth.OAUTH_SIGNATURE_METHOD),
						a);
				String sig = o.getSignature(m);
				req.setAttribute(name.kghost.oauth.lib.OAuth.OAUTH_SIGNATURE, sig);
			} catch (OAuthException e) {
				resp.sendError(500, e.getLocalizedMessage());
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
