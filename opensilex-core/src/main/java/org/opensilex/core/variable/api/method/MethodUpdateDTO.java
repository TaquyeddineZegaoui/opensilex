//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.variable.api.method;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class MethodUpdateDTO extends MethodCreationDTO {

    @NotNull
    @Override
    public URI getUri() {
        return super.getUri();
    }
}
