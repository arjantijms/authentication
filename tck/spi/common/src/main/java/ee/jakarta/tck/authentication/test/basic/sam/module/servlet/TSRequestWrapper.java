/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.authentication.test.basic.sam.module.servlet;

import static java.util.logging.Level.INFO;

import ee.jakarta.tck.authentication.test.basic.servlet.JASPICData;
import ee.jakarta.tck.authentication.test.common.logging.server.TSLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class TSRequestWrapper extends HttpServletRequestWrapper {
    private TSLogger logger;

    Map<String, Object> optionsMap;

    public TSRequestWrapper(HttpServletRequest request) {
        super(request);
        logger = TSLogger.getTSLogger(JASPICData.LOGGER_NAME);
        logMsg("TSRequestWrapper constructor called");
    }

    @Override
    public Object getAttribute(String name) {
        if ("isRequestWrapped".equals(name)) {
            return Boolean.TRUE;
        }

        return super.getAttribute(name);
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        boolean bval = super.authenticate(response);

        debug("made it into TSRequestWrapper.authenticate()");

        //
        // NOTE:
        // It is not clear that flow will make it into this method. So we will
        // write out possible errors messages below and then check for occurances
        // of those error messages from within the tests in spi/servlet.
        //

        // Do some checks and validation relates to JASPIC 1.1 spec
        // section 3.8.4 (para 1) per assertion JASPIC:SPEC:322
        if (bval) {
            String msg = "";

            // "Both cases, must also ensure that the value returned by calling
            // getAuthType on the HttpServletRequest is consistent in terms of
            // being null or non-null with the value returned by getUserPrincipal."
            if (super.getAuthType() != null && super.getRemoteUser() != null) {

                // This is good - both non-null so this is okay
                msg = "HttpServletRequest authentication results match with getAuthType() and getRemoteUser()";
            } else if (super.getAuthType() == null && super.getRemoteUser() == null) {

                // This is good - both null, so this is okay too
                msg = "HttpServletRequest authentication results match with getAuthType() and getRemoteUser()";
            } else {

                // This is bad - must be mismatch between getAuthType() and
                // getRemoteUser()
                msg = "ERROR - HttpServletRequest authentication result mis-match with getAuthType() and getRemoteUser()";
            }

            logger.log(INFO, msg);
        }

        // Test for assertion: JASPIC:SPEC:323 from spec section 3.8.4, para 2:
        // check if getAuthType() != null, and if not null, then check if
        // MessageInfo Map
        // sets/users key=jakarta.servlet.http.authType. If so, getAuthType should be
        // set
        // set to value of key. getAuthType should not be null on successful authN.
        if (bval) {
            String msg = "";

            if (super.getAuthType() != null && optionsMap != null) {

                // See if key=jakarta.servlet.http.authType exists and if so, make
                // sure it matches the getAuthType() value
                if (optionsMap.get("jakarta.servlet.http.authType") != null) {

                    // If here, then we need to make sure the value specified for
                    // getAuthType matches this value.
                    String val = (String) optionsMap.get("jakarta.servlet.http.authType");
                    if (val == null) {

                        // Spec violation - cant be null if key exists!!!
                        msg = "ERROR - invalid setting for jakarta.servlet.http.authType = null";
                    } else if (!val.equalsIgnoreCase(super.getAuthType())) {

                        // Spec violation - these have to match!!
                        msg = "ERROR - mismatch value set for jakarta.servlet.http.authType and getAuthType()";
                    } else {

                        // We are good if here.
                        msg = "getAuthType() matches value for jakarta.servlet.http.authType";
                    }

                    logger.log(INFO, msg);
                    debug(msg);
                    debug("authenticate(): getAuthType() = " + super.getAuthType());
                    debug("authenticate(): jakarta.servlet.http.authType  = " + val);
                }
            }

        }

        return bval;
    }

    public void setOptionsMap(Map<String, Object> options) {
        optionsMap = options;
    }

    public Map<String, Object> getOptionsMap() {
        return optionsMap;
    }

    public void logMsg(String str) {
        if (logger != null) {
            logger.log(INFO, str);
        } else {
            System.out.println("*** TSLogger Not Initialized properly ***");
            System.out.println("*** TSSVLogMessage : ***" + str);
        }
    }

    public void debug(String str) {
        System.out.println("TSRequestWrapper:  " + str);
    }

}
