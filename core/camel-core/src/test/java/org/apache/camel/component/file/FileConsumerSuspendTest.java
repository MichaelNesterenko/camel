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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.RoutePolicySupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileConsumerSuspendTest extends ContextTestSupport {

    @Test
    public void testConsumeSuspendFile() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBodyAndHeader(sfpUri(fileUri()), "Bye World", Exchange.FILE_NAME, "bye.txt");
        template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
        assertTrue(oneExchangeDone.matchesWaitTime());

        // the route is suspended by the policy so we should only receive one
        try (Stream<Path> list = Files.list(testDirectory())) {
            long files = list.count();
            assertEquals(1, files, "The file should exists");
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                MyPolicy myPolicy = new MyPolicy();
                from(fileUri("?maxMessagesPerPoll=1&delete=true&initialDelay=0&delay=10"))
                        .routePolicy(myPolicy).id("myRoute").convertBodyTo(String.class)
                        .to("mock:result");
            }
        };
    }

    private static class MyPolicy extends RoutePolicySupport {

        private int counter;

        @Override
        public void onExchangeDone(Route route, Exchange exchange) {
            // only stop it at first run
            if (counter++ == 0) {
                try {
                    super.stopConsumer(route.getConsumer());
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }
    }

}
