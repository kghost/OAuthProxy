package name.kghost.oauth.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
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

public class OAuthTokenRequestFilter implements Filter {
	public void doFilter(ServletRequest sreq, ServletResponse sresp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse resp = (HttpServletResponse) sresp;

		ResponseWrapper r = new ResponseWrapper(resp);
		chain.doFilter(req, r);
		if (r.isOk()) {
			String form = r.getData();
			Map<String, String> map = new HashMap<String, String>();
			for (String nvp : form.split("\\&")) {
				int equals = nvp.indexOf('=');
				String name;
				String value;
				if (equals < 0) {
					name = URLDecoder.decode(nvp, "UTF-8");
					value = null;
				} else {
					name = URLDecoder.decode(nvp.substring(0, equals), "UTF-8");
					value = URLDecoder.decode(nvp.substring(equals + 1),
							"UTF-8");
				}
				map.put(name, value);
			}

			OAuthUser p = new OAuthUser();
			if (map.containsKey(name.kghost.oauth.lib.OAuth.OAUTH_TOKEN)
					&& map.containsKey(name.kghost.oauth.lib.OAuth.OAUTH_TOKEN_SECRET)) {
				p.OAuthToken = map.get(name.kghost.oauth.lib.OAuth.OAUTH_TOKEN);
				p.OAuthTokenSecret = map
						.get(name.kghost.oauth.lib.OAuth.OAUTH_TOKEN_SECRET);
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					pm.makePersistent(p);
				} finally {
					pm.close();
				}
			}
		}
	}

	@Override
	public void destroy() {
		return;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		return;
	}
}
