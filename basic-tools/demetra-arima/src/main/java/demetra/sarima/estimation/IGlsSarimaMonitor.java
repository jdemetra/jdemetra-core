/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sarima.estimation;

import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
public interface IGlsSarimaMonitor {

    /**
     * Estimate completely the given RegArima model
     * @param regs
     * @return 
     */
    RegArimaEstimation<SarimaModel> compute(RegArimaModel<SarimaModel> regs);

    /**
     * Estimate a RegArima model, starting from the given model.
     * The default implementation ignore the starting point.
     * @param regs
     * @return 
     */
    default RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs){
        return compute(regs);
    }
}
