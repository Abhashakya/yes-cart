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

package org.yes.cart.web.page;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.https.RequireHttps;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.yes.cart.constants.ServiceSpringKeys;
import org.yes.cart.domain.entity.CustomerOrder;
import org.yes.cart.domain.entity.ProductSku;
import org.yes.cart.payment.dto.PaymentMiscParam;
import org.yes.cart.payment.persistence.entity.CustomerOrderPayment;
import org.yes.cart.service.order.OrderException;
import org.yes.cart.service.order.OrderItemAllocationException;
import org.yes.cart.service.payment.PaymentProcessFacade;
import org.yes.cart.shoppingcart.ShoppingCart;
import org.yes.cart.shoppingcart.ShoppingCartCommand;
import org.yes.cart.shoppingcart.ShoppingCartCommandFactory;
import org.yes.cart.web.application.ApplicationDirector;
import org.yes.cart.web.page.component.footer.StandardFooter;
import org.yes.cart.web.page.component.header.HeaderMetaInclude;
import org.yes.cart.web.page.component.header.StandardHeader;
import org.yes.cart.web.page.component.js.ServerSideJs;
import org.yes.cart.web.support.constants.StorefrontServiceSpringKeys;
import org.yes.cart.web.support.service.CheckoutServiceFacade;
import org.yes.cart.web.support.service.ProductServiceFacade;
import org.yes.cart.web.util.WicketUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Page  responsible to handle payments for gateways with internal/advanced
 * integration methods, in this case we are responsible to get the order from db perform
 * gateway call and show successful of failed result.
 *
 * Igor Azarny iazarny@yahoo.com
 * Date: 12/11/11
 * Time: 16:56
 */
@RequireHttps
public class PaymentPage extends AbstractWebPage {


    // ------------------------------------- MARKUP IDs BEGIN ---------------------------------- //
    private static final String RESULT_CONTAINER = "paymentResult";

    private static final String POSITIVE_RESULT_FRAGMENT = "positiveResultFragment";

    private static final String NEGATIVE_RESULT_FRAGMENT = "negativeResultFragment";
    private static final String NEGATIVE_PAYMENT_NOTES = "negativePaymentNotes";
    private static final String POSITIVE_PAYMENT_NOTES = "positivePaymentNotes";
    private static final String NEGATIVE_ALLOCATION_RESULT_FRAGMENT = "negativeItemAllocationResultFragment";
    private static final String PAYMENT_DETAIL_LIST = "paymentDetail";
    private static final String ALLOCATION_DETAIL = "negativeItemAllocationNotes";
    private static final String PAYMENT_ID_LABEL = "id";
    private static final String PAYMENT_TRANS_ID = "transactionId";
    private static final String PAYMENT_AUTH_CODE = "authCode";
    private static final String PAYMENT_ERROR_CODE = "errorCode";
    private static final String PAYMENT_ERROR_DESCR = "errorDescription";

    // ------------------------------------- MARKUP IDs END ---------------------------------- //


    @SpringBean(name = ServiceSpringKeys.PAYMENT_PROCESS_FACADE)
    private PaymentProcessFacade paymentProcessFacade;

    @SpringBean(name = StorefrontServiceSpringKeys.CHECKOUT_SERVICE_FACADE)
    private CheckoutServiceFacade checkoutServiceFacade;

    @SpringBean(name = StorefrontServiceSpringKeys.PRODUCT_SERVICE_FACADE)
    private ProductServiceFacade productServiceFacade;

    @SpringBean(name = ServiceSpringKeys.CART_COMMAND_FACTORY)
    private ShoppingCartCommandFactory shoppingCartCommandFactory;


    private  boolean result;



