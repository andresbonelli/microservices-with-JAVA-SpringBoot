package com.andresbonelli.microservices.product.service;

import com.andresbonelli.microservices.product.controller.dto.CreateProductDTO;
import com.andresbonelli.microservices.product.controller.dto.ProductResponseDTO;
import com.andresbonelli.microservices.product.controller.dto.UpdateProductDTO;
import com.andresbonelli.microservices.product.model.Product;
import com.andresbonelli.microservices.product.repository.ProductRepository;
import com.andresbonelli.microservices.product.service.exceptions.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponseDTO createProduct(CreateProductDTO request) {
        Product product = buildProduct(request);
        Product savedProduct = productRepository.save(product);
        log.info("Product {} successfully created", savedProduct.getId());
        return new ProductResponseDTO(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice()
        );
    }

    public ProductResponseDTO getProduct(String id) throws Exception{
        Product product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    public List<ProductResponseDTO> getAll() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice()
                ))
                .toList();
    }

    private Product buildProduct(CreateProductDTO request) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .build();
    }

    public ProductResponseDTO updateProduct(UpdateProductDTO request) {
        Product product = productRepository.findById(request.id()).orElseThrow();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        Product updatedProduct = productRepository.save(product);
        return new ProductResponseDTO(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice()
        );
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
