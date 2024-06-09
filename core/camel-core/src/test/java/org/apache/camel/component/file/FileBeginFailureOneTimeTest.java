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

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.strategy.GenericFileDeleteProcessStrategy;
import org.apache.camel.spi.Registry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBeginFailureOneTimeTest extends ContextTestSupport {

    private MyStrategy myStrategy = new MyStrategy();

    @Override
    protected Registry createCamelRegistry() throws Exception {
        Registry jndi = super.createCamelRegistry();
        jndi.bind("myStrategy", myStrategy);
        return jndi;
    }

    @Test
    public void testBeginFailureOneTime() throws Exception {
        var mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBodyAndHeader(sfpUri(fileUri()), "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
        assertEquals(2, myStrategy.getInvoked(), "Begin should have been invoked 2 times");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(fileUri("?initialDelay=0&delay=10&processStrategy=#myStrategy")).convertBodyTo(String.class)
                        .to("mock:result");
            }
        };
    }

    private static class MyStrategy extends GenericFileDeleteProcessStrategy<File> {

        private volatile int invoked;

        @Override
        public boolean begin(
                GenericFileOperations<File> fileGenericFileOperations, GenericFileEndpoint<File> fileGenericFileEndpoint,
                Exchange exchange,
                GenericFile<File> fileGenericFile)
                throws Exception {
            if (++invoked <= 1) {
                throw new IllegalArgumentException("Damn I cannot do this");
            }
            return super.begin(fileGenericFileOperations, fileGenericFileEndpoint, exchange, fileGenericFile);
        }

        public int getInvoked() {
            return invoked;
        }
    }

}
