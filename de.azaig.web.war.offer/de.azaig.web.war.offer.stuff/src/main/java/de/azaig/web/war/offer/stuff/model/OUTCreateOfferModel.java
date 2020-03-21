package de.azaig.web.war.offer.stuff.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class OUTCreateOfferModel {

	public Map<Integer, String> categories = new LinkedHashMap<Integer, String>();
	public Set<CreateOfferStuffProblem> problems = new LinkedHashSet<CreateOfferStuffProblem>();
	public Set<Integer> images = new LinkedHashSet<Integer>();
}
