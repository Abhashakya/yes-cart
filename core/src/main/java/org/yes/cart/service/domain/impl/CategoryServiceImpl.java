/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.service.domain.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.yes.cart.constants.AttributeNamesKeys;
import org.yes.cart.constants.Constants;
import org.yes.cart.dao.GenericDAO;
import org.yes.cart.domain.entity.AttrValue;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.entity.Shop;
import org.yes.cart.domain.entity.ShopCategory;
import org.yes.cart.domain.i18n.impl.FailoverStringI18NModel;
import org.yes.cart.service.domain.CategoryService;

import java.util.*;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class CategoryServiceImpl extends BaseGenericServiceImpl<Category> implements CategoryService {

    private final GenericDAO<Category, Long> categoryDao;

    private final GenericDAO<ShopCategory, Long> shopCategoryDao;

    private final GenericDAO<Shop, Long> shopDao;

    /**
     * Construct service to manage categories
     *
     * @param categoryDao     category dao to use
     * @param shopCategoryDao shop category dao to use
     * @param shopDao         shop dao
     */
    public CategoryServiceImpl(
            final GenericDAO<Category, Long> categoryDao,
            final GenericDAO<ShopCategory, Long> shopCategoryDao,
            final GenericDAO<Shop, Long> shopDao) {
        super(categoryDao);
        this.categoryDao = categoryDao;
        this.shopCategoryDao = shopCategoryDao;
        this.shopDao = shopDao;
    }

    /**
     * Get the top level categories assigned to shop.
     *
     * @param shopId given shop
     * @return ordered by rank list of assigned top level categories
     */
    @Cacheable(value = "categoryService-topLevelCategories"/*, key="shop.shopId"*/)
    public List<Category> getTopLevelCategories(final Long shopId) {
        return categoryDao.findByNamedQuery("TOPCATEGORIES.BY.SHOPID", shopId, new Date());
    }

    /**
     * {@inheritDoc}
     */
    public List<Category> findAllByShopId(final long shopId) {
        return categoryDao.findByNamedQuery("ALL.TOPCATEGORIES.BY.SHOPID", shopId);
    }


    /**
     * {@inheritDoc}
     */
    @CacheEvict(value = {
            "categoryService-topLevelCategories",
            "categoryService-currentCategoryMenu",
            "breadCrumbBuilder-breadCrumbs",
            "shopService-shopByCode",
            "shopService-shopById",
            "shopService-shopByDomainName",
            "shopService-allShops",
            "shopService-shopCategoriesIds",
            "shopService-shopAllCategoriesIds",
            "categoryService-searchInSubcategory",
            "categoryService-categoryNewArrivalLimit",
            "categoryService-categoryNewArrivalDate"
    }, allEntries = true)
    public ShopCategory assignToShop(final long categoryId, final long shopId) {
        final ShopCategory shopCategory = shopCategoryDao.getEntityFactory().getByIface(ShopCategory.class);
        shopCategory.setCategory(categoryDao.findById(categoryId));
        shopCategory.setShop(shopDao.findById(shopId));
        return shopCategoryDao.create(shopCategory);
    }

    /**
     * {@inheritDoc}
     */
    @CacheEvict(value = {
            "categoryService-topLevelCategories",
            "categoryService-currentCategoryMenu",
            "breadCrumbBuilder-breadCrumbs",
            "shopService-shopByCode",
            "shopService-shopById",
            "shopService-shopByDomainName",
            "shopService-allShops",
            "shopService-shopCategoriesIds",
            "shopService-shopAllCategoriesIds",
            "categoryService-searchInSubcategory",
            "categoryService-categoryNewArrivalLimit",
            "categoryService-categoryNewArrivalDate"
    }, allEntries = true)
    public void unassignFromShop(final long categoryId, final long shopId) {
        ShopCategory shopCategory = shopCategoryDao.findSingleByNamedQuery(
                "SHOP.CATEGORY",
                categoryId,
                shopId);
        shopCategoryDao.delete(shopCategory);

    }


    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-rootCategory")
    public Category getRootCategory() {
        return categoryDao.findSingleByNamedQuery("ROOTCATEORY");
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-categoryTemplate")
    public String getCategoryTemplate(final long categoryId) {
        final Category category = proxy().findById(categoryId);
        if (category != null && !category.isRoot()) {
            if (StringUtils.isBlank(category.getUitemplate())) {
                return proxy().getCategoryTemplate(category.getParentId());
            } else {
                return category.getUitemplate();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-categoryNewArrivalLimit")
    public int getCategoryNewArrivalLimit(final long categoryId, final long shopId) {

        if (categoryId > 0L) {
            final String value = proxy().getCategoryAttributeRecursive(
                    null, categoryId, AttributeNamesKeys.Category.CATEGORY_ITEMS_NEW_ARRIVAL, null);
            if (value != null) {
                final int limit = NumberUtils.toInt(value, 0);
                if (limit > 1) {
                    return limit;
                }
            }
        }

        return Constants.RECOMMENDATION_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-categoryNewArrivalDate")
    public Date getCategoryNewArrivalDate(final long categoryId, final long shopId) {

        if (categoryId > 0L) {
            final String value = proxy().getCategoryAttributeRecursive(
                    null, categoryId, AttributeNamesKeys.Category.CATEGORY_NEW_ARRIVAL_DAYS_OFFSET, null);
            if (value != null) {
                final int days = NumberUtils.toInt(value, 0);
                if (days > 1) {
                    final Calendar beforeDays = Calendar.getInstance();
                    beforeDays.add(Calendar.DAY_OF_YEAR, -days);
                    beforeDays.set(Calendar.HOUR, 0);
                    beforeDays.set(Calendar.MINUTE, 0);
                    beforeDays.set(Calendar.SECOND, 0);
                    beforeDays.set(Calendar.MILLISECOND, 0);
                    return beforeDays.getTime();
                }
            }
        }

        final List<Object> value = getGenericDao().findQueryObjectByNamedQuery("SHOP.ATTRIBUTE.BY.ID.AND.ATTRCODE",
                shopId, AttributeNamesKeys.Shop.SHOP_NEW_ARRIVAL_DAYS_OFFSET);
        final int days;
        if (value != null && value.size() > 0) {
            days = NumberUtils.toInt((String) value.get(0), 15);
        } else {
            days = 15;
        }
        final Calendar beforeDays = Calendar.getInstance();
        beforeDays.add(Calendar.DAY_OF_YEAR, -days);
        beforeDays.set(Calendar.HOUR, 0);
        beforeDays.set(Calendar.MINUTE, 0);
        beforeDays.set(Calendar.SECOND, 0);
        beforeDays.set(Calendar.MILLISECOND, 0);
        return beforeDays.getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-searchInSubcategory")
    public boolean isSearchInSubcategory(final long categoryId, final long shopId) {

        if (categoryId > 0L) {
            final String value = proxy().getCategoryAttributeRecursive(
                    null, categoryId, AttributeNamesKeys.Category.CATEGORY_INCLUDE_SUBCATEGORIES_IN_SEARCH, null);
            if (value != null) {
                return Boolean.valueOf(value);
            }
        }

        final List<Object> value = getGenericDao().findQueryObjectByNamedQuery("SHOP.ATTRIBUTE.BY.ID.AND.ATTRCODE",
                shopId, AttributeNamesKeys.Shop.SHOP_INCLUDE_SUBCATEGORIES_IN_SEARCH);
        if (value != null && value.size() > 0) {
            return Boolean.valueOf((String) value.get(0));
        }
        return false;
    }

    /**
     * Get the value of given attribute. If value not present in given category
     * failover to parent category will be used.
     *
     *
     * @param locale        locale for localisable value (or null for raw)
     * @param categoryId    given category
     * @param attributeName attribute name
     * @param defaultValue  default value will be returned if value not found in hierarchy
     * @return value of given attribute name or defaultValue if value not found in category hierarchy
     */
    @Cacheable(value = "categoryService-categoryAttributeRecursive")
    public String getCategoryAttributeRecursive(final String locale, final long categoryId, final String attributeName, final String defaultValue) {

        final Category category = proxy().getById(categoryId);

        if (category == null || attributeName == null) {
            return null;
        }

        final AttrValue attrValue = category.getAttributeByCode(attributeName);
        if (attrValue != null) {
            final String val;
            if (locale == null) {
                val = attrValue.getVal();
            } else {
                val = new FailoverStringI18NModel(attrValue.getDisplayVal(), attrValue.getVal()).getValue(locale);
            }
            if (!StringUtils.isBlank(val)) {
                return val;
            }
        }

        if (category.isRoot()) {
            return defaultValue; //root of hierarchy
        }

        return proxy().getCategoryAttributeRecursive(locale, category.getParentId(), attributeName, defaultValue);

    }

    /**
     * Get the values of given attributes. If value not present in given category
     * failover to parent category will be used.  In case if attribute value for first
     * attribute will be found, the rest values also will be collected form the same category.
     *
     *
     * @param locale           locale for localisable value (or null for raw)
     * @param categoryId       given category
     * @param attributeNames set of attributes, to collect values.
     * @return value of given attribute name or defaultValue if value not found in category hierarchy
     */
    @Cacheable(value = "categoryService-categoryAttributesRecursive")
    public String[] getCategoryAttributeRecursive(final String locale, final long categoryId, final String[] attributeNames) {

        final Category category;

        if (categoryId > 0L && attributeNames != null && attributeNames.length > 0) {
            category = proxy().getById(categoryId);
        } else {
            return null;
        }

        if (category == null) {
            return null;
        }

        final String[] rez = new String[attributeNames.length];
        boolean hasValue = false;
        for (int i = 0; i < attributeNames.length; i++) {
            final String attributeName = attributeNames[i];
            final String val = proxy().getCategoryAttributeRecursive(locale, categoryId, attributeName, null);
            if (val != null) {
                hasValue = true;
            }
            rez[i] = val;
        }

        if (hasValue) {
            return rez;
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-categoryHasChildren")
    public boolean isCategoryHasChildren(final long categoryId) {
        List<Object> count = categoryDao.findQueryObjectByNamedQuery("CATEGORY.SUBCATEGORY.COUNT", categoryId);
        if (count != null && count.size() == 1) {
            int qty = ((Number) count.get(0)).intValue();
            if (qty > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-childCategories")
    public List<Category> getChildCategories(final long categoryId) {
        return findChildCategoriesWithAvailability(categoryId, true);
    }

    /**
     * {@inheritDoc}
     */
    public List<Category> findChildCategoriesWithAvailability(final long categoryId, final boolean withAvailability) {
        if (withAvailability) {
            return categoryDao.findByNamedQuery(
                    "CATEGORIES.BY.PARENTID",
                    categoryId,
                    new Date()
            );
        } else {
            return categoryDao.findByNamedQuery(
                    "CATEGORIES.BY.PARENTID.WITHOUT.DATE.FILTERING",
                    categoryId
            );
        }
    }


    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-childCategoriesRecursive")
    public Set<Category> getChildCategoriesRecursive(final long categoryId) {
        final Category thisCat = proxy().findById(categoryId);
        if (thisCat != null) {
            final Set<Category> all = new HashSet<Category>();
            all.add(thisCat);
            loadChildCategoriesRecursiveInternal(all, thisCat);
            return all;
        }
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-childCategoriesRecursiveIds")
    public Set<Long> getChildCategoriesRecursiveIds(final long categoryId) {
        final Set<Category> cats = getChildCategoriesRecursive(categoryId);
        if (cats.isEmpty()) {
            return Collections.emptySet();
        }
        return transform(cats);
    }

    private void loadChildCategoriesRecursiveInternal(final Set<Category> result, final Category category) {
        List<Category> categories = proxy().getChildCategories(category.getCategoryId());
        result.addAll(categories);
        for (Category subCategory : categories) {
            loadChildCategoriesRecursiveInternal(result, subCategory);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "categoryService-categoryHasSubcategory")
    public boolean isCategoryHasSubcategory(final long topCategoryId, final long subCategoryId) {
        final Category start = proxy().findById(subCategoryId);
        if (start != null) {
            if (subCategoryId == topCategoryId) {
                return true;
            } else {
                final List<Category> list = new ArrayList<Category>();
                list.add(start);
                addParent(list, topCategoryId);
                return list.get(list.size() - 1).getCategoryId() == topCategoryId;
            }
        }
        return false;
    }

    private void addParent(final List<Category> categoryChain, final long categoryIdStopAt) {
        final Category cat = categoryChain.get(categoryChain.size() - 1);
        if (!cat.isRoot()) {
            final Category parent = findById(cat.getParentId());
            if (parent != null) {
                categoryChain.add(parent);
                if (parent.getCategoryId() != categoryIdStopAt) {
                    addParent(categoryChain, categoryIdStopAt);
                }
            }
        }
    }

    /**
     * {@inheritDoc} Just to cache
     */
    @Cacheable(value = "categoryService-byId")
    public Category getById(final long pk) {
        return getGenericDao().findById(pk);
    }

    private Set<Long> transform(final Collection<Category> categories) {
        final Set<Long> result = new LinkedHashSet<Long>(categories.size());
        for (Category category : categories) {
            result.add(category.getCategoryId());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Long findCategoryIdBySeoUri(final String seoUri) {
        List<Object> list = categoryDao.findQueryObjectByNamedQuery("CATEGORY.ID.BY.SEO.URI", seoUri);
        if (list != null && !list.isEmpty()) {
            final Object id = list.get(0);
            if (id instanceof Long) {
                return (Long) id;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Long findCategoryIdByGUID(final String guid) {
        List<Object> list = categoryDao.findQueryObjectByNamedQuery("CATEGORY.ID.BY.GUID", guid);
        if (list != null && !list.isEmpty()) {
            final Object id = list.get(0);
            if (id instanceof Long) {
                return (Long) id;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String findSeoUriByCategoryId(final Long categoryId) {
        List<Object> list = categoryDao.findQueryObjectByNamedQuery("SEO.URI.BY.CATEGORY.ID", categoryId);
        if (list != null && !list.isEmpty()) {
            final Object uri = list.get(0);
            if (uri instanceof String) {
                return (String) uri;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Category findCategoryIdBySeoUriOrGuid(final String seoUriOrGuid) {

        final Category category = getGenericDao().findSingleByNamedQuery("CATEGORY.BY.SEO.URI", seoUriOrGuid);
        if (category == null) {
            return getGenericDao().findSingleByNamedQuery("CATEGORY.BY.GUID", seoUriOrGuid);
        }
        return category;
    }

    /**
     * {@inheritDoc}
     */
    public List<Category> findByProductId(final long productId) {
        return categoryDao.findByNamedQuery(
                "CATEGORIES.BY.PRODUCTID",
                productId
        );
    }

    /**
     * {@inheritDoc}
     */
    @CacheEvict(value = {
            "categoryService-topLevelCategories",
            "categoryService-currentCategoryMenu",
            "breadCrumbBuilder-breadCrumbs",
            "categoryService-rootCategory",
            "categoryService-categoryHasChildren",
            "categoryService-childCategories",
            "categoryService-childCategoriesRecursive",
            "categoryService-childCategoriesRecursiveIds",
            "categoryService-categoryHasSubcategory",
            "categoryService-byId",
            "shopService-shopCategoriesIds",
            "shopService-shopAllCategoriesIds"
    }, allEntries = true)
    public Category create(Category instance) {
        return super.create(instance);
    }

    /**
     * {@inheritDoc}
     */
    @CacheEvict(value = {
            "categoryService-topLevelCategories",
            "categoryService-currentCategoryMenu",
            "breadCrumbBuilder-breadCrumbs",
            "categoryService-rootCategory",
            "categoryService-categoryTemplate",
            "categoryService-searchInSubcategory",
            "categoryService-categoryNewArrivalLimit",
            "categoryService-categoryNewArrivalDate",
            "categoryService-categoryAttributeRecursive",
            "categoryService-categoryAttributesRecursive",
            "categoryService-categoryHasChildren",
            "categoryService-childCategories",
            "categoryService-childCategoriesRecursive",
            "categoryService-childCategoriesRecursiveIds",
            "categoryService-categoryHasSubcategory",
            "categoryService-byId",
            "shopService-shopCategoriesIds",
            "shopService-shopAllCategoriesIds"
    }, allEntries = true)
    public Category update(Category instance) {
        return super.update(instance);
    }

    /**
     * {@inheritDoc}
     */
    @CacheEvict(value = {
            "categoryService-topLevelCategories",
            "categoryService-currentCategoryMenu",
            "breadCrumbBuilder-breadCrumbs",
            "categoryService-rootCategory",
            "categoryService-categoryTemplate",
            "categoryService-searchInSubcategory",
            "categoryService-categoryNewArrivalLimit",
            "categoryService-categoryNewArrivalDate",
            "categoryService-categoryAttributeRecursive",
            "categoryService-categoryAttributesRecursive",
            "categoryService-categoryHasChildren",
            "categoryService-childCategories",
            "categoryService-childCategoriesRecursive",
            "categoryService-childCategoriesRecursiveIds",
            "categoryService-categoryHasSubcategory",
            "categoryService-byId"
    }, allEntries = true)
    public void delete(Category instance) {
        super.delete(instance);
    }


    private CategoryService proxy;

    private CategoryService proxy() {
        if (proxy == null) {
            proxy = getSelf();
        }
        return proxy;
    }

    /**
     * @return self proxy to reuse AOP caching
     */
    public CategoryService getSelf() {
        // Spring lookup method to get self proxy
        return null;
    }

}
