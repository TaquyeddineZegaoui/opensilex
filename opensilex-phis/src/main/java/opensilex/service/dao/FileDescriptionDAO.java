//******************************************************************************
//                              FileDescriptionDAO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 7 March 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import com.jcraft.jsch.SftpException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import opensilex.service.PropertiesFileManager;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import org.bson.BSONObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.configuration.DateFormat;
import opensilex.service.dao.manager.MongoDAO;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.ontology.Oeso;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.utils.UriGenerator;
import opensilex.service.view.brapi.Status;
import opensilex.service.model.ConcernedItem;
import opensilex.service.model.FileDescription;
import org.opensilex.fs.service.FileStorageService;
import org.opensilex.sparql.service.SPARQLService;

/**
 * File descriptions DAO.
 * @author Vincent Migot <vincent.migot@inra.fr>
 */
public class FileDescriptionDAO extends MongoDAO<FileDescription> {

    private final SPARQLService sparql;

    public FileDescriptionDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }
    private final static Logger LOGGER = LoggerFactory.getLogger(FileDescriptionDAO.class);

    private final static String DB_FIELD_URI = "uri";
    private final static String DB_FIELD_DATE = "date";
    private final static String DB_FIELD_PROVENANCE = "provenanceUri";
    private final static String DB_FIELD_RDF_TYPE = "rdfType";
    private final static String DB_FIELD_CONCERNED_ITEM_URI = "concernedItems.uri";

    /**
     * Prepares and returns the data file description search query with the given parameters
     * @param rdfType
     * @param startDate
     * @param endDate
     * @param provenanceUri
     * @param jsonValueFilter
     * @param concernedItems
     * @param dateSortAsc
     * @return The data file description search query
     * @example
     *  {
     *      "date": {
     *          $gte: ISODate("2010-06-15T10:51:00+0200"),
     *          $lt: ISODate("2018-06-15T10:51:00+0200")
     *      },
     *      "rdfType": "http://www.phenome-fppn.fr/diaphen/id/variable/v0000001",
     *      "provenance": "http://www.phenome-fppn.fr/mtp/2018/pv181515071552"
     *  }
     */
    protected BasicDBObject prepareSearchQuery(
        String rdfType,
        String startDate,
        String endDate,
        String provenanceUri,
        String jsonValueFilter,
        List<String> concernedItems,
        boolean dateSortAsc
    ) {
        BasicDBObject query = new BasicDBObject();

        try {
            // Define date filter depending if start date and/or end date are defined
            if (startDate != null) {
                Date start = DateFormat.parseDateOrDateTime(startDate, false);

                if (endDate != null) {
                    // In case of start date AND end date defined
                    Date end = DateFormat.parseDateOrDateTime(endDate, true);
                    query.append(DB_FIELD_DATE, BasicDBObjectBuilder.start("$gte", start).add("$lte", end).get());
                } else {
                    // In case of start date ONLY is defined
                    query.append(DB_FIELD_DATE, BasicDBObjectBuilder.start("$gte", start).get());
                }
            } else if (endDate != null) {
                // In case of end date ONLY is defined
                Date end = DateFormat.parseDateOrDateTime(endDate, true);
                query.append(DB_FIELD_DATE, BasicDBObjectBuilder.start("$lte", end).get());
            }
        } catch (ParseException ex) {
            LOGGER.error("Invalid date format", ex);
        }

        // Add filter if a provenance uri is defined
        if (provenanceUri != null) {
            query.append(DB_FIELD_PROVENANCE, provenanceUri);
        }

        if (concernedItems != null && concernedItems.size() > 0) {
            BasicDBList concernedItemsIds = new BasicDBList();
            concernedItemsIds.addAll(concernedItems);
            DBObject inFilter = new BasicDBObject("$in", concernedItemsIds);
            query.append(DB_FIELD_CONCERNED_ITEM_URI, inFilter);
        }

        query.append(DB_FIELD_RDF_TYPE, rdfType);

        if (jsonValueFilter != null) {
            query.putAll((BSONObject) BasicDBObject.parse(jsonValueFilter));
        }

        LOGGER.debug(getTraceabilityLogs() + " query : " + query.toString());

        return query;
    }

    /**
     * Searches all data files descriptions corresponding to the search parameters.
     * @param rdfType
     * @param startDate
     * @param endDate
     * @param provenanceUri
     * @param jsonValueFilter
     * @param concernedItems
     * @param dateSortAsc
     * @return
     */
    public ArrayList<FileDescription> search(
        String rdfType,
        String startDate,
        String endDate,
        String provenanceUri,
        String jsonValueFilter,
        List<String> concernedItems,
        boolean dateSortAsc
    ) {
         // Get the collection corresponding to rdf type uri
        String typeCollection = this.getCollectionFromFileType(rdfType);
        MongoCollection<FileDescription> dataVariableCollection = database.getCollection(typeCollection, FileDescription.class);

        // Get the filter query
        BasicDBObject query = prepareSearchQuery(
            rdfType,
            startDate,
            endDate,
            provenanceUri,
            jsonValueFilter,
            concernedItems,
            dateSortAsc
        );

        // Get paginated documents
        FindIterable<FileDescription> fileDescription = dataVariableCollection.find(query);

        //SILEX:info
        // Results are always sort by date, either ascending or descending depending on dateSortAsc parameter
        //If dateSortAsc=true, sort by date ascending
        //If dateSortAsc=false, sort by date descending
        //\SILEX:info
        if (dateSortAsc) {
            fileDescription = fileDescription.sort(Sorts.ascending(DB_FIELD_DATE));
        } else {
            fileDescription = fileDescription.sort(Sorts.descending(DB_FIELD_DATE));
        }

        // Define pagination for the request
        fileDescription = fileDescription.skip(page * pageSize).limit(pageSize);
        ArrayList<FileDescription> dataList = new ArrayList<>();

        // For each document, create a data Instance and add it to the result list
        try (MongoCursor<FileDescription> cursor = fileDescription.iterator()) {
            while (cursor.hasNext()) {
                dataList.add(cursor.next());
            }
        }

        return dataList;
    }

    /**
     * Gets data count according to the parameters given.
     * @param rdfType
     * @param startDate
     * @param endDate
     * @param provenanceUri
     * @param jsonValueFilter
     * @param concernedItems
     * @param dateSortAsc
     * @return the data count
     */
    public long count(
        String rdfType,
        String startDate,
        String endDate,
        String provenanceUri,
        String jsonValueFilter,
        List<String> concernedItems,
        boolean dateSortAsc
    ) {
        String typeCollection = this.getCollectionFromFileType(rdfType);
        MongoCollection<Document> dataVariableCollection = database.getCollection(typeCollection);

        // Get the filter query
        BasicDBObject query = prepareSearchQuery(
            rdfType,
            startDate,
            endDate,
            provenanceUri,
            jsonValueFilter,
            concernedItems,
            dateSortAsc
        );

        // Return the document count
        return dataVariableCollection.countDocuments(query);
    }

    /**
     * Checks and inserts data file and its metadata.
     * @param fileDescription
     * @param file
     * @return true if file and its metadata are saved
     *         false in case of errors
     */
    public POSTResultsReturn checkAndInsert(FileDescription fileDescription, File file, FileStorageService fs) {
        POSTResultsReturn checkResult = check(fileDescription);
        if (checkResult.getDataState()) {
            return insert(fileDescription, file, fs);
        } else { //Errors in the data
            return checkResult;
        }
    }

    /**
     * Check a given list of FileDescription
     * @param fileDescriptions
     * @return the check result
     */
    private POSTResultsReturn checkList(List<FileDescription> fileDescriptions) {
        boolean dataOk = true;
        List<Status> checkStatus = new ArrayList<>();
        for (FileDescription fileDescription : fileDescriptions) {
            POSTResultsReturn check = check(fileDescription);
            if (!check.getDataState()) {
                checkStatus.addAll(check.getStatusList());
                dataOk = false;
            }
        }
        POSTResultsReturn result = new POSTResultsReturn(dataOk, null, dataOk);
        result.statusList = checkStatus;
        return result;
    }

    /**
     * Inserts the given data in the MongoDB database.
     * @param dataList
     * @return the insertion result
     */
    private POSTResultsReturn insertWithWebPath(List<FileDescription> fileDescriptions) {
        // 1. Initialize transaction
        MongoClient client = MongoDAO.getMongoClient();
        POSTResultsReturn result;
        try (ClientSession session = client.startSession()) {
            session.startTransaction();
            result = null;
            List<Status> status = new ArrayList<>();
            List<String> createdResources = new ArrayList<>();
            boolean hasError = false;

            for (FileDescription fileDescription : fileDescriptions) {
                // 2. Create unique index on uri for file rdf type collection
                //   Mongo won't create index if it already exists
                IndexOptions indexOptions = new IndexOptions().unique(true);
                String fileCollectionName = getCollectionFromFileType(fileDescription.getRdfType());
                MongoCollection<FileDescription> fileDescriptionCollection = database.getCollection(fileCollectionName, FileDescription.class);
                fileDescriptionCollection.createIndex(new BasicDBObject(DB_FIELD_URI, 1), indexOptions);

                try {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    String key = Long.toString(timestamp.getTime());
                    String uri = UriGenerator.generateNewInstanceUri(sparql, Oeso.CONCEPT_DATA_FILE.toString(), fileCollectionName, key);
                    while (uriExists(fileDescription.getRdfType(), uri)) {
                        uri = UriGenerator.generateNewInstanceUri(sparql, Oeso.CONCEPT_DATA_FILE.toString(), fileCollectionName, key);
                    }

                    fileDescription.setUri(uri);

                    //3. Insert metadata first
                    fileDescriptionCollection.insertOne(session, fileDescription);
                    createdResources.add(fileDescription.getUri());
                    status.add(new Status(StatusCodeMsg.RESOURCES_CREATED, StatusCodeMsg.INFO, ""));
                } catch (MongoException ex) {
                    // Define that an error occurs
                    hasError = true;

                    // Error check if it's because of a duplicated data error
                    // Add status according to the error type (duplication or unexpected)
                    if (ex.getCode() == MongoDAO.DUPLICATE_KEY_ERROR_CODE) {
                        status.add(new Status(
                                StatusCodeMsg.ALREADY_EXISTING_DATA,
                                StatusCodeMsg.ERR,
                                ex.getMessage()
                        ));
                    } else {
                        // Add the original exception message for debugging
                        status.add(new Status(
                                StatusCodeMsg.UNEXPECTED_ERROR,
                                StatusCodeMsg.ERR,
                                StatusCodeMsg.DATA_REJECTED + " for the file " + fileDescription.getWebPath() + " - " + ex.getMessage()
                        ));
                    }
                } catch (SftpException ex) {
                    status.add(new Status(
                            StatusCodeMsg.UNEXPECTED_ERROR,
                            StatusCodeMsg.ERR,
                            "An error occurred during file upload, try to submit it again: " + ex.getMessage()
                    ));
                    hasError = true;
                } catch (Exception ex) {
                    status.add(new Status(
                            StatusCodeMsg.UNEXPECTED_ERROR,
                            StatusCodeMsg.ERR,
                            "Unexpected exception, try to submit it again: " + ex.getMessage()
                    ));
                    hasError = true;
                }
            }   // 5. Prepare result to return
            result = new POSTResultsReturn(hasError);
            result.statusList = status;
            if (!hasError) {
                // If no errors commit transaction
                session.commitTransaction();
                result.setHttpStatus(Response.Status.CREATED);
                result.createdResources = createdResources;
            } else {
                // If errors abort transaction
                session.abortTransaction();
                result.setHttpStatus(Response.Status.INTERNAL_SERVER_ERROR);
            }
            // Close transaction session
        }
        return result;
    }

    /**
     * Check and insert the given file descriptions.
     * @param fileDescriptions
     * @return the insertion result.
     */
    public POSTResultsReturn checkAndInsertWithWebPath(List<FileDescription> fileDescriptions) {
        POSTResultsReturn checkResult = checkList(fileDescriptions);
        if (checkResult.getDataState()) {
            return insertWithWebPath(fileDescriptions);
        } else { //Errors in the data
            return checkResult;
        }
    }

    /**
     * Checks the given data file description.
     * @param fileDescription
     * @return the check result with the founded errors
     */
    private POSTResultsReturn check(FileDescription fileDescription) {
        POSTResultsReturn checkResult;
        List<Status> checkStatus = new ArrayList<>();

        boolean dataOk = true;

        ProvenanceDAO provenanceDAO = new ProvenanceDAO(sparql);
        ScientificObjectRdf4jDAO scientificObjectDao = new ScientificObjectRdf4jDAO(sparql);

        // 1. Check if the provenance uri exist and is a provenance
        if (!provenanceDAO.existProvenanceUri(fileDescription.getProvenanceUri())) {
            dataOk = false;
            checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR,
                "Unknwon provenance : " + fileDescription.getProvenanceUri()));
        }

        // 2. Check if the rdf type uri exist, 
        // we use scientificObjectDao for convenience to access existUri method        
        if (!scientificObjectDao.existUri(fileDescription.getRdfType())) {
            dataOk = false;
            checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR,
                "Unknwon file rdf type : " + fileDescription.getRdfType()));
        }
        // 3. Check concerned items consistency
        for (ConcernedItem concernedItem : fileDescription.getConcernedItems()) {
            if (!scientificObjectDao.existScientificObject(concernedItem.getUri())) {
                dataOk = false;
                checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR,
                    "Unknwon concerned item : " + concernedItem.getUri()));
            }
            if (!scientificObjectDao.existUri(concernedItem.getRdfType())) {
                dataOk = false;
                checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR,
                    "Unknwon concerned item type : " + concernedItem.getUri()));
            }
        }

        checkResult = new POSTResultsReturn(dataOk, null, dataOk);
        checkResult.statusList = checkStatus;
        return checkResult;
    }

    /**
     * Inserts the given data in the MongoDB database
     * @param dataList
     * @return the insertion result
     */
    private POSTResultsReturn insert(FileDescription fileDescription, File file, FileStorageService fs) {
        // 1. Initialize transaction
        MongoClient client = MongoDAO.getMongoClient();
        ClientSession session = client.startSession();
        session.startTransaction();

        POSTResultsReturn result = null;
        List<Status> status = new ArrayList<>();
        List<String> createdResources = new ArrayList<>();

        // 2. Create unique index on uri for file rdf type collection
        //   Mongo won't create index if it already exists
        IndexOptions indexOptions = new IndexOptions().unique(true);
        String fileCollectionName = getCollectionFromFileType(fileDescription.getRdfType());
        MongoCollection<FileDescription> fileDescriptionCollection = database.getCollection(fileCollectionName, FileDescription.class);
        fileDescriptionCollection.createIndex(new BasicDBObject(DB_FIELD_URI, 1), indexOptions);

        boolean hasError = false;

        try {
//            Path storageBasePath = fs.getStorageBasePath();
            Path fileStorageDirectory = Paths.get(fs.getStorageBasePath().toString(),fileCollectionName).toAbsolutePath();

//            String fileStorageDirectory = PropertiesFileManager.getConfigFileProperty("service", "uploadFileServerDirectory");
//            final String fileServerDirectory =  Paths.get(fileStorageDirectory + '/' + fileCollectionName + "/").toFile().getAbsolutePath();

            String key = fileDescription.getFilename() + fileDescription.getDate();
            String uri = UriGenerator.generateNewInstanceUri(sparql, Oeso.CONCEPT_DATA_FILE.toString(), fileCollectionName, key);
            while (uriExists(fileDescription.getRdfType(), uri)) {
                uri = UriGenerator.generateNewInstanceUri(sparql, Oeso.CONCEPT_DATA_FILE.toString(), fileCollectionName, key);
            }

            fileDescription.setUri(uri);

            final String filename = Base64.getEncoder().encodeToString(fileDescription.getUri().getBytes());
            fileDescription.setPath(fileStorageDirectory.toString() + filename);

            //3. Insert metadata first
            fileDescriptionCollection.insertOne(session, fileDescription);

            //4. Copy file to directory
            fs.createDirectories(fileStorageDirectory);
//          fs.createDirectories(Paths.get(fileServerDirectory));

            try {
                fs.writeFile(Paths.get(fileDescription.getPath()), file);

                status.add(new Status(
                    StatusCodeMsg.RESOURCES_CREATED,
                    StatusCodeMsg.INFO,
                    StatusCodeMsg.DATA_INSERTED + " for the file " + fileDescription.getFilename()
                ));

                createdResources.add(fileDescription.getUri());
            } catch (Exception ex) {
                status.add(new Status(
                        StatusCodeMsg.UNEXPECTED_ERROR,
                        StatusCodeMsg.ERR,
                        "An error occurred during file upload, try to submit it again."
                ));
                hasError = true;
            }

        } catch (MongoException ex) {
            // Define that an error occurs
            hasError = true;

            // Error check if it's because of a duplicated data error
            // Add status according to the error type (duplication or unexpected)
            if (ex.getCode() == MongoDAO.DUPLICATE_KEY_ERROR_CODE) {
                status.add(new Status(
                        StatusCodeMsg.ALREADY_EXISTING_DATA,
                        StatusCodeMsg.ERR,
                        ex.getMessage()
                ));
            } else {
                // Add the original exception message for debugging
                status.add(new Status(
                        StatusCodeMsg.UNEXPECTED_ERROR,
                        StatusCodeMsg.ERR,
                        StatusCodeMsg.DATA_REJECTED + " for the file " + file.getName() + " - " + ex.getMessage()
                ));
            }
        } catch (SftpException ex) {
            status.add(new Status(
                    StatusCodeMsg.UNEXPECTED_ERROR,
                    StatusCodeMsg.ERR,
                    "An error occurred during file upload, try to submit it again: " + ex.getMessage()
            ));
            hasError = true;
        } catch (Exception ex) {
            status.add(new Status(
                    StatusCodeMsg.UNEXPECTED_ERROR,
                    StatusCodeMsg.ERR,
                    "Unexpected exception, try to submit it again: " + ex.getMessage()
            ));
            hasError = true;
        }

        // 5. Prepare result to return
        result = new POSTResultsReturn(hasError);
        result.statusList = status;

        if (!hasError) {
            // If no errors commit transaction
            session.commitTransaction();
            result.setHttpStatus(Response.Status.CREATED);
            result.createdResources = createdResources;
        } else {
            // If errors abort transaction
            session.abortTransaction();
            result.setHttpStatus(Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Close transaction session
        session.close();
        return result;
    }

    /**
     * Returns collection name from file RDF type URI.
     * @example http://www.opensilex.org/vocabulary/oeso#HemisphericalImage -> HemisphericalImage
     * @param rdfType
     * @return The name of the collection
     */
    private String getCollectionFromFileType(String rdfType) {
        String[] split = rdfType.split("#");
        return split[split.length - 1];
    }

      /**
     * Returns true if the given URI already exists in variable collection
     * @param rdfType
     * @param uri URI to check
     * @return true if the URI exists and false otherwise
     */
    public boolean uriExists(String rdfType, String uri) {
        String variableCollection = getCollectionFromFileType(rdfType);

        BasicDBObject query = new BasicDBObject();
        query.append(DB_FIELD_URI, uri);

        return database.getCollection(variableCollection).countDocuments(query) > 0;
    }

    /**
     * Get file description corresponding to the given URI
     * @param fileUri
     * @return The file description or null if it doesn't exists
     */
    public FileDescription findFileDescriptionByUri(String fileUri) {
        BasicDBObject query = new BasicDBObject();
        query.append(DB_FIELD_URI, fileUri);

        String[] uriParts = fileUri.split("/");
        String collection = uriParts[uriParts.length - 2];
        MongoCollection<FileDescription> datafileCollection = database.getCollection(collection, FileDescription.class);

        // Get paginated documents
        FindIterable<FileDescription> dataMongo = datafileCollection.find(query).limit(1);

        return dataMongo.first();
    }

    @Override
    protected BasicDBObject prepareSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<FileDescription> create(List<FileDescription> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<FileDescription> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<FileDescription> update(List<FileDescription> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileDescription find(FileDescription object) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileDescription findById(String id) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validate(List<FileDescription> objects) throws DAOPersistenceException, DAODataErrorAggregateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
