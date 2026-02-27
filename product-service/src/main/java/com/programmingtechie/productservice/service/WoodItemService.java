package com.programmingtechie.productservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programmingtechie.productservice.model.WoodProduct;
import com.programmingtechie.productservice.repository.WoodProductRepository;

@Service
public class WoodItemService {

    @Autowired
    private WoodProductRepository woodProductRepository;

    public WoodProduct saveWoodProduct(WoodProduct woodProduct) {
        return woodProductRepository.save(woodProduct);
    }

    public List<WoodProduct> getAllWoodProducts() {
        return woodProductRepository.findAll();
    }

    public List<WoodProduct> getActiveWoodProducts() {
        return woodProductRepository.findByIsActiveTrue();
    }

    public Optional<WoodProduct> getWoodProductById(String id) {
        return woodProductRepository.findById(id);
    }

    public WoodProduct updateWoodProduct(String id, WoodProduct woodProduct) {
        if (woodProductRepository.existsById(id)) {
            woodProduct.setId(id);
            return woodProductRepository.save(woodProduct);
        } else {
            throw new RuntimeException("Wood Product not found with id: " + id);
        }
    }

    public void deleteWoodProduct(String id) {
        if (woodProductRepository.existsById(id)) {
            woodProductRepository.deleteById(id);
        } else {
            throw new RuntimeException("Wood Product not found with id: " + id);
        }
    }

    public List<WoodProduct> searchByName(String name) {
        return woodProductRepository.findByNameContainingIgnoreCase(name);
    }

    public List<WoodProduct> getInStockWoodProducts() {
        return woodProductRepository.findByIsActiveTrueAndStockGreaterThan(0);
    }

    public List<WoodProduct> saveAllWoodProduct(List<WoodProduct> woodProductList) {
        List<WoodProduct> saveProducts = new ArrayList<>();
        for (int i = 0; i < woodProductList.size(); i++) {
            WoodProduct woodProduct = woodProductList.get(i);
            if (woodProduct.getName() == null || woodProduct.getName().isEmpty()) {
                throw new IllegalArgumentException("Wood Product name cannot be null or empty");
            }
            if (woodProduct.getPrice() == null || woodProduct.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Wood Product price must be greater than zero");
            }
            if (woodProduct.getStock() == null || woodProduct.getStock() < 0) {
                throw new IllegalArgumentException("Wood Product stocks cannot be negative");
            }
            if (woodProduct.getImageUrl() == null || woodProduct.getImageUrl().isEmpty()) {
                throw new IllegalArgumentException("Wood Product image URL cannot be null or empty");
            }
            if (woodProduct.getCreatedAt() == null) {
                woodProduct.setCreatedAt(LocalDateTime.now());
            }
            if (woodProduct.getIsActive() == null) {
                woodProduct.setIsActive(true);
            }
            saveProducts.add(woodProduct);
            saveWoodProduct(woodProduct);
        }
        return saveProducts;
    }
}
