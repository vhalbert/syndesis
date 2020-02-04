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

import java.util.Collection;

import org.eclipse.lsp4j.jsonrpc.Launcher.Builder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.websocket.WebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.TeiidDdlLanguageServer;


public class TeiidDdlWebSocketEndpoint extends WebSocketEndpoint<LanguageClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlWebSocketEndpoint.class);

    @Override
    protected void configure(Builder<LanguageClient> builder) {
        builder.setLocalService(new TeiidDdlLanguageServer());
        builder.setRemoteInterface(LanguageClient.class);
    }

    @Override
    protected void connect(Collection<Object> localServices, LanguageClient remoteProxy) {
    	LOGGER.info(" #############  TeiidDdlWebSocketEndpoint.connect() ########" );
        localServices.stream()
            .filter(LanguageClientAware.class::isInstance)
            .forEach(languageClientAware -> ((LanguageClientAware) languageClientAware).connect(remoteProxy));
    }

}
