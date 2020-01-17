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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

public class SampleTableItemInfo {
	
	private static List<CompletionItem> sampleTableItems = null;
 	
	private static String[] PARTS_ITEM_DATA = {
			"parts",   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			"parts", // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
			
	private static String[] SUPPLIER_ITEM_DATA = {
			"supplier",   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			"supplier", // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	private static String[] CUSTOMER_ITEM_DATA = {
			"customer",   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			"customer", // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	private static String[] SHIP_VIA_ITEM_DATA = {
			"ship_via",   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			"ship_via", // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	private static String[] PARTS_SUPPLIER_ITEM_DATA = {
			"partssupplier",   // String label;
			null, // String detail;
			null, // Either<String, MarkupContent> documentation;
			"partssupplier", // String insertText;
			null // InsertTextFormat insertTextFormat;
	};
	
	public static final Map<String, String[]> SAMPLE_PARTS_ITEM_DATA = Collections
			.unmodifiableMap(new HashMap<String, String[]>() {
				private static final long serialVersionUID = 1L;

				{
					put("parts", PARTS_ITEM_DATA);
					put("supplier", SUPPLIER_ITEM_DATA);
					put("customer", CUSTOMER_ITEM_DATA);
					put("ship_via", SHIP_VIA_ITEM_DATA);
					put("partssupplier", PARTS_SUPPLIER_ITEM_DATA);
				}
			});
	
	public static List<CompletionItem> getSampleTableItems() {
		if( sampleTableItems ==  null ) {
	    	sampleTableItems = new ArrayList<CompletionItem>(); 
	    	
			for(String[] tableInfo: SAMPLE_PARTS_ITEM_DATA.values() ) {
				sampleTableItems.add(createKeywordItem(tableInfo));
			}
		}
		
		return sampleTableItems;
	}
	
    
    public static CompletionItem createKeywordItem(String[] itemInfo) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(itemInfo[0]);
        ci.setKind(CompletionItemKind.Field);
        ci.setDetail(itemInfo[2]);
        ci.setDocumentation(itemInfo[3]);
        return ci;
    }
    
}
