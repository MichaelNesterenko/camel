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
package org.apache.camel.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class MulticastParallelTimeoutStreamCachingTest extends ContextTestSupport {

    private static final String BODY_STRING = "message body";
    private static final byte[] BODY = BODY_STRING.getBytes(StandardCharsets.UTF_8);

    static final int TASK_TIMEOUT_MS = 5_000;
    static final int TASK_DURATION_MS = 30_000;
    static final int TEST_TIMEOUT_MS = 60_000;

    @Test
    public void testCreateOutputStreamCacheAfterTimeout() throws Exception {
        getMockEndpoint("mock:x").expectedBodiesReceived(BODY_STRING);

        template.sendBody("direct:a", "testMessage");
        awaitMocksAreSatisfied();

        Awaitility.await().untilAsserted(() -> {
            File f = testDirectory().toFile();
            assertTrue(f.isDirectory());
            File[] files = f.listFiles();
            assertEquals(0, files.length);
        });
    }

    @Test
    public void testCreateOutputStreamCacheBeforeTimeoutButWriteToOutputStreamCacheAfterTimeout() throws Exception {
        getMockEndpoint("mock:exception").expectedMessageCount(1);
        getMockEndpoint("mock:y").expectedMessageCount(0);

        template.sendBody("direct:b", "testMessage");
        awaitMocksAreSatisfied();
    }

    private void awaitMocksAreSatisfied() throws InterruptedException {
        assertMockEndpointsSatisfied(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        final Processor processor1 = new Processor() {
            public void process(Exchange exchange) {
                try {
                    // sleep so that the stream cache is built after the main exchange has finished due to timeout on the multicast
                    Thread.sleep(TASK_DURATION_MS);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Unexpected exception", e);
                }
                Message in = exchange.getIn();
                // use FilterInputStream to trigger streamcaching
                in.setBody(new FilterInputStream(new ByteArrayInputStream(BODY)) {

                });
            }
        };

        final Processor processor2 = new Processor() {
            public void process(Exchange exchange) throws IOException {
                // create first the OutputStreamCache and then sleep
                CachedOutputStream outputStream = new CachedOutputStream(exchange);
                try {
                    // sleep so that the write to the CachedOutputStream happens after the main exchange has finished due to timeout on the multicast
                    Thread.sleep(TASK_DURATION_MS);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Unexpected exception", e);
                }
                outputStream.write(BODY);
                Message in = exchange.getIn();
                // use FilterInputStream to trigger streamcaching
                in.setBody(outputStream.getInputStream());
            }
        };

        return new RouteBuilder() {
            public void configure() {
                // enable stream caching
                context.getStreamCachingStrategy().setSpoolDirectory(testDirectory().toFile());
                context.getStreamCachingStrategy().setSpoolEnabled(true);
                context.getStreamCachingStrategy().setEnabled(true);
                context.getStreamCachingStrategy().setRemoveSpoolDirectoryWhenStopping(false);
                context.getStreamCachingStrategy().setSpoolThreshold(1L);
                context.setStreamCaching(true);

                onException(IOException.class).to("mock:exception");

                from("direct:a").multicast().timeout(TASK_TIMEOUT_MS).parallelProcessing().to("direct:x");

                from("direct:x").process(processor1).to("mock:x");

                from("direct:b").multicast().timeout(TASK_TIMEOUT_MS).parallelProcessing().to("direct:y");

                from("direct:y").process(processor2).to("mock:y");
            }
        };
    }
}
