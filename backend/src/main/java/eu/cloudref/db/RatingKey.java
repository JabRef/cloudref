package eu.cloudref.db;

import java.io.Serializable;

public class RatingKey implements Serializable {

    private User user;
    private String bibtexkey;
    private int id;

    public RatingKey() {
    }

    public RatingKey(User user, String bibtexkey, int id) {
        this.user = user;
        this.bibtexkey = bibtexkey;
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUsername(User user) {
        this.user = user;
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
        result = prime * result + ((user == null) ? 0 : user.hashCode());
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

        RatingKey other = (RatingKey) obj;

        if (bibtexkey.equals(other.getBibtexkey())) {
            if (id == other.getId()) {
                if (user.getName().equals(other.getUser().getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
