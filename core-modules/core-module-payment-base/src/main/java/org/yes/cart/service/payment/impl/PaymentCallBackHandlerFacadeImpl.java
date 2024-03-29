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

package org.yes.cart.service.payment.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.yes.cart.domain.entity.CustomerOrder;
import org.yes.cart.payment.PaymentGatewayExternalForm;
import org.yes.cart.service.domain.CustomerOrderService;
import org.yes.cart.service.order.OrderEvent;
import org.yes.cart.service.order.OrderException;
import org.yes.cart.service.order.OrderItemAllocationException;
import org.yes.cart.service.order.OrderStateManager;
import org.yes.cart.service.order.impl.OrderEventImpl;
import org.yes.cart.service.payment.PaymentCallBackHandlerFacade;
import org.yes.cart.service.payment.PaymentModulesManager;
import org.yes.cart.util.ShopCodeContext;

import java.util.Map;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class PaymentCallBackHandlerFacadeImpl implements PaymentCallBackHandlerFacade, ApplicationContextAware {

    private final PaymentModulesManager paymentModulesManager;
    private final CustomerOrderService customerOrderService;

    private ApplicationContext applicationContext;

    private OrderStateManager orderStateManager;

    /**
     * Constract service.
     *
     * @param paymentModulesManager Payment modules manager to get the order number from request parameters.
     * @param customerOrderService  to get order
     */
    public PaymentCallBackHandlerFacadeImpl(final PaymentModulesManager paymentModulesManager,
                                            final CustomerOrderService customerOrderService) {
        this.paymentModulesManager = paymentModulesManager;
        this.customerOrderService = customerOrderService;
    }

    /**
     * {@inheritDoc}
     */
    public void handlePaymentCallback(final Map parameters, final String paymentGatewayLabel) throws OrderException {

        final String orderGuid = getOrderGuid(parameters, paymentGatewayLabel);

        final Logger log = ShopCodeContext.getLog(this);
        log.info("Order guid to handle at call back handler is {}", orderGuid);

        if (StringUtils.isNotBlank(orderGuid)) {

            final CustomerOrder order = customerOrderService.findByGuid(orderGuid);

            if (order == null) {

                if (log.isWarnEnabled()) {
                    log.warn("Can not get order with guid {}", orderGuid);
                }
                return;
            }

            if (log.isInfoEnabled()) {
                log.warn("Processing callback for order with guid {}", orderGuid);
            }

            if (CustomerOrder.ORDER_STATUS_NONE.endsWith(order.getOrderStatus())) {

                // New order flow (this should AUTH or AUTH_CAPTURE)

                try {
                    handleNewOrderToPending(parameters, orderGuid, log);
                } catch (OrderItemAllocationException oiae) {
                    handleNewOrderToCancelWithRefund(parameters, orderGuid, log);
                }

            } else if (CustomerOrder.ORDER_STATUS_WAITING_PAYMENT.endsWith(order.getOrderStatus())) {

                handleWaitingPaymentToPending(parameters, orderGuid, log);

            } else if (CustomerOrder.ORDER_STATUS_CANCELLED_WAITING_PAYMENT.endsWith(order.getOrderStatus()) ||
                    CustomerOrder.ORDER_STATUS_RETURNED_WAITING_PAYMENT.endsWith(order.getOrderStatus())) {

                handleWaitingRefundToPending(parameters, orderGuid, log);

            } else {

                if (log.isWarnEnabled()) {
                    log.warn("Can not handle state {} for order with guid {}", order.getOrderStatus(), orderGuid);
                }

            }



        }
    }

    private void handleNewOrderToCancelWithRefund(final Map parameters, final String orderGuid, final Logger log) throws OrderException {

        // Need to get fresh order instance from db so that we have a clean object
        final CustomerOrder order = customerOrderService.findByGuid(orderGuid);

        if (order == null) {

            if (log.isWarnEnabled()) {
                log.warn("Can not get order with guid {}", orderGuid);
            }

        } else {

            if (CustomerOrder.ORDER_STATUS_NONE.endsWith(order.getOrderStatus())) {

                // Pending event handler will check for payment and will cancel reservation if required
                OrderEvent orderEvent = new OrderEventImpl(
                        OrderStateManager.EVT_CANCEL_NEW_WITH_REFUND,
                        order,
                        null,
                        parameters
                );

                // For cancellation of new orders we only need to refund potentially successful payments, no reservations were made yet
                boolean rez = getOrderStateManager().fireTransition(orderEvent);

                log.info("Order state transition performed for {} . Result is {}", orderGuid, rez);

                customerOrderService.update(order);

            } else {
                log.warn("Order with guid {} not in NONE state, but {}", orderGuid, order.getOrderStatus());
            }

        }
    }

    private void handleNewOrderToPending(final Map parameters, final String orderGuid, final Logger log) throws OrderException {

        final CustomerOrder order = customerOrderService.findByGuid(orderGuid);

        if (order == null) {

            if (log.isWarnEnabled()) {
                log.warn("Can not get order with guid {}", orderGuid);
            }

        } else {

            if (CustomerOrder.ORDER_STATUS_NONE.endsWith(order.getOrderStatus())) {

                // Pending event handler will check for payment and will cancel reservation if required
                OrderEvent orderEvent = new OrderEventImpl(
                        OrderStateManager.EVT_PENDING,
                        order,
                        null,
                        parameters
                );

                // Pending may throw exception during reservation, which happens prior payment saving - need another status? and extra flow?
                boolean rez = getOrderStateManager().fireTransition(orderEvent);

                log.info("Order state transition performed for {} . Result is {}", orderGuid, rez);

                customerOrderService.update(order);

            } else {

                log.warn("Order with guid {} not in NONE state, but {}", orderGuid, order.getOrderStatus());

            }

        }
    }


    private void handleWaitingPaymentToPending(final Map parameters, final String orderGuid, final Logger log) throws OrderException {

        final CustomerOrder order = customerOrderService.findByGuid(orderGuid);

        if (order == null) {

            if (log.isWarnEnabled()) {
                log.warn("Can not get order with guid {}", orderGuid);
            }

        } else {

            if (CustomerOrder.ORDER_STATUS_WAITING_PAYMENT.endsWith(order.getOrderStatus())) {

                // Another call possibly to confirm payment that was processing
                OrderEvent orderEvent = new OrderEventImpl(
                        OrderStateManager.EVT_PAYMENT_PROCESSED,
                        order,
                        null,
                        parameters
                );

                boolean rez = getOrderStateManager().fireTransition(orderEvent);

                log.info("Order state transition performed for {} . Result is {}", orderGuid, rez);

                customerOrderService.update(order);


            } else {

                log.warn("Order with guid {} not in WAITING_PAYMENT state, but {}", orderGuid, order.getOrderStatus());

            }

        }
    }


    private void handleWaitingRefundToPending(final Map parameters, final String orderGuid, final Logger log) throws OrderException {

        final CustomerOrder order = customerOrderService.findByGuid(orderGuid);

        if (order == null) {

            if (log.isWarnEnabled()) {
                log.warn("Can not get order with guid {}", orderGuid);
            }

        } else {

            if (CustomerOrder.ORDER_STATUS_CANCELLED_WAITING_PAYMENT.endsWith(order.getOrderStatus()) ||
                    CustomerOrder.ORDER_STATUS_RETURNED_WAITING_PAYMENT.endsWith(order.getOrderStatus())) {

                // Another call possibly to confirm payment that was processing
                OrderEvent orderEvent = new OrderEventImpl(
                        OrderStateManager.EVT_REFUND_PROCESSED,
                        order,
                        null,
                        parameters
                );

                boolean rez = getOrderStateManager().fireTransition(orderEvent);

                log.info("Order state transition performed for {} . Result is {}", orderGuid, rez);

                customerOrderService.update(order);


            } else {

                log.warn("Order with guid {} not in CANCELLED_WAITING_PAYMENT or RETURNED_WAITING_PAYMENT state, but {}", orderGuid, order.getOrderStatus());

            }

        }
    }



    private String getOrderGuid(final Map privateCallBackParameters, final String paymentGatewayLabel) {
        final PaymentGatewayExternalForm paymentGateway = getPaymentGateway(paymentGatewayLabel);
        final String orderGuid = paymentGateway.restoreOrderGuid(privateCallBackParameters);
        final Logger log = ShopCodeContext.getLog(this);
        if (log.isDebugEnabled()) {
            log.debug("Get order guid {}  from http request with {} payment gateway.",
                    orderGuid, paymentGatewayLabel);
        }
        return orderGuid;
    }

    private PaymentGatewayExternalForm getPaymentGateway(String paymentGatewayLabel) {
        if ("DEFAULT".equals(ShopCodeContext.getShopCode())) {
            throw new RuntimeException("Payment gateway URL must be configured for shop specific URL's");
        }
        return (PaymentGatewayExternalForm) paymentModulesManager.getPaymentGateway(paymentGatewayLabel, ShopCodeContext.getShopCode());
    }



    private OrderStateManager getOrderStateManager() {
        if (orderStateManager == null) {
            orderStateManager = applicationContext.getBean("orderStateManager", OrderStateManager.class);
        }
        return orderStateManager;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
