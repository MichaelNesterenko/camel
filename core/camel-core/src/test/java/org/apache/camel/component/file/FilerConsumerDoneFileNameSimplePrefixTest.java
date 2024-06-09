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

import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for writing done files
 */
public class FilerConsumerDoneFileNameSimplePrefixTest extends ContextTestSupport {

    @Test
    public void testDoneFile() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(0);

        template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);

        resetMocks();
        oneExchangeDone.reset();
        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        // write the done file
        template.sendBodyAndHeader(sfpUri(fileUri()), "", Exchange.FILE_NAME, "done-hello.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
        assertTrue(oneExchangeDone.matchesWaitTime());

        // done file should be deleted now
        assertFileNotExists(testFile("done-hello.txt"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // using $simple{ to avoid clash with spring property
                // placeholder
                from(fileUri("?doneFileName=done-$simple{file:name}&initialDelay=0&delay=10")).to("mock:result");
            }
        };
    }

}
