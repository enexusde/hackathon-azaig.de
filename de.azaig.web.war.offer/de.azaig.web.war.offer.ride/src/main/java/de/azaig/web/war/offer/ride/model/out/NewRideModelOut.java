package de.azaig.web.war.offer.ride.model.out;

import java.util.LinkedHashSet;
import java.util.Set;

public class NewRideModelOut {
	public Set<RideProblem> problems = new LinkedHashSet<>();
	public Set<String> carSerials = new LinkedHashSet<String>();
	public Set<FavPlaceModel> favouritePlaces = new LinkedHashSet<FavPlaceModel>();
}
