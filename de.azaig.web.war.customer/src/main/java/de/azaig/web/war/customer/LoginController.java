package de.azaig.web.war.customer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.Login;
import de.azaig.User;
import de.azaig.web.war.capcha.CaptchaService;
import de.azaig.web.war.customer.model.InfoResult;
import de.azaig.web.war.customer.model.LoginModel;
import de.azaig.web.war.customer.model.LoginProblems;
import de.azaig.web.war.customer.model.LoginResult;
import de.azaig.web.war.customer.model.UnauthenticatedInfoResult;
import de.azaig.web.war.customer.model.UserInfoResult;
import de.azaig.web.war.session.UserObject;

@RestController
public class LoginController {

	private static final String COOKIE_ACCEPTED = "cookieAccepted";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOG = Logger.getLogger(LoginController.class.getCanonicalName());

	@PersistenceContext
	private final EntityManager em = null;

	@Inject
	private final CaptchaService service = null;

	@PostMapping("acceptCookie")
	public void acceptCookie(HttpServletRequest req) {
		LOG.info("User accepted cookie: " + req.getRemoteAddr() + "!");
		req.getSession().setAttribute(COOKIE_ACCEPTED, "yes");
	}

	@PostMapping("info")
	@Transactional
	public InfoResult info(HttpServletRequest req) throws NoSuchAlgorithmException {
		UserObject unwrap = UserObject.unwrap(req.getSession());
		if (unwrap != null && unwrap.check(req)) {
			UserInfoResult userInfoResult = new UserInfoResult();
			int userId = unwrap.getUserId();
			User user = em.find(User.class, userId);
			if (user == null) {
				req.getSession(true);
				LOG.severe("User has been removed. logout!");
				return null;
			}
			userInfoResult.firstname = user.getFirstName();
			userInfoResult.lastname = user.getLastName();
			userInfoResult.admin = user.getIsAdmin();
			Set<Login> logins = user.getLogins();
			Date lastLogin = new Date(0l);
			for (Login login : logins) {
				if (login.getLastLogin().after(lastLogin)) {
					lastLogin = login.getLastLogin();
				}
			}
			userInfoResult.messages = user.getRequestorRideTrackings().size() + user.getRequests().size();

			Locale clientLocale = req.getLocale();
			Calendar calendar = Calendar.getInstance(clientLocale);
			TimeZone clientTimeZone = calendar.getTimeZone();
			userInfoResult.lastLogin = ZonedDateTime.of(lastLogin.toLocalDate(), LocalTime.of(0, 0), clientTimeZone.toZoneId());
			userInfoResult.cookieAccepted = req.getSession().getAttribute(COOKIE_ACCEPTED) != null;
			return userInfoResult;
		} else {
			UnauthenticatedInfoResult unauthenticatedInfoResult = new UnauthenticatedInfoResult();
			unauthenticatedInfoResult.cookieAccepted = req.getSession().getAttribute(COOKIE_ACCEPTED) != null;
			return unauthenticatedInfoResult;
		}
	}

	@PostMapping("login")
	@Transactional
	public LoginResult login(@RequestBody LoginModel lm, HttpServletRequest req) throws NoSuchAlgorithmException {
		LOG.info("Logging in " + lm.username);
		String requiredCapcha = service.generateCaptcha(lm.sec, req);
		LoginResult loginResult = new LoginResult();
		if (lm.password.length() < 8) {
			loginResult.problems.add(LoginProblems.PASSWORD_TOO_SHORT);
		}
		if (lm.username.length() < 8) {
			loginResult.problems.add(LoginProblems.USERNAME_TOO_SHORT);
		}
		if (!requiredCapcha.equals(lm.capcha)) {
			loginResult.problems.add(LoginProblems.ILLEGAL_CAPCHA_LOGIN_UNCHECKED);
		}
		if (loginResult.problems.isEmpty()) {
			try {
				String salt = em.createQuery("SELECT md5salt FROM " + Login.class.getCanonicalName() + " WHERE nick=:nick", String.class)
						.setParameter("nick", lm.username).getSingleResult();
				if (salt == null) {
					loginResult.problems.add(LoginProblems.NO_SUCH_USER);
				} else {
					Login l = findLogin(lm, salt);
					if (l == null) {
						loginResult.problems.add(LoginProblems.WRONG_PASSWD);
					} else {
						req.getSession().setAttribute("obj", new UserObject(req, l.getOwner().getId()));
						loginResult.lastname = l.getOwner().getLastName();
						loginResult.firstname = l.getOwner().getFirstName();
						LOG.info("Login LoginID: " + l.getId());
					}
				}
			} catch (NoResultException e) {
				loginResult.problems.add(LoginProblems.WRONG_PASSWD);
			}
		}
		return loginResult;
	}

	private Login findLogin(LoginModel lm, String salt) throws NoSuchAlgorithmException {
		String saltedPassword = salt + lm.password;
		MessageDigest md5 = MessageDigest.getInstance("md5");
		String md5saltedpassword = new String(md5.digest(saltedPassword.getBytes()));
		Login l;
		try {
			l = em.createQuery("FROM " + Login.class.getCanonicalName() + " WHERE nick=:nick AND md5pass=:md5pass", Login.class)
					.setParameter("nick", lm.username).setParameter("md5pass", md5saltedpassword).getSingleResult();
		} catch (NoResultException e) {
			l = null;
		}
		return l;
	}
}
