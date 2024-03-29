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


/**
 * Single shop can serve several urls, for example shop.com.ru, www.shop.com.ru, wap.shop.com.ru.
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 11:12:54
 */
public interface ShopUrl extends Auditable {

    /**
     * @return primary key value.
     */
    long getStoreUrlId();

    void setStoreUrlId(long storeUrlId);

    /**
     * @return shop url.
     */
    String getUrl();

    void setUrl(String url);

    /**
     * @return {@link Shop}
     */
    Shop getShop();

    void setShop(Shop shop);

}


