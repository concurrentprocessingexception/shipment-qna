package com.example.genai.shipment.qna.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_embeddings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "vector(1024)")
    private float[] embedding;
}
