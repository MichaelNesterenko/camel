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
package org.apache.camel.component.optaplanner;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;

public class OptaPlannerConsumer extends DefaultConsumer {
    private final OptaPlannerEndpoint endpoint;
    private final OptaPlannerConfiguration configuration;
    private OptaplannerSolutionEventListener solverJobListener;

    public OptaPlannerConsumer(OptaPlannerEndpoint endpoint, Processor processor, OptaPlannerConfiguration configuration) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.configuration = configuration;
        this.solverJobListener = this::processSolverJobEvent;
    }

    public void processSolverJobEvent(OptaplannerSolutionEvent event) {
        Exchange exchange = createExchange(true);
        exchange.getMessage().setHeader(OptaPlannerConstants.BEST_SOLUTION, event.getBestSolution());
        try {
            getProcessor().process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        }
        if (exchange.getException() != null) {
            getExceptionHandler().handleException(exchange.getException());
        }
    }

    @Override
    protected void doStart() throws Exception {
        final Long problemId = configuration.getProblemId();
        endpoint.addSolutionEventListener(problemId, solverJobListener);

        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        final Long problemId = configuration.getProblemId();
        endpoint.removeSolutionEventListener(problemId, solverJobListener);

        super.doStop();
    }
}
