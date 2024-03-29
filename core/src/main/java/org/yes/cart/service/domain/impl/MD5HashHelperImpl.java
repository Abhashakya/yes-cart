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

package org.yes.cart.service.domain.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.yes.cart.service.domain.HashHelper;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class MD5HashHelperImpl implements HashHelper, PasswordEncoder {

    private String salt = "";

    public void setSalt(final String salt) {
        this.salt = salt;
    }

    /**
     * Get the md5 string from given password.
     *
     * @param password given password
     * @return md5 password string
     * @throws java.security.NoSuchAlgorithmException     NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException UnsupportedEncodingException
     */
    @Cacheable(value = "passwordHashHelper-hash")
    public String getHash(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] md5hash;

        final String saltedPassword = StringUtils.isNotBlank(salt) ? password.concat(salt) : password;

        digest.update(saltedPassword.getBytes("utf-8"), 0, saltedPassword.length());
        md5hash = digest.digest();
        return convertToHex(md5hash);
    }

    private String convertToHex(final byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encodePassword(final String rawPass, final Object salt) {
        try {
            return self().getHash(rawPass);
        } catch (Exception e) {
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPasswordValid(final String encPass, final String rawPass, final Object salt) {
        return encodePassword(rawPass, salt).equals(encPass);
    }

    private HashHelper self;

    private HashHelper self() {
        if (self == null) {
            self = getSelf();
        }
        return self;
    }

    public HashHelper getSelf() {
        return null;
    }

}
