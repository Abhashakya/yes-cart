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

package org.yes.cart.payment.impl;

import org.apache.commons.lang.StringUtils;
import org.yes.cart.payment.PaymentGateway;
import org.yes.cart.payment.dto.Payment;
import org.yes.cart.payment.dto.PaymentMiscParam;
import org.yes.cart.payment.dto.impl.PaymentImpl;
import org.yes.cart.payment.persistence.entity.PaymentGatewayParameter;
import org.yes.cart.payment.service.ConfigurablePaymentGateway;
import org.yes.cart.payment.service.PaymentGatewayConfigurationVisitor;
import org.yes.cart.payment.service.PaymentGatewayParameterService;
import org.yes.cart.util.HttpParamsUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
* User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public abstract class AbstractPaymentGatewayImpl implements ConfigurablePaymentGateway, PaymentGateway {

    private PaymentGatewayParameterService paymentGatewayParameterService;

    private Collection<PaymentGatewayParameter> allParameters = null;

    private String shopCode;

    /**
     * {@inheritDoc}
     */
    public String getShopCode() {
        return shopCode;
    }

    /**
     * {@inheritDoc}
     */
    public String getName(final String locale) {
        String pgName = getParameterValue("name_" + locale);
        if (pgName == null) {
            pgName = getParameterValue("name");
        }
        if (pgName == null) {
            pgName = getLabel();
        }
        return pgName;
    }

    /**
     * {@inheritDoc}
     */
    public String getHtmlForm(final String cardHolderName, final String locale, final BigDecimal amount,
                              final String currencyCode, final String orderGuid, final Payment payment) {
        return getHtmlForm(cardHolderName, locale);
    }


    /**
     * Default implementation.
     * @param cardHolderName card holder name.
     * @param locale locale
     * @return part of html form. 
     */
    protected String getHtmlForm(final String cardHolderName, final String locale) {
        String htmlForm = getParameterValue("htmlForm_" + locale);
        if (htmlForm == null) {
            htmlForm = getParameterValue("htmlForm");
        }
        if (htmlForm != null) {
            return htmlForm.replace("@CARDHOLDERNAME@", cardHolderName);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get the parameter value from given collection.
     *
     * @param valueLabel key to search
     * @return value or null if not found
     */
    public String getParameterValue(final String valueLabel) {
        if (valueLabel == null || valueLabel.startsWith("#")) {
            return null; // Need to prevent direct access to Shop specific attributes
        }
        if (shopCode != null && !"DEFAULT".equals(shopCode)) {
            final String shopSpecific = getParameterValueInternal("#" + shopCode + "_" + valueLabel);
            if (shopSpecific != null) {
                return shopSpecific;
            }
        }
        return getParameterValueInternal(valueLabel);
    }

    private String getParameterValueInternal(final String valueLabel) {
        final Collection<PaymentGatewayParameter> values = getPaymentGatewayParameters();
        for (PaymentGatewayParameter keyValue : values) {
            if (keyValue.getLabel().equals(valueLabel)) {
                return keyValue.getValue();
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public Payment createPaymentPrototype(final String operation, final Map parametersMap) {

        final Payment payment = new PaymentImpl();
        final Map<String, String> params = HttpParamsUtils.createSingleValueMap(parametersMap);

        payment.setCardHolderName(params.get("ccHolderName"));
        payment.setCardNumber(params.get("ccNumber"));
        payment.setCardExpireMonth(params.get("ccExpireMonth"));
        payment.setCardExpireYear(params.get("ccExpireYear"));
        payment.setCardCvv2Code(params.get("ccSecCode"));
        payment.setCardType(params.get("ccType"));
        payment.setShopperIpAddress(params.get(PaymentMiscParam.CLIENT_IP));


        return payment;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PaymentGatewayParameter> getPaymentGatewayParameters() {
        if (allParameters == null) {
            allParameters = paymentGatewayParameterService.findAll(getLabel(), shopCode);
        }
        return allParameters;
    }

    /**
     * {@inheritDoc}
     */

    public void deleteParameter(final String parameterLabel) {
        paymentGatewayParameterService.deleteByLabel(getLabel(), parameterLabel);
    }

    /**
     * {@inheritDoc}
     */

    public void addParameter(final PaymentGatewayParameter paymentGatewayParameter) {
        paymentGatewayParameterService.create(paymentGatewayParameter);
    }

    /**
     * {@inheritDoc}
     */

    public void updateParameter(final PaymentGatewayParameter paymentGatewayParameter) {
        paymentGatewayParameterService.update(paymentGatewayParameter);
    }

    /**
     * Parameter service for given gateway.
     *
     * @param paymentGatewayParameterService service
     */
    public void setPaymentGatewayParameterService(
            final PaymentGatewayParameterService paymentGatewayParameterService) {
        this.paymentGatewayParameterService = paymentGatewayParameterService;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(final PaymentGatewayConfigurationVisitor visitor) {
        this.shopCode = visitor.getConfiguration("shopCode");
    }


}
