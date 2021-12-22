package com.rost.productwarehouse.productcategory;

import com.rost.productwarehouse.productcategory.service.ProductCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/category")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductCategory>> getAll() {
        return ResponseEntity.ok(productCategoryService.getCategories());
    }

    @PostMapping("/save")
    public ResponseEntity<List<ProductCategory>> save(@RequestBody ProductCategory category) {
        productCategoryService.saveCategory(category);
        return ResponseEntity.ok(productCategoryService.getCategories());
    }

    @PostMapping("/delete")
    public ResponseEntity<List<ProductCategory>> remove(@RequestBody ProductCategory category) {
        productCategoryService.removeCategory(category.getId());
        return ResponseEntity.ok(productCategoryService.getCategories());
    }
}
