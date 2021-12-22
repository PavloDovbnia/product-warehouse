package com.rost.productwarehouse.itemproperty.controller;

import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemProperty;
import com.rost.productwarehouse.itemproperty.service.ItemPropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/property/")
public class ItemPropertyController {

    private final ItemPropertyService itemPropertyService;

    public ItemPropertyController(ItemPropertyService itemPropertyService) {
        this.itemPropertyService = itemPropertyService;
    }

    @GetMapping("{item-level}/getAll")
    public ResponseEntity<List<ItemProperty>> getProperties(@PathVariable("item-level") String itemLevelStr) {
        ItemLevel itemLevel = ItemLevel.of(itemLevelStr);
        return ResponseEntity.ok(itemPropertyService.getItemProperties(itemLevel));
    }

    @PostMapping("save")
    public ResponseEntity<List<ItemProperty>> saveProperty(@RequestBody ItemProperty itemProperty) {
        itemPropertyService.saveProperty(itemProperty);
        return ResponseEntity.ok(itemPropertyService.getItemProperties(itemProperty.getItemLevel()));
    }

    @PostMapping("delete")
    public ResponseEntity<List<ItemProperty>> deleteProperty(@RequestBody ItemProperty itemProperty) {
        itemPropertyService.deleteProperty(itemProperty.getId());
        return ResponseEntity.ok(itemPropertyService.getItemProperties(itemProperty.getItemLevel()));
    }

}
