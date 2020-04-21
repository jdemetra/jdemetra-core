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
package jdplus.x13.regarima;

import demetra.arima.SarimaOrders;
import demetra.arima.SarmaOrders;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.UnitRoots;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.regsarima.regular.IAutoModellingModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class AutoModellingModule implements IAutoModellingModule {

    private final DifferencingModule iddiff;
    private final ArmaModule amdid;

    @Override
    public ProcessingResult process(RegSarimaModelling modelling) {
        if (modelling.needEstimation()) {
            return ProcessingResult.Failed;
        }
        ModelDescription description = modelling.getDescription();
        RegArimaEstimation<SarimaModel> estimation = modelling.getEstimation();
        int period = description.getAnnualFrequency();
        SarimaOrders curspec = description.specification();
        SarimaOrders nspec;
        boolean curMean = description.isMean();
        boolean nmean;
        int nd, nbd;
        try {
            // get residuals of the current estimation
            DoubleSeq res = RegArimaUtility.linearizedData(estimation.getModel(), estimation.getConcentratedLikelihood());

            if (iddiff.process(res, period)) {
                nd = iddiff.getD();
                nbd = iddiff.getBd();
                nmean = iddiff.isMeanCorrection();
            } else {
                nd = 1;
                nbd = 1;
                nmean = false;
            }

            // compute the differences of the residuals
            DoubleSeq dres;
            if (nd != 0 || nbd != 0) {
                BackFilter ur=new BackFilter(UnitRoots.D(nd).times(UnitRoots.D(period, nbd)));
                DataBlock tmp = DataBlock.make(res.length() - ur.getDegree());
                ur.apply(res, tmp);
                dres=tmp.unmodifiable();
            } else {
                dres = res;
            }
//            if (nmean) {
//                dres.sub(dres.sum() / dres.getLength());
//            }
            SarmaOrders rsltSpec = amdid.select(dres, period, nd, nbd);
            nspec = SarimaOrders.of(rsltSpec, nd, nbd);
        } catch (RuntimeException err) {
            nspec=SarimaOrders.airline(period);
            nmean=false;
        }
        boolean changed = false;
        if (!curspec.equals(nspec)) {
            description.setSpecification(nspec);
            changed = true;
        }
        if (nmean != curMean) {
            description.setMean(nmean);
            changed = true;
        }
        if (changed) {
            modelling.clearEstimation();
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    static SarimaOrders calcmaxspec(final int freq, final int inic, final int d,
            final int bd) {
        SarimaOrders spec = new SarimaOrders(freq);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(2);
        spec.setQ(2);
        if (freq > 1) {
//                    if (bd == 0) {
            spec.setBp(1);
//                    }
            spec.setBq(1);
        }
        return spec;
    }

}
