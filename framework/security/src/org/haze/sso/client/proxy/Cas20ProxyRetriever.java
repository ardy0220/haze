/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.haze.sso.client.proxy;

import java.net.URL;
import java.net.URLEncoder;

import org.haze.base.json.JSONUtils;
import org.haze.base.lang.ResponseResult;
import org.haze.sso.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a ProxyRetriever that follows the CAS 2.0 specification.
 * For more information on the CAS 2.0 specification, please see the <a
 * href="http://www.jasig.org/cas/protocol">specification
 * document</a>.
 * <p/>
 * In general, this class will make a call to the CAS server with some specified
 * parameters and receive an XML response to parse.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class Cas20ProxyRetriever implements ProxyRetriever {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = 560409469568911792L;

    private static final Logger logger = LoggerFactory.getLogger(Cas20ProxyRetriever.class);

    /**
     * Url to CAS server.
     */
    private final String casServerUrl;

    private final String encoding;



    /**
     * Main Constructor.
     *
     * @param casServerUrl the URL to the CAS server (i.e. http://localhost/cas/)
     * @param encoding the encoding to use.
     * @param urlFactory url connection factory use when retrieving proxy responses from the server
     */
    public Cas20ProxyRetriever(final String casServerUrl, final String encoding) {
        CommonUtils.assertNotNull(casServerUrl, "casServerUrl cannot be null.");
        this.casServerUrl = casServerUrl;
        this.encoding = encoding;
    }

    public String getProxyTicketIdFor(final String proxyGrantingTicketId, final String targetService) {
        CommonUtils.assertNotNull(proxyGrantingTicketId, "proxyGrantingTicketId cannot be null.");
        CommonUtils.assertNotNull(targetService, "targetService cannot be null.");

        final URL url = constructUrl(proxyGrantingTicketId, targetService);
        final String response = CommonUtils.getResponseFromServer(url, this.encoding);
        logger.info("getProxyTicketIdFor response:" + response);
        
        ResponseResult result = JSONUtils.fromJson(response, ResponseResult.class);
        

        if (! result.getSuccess()) {
            logger.debug(result.getMessage());
            return null;
        }

        return result.getString("ticket");
    }

    private URL constructUrl(final String proxyGrantingTicketId, final String targetService) {
        try {
            return new URL(this.casServerUrl + (this.casServerUrl.endsWith("/") ? "" : "/") + "proxy" + "?pgt="
                    + proxyGrantingTicketId + "&targetService=" + URLEncoder.encode(targetService, "UTF-8"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}