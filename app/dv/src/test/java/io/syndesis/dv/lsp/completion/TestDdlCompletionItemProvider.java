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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.junit.Test;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;

@SuppressWarnings("nls")
public class TestDdlCompletionItemProvider implements DdlAnalyzerConstants {

	private DdlCompletionProvider itemProvider = new DdlCompletionProvider();
	
//	private static final int NUM_NON_DATATYPE_WORDS = 359;
//	private static final int NUM_CREATE_WORDS = 12;
//	private static final int NUM_VIRTUAL_WORDS = 2;
//	private static final int NUM_VIEW_WORDS = 5;
	
	/*
    * The tokenImage[...] call is returning strings wrapped in double-quotes
    * 
    * Need to return a simple string
    * @param tokenImageString string
    * @return string without double quotes
    */
	public static String getLabel(int keywordId) {
		String tokenImageStr = tokenImage[keywordId];
    	return tokenImageStr.substring(1, tokenImageStr.length()-1);
    }
	
    @Test
    public void testEmptyViewCompletions() throws Exception {
    	List<CompletionItem> items = null;
        System.out.println(" TEST:  testEmptyViewCompletions()");
        
        //             01234567890123456789
        String stmt = "";
        
        // CREATE 0, 0, expect 1 items (i.e. CREATE )
        items = itemProvider.getCompletionItems(stmt, new Position(0, 0));
        assertEquals(3, items.size());
    }
    
    @Test
    public void testViewNameCompletions() throws Exception {
    	List<CompletionItem> items = null;
    	String stmt = null;
        System.out.println(" TEST:  testViewNameCompletions()");
        
        //      01234567890123456789
        stmt = "";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 0));
        assertEquals(3, items.size());
        
        //      01234567890123456789
        stmt = "CREATE "; // VIEW ";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 7));
        assertEquals(3, items.size());

        //      01234567890123456789
        stmt = "CREATE VIEW ";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 12));
        assertEquals(SampleTableItemInfo.SAMPLE_PARTS_ITEM_DATA.size(), items.size());
        
        //      01234567890123456789
        stmt = "CREATE VIEW foobar ";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 19));
        assertEquals(1, items.size());
        
        // looking for left parenthesis (
        //      01234567890123456789
        stmt = "CREATE VIEW foobar (";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 20));
        assertEquals(0, items.size());

    }
	
    @Test
    public void testCreateViewCompletions() throws Exception {
    	List<CompletionItem> items = null;
        System.out.println(" TEST:  testCreateViewCompletions()");
        
        //             01234567890123456789
        String stmt = "CREATE VIEW winelist ( \n" + 
	        		  "   wine string(255), price decimal(2, 15), vendor string(255) \n" + 
	        		  ") AS SELECT * FROM PostgresDB.winelist";
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 0));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 5));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 7));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 6));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 7));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 10));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(0, 11));
        assertEquals(3, items.size());
        
        items = itemProvider.getCompletionItems(stmt, new Position(1, 8));
        assertEquals(28, items.size());
    }
}
