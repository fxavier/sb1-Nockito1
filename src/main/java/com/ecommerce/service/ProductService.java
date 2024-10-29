package com.ecommerce.service;

import com.ecommerce.domain.dto.PageDTO;
import com.ecommerce.domain.dto.ProductDTO;
import com.ecommerce.domain.dto.ProductFilterDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductService {
    
    @Inject
    ProductRepository productRepository;
    
    @Inject
    ImageStorageService imageStorageService;
    
    // Previous methods remain unchanged
    
    @Transactional
    public Uni<ProductImage> addProductImage(Long productId, String imageUrl, Boolean isCover) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(imageUrl);
                image.setIsCover(isCover != null && isCover);
                
                if (image.getIsCover()) {
                    // Unset current cover image if exists
                    product.getImages().stream()
                        .filter(ProductImage::getIsCover)
                        .forEach(img -> img.setIsCover(false));
                }
                
                product.getImages().add(image);
                return productRepository.persist(product)
                    .map(p -> image);
            });
    }
    
    @Transactional
    public Uni<Boolean> deleteProductImage(Long productId, Long imageId) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                ProductImage image = product.getImages().stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElse(null);
                
                if (image == null) {
                    return Uni.createFrom().item(false);
                }
                
                String imageUrl = image.getImageUrl();
                product.getImages().remove(image);
                
                return productRepository.persist(product)
                    .chain(() -> imageStorageService.deleteImage(imageUrl))
                    .map(v -> true);
            });
    }
    
    @Transactional
    public Uni<ProductImage> setCoverImage(Long productId, Long imageId) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                ProductImage newCover = product.getImages().stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> 
                        new ResourceNotFoundException("Image not found"));
                
                // Update cover image status
                product.getImages().forEach(img -> 
                    img.setIsCover(img.getId().equals(imageId)));
                
                return productRepository.persist(product)
                    .map(p -> newCover);
            });
    }
}