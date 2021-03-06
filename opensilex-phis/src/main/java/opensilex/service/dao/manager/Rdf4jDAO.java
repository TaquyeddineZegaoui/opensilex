//******************************************************************************
//                              Rdf4jDAO.java 
// SILEX-PHIS
// Copyright © INRA 2016
// Creation date: Aug 2016
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao.manager;

import opensilex.service.PropertiesFileManager;
import opensilex.service.configuration.DateFormat;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.URINamespaces;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.dao.exception.ResourceAccessDeniedException;
import opensilex.service.ontology.Rdf;
import opensilex.service.ontology.Rdfs;
import opensilex.service.utils.sparql.SPARQLQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.update.UpdateRequest;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.opensilex.sparql.rdf4j.RDF4JConnection;
import org.opensilex.sparql.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class to query the triplestore
 *
 * @update [Morgane Vidal] 04 Oct, 2018: Rename existObject to existUri and
 * change the query of the method existUri.
 * @update [Andréas Garcia] 11 Jan, 2019: Add generic date time stamp comparison
 * SparQL filter.
 * @update [Andréas Garcia] 5 March, 2019: Move date related functions in
 * TimeDAO.java Add a generic function to get a string value from a binding set
 * Add the max value of a page (to get all results of a service)
 * @param <T>
 * @author Arnaud Charleroy
 */
public abstract class Rdf4jDAO<T> extends DAO<T> {

    private final static int TIMEOUT = 20;

    final static Logger LOGGER = LoggerFactory.getLogger(Rdf4jDAO.class);

    final private String REPOSITORY_EXCEPTION_GENERIC_MESSAGE_FORMAT
            = "Error while committing or rolling back triplestore statements: %s";
    final private String MALFORMED_QUERY_EXCEPTION_MESSAGE_FORMAT = "Malformed query: %s";
    final private String QUERY_EVALUATION_EXCEPTION_MESSAGE_FORMAT = "Error evaluating the query: %s";
    final private String UPDATE_EXECUTION_EXCEPTION_MESSAGE_FORMAT = "Error executing the update query: %s";
    final private String COUNT_VALUE_PARSING_EXCEPTION_MESSAGE_FORMAT
            = "Error parsing value of " + COUNT_ELEMENT_QUERY + "from binding set";

    protected static final String PROPERTY_FILENAME = "sesame_rdf_config";

    /**
     * Page size max value used to get the highest number of results of an
     * object when getting a list within a list (e.g to get all the concerned
     * items of all the events) //SILEX:todo Pagination should be handled in
     * this case too (i.e when getting a list within a list) For the moment we
     * use only one page by taking the max value //\SILEX:todo
     */
    protected int pageSizeMaxValue = Integer.parseInt(PropertiesFileManager
            .getConfigFileProperty("service", "pageSizeMax"));

    // used for logger
    protected static final String SPARQL_QUERY = "SPARQL query: ";

    protected static final String COUNT_ELEMENT_QUERY = "count";

    /**
     * The following constants are SPARQL variables name used for each subclass
     * to query the triplestore.
     */
    protected static final String URI = "uri";
    protected static final String URI_SELECT_NAME_SPARQL = "?" + URI;
    protected static final String RDF_TYPE = "rdfType";
    protected static final String RDF_TYPE_SELECT_NAME_SPARQL = "?" + RDF_TYPE;
    protected static final String LABEL = "label";
    protected static final String COMMENT = "comment";
    protected static final String OBJECT = "object";
    protected static final String OBJECT_SELECT_NAME_SPARQL = "?" + OBJECT;
    protected static final String PROPERTY = "property";
    protected static final String PROPERTY_SELECT_NAME_SPARQL = "?" + PROPERTY;
    protected static final String SUBJECT = "subject";
    protected static final String SUBJECT_SELECT_NAME_SPARQL = "?" + SUBJECT;
    protected static final String SEE_ALSO = "subject";
    protected static final String SEE_ALSO_SELECT_NAME_SPARQL = "?" + SEE_ALSO;

    protected static final String DATETIMESTAMP_FORMAT_SPARQL = DateFormat.YMDTHMSZZ.toString();

    // Triplestore relations
    protected static final URINamespaces ONTOLOGIES = new URINamespaces();

    protected Integer page;
    protected Integer pageSize;
    protected final SPARQLService sparql;

