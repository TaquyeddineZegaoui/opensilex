//******************************************************************************
//                        InfrastructureResourceService.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 5 Sept. 2018
// Contact: vincent.migot@inra.fr anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.dao.InfrastructureDAO;
import opensilex.service.dao.PropertyDAO;
import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.ontology.Oeso;
import opensilex.service.resource.dto.infrastructure.InfrastructureDTO;
import opensilex.service.resource.dto.rdfResourceDefinition.RdfResourceDefinitionDTO;
import opensilex.service.resource.validation.interfaces.Required;
import opensilex.service.resource.validation.interfaces.URL;
import opensilex.service.view.brapi.Status;
import opensilex.service.result.ResultForm;
import opensilex.service.model.Infrastructure;
import org.opensilex.security.authentication.ApiProtected;
import org.opensilex.sparql.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure resource service.
 * @update [Andréas Garcia] 15 Apr. 2019: handle DAO persistence exceptions thrown by property DAO functions.
 * @author Vincent Migot <vincent.migot@inra.fr>
 */
//@Api("/infrastructures")
//@Path("/infrastructures")
public class InfrastructureResourceService extends ResourceService {

    final static Logger LOGGER = LoggerFactory.getLogger(InfrastructureResourceService.class);

    @Inject
    private SPARQLService sparql;

    /**
     * Searches infrastructures by URI, rdfType.
     *
     * @param pageSize
     * @param page
     * @param uri
     * @param rdfType
     * @param label
     * @param language
     * @return list of the infrastructures corresponding to the search parameters given
     * @example
     * {
     *      "metadata": {
     *          "pagination": {
     *              "pageSize": 20,
     *              "currentPage": 0,
     *              "totalCount": 3,
     *              "totalPages": 1
     *          },
     *          "status": [],
     *          "datafiles": []
     *      },
     *      "result": {
     *          "data": [
     *              {
     *                  "uri": "http://www.phenome-fppn.fr",
     *                  "rdfType": "http://www.phenome-fppn.fr/vocabulary/2018/oepo#NationalInfrastructure",
     *                  "label": "alias",
     *                  "properties": []
     *              },
     *          ]
     *      }
     * }
     */
    @GET
    @ApiOperation(value = "Get all infrastructures corresponding to the search params given",
            notes = "Retrieve all infrastructures authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all infrastructures", response = Infrastructure.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiProtected
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfrastructuresBySearch(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
            @ApiParam(value = "Search by uri", example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_URI) @QueryParam("uri") @URL String uri,
            @ApiParam(value = "Search by type uri", example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_RDF_TYPE) @QueryParam("rdfType") @URL String rdfType,
            @ApiParam(value = "Search by label", example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_LABEL) @QueryParam("label") String label,
            @ApiParam(value = "Language", example = DocumentationAnnotation.EXAMPLE_LANGUAGE) @QueryParam("language") String language
    ) throws Exception {
        // 1. Initialize infrastructureDAO with parameters
        InfrastructureDAO infrastructureDAO = new InfrastructureDAO(sparql);

        if (uri != null) {
            infrastructureDAO.uri = uri;
        }
        if (rdfType != null) {
            infrastructureDAO.rdfType = rdfType;
        }
        if (label != null) {
            infrastructureDAO.label = label;
        }
        if (language == null) {
            language = DEFAULT_LANGUAGE;
        }
        infrastructureDAO.language = language;

        infrastructureDAO.user = userSession.getUser();
        infrastructureDAO.setPage(page);
        infrastructureDAO.setPageSize(pageSize);

        // 2. Get infrastructures count
        Integer totalCount = infrastructureDAO.count();

        // 3. Get infrastructure page list
        ArrayList<Infrastructure> infrastructures = infrastructureDAO.allPaginate();

        // 4. Initialize return variables
        ArrayList<Status> statusList = new ArrayList<>();
        ArrayList<RdfResourceDefinitionDTO> list = new ArrayList<>();
        ResultForm<RdfResourceDefinitionDTO> getResponse;

        if (infrastructures == null) {
            // Request failure
            getResponse = new ResultForm<>(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else if (infrastructures.isEmpty()) {
            // No results
            getResponse = new ResultForm<>(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else {
            // Convert all Infrastructure object to DTO's
            infrastructures.forEach((infrastructure) -> {
                list.add(new InfrastructureDTO(infrastructure));
            });

            // Return list of DTO
            getResponse = new ResultForm<>(infrastructureDAO.getPageSize(), infrastructureDAO.getPage(), list, true, totalCount);
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }
    }

    /**
     * Searches infrastructure details for a given URI
     *
     * @param language
     * @param pageSize
     * @param page
     * @param uri
     * @return list of the infrastructure's detail corresponding to the search uri
     * @example
     * {
     *   "metadata": {
     *     "pagination": null,
     *     "status": [],
     *     "datafiles": []
     *   },
     *   "result": {
     *     "data": [
     *       {
     *         "uri": "http://www.phenome-fppn.fr/diaphen",
     *         "properties": [
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
     *             "value": "http://www.opensilex.org/vocabulary/oeso#Installation"
     *           },
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.w3.org/2000/01/rdf-schema#label",
     *             "value": "DIAPHEN"
     *           },
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.opensilex.org/vocabulary/oeso#hasPart",
     *             "value": "http://www.phenome-fppn.fr/diaphen/ea1"
     *           },
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.opensilex.org/vocabulary/oeso#hasPart",
     *             "value": "http://www.phenome-fppn.fr/diaphen/ef1"
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * }
     */
    @GET
    @Path("{uri}")
    @ApiOperation(value = "Get all infrastructure's details corresponding to the search uri",
            notes = "Retrieve all infrastructure's details authorized for the user corresponding to the searched uri")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve infrastructure's details", response = RdfResourceDefinitionDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiProtected
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfrastructureDetails(
            @ApiParam(value = DocumentationAnnotation.INFRASTRUCTURE_URI_DEFINITION, required = true, example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_URI) @PathParam("uri") @URL @Required String uri,
            @ApiParam(value = "Language", example = DocumentationAnnotation.EXAMPLE_LANGUAGE) @QueryParam("language") String language,
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page) throws Exception {
        // 1. Initialize propertyDAO with parameters
        PropertyDAO propertyDAO = new PropertyDAO(sparql);

        propertyDAO.setSubClassOf(Oeso.CONCEPT_INFRASTRUCTURE);

        if (language == null) {
            language = DEFAULT_LANGUAGE;
        }

        propertyDAO.user = userSession.getUser();
        propertyDAO.setPage(page);
        propertyDAO.setPageSize(pageSize);

        // 2. Initialize result variable
        ArrayList<Status> statusList = new ArrayList<>();
        ResultForm<RdfResourceDefinitionDTO> getResponse;
        ArrayList<RdfResourceDefinitionDTO> list = new ArrayList<>();

        // Get all properties in the given language and fill them in infrastructure object
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setUri(uri);
        try {
            if (propertyDAO.getAllPropertiesWithLabels(infrastructure, language)) {
                // Convert the infrastructure to an InfrastructureDTO
                list.add(new InfrastructureDTO(infrastructure));

                // Return it
                getResponse = new ResultForm<>(propertyDAO.getPageSize(), propertyDAO.getPage(), list, true, list.size());
                getResponse.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(getResponse).build();
            } else {
                // No result found
                getResponse = new ResultForm<>(0, 0, list, true, 0);
                return noResultFound(getResponse, statusList);
            }
        } catch (DAOPersistenceException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return getResponseWhenPersistenceError(ex);
        }
    }
}
