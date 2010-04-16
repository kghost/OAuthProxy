package name.kghost.oauth.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerFactoryConfigurationError;

import name.kghost.oauth.config.persistent.OAuthConsumer;
import name.kghost.oauth.config.persistent.OAuthUser;
import name.kghost.oauth.filter.PMF;

public class Config extends HttpServlet {
	private static final long serialVersionUID = 8927347113229457538L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String action = req.getParameter("action");
		if (action != null) {
			if (action.equals("ConsumerAdd")) {
				String key = req.getParameter("key");
				String method = req.getParameter("method");
				String secret = req.getParameter("secret");
				if (key != null && method != null && secret != null) {
					PersistenceManager pm = PMF.get().getPersistenceManager();
					try {
						pm
								.makePersistent(new OAuthConsumer(key, method,
										secret));
						resp.sendRedirect(req.getRequestURI());
					} finally {
						pm.close();
					}
				}
			} else if (action.equals("ConsumerDel")) {
				String key = req.getParameter("key");
				if (key != null) {
					PersistenceManager pm = PMF.get().getPersistenceManager();
					Transaction tx = pm.currentTransaction();
					try {
						tx.begin();
						OAuthConsumer e = pm.getObjectById(OAuthConsumer.class,
								key);
						pm.deletePersistent(e);
						tx.commit();
						resp.sendRedirect(req.getRequestURI());
					} finally {
						if (tx.isActive())
							tx.rollback();
						pm.close();
					}
				}
			} else if (action.equals("UserDel")) {
				String token = req.getParameter("token");
				if (token != null) {
					PersistenceManager pm = PMF.get().getPersistenceManager();
					Transaction tx = pm.currentTransaction();
					try {
						tx.begin();
						OAuthUser e = pm.getObjectById(OAuthUser.class, token);
						pm.deletePersistent(e);
						tx.commit();
						resp.sendRedirect(req.getRequestURI());
					} finally {
						if (tx.isActive())
							tx.rollback();
						pm.close();
					}
				}
			}
		}

		// no action or unknown action, list all
		OAuthConfig c = new OAuthConfig();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery("select from "
					+ OAuthConsumer.class.getName());
			try {
				List<OAuthConsumer> results = (List<OAuthConsumer>) query
						.execute();
				for (OAuthConsumer e : results) {
					e = pm.getObjectById(OAuthConsumer.class, e.getKey());
					c.getConsumers().add(e);
				}
			} finally {
				query.closeAll();
			}
			query = pm.newQuery(OAuthUser.class);
			try {
				List<OAuthUser> results = (List<OAuthUser>) query.execute();
				for (OAuthUser e : results) {
					e = pm.getObjectById(OAuthUser.class, e.getToken());
					c.getUsers().add(e);
				}
			} finally {
				query.closeAll();
			}

		} finally {
			pm.close();
		}

		OutputStream output = resp.getOutputStream();
		try {
			resp.setContentType("application/xml");
			String proc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<?xml-stylesheet type=\"text/xsl\" href=\"/xsl/config.xsl\"?>\n";
			output.write(proc.getBytes("UTF-8"));
			Marshaller u = JAXBContext.newInstance(OAuthConfig.class)
					.createMarshaller();
			u.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			u.marshal(new ObjectFactory().createOAuthConfig(c), output);
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} finally {
			output.flush();
		}
	}
}
