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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;

public interface DdlCompletionConstants extends DdlAnalyzerConstants {
	
	int ALL_KIND = -1;
	int EMPTY_DDL = -2;

	String[] VIEW_ITEM_DATA = {
			/*
			 * The label of this completion item. By default also the text that is inserted
			 * when selecting this completion.
			 */
			getLabel(VIEW).toUpperCase(), // String label;
			
			/*
			 * A human-readable string with additional information about this item, like
			 * type or symbol information.
			 */
			null, // String detail;
			
			/*
			 * A human-readable string that represents a doc-comment.
			 */
			null, // Either<String, MarkupContent> documentation;
			
			/*
			 * A string that should be inserted a document when selecting this completion.
			 * When `falsy` the label is used.
			 */
			getLabel(VIEW).toUpperCase(), // String insertText;
			
			/*
			 * The format of the insert text. The format applies to both the `insertText`
			 * property and the `newText` property of a provided `textEdit`.
			 */
			null // InsertTextFormat insertTextFormat;
	};
	
	String[] CREATE_ITEM_DATA = {
			getLabel(CREATE).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(CREATE).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	String[] VIRTUAL_ITEM_DATA = {
			getLabel(VIRTUAL).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(VIRTUAL).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	String[] TABLE_ITEM_DATA = {
			getLabel(TABLE).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(TABLE).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	String[] PROCEDURE_ITEM_DATA = {
			getLabel(PROCEDURE).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(PROCEDURE).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] GLOBAL_ITEM_DATA = {
			getLabel(GLOBAL).toUpperCase(),   // String label;d;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(GLOBAL).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] FOREIGN_ITEM_DATA = {
			getLabel(FOREIGN).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(FOREIGN).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] TRIGGER_ITEM_DATA = {
			getLabel(TRIGGER).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(TRIGGER).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] TEMPORARY_ITEM_DATA = {
			getLabel(TEMPORARY).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(TEMPORARY).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] ROLE_ITEM_DATA = {
			getLabel(ROLE).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(ROLE).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] SCHEMA_ITEM_DATA = {
			getLabel(SCHEMA).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(SCHEMA).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] SERVER_ITEM_DATA = {
			getLabel(SERVER).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(SERVER).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] DATABASE_ITEM_DATA = {
			getLabel(DATABASE).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(DATABASE).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] SELECT_ITEM_DATA = {
			getLabel(SELECT).toUpperCase(),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(SELECT).toUpperCase(), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	public static final Map<String, String[]> KEYWORDS_ITEM_DATA = Collections
			.unmodifiableMap(new HashMap<String, String[]>() {
				private static final long serialVersionUID = 1L;

				{
					put(getLabel(VIEW).toUpperCase(), VIEW_ITEM_DATA);
					put(getLabel(VIEW).toUpperCase(), CREATE_ITEM_DATA);
					put(getLabel(VIEW).toUpperCase(), VIRTUAL_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), GLOBAL_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), FOREIGN_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), TABLE_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), TEMPORARY_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), ROLE_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), SCHEMA_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), SERVER_ITEM_DATA);
//					put(getLabel(VIEW).toUpperCase(), DATABASE_ITEM_DATA);
					put(getLabel(VIEW).toUpperCase(), PROCEDURE_ITEM_DATA);
				}
			});
	
	/**
	 * DATATYPES (in lower case)
	 */

	String[] STRING_ITEM_DATA = {
			getLabel(STRING),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(STRING), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] VARBINARY_ITEM_DATA = {
			getLabel(VARBINARY),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(VARBINARY), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] VARCHAR_ITEM_DATA = {
			getLabel(VARCHAR),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(VARCHAR), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] BOOLEAN_ITEM_DATA = {
			getLabel(BOOLEAN),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(BOOLEAN), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] BYTE_ITEM_DATA = {
			getLabel(BYTE),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(BYTE), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] TINYINT_ITEM_DATA = {
			getLabel(TINYINT),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(TINYINT), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] SHORT_ITEM_DATA = {
			getLabel(SHORT),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(SHORT), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] SMALLINT_ITEM_DATA = {
			getLabel(SMALLINT),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(SMALLINT), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] CHAR_ITEM_DATA = {
			getLabel(CHAR),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(CHAR), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] INTEGER_ITEM_DATA = {
			getLabel(INTEGER),   // String label;

			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(INTEGER), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] LONG_ITEM_DATA = {
			getLabel(LONG),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(LONG), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] BIGINT_ITEM_DATA = {
			getLabel(BIGINT),   // String label;

			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(BIGINT), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] BIGINTEGER_ITEM_DATA = {
			getLabel(BIGINTEGER),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(BIGINTEGER), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] FLOAT_ITEM_DATA = {
			getLabel(FLOAT),   // String label;

			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(FLOAT), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] REAL_ITEM_DATA = {
			getLabel(REAL),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(REAL), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] DOUBLE_ITEM_DATA = {
			getLabel(DOUBLE),   // String label;

			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(DOUBLE), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] BIGDECIMAL_ITEM_DATA = {
			getLabel(BIGDECIMAL),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(BIGDECIMAL), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] DECIMAL_ITEM_DATA = {
			getLabel(DECIMAL),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(DECIMAL), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] DATE_ITEM_DATA = {
			getLabel(DATE),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(DATE), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] TIME_ITEM_DATA = {
			getLabel(TIME),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(TIME), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] TIMESTAMP_ITEM_DATA = {
			getLabel(TIMESTAMP),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(TIMESTAMP), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] BLOB_ITEM_DATA = {
			getLabel(BLOB),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(BLOB), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] CLOB_ITEM_DATA = {
			getLabel(CLOB),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(CLOB), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] XML_ITEM_DATA = {
			getLabel(XML),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(XML), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] JSON_ITEM_DATA = {
			getLabel(JSON),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(JSON), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] GEOMETRY_ITEM_DATA = {
			getLabel(GEOMETRY),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(GEOMETRY), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] GEOGRAPHY_ITEM_DATA = {
			getLabel(GEOGRAPHY),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(GEOGRAPHY), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};

	String[] OBJECT_ITEM_DATA = {
			getLabel(OBJECT),   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			getLabel(OBJECT), // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	public static final Map<String, String[]> DATATYPES_ITEM_DATA = Collections
			.unmodifiableMap(new HashMap<String, String[]>() {
				private static final long serialVersionUID = 1L;

				{
					put(getLabel(STRING), STRING_ITEM_DATA);
					put(getLabel(VIEW), VARBINARY_ITEM_DATA);
					put(getLabel(VIEW), VARCHAR_ITEM_DATA);
					put(getLabel(VIEW), BOOLEAN_ITEM_DATA);
					put(getLabel(VIEW), BYTE_ITEM_DATA);
					put(getLabel(VIEW), TINYINT_ITEM_DATA);
					put(getLabel(VIEW), SHORT_ITEM_DATA);
					put(getLabel(VIEW), SMALLINT_ITEM_DATA);
					put(getLabel(VIEW), CHAR_ITEM_DATA);
					put(getLabel(VIEW), INTEGER_ITEM_DATA);
					put(getLabel(VIEW), LONG_ITEM_DATA);
					put(getLabel(VIEW), BIGINT_ITEM_DATA);
					put(getLabel(VIEW), FLOAT_ITEM_DATA);
					put(getLabel(VIEW), REAL_ITEM_DATA);
					put(getLabel(VIEW), DOUBLE_ITEM_DATA);
					put(getLabel(VIEW), BIGDECIMAL_ITEM_DATA);
					put(getLabel(VIEW), DECIMAL_ITEM_DATA);
					put(getLabel(VIEW), DATE_ITEM_DATA);
					put(getLabel(VIEW), TIME_ITEM_DATA);
					put(getLabel(VIEW), TIMESTAMP_ITEM_DATA);
					put(getLabel(VIEW), BLOB_ITEM_DATA);
					put(getLabel(VIEW), CLOB_ITEM_DATA);
					put(getLabel(VIEW), XML_ITEM_DATA);
					put(getLabel(VIEW), JSON_ITEM_DATA);
					put(getLabel(VIEW), GEOMETRY_ITEM_DATA);
					put(getLabel(VIEW), GEOGRAPHY_ITEM_DATA);
					put(getLabel(VIEW), OBJECT_ITEM_DATA);
				}
			});
	
 	public static String getLabel(int keywordId) {
 		return DdlAnalyzerConstants.getLabel(keywordId);
    }
}
