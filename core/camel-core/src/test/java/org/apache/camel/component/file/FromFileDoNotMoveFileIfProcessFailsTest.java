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

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

public class FromFileDoNotMoveFileIfProcessFailsTest extends ContextTestSupport {

    private String body = "Hello World this file will NOT be moved";

    @Test
    public void testPollFileAndShouldNotBeMoved() throws Exception {
        template.sendBodyAndHeader(sfpUri(fileUri()), body, Exchange.FILE_NAME, "hello.txt");

        MockEndpoint mock = getMockEndpoint("mock:error");
        // it could potentially retry the file on the 2nd poll and then fail
        // again
        // so it should be minimum message count
        mock.expectedMinimumMessageCount(1);

        mock.assertIsSatisfied();
        oneExchangeDone.matchesWaitTime();

        // assert the file is not moved
        assertFileExists(testFile("hello.txt"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                onException(IllegalArgumentException.class).to("mock:error");

                from(fileUri("?initialDelay=0&delay=10&move=done")).process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        throw new IllegalArgumentException("Forced by unittest");
                    }
                });
            }
        };
    }

}
