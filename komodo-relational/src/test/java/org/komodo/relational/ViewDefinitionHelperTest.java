/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership. Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.relational;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Model.Type;
import org.komodo.relational.model.View;
import org.komodo.relational.profile.SqlComposition;
import org.komodo.relational.profile.ViewDefinition;
import org.komodo.relational.profile.ViewEditorState;
import org.komodo.relational.vdb.ModelSource;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.spi.KException;

@SuppressWarnings({ "javadoc", "nls" })
public class ViewDefinitionHelperTest extends RelationalModelTest {
	
    private static String viewDefinitionName = "orderInfoView";
    private static String description = "test view description text";
    private boolean isComplete = true;
    private static String sourceTablePath1 = "connection=pgconnection1/schema=public/table=orders";
    private static String sourceTablePath2 = "connection=pgconnection1/schema=public/table=customers";
    private static String sourceTablePath3 = "connection=pgconnection2/schema=public/table=customers";
    private static String comp1Name = "comp1";
    private static String comp1Desc = "description for comp1";
    private static String comp1LeftSource = "pgconnection1schemamodel.orders";
    private static String comp1RightSource = "pgconnection1schemamodel.customers";
    private static String comp1LeftColumn = "ID";
    private static String comp1RightColumn = "ID";
    private static String comp1Operator = "EQ";
    
    private static String FQN_TABLE_1 = "schema=public/table=orders";
    private static String FQN_TABLE_2 = "schema=public/table=customers";
    
    private static final String TEST_VIEW_NAME = "test_view_name";
    private static final String DS_NAME = "pgconnection1";
    private static final String DS_JNDI_NAME = "java:/jndiName1";
    private static final String VDB_NAME = "pgconnection1schemavdb";
    private static final String MODEL_NAME = "pgconnection1schemamodel";
    private static final String DS_NAME_2 = "pgconnection2";
    private static final String DS_JNDI_NAME2 = "java:/jndiName2";
    private static final String VDB_NAME_2 = "pgconnection2schemavdb";
    private static final String MODEL_NAME_2 = "pgconnection2schemamodel";
    private static final String TRANSLATOR_JDBC = "jdbc";
    
    private boolean doPrint = false;
    
    
    private final static String TABLE_OPTION_FQN = "teiid_rel:fqn"; //$NON-NLS-1$
    
    private final static String SET_NAMESPACE_STRING = "SET NAMESPACE 'http://www.teiid.org/ext/relational/2012' AS teiid_rel;\n\n";
    
    private final static String pgconnection1schemamodelDDL = 
    		SET_NAMESPACE_STRING +
    		"CREATE FOREIGN TABLE orders ( "
    		+ "ID long, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" \"" + FQN_TABLE_1 + "\");\n" +
    		"CREATE FOREIGN TABLE customers ( "
    		+ "ID long, name string) OPTIONS(\"" + TABLE_OPTION_FQN + "\" \"" + FQN_TABLE_2 + "\");";
    
    private final static String pgconnection2schemamodelDDL = 
    		SET_NAMESPACE_STRING +
    		"CREATE FOREIGN TABLE orders ( "
    		+ "ID long, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" \"" + FQN_TABLE_1 + "\");\n" +
    		"CREATE FOREIGN TABLE customers ( "
    		+ "ID long, customerName string) OPTIONS(\"" + TABLE_OPTION_FQN + "\" \"" + FQN_TABLE_2 + "\");";
    
    private final static String EXPECTED_JOIN_SQL_TWO_SOURCES_START =
            "CREATE VIEW orderInfoView (RowId integer PRIMARY KEY, ID LONG, orderDate TIMESTAMP, customerName STRING) AS \n"
          + "SELECT ROW_NUMBER() OVER (ORDER BY A.ID), A.ID, A.orderDate, B.customerName\n"
          + "FROM pgconnection1schemamodel.orders AS A \n";
    
    private final static String  EXPECTED_JOIN_SQL_TWO_SOURCES_END = "pgconnection2schemamodel.customers AS B \n"
            + "ON \n"
            + "A.ID = B.ID;";
    
    private final static String EXPECTED_JOIN_SQL_SINGE_SOURCE_START =
            "CREATE VIEW orderInfoView (RowId integer PRIMARY KEY, ID LONG, orderDate TIMESTAMP, name STRING) AS \n"
          + "SELECT ROW_NUMBER() OVER (ORDER BY ID), ID, orderDate, name\n"
          + "FROM pgconnection1schemamodel.orders \n";
    
