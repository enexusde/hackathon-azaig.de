package de.azaig.web.war.manage.categories.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Level2CategoryModel {
	public String name;
	public Set<Level3CategoryModel> categories = new LinkedHashSet<Level3CategoryModel>();
	public int id;
}
