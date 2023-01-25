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

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import internal.jdplus.arima.ModifiedLjungBoxFilter;
import jdplus.data.DataBlock;
import jdplus.stats.linearmodel.LinearModel;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArmaModel;
import jdplus.regsarima.regular.IAmiController;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.SarimaModel;
import jdplus.stats.tests.LjungBox;
import nbbrd.design.Development;


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
    public boolean accept(RegSarimaModelling context) {
        ModelDescription desc = context.getDescription();
        RegArimaEstimation<SarimaModel> estimation = context.getEstimation();
        
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
        int nobs = context.getEstimation().statistics().getEffectiveObservationsCount();
        rvr=Math.sqrt(fres.ssq()/nobs);
        fres = fres.drop(nres - nobs, 0);
        calcResStat(fres);
        int sp = desc.getAnnualFrequency();
        if (!calcLb(fres, calcLbLength(sp, nobs), desc.getArimaSpec().freeParametersCount())) {
            return false;
        }
        if ((1 - lbtest.getPvalue()) > lb) {
            return false;
        }

        return true;
    }

   
    private static int calcLbLength(int sp, int nobs) {
        int lbdf;

        switch (sp) {
            case 12:
                lbdf = 24;
                break;
            case 1:
                lbdf = 8;
                break;
            default:
                lbdf = 4 * sp;
                if (nobs <= 22 && sp == 4) {
                    lbdf = 6;
                }   break;
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
