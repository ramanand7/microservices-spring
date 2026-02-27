package com.programmingtechie.productservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(value = "wood_product")
@AllArgsConstructor
@Data
@NoArgsConstructor
public class WoodProduct {
    @Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    @Field("image_url")
    private String imageUrl;

    @Field("is_active")
    private Boolean isActive;

    @Field("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
