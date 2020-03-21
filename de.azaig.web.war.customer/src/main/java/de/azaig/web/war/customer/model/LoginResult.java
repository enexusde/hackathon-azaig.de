package de.azaig.web.war.customer.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class LoginResult {
	public Set<LoginProblems> problems = new LinkedHashSet<LoginProblems>();
	public String firstname, lastname;
}
