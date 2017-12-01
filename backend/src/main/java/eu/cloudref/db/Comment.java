package eu.cloudref.db;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.cloudref.CalendarDeserializer;
import eu.cloudref.CalendarSerializer;

import javax.persistence.*;
import java.util.Calendar;

@IdClass(CommentKey.class)
@Entity
@Table(name = "Comment")
@Access(AccessType.FIELD)
public class Comment {

    @Id
    @Column(name = "id")
    private int id;
    @Id
    @Column(name = "bibtexkey")
    private String bibtexkey;
    @Column(name="author", nullable = false)
    private String author;
    @Column(name = "publish", nullable = false)
    private boolean publish;
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "real_page_number", nullable = false)
    private String page;
    @Column(name = "sequential_page_number", nullable = false)
    private int pageNumber;
    @Column(name = "creation_date", nullable = false)
    @JsonSerialize(using = CalendarSerializer.class)
    @JsonDeserialize(using = CalendarDeserializer.class)
    private Calendar date;
    @Column(name = "alteration_date")
    @JsonSerialize(using = CalendarSerializer.class)
    @JsonDeserialize(using = CalendarDeserializer.class)
    private Calendar alterationDate;

    public Comment() {}

    public Comment(String bibtexkey, String author, int id, boolean publish, String content, String page, int pageNumber, Calendar date, Calendar alterationDate) {
        this.bibtexkey = bibtexkey;
        this.author = author;
        this.id = id;
        this.publish = publish;
        this.content = content;
        this.page = page;
        this.pageNumber = pageNumber;
        this.date = date;
        this.alterationDate = alterationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBibtexkey() {
        return bibtexkey;
    }

    public void setBibtexkey(String bibtexkey) {
        this.bibtexkey = bibtexkey;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Calendar getAlterationDate() {
        return alterationDate;
    }

    public void setAlterationDate(Calendar alterationDate) {
        this.alterationDate = alterationDate;
    }
}
