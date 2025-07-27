package com.citynote.dto;

import lombok.Data;

@Data
public class EventRequestDTO {
    private String title;
    private String content;
    private String county;
    private Integer countyId;
    private String[] pictureLinks;
    private String videoLink;

    // Manual getters and setters for Lombok compatibility
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public String[] getPictureLinks() {
        return pictureLinks;
    }

    public void setPictureLinks(String[] pictureLinks) {
        this.pictureLinks = pictureLinks;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }
}
