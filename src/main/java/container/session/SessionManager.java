/**
 * 
 */
package container.session;

import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import container.dynamicResource.DynamicResourceManager;

/**
 * @author CoolStranger
 * @date 2018年1月24日
 * @time 下午11:57:20
 *
 */
@SuppressWarnings("deprecation")
public class SessionManager implements HttpSessionContext {
	private ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
	private DynamicResourceManager manager;

	public SessionManager(DynamicResourceManager manager) {
		this.manager = manager;
	}

	public Session getSession(String id, boolean create) {
		Session session = null;
		session = sessionMap.get(id);
		if (session == null) {
			if (create) {
				session = createSession();
			}
		} else {
			session.noNew();
			session.update();
		}
		return session;
	}

	public Session createSession() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replaceAll("-", "");
		Session session = new Session(this);
		session.setId(uuid);
		sessionMap.put(uuid, session);
		return session;
	}

	/**
	 * 此方法不会改变session的isNew状态
	 * @param id
	 * @return
	 */
	public Session getSessionWithoutNoNew(String id) {
		Session session = null;
		session = sessionMap.get(id);
		if (session != null) {
			session.update();
		}
		return session;
	}

	public boolean hasSession(String id) {
		return sessionMap.containsKey(id);
	}

	public void removeSession(String id) {
		sessionMap.remove(id);
	}

	public DynamicResourceManager getServletManager() {
		return manager;
	}

	@Override
	public HttpSession getSession(String sessionId) {
		return null;
	}

	@Override
	public Enumeration<String> getIds() {
		return new Enumeration<String>() {

			@Override
			public boolean hasMoreElements() {
				return false;
			}

			@Override
			public String nextElement() {
				return null;
			}
		};
	}
}
