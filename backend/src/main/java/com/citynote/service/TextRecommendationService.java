package com.citynote.service;

import com.citynote.dto.GenTextRequestDTO;
import com.citynote.dto.GenTextResponseDTO;
import com.openai.models.*;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class TextRecommendationService {

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.base-url}")
    private String openaiApiUrl;

//    // TODO: add usage count
//    @Autowired
//    private final UsageRepository usageRepository;

    private OpenAIClient openAiClient;

    @PostConstruct
    private void init() {
        openAiClient = OpenAIOkHttpClient.builder()
                .apiKey(openaiApiKey)
                .baseUrl(openaiApiUrl)
                .build();
    }

    public GenTextResponseDTO genText(GenTextRequestDTO gTRequest) {

        StructuredChatCompletionCreateParams<GenTextResponseDTO> params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .maxCompletionTokens(3000)
                .addAssistantMessage("""
                        response should strictly follow this JSON format:
                        {title: "your_title_reply", content: "your_content_reply"}
                        """)
                .addUserMessage(String.format("""
                             You are a post completion engine in a travel/social \
                             event application. Provide a completed post according to the title: ```%s```\
                             and the content \
                             ```\
                             %s\
                             ```""", gTRequest.getCurrentText(), gTRequest.getTitle()))
                .responseFormat(GenTextResponseDTO.class)
                .build();

        Optional<GenTextResponseDTO> chatResponse = openAiClient.chat()
                .completions().create(params)
                .choices().get(0)
                .message().content();

        return chatResponse.orElseGet(
            () -> new GenTextResponseDTO()
                .setNewTitle("No value suggested")
                .setNewContent("No value suggested")
        );
    }
}
