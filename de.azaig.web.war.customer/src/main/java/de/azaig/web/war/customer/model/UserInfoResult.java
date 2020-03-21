package de.azaig.web.war.customer.model;

import java.time.ZonedDateTime;

public class UserInfoResult implements InfoResult {
	public boolean cookieAccepted;
	public ZonedDateTime lastLogin;
	public String firstname, lastname;
	public int messages;
	public boolean admin;
	public boolean locked;
	public boolean agbUpdate;
}
