package de.azaig.web.war.offer.ride.model.out;

public class FavPlaceModel {
	public double longitude;
	public double latitude;

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof FavPlaceModel) {
			FavPlaceModel favPlaceModel = (FavPlaceModel) obj;
			return favPlaceModel.longitude == longitude && favPlaceModel.latitude == latitude;
		}
		return false;
	}
}
