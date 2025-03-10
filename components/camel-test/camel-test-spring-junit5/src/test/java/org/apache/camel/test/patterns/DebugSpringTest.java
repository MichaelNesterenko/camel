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
package org.apache.camel.test.patterns;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.test.junit5.DebugBreakpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DebugSpringTest extends CamelSpringTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DebugSpringTest.class);
    private boolean debugged;

    @Override
    public boolean isUseDebugger() {
        // must enable debugger
        return true;
    }

    protected DebugBreakpoint createBreakpoint() {
        return new DebugBreakpoint() {
            @Override
            protected void debugBefore(
                    Exchange exchange, Processor processor, ProcessorDefinition<?> definition, String id, String label) {
                // this method is invoked before we are about to enter the given processor
                // from your Java editor you can add a breakpoint in the code line below
                LOG.info("Before {} with body {}", definition, exchange.getIn().getBody());
                debugged = true;
            }

            @Override
            protected void debugAfter(
                    Exchange exchange, Processor processor, ProcessorDefinition<?> definition, String id, String label,
                    long timeTaken) {

            }
        };
    }

    @Test
    public void testDebugger() throws Exception {
        // set mock expectations
        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:b").expectedMessageCount(1);

        // send a message
        template.sendBody("direct:start", "World");

        // assert mocks
        MockEndpoint.assertIsSatisfied(context);
        assertTrue(debugged, "The debugger is not called!");
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // this is the route we want to debug
                from("direct:start")
                        .to("mock:a")
                        .transform(body().prepend("Hello "))
                        .to("mock:b");
            }
        };
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new GenericApplicationContext();
    }

}
