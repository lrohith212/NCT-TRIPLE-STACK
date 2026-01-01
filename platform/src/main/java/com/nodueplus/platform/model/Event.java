package com.nodueplus.platform.model;

public class Event {
    private String title;
    private String description;
    private String eventDate;
    private String deadline;      // Last date to register
    private String link;
    private String posterImage;   // Base64 Image

    public Event() {}

    public Event(String title, String description, String eventDate, String deadline, String link, String posterImage) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.deadline = deadline;
        this.link = link;
        this.posterImage = posterImage;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getPosterImage() { return posterImage; }
    public void setPosterImage(String posterImage) { this.posterImage = posterImage; }
}