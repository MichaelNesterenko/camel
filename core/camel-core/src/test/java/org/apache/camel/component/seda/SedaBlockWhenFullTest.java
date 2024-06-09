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
package org.apache.camel.component.seda;

import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that a Seda producer supports the blockWhenFull option by blocking when a message is sent while the queue is
 * full.
 */
public class SedaBlockWhenFullTest extends ContextTestSupport {
    private static final int QUEUE_SIZE = 1;
    private static final int DELAY = 1000;
    private static final int OFFER_TIMEOUT = 2 * DELAY;
    private static final String MOCK_URI = "mock:blockWhenFullOutput";
    private static final String SEDA_WITH_OFFER_TIMEOUT_URI
            = "seda:blockingFoo?size=%d&blockWhenFull=true&offerTimeout=200".formatted(QUEUE_SIZE);
    private static final String BLOCK_WHEN_FULL_URI
            = "seda:blockingBar?size=%d&blockWhenFull=true&timeout=0&offerTimeout=%s".formatted(QUEUE_SIZE, OFFER_TIMEOUT);
    private static final String DEFAULT_URI = "seda:foo?size=%d".formatted(QUEUE_SIZE);

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(BLOCK_WHEN_FULL_URI).delay(DELAY).syncDelayed().to(MOCK_URI);

                // use same delay as above on purpose
                from(DEFAULT_URI).delay(DELAY).syncDelayed().to("mock:whatever");
            }
        };
    }

    @Test
    public void testSedaOfferTimeoutWhenFull() {
        SedaEndpoint seda = context.getEndpoint(SEDA_WITH_OFFER_TIMEOUT_URI, SedaEndpoint.class);
        assertEquals(QUEUE_SIZE, seda.getQueue().remainingCapacity());

        Exception e = assertThrows(Exception.class, () -> sendTwoOverCapacity(SEDA_WITH_OFFER_TIMEOUT_URI, QUEUE_SIZE),
                "Failed to insert element into queue, after timeout of " + seda.getOfferTimeout() + " milliseconds");
        assertIsInstanceOf(IllegalStateException.class, e.getCause());
    }

    @Test
    public void testSedaDefaultWhenFull() {
        SedaEndpoint seda = context.getEndpoint(DEFAULT_URI, SedaEndpoint.class);
        assertFalse(seda.isBlockWhenFull(),
                "Seda Endpoint is not setting the correct default (should be false) for \"blockWhenFull\"");

        Exception e = assertThrows(Exception.class, () -> sendTwoOverCapacity(DEFAULT_URI, QUEUE_SIZE),
                "The route didn't fill the queue beyond capacity: test class isn't working as intended");
        assertIsInstanceOf(IllegalStateException.class, e.getCause());
    }

    @Test
    public void testSedaBlockingWhenFull() throws Exception {
        getMockEndpoint(MOCK_URI).setExpectedMessageCount(QUEUE_SIZE + 2);
        SedaEndpoint seda = context.getEndpoint(BLOCK_WHEN_FULL_URI, SedaEndpoint.class);
        assertEquals(QUEUE_SIZE, seda.getQueue().remainingCapacity());

        sendTwoOverCapacity(BLOCK_WHEN_FULL_URI, QUEUE_SIZE);
        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Test
    public void testAsyncSedaBlockingWhenFull() throws Exception {
        getMockEndpoint(MOCK_URI).setExpectedMessageCount(QUEUE_SIZE + 2);

        SedaEndpoint seda = context.getEndpoint(BLOCK_WHEN_FULL_URI, SedaEndpoint.class);
        assertEquals(QUEUE_SIZE, seda.getQueue().remainingCapacity());

        asyncSendTwoOverCapacity(BLOCK_WHEN_FULL_URI, QUEUE_SIZE);
        // wait a bit to allow the async processing to complete
        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    /**
     * This method make sure that we hit the limit by sending two msg over the given capacity which allows the delayer
     * to kick in, leaving the 2nd msg in the queue, blocking/throwing on the third one.
     */
    private void sendTwoOverCapacity(String uri, int capacity) {
        for (int i = 0; i < (capacity + 2); i++) {
            template.sendBody(uri, "Message " + i);
        }
    }

    private void asyncSendTwoOverCapacity(String uri, int capacity) {
        for (int i = 0; i < (capacity + 2); i++) {
            template.asyncSendBody(uri, "Message " + i);
        }
    }

}
