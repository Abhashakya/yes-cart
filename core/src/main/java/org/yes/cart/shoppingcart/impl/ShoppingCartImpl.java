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

package org.yes.cart.shoppingcart.impl;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.yes.cart.shoppingcart.*;
import org.yes.cart.util.MoneyUtils;
import org.yes.cart.util.ShopCodeContext;

import java.math.BigDecimal;
import java.util.*;

/**
 * Shopping cart default implementation.
 *
 * Shopping cart is a high performance class since it represent current application state
 * with respect to the user.
 *
 * YC uses CartTuplizers that allow to Serialize it into byte stream and then package to
 * allow preserving state (e.g. in response cookies, headers or persistent storage).
 * In order to make this process maximum efficient we use Externalization for all object
 * hierarchy of shopping cart implementation.
 *
 * User: dogma
 * Date: Jan 15, 2011
 * Time: 10:39:12 PM
 */
public class ShoppingCartImpl implements MutableShoppingCart {

    private static final long serialVersionUID =  20110509L;

    @JsonProperty
    private List<CartItemImpl> items = new ArrayList<CartItemImpl>();
    @JsonProperty
    private List<CartItemImpl> gifts = new ArrayList<CartItemImpl>();
    @JsonProperty
    private Set<String> coupons = new TreeSet<String>();
    @JsonProperty
    private List<CartItemImpl> shipping = new ArrayList<CartItemImpl>();

    private String guid = java.util.UUID.randomUUID().toString();

    private String currentLocale;

    private String currencyCode;

    private long modifiedTimestamp;

    private long processingStartTimestamp;

    private MutableShoppingContext shoppingContext;

    private MutableOrderInfo orderInfo;

    private Total total = new TotalImpl();

    private transient AmountCalculationStrategy calculationStrategy;


    private AmountCalculationStrategy getCalculationStrategy() {
        if (calculationStrategy == null) {
            ShopCodeContext.getLog(this).error("Cart amount calculation strategy is not configured. Please configure \"calculationStrategy\" and set it to cart");
            throw new RuntimeException("Cart amount calculation strategy is not configured. Please configure \"calculationStrategy\" and set it to cart");
        }
        return calculationStrategy;
    }

