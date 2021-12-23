package com.rost.productwarehouse.manufacturer.service;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.itemproperty.service.ItemPropertyService;
import com.rost.productwarehouse.manufacturer.Manufacturer;
import com.rost.productwarehouse.manufacturer.dao.ManufacturerDao;
import com.rost.productwarehouse.productgroup.ProductGroup;
import com.rost.productwarehouse.productgroup.service.ProductGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerDao manufacturerDao;
    private final ProductGroupService productGroupService;
    private final ItemPropertyService itemPropertyService;

    public ManufacturerServiceImpl(ManufacturerDao manufacturerDao, ProductGroupService productGroupService, ItemPropertyService itemPropertyService) {
        this.manufacturerDao = manufacturerDao;
        this.productGroupService = productGroupService;
        this.itemPropertyService = itemPropertyService;
    }

    @Override
    public List<Manufacturer> getManufacturers() {
        return manufacturerDao.getManufacturers();
    }

    @Override
    public List<Manufacturer> getDecoratedManufacturers() {
        return decorateManufacturers(manufacturerDao.getManufacturers());
    }

    @Override
    public long saveManufacturer(Manufacturer manufacturer) {
        if (manufacturer.getId() <= 0L) {
            manufacturer.setToNew();
        }
        long manufacturerId = manufacturerDao.storeManufacturer(manufacturer);
        manufacturer.setId(manufacturerId);
        manufacturerDao.storeManufacturerGroups(manufacturerId, manufacturer.getProductGroupsIds());

        if (!MapUtils.isNotEmpty(manufacturer.getProperties().getProperties())) {
            itemPropertyService.saveItemValues(manufacturerId, manufacturer.getProperties().getProperties(), ItemLevel.MANUFACTURER);
        }
        return manufacturerId;
    }

    @Override
    public void removeManufacturer(long manufacturerId) {
        manufacturerDao.deleteManufacturerGroups(manufacturerId);
        manufacturerDao.deleteManufacturer(manufacturerId);
    }

    private List<Manufacturer> decorateManufacturers(List<Manufacturer> manufacturers) {
        if (CollectionUtils.isNotEmpty(manufacturers)) {
            List<Long> manufacturerIds = manufacturers.stream().map(Manufacturer::getId).collect(Collectors.toList());
            Map<Long, ItemPropertiesHolder> manufacturersProperties = itemPropertyService.getPropertiesValues(manufacturerIds, ItemLevel.MANUFACTURER);

            List<Long> groupsIds = manufacturers.stream().flatMap(m -> m.getProductGroupsIds().stream()).collect(Collectors.toList());
            Map<Long, ProductGroup> mappedGroups = productGroupService.getDecoratedGroups(groupsIds).stream().collect(Collectors.toMap(ProductGroup::getId, Function.identity()));

            manufacturers.forEach(manufacturer -> {
                ItemPropertiesHolder holder = manufacturersProperties.get(manufacturer.getId());
                if (holder != null) {
                    manufacturer.getProperties().addProperties(holder.getProperties());
                }

                manufacturer.setProductGroups(Lists.newArrayList());
                manufacturer.getProductGroupsIds().forEach(groupId -> {
                    ProductGroup group = mappedGroups.get(groupId);
                    if (group != null) {
                        manufacturer.getProductGroups().add(group);
                    }
                });
            });
        }
        return manufacturers;
    }
}
