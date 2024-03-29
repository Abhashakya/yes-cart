package org.yes.cart.web.page.component.js;

import org.apache.wicket.markup.html.basic.Label;
import org.yes.cart.web.page.component.BaseComponent;
import org.yes.cart.web.util.WicketUtil;

/**
 * User: denispavlov
 * Date: 31/03/2014
 * Time: 22:25
 */
public class ServerSideJs extends BaseComponent {

    public ServerSideJs(final String id) {
        super(id);
    }

    @Override
    protected void onBeforeRender() {

        addOrReplace(new Label("jsInclude", new StringBuilder()
            .append("<script type=\"text/javascript\">")
            .append("ctx.page = '").append(getPage().getClass().getSimpleName()).append("';\n")
            .append("ctx.root = '").append(getWicketUtil().getHttpServletRequest().getContextPath()).append("';\n")
            .append("ctx.resources['areYouSure'] = '").append(getLocalizer().getString("areYouSure", this)).append("';\n")
            .append("ctx.resources['yes'] = '").append(getLocalizer().getString("yes", this)).append("';\n")
            .append("ctx.resources['no'] = '").append(getLocalizer().getString("no", this)).append("';\n")
            .append("ctx.resources['wishlistTagsInfo'] = '").append(getLocalizer().getString("wishlistTagsInfo", this)).append("';\n")
            .append("ctx.resources['wishlistTagLinkOffInfo'] = '").append(getLocalizer().getString("wishlistTagLinkOffInfo", this)).append("';\n")
            .append("ctx.resources['wishlistTagLinkOnInfo'] = '").append(getLocalizer().getString("wishlistTagLinkOnInfo", this)).append("';\n")
            .append("</script>").toString()).setEscapeModelStrings(false));

        super.onBeforeRender();
    }
}
