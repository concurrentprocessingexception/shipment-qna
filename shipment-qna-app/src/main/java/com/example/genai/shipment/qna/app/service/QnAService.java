package com.example.genai.shipment.qna.app.service;

import com.example.genai.shipment.qna.app.exception.AppException;
import com.example.genai.shipment.qna.app.model.ChatRequest;
import com.example.genai.shipment.qna.app.model.ChatResponse;
import com.example.genai.shipment.qna.app.entity.Document;
import com.example.genai.shipment.qna.app.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingOptions;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QnAService {

    @Value("classpath:/prompt-template.st")
    protected Resource systemPrompt;

    @Autowired
    @Qualifier("titanEmbeddingModel")
    private EmbeddingModel embeddingModel;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ChatModel chatModel;

    public ChatResponse handleRequest(ChatRequest chatRequest) {
        log.info("QnAService::handleRequest START");
        try {
            Prompt prompt = createPromptForChat(chatRequest);

            org.springframework.ai.chat.model.ChatResponse chatResponse = chatModel.call(prompt);
            log.debug("Chat Response : {}", chatResponse);

            return parseAiResponse(chatResponse);
        } catch (Exception e) {
            log.error("Exception : {}", e.getMessage());
            throw new AppException(e.getMessage(), e);
        } finally {
            log.info("QnAService::handleRequest END");
        }
    }

    private ChatResponse parseAiResponse(org.springframework.ai.chat.model.ChatResponse chatResponse) {

        String responseStr = null;
        if (null != chatResponse) {
            List<Generation> generations = chatResponse.getResults();
            log.debug("Number of generation from chat client : {}", generations.size());
            responseStr = generations.get(0).getOutput().getText();
        }
        return new ChatResponse(responseStr);
    }

    private Prompt createPromptForChat(ChatRequest chatRequest) {
        log.debug("Creating prompt...");

        List<Message> messages = chatRequest.getHistory().stream().map(m -> {
            if("AI".equals(m.getFrom())) {
                return new AssistantMessage(m.getMessage());
            } else {
                return new UserMessage(m.getMessage());
            }
        }).collect(Collectors.toList());

        // add system message
        Message systemMessage = createSystemMessage(messages, chatRequest.getMessage());

        // add current message
        UserMessage userMessage = new UserMessage(chatRequest.getMessage());

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        log.debug("Prompt created!!! : {}", prompt);
        return prompt;
    }

    private Message createSystemMessage(List<Message> historyMessages, String message) {

        log.debug("Creating system message...");
        try {
            String history = historyMessages.stream()
                    .map(m -> m.getMessageType().name().toLowerCase() + ": " + m.getText())
                    .collect(Collectors.joining(System.lineSeparator()));

            EmbeddingResponse embeddingResponse = embeddingModel.call(
                    new EmbeddingRequest(List.of(message),
                            BedrockTitanEmbeddingOptions.builder()
                                    .withInputType(BedrockTitanEmbeddingModel.InputType.TEXT)
                                    .build()));

            float[] questionEmbedding = embeddingResponse.getResult().getOutput();
            log.info("Embedding Length : {}", questionEmbedding.length);

            log.debug("fetching similar documents from the vector store...");
            List<Document> similarDocuments = documentRepository.findTopNSimilar(questionEmbedding, 3);
            log.debug("Found {} documents in vector store. Creating system message with documents!!!", similarDocuments.size());
            String documents = similarDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n---\n"));
            return new SystemPromptTemplate(systemPrompt)
                    .createMessage(Map.of(
                            "documents", documents,
                            "history", history,
                            "currentDate", java.time.LocalDate.now()
                    ));
        } finally {
            log.debug("System message created!!!");
        }
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }



}
