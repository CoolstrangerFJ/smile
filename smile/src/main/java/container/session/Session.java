/**
 * 
 */
package container.session;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import launcher.Configuration;
import util.Background;

/**
 * @author CoolStranger
 * @date 2018年1月24日
 * @time 下午9:33:44
 *
 */
@SuppressWarnings("deprecation")
public class Session implements HttpSession, Runnable {
	private static ScheduledThreadPoolExecutor cleanPool = Background.getInstance().getPool();
	private String id;
	private SessionManager manager;
	private HashMap<String, Object> attrs = new HashMap<>(8);
	private long creationTime;
	private long lastAccessedTime;
	private int maxInactiveInterval = Configuration.DEFAULT_SESSION_MAX_INACTIVE_INTERVAL;
	private boolean isNew = true;

	/**
	 * @param sessionManager
	 */
	public Session(SessionManager sessionManager) {
		this.manager = sessionManager;
		this.creationTime = System.currentTimeMillis();
	}

	public void setId(String id) {
		this.id = id;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 * 
	 * @param arg0
	 * 
	 * @return
	 */
	@Override
	public Object getAttribute(String name) {
		return attrs.get(name);
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 * 
	 * @return
	 */
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

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 * 
	 * @return
	 */
	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getId()
	 * 
	 * @return
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 * 
	 * @return
	 */
	@Override
	public long getLastAccessedTime() {
		return this.lastAccessedTime;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 * 
	 * @return
	 */
	@Override
	public int getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 * 
	 * @return
	 */
	@Override
	public ServletContext getServletContext() {
		return manager.getServletManager();
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 * 
	 * @return
	 * 
	 * @deprecated
	 */
	@Override
	public HttpSessionContext getSessionContext() {
		return this.manager;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 * 
	 * @param arg0
	 * 
	 * @return
	 * 
	 * @deprecated
	 */
	@Override
	public Object getValue(String arg0) {
		return getAttribute(arg0);
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 * 
	 * @return
	 * 
	 * @deprecated
	 */
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

	/*
	 *
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	@Override
	public void invalidate() {
		manager.removeSession(id);
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#isNew()
	 * 
	 * @return
	 */
	@Override
	public boolean isNew() {
		return this.isNew;
	}

	public void noNew() {
		isNew = false;
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String,
	 * java.lang.Object)
	 * 
	 * @param arg0
	 * 
	 * @param arg1
	 * 
	 * @deprecated
	 */
	@Override
	public void putValue(String arg0, Object arg1) {
		setAttribute(arg0, arg1);
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 * 
	 * @param arg0
	 */
	@Override
	public void removeAttribute(String name) {
		Object obj = attrs.remove(name);
		if (obj instanceof HttpSessionBindingListener) {
			HttpSessionBindingListener httpSessionBindingListener = (HttpSessionBindingListener) obj;
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, obj);
			httpSessionBindingListener.valueUnbound(event);
		}
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 * 
	 * @param arg0
	 * 
	 * @deprecated
	 */
	@Override
	public void removeValue(String arg0) {
		removeAttribute(arg0);
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String,
	 * java.lang.Object)
	 * 
	 * @param name
	 * 
	 * @param object
	 */
	@Override
	public void setAttribute(String name, Object object) {
		attrs.put(name, object);
		if (object instanceof HttpSessionBindingListener) {
			HttpSessionBindingListener httpSessionBindingListener = (HttpSessionBindingListener) object;
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, object);
			httpSessionBindingListener.valueBound(event);
		}
	}

	/*
	 *
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 * 
	 * @param arg0
	 */
	@Override
	public void setMaxInactiveInterval(int arg0) {
		this.maxInactiveInterval = arg0;
	}

	public void update() {
		lastAccessedTime = System.currentTimeMillis();
		if (maxInactiveInterval >= 0) {
			cleanPool.schedule(this, maxInactiveInterval * 1000 + 10, TimeUnit.MILLISECONDS);
		}
	}

	/*
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		long curTime = System.currentTimeMillis() / 1000;

		if ((curTime - this.lastAccessedTime) >= maxInactiveInterval) {
			invalidate();
			System.out.println("已过时,即将删除：" + this.toString());
		}
	}

	@Override
	public String toString() {
		return "Session [id=" + id + ", manager=" + manager + ", attrs=" + attrs + ", creationTime=" + creationTime
				+ ", lastAccessedTime=" + lastAccessedTime + ", maxInactiveInterval=" + maxInactiveInterval + ", isNew="
				+ isNew + "]";
	}

}
