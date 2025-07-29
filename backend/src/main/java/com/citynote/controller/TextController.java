package com.citynote.controller;

import com.citynote.dto.GenTextRequestDTO;
import com.citynote.dto.GenTextResponseDTO;
import com.citynote.service.TextRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/text")
public class TextController {
    private final TextRecommendationService tService;

    public TextController(TextRecommendationService tRService) {
        this.tService = tRService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<GenTextResponseDTO> getGenText(@RequestBody GenTextRequestDTO gtDto) {
        try {
            String title = gtDto.getTitle();
            String content = gtDto.getCurrentText();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        try {
            GenTextResponseDTO genText = this.tService.genText(gtDto);
            return ResponseEntity.ok(genText);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
