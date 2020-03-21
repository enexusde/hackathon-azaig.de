package de.azaig.web.war.customer.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ProfileOut {
	public Set<ProfileProblem> problems = new LinkedHashSet<>(0);
	public String firstname, lastname;
	public Map<String, String> cars = new LinkedHashMap<>(0);
	public Date birthday;
	public Map<String, Date> logins = new LinkedHashMap<>(1);
}
