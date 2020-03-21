package de.azaig.web.war.offer.ride;

import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.PositionPlace;
import de.azaig.RideVehicle;
import de.azaig.User;
import de.azaig.web.war.offer.ride.model.in.NewRideModelIn;
import de.azaig.web.war.offer.ride.model.out.FavPlaceModel;
import de.azaig.web.war.offer.ride.model.out.NewRideModelOut;
import de.azaig.web.war.offer.ride.model.out.RideProblem;
import de.azaig.web.war.session.UserObject;

@RestController
public class RideCreatorController {

	@PersistenceContext
	private final EntityManager em = null;

	@PostMapping("newride")
	@Transactional
	public NewRideModelOut work(@RequestBody NewRideModelIn in, HttpServletRequest req) {
		NewRideModelOut out = new NewRideModelOut();
		UserObject userObject = UserObject.unwrap(req.getSession());

		if (userObject == null) {
			out.problems.add(RideProblem.NO_SESSION);
		} else {
			if (!userObject.check(req)) {
				out.problems.add(RideProblem.SECURITY_CHECK_FAILED);
			}
			User user = em.find(User.class, userObject.getUserId());
			if (user == null) {
				out.problems.add(RideProblem.NO_SUCH_USER);
			} else {
				long oneYear = 1_000 * 60 * 60 * 24 * 356;
				if (user.getBirthday().after(new Date(System.currentTimeMillis() - oneYear * 18))) {
					out.problems.add(RideProblem.YOU_ARE_TOO_YOUNG);
				} else {
					if (in.addSerial != null) {
						long alreadyRegistred = em.createQuery("SELECT COUNT(*) FROM " + RideVehicle.class.getCanonicalName() + " WHERE serial=:s", Long.class)
								.setParameter("s", in.addSerial).getSingleResult();
						if (alreadyRegistred > 0) {
							out.problems.add(RideProblem.CAR_ALREADY_REGISTERED);
						} else {
							RideVehicle rv = new RideVehicle();
							rv.setDriver(user);
							rv.setColor(in.addColor);
							rv.setSerial(in.addSerial);
							em.persist(rv);
						}
					}
					if (user.getRideVehicles().isEmpty()) {
						out.problems.add(RideProblem.NO_CARS);
					} else {

						if (in.carSerial == null) {
							out.problems.add(RideProblem.SELECT_CAR);
							for (RideVehicle car : user.getRideVehicles()) {
								out.carSerials.add(car.getSerial());
							}
						} else {
							RideVehicle c = null;
							for (RideVehicle car : user.getRideVehicles()) {
								if (in.carSerial.equals(car.getSerial())) {
									c = car;
								}
							}
							if (c == null) {
								out.problems.add(RideProblem.NOT_YOUR_CAR);
							} else {
								if (in.add != null) {
									List<PositionPlace> resultList = em
											.createQuery("FROM " + PositionPlace.class.getCanonicalName() + " WHERE longitude=:lo AND latitude  = :la",
													PositionPlace.class)
											.setParameter("lo", in.add.longitude).setParameter("la", in.add.latitude).getResultList();
									PositionPlace existingPosition = null;
									for (PositionPlace positionPlace : resultList) {
										existingPosition = positionPlace;
									}
									if (existingPosition == null) {
										existingPosition = new PositionPlace();
										existingPosition.setLongitude(in.add.longitude);
										existingPosition.setLatitude(in.add.latitude);
										existingPosition.setOfficialName("Position");
										em.persist(existingPosition);
										existingPosition.setOfficialName("Position " + existingPosition.getId());
										em.persist(existingPosition);
									}
									user.getPlaces().add(existingPosition);
									em.persist(user);
								}
								for (PositionPlace place : user.getPlaces()) {
									FavPlaceModel e = new FavPlaceModel();
									e.longitude = place.getLongitude();
									e.latitude = place.getLatitude();
									if (e.equals(in.remove)) {
										user.getPlaces().remove(place);
										em.persist(user);
									} else {
										out.favouritePlaces.add(e);
									}
								}
								if (in.startX == null || in.startY == null) {
									out.problems.add(RideProblem.MISSING_START_POINT);
								} else {

								}
							}
						}
					}
				}
			}
		}
		return out;
	}
}