    private final static String  EXPECTED_JOIN_SQL_SINGLE_SOURCE_END = "pgconnection1schemamodel.customers \n"
          + "ON \n"
          + "pgconnection1schemamodel.orders.ID = pgconnection1schemamodel.customers.ID;";
    
    private final static String EXPECTED_NO_JOIN_SQL_SINGE_SOURCE =
            "CREATE VIEW orderInfoView (RowId integer PRIMARY KEY, ID LONG, orderDate TIMESTAMP) AS \n"
          + "SELECT ROW_NUMBER() OVER (ORDER BY ID), ID, orderDate\n"
          + "FROM pgconnection1schemamodel.orders;";
    
    String EXPECTED_DDL =
            "CREATE VIEW orderInfoView (RowId integer PRIMARY KEY, ID LONG, orderDate TIMESTAMP, customerName STRING) AS \n"
          + "SELECT ROW_NUMBER() OVER (ORDER BY A.ID), A.ID, A.orderDate, B.customerName\n"
          + "FROM pgconnection1schemamodel.orders AS A \n"
          + "INNER JOIN \n"
          + "pgconnection2schemamodel.customers AS B \n"
          + "ON \n"
          + "A.ID = B.ID;";
    
    private final static String INNER_JOIN_STR = "INNER JOIN \n";
    private final static String LEFT_OUTER_JOIN_STR = "LEFT OUTER JOIN \n";
    private final static String RIGHT_OUTER_JOIN_STR = "RIGHT OUTER JOIN \n";
    private final static String FULL_OUTER_JOIN_STR = "FULL OUTER JOIN \n";

    @Before
    public void init() throws Exception {
    	Vdb dsVdb = createVdb(viewDefinitionName, "originalFilePath");
    	Model viewModel = dsVdb.addModel(getTransaction(), TEST_VIEW_NAME);
    	viewModel.setModelType(getTransaction(), Type.VIRTUAL);
    	
        Connection connection = createConnection( DS_NAME );
        connection.setDescription(getTransaction(), description);

        final String extLoc = "new-external-location";
        connection.setExternalLocation(getTransaction(), extLoc );

        connection.setJdbc(getTransaction(), false);
        connection.setJndiName(getTransaction(), DS_JNDI_NAME);
        connection.setDriverName(getTransaction(), "dsDriver");
        connection.setClassName(getTransaction(), "dsClassname");
        connection.setProperty(getTransaction(), "prop1", "prop1Value");
        connection.setProperty(getTransaction(), "prop2", "prop2Value");

        // Create a VDB for this connection
        Vdb sourceVdb = createVdb(VDB_NAME, connection, "originalFilePath");
        Model sourceModel = sourceVdb.addModel(getTransaction(), MODEL_NAME);
        sourceModel.setModelDefinition(getTransaction(), pgconnection1schemamodelDDL);
        ModelSource modelSource = sourceModel.addSource(getTransaction(), DS_NAME);
        modelSource.setJndiName(getTransaction(), DS_JNDI_NAME);
        modelSource.setTranslatorName(getTransaction(), TRANSLATOR_JDBC);
        
        commit();
        
        connection = createConnection( DS_NAME_2 );
        connection.setDescription(getTransaction(), description);
        
        connection.setExternalLocation(getTransaction(), extLoc );
        
        connection.setJdbc(getTransaction(), false);
        connection.setJndiName(getTransaction(), "DS_JNDI_NAME2");
        connection.setDriverName(getTransaction(), "dsDriver");
        connection.setClassName(getTransaction(), "dsClassname2");
        connection.setProperty(getTransaction(), "prop1", "prop1Value");
        connection.setProperty(getTransaction(), "prop2", "prop2Value");

        // Create a VDB for this connection
        sourceVdb = createVdb(VDB_NAME_2, connection, "originalFilePath");
        sourceModel = sourceVdb.addModel(getTransaction(), MODEL_NAME_2);
        sourceModel.addSource(getTransaction(), DS_NAME_2);
        modelSource.setJndiName(getTransaction(), DS_JNDI_NAME2);
        modelSource.setTranslatorName(getTransaction(), TRANSLATOR_JDBC);
        
        // Rather than creating model objects piecemeal, it's easier to have komodo & modeshape do it via
        // setting the model definition via Teiid DDL statements
        sourceModel.setModelDefinition(getTransaction(), pgconnection2schemamodelDDL);
        
        
        commit();
    }
    
