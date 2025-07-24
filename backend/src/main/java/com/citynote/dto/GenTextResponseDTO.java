package com.citynote.dto;

import lombok.Getter;

@Getter
public class GenTextResponseDTO {
    public String newTitle;
    public String newContent;

    public GenTextResponseDTO setNewTitle(String title) {
        this.newTitle = title;
        return this;
    }

    public GenTextResponseDTO setNewContent(String content) {
        this.newContent = content;
        return this;
    }
}
