package de.azaig.web.war.offer.stuff;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import de.azaig.Offer;
import de.azaig.OfferPhotoJpg;

@Named
public class OfferJpegURLMasquerade {
	@PersistenceContext
	private final EntityManager em = null;

	@Transactional
	public Integer mask(int id) throws NoSuchAlgorithmException {
		OfferPhotoJpg find = em.find(OfferPhotoJpg.class, id);
		Offer offer = find.getOffer();
		MessageDigest md5 = MessageDigest.getInstance("md5");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(id);
		baos.write(offer.getId());
		byte[] digest = md5.digest(baos.toByteArray());
		return new String(digest).hashCode();
	}
}
