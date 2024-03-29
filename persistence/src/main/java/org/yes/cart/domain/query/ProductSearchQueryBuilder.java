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

package org.yes.cart.domain.query;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 16:13:01
 */
public interface ProductSearchQueryBuilder extends SearchQueryBuilder {

    //can be used in sort order
    public final static String PRODUCT_NAME_FIELD = "name";
    public final static String PRODUCT_DISPLAYNAME_FIELD = "displayName";
    public final static String PRODUCT_DISPLAYNAME_ASIS_FIELD = "displayNameAsIs"; //for projections only
    public final static String PRODUCT_CODE_FIELD = "code";
    public final static String PRODUCT_MANUFACTURER_CODE_FIELD = "manufacturerCode";
    public final static String PRODUCT_DEFAULT_SKU_CODE_FIELD = "defaultSku";
    public final static String PRODUCT_CODE_STEM_FIELD = "code_stem";
    public final static String PRODUCT_MANUFACTURER_CODE_STEM_FIELD = "manufacturerCode_stem";
    public final static String PRODUCT_MULTISKU = "multisku";

    public final static String PRODUCT_CREATED_FIELD = "createdTimestamp"; //for projections only
    public final static String PRODUCT_FEATURED_FIELD = "featured"; //for projections only
    public final static String PRODUCT_AVAILABILITY_FROM_FIELD = "availablefrom"; //for projections only
    public final static String PRODUCT_AVAILABILITY_TO_FIELD = "availableto"; //for projections only
    public final static String PRODUCT_AVAILABILITY_FIELD = "availability"; //for projections only
    public final static String PRODUCT_QTY_FIELD = "qtyOnWarehouse"; //for projections only
    public final static String PRODUCT_DEFAULTIMAGE_FIELD = "defaultImage"; //for projections only
    public final static String PRODUCT_DESCRIPTION_ASIS_FIELD = "descriptionAsIs"; //for projections only
    public final static String PRODUCT_MIN_QTY_FIELD = "minOrderQuantity"; //for projections only
    public final static String PRODUCT_MAX_QTY_FIELD = "maxOrderQuantity"; //for projections only
    public final static String PRODUCT_STEP_QTY_FIELD = "stepOrderQuantity"; //for projections only

    public final static String PRODUCT_TAG_FIELD = "tag";
    public final static String SKU_PRODUCT_CODE_FIELD = "sku.code";
    public final static String SKU_PRODUCT_CODE_STEM_FIELD = "sku.code_stem";
    public final static String SKU_PRODUCT_MANUFACTURER_CODE_FIELD = "sku.manufacturerCode";
    public final static String SKU_PRODUCT_MANUFACTURER_CODE_STEM_FIELD = "sku.manufacturerCode_stem";
    public final static String PRODUCT_NAME_SORT_FIELD = "name_sort";
    public final static String PRODUCT_DISPLAYNAME_SORT_FIELD = "displayName_sort";
    public final static String PRODUCT_DESCRIPTION_FIELD = "description";
    public final static String PRODUCT_DESCRIPTION_STEM_FIELD = "description_stem";
    public final static String BRAND_FIELD = "brand";
    public final static String ATTRIBUTE_CODE_FIELD = "attribute.attribute";
    public final static String ATTRIBUTE_VALUE_FIELD = "attribute.val";
    public final static String ATTRIBUTE_VALUE_SEARCH_FIELD = "attribute.attrvalsearch";

    public final static String PRODUCT_CATEGORY_FIELD = "productCategory.category";
    public final static String PRODUCT_SHOP_FIELD = "productShopId";
    public final static String PRODUCT_ID_FIELD = "productId";
    public final static String SKU_ID_FIELD = "sku.skuId"; //////////////////////////////////////////////

    // not really a field, but used in query type determination
    public final static String PRODUCT_PRICE = "price";

    // not really a field, but used for global search
    public final static String QUERY = "query";


    public final String TAG_NEWARRIVAL = "newarrival";

    //--------------------------------------- categories -----------------------------------

    public final static String CATEGORY_NAME_FIELD = "name";
    public final static String CATEGORY_DISPLAYNAME_FIELD = "displayName";
    public final static String CATEGORY_DESCRIPTION_FIELD = "description";
    public final static String CATEGORY_SEO_URI_FIELD = "seo.uri";
    public final static String CATEGORY_SEO_TITLE_FIELD = "seo.title";
    public final static String CATEGORY_SEO_METAKEYWORDS_FIELD = "seo.metakeywords";
    public final static String CATEGORY_SEO_METADESCRIPTION_FIELD = "seo.metadescription";

}
