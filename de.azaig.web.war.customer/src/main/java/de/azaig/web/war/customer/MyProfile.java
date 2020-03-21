package de.azaig.web.war.customer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.Login;
import de.azaig.RideVehicle;
import de.azaig.User;
import de.azaig.web.war.capcha.CaptchaService;
import de.azaig.web.war.customer.model.ProfileOut;
import de.azaig.web.war.customer.model.ProfileProblem;
import de.azaig.web.war.customer.model.ProfileUpdateIn;
import de.azaig.web.war.customer.model.ProfileUpdateOut;
import de.azaig.web.war.session.UserObject;

@RestController
public class MyProfile {

	@Inject
	private final CaptchaService service = null;
	@PersistenceContext
	private final EntityManager em = null;

	@Transactional
	@GetMapping("profile")
	public ProfileOut info(HttpServletRequest req) throws NoSuchAlgorithmException {
		ProfileOut result = new ProfileOut();
		HttpSession session = req.getSession(false);
		if (session == null) {
			result.problems.add(ProfileProblem.MISSING_SESSION);
		} else {
			if (result.problems.isEmpty()) {
				UserObject userObj = UserObject.unwrap(session);
				if (userObj == null) {
					result.problems.add(ProfileProblem.MISSING_AUTHENTICATION);
				} else {
					int userId = userObj.getUserId();
					User user = em.find(User.class, userId);
					if (user == null) {
						result.problems.add(ProfileProblem.UNABLE_TO_FIND_USER);
					} else {
						if (!userObj.check(req)) {
							result.problems.add(ProfileProblem.SECURITY_CHECK_FAILED);
						}
					}
					if (result.problems.isEmpty()) {
						result.birthday = user.getBirthday();
						result.firstname = user.getFirstName();
						result.lastname = user.getLastName();
						for (RideVehicle rideVehicle : user.getRideVehicles()) {
							result.cars.put(rideVehicle.getSerial(), rideVehicle.getColor());
						}
						for (Login login : user.getLogins()) {
							result.logins.put(login.getNick(), login.getLastLogin());
						}
					}
				}
			}
		}
		return result;
	}

	@Transactional
	@PostMapping("profile")
	public ProfileUpdateOut info(HttpServletRequest req, @RequestBody ProfileUpdateIn in) throws NoSuchAlgorithmException {
		ProfileUpdateOut result = new ProfileUpdateOut();
		HttpSession session = req.getSession(false);
		if (in.birthday.after(new Date())) {
			result.problems.add(ProfileProblem.UNBORN_NOT_ALLOWED);
		} else if (in.birthday.before(new Date(0, 0, 1))) {
			result.problems.add(ProfileProblem.AGE_IMPROBABLE);
		}
		if (in.firstname == null || in.firstname.length() < 2) {
			result.problems.add(ProfileProblem.FIRSTNAME_TOO_SMALL);
		}
		if (in.lastname == null || in.lastname.length() < 2) {
			result.problems.add(ProfileProblem.LASTNAME_TOO_SMALL);
		}
		if (session == null) {
			result.problems.add(ProfileProblem.MISSING_SESSION);
		} else {
			if (result.problems.isEmpty()) {
				UserObject userObj = UserObject.unwrap(session);
				if (userObj == null) {
					result.problems.add(ProfileProblem.MISSING_AUTHENTICATION);
				} else {
					if (!userObj.check(req)) {
						result.problems.add(ProfileProblem.SECURITY_CHECK_FAILED);
					}
					User user = em.find(User.class, userObj.getUserId());
					boolean birthdayChanged = !user.getBirthday().equals(in.birthday);
					boolean firstnameChanged = !user.getLastName().equals(in.lastname);
					boolean lastnameChanged = !user.getFirstName().equals(in.firstname);
					if (!birthdayChanged & !lastnameChanged & !firstnameChanged) {
						result.problems.add(ProfileProblem.NOT_CHANGED_ANYTHING);
					} else {

						if (birthdayChanged) {
							user.setBirthday(in.birthday);
						}
						if (firstnameChanged) {
							user.setFirstName(in.firstname);
						}
						if (lastnameChanged) {
							user.setLastName(in.lastname);
						}
						em.persist(user);
					}
				}
			}
		}
		return result;
	}
}
