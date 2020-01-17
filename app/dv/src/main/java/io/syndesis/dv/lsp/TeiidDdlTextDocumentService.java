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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.completion.DdlCompletionProvider;
import io.syndesis.dv.lsp.diagnostics.DdlDiagnosticsHelper;
import io.syndesis.dv.lsp.hover.HoverProcessor;

/**
 */
public class TeiidDdlTextDocumentService implements TextDocumentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TeiidDdlTextDocumentService.class);
	private Map<String, TextDocumentItem> openedDocuments = new HashMap<>();
	private TeiidDdlLanguageServer teiidLanguageServer;
	private DdlDiagnosticsHelper diagnosticsHelper;
	
    private DdlCompletionProvider completionProvider = new DdlCompletionProvider();

	public TeiidDdlTextDocumentService(TeiidDdlLanguageServer teiidLanguageServer) {
		this.teiidLanguageServer = teiidLanguageServer;
		this.diagnosticsHelper = DdlDiagnosticsHelper.getInstance();
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams completionParams) {
		String uri = completionParams.getTextDocument().getUri();
		LOGGER.info("completion: {}", uri);
        TextDocumentItem doc = openedDocuments.get(uri);
        
        List<CompletionItem> items = completionProvider.getCompletionItems(doc.getText(), completionParams.getPosition());
        
        if( items != null && !items.isEmpty() ) {
        	System.out.print("\n >> TeiidDdlTextDocumentService.completion() found = " + items.size() + " CompletionItems");
        } else {
            CompletionItem nothingItem = new CompletionItem("< NOTHING FOUND >");
            items.add(nothingItem);
        }
        
        for(CompletionItem item: items) {
        	LOGGER.info("  CompletionItem:  label = " + item.getLabel());
        	System.out.println("  CompletionItem:  label = " + item.getLabel());
        }
        //return CompletableFuture.completedFuture(Either.forRight(results));
        return CompletableFuture.completedFuture(Either.forLeft(items));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		LOGGER.info("resolveCompletionItem: {}", unresolved.getLabel());
		return CompletableFuture.completedFuture(unresolved);
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
      LOGGER.info("hover: {}", position.getTextDocument());
//		TextDocumentItem textDocumentItem = openedDocuments.get(position.getTextDocument().getUri());
//		String htmlContent = new HoverProcessor(textDocumentItem).getHover(position.getPosition());
//      Hover hover = new Hover();
//      hover.setContents(Collections.singletonList((Either.forLeft(htmlContent))));
//        LOGGER.info("hover: {}", position.getTextDocument());
//        Hover hover = new Hover();
//        hover.setContents(Collections.singletonList((Either.forLeft("HELLO HOVER WORLD!!!!"))));
        return CompletableFuture.completedFuture(null); //hover);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		LOGGER.info("signatureHelp: {}", position.getTextDocument());
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(TextDocumentPositionParams params) {
		TextDocumentIdentifier textDocument = params.getTextDocument();
		LOGGER.info("definition: {}", textDocument);
		return CompletableFuture.completedFuture(null);
	}

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		LOGGER.info("documentHighlight: {}", position.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
		// TODO Auto-generated method stub
		return TextDocumentService.super.documentColor(params);
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
		LOGGER.info("documentSymbol: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		LOGGER.info("codeAction: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		LOGGER.info("codeLens: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		LOGGER.info("resolveCodeLens: {}", unresolved.getCommand().getCommand());
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		LOGGER.info("formatting: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		LOGGER.info("rangeFormatting: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		LOGGER.info("onTypeFormatting: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		LOGGER.info("rename: {}", params.getTextDocument());
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocumentItem textDocument = params.getTextDocument();
		LOGGER.info("didOpen: {}", textDocument);
		System.out.println("  ###  TeiidDdlTextDocumentService:  didOpen: {}" );
		openedDocuments.put(textDocument.getUri(), textDocument);
		diagnosticsHelper.compileAndSendDiagnostics();
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		LOGGER.info("didChange: {}", params.getTextDocument());
		System.out.println("  ###  TeiidDdlTextDocumentService:  didChange: {}" );
		List<TextDocumentContentChangeEvent> contentChanges = params.getContentChanges();
		TextDocumentItem textDocumentItem = openedDocuments.get(params.getTextDocument().getUri());
		if (!contentChanges.isEmpty()) {
			textDocumentItem.setText(contentChanges.get(0).getText());
			diagnosticsHelper.compileAndSendDiagnostics();
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		LOGGER.info("didClose: {}", params.getTextDocument());
		String uri = params.getTextDocument().getUri();
		openedDocuments.remove(uri);
		/* The rule observed by VS Code servers as explained in LSP specification is to clear the Diagnostic when it is related to a single file.
		 * https://microsoft.github.io/language-server-protocol/specification#textDocument_publishDiagnostics
		 * */
		diagnosticsHelper.compileAndSendDiagnostics();
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		LOGGER.info("didSave: {}", params.getTextDocument());
//		new DiagnosticService(camelCatalog, teiidLanguageServer).compute(params);
	}

	public TextDocumentItem getOpenedDocument(String uri) {
		return openedDocuments.get(uri);
	}
	
	public Collection<TextDocumentItem> getAllOpenedDocuments() {
		return openedDocuments.values();
	}
}
