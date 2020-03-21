package de.azaig.web.war.manage.categories;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.azaig.Level1Category;
import de.azaig.Level2Category;
import de.azaig.Level3Category;
import de.azaig.web.war.manage.categories.model.AddCategoryModel;
import de.azaig.web.war.manage.categories.model.AddCategoryReturnModel;
import de.azaig.web.war.manage.categories.model.CategoryProblem;
import de.azaig.web.war.manage.categories.model.CategoryTreeModel;
import de.azaig.web.war.manage.categories.model.DelCategoryProblem;
import de.azaig.web.war.manage.categories.model.DelCategoryResult;
import de.azaig.web.war.manage.categories.model.Level1CategoryModel;
import de.azaig.web.war.manage.categories.model.Level2CategoryModel;
import de.azaig.web.war.manage.categories.model.Level3CategoryModel;
import de.azaig.web.war.session.UserObject;

@RestController
public class CategoryController {

	@PersistenceContext
	private final EntityManager em = null;

	@Inject
	private final HttpServletRequest req = null;

	@GetMapping("allcategories")
	@Transactional
	public @ResponseBody CategoryTreeModel getModel() {
		CategoryTreeModel ctm = new CategoryTreeModel();
		UserObject uo = UserObject.unwrap(req.getSession());
		if (uo == null) {
			ctm.problems.add(CategoryProblem.ILLEGAL_ACCESS);
		}
		if (ctm.problems.isEmpty()) {
			if (ctm.problems.isEmpty()) {
				for (Level1Category level1Category : em.createQuery("FROM " + Level1Category.class.getCanonicalName(), Level1Category.class).getResultList()) {
					Level1CategoryModel l1m = new Level1CategoryModel();
					l1m.name = level1Category.getName();
					l1m.id = level1Category.getId();
					for (Level2Category level2Category : level1Category.getLevel2Categorys()) {
						Level2CategoryModel l2m = new Level2CategoryModel();
						l2m.name = level2Category.getName();
						l2m.id = level2Category.getId();
						for (Level3Category l3c : level2Category.getLevel3Categorys()) {
							Level3CategoryModel l3m = new Level3CategoryModel();
							l3m.name = l3c.getName();
							l3m.id = l3c.getId();
							l2m.categories.add(l3m);
						}
						l1m.categories.add(l2m);
					}
					ctm.categories.add(l1m);
				}
			}
		}
		return ctm;
	}

	@PostMapping("addcategoryL1")
	@Transactional
	public AddCategoryReturnModel addCategoryL1(@RequestBody AddCategoryModel m) {
		UserObject uo = UserObject.unwrap(req.getSession());
		AddCategoryReturnModel result = new AddCategoryReturnModel();
		if (uo == null) {
			result.problems.add(CategoryProblem.NOT_AUTHENTICATED);
		}
		m.name = m.name.trim();
		if (m.name.length() < 3) {
			result.problems.add(CategoryProblem.NAME_TOO_SMALL);
		}
		if (m.name.length() > 20) {
			result.problems.add(CategoryProblem.NAME_TOO_LONG);
		}
		for (Level1Category level1Category : em.createQuery("FROM " + Level1Category.class.getCanonicalName(), Level1Category.class).getResultList()) {
			if (level1Category.getName().equals(m.name)) {
				result.problems.add(CategoryProblem.NAME_ALREADY_IN_USE);
			}
		}
		if (result.problems.isEmpty()) {
			if (!uo.isAdmin(em)) {
				result.problems.add(CategoryProblem.ILLEGAL_ACCESS);
			}
			if (result.problems.isEmpty()) {

				if (result.problems.isEmpty()) {

					if (result.problems.isEmpty()) {
						if (result.problems.isEmpty()) {
							Level1Category newCategory = new Level1Category();
							newCategory.setName(m.name);
							em.persist(newCategory);
						}
					}
				}
			}
		}
		return result;
	}

	@PostMapping("addcategoryL2")
	@Transactional
	public AddCategoryReturnModel addCategoryL2(@RequestBody AddCategoryModel m) {
		UserObject uo = UserObject.unwrap(req.getSession());
		AddCategoryReturnModel result = new AddCategoryReturnModel();
		if (uo == null) {
			result.problems.add(CategoryProblem.NOT_AUTHENTICATED);
		}
		m.name = m.name.trim();
		if (m.name.length() < 3) {
			result.problems.add(CategoryProblem.NAME_TOO_SMALL);
		}
		if (m.name.length() > 20) {
			result.problems.add(CategoryProblem.NAME_TOO_LONG);
		}
		for (Level2Category level2Category : em
				.createQuery("FROM " + Level2Category.class.getCanonicalName() + " WHERE level1Cat.id=:l1cid", Level2Category.class)
				.setParameter("l1cid", m.parent).getResultList()) {
			if (level2Category.getName().equals(m.name)) {
				result.problems.add(CategoryProblem.NAME_ALREADY_IN_USE);
			}
		}

		Level1Category parentCat = em.find(Level1Category.class, m.parent);
		if (parentCat == null) {
			result.problems.add(CategoryProblem.NO_SUCH_PARENT_CATEGORY);
		}

		if (result.problems.isEmpty()) {
			if (!uo.isAdmin(em)) {
				result.problems.add(CategoryProblem.ILLEGAL_ACCESS);
			}
			if (result.problems.isEmpty()) {

				if (result.problems.isEmpty()) {

					if (result.problems.isEmpty()) {
						if (result.problems.isEmpty()) {
							Level2Category newCategory = new Level2Category();
							newCategory.setName(m.name);
							newCategory.setLevel1Cat(parentCat);
							em.persist(newCategory);
						}
					}
				}
			}
		}
		return result;
	}

