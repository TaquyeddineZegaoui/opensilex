/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.rest.ontology.dal;

import java.net.URI;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.opensilex.rest.user.dal.UserModel;
import org.opensilex.sparql.service.SPARQLQueryHelper;
import org.opensilex.sparql.service.SPARQLService;
import org.opensilex.sparql.tree.ResourceTree;

/**
 *
 * @author vince
 */
public final class OntologyDAO {

    private final SPARQLService sparql;

    public OntologyDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }

    public ResourceTree<ClassModel> searchSubClasses(URI parent, UserModel user) throws Exception {
        return sparql.searchResourceTree(
                ClassModel.class,
                user.getLang(),
                parent,
                (SelectBuilder select) -> {
                    if (parent != null) {
                        select.addFilter(SPARQLQueryHelper.eq(ClassModel.PARENT_FIELD, parent));
                    }
                }
        );
    }
}