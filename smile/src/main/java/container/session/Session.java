/**
 * 
 */
package container.session;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import launcher.Configuration;
import util.cleaner.CleanTask;
import util.cleaner.Cleanable;

/**
 * @author CoolStranger
 * @date 2018年1月24日
 * @time 下午9:33:44
 *
 */
@SuppressWarnings("deprecation")
public class Session implements HttpSession, Cleanable {
	private String id;
	private SessionManager manager;
	private HashMap<String, Object> attrs = new HashMap<>(8);
	private long creationTime;
	private long lastAccessedTime;
	private int maxInactiveInterval = Configuration.DEFAULT_SESSION_MAX_INACTIVE_INTERVAL;

	private CleanTask cleanTask;
	private boolean isNew = true;

	/**
	 * @param sessionManager
	 */
	public Session(SessionManager sessionManager) {
		this.manager = sessionManager;
		this.creationTime = System.currentTimeMillis();
		this.cleanTask = new CleanTask().setCleanable(this).setTimeOut(maxInactiveInterval * 1000);
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Object getAttribute(String name) {
		return attrs.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		final Iterator<String> iterator = attrs.keySet().iterator();

		return new Enumeration<String>() {

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
	}

	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public long getLastAccessedTime() {
		return this.lastAccessedTime;
	}

	@Override
	public int getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		return manager.getServletManager();
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return this.manager;
	}

	@Override
	public Object getValue(String arg0) {
		return getAttribute(arg0);
	}

	@Override
	public String[] getValueNames() {
		Set<String> keySet = attrs.keySet();
		int size = keySet.size();
		String[] names = new String[size];
		int index = 0;
		for (String keyName : keySet) {
			names[index++] = keyName;
		}
		return names;
	}

	@Override
	public void invalidate() {
		manager.removeSession(id);
	}

	@Override
	public boolean isNew() {
		return this.isNew;
	}

	public void noNew() {
		isNew = false;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		setAttribute(arg0, arg1);
	}

	@Override
	public void removeAttribute(String name) {
		Object obj = attrs.remove(name);
		if (obj instanceof HttpSessionBindingListener) {
			HttpSessionBindingListener httpSessionBindingListener = (HttpSessionBindingListener) obj;
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, obj);
			httpSessionBindingListener.valueUnbound(event);
		}
	}

	@Override
	public void removeValue(String arg0) {
		removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String name, Object object) {
		attrs.put(name, object);
		if (object instanceof HttpSessionBindingListener) {
			HttpSessionBindingListener httpSessionBindingListener = (HttpSessionBindingListener) object;
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, object);
			httpSessionBindingListener.valueBound(event);
		}
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		this.maxInactiveInterval = arg0;
		cleanTask.setTimeOut(maxInactiveInterval * 1000);
	}

	public void update() {
		lastAccessedTime = System.currentTimeMillis();
		if (maxInactiveInterval >= 0) {
			cheakLater(cleanTask, maxInactiveInterval * 1000 + 10);
		}
	}

	@Override
	public long getLastUsedTime() {
		return lastAccessedTime;
	}

	@Override
	public void clean() {
		invalidate();
		System.out.println("已过时,即将删除：" + this.toString());
	}

	@Override
	public String toString() {
		return "Session [id=" + id + ", manager=" + manager + ", attrs=" + attrs + ", creationTime=" + creationTime
				+ ", lastAccessedTime=" + lastAccessedTime + ", maxInactiveInterval=" + maxInactiveInterval + ", isNew="
				+ isNew + "]";
	}

}
