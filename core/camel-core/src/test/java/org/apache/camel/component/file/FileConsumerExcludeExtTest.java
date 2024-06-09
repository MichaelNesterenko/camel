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

public class FileConsumerExcludeExtTest extends ContextTestSupport {

    @Test
    public void testFileConsumer() throws Exception {
        getMockEndpoint("mock:txt").expectedBodiesReceivedInAnyOrder("Hello World", "Bye World");

        template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, "hello.txt");
        template.sendBodyAndHeader(sfpUri(fileUri()), "<customer>123</customer>", Exchange.FILE_NAME, "customer.xml");
        template.sendBodyAndHeader(sfpUri(fileUri()), "123", Exchange.FILE_NAME, "order.dat");
        template.sendBodyAndHeader(sfpUri(fileUri()), "<book>Camel Rocks</book>", Exchange.FILE_NAME, "book.Xml");
        template.sendBodyAndHeader(sfpUri(fileUri()), "Bye World", Exchange.FILE_NAME, "bye.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(fileUri("?initialDelay=0&delay=10&excludeExt=XML,dat"))
                        .to("mock:txt");
            }
        };
    }
}
