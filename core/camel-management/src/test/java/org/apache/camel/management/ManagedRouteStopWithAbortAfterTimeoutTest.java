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
package org.apache.camel.management;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO semantically equivalent to org.apache.camel.impl.StopRouteAbortAfterTimeoutTest, needs unification
@DisabledOnOs({ OS.WINDOWS, OS.AIX })
public class ManagedRouteStopWithAbortAfterTimeoutTest extends ManagementTestSupport {

    static final int MESSAGE_DELAY_MS = 10_000;
    static final int MESSAGE_COUNT = 5;
    static final int ROUTE_SHUTDOWN_AWAIT_MS = MESSAGE_COUNT * MESSAGE_DELAY_MS / 50;
    static final int CTX_SHUTDOWN_TIMEOUT_SEC = 3;

    @Test
    public void testStopRouteWithAbortAfterTimeoutTrue() throws Exception {
        var totalMessageCount = 2 * MESSAGE_COUNT;
        getMockEndpoint("mock:result").setExpectedMessageCount(10);

        MBeanServer mbeanServer = getMBeanServer();
        ObjectName on = getRouteObjectName(mbeanServer);

        // confirm that route has started
        String state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals(ServiceStatus.Started.name(), state, "route should be started");

        //send some message through the route
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        // stop route with a timeout and abortAfterTimeout=true (should abort )
        Long timeout = Long.valueOf(ROUTE_SHUTDOWN_AWAIT_MS / 1000);
        Boolean abortAfterTimeout = Boolean.TRUE;
        Object[] params = { timeout, abortAfterTimeout };
        String[] sig = { "java.lang.Long", "java.lang.Boolean" };
        Boolean stopRouteResponse = (Boolean) mbeanServer.invoke(on, "stop", params, sig);

        // confirm that route is still running
        state = (String) mbeanServer.getAttribute(on, "State");
        assertFalse(stopRouteResponse, "stopRoute response should be False");
        assertEquals(ServiceStatus.Started.name(), state, "route should still be started");

        //send some more messages through the route
        for (int i = MESSAGE_COUNT; i < 2 * MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        assertMockEndpointsSatisfied(totalMessageCount * MESSAGE_DELAY_MS, TimeUnit.SECONDS);
    }

    @Test
    public void testStopRouteWithAbortAfterTimeoutFalse() throws Exception {
        MockEndpoint mockEP = getMockEndpoint("mock:result");

        MBeanServer mbeanServer = getMBeanServer();
        ObjectName on = getRouteObjectName(mbeanServer);

        // confirm that route has started
        String state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals(ServiceStatus.Started.name(), state, "route should be started");

        //send some message through the route
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        // stop route with a 1s timeout and abortAfterTimeout=false (normal timeout behavior)
        Long timeout = Long.valueOf(ROUTE_SHUTDOWN_AWAIT_MS);
        Boolean abortAfterTimeout = Boolean.FALSE;
        Object[] params = { timeout, abortAfterTimeout };
        String[] sig = { "java.lang.Long", "java.lang.Boolean" };
        Boolean stopRouteResponse = (Boolean) mbeanServer.invoke(on, "stop", params, sig);

        // confirm that route is stopped
        state = (String) mbeanServer.getAttribute(on, "State");
        assertTrue(stopRouteResponse, "stopRoute response should be True");
        assertEquals(ServiceStatus.Stopped.name(), state, "route should be stopped");

        int before = mockEP.getExchanges().size();

        // send some more messages through the route
        for (int i = MESSAGE_COUNT; i < 2 * MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(
                () -> assertTrue(mockEP.getExchanges().size() == before, "Should not have received more than 5 messages"));
    }

    static ObjectName getRouteObjectName(MBeanServer mbeanServer) throws Exception {
        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=routes,*"), null);
        assertEquals(1, set.size());
        return set.iterator().next();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // shutdown this test faster
                context.getShutdownStrategy().setTimeout(CTX_SHUTDOWN_TIMEOUT_SEC);

                from("seda:start").routeId("start").delay(MESSAGE_DELAY_MS).to("mock:result");
            }
        };
    }
}
