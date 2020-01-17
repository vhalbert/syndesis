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
package io.syndesis.dv.lsp.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.JavaCharStream;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;

public class DdlTokenAnalyzer implements DdlAnalyzerConstants {
    
    private final String statement;
    private Token[] tokens = null;
    private final STATEMENT_TYPE statementType;
    
    public DdlTokenAnalyzer(String statement) {
        super();
        this.statement = statement;
        init();
        this.statementType = getStatementType();
    }
    
    public String getStatement() {
    	return this.statement;
    }
    
    private void init() {

        JavaCharStream jcs = new JavaCharStream(new StringReader(this.statement));
        TeiidDdlParserTokenManager token_source = new TeiidDdlParserTokenManager(jcs);
        
        List<Token> tokensList = new ArrayList<Token>();
        
        Token currentToken = token_source.getNextToken();
        		
        if( currentToken != null ) {
        	convertToken(currentToken);

        	
        	// Add current token to simple list
            tokensList.add(currentToken);
            
            boolean done = false;
            
            while ( !done ) {
            	// Get next token
            	currentToken = token_source.getNextToken();

                // Check if next token exists 
                if( currentToken != null && (currentToken.image.length() > 0) ) {
                	convertToken(currentToken);
                    tokensList.add(currentToken);
                } else done = true;
            }
        }
        
        this.tokens = tokensList.toArray(new Token[0]);
    }
    
    private void convertToken(Token token) {
    	token.beginColumn--;
    	token.endColumn--;
    	token.beginLine--;
    	token.endLine--;
    }

    public Token[] getTokens() {
        return this.tokens;
    }

    public Token getTokenFor(Position pos) {
    	DdlTokenWalker walker = new DdlTokenWalker(this.tokens);
    	Token token = walker.findToken(pos, this.statementType);
    	//System.out.println("  Walker found Token = " + token + " At " + pos);
    	return token;
    }

    protected String[] getDatatypesList() {
    	return DATATYPE_LIST;
    }
    
    public String[] getNextWordsByKind(int kind) {
    	return getNextWordsByKind(kind, false);
    }
    
    public String[] getNextWordsByKind(int kind, boolean isStatementId) {
        List<String> words = new ArrayList<String>();
        
        switch (kind) {
            case CREATE:
                words.add(getKeywordLabel(VIEW).toUpperCase());
                words.add(getKeywordLabel(VIRTUAL).toUpperCase());
//                words.add(getKeywordLabel(GLOBAL).toUpperCase());
//                words.add(getKeywordLabel(FOREIGN).toUpperCase());
//                words.add(getKeywordLabel(TABLE).toUpperCase());
//                words.add(getKeywordLabel(TRIGGER).toUpperCase());
//                words.add(getKeywordLabel(TEMPORARY).toUpperCase());
//                words.add(getKeywordLabel(ROLE).toUpperCase());
//                words.add(getKeywordLabel(SCHEMA).toUpperCase());
//                words.add(getKeywordLabel(SERVER).toUpperCase());
//                words.add(getKeywordLabel(DATABASE).toUpperCase());
                words.add(getKeywordLabel(PROCEDURE).toUpperCase());
            break;
            
            case GLOBAL:
                words.add(getKeywordLabel(TEMPORARY).toUpperCase());
            break;
            
            case TEMPORARY:
                words.add(getKeywordLabel(TABLE).toUpperCase());
            break;
            
            case FOREIGN:
                words.add(getKeywordLabel(TABLE).toUpperCase());
                words.add(getKeywordLabel(TEMPORARY).toUpperCase());
            break;
            
            case VIRTUAL:
                words.add(getKeywordLabel(VIEW).toUpperCase());
                words.add(getKeywordLabel(PROCEDURE).toUpperCase());
            break;
            
            case ID:
            	if( isStatementId ) {
            		words.add(getKeywordLabel(LPAREN));
            	}
            break;
            
            case SELECT:
                words.add(getKeywordLabel(STAR).toUpperCase());
                break;

            default:
        }
        
        return stringListToArray(words);
    }
    
    private String[] stringListToArray(List<String> array) {
        return array.toArray(new String[0]);
    }
    
