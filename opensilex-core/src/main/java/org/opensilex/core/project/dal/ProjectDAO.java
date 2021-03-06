//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.project.dal;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.opensilex.sparql.model.SPARQLResourceModel;
import org.opensilex.sparql.service.SPARQLQueryHelper;
import org.opensilex.sparql.service.SPARQLService;
import org.opensilex.utils.OrderBy;
import org.opensilex.utils.ListWithPagination;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.opensilex.core.experiment.dal.ExperimentModel;
import org.opensilex.core.ontology.Oeso;
import org.opensilex.security.authentication.ForbiddenURIAccessException;
import org.opensilex.security.authentication.NotFoundURIException;
import org.opensilex.security.group.dal.GroupModel;
import org.opensilex.security.user.dal.UserModel;
import org.opensilex.sparql.deserializer.SPARQLDeserializers;
import static org.opensilex.sparql.service.SPARQLQueryHelper.makeVar;

/**
 * @author vidalmor
 */
public class ProjectDAO {

    protected final SPARQLService sparql;

    public ProjectDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }

    public ProjectModel create(ProjectModel instance) throws Exception {
        sparql.create(instance);
        return instance;
    }

    public ProjectModel update(ProjectModel instance, UserModel user) throws Exception {
        validateProjectAccess(instance.getUri(), user);
        sparql.update(instance);
        return instance;
    }

    @Deprecated
    public void update(List<ProjectModel> instances) throws Exception {
        sparql.update(instances);
    }

    public void delete(URI uri, UserModel user) throws Exception {
        validateProjectAccess(uri, user);
        sparql.delete(ProjectModel.class, uri);
    }

    public ProjectModel get(URI uri, UserModel user) throws Exception {
        validateProjectAccess(uri, user);
        return sparql.getByURI(ProjectModel.class, uri, user.getLanguage());
    }

    @Deprecated
    public ListWithPagination<ProjectModel> search(URI uri,
            String name, String shortname, String description, String startDate, String endDate, URI homePage, String objective,
            List<OrderBy> orderByList, Integer page, Integer pageSize, String lang) throws Exception {

        List<Expr> filterList = new ArrayList<>();

        // append uri regex filter
        if (uri != null) {
            Var uriVar = makeVar(SPARQLResourceModel.URI_FIELD);
            Expr strUriExpr = SPARQLQueryHelper.getExprFactory().str(uriVar);
            filterList.add(SPARQLQueryHelper.regexFilter(strUriExpr, uri.toString(), null));
        }
        if (homePage != null) {
            Var homepageVar = makeVar(ProjectModel.HOMEPAGE_FIELD);
            Expr strUriExpr = SPARQLQueryHelper.getExprFactory().str(homepageVar);
            filterList.add(SPARQLQueryHelper.regexFilter(strUriExpr, homePage.toString(), null));
        }

        // append regex filter
        filterList.add(SPARQLQueryHelper.regexFilter(ProjectModel.LABEL_FIELD, name));

        filterList.add(SPARQLQueryHelper.regexFilter(ProjectModel.SHORTNAME_FIELD, shortname));
        filterList.add(SPARQLQueryHelper.regexFilter(ProjectModel.DESCRIPTION_FIELD, description));
        filterList.add(SPARQLQueryHelper.regexFilter(ProjectModel.OBJECTIVE_FIELD, objective));

        // append date filters
        if (!StringUtils.isEmpty(startDate)) {
            filterList.add(SPARQLQueryHelper.eq(ProjectModel.START_DATE_FIELD, LocalDate.parse(startDate)));
        }
        if (!StringUtils.isEmpty(endDate)) {
            filterList.add(SPARQLQueryHelper.eq(ProjectModel.END_DATE_FIELD, LocalDate.parse(endDate)));
        }

        return sparql.searchWithPagination(
                ProjectModel.class,
                lang,
                (SelectBuilder select) -> {
                    filterList.stream().filter(Objects::nonNull).forEach(select::addFilter);
                },
                orderByList,
                page,
                pageSize
        );
    }

    public void create(List<ProjectModel> instances) throws Exception {
        sparql.create(instances);
    }

    public ListWithPagination<ProjectModel> search(String label, String financialFunding, LocalDate startDate, LocalDate endDate, UserModel user, List<OrderBy> orderByList, int page, int pageSize) throws Exception {

        Expr stringFilter = SPARQLQueryHelper.or(
                SPARQLQueryHelper.regexFilter(ProjectModel.SHORTNAME_FIELD, label),
                SPARQLQueryHelper.regexFilter(ProjectModel.LABEL_FIELD, label)
        );

        Expr financialFundingFilter = SPARQLQueryHelper.regexFilter(ProjectModel.FINANCIAL_FUNDING_FIELD, financialFunding);

        Expr dateFilter = SPARQLQueryHelper.intervalDateRange(ProjectModel.START_DATE_FIELD, startDate, ProjectModel.END_DATE_FIELD, endDate);

        return sparql.searchWithPagination(
                ProjectModel.class,
                null,
                (SelectBuilder select) -> {
                    if (stringFilter != null) {
                        select.addFilter(stringFilter);
                    }

                    if (financialFundingFilter != null) {
                        select.addFilter(financialFundingFilter);
                    }

                    if (dateFilter != null) {
                        select.addFilter(dateFilter);
                    }

                    appendUserProjectsFilter(select, user);
                },
                orderByList,
                page,
                pageSize
        );
    }

    private void appendUserProjectsFilter(SelectBuilder select, UserModel user) throws Exception {
        if (user == null || user.isAdmin()) {
            return;
        }

        Var uriVar = makeVar(ExperimentModel.URI_FIELD);

        Node userNodeURI = SPARQLDeserializers.nodeURI(user.getUri());

        Var coordinatorVar = makeVar(ProjectModel.COORDINATORS_FIELD);
        select.addOptional(new Triple(uriVar, Oeso.hasCoordinator.asNode(), coordinatorVar));
        Expr hasCoordinator = SPARQLQueryHelper.eq(coordinatorVar, userNodeURI);

        Var scientificSupervisorVar = makeVar(ProjectModel.SCIENTIFIC_CONTACTS_FIELD);
        select.addOptional(new Triple(uriVar, Oeso.hasScientificContact.asNode(), scientificSupervisorVar));
        Expr hasScientificContact = SPARQLQueryHelper.eq(scientificSupervisorVar, userNodeURI);

        Var technicalSupervisorVar = makeVar(ProjectModel.ADMINISTRATIVE_CONTACTS_FIELD);
        select.addOptional(new Triple(uriVar, Oeso.hasAdministrativeContact.asNode(), technicalSupervisorVar));
        Expr hasAdministrativeContact = SPARQLQueryHelper.eq(technicalSupervisorVar, userNodeURI);

        select.addFilter(SPARQLQueryHelper.or(
                hasCoordinator,
                hasScientificContact,
                hasAdministrativeContact
        ));
    }

    public void validateProjectAccess(URI projectURI, UserModel user) throws Exception {
        if (!sparql.uriExists(ProjectModel.class, projectURI)) {
            throw new NotFoundURIException(projectURI);
        }

        if (user.isAdmin()) {
            return;
        }

        Node userNodeURI = SPARQLDeserializers.nodeURI(user.getUri());
        Var uriVar = makeVar(ExperimentModel.URI_FIELD);

        AskBuilder ask = sparql.getUriExistsQuery(ProjectModel.class, projectURI);

        Var coordinatorVar = makeVar(ProjectModel.COORDINATORS_FIELD);
        ask.addOptional(new Triple(uriVar, Oeso.hasCoordinator.asNode(), coordinatorVar));
        Expr hasCoordinator = SPARQLQueryHelper.eq(coordinatorVar, userNodeURI);

        Var scientificSupervisorVar = makeVar(ProjectModel.SCIENTIFIC_CONTACTS_FIELD);
        ask.addOptional(new Triple(uriVar, Oeso.hasScientificContact.asNode(), scientificSupervisorVar));
        Expr hasScientificContact = SPARQLQueryHelper.eq(scientificSupervisorVar, userNodeURI);

        Var technicalSupervisorVar = makeVar(ProjectModel.ADMINISTRATIVE_CONTACTS_FIELD);
        ask.addOptional(new Triple(uriVar, Oeso.hasAdministrativeContact.asNode(), technicalSupervisorVar));
        Expr hasAdministrativeContact = SPARQLQueryHelper.eq(technicalSupervisorVar, userNodeURI);

        ask.addFilter(
                SPARQLQueryHelper.or(
                        hasCoordinator,
                        hasScientificContact,
                        hasAdministrativeContact
                )
        );

        if (!sparql.executeAskQuery(ask)) {
            throw new ForbiddenURIAccessException(projectURI);
        }
    }

    public List<ProjectModel> getList(List<URI> uris, UserModel user) throws Exception {
        return sparql.getListByURIs(ProjectModel.class, uris, user.getLanguage());
    }
}