    /**
     * Construct payment page.
     * @param params page parameters
     */
    public PaymentPage(final PageParameters params) {

        super(params);

        addOrReplace(
                new FeedbackPanel(FEEDBACK));

        try {

            final Map param =  getWicketUtil().getHttpServletRequest().getParameterMap();
            final Map mparam = new HashMap();
            mparam.putAll(param);
            mparam.put(PaymentMiscParam.CLIENT_IP, ApplicationDirector.getShopperIPAddress());

            final ShoppingCart cart = ApplicationDirector.getShoppingCart();

            if (cart.getCartItemsCount() > 0) {
                result = paymentProcessFacade.pay(cart, mparam);
                if (result) {
                    addOrReplace(createPositiveResultFragment());

                } else {
                    addOrReplace(createNegativePaymentResultFragment());
                }
            } else {
               addOrReplace(createNeutralResultFragment());
            }


        } catch (OrderItemAllocationException e) {

            addOrReplace(createNegativeItemAllocationResultFragment(e.getProductSkuCode()));
            result = false;

        } catch (OrderException e) {

            addOrReplace(createNegativePaymentResultFragment());
            result = false;
        }

        if (result) {
            shoppingCartCommandFactory.execute(
                    ShoppingCartCommand.CMD_CLEAN, ApplicationDirector.getShoppingCart(),
                                        Collections.singletonMap(
                                                ShoppingCartCommand.CMD_CLEAN,
                                                null)
                                );
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        executeHttpPostedCommands();


        addOrReplace(
                new StandardFooter(FOOTER)
        ).addOrReplace(
                new StandardHeader(HEADER)
        ).addOrReplace(
                new ServerSideJs("serverSideJs")
        ).addOrReplace(
                new HeaderMetaInclude("headerInclude")
        );

        super.onBeforeRender();

        persistCartIfNecessary();
    }




    /**
     * Get the negative result fragment, which responsible
     * to show item allocation error.
     *
     * @param sku the {@link ProductSku} which quantity can not be allocated.
     *
     * @return negative result fragment
     */
    private MarkupContainer createNegativeItemAllocationResultFragment(final String sku) {

        final ProductSku productSku = productServiceFacade.getProductSkuBySkuCode(sku);

        final Map<String, Object> param = new HashMap<String, Object>();
        param.put("product", getI18NSupport().getFailoverModel(productSku.getDisplayName(), productSku.getName()).getValue(getLocale().getLanguage()));
        param.put("sku", sku);
        final String errorMessage =
                WicketUtil.createStringResourceModel(this, ALLOCATION_DETAIL, param).getString();

        error(errorMessage);

        return new Fragment(RESULT_CONTAINER, NEGATIVE_ALLOCATION_RESULT_FRAGMENT, this)
                .add(
                        new Label(
                                ALLOCATION_DETAIL,
                                errorMessage )
                ) ;

    }

    /**
     * Get the negative result fragment.
     * @return negative result fragment
     */
    private MarkupContainer createNegativePaymentResultFragment() {

        error(getLocalizer().getString(NEGATIVE_PAYMENT_NOTES, this));

        final List<CustomerOrderPayment> payments = checkoutServiceFacade.findPaymentRecordsByOrderNumber(getOrderNumber());


        return new Fragment(RESULT_CONTAINER, NEGATIVE_RESULT_FRAGMENT, this)
                .add(
                        new ListView<CustomerOrderPayment>(PAYMENT_DETAIL_LIST, payments) {
                            protected void populateItem(final ListItem<CustomerOrderPayment> item) {
                                final CustomerOrderPayment payment = item.getModelObject();
                                item.add(new Label(PAYMENT_ID_LABEL, String.valueOf(payment.getCustomerOrderPaymentId())));
                                item.add(new Label(PAYMENT_TRANS_ID, StringUtils.defaultIfEmpty(payment.getTransactionReferenceId(), StringUtils.EMPTY)));
                                item.add(new Label(PAYMENT_AUTH_CODE, StringUtils.defaultIfEmpty(payment.getTransactionAuthorizationCode(), StringUtils.EMPTY)));
                                item.add(new Label(PAYMENT_ERROR_CODE, StringUtils.defaultIfEmpty(payment.getTransactionOperationResultCode(), StringUtils.EMPTY)));
                                item.add(new Label(PAYMENT_ERROR_DESCR, StringUtils.defaultIfEmpty(payment.getTransactionOperationResultMessage(), StringUtils.EMPTY)));
                            }
                        }

                );
    }


    /**
     * Get positive result fragment.
     * @return positive result fragment.
     */
    private MarkupContainer createPositiveResultFragment() {
        info(getLocalizer().getString(POSITIVE_PAYMENT_NOTES, this));
        return new Fragment(RESULT_CONTAINER, POSITIVE_RESULT_FRAGMENT, this);
    }


    /**
     * Neutral result (e.g. if someone changes locale on this page - then there is nothing to check).
     * @return neutral result fragment.
     */
    private MarkupContainer createNeutralResultFragment() {
        return new Fragment(RESULT_CONTAINER, POSITIVE_RESULT_FRAGMENT, this);
    }


    /**
     * Get order number. Can be obtained from request in case if
     *  direct post method gateway was used or from shopping cart.
     *
     * @return order number.
     */
    private String getOrderNumber() {
        final ShoppingCart cart = ApplicationDirector.getShoppingCart();
        final CustomerOrder customerOrder = checkoutServiceFacade.findByGuid(cart.getGuid());
        return customerOrder.getOrdernum();
    }

    @Override
    public IModel<String> getPageTitle() {
        return new Model<String>(getLocalizer().getString("orderPayment", this));
    }
}
