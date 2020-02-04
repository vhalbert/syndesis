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
package io.syndesis.dv.lsp;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//public class TeiidDdlLanguageServer implements LanguageServer, LanguageClientAware {
public class TeiidDdlLanguageServer implements LanguageServer, LanguageClientAware {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static CompletionOptions DEFAULT_COMPLETION_OPTIONS = new CompletionOptions(Boolean.TRUE, Arrays.asList(".", "@", "#", "*"));

    private final class TeiidDdlLanguageServerRunnable implements Runnable {
        @Override
        public void run() {
          LOGGER.info("Starting Teiid DDL Language Server...");
            while (!shutdown && parentProcessStillRunning() && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                  LOGGER.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
              LOGGER.info("Teiid DDL Language Server - Client vanished...");
            }
        }
    }

  private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlLanguageServer.class);

    public static final String LANGUAGE_ID = "TEIID_DDL_LANGUAGE_ID";

    private Thread runner;
    private volatile boolean shutdown;
    private long parentProcessId;
    private WorkspaceService workspaceService;
    private TeiidDdlTextDocumentService textDocumentService;

    private LanguageClient client;

    public TeiidDdlLanguageServer() {
        this.textDocumentService = new TeiidDdlTextDocumentService(this);
        this.workspaceService = new TeiidDdlWorkspaceService();
        LOGGER.info("TeiidDdlLanguageServer()  doc and workspace services created");
    }


    /**
     * starts the language server process
     *
     * @return  the exit code of the process
     */
    public int startServer() {
        runner = new Thread(new TeiidDdlLanguageServerRunnable(), "Language Client Watcher");
        runner.start();
        LOGGER.info(" >>> TeiidDdlLanguageServer  runner.start() called");
        return 0;
    }

    /**
     * Checks whether the parent process is still running.
     * If not, then we assume it has crashed, and we have to terminate the Camel Language Server.
     *
     * @return true if the parent process is still running
     */
    protected boolean parentProcessStillRunning() {
        // Wait until parent process id is available

        if (parentProcessId == 0) {
            LOGGER.info("Waiting for a client connection...");
        } else {
            LOGGER.info("Checking for client process pid: {}", parentProcessId);
        }

        if (parentProcessId == 0) return true;

        String command;
        if (OS.indexOf("win") != -1) { // && "x86".equals(ARCH)
            command = "cmd /c \"tasklist /FI \"PID eq " + parentProcessId + "\" | findstr " + parentProcessId + "\"";
        } else {
            command = "ps -p " + parentProcessId;
        }
        try {
            Process process = Runtime.getRuntime().exec(command);
            int processResult = process.waitFor();
            return processResult == 0;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return true;
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            return true;
        }
    }

    /**
     * returns the parent process id
     *
     * @return  the parent process id
     */
    protected synchronized long getParentProcessId() {
        return parentProcessId;
    }

    /**
     *
     * @param processId the process id
     */
    protected synchronized void setParentProcessId(long processId) {
        LOGGER.info("Setting client pid to {}", processId);
        parentProcessId = processId;
    }

    /**
     * @return the textDocumentService
     */
    @Override
    public TeiidDdlTextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    /**
     * @return the workspaceService
     */
    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        Integer processId = params.getProcessId();
        if(processId != null) {
            setParentProcessId(processId.longValue());
        } else {
          LOGGER.info("Missing Parent process ID!!");
            setParentProcessId(0);
        }

        ServerCapabilities capabilities = createServerCapabilities();
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
      LOGGER.info("Shutting Teiid DDL down language server");
        shutdown = true;
        return CompletableFuture.completedFuture(new Object());
    }

    @Override
    public void exit() {
        stopServer();
        System.exit(0);
    }

    void stopServer() {
      LOGGER.info("Stopping Teiid DDL language server");
        if (runner != null) {
            runner.interrupt();
        } else {
          LOGGER.info("Request to stop the server has been received but it wasn't started.");
        }
    }

    private ServerCapabilities createServerCapabilities() {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setHoverProvider(Boolean.TRUE);
        capabilities.setDocumentHighlightProvider(Boolean.TRUE);
        capabilities.setDocumentSymbolProvider(Boolean.TRUE);
        // TODO: define capabilities, usually the first provided is completion
        capabilities.setCompletionProvider(DEFAULT_COMPLETION_OPTIONS); // new CompletionOptions(Boolean.TRUE, Arrays.asList(".","?","&", "\"", "=")));
        return capabilities;
    }

    public LanguageClient getClient() {
        return client;
    }
}
