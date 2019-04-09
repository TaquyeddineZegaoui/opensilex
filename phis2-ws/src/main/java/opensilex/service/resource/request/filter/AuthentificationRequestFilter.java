package opensilex.service.resource.request.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.authentication.TokenManager;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.resource.DataResourceService;
import opensilex.service.view.brapi.Status;
import opensilex.service.view.brapi.form.ResponseFormGET;

/**
 * Permet de filtrer les envois au Web Service grâce au header des requêtes
 * ainsi que d'autres paramètres
 *
 * @author Arnaud Charleroy
 * @date 05/16
 * @update 10/16 brapi v1
 */
@Provider
public class AuthentificationRequestFilter implements ContainerRequestFilter {

    final static Logger LOGGER = LoggerFactory.getLogger(AuthentificationRequestFilter.class);

    @Context
    private ResourceInfo resourceInfo;
        
    /**
     * Filtre le token de session
     *
     * @param requestContext
     * @throws IOException
     */
    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        Response accessDenied = Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ResponseFormGET(
                        new Status("You cannot access this resource.", StatusCodeMsg.ERR,
                                "Invalid token")))
                .type(MediaType.APPLICATION_JSON).build();
        
        
        final UriInfo uriInfo = requestContext.getUriInfo();
        final String resourcePath = uriInfo.getPath();
//        logger.debug(resourcePath);
        // Swagger.json and token authorized
        if (resourcePath != null 
                && !resourcePath.contains("token") 
                && !resourcePath.contains("calls") 
                && !resourcePath.contains("swagger.json")
                && !(resourceInfo.getResourceClass() == DataResourceService.class && resourceInfo.getResourceMethod().getName().equals("getDataFile"))) {
            //Get request headers
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            if (headers != null && !headers.containsKey(GlobalWebserviceValues.AUTHORIZATION_PROPERTY)) {
                throw new WebApplicationException(accessDenied);
            }
            //Fetch authorization header
//            logger.debug(headers.toString());
            String authorization = requestContext.getHeaderString(GlobalWebserviceValues.AUTHORIZATION_PROPERTY);
//            logger.debug(authorization.toString());
            //If no authorization information present; block access
            if (authorization == null || authorization.isEmpty()) {
                throw new WebApplicationException(accessDenied);
            }

            Pattern authorizationPattern = Pattern.compile(GlobalWebserviceValues.AUTHENTICATION_SCHEME + " .*");
            Matcher m = authorizationPattern.matcher(authorization);

            if (!m.matches()) {
                throw new WebApplicationException(accessDenied);
            }

            //Get session id
            String userToken = authorization.replace("Bearer ", "");
//            logger.debug(authorization);
//            logger.debug(userToken);
//            logger.debug(Boolean.toString(TokenManager.Instance().checkAuthentification(userToken)));
            if (!TokenManager.Instance().checkAuthentification(userToken)) {
                throw new WebApplicationException(accessDenied);
            }
        }
    }
}
