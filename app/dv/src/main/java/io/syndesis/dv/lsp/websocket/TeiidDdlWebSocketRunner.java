/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dv.lsp.websocket;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TeiidDdlWebSocketRunner {


    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlWebSocketRunner.class);

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8025;
    private static final String DEFAULT_CONTEXT_PATH = "/";

    public void runWebSocketServer(String hostname, int port, String contextPath) {
        hostname = hostname != null ? hostname : DEFAULT_HOSTNAME;
        port = port != -1 ? port : DEFAULT_PORT;
        contextPath = contextPath != null ? contextPath : DEFAULT_CONTEXT_PATH;
        Server server = new Server(hostname, port, contextPath, null, TeiidDdlWebSocketServerConfigProvider.class);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "teiid-ddl-lsp-websocket-server-shutdown-hook"));

        try {
            LOGGER.info(" ####################    Teiid DDL LSP Websocket server started at " + hostname + ":" + port );
            server.start();
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.error("Teiid DDL LSP Websocket server has been interrupted.", e);
            Thread.currentThread().interrupt();
        } catch (DeploymentException e) {
            LOGGER.error("Cannot start Teiid DDL LSP Websocket server.", e);
        } finally {
            server.stop();
        }
    }

}