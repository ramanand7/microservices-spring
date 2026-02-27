package com.programmingtechie.productservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.programmingtechie.productservice.model.WoodProduct;

public interface WoodProductRepository extends MongoRepository<WoodProduct, String> {

    List<WoodProduct> findByIsActiveTrue();

    List<WoodProduct> findByNameContainingIgnoreCase(String name);

    List<WoodProduct> findByStockGreaterThan(Integer stock);

    List<WoodProduct> findByIsActiveTrueAndStockGreaterThan(Integer stock);
}
