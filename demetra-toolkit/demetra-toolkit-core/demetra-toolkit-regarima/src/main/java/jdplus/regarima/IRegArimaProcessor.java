/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package jdplus.regarima;

import jdplus.arima.IArimaModel;
import nbbrd.design.Development;
import jdplus.arima.estimation.IArimaMapping;


/**
 * Defines the generic interface for the actual estimation of a RegArima model
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IRegArimaProcessor<M extends IArimaModel> {

    /**
     * 
     * @param regs
     * @param mapping
     * @return
     */
    public RegArimaEstimation<M> optimize(final RegArimaModel<M> regs, IArimaMapping<M> mapping);

    /**
     *
     * @param regs
     * @param mapping
     * @return
     */
    public RegArimaEstimation<M> process(final RegArimaModel<M> regs, IArimaMapping<M> mapping);

}
