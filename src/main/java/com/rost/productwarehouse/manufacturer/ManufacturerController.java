package com.rost.productwarehouse.manufacturer;

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

    @GetMapping("getDecoratedManufacturers")
    public ResponseEntity<List<Manufacturer>> getDecoratedManufacturers() {
        return ResponseEntity.ok(manufacturerService.getDecoratedManufacturers());
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