    private String helpGenerateDdlForWithJoinType(String secondSourceTablePath, String joinType) throws KException {
    	
        ViewDefinitionHelper helper = new ViewDefinitionHelper(WorkspaceManager.getInstance(_repo, getTransaction()));

        String[] sourceTablePaths = { sourceTablePath1, secondSourceTablePath };
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName(getTransaction())).thenReturn("vdbDefinition");
        when(viewDef.getViewName(getTransaction())).thenReturn(viewDefinitionName);
        when(viewDef.getDescription(getTransaction())).thenReturn(description);
        when(viewDef.isComplete(getTransaction())).thenReturn(isComplete);
        when(viewDef.getSourcePaths(getTransaction())).thenReturn(sourceTablePaths);
        
        SqlComposition sqlComp1 = mock(SqlComposition.class);
        when(sqlComp1.getName(getTransaction())).thenReturn(comp1Name);
        when(sqlComp1.getDescription(getTransaction())).thenReturn(comp1Desc);
        when(sqlComp1.getLeftSourcePath(getTransaction())).thenReturn(comp1LeftSource);
        when(sqlComp1.getRightSourcePath(getTransaction())).thenReturn(comp1RightSource);
        when(sqlComp1.getLeftCriteriaColumn(getTransaction())).thenReturn(comp1LeftColumn);
        when(sqlComp1.getRightCriteriaColumn(getTransaction())).thenReturn(comp1RightColumn);
        when(sqlComp1.getType(getTransaction())).thenReturn(joinType);
        when(sqlComp1.getOperator(getTransaction())).thenReturn(comp1Operator);
        
        SqlComposition[] sqlComps = new SqlComposition[1];
        sqlComps[0] = sqlComp1;
        when(viewDef.getSqlCompositions(getTransaction())).thenReturn(sqlComps);