    public STATEMENT_TYPE getStatementType() {
        // walk through start of token[] array and return the type
        if( tokens.length < 2 ) return STATEMENT_TYPE.UNKNOWN_STATEMENT_TYPE;
        
        if( isStatementType(tokens, CREATE_VIRTUAL_VIEW_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_VIRTUAL_VIEW_TYPE;
        }
        
        if( isStatementType(tokens, CREATE_VIEW_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_VIEW_TYPE;
        }
        
        if( isStatementType(tokens, CREATE_GLOBAL_TEMPORARY_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_GLOBAL_TEMPORARY_TABLE_TYPE;
        }
        
        if( isStatementType(tokens, CREATE_FOREIGN_TEMPORARY_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_FOREIGN_TEMPORARY_TABLE_TYPE;
        }
        
        if( isStatementType(tokens, CREATE_FOREIGN_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_FOREIGN_TABLE_TYPE;
        }
        
        if( isStatementType(tokens, CREATE_TABLE_STATEMENT) ) {
            return STATEMENT_TYPE.CREATE_TABLE_TYPE;
        }

        return STATEMENT_TYPE.UNKNOWN_STATEMENT_TYPE;
    }
    
    private boolean isStatementType(Token[] tkns, int[] statementTokens) {
        int iTkn = 0;
        for(int kind : statementTokens ) {
            // Check each token for kind
            if( tkns[iTkn].kind == kind) {
                if( ++iTkn == statementTokens.length) return true;
                continue;
            };
            break;
        }
        return false;
    }
    
    public boolean allParensMatch(Token[] tkns) {
        return parensMatch(tkns, 0);
    }
    
    
    
    public DdlAnalyzerException checkAllParens() {
    	return checkAllBrackets(LPAREN, RPAREN);
    }
    
    public DdlAnalyzerException checkAllBrackets(int leftBracketKind, int rightBracketKind) {
        int numUnmatchedParens = 0;
        DdlAnalyzerException exception = null;

        for(int iTkn= 0; iTkn<tokens.length; iTkn++) {
            Token token = tokens[iTkn];
            if( token.kind == leftBracketKind) numUnmatchedParens++;
            if( token.kind == rightBracketKind) numUnmatchedParens--;
            
            // If the ## goes < 0 throw exception because they should be correctly nested
            //  VALID:  (  () () )
            //  INVALID (  )) () (
            //              ^ would occur here
            if( exception == null && numUnmatchedParens < 0 ) {
                exception = new DdlAnalyzerException("Bracket at location " //$NON-NLS-1$ 
                        + getPositionString(token) + " does not properly match previous bracket"); //$NON-NLS-1$ 
            }
            if( exception != null ) break;
        }
        
        if( numUnmatchedParens != 0 ) {
            exception = new DdlAnalyzerException("Missing or mismatched brackets"); //$NON-NLS-1$ 
        }

        return exception;
    }
    
    public String getPositionString(Token tkn) {
        return "( " + tkn.beginLine + ", " + tkn.beginColumn + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public boolean bracketsMatch(Token[] tkns, int startTokenId, int leftBracket, int rightBracket) {
        int numUnmatchedParens = 0;
        
        for(int iTkn= 0; iTkn<tokens.length; iTkn++) {
            if( iTkn < startTokenId) continue;
            
            Token token = tkns[iTkn];
            if( token.kind == leftBracket) numUnmatchedParens++;
            if( token.kind == rightBracket) numUnmatchedParens--;
        }
        return numUnmatchedParens == 0;
    }
    
    public boolean parensMatch(Token[] tkns, int startTokenId) {
    	return bracketsMatch(tkns, startTokenId, LPAREN, RPAREN);
    }
    
    public Token[] getBracketedTokens(Token[] tkns, int startTokenId, int bracketStart, int bracketEnd) throws DdlAnalyzerException {
        int numUnmatchedParens = 0;
        
        for(int iTkn = 0; iTkn<tokens.length; iTkn++) {
            if( iTkn < startTokenId) continue;
            Token token = tkns[iTkn];
            if( token.kind == bracketStart) numUnmatchedParens++;
            if( token.kind == bracketEnd) numUnmatchedParens--;
            
            if( numUnmatchedParens == 0) {
                List<Token> bracketedTokens = new ArrayList<Token>(tkns.length);
                for(int jTkn = startTokenId+1; jTkn < iTkn; jTkn++) {
                    bracketedTokens.add(tkns[jTkn]);
                }
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
    public List<Token[]> processTableBodyTokens(Token[] tkns) throws DdlAnalyzerException{
        List<Token[]> tknTknList = new ArrayList<Token[]>();
        
        List<Token> columnTkns = null;
        
        for(int iTkn = 0; iTkn<tkns.length; iTkn++) {
            Token tkn = tkns[iTkn];
            
            if( columnTkns == null ) columnTkns = new ArrayList<Token>();
            if( tkn.kind != COMMA) {
                if( isDatatype(tkn) ) {
                    columnTkns.add(tkn); // the datatype
                    // process table body (column) OPTIONS() tokens
                    Token[] datatypeTokens = getBracketedTokens(tkns, iTkn+1, LPAREN, RPAREN);
                    
                    if( datatypeTokens.length > 0 ) {
                        iTkn++;
                        columnTkns.add(tkns[iTkn]); // '('

                        for(Token optTkn: datatypeTokens) {
                            columnTkns.add(optTkn);
                        }
                        iTkn = iTkn + datatypeTokens.length;
                        iTkn++;
                        columnTkns.add(tkns[iTkn]); // ')'
                    }
                } else if( tkn.kind == OPTIONS ) {
                    columnTkns.add(tkn); // OPTIONS
                    
                    // process table body (column) OPTIONS() tokens
                    Token[] optionsTokens = getBracketedTokens(tkns, iTkn+1, LPAREN, RPAREN);
                    iTkn++;
                    columnTkns.add(tkns[iTkn]); // '('

                    for(Token optTkn: optionsTokens) {
                        columnTkns.add(optTkn);
                    }
                    iTkn = iTkn + optionsTokens.length;
                    iTkn++;
                    columnTkns.add(tkns[iTkn]); // ')'
                } else {
                    columnTkns.add(tkn);
                }
            } else {
                if( columnTkns.isEmpty() ) throw new DdlAnalyzerException("Error in column definition");
                
                // found comma, so need to reset tkn list and add
                tknTknList.add(columnTkns.toArray(new Token[0]));
                columnTkns = null;
            }
        }
        
        if( columnTkns != null && !columnTkns.isEmpty() ) 
            tknTknList.add(columnTkns.toArray(new Token[0]));
        
        return tknTknList;
    }
    
    public boolean isDatatype(Token token) {
        for( int dType: DATATYPES) {
            if( token.kind == dType) return true;
        }
        return false;
    }

    public int getStatementStartLength(STATEMENT_TYPE type) {
    	switch(type) {
	    	case CREATE_VIEW_TYPE:
	    	case CREATE_TABLE_TYPE:
	    		return 2;
	    	case CREATE_FOREIGN_TABLE_TYPE:
	    	case CREATE_VIRTUAL_VIEW_TYPE:
	    		return 3;
	    	case CREATE_GLOBAL_TEMPORARY_TABLE_TYPE:
	    	case CREATE_FOREIGN_TEMPORARY_TABLE_TYPE:
	    		return 4;
	    	case UNKNOWN_STATEMENT_TYPE:
	    	default:
    	}
		return 0;
    }
    
    public void printTokens() {
    	printTokens(this.tokens, null);
    }
    
    public Token getToken(int tokenIndex) {
    	return this.tokens[tokenIndex];
    }
    
    private void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
			System.out.println("  >> Token = " + token.image +
					"\n\t   >> KIND = " + token.kind + 
					"\n\t   >> begins at ( " + 
					token.beginLine + ", " + token.beginColumn + " )" +
					"\n\t   >> ends   at ( " + 
					token.endLine + ", " + token.endColumn + " )");

        }
    }
    
    public String[] getKeywordLabels(int[] keywordIds) {
    	List<String> labels = new ArrayList<String>();
    	
    	for( int id: keywordIds) {
    		labels.add(getKeywordLabel(id).toUpperCase());
    	}
    	
    	return labels.toArray(new String[0]);
    }
    
    /* 
     * The tokenImage[...] call is returning strings wrapped in double-quotes
     * 
     * Need to return a simple string
     * @param tokenImageString string
     * @return string without double quotes
     */
    public String getKeywordLabel(int keywordId) {
    	return DdlCompletionConstants.getLabel(keywordId);
    }
}
