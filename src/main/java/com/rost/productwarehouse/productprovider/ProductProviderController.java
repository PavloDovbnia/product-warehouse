package com.rost.productwarehouse.productprovider;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.productprovider.service.ProductProviderService;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.UserDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provider/")
public class ProductProviderController {

    private final ProductProviderService productProviderService;
    private final UserDao userDao;

    public ProductProviderController(ProductProviderService productProviderService, UserDao userDao) {
        this.productProviderService = productProviderService;
        this.userDao = userDao;
    }

    @GetMapping("getUserProviders")
    public ResponseEntity<List<User>> getUserProviders() {
        return ResponseEntity.ok(userDao.getUsersByRoles(Lists.newArrayList(Role.Type.ROLE_PRODUCT_PROVIDER)));
    }

    @GetMapping("getAllNotDecoratedProviders")
    public ResponseEntity<List<ProductProvider>> getAllProviders() {
        return ResponseEntity.ok(Lists.newArrayList(productProviderService.getProviders().values()));
    }

    @GetMapping("getDecoratedProviders")
    public ResponseEntity<Map<Long, ProductProvider>> getDecoratedProviders(@RequestParam("providersIds") String idsStr) {
        List<Long> providersIds = Arrays.stream(idsStr.split("-")).map(Long::valueOf).collect(Collectors.toList());
        return ResponseEntity.ok(productProviderService.getDecoratedProviders(providersIds));
    }

    @GetMapping("getDecoratedProductsProviders")
    public ResponseEntity<Map<Long, List<ProductProvider>>> getDecoratedProductsProviders(@RequestParam("productsIds") String idsStr) {
        List<Long> productsIds = Arrays.stream(idsStr.split("-")).map(Long::valueOf).collect(Collectors.toList());
        return ResponseEntity.ok(productProviderService.getDecoratedProductProviders(productsIds));
    }

    @PostMapping("save")
    public ResponseEntity<List<ProductProvider>> save(@RequestBody ProductProvider provider) {
        productProviderService.saveProvider(provider);
        return ResponseEntity.ok(Lists.newArrayList(productProviderService.getProviders().values()));
    }

    @DeleteMapping("delete")
    public ResponseEntity<List<ProductProvider>> delete(@RequestParam("providerId") long providerId) {
        productProviderService.deleteProvider(providerId);
        return ResponseEntity.ok(Lists.newArrayList(productProviderService.getProviders().values()));
    }
}
