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

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Test;
import org.yes.cart.BaseCoreDBTestCase;
import org.yes.cart.constants.ServiceSpringKeys;
import org.yes.cart.domain.entity.Carrier;
import org.yes.cart.service.domain.CarrierService;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CarrierSlaServiceImplTest extends BaseCoreDBTestCase {

    private CarrierService carrierService;

    protected IDataSet createDataSet() throws Exception {
        return new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream("initialdata_carrier.xml"), false);
    }

    @Before
    public void setUp() {
        carrierService = (CarrierService) ctx().getBean(ServiceSpringKeys.CARRIER_SERVICE);
        super.setUp();
    }

    @Test
    public void testFindCarriersFilterByCurrency() {
        List<Carrier> carriers;
        carriers = carrierService.getCarriersByShopIdAndCurrency(20L, "USD"); // no shop 20
        assertEquals(0, carriers.size());
        carriers = carrierService.getCarriersByShopIdAndCurrency(10L, "USD"); // 2 USD Carriers
        assertEquals(2, carriers.size());
        assertEquals(1, carriers.get(0).getCarrierSla().size());
        assertEquals(1, carriers.get(1).getCarrierSla().size());
        carriers = carrierService.getCarriersByShopIdAndCurrency(10L, "RUB"); // 1 RUB Carrier
        assertEquals(1, carriers.size());
        assertEquals(1, carriers.get(0).getCarrierSla().size());
        carriers = carrierService.getCarriersByShopIdAndCurrency(10L, "MWZ"); // no web money carriers
        assertEquals(0, carriers.size());
    }
}
