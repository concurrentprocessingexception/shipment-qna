package com.example.genai.shipment.qna.app.repository;

import com.example.genai.shipment.qna.app.entity.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class DocumentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Convert float[] to pgvector literal: "[0.1, 0.2, ...]"
    private String toVectorLiteral(float[] vector) {
        return "[" + IntStream.range(0, vector.length)
                .mapToObj(i -> Float.toString(vector[i]))
                .collect(Collectors.joining(", ")) + "]";
    }

    public List<Document> findTopNSimilar(float[] vector, int limit) {
        String vectorLiteral = toVectorLiteral(vector);
        String sql = "SELECT id, content FROM document_embeddings " +
                "ORDER BY embedding <-> CAST(? AS vector) LIMIT ?";

        return jdbcTemplate.query(sql, new Object[]{vectorLiteral, limit},
                (rs, rowNum) -> {
                    Document doc = new Document();
                    doc.setId(rs.getLong("id"));
                    doc.setContent(rs.getString("content"));
                    // optionally set embedding if needed
                    return doc;
                });
    }

    public void save(Document document) {
        String sql = "INSERT INTO document_embeddings (content, embedding) VALUES (?, CAST(? AS vector))";

        jdbcTemplate.update(sql,
                document.getContent(),
                toVectorLiteral(document.getEmbedding()));
    }

}
