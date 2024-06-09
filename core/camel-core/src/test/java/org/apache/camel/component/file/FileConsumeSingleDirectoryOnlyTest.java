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

/**
 * Unit test for consuming the single directory only.
 */
public class FileConsumeSingleDirectoryOnlyTest extends ContextTestSupport {

    @Test
    public void testConsumeFileOnly() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceivedInAnyOrder("Hello World", "Bye World");

        template.sendBodyAndHeader(sfpUri(fileUri("2008")), "2008 Report", Exchange.FILE_NAME, "report2008.txt");
        template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, "report.txt");
        template.sendBodyAndHeader(sfpUri(fileUri()), "Bye World", Exchange.FILE_NAME, "report2.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(fileUri("?recursive=false&delete=true&initialDelay=0&delay=10"))
                        .convertBodyTo(String.class).to("mock:result");
            }
        };
    }
}
