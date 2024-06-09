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
package org.apache.camel.processor.throttle.requests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.ThrottlerRejectedExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
public class ThrottlerTest extends ContextTestSupport {
    private static final int THROTTLE_WINDOW_DURATION_MS = 1_000;
    private static final int THROTTLE_WINDOW_SIZE = 3;

    private static final int THROTTLE_WINDOW_PRESET_SIZE = 5;

    private static final int MESSAGE_COUNT = 9;
    private static final int WORKER_COUNT = 9;

    @Test
    public void testSendLotsOfMessagesAndThrottleDuringThrottleWindow() throws Exception {
        MockEndpoint resultEndpoint = getMockEndpoint("mock:result");
        resultEndpoint.expectedMessageCount(THROTTLE_WINDOW_SIZE);
        resultEndpoint.setResultWaitTime(THROTTLE_WINDOW_DURATION_MS);

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            template.sendBody("seda:a", "<message>" + i + "</message>");
        }

        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testSendLotsOfMessagesWithRejectExecution() throws Exception {
        var excessiveMessages = 4;
        getMockEndpoint("mock:result").expectedMessageCount(THROTTLE_WINDOW_SIZE);
        getMockEndpoint("mock:error").expectedMessageCount(excessiveMessages);

        for (int i = 0; i < THROTTLE_WINDOW_SIZE + excessiveMessages; i++) {
            template.sendBody("direct:start", "<message>" + i + "</message>");
        }

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSendLotsOfMessagesSimultaneouslyAndReceiveWithinExpectedDurationBoundaries() throws Exception {
        sendSimpleMessagesAndAwaitDelivery(MESSAGE_COUNT, THROTTLE_WINDOW_SIZE, THROTTLE_WINDOW_DURATION_MS, "direct:a");
    }

    @Test
    public void testConfigurationWithConstantExpression() throws Exception {
        sendSimpleMessagesAndAwaitDelivery(MESSAGE_COUNT, THROTTLE_WINDOW_PRESET_SIZE, THROTTLE_WINDOW_DURATION_MS,
                "direct:expressionConstant");
    }

    @Test
    public void testConfigurationWithHeaderExpression() throws Exception {
        sendMessagesWithHeaderExpression(MESSAGE_COUNT, 5, THROTTLE_WINDOW_DURATION_MS);
    }

    @Test
    public void testConfigurationWithChangingHeaderExpression() throws Exception {
        sendMessagesWithHeaderExpression(MESSAGE_COUNT, 5, THROTTLE_WINDOW_DURATION_MS);
        sendMessagesWithHeaderExpression(MESSAGE_COUNT, 10, THROTTLE_WINDOW_DURATION_MS);
    }

    private void sendSimpleMessagesAndAwaitDelivery(
            final int messageCount,
            final int throttleWindowSize,
            final int throttleWindowDurationMs,
            final String inputUri)
            throws InterruptedException {
        sendMessagesAndAwaitDelivery(
                messageCount,
                throttleWindowSize,
                throttleWindowDurationMs,
                () -> template.sendBody(inputUri, "<message>payload</message>"));
    }

    private void sendMessagesWithHeaderExpression(
            final int messageCount,
            final int throttleWindowSize,
            final int throttleWindowDurationMs)
            throws InterruptedException {
        sendMessagesAndAwaitDelivery(
                messageCount,
                throttleWindowSize,
                throttleWindowDurationMs,
                () -> template.sendBodyAndHeader(
                        "direct:expressionHeader", "<message>payload</message>",
                        "throttleValue", throttleWindowSize));
    }

    private void sendMessagesAndAwaitDelivery(
            final int messageCount,
            final int throttleWindowSize,
            final int throttleWindowDurationMs,
            final Runnable messageSender)
            throws InterruptedException {
        long minimumDuration = ((messageCount / throttleWindowSize) - 1) * throttleWindowDurationMs;
        long maximumDuration = messageCount * throttleWindowDurationMs;

        ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
        try {
            var resultEndpoint = getMockEndpoint("mock:result");
            resultEndpoint.expectedMessageCount(messageCount);
            resultEndpoint.setResultWaitTime(maximumDuration);

            long start = System.nanoTime();
            IntStream.range(0, messageCount).forEach(idx -> messageSender.run());
            var elapsedTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            log.info(
                    "Sent {} exchanges in {}ms, with throttle rate of {} per {}ms. Calculated min {}ms and max {}ms",
                    messageCount, elapsedTimeMs, throttleWindowSize, throttleWindowDurationMs, minimumDuration,
                    maximumDuration);
            resultEndpoint.assertIsSatisfied();
            resultEndpoint.reset();
            assertTrue(elapsedTimeMs >= minimumDuration,
                    "Should take at least " + minimumDuration + "ms, was: " + elapsedTimeMs);
            assertTrue(elapsedTimeMs <= maximumDuration,
                    "Should take at most " + maximumDuration + "ms, was: " + elapsedTimeMs);
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                onException(ThrottlerRejectedExecutionException.class).handled(true).to("mock:error");

                from("seda:a").throttle(THROTTLE_WINDOW_SIZE).timePeriodMillis(THROTTLE_WINDOW_DURATION_MS).to("log:result",
                        "mock:result");

                from("direct:start").throttle(THROTTLE_WINDOW_SIZE).timePeriodMillis(THROTTLE_WINDOW_DURATION_MS)
                        .rejectExecution(true)
                        .to("log:result", "mock:result");

                from("direct:a").throttle(THROTTLE_WINDOW_SIZE).timePeriodMillis(THROTTLE_WINDOW_DURATION_MS).to("log:result",
                        "mock:result");

                from("direct:expressionConstant").throttle(constant(THROTTLE_WINDOW_PRESET_SIZE))
                        .timePeriodMillis(THROTTLE_WINDOW_DURATION_MS)
                        .to("log:result", "mock:result");

                from("direct:expressionHeader").throttle(header("throttleValue")).timePeriodMillis(THROTTLE_WINDOW_DURATION_MS)
                        .to("log:result", "mock:result");

                from("direct:highThrottleRate").throttle(10000).timePeriodMillis(THROTTLE_WINDOW_DURATION_MS).to("mock:result");
            }
        };
    }
}
