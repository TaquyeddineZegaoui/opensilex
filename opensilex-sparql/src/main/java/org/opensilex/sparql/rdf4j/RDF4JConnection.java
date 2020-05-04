//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.sparql.rdf4j;

import java.io.StringReader;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.DescribeBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;
import org.opensilex.service.BaseService;
import org.opensilex.service.ServiceDefaultDefinition;
import org.opensilex.sparql.exceptions.SPARQLException;
import org.opensilex.sparql.service.SPARQLConnection;
import org.opensilex.sparql.service.SPARQLResult;
import org.opensilex.sparql.service.SPARQLStatement;
import org.opensilex.sparql.exceptions.SPARQLValidationException;
import org.opensilex.sparql.mapping.SPARQLClassObjectMapperIndex;
import org.opensilex.sparql.utils.SHACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vincent
 */
@ServiceDefaultDefinition(
        configClass = RDF4JConfig.class,
        configID = "rdf4j"
)
public class RDF4JConnection extends BaseService implements SPARQLConnection {

    private final static Logger LOGGER = LoggerFactory.getLogger(RDF4JConnection.class);

    private final RepositoryConnection rdf4JConnection;

    private static AtomicInteger connectionCount = new AtomicInteger(0);

    public RDF4JConnection(RepositoryConnection rdf4JConnection) {
        this.rdf4JConnection = rdf4JConnection;
        LOGGER.debug("Acquire RDF4J sparql connection: " + this.rdf4JConnection.hashCode() + " (" + RDF4JConnection.connectionCount.incrementAndGet() + ")");
    }

    private int timeout;

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public void shutdown() throws Exception {
        LOGGER.debug("Release RDF4J sparql connection: " + this.rdf4JConnection.hashCode() + " (" + RDF4JConnection.connectionCount.decrementAndGet() + ")");
        this.rdf4JConnection.close();
    }

