package de.azaig.web.war.offer.stuff.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class INCreateOfferModel {
	public Integer cat1;
	public Integer cat2;
	public Integer cat3;
	public Set<Integer> pictures = new LinkedHashSet<Integer>();
	public boolean hasPictures = true;
	public String text;
	public Boolean business;
	public Boolean confirmed;
}
