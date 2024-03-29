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

package org.yes.cart.domain.entity;

import java.util.Collection;

/**
 * Brand
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 11:12:54
 */
public interface Brand extends Auditable {

    /**
     * Get brand pk.
     *
     * @return brand pk.
     */
    long getBrandId();

    /**
     * Set brand pk.
     *
     * @param brandId brand pk.
     */
    void setBrandId(long brandId);

    /**
     * Brand name.
     *
     * @return name.
     */
    String getName();

    /**
     * Set brand name.
     *
     * @param name name
     */
    void setName(String name);

    /**
     * Get description.
     *
     * @return description.
     */
    String getDescription();

    /**
     * Set description.
     *
     * @param description description
     */
    void setDescription(String description);

    /**
     * Get brand's attributes.
     *
     * @return brand's attributes.
     */
    Collection<AttrValueBrand> getAttributes();

    /**
     * Set brand's attributes.
     *
     * @param attributes brand's attributes.
     */
    void setAttributes(Collection<AttrValueBrand> attributes);


}


