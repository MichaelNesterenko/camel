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
package org.apache.camel.component.file.stress;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileAsyncStressTest extends ContextTestSupport {

    private int files = 150;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < files; i++) {
            template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, i + ".txt");
        }
    }

    @Test
    public void testAsyncStress() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(files);

        // start route when all the files have been written
        context.getRouteController().startRoute("foo");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // leverage the fact that we can limit to max 50 files per poll
                // this will result in polling again and potentially picking up
                // files
                // that already are in progress
                from(fileUri("?maxMessagesPerPoll=50")).routeId("foo").noAutoStartup().threads(10)
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                // simulate some work with random time to complete
                                Random ran = new Random();
                                int delay = ran.nextInt(50) + 10;
                                Thread.sleep(delay);
                            }
                        }).to("mock:result");
            }
        };
    }

}
