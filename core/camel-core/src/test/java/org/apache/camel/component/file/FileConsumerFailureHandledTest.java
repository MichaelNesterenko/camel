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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for consuming files but the exchange fails and is handled by the failure handler (usually the
 * DeadLetterChannel)
 */
public class FileConsumerFailureHandledTest extends ContextTestSupport {

    @Test
    public void testParis() throws Exception {
        getMockEndpoint("mock:valid").expectedBodiesReceived("Hello Paris");

        template.sendBodyAndHeader(sfpUri(fileUri()), "Paris", Exchange.FILE_NAME, "paris.txt");

        assertResult("paris.txt", true);
    }

    @Test
    public void testLondon() throws Exception {
        getMockEndpoint("mock:invalid").expectedBodiesReceived("London");

        template.sendBodyAndHeader(sfpUri(fileUri()), "London", Exchange.FILE_NAME, "london.txt");

        assertResult("london.txt", true);
    }

    @Test
    public void testDublin() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:beer");
        // we get the original input so its not Hello London but only London
        mock.allMessages().body().isEqualTo("Dublin");
        mock.setMinimumExpectedMessageCount(1);

        template.sendBodyAndHeader(sfpUri(fileUri()), "Dublin", Exchange.FILE_NAME, "dublin.txt");

        assertResult("dublin.txt", false);
    }

    @Test
    public void testMadrid() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:error");
        // we get the original input so its not Hello London but only London
        mock.expectedBodiesReceived("Madrid");

        template.sendBodyAndHeader(sfpUri(fileUri()), "Madrid", Exchange.FILE_NAME, "madrid.txt");

        assertResult("madrid.txt", true);
    }

    private void assertResult(String fileName, boolean isDeleted) throws InterruptedException {
        assertMockEndpointsSatisfied(60, TimeUnit.SECONDS);
        assertTrue(oneExchangeDone.matchesWaitTime());
        assertFiles(fileName, isDeleted);
    }

    private void assertFiles(String filename, boolean deleted) throws InterruptedException {
        // file should be deleted as delete=true in parameter in the route below
        Path file = testFile(filename);
        assertEquals(deleted, !Files.exists(file), "File " + filename + " should be deleted: " + deleted);

        // and no lock files
        String lock = filename + FileComponent.DEFAULT_LOCK_FILE_POSTFIX;
        file = testFile(lock);
        assertFalse(Files.exists(file), "File " + lock + " should be deleted");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                // make sure mock:error is the dead letter channel
                // use no delay for fast unit testing
                errorHandler(deadLetterChannel("mock:error").maximumRedeliveries(2).redeliveryDelay(0).logStackTrace(false));

                // special for not handled when we got beer
                onException(ValidationException.class).onWhen(exceptionMessage().contains("beer")).handled(false)
                        .to("mock:beer");

                // special failure handler for ValidationException
                onException(ValidationException.class).handled(true).to("mock:invalid");

                // our route logic to process files from the input folder
                from(fileUri("?initialDelay=0&delay=10&delete=true")).process(new MyValidatorProcessor())
                        .to("mock:valid");
            }
        };
    }

    private static class MyValidatorProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            String body = exchange.getIn().getBody(String.class);
            if ("London".equals(body)) {
                throw new ValidationException(exchange, "Forced exception by unit test");
            } else if ("Madrid".equals(body)) {
                throw new RuntimeCamelException("Madrid is not a supported city");
            } else if ("Dublin".equals(body)) {
                throw new ValidationException(exchange, "Dublin have good beer");
            }
            exchange.getMessage().setBody("Hello " + body);
        }
    }

}
