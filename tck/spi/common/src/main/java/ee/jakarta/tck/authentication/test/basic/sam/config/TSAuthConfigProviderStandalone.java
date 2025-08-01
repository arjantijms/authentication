/*
 * Copyright (c) 2024, 2025 Contributors to Eclipse Foundation.
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.authentication.test.basic.sam.config;

import static java.util.logging.Level.INFO;

import ee.jakarta.tck.authentication.test.basic.servlet.JASPICData;
import ee.jakarta.tck.authentication.test.common.logging.server.TSLogger;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.config.ClientAuthConfig;
import jakarta.security.auth.message.config.ServerAuthConfig;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 * This class is an implementation of the AuthConfigProvider for use by the core tests. This is used to support the
 * scenario when a profile other than Servlet/SOAP is being defined and used with Jakarta Authentication.
 *
 * At the time of this test creation, the 2 main supported profiles are SOAP and Servlet. But it is very possible that
 * someone may use Jakarta Authentication to define their own profiles. For that case, we need to have something we can use for the
 * core tests besides SOAP/Servlet tests. This is used to support that case.
 *
 * @author Sun Microsystems
 */
public class TSAuthConfigProviderStandalone implements jakarta.security.auth.message.config.AuthConfigProvider {

    // this is a default string used for Persistent Registration description
    public static String DESC_KEY = "description_key";

    private static TSLogger logger;

    private Map<String, ServerAuthConfig> serverAuthConfigMap = new HashMap<>();

    private static Map<String, Object> properties;

    private String description;

    // This will be called when a vendor registers TSAuthConfigProviderStandalone
    public TSAuthConfigProviderStandalone(Map<String, Object> props, AuthConfigFactory factory) {
        this(props, null, factory, null);

        logger.log(INFO, "invoked TSAuthConfigProviderStandalone() constructor(2 args)");
    }

    /**
     * This constructor takes a TSLogger instance as a param.
     */
    public TSAuthConfigProviderStandalone(Map<String, Object> props, TSLogger tsLogger, AuthConfigFactory factory) {
        this(props, tsLogger, factory, null);

        logger.log(INFO, "invoked TSAuthConfigProviderStandalone() constructor(3 args)");
    }

    /**
     * This constructor takes a TSLogger instance as a param.
     */
    public TSAuthConfigProviderStandalone(Map<String, Object> props, TSLogger tsLogger, AuthConfigFactory factory, String description) {
        properties = props;

        // For self registration
        if (factory != null) {
            factory.registerConfigProvider(this, null, null, "TSAuthConfigProviderStandalone self registration");
        }

        if (tsLogger == null) {
            initializeTSLogger();
        } else {
            TSAuthConfigProviderStandalone.logger = tsLogger;
        }

        if (description != null) {
            // this is usually the case for in-memory registration
            this.description = description;
        } else {
            // this is usually the case for persistent registration
            this.description = getDescFromProps(props);
            if (this.description == null) {
                this.description = "PERSIST_DEFAULT";
            }
        }

        logger.log(INFO, "invoked TSAuthConfigProviderStandalone() constructor(4 args)");
    }

    private String getDescFromProps(Map<String, Object> props) {
        String strDesc = null;
        if (props != null) {
            strDesc = (String) props.get(DESC_KEY);
        }

        return strDesc;
    }

