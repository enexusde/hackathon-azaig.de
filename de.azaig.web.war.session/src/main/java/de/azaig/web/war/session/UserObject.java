package de.azaig.web.war.session;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import de.azaig.User;

public class UserObject implements Serializable {
	private String browsersIp, agent;
	private int userId;
	private long timeout;
	private Integer currentOffer;

	public UserObject(HttpServletRequest req, Integer id) {
		browsersIp = req.getRemoteAddr();
		userId = id;
		agent = req.getHeader("User-Agent");
		timeout = 0;
		notifyActivity();
	}

	public boolean check(HttpServletRequest r) {
		return r.getRemoteAddr().equals(browsersIp) && agent.equals(r.getHeader("User-Agent")) && timeout < System.currentTimeMillis();
	}

	public void notifyActivity() {
		timeout = System.currentTimeMillis();
	}

	public int getUserId() {
		return userId;
	}

	public static UserObject unwrap(HttpSession sess) {
		Object attribute = sess.getAttribute("obj");
		if (attribute instanceof UserObject) {
			UserObject userObject = (UserObject) attribute;
			return userObject;
		}
		return null;
	}

	public boolean isAdmin(EntityManager em) {
		return em.find(User.class, userId).getIsAdmin();
	}

	public Integer getCurrentOffer() {
		return currentOffer;
	}

	public void setCurrentOffer(int currentOffer) {
		this.currentOffer = currentOffer;
	}

	public void removeCurrentOffer() {
		this.currentOffer = null;
	}
}
