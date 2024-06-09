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
 * Unit test that file consumer will not match directories (CAMEL-920)
 */
public class FileConsumerDirectoryNotMatchedTest extends ContextTestSupport {

    @Test
    public void testSkipDirectories() throws Exception {
        template.sendBodyAndHeader(sfpUri(fileUri()), "This is a dot file", Exchange.FILE_NAME, ".skipme");
        template.sendBodyAndHeader(sfpUri(fileUri()), "This is a web file", Exchange.FILE_NAME, "index.html");
        template.sendBodyAndHeader(sfpUri(fileUri("2007")), "2007 report", Exchange.FILE_NAME, "report2007.txt");
        template.sendBodyAndHeader(sfpUri(fileUri("2008")), "2008 report", Exchange.FILE_NAME, "report2008.txt");

        getMockEndpoint("mock:result").expectedMessageCount(2);

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(fileUri("?initialDelay=0&delay=10&recursive=true&include=.*txt$"))
                        .convertBodyTo(String.class)
                        .to("mock:result");
            }
        };
    }

}
