package com.citynote.controller;

import com.citynote.dto.GenTextRequestDTO;
import com.citynote.dto.GenTextResponseDTO;
import com.citynote.service.TextRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TextControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TextRecommendationService textRecommendationService;

    @InjectMocks
    private TextController textController;

    private ObjectMapper objectMapper;
    private GenTextRequestDTO validRequest;
    private GenTextResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        // Arrange - Set up MockMvc with just the controller (no Spring context)
        mockMvc = MockMvcBuilders.standaloneSetup(textController).build();
        objectMapper = new ObjectMapper();

        // Arrange - Set up test data
        validRequest = new GenTextRequestDTO();
        validRequest.setTitle("sample title");
        validRequest.setCurrentText("sample text");
        // Add any required fields for GenTextRequestDTO here
        // Example: validRequest.setPrompt("Generate text about spring boot");

        expectedResponse = new GenTextResponseDTO();
        expectedResponse.setNewTitle("sample new title");
        expectedResponse.setNewContent("sample new text");
        // Add expected response fields here
        // Example: expectedResponse.setGeneratedText("Sample generated text");
        // Example: expectedResponse.setStatus("SUCCESS");
    }

    @Test
    void getGenText_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        when(textRecommendationService.genText(any(GenTextRequestDTO.class)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/text/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
        // Add more specific assertions based on your GenTextResponseDTO structure
        // Example: .andExpected(jsonPath("$.generatedText").value("Sample generated text"))
        // Example: .andExpected(jsonPath("$.status").value("SUCCESS"))
    }

    @Test
    void getGenText_WithValidRequest_ShouldCallServiceOnce() throws Exception {
        // Arrange
        when(textRecommendationService.genText(any(GenTextRequestDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        mockMvc.perform(post("/api/text/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Assert
        // Verify that the service method was called exactly once
        org.mockito.Mockito.verify(textRecommendationService, org.mockito.Mockito.times(1))
                .genText(any(GenTextRequestDTO.class));
    }

    @Test
    void getGenText_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(post("/api/text/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGenText_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/text/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGenText_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(textRecommendationService.genText(any(GenTextRequestDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/text/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getGenText_WithWrongHttpMethod_ShouldReturnMethodNotAllowed() throws Exception {
        // Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/text/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getGenText_WithWrongContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/text/recommend")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }
}