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

package org.yes.cart.web.page.component.cart;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.yes.cart.constants.Constants;
import org.yes.cart.domain.entity.CustomerWishList;
import org.yes.cart.domain.entity.ProductAvailabilityModel;
import org.yes.cart.domain.entity.ProductQuantityModel;
import org.yes.cart.domain.entity.ProductSku;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.shoppingcart.CartItem;
import org.yes.cart.shoppingcart.ShoppingCartCommand;
import org.yes.cart.util.ShopCodeContext;
import org.yes.cart.web.page.AbstractWebPage;
import org.yes.cart.web.page.component.BaseComponent;
import org.yes.cart.web.page.component.price.PriceView;
import org.yes.cart.web.service.wicketsupport.WicketSupportFacade;
import org.yes.cart.web.support.constants.StorefrontServiceSpringKeys;
import org.yes.cart.web.support.constants.WebParametersKeys;
import org.yes.cart.web.support.constants.WicketServiceSpringKeys;
import org.yes.cart.web.support.entity.decorator.DecoratorFacade;
import org.yes.cart.web.support.entity.decorator.ProductSkuDecorator;
import org.yes.cart.web.support.service.CategoryServiceFacade;
import org.yes.cart.web.support.service.ProductServiceFacade;
import org.yes.cart.web.util.WicketUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 10/9/11
 * Time: 11:30 AM
 */
public class ShoppingCartItemsList extends ListView<CartItem> {


    // ------------------------------------- MARKUP IDs BEGIN ---------------------------------- //


    private static final String PRICE_PANEL = "pricePanel";
    private static final String SKU_NUM_LABEL = "skunum";
    private static final String ADD_ONE_LINK = "addOneLink";
    private static final String REMOVE_ONE_LINK = "removeOneLink";
    private static final String REMOVE_ALL_LINK = "removeAllLink";
    private static final String PRODUCT_LINK = "productLink";
    private static final String PRODUCT_NAME_LABEL = "name";
    private static final String DEFAULT_IMAGE = "defaultImage";
    private static final String QUANTITY_TEXT = "quantity";
    private static final String QUANTITY_TEXT_RO = "quantityRo";


    private static final String LINE_TOTAL_VIEW = "lineAmountView";
    private final static String PRICE_VIEW = "skuPriceView";

    private static final String QUANTITY_ADJUST_BUTTON = "quantityButton";

    private final static String ADD_TO_WISHLIST_LINK = "addToWishListLink";
    private final static String ADD_TO_WISHLIST_LINK_LABEL = "addToWishListLinkLabel";

    private final static String SAVE_FOR_LATER_LINK = "saveForLaterLink";
    private final static String SAVE_FOR_LATER_LINK_LABEL = "saveForLaterLinkLabel";

    // ------------------------------------- MARKUP IDs END ---------------------------------- //

    @SpringBean(name = StorefrontServiceSpringKeys.DECORATOR_FACADE)
    private DecoratorFacade decoratorFacade;

    @SpringBean(name = StorefrontServiceSpringKeys.CATEGORY_SERVICE_FACADE)
    private CategoryServiceFacade categoryServiceFacade;

    @SpringBean(name = StorefrontServiceSpringKeys.PRODUCT_SERVICE_FACADE)
    private ProductServiceFacade productServiceFacade;

    @SpringBean(name = WicketServiceSpringKeys.WICKET_UTIL)
    private WicketUtil wicketUtil;

    @SpringBean(name = WicketServiceSpringKeys.WICKET_SUPPORT_FACADE)
    private WicketSupportFacade wicketSupportFacade;


