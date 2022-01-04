package com.rost.productwarehouse.manufacturer.dao;

import com.rost.productwarehouse.manufacturer.Manufacturer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ManufacturerDao {

    List<Manufacturer> getManufacturers();

    Map<Long, Manufacturer> getManufacturers(Collection<Long> manufacturersIds);

    long storeManufacturer(Manufacturer manufacturer);

    void deleteManufacturer(long manufacturerId);

    void storeManufacturerGroups(long manufacturerId, Collection<Long> groupsIds);

    void deleteManufacturerGroups(long manufacturerId);
}
