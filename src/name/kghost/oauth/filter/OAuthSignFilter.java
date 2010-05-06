package name.kghost.oauth.filter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

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
import name.kghost.oauth.lib.OAuth;
import name.kghost.oauth.lib.OAuthException;
import name.kghost.oauth.lib.OAuthMessage;
import name.kghost.oauth.lib.signature.OAuthSignatureMethod;
import name.kghost.oauth.servlet.HttpUtil;

public class OAuthSignFilter implements Filter {
	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse resp = (HttpServletResponse) sresp;

		if (req.getParameter(OAuth.OAUTH_SIGNATURE) != null) {
			String consumer_key = req.getParameter(OAuth.OAUTH_CONSUMER_KEY);
			if (consumer_key == null) {
				resp.sendError(401, "Token not found.");
				return;
			}

			OAuthConsumer c;
			OAuthUser u;
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				OAuthConsumer t = pm.getObjectById(OAuthConsumer.class, consumer_key);
				c = pm.getObjectById(OAuthConsumer.class, t.getReplaceKey());

				String token = req.getParameter(OAuth.OAUTH_TOKEN);
				if (token != null) {
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
				String url = (String) req.getAttribute("fullhost")
						+ req.getRequestURI();
				Map<String, String> headers = HttpUtil.getOverwriteParams(req);
				OAuthMessage m = new OAuthMessage(req.getMethod(), url, req
						.getParameterMap(), headers);
				OAuthSignatureMethod o = OAuthSignatureMethod.newSigner(c, u);
				String sig = o.getSignature(m);
				headers.put(OAuth.OAUTH_CONSUMER_KEY, c.getKey());
				headers.put(OAuth.OAUTH_SIGNATURE_METHOD, c.getMethod());
				headers.put(OAuth.OAUTH_SIGNATURE, sig);
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
		return;
	}

	@Override
	public void destroy() {
		return;
	}
}
