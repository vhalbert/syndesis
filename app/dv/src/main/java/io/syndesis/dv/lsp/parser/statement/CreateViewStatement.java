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

public class CreateViewStatement extends AbstractStatementObject {
	private Token viewNameToken;
	private TableBody tableBody;
	private QueryExpression queryExpression;
	private int numTokens;
	
	private List<DdlAnalyzerException> exceptions = new ArrayList<DdlAnalyzerException>();
	
	public CreateViewStatement(DdlTokenAnalyzer analyzer) {
		super(analyzer);

		tableBody = new TableBody(analyzer);
		queryExpression = new QueryExpression(analyzer);
		this.numTokens = this.analyzer.getTokens().length;
		
		parseAndValidate();
		
		for(DdlAnalyzerException ex: exceptions ) {
			log(ex.getMessage());
		}
	}
	
	public String getStatement() {
		return this.analyzer.getStatement();
	}
	
	public String getViewName() {
		return viewNameToken.image;
	}
	
	public Token getViewNameToken() {
		return viewNameToken;
	}

	public TableBody getTableBody() {
		return tableBody;
	}

	public void setTableBody(TableBody tableBody) {
		this.tableBody = tableBody;
	}

	public QueryExpression getQueryExpression() {
		return queryExpression;
	}

	public void setQueryExpression(QueryExpression queryExpression) {
		this.queryExpression = queryExpression;
	}
	
	@Override
	protected void parseAndValidate() {
		// Check statement
		if( this.analyzer.getStatementType() != STATEMENT_TYPE.CREATE_VIEW_TYPE) {
			// TODO:  Add warning that statement type is not CREATE VIEW
			log("Statement does not start with CREATE VIEW");
		}

		
		// Check view name exists
		if( this.analyzer.getTokens() == null ) return;
		if( numTokens == 1) {
			setFirstTknIndex(0);
			setLastTknIndex(0);
			return;
		}
		
		if( numTokens == 2) {
			setFirstTknIndex(0);
			setLastTknIndex(1);
			return;
		}
		
		this.viewNameToken = getToken(2);
		Token token = getToken(2);
		if( token == null || token.kind != ID ) {
			// TODO: Add error that VIEW NAME IS REQUIRED
			exceptions.add(new DdlAnalyzerException("View name is missing after : " + getToken(1))); //$NON-NLS-1$ 
//			log("CREATE VIEW is missing name at position");
		} else {
			this.viewNameToken = token;
		}
		
		setFirstTknIndex(0);
		setLastTknIndex(2);
		
		// Check brackets match
		if( !isOk(this.analyzer.checkAllParens()) ) {
			log("Check all parens failed");
		}
		
		if( !isOk(this.analyzer.checkAllBrackets(LBRACE, RBRACE)) ) {
			log("Check all brackets failed");
		}
		
		// Check Table Body
		// If token[4] == '(' then search for 
		if( numTokens < 4 ) return;
		
		if( getTokenValue(3).equals("(") ) {
			parseTableBody();
		}

		queryExpression.parseAndValidate();
	}
	
	private void parseTableBody() {
		tableBody.setFirstTknIndex(4);
		// Now parse each table element
		// Break up table body into TableElements based on finding a "comma"
		tableBody.parseAndValidate();
		
	}
	
	private boolean isOk(DdlAnalyzerException exception) {
		if( exception == null ) return true;
		
		exceptions.add(exception);
		
		return false;
	}

	private String getTokenValue(int tokenIndex) {
		return getToken(tokenIndex).image;
	}
	
	private Token getToken(int tokenIndex) {
		return analyzer.getToken(tokenIndex);
	}
	
	private void log(String message) {
		System.out.println(message);
	}
	
	public List<DdlAnalyzerException> getStatus() {
		return exceptions;
	}
	
	public TokenContext getTokenContext(Position position) {
		if( isBetween(getFirstTknIndex(), getLastTknIndex(), position) ) {
			// PREFIX token
			return new TokenContext(position, this.analyzer.getTokenFor(position), CONTEXT.PREFIX);
		} else if( isBetween(tableBody.getFirstTknIndex(), 
							 tableBody.getLastTknIndex(), position) ) {
			// TABLE BODY 
			return tableBody.getTokenContext(position);
		} else if( isBetween(tableBody.getOptions().getFirstTknIndex(), 
							 tableBody.getOptions().getLastTknIndex(), position) ) {
			// TABLE OPTIONS
			return tableBody.getOptions().getTokenContext(position);
		} else if( isBetween(queryExpression.getFirstTknIndex(),
							 queryExpression.getLastTknIndex(), position) ) {
			return queryExpression.getTokenContext(position);
		}
		
		return new TokenContext(position, null, CONTEXT.NONE_FOUND);
	}


}