	@GetMapping("delCategoryL3")
	@Transactional
	public DelCategoryResult delCategoryL3(int id) {
		UserObject uo = UserObject.unwrap(req.getSession());
		DelCategoryResult result = new DelCategoryResult();
		if (uo == null) {
			result.problems.add(DelCategoryProblem.NOT_AUTHENTICATED);
		}
		if (!uo.isAdmin(em)) {
			result.problems.add(DelCategoryProblem.ILLEGAL_ACCESS);
		}
		if (result.problems.isEmpty()) {
			Level3Category cat = em.find(Level3Category.class, id);
			if (!cat.getOffers().isEmpty()) {
				result.problems.add(DelCategoryProblem.REMAINING_OFFERS);
			} else if (!cat.getRequests().isEmpty()) {
				result.problems.add(DelCategoryProblem.REMAINING_REQUESTS);
			} else {
				em.remove(cat);
			}
		}
		return result;
	}

	@GetMapping("delCategoryL1")
	@Transactional
	public DelCategoryResult delCategoryL1(int id) {
		UserObject uo = UserObject.unwrap(req.getSession());
		DelCategoryResult result = new DelCategoryResult();
		if (uo == null) {
			result.problems.add(DelCategoryProblem.NOT_AUTHENTICATED);
		}
		if (!uo.isAdmin(em)) {
			result.problems.add(DelCategoryProblem.ILLEGAL_ACCESS);
		}
		if (result.problems.isEmpty()) {
			Level1Category cat = em.find(Level1Category.class, id);
			if (!cat.getLevel2Categorys().isEmpty()) {
				result.problems.add(DelCategoryProblem.REMAINING_SUBCATEGORIES);
			} else {
				em.remove(cat);
			}
		}
		return result;
	}

	@GetMapping("delCategoryL2")
	@Transactional
	public DelCategoryResult delCategoryL2(int id) {
		UserObject uo = UserObject.unwrap(req.getSession());
		DelCategoryResult result = new DelCategoryResult();
		if (uo == null) {
			result.problems.add(DelCategoryProblem.NOT_AUTHENTICATED);
		}
		if (!uo.isAdmin(em)) {
			result.problems.add(DelCategoryProblem.ILLEGAL_ACCESS);
		}
		if (result.problems.isEmpty()) {
			Level2Category cat = em.find(Level2Category.class, id);
			if (!cat.getLevel3Categorys().isEmpty()) {
				result.problems.add(DelCategoryProblem.REMAINING_SUBCATEGORIES);
			} else {
				em.remove(cat);
			}
		}
		return result;
	}

	@PostMapping("addcategoryL3")
	@Transactional
	public AddCategoryReturnModel addCategoryL3(@RequestBody AddCategoryModel m) {
		UserObject uo = UserObject.unwrap(req.getSession());
		AddCategoryReturnModel result = new AddCategoryReturnModel();
		if (uo == null) {
			result.problems.add(CategoryProblem.NOT_AUTHENTICATED);
		}
		m.name = m.name.trim();
		if (m.name.length() < 3) {
			result.problems.add(CategoryProblem.NAME_TOO_SMALL);
		}
		if (m.name.length() > 20) {
			result.problems.add(CategoryProblem.NAME_TOO_LONG);
		}
		for (Level3Category level3Category : em
				.createQuery("FROM " + Level3Category.class.getCanonicalName() + " WHERE level2Cat.id=:l2cid", Level3Category.class)
				.setParameter("l2cid", m.parent).getResultList()) {
			if (level3Category.getName().equals(m.name)) {
				result.problems.add(CategoryProblem.NAME_ALREADY_IN_USE);
			}
		}
		Level2Category parentCat = em.find(Level2Category.class, m.parent);
		if (parentCat == null) {
			result.problems.add(CategoryProblem.NO_SUCH_PARENT_CATEGORY);
		}
		if (result.problems.isEmpty()) {
			if (!uo.isAdmin(em)) {
				result.problems.add(CategoryProblem.ILLEGAL_ACCESS);
			}
			if (result.problems.isEmpty()) {
				if (result.problems.isEmpty()) {
					if (result.problems.isEmpty()) {
						if (result.problems.isEmpty()) {
							Level3Category newCategory = new Level3Category();
							newCategory.setName(m.name);
							newCategory.setLevel2Cat(parentCat);
							em.persist(newCategory);
						}
					}
				}
			}
		}
		return result;
	}
}