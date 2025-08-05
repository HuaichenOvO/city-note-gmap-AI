package com.citynote.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenTextRequestDTO {
    public String title;
    public String currentText;
    public List<String> URLs;
}
