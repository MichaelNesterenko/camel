/* Generated by camel build tools - do NOT edit this file! */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.builder.component.dsl;

import javax.annotation.processing.Generated;
import org.apache.camel.Component;
import org.apache.camel.builder.component.AbstractComponentBuilder;
import org.apache.camel.builder.component.ComponentBuilder;
import org.apache.camel.component.etcd3.Etcd3Component;

/**
 * Get, set, delete or watch keys in etcd key-value store.
 * 
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface Etcd3ComponentBuilderFactory {

    /**
     * Etcd v3 (camel-etcd3)
     * Get, set, delete or watch keys in etcd key-value store.
     * 
     * Category: clustering,database
     * Since: 3.19
     * Maven coordinates: org.apache.camel:camel-etcd3
     * 
     * @return the dsl builder
     */
    static Etcd3ComponentBuilder etcd3() {
        return new Etcd3ComponentBuilderImpl();
    }

    /**
     * Builder for the Etcd v3 component.
     */
    interface Etcd3ComponentBuilder extends ComponentBuilder<Etcd3Component> {
    
        /**
         * Component configuration.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.etcd3.Etcd3Configuration&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder configuration(org.apache.camel.component.etcd3.Etcd3Configuration configuration) {
            doSetProperty("configuration", configuration);
            return this;
        }
    
        
        /**
         * Configure etcd server endpoints using the IPNameResolver.
         * 
         * The option is a: &lt;code&gt;java.lang.String[]&lt;/code&gt; type.
         * 
         * Default: http://localhost:2379
         * Group: common
         * 
         * @param endpoints the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder endpoints(java.lang.String[] endpoints) {
            doSetProperty("endpoints", endpoints);
            return this;
        }
    
        
        /**
         * Configure the charset to use for the keys.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: UTF-8
         * Group: common
         * 
         * @param keyCharset the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder keyCharset(java.lang.String keyCharset) {
            doSetProperty("keyCharset", keyCharset);
            return this;
        }
    
        /**
         * Configure the namespace of keys used. / will be treated as no
         * namespace.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param namespace the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder namespace(java.lang.String namespace) {
            doSetProperty("namespace", namespace);
            return this;
        }
    
        
        /**
         * To apply an action on all the key-value pairs whose key that starts
         * with the target path.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param prefix the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder prefix(boolean prefix) {
            doSetProperty("prefix", prefix);
            return this;
        }
    
        
        /**
         * Allows for bridging the consumer to the Camel routing Error Handler,
         * which mean any exceptions (if possible) occurred while the Camel
         * consumer is trying to pickup incoming messages, or the likes, will
         * now be processed as a message and handled by the routing Error
         * Handler. Important: This is only possible if the 3rd party component
         * allows Camel to be alerted if an exception was thrown. Some
         * components handle this internally only, and therefore
         * bridgeErrorHandler is not possible. In other situations we may
         * improve the Camel component to hook into the 3rd party component and
         * make this possible for future releases. By default the consumer will
         * use the org.apache.camel.spi.ExceptionHandler to deal with
         * exceptions, that will be logged at WARN or ERROR level and ignored.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: consumer
         * 
         * @param bridgeErrorHandler the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder bridgeErrorHandler(boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
            return this;
        }
    
        /**
         * The index to watch from.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Group: consumer (advanced)
         * 
         * @param fromIndex the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder fromIndex(long fromIndex) {
            doSetProperty("fromIndex", fromIndex);
            return this;
        }
    
        
        /**
         * Whether the producer should be started lazy (on the first message).
         * By starting lazy you can use this to allow CamelContext and routes to
         * startup in situations where a producer may otherwise fail during
         * starting and cause the route to fail being started. By deferring this
         * startup to be lazy then the startup failure can be handled during
         * routing messages via Camel's routing error handlers. Beware that when
         * the first message is processed then creating and starting the
         * producer may take a little time and prolong the total processing time
         * of the processing.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder lazyStartProducer(boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
    
        
        /**
         * Configure the charset to use for the values.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: UTF-8
         * Group: producer
         * 
         * @param valueCharset the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder valueCharset(java.lang.String valueCharset) {
            doSetProperty("valueCharset", valueCharset);
            return this;
        }
    
        /**
         * Configure the headers to be added to auth request headers.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.lang.String&amp;gt;&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param authHeaders the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder authHeaders(java.util.Map<java.lang.String, java.lang.String> authHeaders) {
            doSetProperty("authHeaders", authHeaders);
            return this;
        }
    
        /**
         * Configure the authority used to authenticate connections to servers.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param authority the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder authority(java.lang.String authority) {
            doSetProperty("authority", authority);
            return this;
        }
    
        
        /**
         * Whether autowiring is enabled. This is used for automatic autowiring
         * options (the option must be marked as autowired) by looking up in the
         * registry to find if there is a single instance of matching type,
         * which then gets configured on the component. This can be used for
         * automatic configuring JDBC data sources, JMS connection factories,
         * AWS Clients, etc.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param autowiredEnabled the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
    
        /**
         * Configure the connection timeout.
         * 
         * The option is a: &lt;code&gt;java.time.Duration&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param connectionTimeout the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder connectionTimeout(java.time.Duration connectionTimeout) {
            doSetProperty("connectionTimeout", connectionTimeout);
            return this;
        }
    
        /**
         * Configure the headers to be added to http request headers.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.lang.String&amp;gt;&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param headers the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder headers(java.util.Map<java.lang.String, java.lang.String> headers) {
            doSetProperty("headers", headers);
            return this;
        }
    
        
        /**
         * Configure the interval for gRPC keepalives. The current minimum
         * allowed by gRPC is 10 seconds.
         * 
         * The option is a: &lt;code&gt;java.time.Duration&lt;/code&gt; type.
         * 
         * Default: 30 seconds
         * Group: advanced
         * 
         * @param keepAliveTime the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder keepAliveTime(java.time.Duration keepAliveTime) {
            doSetProperty("keepAliveTime", keepAliveTime);
            return this;
        }
    
        
        /**
         * Configure the timeout for gRPC keepalives.
         * 
         * The option is a: &lt;code&gt;java.time.Duration&lt;/code&gt; type.
         * 
         * Default: 10 seconds
         * Group: advanced
         * 
         * @param keepAliveTimeout the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder keepAliveTimeout(java.time.Duration keepAliveTimeout) {
            doSetProperty("keepAliveTimeout", keepAliveTimeout);
            return this;
        }
    
        /**
         * Configure etcd load balancer policy.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param loadBalancerPolicy the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder loadBalancerPolicy(java.lang.String loadBalancerPolicy) {
            doSetProperty("loadBalancerPolicy", loadBalancerPolicy);
            return this;
        }
    
        /**
         * Configure the maximum message size allowed for a single gRPC frame.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param maxInboundMessageSize the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder maxInboundMessageSize(java.lang.Integer maxInboundMessageSize) {
            doSetProperty("maxInboundMessageSize", maxInboundMessageSize);
            return this;
        }
    
        
        /**
         * Configure the delay between retries in milliseconds.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Default: 500
         * Group: advanced
         * 
         * @param retryDelay the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder retryDelay(long retryDelay) {
            doSetProperty("retryDelay", retryDelay);
            return this;
        }
    
        
        /**
         * Configure the max backing off delay between retries in milliseconds.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Default: 2500
         * Group: advanced
         * 
         * @param retryMaxDelay the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder retryMaxDelay(long retryMaxDelay) {
            doSetProperty("retryMaxDelay", retryMaxDelay);
            return this;
        }
    
        /**
         * Configure the retries max duration.
         * 
         * The option is a: &lt;code&gt;java.time.Duration&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param retryMaxDuration the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder retryMaxDuration(java.time.Duration retryMaxDuration) {
            doSetProperty("retryMaxDuration", retryMaxDuration);
            return this;
        }
    
        
        /**
         * The path to look for service discovery.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: /services/
         * Group: cloud
         * 
         * @param servicePath the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder servicePath(java.lang.String servicePath) {
            doSetProperty("servicePath", servicePath);
            return this;
        }
    
        /**
         * Configure etcd auth password.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param password the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder password(java.lang.String password) {
            doSetProperty("password", password);
            return this;
        }
    
        /**
         * Configure SSL/TLS context to use instead of the system default.
         * 
         * The option is a:
         * &lt;code&gt;io.netty.handler.ssl.SslContext&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslContext the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder sslContext(io.netty.handler.ssl.SslContext sslContext) {
            doSetProperty("sslContext", sslContext);
            return this;
        }
    
        /**
         * Configure etcd auth user.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param userName the value to set
         * @return the dsl builder
         */
        default Etcd3ComponentBuilder userName(java.lang.String userName) {
            doSetProperty("userName", userName);
            return this;
        }
    }

    class Etcd3ComponentBuilderImpl
            extends AbstractComponentBuilder<Etcd3Component>
            implements Etcd3ComponentBuilder {
        @Override
        protected Etcd3Component buildConcreteComponent() {
            return new Etcd3Component();
        }
        private org.apache.camel.component.etcd3.Etcd3Configuration getOrCreateConfiguration(Etcd3Component component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.etcd3.Etcd3Configuration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "configuration": ((Etcd3Component) component).setConfiguration((org.apache.camel.component.etcd3.Etcd3Configuration) value); return true;
            case "endpoints": getOrCreateConfiguration((Etcd3Component) component).setEndpoints((java.lang.String[]) value); return true;
            case "keyCharset": getOrCreateConfiguration((Etcd3Component) component).setKeyCharset((java.lang.String) value); return true;
            case "namespace": getOrCreateConfiguration((Etcd3Component) component).setNamespace((java.lang.String) value); return true;
            case "prefix": getOrCreateConfiguration((Etcd3Component) component).setPrefix((boolean) value); return true;
            case "bridgeErrorHandler": ((Etcd3Component) component).setBridgeErrorHandler((boolean) value); return true;
            case "fromIndex": getOrCreateConfiguration((Etcd3Component) component).setFromIndex((long) value); return true;
            case "lazyStartProducer": ((Etcd3Component) component).setLazyStartProducer((boolean) value); return true;
            case "valueCharset": getOrCreateConfiguration((Etcd3Component) component).setValueCharset((java.lang.String) value); return true;
            case "authHeaders": getOrCreateConfiguration((Etcd3Component) component).setAuthHeaders((java.util.Map) value); return true;
            case "authority": getOrCreateConfiguration((Etcd3Component) component).setAuthority((java.lang.String) value); return true;
            case "autowiredEnabled": ((Etcd3Component) component).setAutowiredEnabled((boolean) value); return true;
            case "connectionTimeout": getOrCreateConfiguration((Etcd3Component) component).setConnectionTimeout((java.time.Duration) value); return true;
            case "headers": getOrCreateConfiguration((Etcd3Component) component).setHeaders((java.util.Map) value); return true;
            case "keepAliveTime": getOrCreateConfiguration((Etcd3Component) component).setKeepAliveTime((java.time.Duration) value); return true;
            case "keepAliveTimeout": getOrCreateConfiguration((Etcd3Component) component).setKeepAliveTimeout((java.time.Duration) value); return true;
            case "loadBalancerPolicy": getOrCreateConfiguration((Etcd3Component) component).setLoadBalancerPolicy((java.lang.String) value); return true;
            case "maxInboundMessageSize": getOrCreateConfiguration((Etcd3Component) component).setMaxInboundMessageSize((java.lang.Integer) value); return true;
            case "retryDelay": getOrCreateConfiguration((Etcd3Component) component).setRetryDelay((long) value); return true;
            case "retryMaxDelay": getOrCreateConfiguration((Etcd3Component) component).setRetryMaxDelay((long) value); return true;
            case "retryMaxDuration": getOrCreateConfiguration((Etcd3Component) component).setRetryMaxDuration((java.time.Duration) value); return true;
            case "servicePath": getOrCreateConfiguration((Etcd3Component) component).setServicePath((java.lang.String) value); return true;
            case "password": getOrCreateConfiguration((Etcd3Component) component).setPassword((java.lang.String) value); return true;
            case "sslContext": getOrCreateConfiguration((Etcd3Component) component).setSslContext((io.netty.handler.ssl.SslContext) value); return true;
            case "userName": getOrCreateConfiguration((Etcd3Component) component).setUserName((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}