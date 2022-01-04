package com.rost.productwarehouse.manufacturer.service;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.itemproperty.service.ItemPropertyService;
import com.rost.productwarehouse.manufacturer.Manufacturer;
import com.rost.productwarehouse.manufacturer.dao.ManufacturerDao;
import com.rost.productwarehouse.productgroup.ProductGroup;
import com.rost.productwarehouse.productgroup.service.ProductGroupService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
    public Map<Long, Manufacturer> getDecoratedManufacturers(Collection<Long> manufacturersIds) {
        return decorateManufacturers(manufacturerDao.getManufacturers(manufacturersIds));
    }

    @Override
    public long saveManufacturer(Manufacturer manufacturer) {
        if (manufacturer.getId() <= 0L) {
            manufacturer.setToNew();
        }
        long manufacturerId = manufacturerDao.storeManufacturer(manufacturer);
        manufacturer.setId(manufacturerId);
        manufacturerDao.storeManufacturerGroups(manufacturerId, manufacturer.getProductGroupsIds());

        if (MapUtils.isNotEmpty(manufacturer.getProperties().getProperties())) {
            itemPropertyService.saveItemValues(manufacturerId, manufacturer.getProperties().getProperties(), ItemLevel.MANUFACTURER);
        }
        return manufacturerId;
    }

    @Override
    public void removeManufacturer(long manufacturerId) {
        manufacturerDao.deleteManufacturer(manufacturerId);
    }

    private Map<Long, Manufacturer> decorateManufacturers(Map<Long, Manufacturer> manufacturers) {
        if (MapUtils.isNotEmpty(manufacturers)) {
            List<Long> manufacturerIds = manufacturers.values().stream().map(Manufacturer::getId).collect(Collectors.toList());
            Map<Long, ItemPropertiesHolder> manufacturersProperties = itemPropertyService.getPropertiesValues(manufacturerIds, ItemLevel.MANUFACTURER);

            List<Long> groupsIds = manufacturers.values().stream().flatMap(m -> m.getProductGroupsIds().stream()).collect(Collectors.toList());
            Map<Long, ProductGroup> mappedGroups = productGroupService.getDecoratedGroups(groupsIds).stream().collect(Collectors.toMap(ProductGroup::getId, Function.identity()));

            manufacturers.forEach((key, manufacturer) -> {
                decorate(manufacturer, manufacturersProperties, mappedGroups);
            });
        }
        return manufacturers;
    }

    private Manufacturer decorate(Manufacturer manufacturer, Map<Long, ItemPropertiesHolder> manufacturersProperties, Map<Long, ProductGroup> mappedGroups) {
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
        return manufacturer;
    }
}