    @Override
    public boolean executeAskQuery(AskBuilder ask) throws SPARQLException {
        try {

            BooleanQuery askQuery = rdf4JConnection.prepareBooleanQuery(QueryLanguage.SPARQL, ask.buildString());
            if (getTimeout() > 0) {
                askQuery.setMaxExecutionTime(getTimeout());
            }
            return askQuery.evaluate();
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public List<SPARQLStatement> executeDescribeQuery(DescribeBuilder describe) throws SPARQLException {
        try {
            GraphQuery describeQuery = rdf4JConnection.prepareGraphQuery(QueryLanguage.SPARQL, describe.buildString());
            if (getTimeout() > 0) {
                describeQuery.setMaxExecutionTime(getTimeout());
            }
            GraphQueryResult results = describeQuery.evaluate();

            return statementsToSPARQLResultList(results);
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public List<SPARQLStatement> executeConstructQuery(ConstructBuilder construct) throws SPARQLException {
        try {
            GraphQuery constructQuery = rdf4JConnection.prepareGraphQuery(QueryLanguage.SPARQL, construct.buildString());
            if (getTimeout() > 0) {
                constructQuery.setMaxExecutionTime(getTimeout());
            }
            GraphQueryResult results = constructQuery.evaluate();

            return statementsToSPARQLResultList(results);
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public List<SPARQLResult> executeSelectQuery(SelectBuilder select, Consumer<SPARQLResult> resultHandler) throws SPARQLException {
        try {
            TupleQuery selectQuery = rdf4JConnection.prepareTupleQuery(QueryLanguage.SPARQL, select.buildString());
            if (getTimeout() > 0) {
                selectQuery.setMaxExecutionTime(getTimeout());
            }
            TupleQueryResult results = selectQuery.evaluate();

            return bindingSetsToSPARQLResultList(results, resultHandler);
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void executeUpdateQuery(UpdateBuilder update) throws SPARQLException {
        try {
            Update updateQuery = rdf4JConnection.prepareUpdate(QueryLanguage.SPARQL, update.buildRequest().toString());
            if (getTimeout() > 0) {
                updateQuery.setMaxExecutionTime(getTimeout());
            }
            updateQuery.execute();
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void executeDeleteQuery(UpdateBuilder update) throws SPARQLException {
        try {
            Update updateQuery = rdf4JConnection.prepareUpdate(QueryLanguage.SPARQL, update.buildRequest().toString());
            if (getTimeout() > 0) {
                updateQuery.setMaxExecutionTime(getTimeout());
            }
            updateQuery.execute();
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void startTransaction() throws SPARQLException {
        try {
            rdf4JConnection.begin();
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void commitTransaction() throws SPARQLException {
        try {
            rdf4JConnection.commit();
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void rollbackTransaction(Exception ex) throws Exception {
        try {
            rdf4JConnection.rollback();
        } catch (RepositoryException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(e.getMessage());
            }
        }
    }

    @Override
    public void clearGraph(URI graph) throws SPARQLException {
        clearGraph(SimpleValueFactory.getInstance().createIRI(graph.toString()));
    }

    public void clearGraph(IRI graph) throws SPARQLException {
        try {
            rdf4JConnection.clear(graph);
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void renameGraph(URI oldGraphURI, URI newGraphURI) throws SPARQLException {

        try {
            String moveQuery = "MOVE <" + oldGraphURI + "> TO <" + newGraphURI + ">";
            Update renameQuery = rdf4JConnection.prepareUpdate(QueryLanguage.SPARQL, moveQuery);
            if (getTimeout() > 0) {
                renameQuery.setMaxExecutionTime(getTimeout());
            }
            renameQuery.execute();

        } catch (UpdateExecutionException | RepositoryException | MalformedQueryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public List<SPARQLStatement> getGraphStatement(URI graph) throws SPARQLException {
        try {
            RepositoryResult<Statement> results = rdf4JConnection.getStatements(null, null, null, SimpleValueFactory.getInstance().createIRI(graph.toString()));

            return repoStatementsToSPARQLResultList(results);
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void clear() throws SPARQLException {
        try {
            rdf4JConnection.clear();
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    private List<SPARQLStatement> statementsToSPARQLResultList(QueryResult<Statement> queryResults) {
        List<SPARQLStatement> resultList = new LinkedList<>();

        while (queryResults.hasNext()) {
            Statement result = queryResults.next();
            resultList.add(new RDF4JStatement(result));
        }

        queryResults.close();
        return resultList;
    }

    private List<SPARQLStatement> repoStatementsToSPARQLResultList(RepositoryResult<Statement> queryResults) {
        List<SPARQLStatement> resultList = new LinkedList<>();

        while (queryResults.hasNext()) {
            Statement result = queryResults.next();
            resultList.add(new RDF4JStatement(result));
        }

        queryResults.close();
        return resultList;
    }

    private List<SPARQLResult> bindingSetsToSPARQLResultList(QueryResult<BindingSet> queryResults, Consumer<SPARQLResult> resultHandler) {
        List<SPARQLResult> resultList = new LinkedList<>();

        while (queryResults.hasNext()) {
            RDF4JResult result = new RDF4JResult(queryResults.next());
            if (resultHandler != null) {
                resultHandler.accept(result);
            }

            resultList.add(result);
        }

        queryResults.close();
        return resultList;
    }

    private SPARQLValidationException convertRDF4JSHACLException(ShaclSailValidationException validationEx) {
        SPARQLValidationException exception = new SPARQLValidationException();
        validationEx.getValidationReport().getValidationResult().forEach((validationResult) -> {
            URI invalidObject = null;
            URI invalidObjectProperty = null;
            URI brokenConstraint = null;
            for (Statement stmt : validationResult.asModel()) {
                try {
                    if (stmt.getPredicate().toString().equals(SHACL.focusNode.toString())) {
                        invalidObject = new URI(stmt.getObject().stringValue());
                    } else if (stmt.getPredicate().toString().equals(SHACL.resultPath.toString())) {
                        invalidObjectProperty = new URI(stmt.getObject().stringValue());
                    } else if (stmt.getPredicate().toString().equals(SHACL.sourceConstraintComponent.toString())) {
                        brokenConstraint = new URI(stmt.getObject().stringValue());
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Unexpected error while analyzing SHACL exception", ex);
                }

            }
            if (invalidObject != null && invalidObjectProperty != null && brokenConstraint != null) {
                exception.addValidationError(invalidObject, invalidObjectProperty, brokenConstraint);
            }

        });
        return exception;
    }

    @Deprecated
    public RepositoryConnection getRepositoryConnectionImpl() {
        return rdf4JConnection;
    }

    private boolean shaclEnabled = false;

    @Override
    public void disableSHACL() throws SPARQLException {
        try {
            clearGraph(RDF4J.SHACL_SHAPE_GRAPH);
            shaclEnabled = false;
        } catch (RepositoryException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ShaclSailValidationException) {
                throw convertRDF4JSHACLException((ShaclSailValidationException) cause);
            } else {
                throw new SPARQLException(ex.getMessage());
            }
        }
    }

    @Override
    public void enableSHACL() throws SPARQLException {
        rdf4JConnection.begin();
        clearGraph(RDF4J.SHACL_SHAPE_GRAPH);

        for (Class<?> c : getMapperIndex().getResourceClasses()) {
            try {
                String shaclTTL = SHACL.generateSHACL(c, getMapperIndex());
                if (shaclTTL != null) {
                    LOGGER.debug("Generated SHACL for: " + c.getCanonicalName() + "\n" + shaclTTL);
                    rdf4JConnection.add(new StringReader(shaclTTL), "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
                } else {
                    LOGGER.debug("No SHACL Validation for: " + c.getCanonicalName() + "\n" + shaclTTL);
                }
            } catch (Exception ex) {
                LOGGER.warn("Error while loading SHACL for: " + c.getCanonicalName(), ex);
            }
        }

        rdf4JConnection.commit();
        shaclEnabled = true;

    }

    @Override
    public boolean isShaclEnabled() {
        return shaclEnabled;
    }

    private SPARQLClassObjectMapperIndex mapperIndex;

    @Override
    public SPARQLClassObjectMapperIndex getMapperIndex() {
        return mapperIndex;
    }

    @Override
    public void setMapperIndex(SPARQLClassObjectMapperIndex mapperIndex) {
        this.mapperIndex = mapperIndex;
    }
}
