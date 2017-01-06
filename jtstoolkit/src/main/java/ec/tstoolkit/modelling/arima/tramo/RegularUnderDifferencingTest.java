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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.modelling.arima.AbstractModelController;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
public class RegularUnderDifferencingTest extends AbstractModelController {

    private static final double RTVAL = 1.6, IM = .01, MOD = .9;

    public RegularUnderDifferencingTest() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        SarimaModel cur = context.estimation.getArima();
        SarimaSpecification spec = cur.getSpecification();
        if (spec.getD() == DifferencingModule.MAXD || spec.getP() == 0 || !context.description.isEstimatedMean()) {
            return ProcessingResult.Unchanged;
        }
        if (checkResiduals(context)) {
            return ProcessingResult.Unchanged;
        }
        if (!hasQuasiUnitRoots(cur)) {
            return ProcessingResult.Unchanged;
        }
        spec.setD(spec.getD() + 1);
        spec.setP(spec.getP() - 1);
        ModellingContext ncontext = new ModellingContext();
        ncontext.description = context.description.clone();
        ncontext.description.setSpecification(spec);
        ncontext.description.setMean(false);
        if (!estimate(ncontext, false)) {
            return ProcessingResult.Failed;
        }
        else {
            transferInformation(ncontext, context);
            return ProcessingResult.Changed;
        }
    }

    private boolean checkResiduals(ModellingContext context) {
        DataBlock res = new DataBlock(context.estimation.getLikelihood().getResiduals());
        double rm = res.sum(), rv = res.ssq();
        int n = res.getLength();
        rm /= n;
        rv = rv / n - rm * rm;
        double rstd = Math.sqrt(rv / n);
        double rtval = rm / rstd;
        return Math.abs(rtval) <= RTVAL;
    }

    private boolean hasQuasiUnitRoots(SarimaModel m) {
        Complex[] roots = m.getRegularAR().mirror().roots();
        for (int i = 0; i < roots.length; ++i) {
            if (roots[i].getRe() > 0 && Math.abs(roots[i].getIm()) <= IM && roots[i].abs() >= MOD) {
                return true;
            }
        }
        return false;
    }
}
