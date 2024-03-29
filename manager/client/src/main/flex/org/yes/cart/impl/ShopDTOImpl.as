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

package org.yes.cart.impl {

[Bindable]
[RemoteClass(alias="org.yes.cart.domain.dto.impl.ShopDTOImpl")]
public class ShopDTOImpl {

    public var code:String;

    public var name:String;

    public var description:String;

    public var fspointer:String;

    public var shopId:Number;

    public var uri:String;

    public var title:String;

    public var metakeywords:String;

    public var metadescription:String;

    public var displayTitles:Object;

    public var displayMetakeywords:Object;

    public var displayMetadescriptions:Object;


    public function ShopDTOImpl() {
        shopId = 0;
    }


    public function toString():String {
        return String(name);
    }
}
}