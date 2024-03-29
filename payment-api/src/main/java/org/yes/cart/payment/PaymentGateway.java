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

package org.yes.cart.payment;

import org.yes.cart.payment.dto.Payment;
import org.yes.cart.payment.dto.PaymentGatewayFeature;
import org.yes.cart.payment.persistence.entity.PaymentGatewayParameter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 10:22:53
 */
public interface PaymentGateway extends Serializable {

    String AUTH = "AUTH";         //Authorize a payment.
    String REVERSE_AUTH = "REVERSE_AUTH";  // Reverse the authorization.
    String CAPTURE = "CAPTURE";  //fund capture
    String AUTH_CAPTURE = "AUTH_CAPTURE"; //
    String VOID_CAPTURE = "VOID_CAPTURE";   //void capture fund  operation
    String REFUND = "REFUND"; //return money back to client

    /**
     * Locale specific name for given payment gateway.
     *
     * @param locale     to get localized name
     *
     * @return localized name
     */
    String getName(String locale);

    /**
     * Shop code of the shop for this PG.
     *
     * @return shop code
     */
    String getShopCode();

    /**
     * Get the whole or part of html form to fill data for payment.
     *
     * @param cardHolderName card  holder name hint based on customer profile
     * @param locale         to get localized html
     * @param currencyCode   shopping cart currency code
     * @param amount         amount payment , used for external payment processing
     * @param orderGuid order guid to restore real order after jumping of payment gateway sites.
     * @param payment payment details , only a few gateways require this parameter
     *
     * @return html.
     */
    String getHtmlForm(String cardHolderName, String locale, BigDecimal amount, String currencyCode, String orderGuid, Payment payment);

    /**
     * Authorize and capture payment. Not all gateways allow to capture payment without order delivery.
     * A credit card transaction request to authorize and capture, or settle,
     * funds for a purchase. The payment gateway submits the request to the card issuing bank
     * for authorization and upon approval, will automatically submit the
     * transaction for settlement.
     * <p/>
     * This method must be used in case of external form
     * processing like Authorize.net Sim method & etc.
     * <p/>
     * If operation is not supported payment status must be {@link Payment#PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED}
     *
     * @param payment to capture.
     *
     * @return payment
     */
    Payment authorizeCapture(Payment payment);


    /**
     * Authorize a payment. The response from a card issuing bank to a
     * merchant's transaction authorization request indicating that payment information is valid
     * and funds are available on the customer's credit card.
     * <p/>
     * If operation is not supported payment status must be {@link Payment#PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED}
     *
     * @param payment to authorize.
     *
     * @return payment. Gateways can return reference id, token, etc.
     */
    Payment authorize(Payment payment);

    /**
     * Reverse the authorization. Not always supported by payment gateways. Used to cancel
     * AUTH transaction (see {@link #authorize(org.yes.cart.payment.dto.Payment)}).
     * <p/>
     * If operation is not supported payment status must be {@link Payment#PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED}
     *
     * @param payment payment
     *
     * @return payment.
     */
    Payment reverseAuthorization(Payment payment);

    /**
     * Payment capture on authorized card.
     * Capture of prior authorization of credit card transaction request to capture funds.
     * With this type of transaction, the merchant will submit an authorization code
     * that was received from the issuing bank at the time of the original authorization-only
     * transaction. (see {@link #authorize(org.yes.cart.payment.dto.Payment)}
     * <p/>
     * If operation is not supported payment status must be {@link Payment#PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED}
     *
     * @param payment to capture.
     *
     * @return payment
     */
    Payment capture(Payment payment);


    /**
     * Void transactions are used to cancel original charge transactions (CAPTURE or AUTH_CAPTURE)
     * that have not yet been submitted for batch settlement. For this type of transaction,
     * the merchant must submit the transaction ID of the original charge transaction against
     * which the Void is being submitted (see {@link #capture(org.yes.cart.payment.dto.Payment)}
     * and {@link #authorizeCapture(org.yes.cart.payment.dto.Payment)}).
     * <p/>
     * No further action may be taken for Void transactions.
     *
     * To cancel a transaction that has already settled, a <b>refund</b> must be submitted.
     * <p/>
     * If operation is not supported payment status must be {@link Payment#PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED}
     *
     * @param payment to void capture.
     * @return payment
     */
    Payment voidCapture(Payment payment);


    /**
     * A credit card transaction request to refunds from the merchant's
     * bank account back to the customer's credit card account as a
     * refund for a previous charge transaction. For this type of
     * transaction, the merchant must submit the transaction ID of the
     * original charge transaction against which the refund is being applied.
     * <p/>
     * AKA as Credit operation.
     * <p/>
     * If operation is not supported payment status must be {@link Payment#PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED}
     *
     * @param payment to refund
     * @return payment
     */
    Payment refund(Payment payment);


    /**
     * Create payment prototype from given map (of HTTP parameters).
     * In case of external form processing return value must be completely filled , because  #authorizeCapture
     * will simply return given argument
     *
     *
     * @param operation operation for which to create prototype for
     * @param map given map of parameters, from http request. Each Payment gateway know how to
     *            create template payment from HttpServletRequest#getParameterMap and configuration parameters.
     *
     * @return payment template.
     */
    Payment createPaymentPrototype(String operation, Map map);


    /**
     * Get unique payment gateway label. <b>Must</b> correspond to
     * descriptor entry in configuration and vise versa
     *
     * @return unique payment gateway label.
     */
    String getLabel();

    /**
     * Get payment gateway supported features.
     *
     * @return feature set of payment gateway.
     */
    PaymentGatewayFeature getPaymentGatewayFeatures();

    /**
     * Get parameters.
     *
     * @return PG parameters.
     */
    Collection<PaymentGatewayParameter> getPaymentGatewayParameters();

    /**
     * Delete configuration parameter.
     *
     * @param parameterLabel configuration parameter label.
     */
    void deleteParameter(String parameterLabel);

    /**
     * Add new configuration parameter.
     *
     * @param paymentGatewayParameter configuration parameter.
     */
    void addParameter(PaymentGatewayParameter paymentGatewayParameter);

    /**
     * Update configuration parameter.
     *
     * @param paymentGatewayParameter {@link PaymentGatewayParameter}
     */
    void updateParameter(PaymentGatewayParameter paymentGatewayParameter);

    /**
     * Get the parameter value from given collection.
     *
     * @param valueLabel key to search
     * @return value or null if not found
     */
    String getParameterValue(String valueLabel);

    enum CallbackResult {

        OK(Payment.PAYMENT_STATUS_OK, true),
        UNSETTLED(Payment.PAYMENT_STATUS_OK, false),
        PROCESSING(Payment.PAYMENT_STATUS_PROCESSING, false),
        FAILED(Payment.PAYMENT_STATUS_FAILED, false),
        MANUAL_REQUIRED(Payment.PAYMENT_STATUS_MANUAL_PROCESSING_REQUIRED, false);

        private String status;
        private boolean settled;

        private CallbackResult(final String status, final boolean settled) {
            this.status = status;
            this.settled = settled;
        }

        /**
         * @return payment result (see {@link Payment})
         */
        public String getStatus() {
            return status;
        }

        /**
         * @return AUTH_CAPTURE and CAPTURE operations only (denotes if funds were captured)
         */
        public boolean isSettled() {
            return settled;
        }
    }

}
