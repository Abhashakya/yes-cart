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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.yes.cart.web.page.component.footer.StandardFooter;
import org.yes.cart.web.page.component.header.HeaderMetaInclude;
import org.yes.cart.web.page.component.header.StandardHeader;
import org.yes.cart.web.page.component.js.ServerSideJs;

/**
 * User: igora Igor Azarny
 * Date: 4/28/12
 * Time: 1:58 PM
 */
public class ContactPage  extends AbstractWebPage {


    /**
     * Construct contact information page.
     * @param params page parameters.
     */
    public ContactPage(final PageParameters params) {
        super(params);
    }

    /**
     * {@inheritDoc}
     */
    protected void onBeforeRender() {

        executeHttpPostedCommands();
        add(
                new StandardFooter(FOOTER)
        ).add(
                new StandardHeader(HEADER)
        ).add(
                new ServerSideJs("serverSideJs")
        ).add(
                new HeaderMetaInclude("headerInclude")
        );

        super.onBeforeRender();
        persistCartIfNecessary();
    }

    /**
     * Get page title.
     *
     * @return page title
     */
    public IModel<String> getPageTitle() {
        return new Model<String>(getLocalizer().getString("contact",this));
    }
}
