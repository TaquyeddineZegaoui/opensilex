//**********************************************************************************************
//                                       ResultatLabel.java 
//
// Author(s): Eloan LAGIER
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date:Feb 1 2018
// Contact: eloan.lagier@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  Feb 1, 2018
// Subject:  extend form Resultat adapted to the Label
//***********************************************************************************************
package phis2ws.service.view.brapi.results;

import java.util.ArrayList;
import phis2ws.service.view.brapi.Pagination;
import phis2ws.service.view.manager.Resultat;
import phis2ws.service.view.model.phis.Label;

/**
 *
 * @author Eloan LAGIER
 */
public class ResultatLabel extends Resultat<Label>{
     /**
     * Constructeur qui appelle celui de la classe mère dans le cas d'une liste 
     * à un seul élément
     * @param labels 
     */
    public ResultatLabel(ArrayList<Label> labels) {
        super(labels);
    }
    
     /**
     * Constructeur qui appelle celui de la classe mère dans le cas d'une liste 
     * à plusieurs éléments
     * @param labels
     * @param pagination
     * @param paginate 
     */
    public ResultatLabel(ArrayList<Label> labels, Pagination pagination, boolean paginate) {
        super(labels, pagination, paginate);
    }
}
