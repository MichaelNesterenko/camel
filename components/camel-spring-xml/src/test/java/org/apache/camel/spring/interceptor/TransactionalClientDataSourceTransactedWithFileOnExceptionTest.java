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
package org.apache.camel.spring.interceptor;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.SpringRouteBuilder;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionalClientDataSourceTransactedWithFileOnExceptionTest extends TransactionClientDataSourceSupport {

    @Test
    public void testTransactionSuccess() throws Exception {
        template.sendBodyAndHeader(sfpUri(fileUri("okay")), "Hello World", Exchange.FILE_NAME, "okay.txt");

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            // wait for route to complete
            int count = jdbc.queryForObject("select count(*) from books", Integer.class);
            assertEquals(3, count, "Number of books");
        });
    }

    @Test
    public void testTransactionRollback() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);
        error.message(0).header(Exchange.EXCEPTION_CAUGHT).isNotNull();
        error.message(0).header(Exchange.EXCEPTION_CAUGHT).isInstanceOf(IllegalArgumentException.class);
        error.expectedFileExists(testFile("failed/fail.txt"));

        template.sendBodyAndHeader(sfpUri(fileUri("fail")), "Hello World", Exchange.FILE_NAME, "fail.txt");

        // wait for route to complete
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            // should not be able to process the file so we still got 1 book as we did from the start
            int count = jdbc.queryForObject("select count(*) from books", Integer.class);
            assertEquals(1, count, "Number of books");
        });

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new SpringRouteBuilder() {
            public void configure() throws Exception {
                onException(IllegalArgumentException.class).handled(false).to("mock:error");

                from(fileUri("okay?initialDelay=0&delay=10"))
                        .transacted()
                        .setBody(constant("Tiger in Action")).bean("bookService")
                        .setBody(constant("Elephant in Action")).bean("bookService");

                from(fileUri("fail?initialDelay=0&delay=10&moveFailed=../failed"))
                        .transacted()
                        .setBody(constant("Tiger in Action")).bean("bookService")
                        .setBody(constant("Donkey in Action")).bean("bookService");
            }
        };
    }

}
