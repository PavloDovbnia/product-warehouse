package com.rost.productwarehouse.manufacturer;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.manufacturer.service.ManufacturerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/manufacturer")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping("getAllNotDecorated")
    public ResponseEntity<List<Manufacturer>> getManufacturers() {
        return ResponseEntity.ok(manufacturerService.getManufacturers());
    }

    @GetMapping("getDecoratedManufacturer")
    public ResponseEntity<Manufacturer> getDecoratedManufacturer(@RequestParam("manufacturerId") long manufacturerId) {
        return ResponseEntity.ok(manufacturerService.getDecoratedManufacturers(Lists.newArrayList(manufacturerId)).get(manufacturerId));
    }

    @PostMapping("/save")
    public ResponseEntity<List<Manufacturer>> saveManufacturer(@RequestBody Manufacturer manufacturer) {
        manufacturerService.saveManufacturer(manufacturer);
        return ResponseEntity.ok(manufacturerService.getManufacturers());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<Manufacturer>> deleteManufacturer(@RequestParam("manufacturerId") long manufacturerId) {
        manufacturerService.removeManufacturer(manufacturerId);
        return ResponseEntity.ok(manufacturerService.getManufacturers());
    }
}
