//******************************************************************************
//                          ExperimentDTO.java
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRAE 2020
// Contact: renaud.colin@inrae.fr, anne.tireau@inrae.fr, pascal.neveu@inrae.fr
//******************************************************************************
package org.opensilex.core.experiment.api;

import io.swagger.annotations.ApiModelProperty;
import org.opensilex.core.experiment.dal.ExperimentModel;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.opensilex.server.rest.validation.Required;

/**
 * @author Renaud COLIN A basic DTO class about an {@link ExperimentModel}
 */
public abstract class ExperimentDTO {

    protected URI uri;

    protected String label;

    protected List<URI> projects = new ArrayList<>();

    protected LocalDate startDate;

    protected LocalDate endDate;

    protected String objective;

    protected String comment;

    protected Integer campaign;

    protected List<String> keywords = new ArrayList<>();

    protected List<URI> scientificSupervisors = new ArrayList<>();

    protected List<URI> technicalSupervisors = new ArrayList<>();

    protected List<URI> groups = new ArrayList<>();

    protected List<URI> infrastructures = new ArrayList<>();

    protected List<URI> installations = new ArrayList<>();

    protected List<URI> species = new ArrayList<>();

    protected Boolean isPublic;

    protected List<URI> variables = new ArrayList<>();

    protected List<URI> sensors = new ArrayList<>();

    protected List<URI> factors = new ArrayList<>();

    public URI getUri() {
        return uri;
    }

    public ExperimentDTO setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    @Required
    @ApiModelProperty(example = "ZA17")
    public String getLabel() {
        return label;
    }

    public ExperimentDTO setLabel(String label) {
        this.label = label;
        return this;
    }

    @ApiModelProperty(example = "http://www.phenome-fppn.fr/id/species/zeamays")
    public List<URI> getProjects() {
        return projects;
    }

    public ExperimentDTO setProjects(List<URI> projects) {
        this.projects = projects;
        return this;
    }

    @NotNull
    @ApiModelProperty(example = "2020-02-20")
    public LocalDate getStartDate() {
        return startDate;
    }

    public ExperimentDTO setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    @ApiModelProperty(example = "2020-02-20")
    public LocalDate getEndDate() {
        return endDate;
    }

    public ExperimentDTO setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    @ApiModelProperty(example = "objective")
    public String getObjective() {
        return objective;
    }

    public ExperimentDTO setObjective(String objective) {
        this.objective = objective;
        return this;
    }

    @ApiModelProperty(example = "comment")
    public String getComment() {
        return comment;
    }

    public ExperimentDTO setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @ApiModelProperty(example = "2020")
    public Integer getCampaign() {
        return campaign;
    }

    public ExperimentDTO setCampaign(Integer campaign) {
        this.campaign = campaign;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public ExperimentDTO setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public List<URI> getScientificSupervisors() {
        return scientificSupervisors;
    }

    public ExperimentDTO setScientificSupervisors(List<URI> scientificSupervisors) {
        this.scientificSupervisors = scientificSupervisors;
        return this;
    }

    public List<URI> getTechnicalSupervisors() {
        return technicalSupervisors;
    }

    public ExperimentDTO setTechnicalSupervisors(List<URI> technicalSupervisors) {
        this.technicalSupervisors = technicalSupervisors;
        return this;
    }

    public List<URI> getGroups() {
        return groups;
    }

    public ExperimentDTO setGroups(List<URI> groups) {
        this.groups = groups;
        return this;
    }

    @ApiModelProperty(example = "http://www.phenome-fppn.fr/id/species/zeamays")
    public List<URI> getSpecies() {
        return species;
    }

    public ExperimentDTO setSpecies(List<URI> species) {
        this.species = species;
        return this;
    }

    @ApiModelProperty(example = "true")
    public Boolean getIsPublic() {
        return isPublic;
    }

    public ExperimentDTO setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    public List<URI> getInfrastructures() {
        return infrastructures;
    }

    public ExperimentDTO setInfrastructures(List<URI> infrastructures) {
        this.infrastructures = infrastructures;
        return this;
    }

    public List<URI> getVariables() {
        return variables;
    }

    public ExperimentDTO setVariables(List<URI> variables) {
        this.variables = variables;
        return this;
    }

    public List<URI> getSensors() {
        return sensors;
    }

    public ExperimentDTO setSensors(List<URI> sensors) {
        this.sensors = sensors;
        return this;
    }

    public List<URI> getFactors() {
        return factors;
    }

    public ExperimentDTO setFactors(List<URI> factors) {
        this.factors = factors;
        return this;
    }

    public List<URI> getInstallations() {
        return installations;
    }

    public ExperimentDTO setInstallations(List<URI> installations) {
        this.installations = installations;
        return this;
    }
}
