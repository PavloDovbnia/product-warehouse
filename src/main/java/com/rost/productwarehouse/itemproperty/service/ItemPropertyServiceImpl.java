package com.rost.productwarehouse.itemproperty.service;

import com.github.slugify.Slugify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.itemproperty.ItemProperty;
import com.rost.productwarehouse.itemproperty.ItemPropertyValue;
import com.rost.productwarehouse.itemproperty.dao.ItemPropertyDao;
import com.rost.productwarehouse.itemproperty.dao.ItemPropertyValueDao;
import com.rost.productwarehouse.itemproperty.exceptions.ItemPropertySaveValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemPropertyServiceImpl implements ItemPropertyService {

    private final ItemPropertyDao itemPropertyDao;
    private final ItemPropertyValueDao itemPropertyValueDao;

    public ItemPropertyServiceImpl(ItemPropertyDao itemPropertyDao, ItemPropertyValueDao itemPropertyValueDao) {
        this.itemPropertyDao = itemPropertyDao;
        this.itemPropertyValueDao = itemPropertyValueDao;
    }

    @Override
    public List<ItemProperty> getItemProperties(ItemLevel itemLevel) {
        switch (itemLevel) {
            case PRODUCT:
                return itemPropertyDao.getProperties(Lists.newArrayList(ItemLevel.PRODUCT, ItemLevel.PRODUCT_GROUP, ItemLevel.MANUFACTURER));
            case PRODUCT_GROUP:
                return itemPropertyDao.getProperties(Lists.newArrayList(ItemLevel.PRODUCT_GROUP, ItemLevel.MANUFACTURER));
            case MANUFACTURER:
                return itemPropertyDao.getProperties(Lists.newArrayList(ItemLevel.MANUFACTURER));
            default:
                return Lists.newArrayList();
        }
    }

    @Override
    public Map<Long, ItemPropertiesHolder> getPropertiesValues(List<Long> productsIds, ItemLevel itemLevel) {
        switch (itemLevel) {
            case PRODUCT:
                return convert(itemPropertyValueDao.getProductsProperties(productsIds));
            case PRODUCT_GROUP:
                return convert(itemPropertyValueDao.getGroupsProperties(productsIds));
            case MANUFACTURER:
                return convert(itemPropertyValueDao.getManufacturersProperties(productsIds));
            default:
                return Maps.newHashMap();
        }
    }

    @Override
    public void saveProperty(ItemProperty itemProperty) {
        if (itemProperty.getId() <= 0L) {
            itemProperty.setToNew();
            String token = new Slugify().slugify(itemProperty.getName());
            itemProperty.setToken(token);
        }
        if (itemPropertyDao.getProperty(itemProperty.getToken(), itemProperty.getItemLevel()) == null) {
            itemPropertyDao.saveProperty(itemProperty);
        } else {
            throw new ItemPropertySaveValidationException("Can't save property. The other " + itemProperty.getName() + " Property on " + itemProperty.getItemLevel().name() + " level exists");
        }
    }

    @Override
    public void deleteProperty(long id) {
        itemPropertyDao.deleteProperty(id);
    }

    @Override
    public void saveItemValues(long itemId, Map<String, ItemPropertyValue<?>> values, ItemLevel itemLevel) {
        switch (itemLevel) {
            case PRODUCT:
                itemPropertyValueDao.saveProductValues(itemId, values);
                break;
            case PRODUCT_GROUP:
                itemPropertyValueDao.saveGroupValues(itemId, values);
                break;
            case MANUFACTURER:
                itemPropertyValueDao.saveManufacturerValues(itemId, values);
                break;
        }
    }

    @Override
    public void deleteItemValues(long itemId, ItemLevel itemLevel) {
        switch (itemLevel) {
            case PRODUCT:
                itemPropertyValueDao.deleteProductValues(itemId);
                break;
            case PRODUCT_GROUP:
                itemPropertyValueDao.deleteGroupValues(itemId);
                break;
            case MANUFACTURER:
                itemPropertyValueDao.deleteManufacturerValues(itemId);
                break;
        }
    }

    private Map<Long, ItemPropertiesHolder> convert(Map<Long, Map<String, ItemPropertyValue<?>>> itemsPropertiesValues) {
        return itemsPropertiesValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new ItemPropertiesHolder(entry.getValue())));
    }
}
