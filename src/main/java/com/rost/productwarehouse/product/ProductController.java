package com.rost.productwarehouse.product;

import com.rost.productwarehouse.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product/product/")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("getAllNotDecorated")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("getDecoratedProducts")
    public ResponseEntity<List<Product>> getProducts(@RequestParam("productsIds") String idsStr) {
        List<Long> productsIds = Arrays.stream(idsStr.split("-")).map(Long::valueOf).collect(Collectors.toList());
        return ResponseEntity.ok(productService.getDecoratedProducts(productsIds));
    }

    @GetMapping("getDecoratedProduct")
    public ResponseEntity<Product> getProduct(@RequestParam("productId") long productId) {
        return ResponseEntity.ok(productService.getDecoratedProduct(productId));
    }

    @PostMapping("/save")
    public ResponseEntity<List<Product>> saveProduct(@RequestBody Product product) {
        productService.storeProduct(product);
        return ResponseEntity.ok(productService.getProducts());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<Product>> deleteProduct(@RequestParam("productId") long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(productService.getProducts());
    }
}
