//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.ontology;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.opensilex.sparql.utils.Ontology;

/**
 * @author Vincent MIGOT
 */
public class Oeso {

    public static final String DOMAIN = "http://www.opensilex.org/vocabulary/oeso";

    public static final String PREFIX = "vocabulary";

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String NS = DOMAIN + "#";

    /**
     * The namespace of the vocabulary as a string
     *
     * @return namespace as String
     * @see #NS
     */
    public static String getURI() {
        return NS;
    }

    /**
     * Vocabulary namespace
     */
    public static final Resource NAMESPACE = Ontology.resource(NS);

    // ---- COMMON PROPERTIES ----
    public static final Property startDate = Ontology.property(NS, "startDate");
    public static final Property endDate = Ontology.property(NS, "endDate");
    public static final Property hasKeyword = Ontology.property(NS, "hasKeyword");
    public static final Property hasPart = Ontology.property(NS, "hasPart");
    public static final Property hasLongName = Ontology.property(NS, "hasLongName");
    public static final Property hasSynonym = Ontology.property(NS, "hasSynonym");


    // ---- VARIABLES ----
    public static final Resource Variable = Ontology.resource(NS, "Variable");
    public static final Resource Entity = Ontology.resource(NS, "Entity");
    public static final Resource Quality = Ontology.resource(NS, "Quality");
    public static final Resource Method = Ontology.resource(NS, "Method");
    public static final Resource Unit = Ontology.resource(NS, "Unit");

    public static final Property hasEntity = Ontology.property(NS, "hasEntity");
    public static final Property hasQuality = Ontology.property(NS, "hasQuality");
    public static final Property hasTraitUri = Ontology.property(NS,"hasTraitUri");
    public static final Property hasTraitName = Ontology.property(NS,"hasTraitName");

    public static final Property hasMethod = Ontology.property(NS, "hasMethod");
    public static final Property hasUnit = Ontology.property(NS, "hasUnit");

    // ---- VARIABLES UNIT ----
    public static final Property hasDimension = Ontology.property(NS, "hasDimension");
    public static final Property hasSymbol = Ontology.property(NS, "hasSymbol");
    public static final Property hasAlternativeSymbol = Ontology.property(NS, "hasAlternativeSymbol");

    // ----- USERS ------
    public static final Resource ScientificSupervisor = Ontology.resource(NS, "ScientificSupervisor");
    public static final Resource TechnicalSupervisor = Ontology.resource(NS, "TechnicalSupervisor");

    // ---- PROJECTS ----
    public static final Resource Project = Ontology.resource(NS, "Project");

    public static final Property hasShortname = Ontology.property(NS, "hasShortname");
    public static final Property hasObjective = Ontology.property(NS, "hasObjective");
    public static final Property hasExperiment = Ontology.property(NS, "hasExperiment");
    public static final Property hasAdministrativeContact = Ontology.property(NS, "hasAdministrativeContact");
    public static final Property hasCoordinator = Ontology.property(NS, "hasCoordinator");
    public static final Property hasScientificContact = Ontology.property(NS, "hasScientificContact");
    public static final Property hasRelatedProject = Ontology.property(NS, "hasRelatedProject");
    public static final Property hasFinancialFunding = Ontology.property(NS, "hasFinancialFunding");

    // ---- EXPERIMENTS ----
    public static final Resource Experiment = Ontology.resource(NS, "Experiment");

    public static final Property hasDevice = Ontology.property(NS, "hasDevice");
    public static final Property hasInfrastructure = Ontology.property(NS, "hasInfrastructure");
    public static final Property hasProject = Ontology.property(NS, "hasProject");
    public static final Property hasScientificSupervisor = Ontology.property(NS, "hasScientificSupervisor");
    public static final Property hasTechnicalSupervisor = Ontology.property(NS, "hasTechnicalSupervisor");
    public static final Property hasCampaign = Ontology.property(NS, "hasCampaign");
    public static final Property hasSpecies = Ontology.property(NS, "hasSpecies");
    public static final Property isPublic = Ontology.property(NS, "isPublic");
    public static final Property measures = Ontology.property(NS, "measures");
    public static final Property participatesIn = Ontology.property(NS, "participatesIn");

    // ---- INFRASTRUCTURES AND INSTALLATION
    public static final Resource Infrastructure = Ontology.resource(NS, "Infrastructure");
    public static final Resource InfrastructureFacility = Ontology.resource(NS, "InfrastructureFacility");
    public static final Resource InfrastructureTeam = Ontology.resource(NS, "InfrastructureTeam");
    public static final Resource Installation = Ontology.resource(NS, "Installation");
    public static final Property hasFacility = Ontology.property(NS, "hasFacility");

    // ---- SPECIES ----
    //public static final Resource Species = Ontology.resource(NS, "Species");
    // ---- FACTORS ----
    public static final Resource Factor = Ontology.resource(NS, "Factor");
    public static final Resource FactorLevel = Ontology.resource(NS, "FactorLevel");
    public static final Property hasFactorLevel = Ontology.property(NS, "hasFactorLevel");
    public static final Property hasFactor = Ontology.property(NS, "hasFactor");
    public static final Property hasCategory = Ontology.property(NS, "hasCategory");
    
    // Link with experiment
    public static final Property studyEffectOf = Ontology.property(NS, "studyEffectOf");

    public static final Resource SensingDevice = Ontology.resource(NS, "SensingDevice");

    // ---- GERMPLASM ----
    public static final Resource Germplasm = Ontology.resource(NS, "Germplasm");
    public static final Resource Species = Ontology.resource(NS, "Species");
    public static final Resource Variety = Ontology.resource(NS, "Variety");
    public static final Resource Accession = Ontology.resource(NS, "Accession");
    public static final Resource PlantMaterialLot = Ontology.resource(NS, "PlantMaterialLot");
    public static final Property fromSpecies = Ontology.property(NS, "fromSpecies");
    public static final Property fromVariety = Ontology.property(NS, "fromVariety");
    public static final Property fromAccession = Ontology.property(NS, "fromAccession");
    public static final Property fromInstitute = Ontology.property(NS, "fromInstitute");
    public static final Property hasProductionYear = Ontology.property(NS, "hasProductionYear");
    public static final Property hasGermplasm = Ontology.property(NS, "hasGermplasm");
    public static final Property hasId = Ontology.property(NS, "hasId");

}