        return helper.getODataViewDdl(getTransaction(), viewDef);
    }
    
    private ViewEditorState[] helpCreateViewEditorState(int numSources) throws KException {

        ViewEditorState[] stateArray = new ViewEditorState[1];

        ViewEditorState state = mock(ViewEditorState.class);
        when(state.getName(getTransaction())).thenReturn(viewDefinitionName);
        stateArray[0] = state;
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(state.setViewDefinition(getTransaction())).thenReturn(viewDef);
        when(state.getViewDefinition(getTransaction())).thenReturn(viewDef);
        if( numSources == 1 ) {
        	helpCreateViewDefinition(viewDef, sourceTablePath2);
        } else {
        	helpCreateViewDefinition(viewDef, sourceTablePath3);
        }
        
        return stateArray;
    }
    
    private ViewDefinition helpCreateViewDefinition(ViewDefinition viewDef, String secondSourceTablePath) throws KException {

        String[] sourceTablePaths = { sourceTablePath1, secondSourceTablePath };
        when(viewDef.getName(getTransaction())).thenReturn("vdbDefinition");
        when(viewDef.getViewName(getTransaction())).thenReturn(viewDefinitionName);
        when(viewDef.getDescription(getTransaction())).thenReturn(description);
        when(viewDef.isComplete(getTransaction())).thenReturn(isComplete);
        when(viewDef.getSourcePaths(getTransaction())).thenReturn(sourceTablePaths);
        
        SqlComposition sqlComp1 = mock(SqlComposition.class);
        when(sqlComp1.getName(getTransaction())).thenReturn(comp1Name);
        when(sqlComp1.getDescription(getTransaction())).thenReturn(comp1Desc);
        when(sqlComp1.getLeftSourcePath(getTransaction())).thenReturn(comp1LeftSource);
        when(sqlComp1.getRightSourcePath(getTransaction())).thenReturn(comp1RightSource);
        when(sqlComp1.getLeftCriteriaColumn(getTransaction())).thenReturn(comp1LeftColumn);
        when(sqlComp1.getRightCriteriaColumn(getTransaction())).thenReturn(comp1RightColumn);
        when(sqlComp1.getType(getTransaction())).thenReturn(ViewDefinitionHelper.JOIN_INNER);
        when(sqlComp1.getOperator(getTransaction())).thenReturn(comp1Operator);
        
        SqlComposition[] sqlComps = new SqlComposition[1];
        sqlComps[0] = sqlComp1;
        when(viewDef.getSqlCompositions(getTransaction())).thenReturn(sqlComps);

        return viewDef;
    }
    
    private void printResults(String expected, String generated) {
    	if( doPrint ) {
    		System.out.println("\nViewDefinitionHelperTest\n\tEXPECTED DDL = \n" + expected);
    		System.out.println("\nViewDefinitionHelperTest\n\tGENERATED DDL = \n" + generated);
    	}
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_NoJoinOneTable() throws Exception {
    	String EXPECTED_DDL = EXPECTED_NO_JOIN_SQL_SINGE_SOURCE;
    	
    	ViewDefinitionHelper helper = new ViewDefinitionHelper(WorkspaceManager.getInstance(_repo, getTransaction()));

        String[] sourceTablePaths = { sourceTablePath1 };
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName(getTransaction())).thenReturn("vdbDefinition");
        when(viewDef.getViewName(getTransaction())).thenReturn(viewDefinitionName);
        when(viewDef.getDescription(getTransaction())).thenReturn(description);
        when(viewDef.isComplete(getTransaction())).thenReturn(isComplete);
        when(viewDef.getSourcePaths(getTransaction())).thenReturn(sourceTablePaths);

        String viewDdl = helper.getODataViewDdl(getTransaction(), viewDef);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_InnerJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ViewDefinitionHelper.JOIN_INNER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_LeftOuterJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ViewDefinitionHelper.JOIN_LEFT_OUTER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_RightOuterJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ViewDefinitionHelper.JOIN_RIGHT_OUTER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_FullOuterJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ViewDefinitionHelper.JOIN_FULL_OUTER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_InnerJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ViewDefinitionHelper.JOIN_INNER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_LeftOuterJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ViewDefinitionHelper.JOIN_LEFT_OUTER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_RightOuterJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ViewDefinitionHelper.JOIN_RIGHT_OUTER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_FullOuterJoin() throws Exception {
    	String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ViewDefinitionHelper.JOIN_FULL_OUTER);
        printResults(EXPECTED_DDL, viewDdl);
        assertThat(viewDdl, is(EXPECTED_DDL));
    }
    
    @Test
    public void shouldRefreshServiceVdb_SingleSource() throws Exception {
    	ViewEditorState[] states = helpCreateViewEditorState(1);
    	Vdb[] vdbs = WorkspaceManager.getInstance(_repo, getTransaction()).findVdbs(getTransaction());
    	
    	Vdb serviceVdb = null;
    	for( Vdb vdb : vdbs ) {
    		if( vdb.getName(getTransaction()).equals(viewDefinitionName)) {
    			serviceVdb = vdb;
    			break;
    		}
    	}
    	
    	ViewDefinitionHelper helper = new ViewDefinitionHelper(WorkspaceManager.getInstance(_repo, getTransaction()));
    	
    	helper.refreshServiceVdb(getTransaction(), serviceVdb, states);
    	
    	commit();
    	
    	Model[] models = serviceVdb.getModels(getTransaction());
    	
        assertThat(models.length, is(2));
        Model viewModel = ViewDefinitionHelper.getViewModel(getTransaction(), serviceVdb);
        assertNotNull(viewModel);
        assertThat(viewModel.getViews(getTransaction()).length, is(1));
    	View view = viewModel.getViews(getTransaction())[0];
    	assertNotNull(view);
    	assertThat(view.getColumns(getTransaction()).length, is(4));
    }
    
    @Test
    public void shouldRefreshServiceVdb_TwoSources() throws Exception {
    	ViewEditorState[] states = helpCreateViewEditorState(2);
    	Vdb[] vdbs = WorkspaceManager.getInstance(_repo, getTransaction()).findVdbs(getTransaction());
    	
    	Vdb serviceVdb = null;
    	for( Vdb vdb : vdbs ) {
    		if( vdb.getName(getTransaction()).equals(viewDefinitionName)) {
    			serviceVdb = vdb;
    			break;
    		}
    	}
    	
    	ViewDefinitionHelper helper = new ViewDefinitionHelper(WorkspaceManager.getInstance(_repo, getTransaction()));
    	
    	helper.refreshServiceVdb(getTransaction(), serviceVdb, states);
    	
    	commit();
    	
    	Model[] models = serviceVdb.getModels(getTransaction());
    	
        assertThat(models.length, is(3));
        Model viewModel = ViewDefinitionHelper.getViewModel(getTransaction(), serviceVdb);
        assertNotNull(viewModel);
        assertThat(viewModel.getViews(getTransaction()).length, is(1));
    	View view = viewModel.getViews(getTransaction())[0];
    	assertNotNull(view);
    	assertThat(view.getColumns(getTransaction()).length, is(4));
    }
}
