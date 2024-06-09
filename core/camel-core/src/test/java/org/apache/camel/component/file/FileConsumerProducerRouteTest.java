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

public class FileConsumerProducerRouteTest extends ContextTestSupport {

    @Test
    public void testFileRoute() throws Exception {
        MockEndpoint result = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
        result.expectedMessageCount(2);

        template.sendBodyAndHeader(sfpUri(fileUri("a")), "Hello World", Exchange.FILE_NAME, "hello.txt");
        template.sendBodyAndHeader(sfpUri(fileUri("a")), "Bye World", Exchange.FILE_NAME, "bye.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(fileUri("a?initialDelay=0&delay=10")).to(fileUri("b"));
                from(fileUri("b?initialDelay=0&delay=10")).to("mock:result");
            }
        };
    }
}
