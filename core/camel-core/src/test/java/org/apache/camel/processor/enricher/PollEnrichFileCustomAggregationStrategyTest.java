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
package org.apache.camel.processor.enricher;

import java.util.concurrent.TimeUnit;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

public class PollEnrichFileCustomAggregationStrategyTest extends ContextTestSupport {

    final int ENRICH_TIMEOUT = 30_000;

    @Test
    public void testPollEnrichCustomAggregationStrategyBody() throws Exception {
        getMockEndpoint("mock:start").expectedBodiesReceived("Start");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Big file");
        mock.expectedFileExists(testFile("enrich/.done/AAA.fin"));
        mock.expectedFileExists(testFile("enrichdata/.done/AAA.dat"));

        template.sendBodyAndHeader(sfpUri(fileUri("enrich")), "Start", Exchange.FILE_NAME, "AAA.fin");
        template.sendBodyAndHeader(sfpUri(fileUri("enrichdata")), "Big file", Exchange.FILE_NAME, "AAA.dat");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
        assertFileNotExists(testFile("enrichdata/AAA.dat.camelLock"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(fileUri("enrich?initialDelay=0&delay=10&move=.done"))
                        .to("mock:start")
                        .pollEnrich(
                                fileUri("enrichdata?initialDelay=0&delay=10&readLock=markerFile&move=.done"),
                                ENRICH_TIMEOUT, new ReplaceAggregationStrategy())
                        .to("mock:result");
            }
        };
    }

    static class ReplaceAggregationStrategy implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange original, Exchange resource) {
            Object resourceResponse = resource.getIn().getBody();
            if (original.getPattern().isOutCapable()) {
                original.getMessage().setBody(resourceResponse);
                original.getProperties().putAll(resource.getProperties());
            } else {
                original.getIn().setBody(resourceResponse);
                original.getProperties().putAll(resource.getProperties());
            }
            return original;
        }
    }
}
