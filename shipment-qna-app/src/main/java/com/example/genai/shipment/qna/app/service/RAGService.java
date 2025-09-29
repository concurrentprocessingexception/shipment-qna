package com.example.genai.shipment.qna.app.service;

import com.example.genai.shipment.qna.app.entity.Document;
import com.example.genai.shipment.qna.app.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RAGService {

    @Autowired
    @Qualifier("titanEmbeddingModel")
    private EmbeddingModel embeddingModel;
    @Autowired
    private DocumentRepository documentRepository;

    @Transactional
    public void createAndStoreEmbeddings(String document) {
        log.info("Creating embedding for uploaded document, size={} chars", document.length());

        List<String> chunks = splitIntoChunks(document, 1000, 100);

        chunks.stream().map(chunk -> {
            EmbeddingResponse embeddingResponse = embeddingModel.call(
                    new EmbeddingRequest(List.of(document),
                            BedrockTitanEmbeddingOptions.builder()
                                    .withInputType(BedrockTitanEmbeddingModel.InputType.TEXT)
                                    .build()));

            log.debug("Embedding created. {}", embeddingResponse.getResult());
            float[] vector = embeddingResponse.getResult().getOutput();

            Document entity = new Document();
            entity.setContent(chunk);
            entity.setEmbedding(vector);
            return entity;
        }).forEach(documentRepository::save);

        log.debug("Document Created!!!");
    }

    private List<String> splitIntoChunks(String text, int size, int overlap) {
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        if (overlap < 0 || overlap >= size)
            throw new IllegalArgumentException("overlap must be >= 0 and < size");

        List<String> chunks = new ArrayList<>();
        int step = size - overlap;

        for (int start = 0; start < text.length(); start += step) {
            int end = Math.min(start + size, text.length());
            chunks.add(text.substring(start, end));
        }
        return chunks;
    }


}
