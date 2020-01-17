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

public class TableElement extends AbstractStatementObject {
	private Token nameToken;
	private Token[] datatypeTokens;
	private Token[] notNullTokens;
	private Token autoIncrementToken;
	private Token uniqueToken;
	private Token indexToken;
	private Token[] pkTokens;
	private Token[] defaultTokens;
	private OptionsClause optionClause;
	private int datatypeKind;

	private List<DdlAnalyzerException> exceptions = new ArrayList<DdlAnalyzerException>();
	
	public TableElement(DdlTokenAnalyzer analyzer) {
		super(analyzer);
	}

	@Override
	protected void parseAndValidate() {

	}

	public Token getNameToken() {
		return nameToken;
	}

	public void setNameToken(Token nameToken) {
		this.nameToken = nameToken;
	}

	public Token[] getDatatypeTokens() {
		return datatypeTokens;
	}

	public void setDatatypeTokens(Token[] datatypeTokens) {
		this.datatypeTokens = datatypeTokens;
	}

	public OptionsClause getOptionClause() {
		return optionClause;
	}

	public void setOptionClause(OptionsClause optionClause) {
		this.optionClause = optionClause;
	}

	public int getDatatypeKind() {
		return datatypeKind;
	}

	public void setDatatypeKind(int datatypeKind) {
		this.datatypeKind = datatypeKind;
	}

	public Token[] getNotNullTokens() {
		return notNullTokens;
	}

	public void setNotNullTokens(Token[] notNullTokens) {
		this.notNullTokens = notNullTokens;
	}
	
	public Token[] getPrimaryKeyTokens() {
		return pkTokens;
	}

	public void setPrimaryKeyTokens(Token[] pkTokens) {
		this.pkTokens = pkTokens;
	}
	
	public Token[] getDefaultTokens() {
		return defaultTokens;
	}

	public void setDefaultTokens(Token[] defaultTokens) {
		this.defaultTokens = defaultTokens;
	}

	public Token getAutoIncrementToken() {
		return autoIncrementToken;
	}

	public void setAutoIncrementToken(Token autoIncrementToken) {
		this.autoIncrementToken = autoIncrementToken;
	}

	public Token getUniqueToken() {
		return uniqueToken;
	}

	public void setUniqueToken(Token uniqueToken) {
		this.uniqueToken = uniqueToken;
	}

	public Token getIndexToken() {
		return indexToken;
	}

	public void setIndexToken(Token indexToken) {
		this.indexToken = indexToken;
	}

	public List<DdlAnalyzerException> getExceptions() {
		return exceptions;
	}

	@Override
	protected TokenContext getTokenContext(Position position) {
		boolean isInElement = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
		if( isInElement ) {
			Token tkn = this.analyzer.getTokenFor(position);
			return new TokenContext(position, tkn, CONTEXT.TABLE_ELEMENT);
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TableElement:   ").append(getNameToken());
		if( getDatatypeTokens() != null ) {
			for( Token tkn: getDatatypeTokens()) {
				sb.append(" " + tkn.image);
			}
		}
		if( getNotNullTokens() != null ) {
			for( Token tkn: getNotNullTokens()) {
				sb.append(" " + tkn.image);
			}
		}
		if( getAutoIncrementToken() != null ) {
			sb.append(getAutoIncrementToken().image);
		}
		if( getIndexToken() != null ) {
			sb.append(" " + getIndexToken().image);
		}
		if( getDefaultTokens() != null ) {
			for( Token tkn: getDefaultTokens()) {
				sb.append(" " + tkn.image);
			}
		}
		if( getUniqueToken() != null ) {
			sb.append(" " + getUniqueToken().image);
		}
		if( getPrimaryKeyTokens() != null ) {
			for( Token tkn: getPrimaryKeyTokens()) {
				sb.append(" " + tkn.image);
			}
		}
		if( getOptionClause() != null ) {
			for( Token tkn: getOptionClause().getOptionsTokens()) {
				sb.append(" " + tkn.image);
			}
		}
		
		return sb.toString();
	}

	
}
