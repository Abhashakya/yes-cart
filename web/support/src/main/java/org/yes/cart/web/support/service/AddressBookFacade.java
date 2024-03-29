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

package org.yes.cart.web.support.service;

import org.yes.cart.domain.entity.*;

import java.util.List;

/**
 * User: denispavlov
 * Date: 13-10-15
 * Time: 12:22 AM
 */
public interface AddressBookFacade {

    /**
     * Returns true if customer identified by given email address
     * has at least one address in the address book.
     *
     * @param email customer email
     *
     * @return true if at least one address exists
     */
    boolean customerHasAtLeastOneAddress(String email);


    /**
     * Get addresses applicable for given shop.
     *
     * @param customer customer
     * @param shop shop
     * @param addressType address type
     * @return list of applicable addresses
     */
    List<Address> getAddresses(Customer customer, Shop shop, String addressType);

    /**
     * Get existing address or create new instance object.
     *
     *
     *
     * @param customer customer of the address
     * @param addrId address PK
     * @param addressType type of address
     * @return address instance
     */
    Address getAddress(Customer customer, String addrId, String addressType);

    /**
     * Create or update address object.
     *
     * @param address address
     */
    void createOrUpdate(Address address);

    /**
     * Removes address and resets default address.
     *
     * @param address address to remove
     */
    void remove(Address address);

    /**
     * Set given address as default inside address type group.
     *
     * @param address instance to update
     *
     * @return persisted instance of address.
     */
    Address useAsDefault(Address address);

    /**
     * Find all countries.
     *
     * @param shopCode shop code
     * @param addressType address type
     *
     * @return all countries
     */
    List<Country> getAllCountries(String shopCode, String addressType);

    /**
     * Find by country code.
     *
     * @param countryCode country code.
     * @return list of states , that belong to given country.
     */
    List<State> getStatesByCountry(String countryCode);

}
