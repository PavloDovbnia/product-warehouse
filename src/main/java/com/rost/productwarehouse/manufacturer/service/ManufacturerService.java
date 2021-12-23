package com.rost.productwarehouse.manufacturer.service;

import com.rost.productwarehouse.manufacturer.Manufacturer;

import java.util.List;

public interface ManufacturerService {

    List<Manufacturer> getManufacturers();

    List<Manufacturer> getDecoratedManufacturers();

    long saveManufacturer(Manufacturer manufacturer);

    void removeManufacturer(long manufacturerId);
}
