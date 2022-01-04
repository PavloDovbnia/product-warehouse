package com.rost.productwarehouse.manufacturer.service;

import com.rost.productwarehouse.manufacturer.Manufacturer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ManufacturerService {

    List<Manufacturer> getManufacturers();

    Map<Long, Manufacturer> getDecoratedManufacturers(Collection<Long> manufacturersIds);

    long saveManufacturer(Manufacturer manufacturer);

    void removeManufacturer(long manufacturerId);
}
