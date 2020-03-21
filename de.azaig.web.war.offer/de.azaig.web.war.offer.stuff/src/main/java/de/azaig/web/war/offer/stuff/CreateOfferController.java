package de.azaig.web.war.offer.stuff;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.Level1Category;
import de.azaig.Level2Category;
import de.azaig.Level3Category;
import de.azaig.Offer;
import de.azaig.OfferPhotoJpg;
import de.azaig.User;
import de.azaig.web.war.offer.stuff.model.CreateOfferStuffProblem;
import de.azaig.web.war.offer.stuff.model.INCreateOfferModel;
import de.azaig.web.war.offer.stuff.model.OUTCreateOfferModel;
import de.azaig.web.war.session.UserObject;

@RestController
public class CreateOfferController {
	@PersistenceContext
	private final EntityManager em = null;

	@Inject
	private final OfferJpegURLMasquerade jpegMask = null;

	@PostMapping("createOfferStuff")
	@Transactional
	public OUTCreateOfferModel createOfferStuff(@RequestBody INCreateOfferModel rb, HttpServletRequest req) throws NoSuchAlgorithmException {
		OUTCreateOfferModel out = new OUTCreateOfferModel();
		UserObject userObject = UserObject.unwrap(req.getSession());
		if (userObject == null) {
			out.problems.add(CreateOfferStuffProblem.PLEASE_LOGIN);
		}
		if (rb.cat1 == null) {
			out.problems.add(CreateOfferStuffProblem.PLEASE_CHOOSE_CATEGORY1);
			for (Level1Category l : em.createQuery("FROM " + Level1Category.class.getCanonicalName(), Level1Category.class).getResultList()) {
				out.categories.put(l.getId(), l.getName());
			}
		} else {
			if (rb.cat2 == null) {
				out.problems.add(CreateOfferStuffProblem.PLEASE_CHOOSE_CATEGORY2);
				for (Level2Category l : em
						.createQuery("FROM " + Level2Category.class.getCanonicalName() + " WHERE level1Cat.id=" + rb.cat1, Level2Category.class)
						.getResultList()) {
					out.categories.put(l.getId(), l.getName());
				}
			} else {
				if (rb.cat3 == null) {
					out.problems.add(CreateOfferStuffProblem.PLEASE_CHOOSE_CATEGORY3);
					for (Level3Category l : em
							.createQuery("FROM " + Level3Category.class.getCanonicalName() + " WHERE level2Cat.id=" + rb.cat2, Level3Category.class)
							.getResultList()) {
						out.categories.put(l.getId(), l.getName());
					}
				} else {
					if (rb.business == null) {
						out.problems.add(CreateOfferStuffProblem.SET_BUSINESS_OR_NOT);
					} else {
						// all categories set.
						if (out.problems.isEmpty()) {
							if (rb.hasPictures && rb.pictures.isEmpty()) {

								if (userObject.getCurrentOffer() == null) {
									Offer o = new Offer();
									o.setBusiness(false);
									o.setOfferText("");
									o.setEditing(true);
									o.setCategory(em.find(Level3Category.class, rb.cat3));
									o.setOfferHolder(em.find(User.class, userObject.getUserId()));
									o.setCreated(new Timestamp(System.currentTimeMillis()));
									em.persist(o);
									userObject.setCurrentOffer(o.getId());
								}
								out.problems.add(CreateOfferStuffProblem.ADD_PICTURES);
							} else {
								if (rb.text == null) {
									out.problems.add(CreateOfferStuffProblem.ADD_TEXT);
								} else {
									if (rb.text.length() < 20) {
										out.problems.add(CreateOfferStuffProblem.TEXT_TOO_SMALL);
									}
									if (rb.text.length() > 2048) {
										out.problems.add(CreateOfferStuffProblem.TEXT_TOO_LONG);
									}
								}
							}
						}
					}
				}
			}
		}
		if (userObject != null && userObject.getCurrentOffer() != null) {
			Offer o = em.find(Offer.class, userObject.getCurrentOffer());
			if (o != null) {
				for (OfferPhotoJpg offerPhotoJpg : o.getOfferPhotoJpgs()) {
					out.images.add(jpegMask.mask(offerPhotoJpg.getId()));
				}
			}
		}

		if (out.problems.isEmpty()) {
			if (rb.confirmed == null) {
				out.problems.add(CreateOfferStuffProblem.PLEASE_CHECK_AND_CONFIRM);
			} else {
				Offer offer = em.find(Offer.class, userObject.getCurrentOffer());
				if (rb.confirmed == true) {
					offer.setEditing(false);
					em.persist(offer);
					out.problems.add(CreateOfferStuffProblem.PUBLISHED);
				} else {
					em.remove(offer);
					userObject.removeCurrentOffer();
					out.problems.add(CreateOfferStuffProblem.DROPED);
				}
			}
		}

		return out;
	}

}
