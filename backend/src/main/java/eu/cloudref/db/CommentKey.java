package eu.cloudref.db;

import java.io.Serializable;

public class CommentKey implements Serializable {

    private String bibtexkey;
    private int id;

    public CommentKey() {
    }

    public CommentKey(String bibtexkey, int id) {
        this.bibtexkey = bibtexkey;
        this.id = id;
    }

    public String getBibtexkey() {
        return bibtexkey;
    }

    public void setBibtexkey(String bibtexkey) {
        this.bibtexkey = bibtexkey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (Integer.valueOf(id).hashCode());
        result = prime * result + ((bibtexkey == null) ? 0 : bibtexkey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        CommentKey other = (CommentKey) obj;

        if (bibtexkey.equals(other.getBibtexkey())) {
            if (id == other.getId()) {
                return true;
            }
        }
        return false;
    }
}