    /**
     * Construct list of product in shopping cart.
     *
     * @param id        component id
     * @param cartItems cart items
     */
    public ShoppingCartItemsList(final String id, final List<? extends CartItem> cartItems) {
        super(id, cartItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateItem(final ListItem<CartItem> cartItemListItem) {

        final CartItem cartItem = cartItemListItem.getModelObject();

        final String skuCode = cartItem.getProductSkuCode();

        final PageParameters params = new PageParameters();
        params.add(WebParametersKeys.PAGE_TYPE, "cart");

        final ProductSku sku = productServiceFacade.getProductSkuBySkuCode(skuCode);
        final ProductAvailabilityModel skuPam = productServiceFacade.getProductAvailability(sku, ShopCodeContext.getShopId());
        final ProductQuantityModel pqm = productServiceFacade.getProductQuantity(cartItem.getQty(), sku);

        final ProductSkuDecorator productSkuDecorator = decoratorFacade.decorate(sku, wicketUtil.getHttpServletRequest().getContextPath(), true);

        final boolean notGift = !cartItem.isGift();
        final boolean available = skuPam.isAvailable();

        cartItemListItem.add(
                createAddOneSkuLink(skuCode).setVisible(available && notGift && pqm.canOrderMore())
        ).add(
                createRemoveAllSkuLink(skuCode).setVisible(notGift)
        ).add(
                createRemoveOneSkuLink(skuCode).setVisible(available && notGift && pqm.canOrderLess())
        ).add(
                new Label(SKU_NUM_LABEL, skuCode)
        ).add(
                getProductLink(productSkuDecorator)
        ).add(
                new PriceView(PRICE_VIEW, new Pair<BigDecimal, BigDecimal>(
                        cartItem.getListPrice(), cartItem.getPrice()), null, cartItem.getAppliedPromo(), false, true)
                            .setVisible(available)
        ).add(
                new PriceView(LINE_TOTAL_VIEW, new Pair<BigDecimal, BigDecimal>(
                        cartItem.getPrice().multiply(cartItem.getQty()), null), null, null, false, false)
                            .setVisible(available)
        ).add(
                wicketSupportFacade.links().newAddToWishListLink(ADD_TO_WISHLIST_LINK, sku.getCode(), null, null, null, params)
                        .add(new Label(ADD_TO_WISHLIST_LINK_LABEL, getLocalizer().getString("addToWishlist", this)))
        ).add(
                wicketSupportFacade.links().newAddToWishListLink(SAVE_FOR_LATER_LINK, sku.getCode(), cartItem.getQty().toPlainString(), CustomerWishList.CART_SAVE_FOR_LATER, null, params)
                        .add(new Label(SAVE_FOR_LATER_LINK_LABEL, getLocalizer().getString("saveForLater", this)))
        );


        final String message;
        if (!pqm.canOrderMore()) {

            final Map<String, Object> mparams = new HashMap<String, Object>();
            mparams.put("cart", pqm.getCartQty().toPlainString());

            message = getLocalizer().getString("quantityPickerFullTooltip", this,
                    new Model<Serializable>(new ValueMap(mparams)));

        } else if (pqm.hasMax()) {

            final Map<String, Object> mparams = new HashMap<String, Object>();
            mparams.put("min", pqm.getMin().toPlainString());
            mparams.put("step", pqm.getStep().toPlainString());
            mparams.put("max", pqm.getMax().toPlainString());
            mparams.put("cart", pqm.getCartQty().toPlainString());

            message = getLocalizer().getString("quantityPickerTooltip", this,
                    new Model<Serializable>(new ValueMap(mparams)));

        } else {

            final Map<String, Object> mparams = new HashMap<String, Object>();
            mparams.put("min", pqm.getMin().toPlainString());
            mparams.put("step", pqm.getStep().toPlainString());
            mparams.put("cart", pqm.getCartQty().toPlainString());

            message = getLocalizer().getString("quantityPickerTooltipNoMax", this,
                    new Model<Serializable>(new ValueMap(mparams)));
        }


        final TextField<BigDecimal> quantity = new TextField<BigDecimal>(QUANTITY_TEXT,
                new Model<BigDecimal>(pqm.getCartQty()));
        quantity.add(new AttributeModifier("title", message));

        cartItemListItem.add(
                quantity.setVisible(available && notGift)
        )
        .add(new Label(QUANTITY_TEXT_RO, pqm.getCartQty().toPlainString()).setVisible(!notGift))
        .add(
                createAddSeveralSkuButton(skuCode, quantity).setVisible(available && notGift)
        );


        final Pair<String, String> size = categoryServiceFacade.getThumbnailSizeConfig(0L, ShopCodeContext.getShopId());

        final String width = size.getFirst();
        final String height = size.getSecond();

        final String lang = getLocale().getLanguage();
        final String defaultImageRelativePath = productSkuDecorator.getDefaultImage(width, height, lang);

        cartItemListItem.add(new ContextImage(DEFAULT_IMAGE, defaultImageRelativePath)
                .add(
                        new AttributeModifier(BaseComponent.HTML_WIDTH, width),
                        new AttributeModifier(BaseComponent.HTML_HEIGHT, height)
                )
        );


    }


    /**
     * Adjust quantity.
     *
     * @param productSkuCode sku code
     * @param qtyField       quantity input box
     * @return BookmarkablePageLink for remove one sku from cart command
     */
    private Button createAddSeveralSkuButton(final String productSkuCode, final TextField<BigDecimal> qtyField) {
        final Button adjustQuantityButton = new Button(QUANTITY_ADJUST_BUTTON) {
            @Override
            public void onSubmit() {
                String qty = qtyField.getInput();
                if (StringUtils.isNotBlank(qty)) {
                    qty = qty.replace(',', '.');
                }
                final String skuCode  = productSkuCode;
                /*try {
                    skuCode = URLEncoder.encode(productSkuCode, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    skuCode = productSkuCode;
                }*/


                //TODOv2 add flag for product is quantity with float point enabled for this product or not
                //ATM this is CPOINT
                if (NumberUtils.isNumber(qty) /*&& NumberUtils.toInt(qty) >= 1*/) {
                    qtyField.setConvertedInput(new BigDecimal(qty).setScale(Constants.DEFAULT_SCALE, BigDecimal.ROUND_HALF_UP));
                    setResponsePage(
                            getPage().getPageClass(),
                            new PageParameters()
                                    .add(ShoppingCartCommand.CMD_SETQTYSKU, skuCode)
                                    .add(ShoppingCartCommand.CMD_SETQTYSKU_P_QTY, qty)
                    );


                } else {
                    qtyField.setConvertedInput(BigDecimal.ONE.setScale(Constants.DEFAULT_SCALE));
                    error(getLocalizer().getString("nonZeroDigits", this, "Need positive integer value"));
                }
            }
        };
        adjustQuantityButton.setDefaultFormProcessing(true);
        return adjustQuantityButton;
    }

    /**
     * Get link to show product with selected in cart product sku.
     *
     * @param productSku product    sku
     * @return link to show product and selected SKU
     */
    private Link getProductLink(final ProductSkuDecorator productSku) {
        final Link productLink = ((AbstractWebPage) getPage()).getWicketSupportFacade().links()
                .newProductSkuLink(PRODUCT_LINK, productSku.getId());
        productLink.add(new Label(PRODUCT_NAME_LABEL, productSku.getName(getLocale().getLanguage())).setEscapeModelStrings(false));
        return productLink;
    }


    /**
     * Create BookmarkablePageLink for add one sku to cart command.
     *
     * @param skuCode sku code
     * @return BookmarkablePageLink for add one sku to cart command
     */
    private BookmarkablePageLink createAddOneSkuLink(final String skuCode) {
        final PageParameters paramsMap = new PageParameters();
        paramsMap.set(ShoppingCartCommand.CMD_ADDTOCART, skuCode);
        return new BookmarkablePageLink<Page>(
                ADD_ONE_LINK,
                getPage().getPageClass(),
                paramsMap
        );
    }

    /**
     * Create BookmarkablePageLink for remove one sku from cart command.
     *
     * @param skuCode sku code
     * @return BookmarkablePageLink for remove one sku from cart command
     */
    private BookmarkablePageLink createRemoveOneSkuLink(final String skuCode) {
        final PageParameters paramsMap = new PageParameters();
        paramsMap.set(ShoppingCartCommand.CMD_REMOVEONESKU, skuCode);
        return new BookmarkablePageLink<Page>(
                REMOVE_ONE_LINK,
                getPage().getPageClass(),
                paramsMap
        );
    }


    /**
     * Create BookmarkablePageLink for remove one sku from cart command.
     *
     * @param skuCode sku code
     * @return BookmarkablePageLink for remove one sku from cart command
     */
    private BookmarkablePageLink createRemoveAllSkuLink(final String skuCode) {
        final PageParameters paramsMap = new PageParameters();
        paramsMap.set(ShoppingCartCommand.CMD_REMOVEALLSKU, skuCode);
        return new BookmarkablePageLink<Page>(
                REMOVE_ALL_LINK,
                getPage().getPageClass(),
                paramsMap
        );
    }


}