    public Rdf4jDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }

    /**
     * Brapi API page starts at 0
     *
     * @return current page number
     */
    public Integer getPage() {
        if (page == null || pageSize < 0) {
            return 0;
        }
        return page;
    }

    /**
     * Brapi page to be used for pagination in database
     *
     * @return current page number + 1
     */
    public Integer getPageForDBQuery() {
        if (page == null || pageSize < 0) {
            return 1;
        }
        return page + 1;
    }

    public void setPage(Integer page) {
        if (page < 0) {
            this.page = Integer.valueOf(DefaultBrapiPaginationValues.PAGE);
        }
        this.page = page;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 0) {
            return Integer.valueOf(DefaultBrapiPaginationValues.PAGE_SIZE);
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Executes an update request from an update builder.
     *
     * @param updateBuilder
     * @throws opensilex.service.dao.exception.DAOPersistenceException
     */
    protected void executeUpdateRequest(UpdateBuilder updateBuilder) throws DAOPersistenceException {
        try {
            UpdateRequest query = updateBuilder.buildRequest();
            LOGGER.debug(SPARQL_QUERY + " " + query.toString());
            prepareRDF4JUpdateQuery(query).execute();
        } catch (JenaException | RDF4JException ex) {
            handleTriplestoreException(ex);
        }
    }

    /**
     * Checks if a subject exists by triplet.
     *
     * @param subject
     * @param predicate
     * @param object
     * @return boolean
     */
    public boolean exist(String subject, String predicate, String object)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        boolean exist = false;
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendSelect(null);
        query.appendTriplet(subject, predicate, object, null);
        query.appendParameters("LIMIT 1");
        TupleQuery tupleQuery = prepareRDF4JTupleQuery(query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                exist = true;
            }
        }
        return exist;
    }

    /**
     * Check if a given URI exist in the triplestore.
     *
     * @param uri the uri to test
     * @example ASK { VALUES (?r) {
     * (<http://www.w3.org/2000/01/rdf-schema#Literal>) } { ?r ?p ?o } UNION {
     * ?s ?r ?o } UNION { ?s ?p ?r } }
     * @return true if the URI exist in the triplestore false if it does not
     * exist
     */
    public boolean existUri(String uri) throws MalformedQueryException, QueryEvaluationException, RepositoryException {
        if (uri == null) {
            return false;
        }
        try {
            //SILEX:warning
            //Remember to add rdf, rdfs and owl ontologies in your triplestore
            //\SILEX:warning
            SPARQLQueryBuilder query = new SPARQLQueryBuilder();
            query.appendAsk("VALUES (?r) { (<" + uri + ">) }\n"
                    + "    { ?r ?p ?o }\n"
                    + "    UNION\n"
                    + "    { ?s ?r ?o }\n"
                    + "    UNION\n"
                    + "    { ?s ?p ?r }\n");

            LOGGER.debug(SPARQL_QUERY + query.toString());
            BooleanQuery booleanQuery = prepareRDF4JBooleanQuery(query);
            return booleanQuery.evaluate();
        } catch (MalformedQueryException | QueryEvaluationException | RepositoryException e) {
            throw (e);
        }
    }

    /**
     * Check if a given URI exist in a given Graph in the triplestore.
     *
     * @param uri the uri to test
     * @param graph
     * @example ASK FROM <http://www.mygraph.com> { VALUES (?r) {
     * (<http://www.w3.org/2000/01/rdf-schema#Literal>) } { ?r ?p ?o } UNION {
     * ?s ?r ?o } UNION { ?s ?p ?r } }
     * @return true if the uri exist in the graph false if it does not exist
     */
    public boolean existUriInGraph(String uri, String graph) {
        if (uri == null) {
            return false;
        }
        if (graph == null) {
            return false;
        }
        try {
            String query = "ASK \n"
                    + "  FROM <" + graph + "> {\n"
                    + "\n"
                    + " VALUES (?r) { (<" + uri + ">) }\n"
                    + " { ?r ?p ?o }\n"
                    + " UNION\n"
                    + " { ?s ?r ?o }\n"
                    + " UNION\n"
                    + " { ?s ?p ?r }\n"
                    + "  \n"
                    + "}";

            LOGGER.debug(SPARQL_QUERY + query);
            BooleanQuery booleanQuery = prepareRDF4JBooleanQuery(query);
            return booleanQuery.evaluate();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Recovers the existence of an element by triplet.
     *
     * @param subject
     * @param predicate
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public String getValueFromPredicate(String subject, String predicate) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String value = null;
        if (subject != null || predicate != null) {
            SPARQLQueryBuilder query = new SPARQLQueryBuilder();
            query.appendSelect("?x");
            query.appendTriplet(subject, predicate, "?x", null);
            query.appendParameters("LIMIT 1");
            LOGGER.trace(query.toString());
            TupleQuery tupleQuery = prepareRDF4JTupleQuery(query);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    value = result.next().getBinding("x").getValue().stringValue();
                }
            }
            LOGGER.trace(value);
        }
        return value;
    }

    /**
     * Adds object properties to a given object.
     *
     * @param subjectUri the subject URI which will have the object properties
     * URIs.
     * @param predicateUri the URI of the predicates
     * @param objectPropertiesUris the list of the object properties to link to
     * the subject
     * @param graphUri
     * @example INSERT DATA { GRAPH <http://www.phenome-fppn.fr/diaphen/sensors>
     * {
     * <http://www.phenome-fppn.fr/diaphen/2018/s18533>
     * <http://www.opensilex.org/vocabulary/oeso#measures>
     * <http://www.phenome-fppn.fr/id/variables/v001> . }}
     * @return true if the insertion has been done false if an error occurred
     * (see the error logs to get more details)
     */
    protected boolean addObjectProperties(String subjectUri, String predicateUri, List<String> objectPropertiesUris, String graphUri) {
        //Generates insert query
        UpdateBuilder spql = new UpdateBuilder();
        Node graph = NodeFactory.createURI(graphUri);

        objectPropertiesUris.forEach((objectProperty) -> {
            Node subjectUriNode = NodeFactory.createURI(subjectUri);
            Node predicateUriNode = NodeFactory.createURI(predicateUri);
            Node objectPropertyNode = NodeFactory.createURI(objectProperty);

            spql.addInsert(graph, subjectUriNode, predicateUriNode, objectPropertyNode);
        });

        LOGGER.debug(SPARQL_QUERY + spql.toString());

        //Insert the properties in the triplestore
        Update prepareUpdate = prepareRDF4JUpdateQuery(spql.build());
        try {
            prepareUpdate.execute();
        } catch (UpdateExecutionException ex) {
            LOGGER.error("Add object properties error : " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Deletes the given object properties.
     *
     * @param subjectUri
     * @param predicateUri
     * @param objectPropertiesUris
     * @example DELETE WHERE {
     * <http://www.phenome-fppn.fr/diaphen/2018/s18533>
     * <http://www.opensilex.org/vocabulary/oeso#measures>
     * <http://www.phenome-fppn.fr/id/variables/v001> . }
     * @return true if the object properties have been deleted false if the
     * delete has not been done.
     */
    protected boolean deleteObjectProperties(String subjectUri, String predicateUri, List<String> objectPropertiesUris) {
        //1. Generates delete query
        UpdateBuilder query = new UpdateBuilder();

        Resource subject = ResourceFactory.createResource(subjectUri);
        Property predicate = ResourceFactory.createProperty(predicateUri);

        for (String objectProperty : objectPropertiesUris) {
            Node object = NodeFactory.createURI(objectProperty);
            query.addWhere(subject, predicate, object);
        }

        UpdateDeleteWhere request = query.buildDeleteWhere();
        LOGGER.debug(request.toString());

        //2. Delete data in the triplestore
        Update prepareDelete = prepareRDF4JUpdateQuery(request);
        try {
            prepareDelete.execute();
        } catch (UpdateExecutionException ex) {
            LOGGER.error("Delete object properties error : " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Gets the value of a name in the SELECT statement from a binding set.
     *
     * @param selectName
     * @param bindingSet
     * @return the string value of the "selectName" variable in the binding set.
     */
    protected static String getStringValueOfSelectNameFromBindingSet(String selectName, BindingSet bindingSet) {
        Value selectedFieldValue = bindingSet.getValue(selectName);
        if (selectedFieldValue != null) {
            return selectedFieldValue.stringValue();
        }
        return null;
    }

    /**
     * Handle a NumberFormatException when getting the value of the count of
     * results.
     *
     * @param ex
     * @throws opensilex.service.dao.exception.DAOPersistenceException
     */
    protected void handleCountValueNumberFormatException(NumberFormatException ex) throws Exception {
        throw new Exception(String.format(COUNT_VALUE_PARSING_EXCEPTION_MESSAGE_FORMAT, COUNT_ELEMENT_QUERY), ex);
    }

    /**
     * Handle a RDF4J exception throwing a DAO persistence exception according
     * to the given exception type.
     *
     * @param exception
     * @throws opensilex.service.dao.exception.DAOPersistenceException
     */
    protected void handleTriplestoreException(RuntimeException exception) throws DAOPersistenceException {
        String daoPersistenceExceptionMessage;
        if (exception instanceof RepositoryException) {
            daoPersistenceExceptionMessage
                    = String.format(REPOSITORY_EXCEPTION_GENERIC_MESSAGE_FORMAT, exception.getMessage());
        } else if (exception instanceof MalformedQueryException) {
            daoPersistenceExceptionMessage
                    = String.format(MALFORMED_QUERY_EXCEPTION_MESSAGE_FORMAT, exception.getMessage());
        } else if (exception instanceof QueryEvaluationException) {
            daoPersistenceExceptionMessage
                    = String.format(QUERY_EVALUATION_EXCEPTION_MESSAGE_FORMAT, exception.getMessage());
        } else if (exception instanceof UpdateExecutionException) {
            daoPersistenceExceptionMessage
                    = String.format(UPDATE_EXECUTION_EXCEPTION_MESSAGE_FORMAT, exception.getMessage());
        } else {
            daoPersistenceExceptionMessage = DAOPersistenceException.GENERIC_MESSAGE + " " + exception.getMessage();
        }

        LOGGER.error(exception.getMessage(), exception);
        throw new DAOPersistenceException(daoPersistenceExceptionMessage, exception);
    }

    /**
     * Get the list of URIs corresponding to the given label (like).
     *
     * @param rdfType
     * @example SELECT DISTINCT ?uri ?label WHERE { ?uri
     * <http://www.w3.org/2000/01/rdf-schema#label> ?label . ?uri
     * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?rdfType . ?rdfType
     * <http://www.w3.org/2000/01/rdf-schema#subClassOf>
     *
     * <http://www.opensilex.org/vocabulary/oeso#ScientificObject> . FILTER (
     * (REGEX ( str(?label),".*2.*","i")) ) }
     * @param label
     * @return the list of URIs and labels
     */
    public Map<String, List<String>> findUriAndLabelsByLabelAndRdfType(String label, String rdfType) {
        //1. Generate the query
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendSelect("?" + URI + " ?" + LABEL);
        query.appendDistinct(Boolean.TRUE);
        query.appendTriplet("?" + URI, Rdfs.RELATION_LABEL.toString(), "?" + LABEL, null);
        query.appendAndFilter("REGEX ( str(?" + LABEL + "),\".*" + label + ".*\",\"i\")");
        query.appendTriplet("?" + URI, Rdf.RELATION_TYPE.toString(), "?" + RDF_TYPE, null);
        query.appendTriplet("?" + RDF_TYPE, "<" + Rdfs.RELATION_SUBCLASS_OF + ">*", rdfType, null);
        LOGGER.debug(query.toString());

        Map<String, List<String>> urisAndLabels = new HashMap<>();

        //2. Get the result of the query
        TupleQuery tupleQuery = prepareRDF4JTupleQuery(query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                if (urisAndLabels.containsKey(bindingSet.getValue(URI).stringValue())) {
                    List<String> labels = urisAndLabels.get(bindingSet.getValue(URI).stringValue());
                    labels.add(bindingSet.getValue(LABEL).stringValue());
                    urisAndLabels.put(bindingSet.getValue(URI).stringValue(), labels);
                } else {
                    List<String> labels = new ArrayList<>();
                    labels.add(bindingSet.getValue(LABEL).stringValue());
                    urisAndLabels.put(bindingSet.getValue(URI).stringValue(), labels);
                }
            }
        }

        return urisAndLabels;
    }

    /**
     * Get the list of labels for a given uri.
     *
     * @param uri
     * @example SELECT DISTINCT ?label WHERE {
     * <http://www.opensilex.org/opensilex/2019/o19000060>
     * <http://www.w3.org/2000/01/rdf-schema#label> ?label . }
     * @return the list of labels.
     */
    public List<String> findLabelsForUri(String uri) {
        //1. Generate the query
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendSelect("?" + LABEL);
        query.appendDistinct(Boolean.TRUE);
        query.appendTriplet(uri, Rdfs.RELATION_LABEL.toString(), "?" + LABEL, null);
        LOGGER.debug(query.toString());

        List<String> labels = new ArrayList<>();

        //2. Get the result of the query
        TupleQuery tupleQuery = prepareRDF4JTupleQuery(query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                labels.add(bindingSet.getValue(LABEL).stringValue());
            }
        }

        return labels;
    }

    /**
     * Delete a list of objects into the triplestore.
     *
     * @param uris : a {@link List} of objects Uris
     * @throws Exception
     * @throws RepositoryException
     * @throws UpdateExecutionException
     */
    protected void deleteAll(List<String> uris) throws Exception, RepositoryException, UpdateExecutionException {

    }

    /**
     * Delete a list of objects into the triplestore.
     *
     * @param uris : a {@link Iterable} over objects Uris
     * @throws IllegalArgumentException if the {@link #user} is not an admin
     * user or if a given uri is not present into the TripleStore.
     * @throws DAOPersistenceException : if an {@link Exception} related to the
     * {@link Repository} is encountered.
     * @throws Exception : for any other encountered {@link Exception}
     * @see #deleteAll(List)
     */
    public void checkAndDeleteAll(List<String> uris) throws IllegalArgumentException, DAOPersistenceException, Exception {

        if (user == null || StringUtils.isEmpty(user.getAdmin())) {
            throw new IllegalArgumentException("No user/bad user provided");
        }

        StringBuilder errorMsgs = new StringBuilder();
        boolean allUriExists = true;
        for (String uri : uris) {
            if (!existUri(uri)) {
                errorMsgs.append(uri + " , ");
                allUriExists = false;
            }
        }
        if (!allUriExists) {
            throw new IllegalArgumentException(errorMsgs.append(" don't belongs to the TripleStore").toString());
        }

        Exception returnedException = null;
        try {
            sparql.startTransaction();
            deleteAll(uris);
            sparql.commitTransaction();
        } catch (RepositoryException | UpdateExecutionException e) {
            sparql.rollbackTransaction();
            returnedException = new DAOPersistenceException(e);
        } catch (Exception e) {
            sparql.rollbackTransaction();
            returnedException = e;
        } finally {
            if (returnedException != null) {
                throw returnedException;
            }
        }
    }

    private synchronized RepositoryConnection getConnection() {
        return sparql.getRepositoryConnection();
    }

    public TupleQuery prepareRDF4JTupleQuery(Object query) {
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        tupleQuery.setMaxExecutionTime(TIMEOUT);
        return tupleQuery;
    }

    public Update prepareRDF4JUpdateQuery(Object query) {
        Update update = getConnection().prepareUpdate(QueryLanguage.SPARQL, query.toString());
        update.setMaxExecutionTime(TIMEOUT);
        return update;
    }

    public BooleanQuery prepareRDF4JBooleanQuery(Object query) {
        BooleanQuery booleanQuery = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
        booleanQuery.setMaxExecutionTime(TIMEOUT);
        return booleanQuery;
    }

    public RepositoryResult<Namespace> getNamespaces() {
        RepositoryResult<Namespace> result = getConnection().getNamespaces();
        return result;
    }

    /**
     * Validates and creates objects.
     *
     * @param objects
     * @return the annotations created.
     * @throws opensilex.service.dao.exception.DAOPersistenceException
     * @throws opensilex.service.dao.exception.DAODataErrorAggregateException
     * @throws opensilex.service.dao.exception.ResourceAccessDeniedException
     */
    public List<T> validateAndCreate(List<T> objects)
            throws DAOPersistenceException, DAODataErrorAggregateException, ResourceAccessDeniedException, Exception {
        validate(objects);
        List<T> objectsCreated;
        try {
            sparql.startTransaction();
            objectsCreated = create(objects);
            sparql.commitTransaction();
        } catch (Exception ex) {
            sparql.rollbackTransaction();
            throw ex;
        }
        return objectsCreated;
    }

    /**
     * Validates and updates objects.
     *
     * @param objects
     * @return the objects created.
     * @throws opensilex.service.dao.exception.DAOPersistenceException
     * @throws opensilex.service.dao.exception.DAODataErrorAggregateException
     * @throws opensilex.service.dao.exception.ResourceAccessDeniedException
     */
    public List<T> validateAndUpdate(List<T> objects)
            throws DAOPersistenceException, DAODataErrorAggregateException, ResourceAccessDeniedException, Exception {
        validate(objects);
        List<T> objectsUpdated;
        try {
            sparql.startTransaction();
            objectsUpdated = update(objects);
            sparql.commitTransaction();
        } catch (Exception ex) {
            sparql.rollbackTransaction();
            throw ex;
        }
        return objectsUpdated;
    }

}
