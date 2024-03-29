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

package org.yes.cart.service.dto.support;

import org.yes.cart.domain.dto.ShopDTO;

import java.util.Date;

/**
 * Price list filter DTO.
 *
 * User: denispavlov
 * Date: 12-11-29
 * Time: 6:53 PM
 */
public interface PriceListFilter {

    /**
     * @return shop filter.
     */
    ShopDTO getShop();

    /**
     * @return currency filter.
     */
    String getCurrencyCode();

    /**
     * @return product filter.
     */
    String getProductCode();

    /**
     * @return use exact match for product code.
     */
    Boolean getProductCodeExact();

    /**
     * @return from filter.
     */
    Date getFrom();

    /**
     * @return to filter.
     */
    Date getTo();

    /**
     * @return price tag.
     */
    String getTag();

    /**
     * @return price tag exact match.
     */
    Boolean getTagExact();

}
