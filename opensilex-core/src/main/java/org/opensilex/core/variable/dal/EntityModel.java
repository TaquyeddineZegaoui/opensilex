//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.variable.dal;

import java.net.URI;
import org.opensilex.core.ontology.Oeso;
import org.opensilex.sparql.annotations.SPARQLResource;
import org.opensilex.sparql.utils.ClassURIGenerator;


/**
 *
 * @author vidalmor
 */
@SPARQLResource(
        ontology = Oeso.class,
        resource = "Entity",
        graph = "variable"
)
public class EntityModel extends BaseVariableModel implements ClassURIGenerator<EntityModel> {

    public EntityModel() {

    }

    public EntityModel(URI uri) {
        setUri(uri);
    }

    @Override
    public String[] getUriSegments(EntityModel instance) {
        return new String[]{
            "variable",
            "entity",
            instance.getName()
        };
    }
}
