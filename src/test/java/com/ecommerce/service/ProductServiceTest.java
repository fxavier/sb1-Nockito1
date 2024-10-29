package com.ecommerce.service;

import com.ecommerce.domain.dto.ProductDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(10);
        testProduct.setActive(true);

        testProductDTO = new ProductDTO();
        testProductDTO.setName("Test Product");
        testProductDTO.setDescription("Test Description");
        testProductDTO.setPrice(new BigDecimal("99.99"));
        testProductDTO.setStockQuantity(10);
    }

    @Test
    void getProductById_ExistingProduct_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Uni.createFrom().item(testProduct));

        Product result = productService.getProductById(1L)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NonExistingProduct_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(99L).await().indefinitely();
        });
        verify(productRepository).findById(99L);
    }

    @Test
    void createProduct_ValidProduct_ReturnsCreatedProduct() {
        when(productRepository.persist(any(Product.class))).thenReturn(Uni.createFrom().item(testProduct));

        Product result = productService.createProduct(testProductDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(testProductDTO.getName(), result.getName());
        assertEquals(testProductDTO.getPrice(), result.getPrice());
        verify(productRepository).persist(any(Product.class));
    }

    @Test
    void updateProduct_ExistingProduct_ReturnsUpdatedProduct() {
        when(productRepository.findById(1L)).thenReturn(Uni.createFrom().item(testProduct));
        when(productRepository.persist(any(Product.class))).thenReturn(Uni.createFrom().item(testProduct));

        testProductDTO.setName("Updated Name");
        Product result = productService.updateProduct(1L, testProductDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(testProductDTO.getName(), result.getName());
        verify(productRepository).findById(1L);
        verify(productRepository).persist(any(Product.class));
    }

    @Test
    void deleteProduct_ExistingProduct_ReturnsTrue() {
        when(productRepository.findById(1L)).thenReturn(Uni.createFrom().item(testProduct));
        when(productRepository.delete(any(Product.class))).thenReturn(Uni.createFrom().item(1L));

        Boolean result = productService.deleteProduct(1L)
            .await().indefinitely();

        assertTrue(result);
        verify(productRepository).findById(1L);
        verify(productRepository).delete(any(Product.class));
    }

    @Test
    void findActiveProducts_ReturnsActiveProductsList() {
        List<Product> activeProducts = Arrays.asList(testProduct);
        when(productRepository.findActiveProducts()).thenReturn(Uni.createFrom().item(activeProducts));

        List<Product> result = productService.findActiveProducts()
            .await().indefinitely();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(productRepository).findActiveProducts();
    }
}