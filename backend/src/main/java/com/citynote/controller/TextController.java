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
        return ResponseEntity.ok(this.tService.genText(gtDto));
    }
}
