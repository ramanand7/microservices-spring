package com.programmingtechie.productservice.controller;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.programmingtechie.productservice.model.WoodProduct;
import com.programmingtechie.productservice.service.WoodItemService;

@RestController
@RequestMapping("/api/woodproducts")
@CrossOrigin(origins = "*")
public class WoodItemController {

    @Autowired
    private WoodItemService woodProductService;

    @PostMapping
    public ResponseEntity<WoodProduct> createWoodProduct(@RequestBody WoodProduct woodProduct) {
        try {
            WoodProduct savedItem = woodProductService.saveWoodProduct(woodProduct);
            return new ResponseEntity<WoodProduct>(savedItem, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/storeall")
    public ResponseEntity<List<WoodProduct>> createAllWoodProduct(@RequestBody List<WoodProduct> woodProductList) {
        try {
            List<WoodProduct> savedItems = woodProductService.saveAllWoodProduct(woodProductList);
            return new ResponseEntity<List<WoodProduct>>(savedItems, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<WoodProduct>> getAllWoodProducts() {
        try {
            List<WoodProduct> woodProductsList = woodProductService.getAllWoodProducts();
            if (woodProductsList.isEmpty()) {
                throw new Exception("No wood products found");
            }
            return new ResponseEntity<List<WoodProduct>>(woodProductsList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<WoodProduct>> getActiveProductList() {
        try {
            List<WoodProduct> activeListProducts = woodProductService.getActiveWoodProducts();
            if (activeListProducts.isEmpty()) {
                throw new Exception("No active wood products found");
            }
            return new ResponseEntity<List<WoodProduct>>(activeListProducts, HttpStatus.OK);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<WoodProduct> getWoodProductById(@PathVariable("id") String id) {
        try {
            WoodProduct woodProduct = woodProductService.getWoodProductById(id)
                    .orElseThrow(() -> new Exception("Wood Product not found with id: " + id));
            return new ResponseEntity<WoodProduct>(woodProduct, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WoodProduct> updateWoodProduct(@PathVariable("id") String id,
            @RequestBody WoodProduct woodProduct) {
        Optional<WoodProduct> productitem = woodProductService.getWoodProductById(id);
        if (productitem.isPresent()) {
            WoodProduct item = productitem.get();
            item.setName(woodProduct.getName());
            item.setDescription(woodProduct.getDescription());
            item.setStock(woodProduct.getStock());
            item.setImageUrl(woodProduct.getImageUrl());
            item.setIsActive(woodProduct.getIsActive());
            item.setPrice(woodProduct.getPrice());
            WoodProduct updadedProduct = woodProductService.updateWoodProduct(id, item);
            return new ResponseEntity<WoodProduct>(updadedProduct, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteWoodItem(@PathVariable("id") String id) {
        try {
            woodProductService.deleteWoodProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<WoodProduct>> searchByName(@RequestParam String name) {
        try {
            List<WoodProduct> woodProducts = woodProductService.searchByName(name);
            if (woodProducts.isEmpty()) {
                throw new Exception("No wood products found with name: " + name);
            }
            return new ResponseEntity<List<WoodProduct>>(woodProducts, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<WoodProduct>> getInStockItems() {
        try {
            List<WoodProduct> items = woodProductService.getInStockWoodProducts();
            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
