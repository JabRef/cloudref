package eu.cloudref.models;

public class ResponseRatingReference {

    private boolean confirmed;
    private int overallRating;

    public ResponseRatingReference() {
    }

    public ResponseRatingReference(boolean confirmed, int overallRating) {
        this.confirmed = confirmed;
        this.overallRating = overallRating;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }
}