    /**
     * Get an instance of ClientAuthConfig from this provider.
     *
     * <p>
     * The implementation of this method returns a ClientAuthConfig instance that describes the configuration of
     * ClientAuthModules at a given message layer, and for use in an identified application context.
     *
     * @param layer a String identifying the message layer for the returned ClientAuthConfig object. This argument must not
     * be null.
     *
     * @param appContext a String that identifies the messaging context for the returned ClientAuthConfig object. This
     * argument must not be null.
     *
     * @param handler a CallbackHandler to be passed to the ClientAuthModules encapsulated by ClientAuthContext objects
     * derived from the returned ClientAuthConfig. This argument may be null, in which case the implementation may assign a
     * default handler to the configuration.
     *
     * @return a ClientAuthConfig Object that describes the configuration of ClientAuthModules at the message layer and
     * messaging context identified by the layer and appContext arguments. This method does not return null.
     *
     * @exception AuthException if this provider does not support the assignment of a default CallbackHandler to the
     * returned ClientAuthConfig.
     *
     * @exception SecurityException if the caller does not have permission to retrieve the configuration.
     *
     * The CallbackHandler assigned to the configuration must support the Callback objects required to be supported by the
     * profile of this specification being followed by the messaging runtime. The CallbackHandler instance must be
     * initialized with any application context needed to process the required callbacks on behalf of the corresponding
     * application.
     */
    @Override
    public ClientAuthConfig getClientAuthConfig(String layer, String appContext, CallbackHandler handler) throws AuthException {
        logger.log(INFO, "WARNING:  shouldnt get into ClientAuthConfig() for servlet profile");

        // Shouldnt get in here for servlet profile
        return null;
    }

    /**
     * Get an instance of ServerAuthConfig from this provider.
     *
     * <p>
     * The implementation of this method returns a ServerAuthConfig instance that describes the configuration of
     * ServerAuthModules at a given message layer, and for a particular application context.
     *
     * @param layer a String identifying the message layer for the returned ServerAuthConfig object. This argument must not
     * be null.
     *
     * @param appContext a String that identifies the messaging context for the returned ServerAuthConfig object. This
     * argument must not be null.
     *
     * @param handler a CallbackHandler to be passed to the ServerAuthModules encapsulated by ServerAuthContext objects
     * derived from the returned ServerAuthConfig. This argument may be null, in which case the implementation may assign a
     * default handler to the configuration.
     *
     * @return a ServerAuthConfig Object that describes the configuration of ServerAuthModules at a given message layer, and
     * for a particular application context. This method does not return null.
     *
     * @exception AuthException if this provider does not support the assignment of a default CallbackHandler to the
     * returned ServerAuthConfig.
     *
     * @exception SecurityException if the caller does not have permission to retrieve the configuration.
     * <p>
     * The CallbackHandler assigned to the configuration must support the Callback objects required to be supported by the
     * profile of this specification being followed by the messaging runtime. The CallbackHandler instance must be
     * initialized with any application context needed to process the required callbacks on behalf of the corresponding
     * application.
     */
    @Override
    public ServerAuthConfig getServerAuthConfig(String layer, String appContext, CallbackHandler handler) throws AuthException {
        logger.log(INFO, "TSAuthConfigProviderStandalone.getServerAuthConfig() called");

        String logStr = "TSAuthConfigProviderStandalone.getServerAuthConfig" + " : layer=" + layer + " : appContext=" + appContext;
        logger.log(INFO, logStr);
        try {
            if (handler == null) {
                // this is used to help verify assertion JASPI:SPEC:71 which
                // that we should NOT have a null cbh passed in
                String msg = "FAILURE: layer=" + layer + " appContext=" + appContext;
                msg += " getServerAuthConfig() received CallbackHandler=null";
                logger.log(INFO, msg);
            } else {
                String msg = "layer=" + layer + " appContext=" + appContext;
                msg += " getServerAuthConfig() received CallbackHandler=non-null";
                logger.log(INFO, msg);
            }

            ServerAuthConfig serverAuthConfig = new TSServerAuthConfig(layer, appContext, handler, properties, logger);
            serverAuthConfigMap.put(layer + appContext, serverAuthConfig);

            return serverAuthConfig;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthException(e.getMessage());
        }
    }

    /**
     * Causes a dynamic configuration provider to update its internal state such that any resulting change to its state is
     * reflected in the corresponding authentication context configuration objects previously created by the provider within
     * the current process context.
     *
     * @exception AuthException if an error occured during the refresh.
     *
     * @exception SecurityException if the caller does not have permission to refresh the provider.
     */
    @Override
    public void refresh() {
    }

    private static void initializeTSLogger() {
        if (logger != null) {
            return;
        }
        try {
            logger = TSLogger.getTSLogger(JASPICData.LOGGER_NAME);
        } catch (Exception e) {
            throw new RuntimeException("TSLogger Initialization failed", e);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String val) {
        description = val;
    }
}
