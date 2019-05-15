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

package demetra.x12;

import demetra.arima.IArimaModel;
import demetra.arima.internal.ModifiedLjungBoxFilter;
import jd.data.DataBlock;
import demetra.design.Development;
import demetra.linearmodel.LinearModel;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArmaModel;
import demetra.regarima.regular.IAmiController;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.SarimaModel;
import demetra.stats.tests.LjungBox;
import demetra.stats.tests.StatisticalTest;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 * @remark See mdlchk.for routine of the USCB implementation
 */
@Development(status = Development.Status.Preliminary)
public class ModelController implements IAmiController {

    private final double lb;
    private StatisticalTest lbtest;
    private double rvr, rtval;

    public ModelController(double lb) {
        this.lb = lb;
    }

    public StatisticalTest getLjungBoxTest() {
        return lbtest;
    }

    @Override
    public boolean accept(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        ModelEstimation estimation = context.getEstimation();
        
        DoubleSeq coeff = estimation.getConcentratedLikelihood().allCoefficients();
        RegArmaModel<SarimaModel> dmodel = desc.regarima().differencedModel();
        LinearModel lm = dmodel.asLinearModel();
        DataBlock res = lm.calcResiduals(coeff);
        // filter the residuals with the filter used in X12
        SarimaModel arma = dmodel.getArma();
        ModifiedLjungBoxFilter filter = new ModifiedLjungBoxFilter();
        int nres = filter.prepare(arma, res.length());
        DataBlock fres = DataBlock.make(nres);
        filter.apply(res, fres);
        int nobs = context.getEstimation().getStatistics().getEffectiveObservationsCount();
        rvr=Math.sqrt(fres.ssq()/nobs);
        fres = fres.drop(nres - nobs, 0);
        calcResStat(fres);
        int sp = desc.getAnnualFrequency();
        if (!calcLb(fres, calcLbLength(sp, nobs), desc.getArimaComponent().getFreeParametersCount())) {
            return false;
        }
        if ((1 - lbtest.getPValue()) > lb) {
            return false;
        }

        return true;
    }

   
    private static int calcLbLength(int sp, int nobs) {
        int lbdf;

        if (sp == 12) {
            lbdf = 24;
        } else if (sp == 1) {
            lbdf = 8;
        } else {
            lbdf = 4 * sp;
            if (nobs <= 22 && sp == 4) {
                lbdf = 6;
            }
        }
        if (lbdf >= nobs) {
            lbdf = nobs / 2;
        }
        return lbdf;
    }

    private boolean calcLb(DataBlock res, int nlb, int nhp) {
        try {
            double mu=res.sum()/res.length();
            res.sub(mu);
            lbtest=new LjungBox(res)
                    .autoCorrelationsCount(nlb)
                    .hyperParametersCount(nhp)
                    .build();
            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }

    private void calcResStat(DataBlock res) {
        double rm = res.sum(), rv = res.ssq();
        int n=res.length();
        rm /= n;
        rv = rv / n - rm * rm;
        double rstd = Math.sqrt(rv/n);
        rtval = rm / rstd;
    }

    public double getRTval() {
        return rtval;
    }

    public double getRvr() {
        return rvr;
    }
}
