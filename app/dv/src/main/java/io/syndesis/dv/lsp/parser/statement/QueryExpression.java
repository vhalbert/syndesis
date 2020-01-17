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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class QueryExpression extends AbstractStatementObject {
	public QueryExpression(DdlTokenAnalyzer analyzer) {
		super(analyzer);
	}

	@Override
	protected void parseAndValidate() {
		// For now we're going to pull ALL the tokens from AS... on... except for the ';' if it exists
		
		Token[] tokens = getTokens();
		Token lastToken = tokens[tokens.length-1];
		if( tokens[tokens.length-1].kind == SEMICOLON ) {
			lastToken = tokens[tokens.length-2];
			setLastTknIndex(tokens.length-2);
		}
		Token firstToken = findTokenByKind(AS);
		
		setFirstTknIndex(firstTknIndex);
	}

	@Override
	protected TokenContext getTokenContext(Position position) {
		// TODO Auto-generated method stub
		return null;
	}
}