    /** {@inheritDoc} */
    public void initialise(final AmountCalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
        this.processingStartTimestamp = System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    public void markDirty() {
        this.modifiedTimestamp = System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    public void clean() {
        guid = java.util.UUID.randomUUID().toString();
        items.clear();
        gifts.clear();
        coupons.clear();
        orderInfo = null;
        total = new TotalImpl();
        modifiedTimestamp = System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    public void recalculate() {
        total = getCalculationStrategy().calculate(this);
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public boolean isSeparateBillingAddress() {
        return getOrderInfo().isSeparateBillingAddress();
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public boolean isBillingAddressNotRequired() {
        return getOrderInfo().isBillingAddressNotRequired();
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public boolean isDeliveryAddressNotRequired() {
        return getOrderInfo().isDeliveryAddressNotRequired();
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public String getOrderMessage() {
        return getOrderInfo().getOrderMessage();
    }

    /** {@inheritDoc} */
    public String getGuid() {
        return guid;
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public Long getCarrierSlaId() {
        return getOrderInfo().getCarrierSlaId();
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public List<CartItem> getCartItemList() {
        final List<CartItem> immutableItems = new ArrayList<CartItem>(getItems().size());
        // first items (in the order they were added)
        for (CartItem item : getItems()) {
            immutableItems.add(new ImmutableCartItemImpl(item));
        }
        // gifts in the order promotions were applied
        for (CartItem item : getGifts()) {
            immutableItems.add(new ImmutableCartItemImpl(item));
        }
        return Collections.unmodifiableList(immutableItems);
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public List<CartItem> getShippingList() {
        final List<CartItem> immutableItems = new ArrayList<CartItem>(getShipping().size());
        // all shipping lines
        for (CartItem item : getShipping()) {
            immutableItems.add(new ImmutableCartItemImpl(item));
        }
        return Collections.unmodifiableList(immutableItems);
    }

    /** {@inheritDoc} */
    public BigDecimal getProductSkuQuantity(final String sku) {
        final int skuIndex = indexOfProductSku(sku);
        if (skuIndex == -1) { //not found
            return BigDecimal.ZERO;
        }
        return getItems().get(skuIndex).getQty();
    }

    /** {@inheritDoc} */
    public boolean addProductSkuToCart(final String sku, final BigDecimal quantity) {

        final int skuIndex = indexOfProductSku(sku);
        if (skuIndex != -1) {
            getItems().get(skuIndex).addQuantity(quantity);
            return false;
        }

        final CartItemImpl newItem = new CartItemImpl();
        newItem.setProductSkuCode(sku);
        newItem.setQuantity(quantity);
        getItems().add(newItem);
        return true;
    }

    /** {@inheritDoc} */
    public boolean addShippingToCart(final String carrierSlaId, final BigDecimal quantity) {
        final int shipIndex = indexOfShipping(carrierSlaId);
        if (shipIndex != -1) {
            getShipping().get(shipIndex).addQuantity(quantity);
            return false;
        }

        final CartItemImpl newItem = new CartItemImpl();
        newItem.setProductSkuCode(carrierSlaId);
        newItem.setQuantity(quantity);
        getShipping().add(newItem);
        return true;
    }

    /** {@inheritDoc} */
    public boolean addGiftToCart(final String sku, final BigDecimal quantity, final String promotionCode) {

        final int skuIndex = indexOfGift(sku);
        if (skuIndex != -1) {
            final CartItemImpl item = getGifts().get(skuIndex);
            item.addQuantity(quantity);
            addPromoCode(item, promotionCode);
            return false;
        }

        final CartItemImpl newItem = new CartItemImpl();
        newItem.setProductSkuCode(sku);
        newItem.setQuantity(quantity);
        newItem.setGift(true);
        newItem.setPromoApplied(true);
        newItem.setAppliedPromo(promotionCode);
        getGifts().add(newItem);
        return true;
    }

    private void addPromoCode(final CartItemImpl item, final String promotionCode) {
        final String promo = item.getAppliedPromo();
        if (promo == null) {
            item.setAppliedPromo(promotionCode);
            item.setPromoApplied(true);
        } else {
            final String[] promoCodes = StringUtils.split(promo, ',');
            // could do a binary search but in most cases this would be a single promo
            // and we have overhead of sorting before binary search
            boolean duplicate = false;
            for (final String promoCode : promoCodes) {
                if (promoCode.equals(promotionCode)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                item.setAppliedPromo(item.getAppliedPromo() + "," + promotionCode);
                item.setPromoApplied(true);
            }
        }
    }

    /** {@inheritDoc} */
    public boolean setProductSkuToCart(final String sku, final BigDecimal quantity) {

        final CartItemImpl newItem = new CartItemImpl();
        newItem.setProductSkuCode(sku);
        newItem.setQuantity(quantity);

        final int skuIndex = indexOfProductSku(sku);
        if (skuIndex == -1) { //not found
            getItems().add(newItem);
        } else {
            getItems().set(skuIndex, newItem);
        }
        return true;
    }

    /** {@inheritDoc} */
    public boolean removeCartItem(final String productSku) {
        final int skuIndex = indexOfProductSku(productSku);
        if (skuIndex != -1) {
            getItems().remove(skuIndex);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean removeCartItemQuantity(final String productSku, final BigDecimal quantity) {
        final int skuIndex = indexOfProductSku(productSku);
        if (skuIndex != -1) {
            try {
                getItems().get(skuIndex).removeQuantity(quantity);
            } catch (final CartItemRequiresDeletion cartItemRequiresDeletion) {
                getItems().remove(skuIndex);
            }
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean removeItemPromotions() {
        boolean removed = getGifts().isEmpty();
        getGifts().clear();

        for (final CartItemImpl item : getItems()) {
            if (item.isPromoApplied()) {
                // reinstate sale price
                removed = true;
                item.setPrice(item.getSalePrice());
                item.setAppliedPromo(null);
                item.setPromoApplied(false);
            }
        }
        return removed;
    }

    /** {@inheritDoc} */
    public boolean removeShipping() {
        if (getShipping().isEmpty()) {
            return false;
        }
        getShipping().clear();
        return true;
    }

    /** {@inheritDoc} */
    public boolean removeShipping(final String carrierSlaId) {
        final int skuIndex = indexOfShipping(carrierSlaId);
        if (skuIndex != -1) {
            getItems().remove(skuIndex);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setProductSkuPrice(final String skuCode, final BigDecimal salePrice, final BigDecimal listPrice) {
        final int skuIndex = indexOfProductSku(skuCode);
        if (skuIndex != -1) {
            final CartItemImpl cartItem = getItems().get(skuIndex);
            cartItem.setPrice(salePrice);
            cartItem.setSalePrice(salePrice);
            cartItem.setListPrice(listPrice);
            // clear promotion as we effectively changed the base price for promo calculations
            cartItem.setAppliedPromo(null);
            cartItem.setPromoApplied(false);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setShippingPrice(final String carrierSlaId, final BigDecimal salePrice, final BigDecimal listPrice) {
        final int shipIndex = indexOfShipping(carrierSlaId);
        if (shipIndex != -1) {
            final CartItemImpl shipItem = getShipping().get(shipIndex);
            shipItem.setPrice(salePrice);
            shipItem.setSalePrice(salePrice);
            shipItem.setListPrice(listPrice);
            // clear promotion as we effectively changed the base price for promo calculations
            shipItem.setAppliedPromo(null);
            shipItem.setPromoApplied(false);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setGiftPrice(final String skuCode, final BigDecimal salePrice, final BigDecimal listPrice) {
        final int skuIndex = indexOfGift(skuCode);
        if (skuIndex != -1) {
            final CartItemImpl cartItem = getGifts().get(skuIndex);
            cartItem.setPrice(MoneyUtils.ZERO);
            cartItem.setNetPrice(MoneyUtils.ZERO);
            cartItem.setGrossPrice(MoneyUtils.ZERO);
            cartItem.setTaxRate(MoneyUtils.ZERO);
            cartItem.setTaxCode("");
            cartItem.setTaxExclusiveOfPrice(false);
            cartItem.setSalePrice(salePrice);
            cartItem.setListPrice(listPrice);
            // must not clear any promotion data
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setProductSkuPromotion(final String skuCode, final BigDecimal promoPrice, final String promoCode) {
        final int skuIndex = indexOfProductSku(skuCode);
        if (skuIndex != -1) {
            final CartItemImpl cartItem = getItems().get(skuIndex);
            cartItem.setPrice(promoPrice);
            addPromoCode(cartItem, promoCode);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setShippingPromotion(final String carrierSlaId, final BigDecimal promoPrice, final String promoCode) {
        final int shipIndex = indexOfShipping(carrierSlaId);
        if (shipIndex != -1) {
            final CartItemImpl shipItem = getShipping().get(shipIndex);
            shipItem.setPrice(promoPrice);
            addPromoCode(shipItem, promoCode);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setProductSkuTax(final String skuCode, final BigDecimal netPrice, final BigDecimal grossPrice, final BigDecimal rate, final String taxCode, final boolean exclPrice) {
        final int skuIndex = indexOfProductSku(skuCode);
        if (skuIndex != -1) {
            final CartItemImpl cartItem = getItems().get(skuIndex);
            cartItem.setNetPrice(netPrice);
            cartItem.setGrossPrice(grossPrice);
            cartItem.setTaxRate(rate);
            cartItem.setTaxCode(taxCode);
            cartItem.setTaxExclusiveOfPrice(exclPrice);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean setShippingTax(final String carrierSlaPk, final BigDecimal netPrice, final BigDecimal grossPrice, final BigDecimal rate, final String taxCode, final boolean exclPrice) {
        final int shipIndex = indexOfShipping(carrierSlaPk);
        if (shipIndex != -1) {
            final CartItemImpl shipItem = getShipping().get(shipIndex);
            shipItem.setNetPrice(netPrice);
            shipItem.setGrossPrice(grossPrice);
            shipItem.setTaxRate(rate);
            shipItem.setTaxCode(taxCode);
            shipItem.setTaxExclusiveOfPrice(exclPrice);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public List<String> getCoupons() {
        return Collections.unmodifiableList(new ArrayList<String>(coupons));
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public List<String> getAppliedCoupons() {

        final List<String> all = new ArrayList<String>(coupons);
        final List<String> applied = new ArrayList<String>();

        if (!all.isEmpty()) {
            checkCouponPromoApplied(total.getAppliedOrderPromo(), all, applied);
            checkCouponPromoApplied(total.getAppliedDeliveryPromo(), all, applied);
        }

        if (!all.isEmpty()) {
            for (final CartItem item : getItems()) {
                if (all.isEmpty()) {
                    break;
                }
                final String appliedPromo = item.getAppliedPromo();
                checkCouponPromoApplied(appliedPromo, all, applied);
            }
        }

        if (!all.isEmpty()) {
            for (final CartItem gift : getGifts()) {
                if (all.isEmpty()) {
                    break;
                }
                final String appliedPromo = gift.getAppliedPromo();
                checkCouponPromoApplied(appliedPromo, all, applied);
            }
        }

        return applied;
    }

    private void checkCouponPromoApplied(final String appliedPromo, final List<String> all, final List<String> applied) {
        if (appliedPromo != null) {
            for (final String promo : StringUtils.split(appliedPromo, ',')) {
                final Iterator<String> enteredIt = all.iterator();
                while (enteredIt.hasNext()) {
                    final String entered = enteredIt.next();
                    if (promo.endsWith(":" + entered)) { // format of coupon promo is PROMOCODE:COUPON
                        applied.add(entered);
                        enteredIt.remove();
                        break;
                    }
                }
                if (all.isEmpty()) {
                    break;
                }
            }
        }
    }

    /** {@inheritDoc} */
    public boolean addCoupon(final String coupon) {
        return coupons.add(coupon);
    }

    /** {@inheritDoc} */
    public boolean removeCoupon(final String coupon) {
        return coupons.remove(coupon);
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public int getCartItemsCount() {
        BigDecimal quantity = BigDecimal.ZERO;
        for (CartItem cartItem : getItems()) {
            quantity = quantity.add(cartItem.getQty());
        }
        for (CartItem cartItem : getGifts()) {
            quantity = quantity.add(cartItem.getQty());
        }
        return quantity.intValue();
    }

    /** {@inheritDoc} */
    public int indexOfProductSku(final String skuCode) {
        return indexOf(skuCode, getItems());
    }

    /** {@inheritDoc} */
    public int indexOfShipping(final String carrierSlaId) {
        return indexOf(carrierSlaId, getShipping());
    }

    /** {@inheritDoc} */
    public int indexOfGift(final String skuCode) {
        return indexOf(skuCode, getGifts());
    }

    public int indexOf(final String skuCode, final List<? extends CartItem> items) {
        for (int index = 0; index < items.size(); index++) {
            final CartItem item = items.get(index);
            if (item.getProductSkuCode().equals(skuCode)) {
                return index;
            }
        }
        return -1;
    }



    /** {@inheritDoc} */
    public boolean contains(final String skuCode) {
        return (indexOfProductSku(skuCode) != -1);
    }


    /**
     * Internal access to mutable items.
     */
    List<CartItemImpl> getItems() {
        if (items == null) {
            items = new ArrayList<CartItemImpl>();
        }
        return items;
    }

    /**
     * Internal access to mutable items.
     */
    List<CartItemImpl> getGifts() {
        if (gifts == null) {
            gifts = new ArrayList<CartItemImpl>();
        }
        return gifts;
    }

    /**
     * Internal access to mutable items.
     */
    List<CartItemImpl> getShipping() {
        if (shipping == null) {
            shipping = new ArrayList<CartItemImpl>();
        }
        return shipping;
    }

    /** {@inheritDoc} */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /** {@inheritDoc} */
    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public String getCustomerName() {
        return getShoppingContext().getCustomerName();
    }

    /** {@inheritDoc} */
    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public boolean isModified() {
        return processingStartTimestamp < modifiedTimestamp;
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public String getCustomerEmail() {
        return  getShoppingContext().getCustomerEmail();
    }

    /** {@inheritDoc} */
    @JsonIgnore
    public int getLogonState() {
        if (StringUtils.isBlank(getCustomerEmail())
                   && StringUtils.isNotBlank(getCustomerName())) {
            return ShoppingCart.SESSION_EXPIRED;
        } else if (StringUtils.isNotBlank(getCustomerEmail())
                   && StringUtils.isNotBlank(getCustomerName())) {
            final String currentShop = getShoppingContext().getShopCode();
            if (getShoppingContext().getCustomerShops().contains(currentShop)) {
                return ShoppingCart.LOGGED_IN;
            }
            return ShoppingCart.INACTIVE_FOR_SHOP;
        }
        return ShoppingCart.NOT_LOGGED;
    }

    /** {@inheritDoc} */
    public MutableShoppingContext getShoppingContext() {
        if (shoppingContext == null) {
            shoppingContext = new ShoppingContextImpl();
        }
        return shoppingContext;
    }

    /** {@inheritDoc} */
    public String getCurrentLocale() {
        return currentLocale;
    }

    /** {@inheritDoc} */
    public void setCurrentLocale(final String currentLocale) {
        this.currentLocale = currentLocale;
    }

    /** {@inheritDoc} */
    public MutableOrderInfo getOrderInfo() {
        if (orderInfo == null) {
            orderInfo = new OrderInfoImpl();
        }
        return orderInfo;
    }

    /** {@inheritDoc} */
    public long getProcessingStartTimestamp() {
        return processingStartTimestamp;
    }

    /** {@inheritDoc} */
    public Total getTotal() {
        if (total == null) {
            ShopCodeContext.getLog(this).error("Total requested before cart was recalculated - revise page flow to ensure that recalculation happens");
            total = new TotalImpl();
        }
        return total;
    }
}
