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

package org.yes.cart.web.support.service.impl;

import org.apache.commons.lang.StringUtils;
import org.yes.cart.constants.AttributeNamesKeys;
import org.yes.cart.constants.Constants;
import org.yes.cart.domain.entity.AttrValue;
import org.yes.cart.domain.entity.Attributable;
import org.yes.cart.web.support.service.CategoryImageRetrieveStrategy;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 2011-May-17
 * Time: 16:18:53
 */
public class CategoryImageRetrieveStrategyAttributeImpl implements CategoryImageRetrieveStrategy {

    private final String attributeCode;

    /**
     * Construct image retrieve strategy
     *
     * @param attributeCode attribute name
     */
    public CategoryImageRetrieveStrategyAttributeImpl(final String attributeCode) {
        this.attributeCode = attributeCode;
    }

    /**
     * {@inheritDoc}
     */
    public String getImageName(final Attributable category, final String attributeCodeHint, final String locale) {
        final AttrValue attrValue = category.getAttributeByCode(StringUtils.isBlank(attributeCodeHint) ? attributeCode : attributeCodeHint);
        if (attrValue == null) {
            return Constants.NO_IMAGE;
        }
        return attrValue.getVal();
    }

    /**
     * {@inheritDoc}
     */
    public String getImageRepositoryUrlPattern() {
        return Constants.CATEGORY_IMAGE_REPOSITORY_URL_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    public String getImageAttributePrefix() {
        return AttributeNamesKeys.Category.CATEGORY_IMAGE_PREFIX;
    }

}
