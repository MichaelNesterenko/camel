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
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("This test is regularly flaky")
public class FileProducerRenameUsingCopyTest extends ContextTestSupport {

    @Test
    public void testMove() throws Exception {
        final String body = "Hello Camel";
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBodyAndHeader(sfpUri(fileUri()), body, Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
        assertTrue(oneExchangeDone.matchesWaitTime());

        assertFileExists(testFile("done/hello.txt"), body);
        assertFileNotExists(testFile("hello.txt"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(fileUri("?renameUsingCopy=true&move=done")).convertBodyTo(String.class).to("mock:result");
            }
        };
    }
}
