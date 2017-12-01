package eu.cloudref.models;

public class MergeInstruction {

    MergeEnum mergeInstruction;

    public MergeInstruction() {

    }

    public MergeInstruction(MergeEnum mergeInstruction) {
        this.mergeInstruction = mergeInstruction;
    }

    public MergeEnum getMergeInstruction() {
        return mergeInstruction;
    }

    public void setUserRating(MergeEnum mergeInstruction) {
        this.mergeInstruction = mergeInstruction;
    }

    public enum MergeEnum {
        ACCEPT("ACCEPT"), REJECT("REJECT");

        private String name;

        private MergeEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}