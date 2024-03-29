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

package org.yes.cart.bulkimport.csv.impl;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.yes.cart.bulkimport.model.DataTypeEnum;
import org.yes.cart.bulkimport.model.ValueAdapter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: denispavlov
 * Date: 12-08-11
 * Time: 1:04 PM
 */
public class CsvImportValueAdapter implements ValueAdapter {

    private final GenericConversionService extendedConversionService;

    private static final Map<DataTypeEnum, Class> MAPPING = new HashMap<DataTypeEnum, Class>() {{
        put(DataTypeEnum.STRING,    String.class);
        put(DataTypeEnum.BOOLEAN,   Boolean.class);
        put(DataTypeEnum.INT,       Integer.class);
        put(DataTypeEnum.LONG,      Long.class);
        put(DataTypeEnum.DECIMAL,   BigDecimal.class);
        put(DataTypeEnum.DATETIME,  Date.class);
    }};

    public CsvImportValueAdapter(final GenericConversionService extendedConversionService) {
        this.extendedConversionService = extendedConversionService;
    }

    public Object fromRaw(final Object rawValue, final DataTypeEnum requiredType) {
        if (requiredType == null || !MAPPING.containsKey(requiredType)) {
            return rawValue;
        }
        return this.extendedConversionService.convert(rawValue,
                TypeDescriptor.valueOf(rawValue.getClass()),
                TypeDescriptor.valueOf(MAPPING.get(requiredType)));
    }
}
