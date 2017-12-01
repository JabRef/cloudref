package eu.cloudref.models;

public class ResponseRatingSuggestion {

    private MergeInstruction.MergeEnum merged;
    private int overallRating;

    public ResponseRatingSuggestion() {
    }

    public ResponseRatingSuggestion(MergeInstruction.MergeEnum merged, int overallRating) {
        this.merged = merged;
        this.overallRating = overallRating;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }

    public MergeInstruction.MergeEnum getMerged() {
        return merged;
    }

    public void setMerged(MergeInstruction.MergeEnum merged) {
        this.merged = merged;
    }
}
