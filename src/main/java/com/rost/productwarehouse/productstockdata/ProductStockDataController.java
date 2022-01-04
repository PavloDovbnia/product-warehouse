package com.rost.productwarehouse.productstockdata;

import com.rost.productwarehouse.productstockdata.service.ProductStockDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stock/")
public class ProductStockDataController {

    private final ProductStockDataService productStockDataService;

    public ProductStockDataController(ProductStockDataService productStockDataService) {
        this.productStockDataService = productStockDataService;
    }

    @GetMapping("getGroup")
    public ResponseEntity<Map<Long, ProductStockData>> getProductsStockData(@RequestParam("groupId") long groupId) {
        return ResponseEntity.ok(productStockDataService.getGroupProductsStockData(groupId));
    }

    @GetMapping("getLessValue")
    public ResponseEntity<Map<Long, ProductStockData>> getLessProductsStockData(@RequestParam("value") int value) {
        return ResponseEntity.ok(productStockDataService.getProductsStockDataLessValue(value));
    }
}
