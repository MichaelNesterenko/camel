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

public class FileProducerFileExistTryRenameTest extends ContextTestSupport {

    @Test
    public void testIgnore() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Bye World");
        mock.expectedFileExists(testFile("hello.txt"), "Bye World");

        template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, "hello.txt");
        template.sendBodyAndHeader(sfpUri(fileUri("?fileExist=TryRename&tempPrefix=tmp")), "Bye World",
                Exchange.FILE_NAME, "hello.txt");

        context.getRouteController().startAllRoutes();

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(fileUri("?noop=true&initialDelay=0&delay=10")).noAutoStartup().convertBodyTo(String.class)
                        .to("mock:result");
            }
        };
    }
}
