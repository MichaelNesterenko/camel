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
package org.apache.camel.component.file;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

public class FileConsumePollEnrichFileIdleEventTest extends ContextTestSupport {

    final CountDownLatch enrichDataLatch = new CountDownLatch(1);

    @Test
    public void testNonEmptyAfterEmpty() throws Exception {
        getMockEndpoint("mock:start").expectedBodiesReceived("Event1", "Event2");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Event1", "EnrichData");
        mock.expectedFileExists(testFile("enrich/.done/Event1.txt"));
        mock.expectedFileExists(testFile("enrich/.done/Event2.txt"));
        mock.expectedFileExists(testFile("enrichdata/.done/AAA.dat"));

        template.sendBodyAndHeader(sfpUri(fileUri("enrich")), "Event1", Exchange.FILE_NAME, "Event1.txt");

        // needs to be with a delay to trigger an empty message
        enrichDataLatch.await(60, TimeUnit.SECONDS);
        template.sendBodyAndHeader(sfpUri(fileUri("enrichdata")), "EnrichData", Exchange.FILE_NAME, "AAA.dat");
        // Trigger second event which should find the EnrichData file
        template.sendBodyAndHeader(sfpUri(fileUri("enrich")), "Event2", Exchange.FILE_NAME, "Event2.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Test
    public void testPollEmptyEnrich() throws Exception {
        getMockEndpoint("mock:start").expectedBodiesReceived("Event1");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Event1");
        mock.expectedFileExists(testFile("enrich/.done/Event1.txt"));

        template.sendBodyAndHeader(sfpUri(fileUri("enrich")), "Event1", Exchange.FILE_NAME, "Event1.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(fileUri("enrich?initialDelay=0&delay=10&move=.done"))
                        .to("mock:start")
                        .pollEnrich(
                                fileUri("enrichdata?initialDelay=0&delay=10&move=.done&sendEmptyMessageWhenIdle=true"), 1000)
                        .process(e -> enrichDataLatch.countDown())
                        .to("mock:result");
            }
        };
    }

}
