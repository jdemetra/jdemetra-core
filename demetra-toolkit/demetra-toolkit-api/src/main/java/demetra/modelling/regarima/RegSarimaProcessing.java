/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regarima;

import demetra.arima.SarimaModel;
import demetra.regarima.RegArimaModel;

/**
 * Specialized version of a RegArimaProcessing. Usual Box-Jenkins model
 * @author Jean Palate
 */
@lombok.Value
@lombok.EqualsAndHashCode(callSuper=false)
public class RegSarimaProcessing implements RegArimaProcessing<SarimaModel, SarimaSpec> {

    private Specification<SarimaModel, SarimaSpec> specification;
    private RegArimaModel<SarimaModel> estimation;
}
