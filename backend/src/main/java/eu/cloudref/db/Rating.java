package eu.cloudref.db;

import javax.persistence.*;

@IdClass(RatingKey.class)
@Entity
@Table(name = "Rating")
public class Rating {

    @Id
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "username")
    private User user;
    @Column(name="username", updatable = false, insertable = false)
    private String username;
    @Id
    @Column(name = "bibtexkey")
    private String bibtexkey;
    @Id
    @Column(name = "suggestion_id")
    private int id;
    @Column(name = "rating", nullable = false)
    private eu.cloudref.models.Rating.RatingEnum rating;

    public Rating() {}

    public Rating(User user, String bibtexkey, int id, eu.cloudref.models.Rating.RatingEnum rating) {
        this.user = user;
        this.bibtexkey = bibtexkey;
        this.id = id;
        this.rating = rating;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public eu.cloudref.models.Rating.RatingEnum getRating() {
        return rating;
    }

    public void setRating(eu.cloudref.models.Rating.RatingEnum rating) {
        this.rating = rating;
    }
}
