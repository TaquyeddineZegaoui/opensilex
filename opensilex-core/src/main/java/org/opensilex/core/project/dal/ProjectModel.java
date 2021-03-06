/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.core.project.dal;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.opensilex.core.experiment.dal.ExperimentModel;
import org.opensilex.core.ontology.Oeso;
import org.opensilex.security.user.dal.UserModel;
import org.opensilex.sparql.annotations.SPARQLProperty;
import org.opensilex.sparql.annotations.SPARQLResource;
import org.opensilex.sparql.model.SPARQLResourceModel;
import org.opensilex.sparql.utils.ClassURIGenerator;

/**
 * @author Julien BONNEFONT
 */
@SPARQLResource(
        ontology = Oeso.class,
        resource = "Project",
        graph = "set/projects",
        prefix = "prj"
)
public class ProjectModel extends SPARQLResourceModel implements ClassURIGenerator<ProjectModel> {

    @SPARQLProperty(
            ontology = RDFS.class,
            property = "label"
    )
    String label;
    public static final String LABEL_FIELD = "label";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasShortname"
    )
    private String shortname;
    public static final String SHORTNAME_FIELD = "shortname";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasFinancialFunding"
    )
    private String hasFinancialFunding;
    public static final String FINANCIAL_FUNDING_FIELD = "hasFinancialFunding";

    @SPARQLProperty(
            ontology = DCTerms.class,
            property = "description"
    )
    private String description;
    public static final String DESCRIPTION_FIELD = "description";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasExperiment"
    )
    List<ExperimentModel> experiments;
    public static final String EXPERIMENT_URI_FIELD = "experiment";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasObjective"
    )
    private String objective;
    public static final String OBJECTIVE_FIELD = "objective";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "startDate"
    )
    private LocalDate startDate;
    public static final String START_DATE_FIELD = "startDate";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "endDate"
    )
    private LocalDate endDate;
    public static final String END_DATE_FIELD = "endDate";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasKeyword"
    )
    private List<String> keywords;

    @SPARQLProperty(
            ontology = FOAF.class,
            property = "homepage"
    )
    private URI homePage;
    public static final String HOMEPAGE_FIELD = "homePage";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasAdministrativeContact"
    )
    private List<UserModel> administrativeContacts;
    public static final String ADMINISTRATIVE_CONTACTS_FIELD = "administrativeContacts";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasCoordinator"
    )
    private List<UserModel> coordinators;
    public static final String COORDINATORS_FIELD = "coordinators";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasScientificContact"
    )
    private List<UserModel> scientificContacts;
    public static final String SCIENTIFIC_CONTACTS_FIELD = "scientificContacts";

    @SPARQLProperty(
            ontology = Oeso.class,
            property = "hasRelatedProject"
    )
    private List<ProjectModel> relatedProjects;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getHasFinancialFunding() {
        return hasFinancialFunding;
    }

    public void setHasFinancialFunding(String hasFinancialFunding) {
        this.hasFinancialFunding = hasFinancialFunding;
    }

    public List<ExperimentModel> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentModel> experiments) {
        this.experiments = experiments;
    }

    public List<ProjectModel> getRelatedProjects() {
        return relatedProjects;
    }

    public void setRelatedProjects(List<ProjectModel> relatedProjects) {
        this.relatedProjects = relatedProjects;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public URI getHomePage() {
        return homePage;
    }

    public void setHomePage(URI homePage) {
        this.homePage = homePage;
    }

    public List<UserModel> getAdministrativeContacts() {
        return administrativeContacts;
    }

    public void setAdministrativeContacts(List<UserModel> administrativeContacts) {
        this.administrativeContacts = administrativeContacts;
    }

    public List<UserModel> getCoordinators() {
        return coordinators;
    }

    public void setCoordinators(List<UserModel> coordinators) {
        this.coordinators = coordinators;
    }

    public List<UserModel> getScientificContacts() {
        return scientificContacts;
    }

    public void setScientificContacts(List<UserModel> scientificContacts) {
        this.scientificContacts = scientificContacts;
    }
    
    @Override
    public String[] getUriSegments(ProjectModel instance) {
        return new String[]{
                instance.getShortname()
        };
    }

}
