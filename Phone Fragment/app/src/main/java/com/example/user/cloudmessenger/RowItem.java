package com.example.user.cloudmessenger;

import java.util.Date;

/**
 * Created by LEKHA VARGHESE on 18-04-2015.
 */
public class RowItem {

    private int imageId;
    private String title;
    private Date date;
    private String dur;

    public RowItem(int imageId, String title, Date date, String dur) {
        this.imageId = imageId;
        this.title = title;
       this.date = date;
        this.dur=dur;
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date desc) {
        this.date = date;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDuration() { return dur; }
    public void setDuration(String dur) { this.dur=dur;}
    @Override
    public String toString() {
        return title + "\n" ;
    }
}
