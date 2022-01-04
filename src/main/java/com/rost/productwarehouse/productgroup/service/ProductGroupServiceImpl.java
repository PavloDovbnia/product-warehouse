package com.rost.productwarehouse.productgroup.service;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.itemproperty.service.ItemPropertyService;
import com.rost.productwarehouse.product.Product;
import com.rost.productwarehouse.product.service.ProductService;
import com.rost.productwarehouse.productcategory.ProductCategory;
import com.rost.productwarehouse.productcategory.service.ProductCategoryService;
import com.rost.productwarehouse.productgroup.ProductGroup;
import com.rost.productwarehouse.productgroup.dao.ProductGroupDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductGroupServiceImpl implements ProductGroupService {

    private final ProductGroupDao productGroupDao;
    private final ProductService productService;
    private final ItemPropertyService itemPropertyService;
    private final ProductCategoryService productCategoryService;

    public ProductGroupServiceImpl(ProductGroupDao productGroupDao, ProductService productService, ItemPropertyService itemPropertyService, ProductCategoryService productCategoryService) {
        this.productGroupDao = productGroupDao;
        this.productService = productService;
        this.itemPropertyService = itemPropertyService;
        this.productCategoryService = productCategoryService;
    }

    @Override
    public List<ProductGroup> getGroups() {
        return productGroupDao.getGroups();
    }

    @Override
    public List<ProductGroup> getDecoratedGroups(List<Long> groupsIds) {
        return decorateGroups(productGroupDao.getGroups(groupsIds));
    }

    @Override
    public ProductGroup getDecoratedGroup(long groupId) {
        List<ProductGroup> groups = decorateGroups(productGroupDao.getGroups(Lists.newArrayList(groupId)));
        return CollectionUtils.isNotEmpty(groups) ? groups.iterator().next() : null;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public long storeGroup(ProductGroup group) {
        if (group.getId() <= 0L) {
            group.setToNew();
        }
        long groupId = productGroupDao.storeProductGroup(group);
        group.setId(groupId);
        productGroupDao.storeGroupProducts(group.getId(), group.getProductsIds());
        if (group.getCategory() != null) {
            productGroupDao.storeGroupCategory(groupId, group.getCategory().getId());
        }

        if (MapUtils.isNotEmpty(group.getProperties().getProperties())) {
            itemPropertyService.saveItemValues(groupId, group.getProperties().getProperties(), ItemLevel.PRODUCT_GROUP);
        }
        return groupId;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void deleteGroup(long groupId) {
        productGroupDao.deleteProductGroup(groupId);
    }

    private List<ProductGroup> decorateGroups(List<ProductGroup> groups) {
        if (CollectionUtils.isNotEmpty(groups)) {
            List<Long> groupsIds = groups.stream().map(ProductGroup::getId).collect(Collectors.toList());
            Map<Long, ItemPropertiesHolder> groupsProperties = itemPropertyService.getPropertiesValues(groupsIds, ItemLevel.PRODUCT_GROUP);

            List<Long> productsIds = groups.stream().flatMap(g -> g.getProductsIds().stream()).collect(Collectors.toList());
            Map<Long, Product> mappedProducts = productService.getDecoratedProducts(productsIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));

            Map<Long, ProductCategory> mappedCategories = productCategoryService.getCategories().stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));

            groups.forEach(group -> {
                ItemPropertiesHolder holder = groupsProperties.get(group.getId());
                if (holder != null) {
                    group.getProperties().addProperties(holder.getProperties());
                }

                if (group.getCategory() != null && mappedCategories.containsKey(group.getCategory().getId())) {
                    ProductCategory category = mappedCategories.get(group.getCategory().getId());
                    group.setCategory(category);
                }

                group.setProducts(Lists.newArrayList());
                group.getProductsIds().forEach(productId -> {
                    Product product = mappedProducts.get(productId);
                    if (product != null) {
                        group.getProducts().add(product);
                    }
                });
            });
        }
        return groups;
    }
}
