package com.programmingtechie.productservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmingtechie.productservice.dto.ApiResponse;
import com.programmingtechie.productservice.dto.ProductRequest;
import com.programmingtechie.productservice.dto.ProductResponse;
import com.programmingtechie.productservice.dto.UserInfoDetails;
import com.programmingtechie.productservice.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
     private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest productRequest) {
        productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts(HttpServletRequest request) {
         String userInfoJson = request.getHeader("X-User-Info");
    if (userInfoJson != null) {
        // everything is working fine, parse the user info
        try {
            ApiResponse<UserInfoDetails> userInfo = objectMapper.readValue(userInfoJson, new TypeReference<ApiResponse<UserInfoDetails>>() {});
            // Now you can use userInfo as needed
            System.out.println("User info: " + userInfo);
            System.out.println("Username: " + userInfo.getData().getUsername());
            System.out.println("Mobile Number: " + userInfo.getData().getMobileNumber());
            System.out.println("Authorities: " + userInfo.getData().getAuthorities());
            System.out.println("Account Non Expired: " + userInfo.getData().isAccountNonExpired());
            System.out.println("Account Non Locked: " + userInfo.getData().isAccountNonLocked());
            System.out.println("Credentials Non Expired: " + userInfo.getData().isCredentialsNonExpired());
            System.out.println("Enabled: " + userInfo.getData().isEnabled());
        } catch (Exception e) {
            // Handle parsing error
            e.printStackTrace();
        }
    }
        return productService.getAllProducts();
    }

}
