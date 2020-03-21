package de.azaig.web.welcome;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.web.welcome.update.model.UpdateModel;

@RestController
public class NewestContributions {

	@PersistenceContext
	private final EntityManager em = null;

	@GetMapping("update")
	public @ResponseBody UpdateModel getModel() {
		// FROM(囗Offer.class).AS(O->WHERE(O.editing.))
		return new UpdateModel();
	}
}
