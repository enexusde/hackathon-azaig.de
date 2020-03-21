package de.azaig.web.war.manage.categories.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class CategoryTreeModel {
	public Set<Level1CategoryModel> categories = new LinkedHashSet<Level1CategoryModel>();
	public Set<CategoryProblem> problems = new LinkedHashSet<CategoryProblem>();
}
