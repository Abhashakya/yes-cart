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

package org.yes.cart.domain.ro;

import org.yes.cart.domain.ro.xml.impl.StringMapAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * User: denispavlov
 * Date: 21/08/2014
 * Time: 16:21
 */
@XmlRootElement(name = "register-form")
public class RegisterFormRO implements Serializable {

    private static final long serialVersionUID = 20150301L;

    private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private List<AttrValueCustomerRO> custom;

    @XmlElementWrapper(name = "custom")
    @XmlElement(name = "attribute")
    public List<AttrValueCustomerRO> getCustom() {
        return custom;
    }

    public void setCustom(final List<AttrValueCustomerRO> custom) {
        this.custom = custom;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }
}
