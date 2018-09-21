//******************************************************************************
//                                       BrapiTraitDAO.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 28 Aug, 2018
// Contact: alice.boizet@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.dao.sesame;

import java.util.ArrayList;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.dao.manager.DAOSesame;
import phis2ws.service.ontologies.Vocabulary;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.view.model.phis.BrapiTrait;
import phis2ws.service.view.model.phis.Trait;

/**
 * Get all traits available in the system according to brapi specifications
 * @see https://brapi.docs.apiary.io/#reference/traits/list-all-traits/list-all-traits
 * @author Alice Boizet <alice.boizet@inra.fr>
 */
public class BrapiTraitDAO extends DAOSesame<BrapiTrait> {
    final static Logger LOGGER = LoggerFactory.getLogger(BrapiTraitDAO.class);
    
    public String traitDbId;
 
    /**
     * Get the Variables associated to the traits
     * @author Alice Boizet <alice.boizet@inra.fr>
     * @return traits list of traits
     */    
    private ArrayList<BrapiTrait> getVariables(ArrayList<BrapiTrait> traits){
                
        for (BrapiTrait bt:traits) {
            /**
            * Query generated by the searched parameter above (traitDbId). .e.g. 
            * SELECT DISTINCT ?varUri
            * WHERE {
            * ?varUri <http://www.phenome-fppn.fr/vocabulary/2017#hasTrait> bt.getTraitDbId() .}
            *
            * @return query generated with the searched parameter above
            */
            SPARQLQueryBuilder query = new SPARQLQueryBuilder();
            query.appendSelect("?varUri");
            query.appendTriplet("?varUri", Vocabulary.RELATION_HAS_TRAIT.toString(),bt.getTraitDbId(), null);   
            TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
            ArrayList<String> varList = new ArrayList();
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    varList.add(bindingSet.getValue("varUri").stringValue());
                }                    
            }
            if (!varList.isEmpty()) {
                bt.setObservationVariables(varList);
            }           
        }
        return traits; 
    }    

    /**
     * Collect the list of traits
     * @author Alice Boizet <alice.boizet@inra.fr>
     * @return traits list of traits
     */
    public ArrayList<BrapiTrait> allPaginate() {
                
        TraitDaoSesame traitDAO = new TraitDaoSesame();
        if (this.traitDbId != null) {
            traitDAO.uri = traitDbId;
        }
        
        ArrayList<Trait> traits = traitDAO.allPaginate();
        ArrayList<BrapiTrait> brapiTraits = new ArrayList();
        for (Trait tr:traits) {
            BrapiTrait brapitr = new BrapiTrait();
            brapitr.setTraitDbId(tr.getUri());
            brapitr.setName(tr.getLabel());
            brapitr.setDescription(tr.getComment());
            brapiTraits.add(brapitr);
        }
        
        brapiTraits = getVariables(brapiTraits);
                
        return brapiTraits;
    }    

    @Override
    protected SPARQLQueryBuilder prepareSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Integer count() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
