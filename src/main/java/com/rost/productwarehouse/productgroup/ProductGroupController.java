package com.rost.productwarehouse.productgroup;

import com.rost.productwarehouse.productgroup.service.ProductGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product/group/")
public class ProductGroupController {

    private final ProductGroupService productGroupService;

    public ProductGroupController(ProductGroupService productGroupService) {
        this.productGroupService = productGroupService;
    }

    @GetMapping("getAllNotDecorated")
    public ResponseEntity<List<ProductGroup>> getGroups() {
        return ResponseEntity.ok(productGroupService.getGroups());
    }

    @GetMapping("getDecoratedGroups")
    public ResponseEntity<List<ProductGroup>> getProducts(@RequestParam("groupsIds") String idsStr) {
        List<Long> groupsIds = Arrays.stream(idsStr.split("-")).map(Long::valueOf).collect(Collectors.toList());
        return ResponseEntity.ok(productGroupService.getDecoratedGroups(groupsIds));
    }

    @GetMapping("getDecoratedGroup")
    public ResponseEntity<ProductGroup> getGroup(@RequestParam("groupId") long groupId) {
        return ResponseEntity.ok(productGroupService.getDecoratedGroup(groupId));
    }

    @PostMapping("/save")
    public ResponseEntity<List<ProductGroup>> saveGroup(@RequestBody ProductGroup group) {
        productGroupService.storeGroup(group);
        return ResponseEntity.ok(productGroupService.getGroups());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<ProductGroup>> deleteGroup(@RequestParam("groupId") long groupId) {
        productGroupService.deleteGroup(groupId);
        return ResponseEntity.ok(productGroupService.getGroups());
    }
}
