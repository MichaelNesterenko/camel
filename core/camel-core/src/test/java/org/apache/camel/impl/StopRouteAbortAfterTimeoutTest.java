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
package org.apache.camel.impl;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO semantically equivalent to org.apache.camel.management.ManagedRouteStopWithAbortAfterTimeoutTest, needs unification
public class StopRouteAbortAfterTimeoutTest extends ContextTestSupport {

    static final int MESSAGE_DELAY_MS = 10_000;
    static final int MESSAGE_COUNT = 5;
    static final int ROUTE_SHUTDOWN_AWAIT_MS = MESSAGE_COUNT * MESSAGE_DELAY_MS / 50;
    static final int CTX_SHUTDOWN_TIMEOUT_SEC = 3;

    @Test
    public void testStopRouteWithAbortAfterTimeoutTrue() throws Exception {
        var totalMessageCount = 2 * MESSAGE_COUNT;
        getMockEndpoint("mock:result").setExpectedMessageCount(totalMessageCount);

        // send some message through the route
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        // stop route with a 1s timeout and abortAfterTimeout=true (should abort
        // after 1s)
        boolean stopRouteResponse
                = context.getRouteController().stopRoute("start", ROUTE_SHUTDOWN_AWAIT_MS, TimeUnit.MILLISECONDS, true);

        // confirm that route is still running
        assertFalse(stopRouteResponse, "stopRoute response should be False");
        assertTrue(context.getRouteController().getRouteStatus("start").isStarted(), "route should still be started");

        // send some more messages through the route
        for (int i = MESSAGE_COUNT; i < 2 * MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        assertMockEndpointsSatisfied(totalMessageCount * MESSAGE_DELAY_MS, TimeUnit.SECONDS);
    }

    @Test
    public void testStopRouteWithAbortAfterTimeoutFalse() throws Exception {
        MockEndpoint mockEP = getMockEndpoint("mock:result");

        // send some message through the route
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        // stop route with a 1s timeout and abortAfterTimeout=false (normal
        // timeout behavior)
        boolean stopRouteResponse
                = context.getRouteController().stopRoute("start", ROUTE_SHUTDOWN_AWAIT_MS, TimeUnit.MILLISECONDS, false);

        // the route should have been forced stopped
        assertTrue(stopRouteResponse, "stopRoute response should be True");
        assertTrue(context.getRouteController().getRouteStatus("start").isStopped(), "route should be stopped");

        int before = mockEP.getExchanges().size();

        // send some more messages through the route
        for (int i = MESSAGE_COUNT; i < 2 * MESSAGE_COUNT; i++) {
            template.sendBody("seda:start", "message-" + i);
        }

        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(
                () -> assertTrue(mockEP.getExchanges().size() == before, "Should not have received more than 5 messages"));
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
