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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO semantically equivalent with MulticastParallelStreamingTimeoutTest, needs unification
public class MulticastParallelStreamingTest extends ContextTestSupport {

    @Test
    public void testMulticastParallelStreaming() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("BA");

        template.sendBody("direct:start", "Hello");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Test
    public void testMulticastParallel() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(10);
        mock.whenAnyExchangeReceived(new Processor() {
            public void process(Exchange exchange) throws Exception {
                // they should all be BA as B is faster than A
                assertEquals("BA", exchange.getIn().getBody(String.class));
            }
        });

        for (int i = 0; i < 10; i++) {
            template.sendBody("direct:start", "Hello");
        }

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .process(this::setLatch)
                        .multicast(new AggregationStrategy() {
                            public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                                if (oldExchange == null) {
                                    getLatch(newExchange).countDown();
                                    return newExchange;
                                }

                                String body = oldExchange.getIn().getBody(String.class);
                                oldExchange.getIn().setBody(body + newExchange.getIn().getBody(String.class));
                                return oldExchange;
                            }
                        }).parallelProcessing().streaming().to("direct:a", "direct:b").end()
                        .to("mock:result");

                from("direct:a").process(e -> getLatch(e).await(60, TimeUnit.SECONDS)).setBody(constant("A"));

                from("direct:b").setBody(constant("B"));
            }

            private void setLatch(Exchange exchange) {
                exchange.setProperty("orderingLatch", new CountDownLatch(1));
            }

            private CountDownLatch getLatch(Exchange exchange) {
                return exchange.getProperty("orderingLatch", CountDownLatch.class);
            }
        };
    }
}
