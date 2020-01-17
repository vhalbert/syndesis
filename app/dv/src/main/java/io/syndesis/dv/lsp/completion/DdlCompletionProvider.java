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
package io.syndesis.dv.lsp.completion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

public class DdlCompletionProvider implements DdlCompletionConstants {
	private List<CompletionItem> allNonDatatypeItems = null;
	private boolean doPrintToConsole = false;
	
	public List<CompletionItem> getCompletionItems(String statement, Position position) {
		List<CompletionItem> items = new ArrayList<CompletionItem>();
		
		try {
			DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(statement);
			
			CreateViewStatement createStatement = new CreateViewStatement(analyzer);
			
			TokenContext tokenContext = createStatement.getTokenContext(position);
			System.out.println("Found token context:\n >>  " + tokenContext);
			Token previousToken = tokenContext.getToken(); //analyzer.getTokenFor(position);
			
			String[] words = null;

			if( previousToken != null ) {
				systemPrint(" getCompletionItems() for Token = " + previousToken.image);
				
				switch(previousToken.kind) {
					case CREATE:
						words = analyzer.getNextWordsByKind(CREATE);
						systemPrint("\n >>> Token = CREATE found " + words.length + " words\n");
						items.addAll(generateCompletionItems(words));
						break;
						
//					case VIRTUAL:
//						words = helper.getAnalyzer().getNextWordsByKind(VIRTUAL);
//						systemPrint("\n >>> Token = VIRTUAL found " + words.length + " words\n");
//						items.addAll(generateCompletionItems(words));
//						break;
						
					case VIEW:
						words = analyzer.getNextWordsByKind(VIEW);
						systemPrint("\n >>> Token = VIEW found " + words.length + " words\n");
						items.addAll(SampleTableItemInfo.getSampleTableItems());
						break;
						
					case SELECT:
						words = analyzer.getNextWordsByKind(SELECT);
						systemPrint("\n >>> Token = SELECT found " + words.length + " words\n");
						items.addAll(generateCompletionItems(words));
						break;
					case ID:
						// An ID represents any variable representing a view or source name
						// i.e. table name, column name, etc....
						// If an ID is found, then we'll need to identify the context of the ID
						// Basically where in the statement does it exist
						switch(tokenContext.getContext() ) {
							case PREFIX: {
								items.add(createKeywordItem(DdlCompletionConstants.getLabel(LPAREN),
										DdlCompletionConstants.getLabel(LPAREN) + "\n) ", null, null));
							} break;
							case TABLE_ELEMENT: {
								items.addAll(generateCompletionItems(analyzer.getKeywordLabels(DATATYPES)));
							} break;
							case QUERY_EXPRESSION: {
								
							} break;
							case NONE_FOUND:
								break;
							case TABLE_BODY:
								break;
							case TABLE_ELEMENT_OPTIONS:
								break;
							case TABLE_OPTIONS:
								break;
							default:
								break;
							
						}
						break;
		
					default: // RETURN ALL KEYWORDS
						if( allNonDatatypeItems == null ) {
							allNonDatatypeItems = generateCompletionItems(analyzer.getKeywordLabels(NON_DATATYPE_KEYWORDS));
						}
						items = allNonDatatypeItems;
				}
			} else {
				words = analyzer.getNextWordsByKind(EMPTY_DDL);
				systemPrint("\n >>> Token = NULL found " + words.length + " words\n");
				CompletionItem createItem = 
						createKeywordItem(DdlCompletionConstants.getLabel(CREATE).toUpperCase(),
								DdlCompletionConstants.getLabel(CREATE).toUpperCase() + " ", null, null);
				createItem.setPreselect(true);
				items.add(createItem);		
				items.add(getCreateCreateViewCompletionItem(1));
				items.add(getCreateViewCompletionItem(2));			
			}
		} catch (Exception e) {
			System.out.print("\n TeiidDdlCompletionProvider.getCompletionItems() DID NOT FIND CompltionItems");
		}
		systemPrint("\n CompletionItems = " + items.size() + "\n");
		return items;
	}
	
	private void systemPrint(String str) {
		if( doPrintToConsole ) System.out.print(str);
	}

    public CompletionItem getCreateCreateViewCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW");
        ci.setInsertText(
                "CREATE VIEW ");
        ci.setKind(CompletionItemKind.Keyword);
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }
    
    public CompletionItem getCreateViewCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW...");
        ci.setInsertText(
                "CREATE VIEW ${1:name} (\n) AS SELECT * FROM ${2:name};");
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" Create View statement including ....");
        ci.setDocumentation(beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    public CompletionItem getColumnCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("column definition");
        ci.setInsertText("\\t${1:name} ${2:type}");
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" insert new column definition ....");
        ci.setDocumentation(beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    private static Either<String, MarkupContent> beautifyDocument(String raw) {
        // remove the placeholder for the plain cursor like: ${0}, ${1:variable}
        String escapedString = raw.replaceAll("\\$\\{\\d:?(.*?)\\}", "$1");

        MarkupContent markupContent = new MarkupContent();
        markupContent.setKind(MarkupKind.MARKDOWN);
        markupContent.setValue(
                String.format("```%s\n%s\n```", "java", escapedString));
        return Either.forRight(markupContent);
    }

    public CompletionItem createKeywordItem(String label, String text,
            String detail, String documentation) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Keyword);
        if (detail != null) {
            ci.setDetail(detail);
        }
        if (documentation != null) {
            ci.setDocumentation(documentation);
        }
        return ci;
    }

    public CompletionItem createKeywordItem(String label) {
    	String[] itemData = getItemData(label);
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Keyword);

        String detail = itemData[1];
        if (detail != null) {
            ci.setDetail(detail);
        }

        String documentation = itemData[2];
        if (documentation != null) {
            ci.setDocumentation(documentation);
        }

        String insertText = itemData[3];
        if (insertText != null) {
            ci.setInsertText(insertText);
        }

        return ci;
    }

    public CompletionItem createFieldItem(String label, String detail,
            String documentation) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Field);
        if (detail != null) {
            ci.setDetail(detail);
        }
        if (documentation != null) {
            ci.setDocumentation(documentation);
        }
        return ci;
    }

    public CompletionItem createSnippetItem(String label, String detail,
            String documentation, String insertText) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setInsertText(insertText);
        if (documentation != null) {
            ci.setDocumentation(documentation);
        } else {
            ci.setDocumentation(beautifyDocument(ci.getInsertText()));
        }
        if (detail != null) {
            ci.setDetail(detail);
        }

        return ci;
    }
    
    private List<CompletionItem> generateCompletionItems(String[] words) {
    	List<CompletionItem> items = new ArrayList<CompletionItem>(); 
    	
		for(String word: words ) {
			items.add(createKeywordItem(word, word, null, null));
		}
		
		return items;
    }
    
	/**
	 * 
	 * @param label
	 * @return	String[] array >>>>  
		String[0] label;
		String[1] detail;
		String[2] documentation;
		String[3] insertText;
		String[4] insertTextFormat;
	 */
	public String[] getItemData(String label) {
		String[] result = KEYWORDS_ITEM_DATA.get(label.toUpperCase());
		
		if( result == null ) {
			result = DATATYPES_ITEM_DATA.get(label);
		}
		
		return result;
	}
}
