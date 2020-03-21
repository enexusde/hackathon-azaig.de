package de.azaig.web.war.offer.stuff;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.Offer;
import de.azaig.OfferPhotoJpg;
import de.azaig.User;
import de.azaig.web.war.session.UserObject;

@RestController
public class CreateOfferImagesController {
	public static class AddImageModel {
		public String jpegDataUrl;
	}

	@PersistenceContext
	private final EntityManager em = null;

	@Inject
	private final OfferJpegURLMasquerade jpegMask = null;

	@GetMapping("offerimage/{maskedId}.jpg")
	@Transactional
	public ResponseEntity<byte[]> getImage(@PathVariable int maskedId, HttpServletRequest req) throws NoSuchAlgorithmException {
		UserObject userObject = UserObject.unwrap(req.getSession());
		if (userObject == null) {
			throw new IllegalStateException("You are unknown.");
		}
		if (userObject.getUserId() == 0) {
			throw new IllegalStateException("Not authenticated!");
		}
		if (!userObject.check(req)) {
			throw new IllegalStateException("Security check failed.");
		}
		if (userObject.getCurrentOffer() == null) {
			throw new IllegalStateException("You have currently no offer.");
		}
		User user = em.find(User.class, userObject.getUserId());
		if (user == null) {
			throw new IllegalStateException("No such user.");
		}
		Offer offer = em.find(Offer.class, userObject.getCurrentOffer());
		if (offer == null) {
			throw new IllegalStateException("No such offer.");
		}
		if (offer.getOfferHolder() != user) {
			if (!user.getIsAdmin()) {
				throw new IllegalStateException("You are not the owner of the offer.");
			}
		}
		OfferPhotoJpg photo = null;
		for (OfferPhotoJpg offerPhotoJpg : offer.getOfferPhotoJpgs()) {
			int mask = jpegMask.mask(offerPhotoJpg.getId());
			if (mask == maskedId) {
				if (photo != null) {
					throw new IllegalStateException("Ambiguous image.");
				}
				photo = offerPhotoJpg;
			}
		}
		if (photo == null) {
			throw new IllegalStateException("No such image.");
		}
		byte[] data = photo.getData();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(data.length);
		headers.setContentType(MediaType.IMAGE_JPEG);
		headers.setETag("\"" + photo.getEtagWithoutQuotas() + "\"");
		return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
	}

	@PostMapping("addStuffImage")
	@Transactional
	public int addImage(@RequestBody AddImageModel model, HttpServletRequest req) throws IOException, NoSuchAlgorithmException {
		UserObject userObject = UserObject.unwrap(req.getSession());
		if (userObject == null) {
			throw new IllegalStateException("Who are you?");
		}
		if (!userObject.check(req)) {
			throw new IllegalStateException("Source check failed!");
		}
		Offer currentOffer = em.find(Offer.class, userObject.getCurrentOffer());
		if (userObject.getCurrentOffer() == null) {
			throw new IllegalStateException("No current offer!");
		}
		if (currentOffer == null) {
			throw new IllegalStateException("No such offer!");
		}

		byte[] jpegData = Base64.getDecoder().decode(model.jpegDataUrl.substring("data:image/jpeg;base64,".length()));
		model.jpegDataUrl = null;
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpegData));
		if (img.getWidth() > 600) {
			throw new RuntimeException("Image too wide.");
		}
		if (img.getHeight() > 600) {
			throw new RuntimeException("Image too high.");
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(img, "jpeg", output);
		OfferPhotoJpg opj = new OfferPhotoJpg();
		opj.setAbuse(false);
		opj.setActive(false);
		opj.setData(output.toByteArray());
		opj.setDescription("");
		opj.setHeight((short) img.getHeight());
		opj.setWidth((short) img.getWidth());
		opj.setReported(false);
		opj.setOffer(currentOffer);
		MessageDigest md = MessageDigest.getInstance("md5");
		opj.setEtagWithoutQuotas(new String(md.digest(output.toByteArray())));
		em.persist(opj);
		return opj.getId();
	}
}
