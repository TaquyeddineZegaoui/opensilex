package org.opensilex.core.variable.api;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URI;

import io.swagger.annotations.ApiModelProperty;
import org.opensilex.core.ontology.SKOSReferencesDTO;
import org.opensilex.core.variable.dal.EntityModel;
import org.opensilex.core.variable.dal.MethodModel;
import org.opensilex.core.variable.dal.QualityModel;
import org.opensilex.core.variable.dal.UnitModel;
import org.opensilex.core.variable.dal.VariableModel;
import org.opensilex.sparql.response.NamedResourceDTO;


/**
 *
 * @author Renaud COLIN
 */
public class VariableDetailsDTO extends SKOSReferencesDTO {

    private URI uri;

    private String name;

    private String longName;

    private String comment;

    private NamedResourceDTO<EntityModel> entity;

    private NamedResourceDTO<QualityModel> quality;

    private NamedResourceDTO<MethodModel> method;

    private URI traitUri;

    private String traitName;

    private NamedResourceDTO<UnitModel> unit;

    private String synonym;

    private String dimension;

    @ApiModelProperty(example = "http://opensilex.dev/set/variables/Plant_Height")
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @ApiModelProperty(example = "Plant_Height")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(example = "Plant_Height_Estimation_Cm")
    public String getLongName() { return longName; }

    public void setLongName(String longName) { this.longName = longName; }

    @ApiModelProperty(example = "Describe the height of a plant.")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public NamedResourceDTO<EntityModel> getEntity() {
        return entity;
    }

    public void setEntity(NamedResourceDTO<EntityModel> entity) {
        this.entity = entity;
    }

    public NamedResourceDTO<QualityModel> getQuality() {
        return quality;
    }

    public void setQuality(NamedResourceDTO<QualityModel> quality) { this.quality = quality; }

    @ApiModelProperty(notes = "Additional trait URI. Could be used for interoperability", example = "http://purl.obolibrary.org/obo/TO_0002644")
    public URI getTraitUri() { return traitUri; }

    public void setTraitUri(URI traitUri) { this.traitUri = traitUri; }

    @ApiModelProperty(notes = "Additional trait name. Could be used for interoperability if you describe the trait URI", example = "dry matter digestibility")
    public String getTraitName() { return traitName; }

    public void setTraitName(String traitName) { this.traitName = traitName; }

    public NamedResourceDTO<MethodModel> getMethod() { return method; }

    public void setMethod(NamedResourceDTO<MethodModel> method) { this.method = method; }

    public NamedResourceDTO<UnitModel> getUnit() {
        return unit;
    }

    public void setUnit(NamedResourceDTO<UnitModel> unit) {
        this.unit = unit;
    }

    @ApiModelProperty(example = "Plant_Length")
    public String getSynonym() {
        return synonym;
    }

    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }

    @ApiModelProperty(notes = "Describe the dimension on which variables values are integrated", example = "minutes")
    public String getDimension() { return dimension; }

    public void setDimension(String dimension) { this.dimension = dimension; }

    public static VariableDetailsDTO fromModel(VariableModel model) {
        VariableDetailsDTO dto = new VariableDetailsDTO();

        dto.setUri(model.getUri());
        dto.setName(model.getName());
        dto.setComment(model.getComment());
        dto.setLongName(model.getLongName());
        dto.setSynonym(model.getSynonym());
        dto.setDimension(model.getDimension());

        dto.setTraitUri(model.getTraitUri());
        dto.setTraitName(model.getTraitName());

        NamedResourceDTO<EntityModel> entityDto = new NamedResourceDTO<>();
        EntityModel entity = model.getEntity();
        entityDto.setUri(entity.getUri());
        entityDto.setName(entity.getName());
        dto.setEntity(entityDto);

        NamedResourceDTO<QualityModel> qualityDto = new NamedResourceDTO<>();
        QualityModel quality = model.getQuality();
        qualityDto.setUri(quality.getUri());
        qualityDto.setName(quality.getName());
        dto.setQuality(qualityDto);

        NamedResourceDTO<MethodModel> methodDto = new NamedResourceDTO<>();
        MethodModel method = model.getMethod();
        methodDto.setUri(method.getUri());
        methodDto.setName(method.getName());
        dto.setMethod(methodDto);

        NamedResourceDTO<UnitModel> unitDto = new NamedResourceDTO<>();
        UnitModel unit = model.getUnit();
        unitDto.setUri(unit.getUri());
        unitDto.setName(unit.getName());
        dto.setUnit(unitDto);

        dto.setSkosReferencesFromModel(model);
        return dto;
    }
}

