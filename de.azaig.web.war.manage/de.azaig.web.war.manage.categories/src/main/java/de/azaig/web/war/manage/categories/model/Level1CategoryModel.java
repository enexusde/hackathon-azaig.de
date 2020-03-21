package de.azaig.web.war.manage.categories.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Level1CategoryModel {
	public String name;
	public Set<Level2CategoryModel> categories = new LinkedHashSet<Level2CategoryModel>();
	public int id;
}
