/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Kevin Leturc
 */
package org.nuxeo.ecm.core.storage.marklogic;

import static org.nuxeo.ecm.core.storage.dbs.DBSDocument.KEY_PRIMARY_TYPE;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.query.sql.model.Expression;
import org.nuxeo.ecm.core.query.sql.model.IntegerLiteral;
import org.nuxeo.ecm.core.query.sql.model.LiteralList;
import org.nuxeo.ecm.core.query.sql.model.MultiExpression;
import org.nuxeo.ecm.core.query.sql.model.Operator;
import org.nuxeo.ecm.core.query.sql.model.Reference;
import org.nuxeo.ecm.core.query.sql.model.SelectClause;
import org.nuxeo.ecm.core.query.sql.model.StringLiteral;
import org.nuxeo.ecm.core.storage.dbs.DBSExpressionEvaluator;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

import com.marklogic.client.query.RawQueryDefinition;

@RunWith(FeaturesRunner.class)
@Features(RuntimeFeature.class)
@Deploy("org.nuxeo.ecm.core.schema")
@LocalDeploy("org.nuxeo.ecm.core.storage.marklogic.tests:OSGI-INF/test-types-contrib.xml")
public class TestMarkLogicQueryBuilder extends AbstractTest {

    @Test
    public void testLtOperator() throws Exception {
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        Expression expression = new Expression(new Reference(NXQL.ECM_NAME), Operator.LT, new IntegerLiteral(10L));

        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/lt-operator.xml", query.getHandle().toString());
    }

    @Test
    public void testInOperator() throws Exception {
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        LiteralList inPrimaryTypes = new LiteralList();
        inPrimaryTypes.add(new StringLiteral("Document"));
        inPrimaryTypes.add(new StringLiteral("Folder"));
        Expression expression = new Expression(new Reference(KEY_PRIMARY_TYPE), Operator.IN, inPrimaryTypes);
        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/in-operator.xml", query.getHandle().toString());
    }

    @Test
    public void testIsNullOperator() throws Exception {
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        Expression expression = new Expression(new Reference(NXQL.ECM_LOCK_CREATED), Operator.ISNULL, null);

        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/is-null-operator.xml", query.getHandle().toString());
    }

    @Test
    public void testWildcardReference() throws Exception {
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        Expression expression = new Expression(new Reference("picture:views/*/title"), Operator.EQ, new StringLiteral(
                "Original"));

        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/wildcard-reference.xml", query.getHandle().toString());
    }

    @Test
    public void testSimpleArrayCondition() throws Exception {
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        Expression expression = new Expression(new Reference("dc:contributors"), Operator.EQ, new StringLiteral("bob"));

        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/simple-array-condition.xml", query.getHandle().toString());
    }

    @Test
    public void testSimpleArrayConditionWithWildcard() throws Exception {
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        Expression expression = new Expression(new Reference("dc:contributors/*"), Operator.EQ, new StringLiteral("bob"));

        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/simple-array-condition.xml", query.getHandle().toString());
    }

    @Test
    public void testCoreFeatureQuery() throws Exception {
        // Init parameters
        SelectClause selectClause = new SelectClause();
        selectClause.add(new Reference(NXQL.ECM_UUID));

        LiteralList inPrimaryTypes = new LiteralList();
        inPrimaryTypes.addAll(Stream.of("OrderedFolder", "HiddenFile", "DocWithAge", "TemplateRoot", "TestDocument2",
                "TestDocumentWithDefaultPrefetch", "SectionRoot", "Document", "Folder", "WorkspaceRoot",
                "HiddenFolder", "Section", "TestDocument", "Relation", "FolderWithSearch", "MyDocType", "Book", "Note",
                "ComplexDoc", "Domain", "File", "Workspace")
                                    .map(StringLiteral::new)
                                    .collect(Collectors.toList()));
        MultiExpression expression = new MultiExpression(Operator.AND, Collections.singletonList(new Expression(
                new Reference(KEY_PRIMARY_TYPE), Operator.IN, inPrimaryTypes)));
        // SELECT 'ecm:id' WHERE ecm:primaryType IN 'OrderedFolder', 'HiddenFile', 'DocWithAge', 'TemplateRoot',
        // 'TestDocument2', 'TestDocumentWithDefaultPrefetch', 'SectionRoot', 'Document', 'Folder', 'WorkspaceRoot',
        // 'HiddenFolder', 'Section', 'TestDocument', 'Relation', 'FolderWithSearch', 'MyDocType', 'Book', 'Note',
        // 'ComplexDoc', 'Domain', 'File', 'Workspace'
        DBSExpressionEvaluator evaluator = new DBSExpressionEvaluator(null, selectClause, expression, null, null, false);

        // Test
        RawQueryDefinition query = new MarkLogicQueryBuilder(CLIENT.newQueryManager(), evaluator.getExpression(),
                evaluator.getSelectClause(), null, evaluator.pathResolver, evaluator.fulltextSearchDisabled).buildQuery();
        assertXMLFileAgainstString("query-expression/core-feature.xml", query.getHandle().toString());
    }

}