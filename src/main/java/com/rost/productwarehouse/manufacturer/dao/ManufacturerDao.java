package com.rost.productwarehouse.manufacturer.dao;

import com.rost.productwarehouse.manufacturer.Manufacturer;

import java.util.List;

public interface ManufacturerDao {

    List<Manufacturer> getManufacturers();

    long storeManufacturer(Manufacturer manufacturer);

    void deleteManufacturer(long manufacturerId);
}