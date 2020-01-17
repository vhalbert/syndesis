package io.syndesis.dv.lsp.parser.statement;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;


public class TokenContext implements DdlAnalyzerConstants {
	private Position position;
	private Token token;
	private CONTEXT context;
	
	public TokenContext(Position position, Token token, CONTEXT context) {
		super();
		this.position = position;
		this.token = token;
		this.context = context;
	}

	public Position getPosition() {
		return position;
	}

	public Token getToken() {
		return token;
	}

	public CONTEXT getContext() {
		return context;
	}
	
	public String contextToString() {
		switch(context) {
			case PREFIX: return "PREFIX";
			case TABLE_BODY: return "TABLE_BODY";
			case TABLE_OPTIONS: return "TABLE_OPTIONS";
			case TABLE_ELEMENT: return "TABLE_ELEMENT";
			case TABLE_ELEMENT_OPTIONS: return "TABLE_ELEMENT_OPTIONS";
			case QUERY_EXPRESSION: return "QUERY_EXPRESSION";
			default:  return "NONE_FOUND";
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TokenContext: ");
		if( token != null ) {
			sb.append(token.image);
		} else sb.append(" NONE ");
		sb.append(" at (")
			.append(position.getLine())
			.append(", ")
			.append(position.getCharacter())
			.append(")");
		sb.append(" Context: ").append(contextToString());
		
		return sb.toString();
	}
}
