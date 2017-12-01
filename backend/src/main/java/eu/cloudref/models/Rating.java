package eu.cloudref.models;

public class Rating {

    RatingEnum userRating;

    public Rating() {

    }

    public Rating(RatingEnum userRating) {
        this.userRating = userRating;
    }

    public RatingEnum getUserRating() {
        return userRating;
    }

    public void setUserRating(RatingEnum userRating) {
        this.userRating = userRating;
    }

    public enum RatingEnum {
        POSITIVE("POSITIVE"), NEGATIVE("NEGATIVE");

        private String name;

        private RatingEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}