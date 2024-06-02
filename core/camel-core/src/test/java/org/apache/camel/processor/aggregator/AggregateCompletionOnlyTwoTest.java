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
package org.apache.camel.processor.aggregator;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.BodyInAggregatingStrategy;
import org.apache.camel.processor.aggregate.MemoryAggregationRepository;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregateCompletionOnlyTwoTest extends ContextTestSupport {

    private final MyRepo repo = new MyRepo();

    @Test
    void testOnlyTwo() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:aggregated");
        mock.expectedBodiesReceived("A+B", "C+END");

        template.sendBodyAndHeader("direct:start", "A", "id", "foo");
        template.sendBodyAndHeader("direct:start", "B", "id", "foo");
        template.sendBodyAndHeader("direct:start", "C", "id", "foo");
        template.sendBodyAndHeader("direct:start", "END", "id", "foo");

        assertMockEndpointsSatisfied();

        assertEquals(4, repo.getGet());
        assertEquals(2, repo.getAdd());
        assertEquals(2, repo.getRemove());
        // A second thread is involved so let's use awaitility to add more flexibility to the test
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(2, repo.getConfirm()));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").aggregate(header("id"), new BodyInAggregatingStrategy()).aggregationRepository(repo)
                        .completionSize(2).to("mock:aggregated");
            }
        };
    }

    private static class MyRepo extends MemoryAggregationRepository {

        private AtomicInteger add = new AtomicInteger();
        private AtomicInteger get = new AtomicInteger();
        private AtomicInteger remove = new AtomicInteger();
        private AtomicInteger confirm = new AtomicInteger();

        @Override
        public Exchange add(CamelContext camelContext, String key, Exchange exchange) {
            add.incrementAndGet();
            return super.add(camelContext, key, exchange);
        }

        @Override
        public Exchange get(CamelContext camelContext, String key) {
            get.incrementAndGet();
            return super.get(camelContext, key);
        }

        @Override
        public void remove(CamelContext camelContext, String key, Exchange exchange) {
            remove.incrementAndGet();
            super.remove(camelContext, key, exchange);
        }

        @Override
        public void confirm(CamelContext camelContext, String exchangeId) {
            confirm.incrementAndGet();
            super.confirm(camelContext, exchangeId);
        }

        @Override
        public Set<String> getKeys() {
            return super.getKeys();
        }

        public int getAdd() {
            return add.get();
        }

        public int getGet() {
            return get.get();
        }

        public int getRemove() {
            return remove.get();
        }

        public int getConfirm() {
            return confirm.get();
        }
    }
}
