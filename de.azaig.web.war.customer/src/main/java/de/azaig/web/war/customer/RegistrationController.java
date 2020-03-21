package de.azaig.web.war.customer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.Login;
import de.azaig.User;
import de.azaig.web.war.customer.model.RegisrationProblem;
import de.azaig.web.war.customer.model.RegistrationAnswerModel;
import de.azaig.web.war.customer.model.RegistrationModel;

@RestController
public class RegistrationController {
	@PersistenceContext
	private final EntityManager em = null;

	@Transactional
	@PostMapping("register")
	public RegistrationAnswerModel register(@RequestBody RegistrationModel rm) throws NoSuchAlgorithmException {
		RegistrationAnswerModel result = new RegistrationAnswerModel();
		if (!rm.agbok)
			result.problems.add(RegisrationProblem.AGB_MUST_CHECK);
		if (rm.birthday == null) {
			result.problems.add(RegisrationProblem.INVALID_BIRTHDAY);
		}
		if (rm.password == null || rm.password.length() < 8) {
			result.problems.add(RegisrationProblem.PASSWORT_TOO_SHORT);
		}
		if (rm.password2 != null && !rm.password2.equals(rm.password)) {
			result.problems.add(RegisrationProblem.PASSWORD_MISMATCH);
		}
		if (rm.firstname == null || rm.firstname.trim().length() < 1) {
			result.problems.add(RegisrationProblem.MISSING_FIRSTNAME);
		}
		if (rm.lastname == null || rm.lastname.trim().length() < 1) {
			result.problems.add(RegisrationProblem.MISSING_LASTNAME);
		}
		if (rm.nick == null || rm.nick.trim().length() < 5) {
			result.problems.add(RegisrationProblem.NICK_TOO_SHORT);
		}
		Number nicksInUse = em.createQuery("SELECT COUNT(*) FROM " + Login.class.getCanonicalName() + " WHERE nick=:nick", Number.class)
				.setParameter("nick", rm.nick).getSingleResult();
		if (nicksInUse.intValue() > 0) {
			result.problems.add(RegisrationProblem.NICK_ALREADY_IN_USE);
		}
		if (result.problems.isEmpty()) {
			User u = new User();
			u.setBirthday(new Date(rm.birthday.getTime()));
			u.setFirstName(rm.firstname);
			u.setLastName(rm.lastname);
			em.persist(u);
			Login l = new Login();
			l.setNick(rm.nick);
			l.setCaptcha("");
			l.setOwner(u);
			l.setLastLogin(new Date(System.currentTimeMillis()));
			l.setJavascriptStorageCode("");
			SecureRandom sr = new SecureRandom();
			int d = sr.nextInt('Z' - 'A');
			l.setMd5salt(d + "");
			String saltedPassword = d + rm.password;
			MessageDigest md = MessageDigest.getInstance("md5");
			l.setMd5pass(new String(md.digest(saltedPassword.getBytes())));
			em.persist(l);
		}
		return result;
	}
}
