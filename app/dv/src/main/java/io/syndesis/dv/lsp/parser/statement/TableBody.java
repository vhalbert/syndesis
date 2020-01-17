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
package io.syndesis.dv.lsp.parser.statement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class TableBody extends AbstractStatementObject {
	private Token[] tableBodyTokens;
	private TableElement[] elements;
	private OptionsClause options;
	
	private List<DdlAnalyzerException> exceptions = new ArrayList<DdlAnalyzerException>();
	
	public TableBody(DdlTokenAnalyzer analyzer) {
		super(analyzer);
	}

	public TableElement[] getElements() {
		return elements;
	}

	public void setElements(TableElement[] elements) {
		this.elements = elements;
	}

	public OptionsClause getOptions() {
		return options;
	}

	public void setOptions(OptionsClause options) {
		this.options = options;
	}
	
	@Override
	protected void parseAndValidate() {
        try {
            tableBodyTokens = getBracketedTokens(getTokens(), 3, LPAREN, RPAREN);
            
            setFirstTknIndex(getTokenIndex(tableBodyTokens[0]));
            setLastTknIndex(getTokenIndex(tableBodyTokens[tableBodyTokens.length-1]));
            
            // Process table body (i.e. columns definition)
            processTableBodyTokens(tableBodyTokens);
            
    		// Parse Table Options
    		
            int iTkn = 3 + tableBodyTokens.length;
            Token nextTkn = getTokens()[iTkn];
            
    		if( nextTkn.kind == OPTIONS) { 
                List<Token> optionsTkns = new ArrayList<Token>();
                optionsTkns.add(nextTkn);
                
                // Check for parens in case of string(), decimal() types.. etc
                Token lastTkn = nextTkn;
                if( isNextTokenOfKind(getTokens(), iTkn, LPAREN) ) {
                    Token[] bracketedTkns = getBracketedTokens(getTokens(), iTkn+1, LPAREN, RPAREN);
                    if( bracketedTkns.length > 0 ) {     	
                        for(Token optionsTkn: bracketedTkns) {
                        	iTkn++;
                        	optionsTkns.add(optionsTkn);
                        	lastTkn = optionsTkn;
                        }
                    }
                }
                
                if( optionsTkns.isEmpty() ) {
                    OptionsClause options = new OptionsClause(analyzer);
                    options.setOptionsTokens(optionsTkns.toArray(new Token[0]));
                    setOptions(options);
                    setFirstTknIndex(getTokenIndex(nextTkn));
                    setLastTknIndex(getTokenIndex(lastTkn));
                }
                
        	}
      
        } catch (DdlAnalyzerException e) {
        	exceptions.add(e);
            System.out.println(e.getMessage());
        }
	}
	
    public Token[] getBracketedTokens(Token[] tkns, int startTokenId, int bracketStartKind, int bracketEndKind) throws DdlAnalyzerException {
        int numUnmatchedParens = 0;
        
        for(int iTkn = 0; iTkn<getTokens().length; iTkn++) {
            if( iTkn < startTokenId) continue;
            Token token = tkns[iTkn];
            if( token.kind == bracketStartKind) numUnmatchedParens++;
            if( token.kind == bracketEndKind) numUnmatchedParens--;
            
            if( numUnmatchedParens == 0) {
                List<Token> bracketedTokens = new ArrayList<Token>(tkns.length);
            	bracketedTokens.add(tkns[startTokenId]);
                for(int jTkn = startTokenId+1; jTkn < iTkn; jTkn++) {
                    bracketedTokens.add(tkns[jTkn]);
                }
                bracketedTokens.add(tkns[startTokenId + bracketedTokens.size()]);
                return bracketedTokens.toArray(new Token[0]);
            }
        }
        
        Token startTkn = tkns[startTokenId];
        throw new DdlAnalyzerException(
                "Brackets do not match for bracket type '" + startTkn.image +
                "' at position (" + startTkn.beginLine + ", " + startTkn.beginColumn + ")");
    }
    
    /**
     * This method returns an array list of token arrays representing column definitions
     * 
     *   Example:  e1 integer primary key, e2 varchar(10) unique, e3 date not null unique" +
     * 
     * @param tkns
     * @return
     */
    private void processTableBodyTokens(Token[] tkns) throws DdlAnalyzerException {
        List<TableElement> tblElementList = new ArrayList<TableElement>();
        
        // EXAMPLE:  e1 string(255) NOT NULL primary key,
        // EXAMPLE:  e1 decimal(10, 2) AUTO_INCREMENT unique,
        // EXAMPLE:  e6 varchar index default 'hello'

        // 1) walk through each table element definition and create an array of tokens

        int count = 0;
        TableElement tableElement = null;
        
        for(int iTkn = 0; iTkn<tkns.length; iTkn++) {
            Token tkn = tkns[iTkn];
            
            if( tableElement == null ) {
            	tableElement = new TableElement(super.analyzer);
            }
            
            switch(count) {
	            case 0: {
	            	if( tkn.kind != LPAREN ) {
	            		exceptions.add(
	                			new DdlAnalyzerException("Table body missing left parens")); //$NON-NLS-1$ 
	            	}
	            	tableElement.setFirstTknIndex(getTokenIndex(tkn));
	            } break;
	            
	            case 1: {
	                // first token should be kind == ID
	                if( tkn.kind == ID ) {
	                	tableElement.setNameToken(tkn);
	                	if( tableElement.getFirstTknIndex() > 0) {
	                		tableElement.setFirstTknIndex(getTokenIndex(tkn));
	                	}
	                } else {
	                	exceptions.add(
	                			new DdlAnalyzerException("column name + '" + tkn.image + "' is invalid at: " + getBeginPosition(tkn))); //$NON-NLS-1$ //$NON-NLS-2$ 
	                }
	            } break;
	            
	            case 2: {
	                // second token should be datatype kind
	                if( isDatatype(tkn) ) {
	                    tableElement.setDatatypeKind(tkn.kind);
	                    List<Token> dTypeTkns = new ArrayList<Token>();
	                    dTypeTkns.add(tkn);
	                    
	                    // Check for parens in case of string(), decimal() types.. etc
	                    if( isNextTokenOfKind(tkns, iTkn, LPAREN) ) {
		                    Token[] bracketedTkns = getBracketedTokens(tkns, iTkn+1, LPAREN, RPAREN);
		                    if( bracketedTkns.length > 0 ) {     	
		                    	
		                    	
		                        for(Token dTypeTkn: bracketedTkns) {
		                        	iTkn++;
		                        	dTypeTkns.add(dTypeTkn);
		                        }
		                    }
	                    }
	                    
	                    tableElement.setDatatypeTokens(dTypeTkns.toArray(new Token[0]));
	                } else {
	                	exceptions.add(new DdlAnalyzerException("invalid datatype + '" + tkn.image + "' at: " + getBeginPosition(tkn))); //$NON-NLS-1$ //$NON-NLS-2$ 
	                }
	            } break;
	            
	            default: {
	            	if( tkn.kind == NOT) {
	            		// Check for NULL
	            		if( hasAnotherToken(tkns, iTkn)) {
	            			List<Token> tmpTkns = new ArrayList<Token>();
	            			tmpTkns.add(tkn);
	            			iTkn++;
	            			tkn = tkns[iTkn];
	            			if( tkn.kind == NULL) {
	            				tmpTkns.add(tkn);
	            				tableElement.setNotNullTokens(tmpTkns.toArray(new Token[0]));
	            			}
	            		}
	            	} else if( tkn.kind == AUTO_INCREMENT) {
	            		tableElement.setAutoIncrementToken(tkn);
	            	} else if( tkn.kind == PRIMARY ) {
	            		// Check for NULL
	            		if( hasAnotherToken(tkns, iTkn)) {
	            			List<Token> tmpTkns = new ArrayList<Token>();
	            			tmpTkns.add(tkn);
	            			iTkn++;
	            			tkn = tkns[iTkn];
	            			if( tkn.kind == KEY) {
	            				tmpTkns.add(tkn);
	            				tableElement.setPrimaryKeyTokens(tmpTkns.toArray(new Token[0]));
	            			}
	            		}
	            	} else if( tkn.kind == DEFAULT_KEYWORD ) {
	            		// Check for NULL
	            		if( hasAnotherToken(tkns, iTkn)) {
	            			List<Token> tmpTkns = new ArrayList<Token>();
	            			tmpTkns.add(tkn);
	            			iTkn++;
	            			tkn = tkns[iTkn];
	            			if( tkn.kind == STRINGVAL ) {
	            				tmpTkns.add(tkn);
	            				tableElement.setDefaultTokens(tmpTkns.toArray(new Token[0]));
	            			}
	            		}
	            	} else if( tkn.kind == INDEX) {
	            		tableElement.setIndexToken(tkn);
	            	} else if( tkn.kind == UNIQUE) {
	            		tableElement.setUniqueToken(tkn);
	            	} else if( tkn.kind == OPTIONS) { 
	                    List<Token> optionsTkns = new ArrayList<Token>();
	                    optionsTkns.add(tkn);
	                    
	                    // Check for parens in case of string(), decimal() types.. etc
	                    if( isNextTokenOfKind(tkns, iTkn, LPAREN) ) {
		                    Token[] bracketedTkns = getBracketedTokens(tkns, iTkn+1, LPAREN, RPAREN);
		                    if( bracketedTkns.length > 0 ) {     	
		                        for(Token dTypeTkn: bracketedTkns) {
		                        	iTkn++;
		                        	optionsTkns.add(dTypeTkn);
		                        }
		                    }
	                    }
	                    
	                    if( !optionsTkns.isEmpty() ) {
		                    OptionsClause options = new OptionsClause(analyzer);
		                    options.setOptionsTokens(optionsTkns.toArray(new Token[0]));
	                    	tableElement.setOptionClause(options);
	                    }
	                    
	            	} else if( tkn.kind == COMMA) {
	            		tblElementList.add(tableElement);
	            		tableElement.setLastTknIndex(getTokenIndex(tkn));
	            		tableElement = null;
	            		count = 0;
	            	}
	            	// After datatype could be
	            	// NOT NULL (2 tokens)
	            	// AUTO_INCREMENT
	            	// INDEX
	            	// PRIMARY KEY (a, b)
	            	// UNIQUE (a, b)
	            	// default 'someValue'
	            	// OPTIONS(....)
	            	// ','
	            } break;
	            
            }
            
            count++;
            
            // 2) Create a TableElement object with this tokens array
        }
        
        if( tableElement != null ) {
        	tableElement.setLastTknIndex(getTokenIndex(tkns[tkns.length-1]));
        	tblElementList.add(tableElement);
        }
  
        setElements(tblElementList.toArray(new TableElement[0]));
    }
    
    private boolean hasAnotherToken(Token[] tkns, int currentIndex) {
    	return currentIndex+2 < tkns.length;
    }
    
    
    protected boolean isNextTokenOfKind(Token[] tkns, int currentIndex, int kind) {
    	return hasAnotherToken(tkns, currentIndex) && tkns[currentIndex+1].kind == kind;
    }
    
    public boolean isDatatype(Token token) {
        for( int dType: DATATYPES) {
            if( token.kind == dType) return true;
        }
        return false;
    }

	@Override
	protected TokenContext getTokenContext(Position position) {
		TokenContext context = null;
		// Need to check each table element
		for(TableElement element: getElements()) {
			context = element.getTokenContext(position);
			if(context != null ) return context;
		}
		
		// Check options
		context = options.getTokenContext(position);
		
		return context;
	}
}
