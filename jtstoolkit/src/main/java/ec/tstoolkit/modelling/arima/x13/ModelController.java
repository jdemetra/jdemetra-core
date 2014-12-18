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

package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.IPreprocessingController;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.stats.LjungBoxTest;

/**
 *
 * @author Jean Palate
 * @remark See mdlchk.for routine of the USCB implementation
 */
@Development(status = Development.Status.Preliminary)
public class ModelController implements IPreprocessingController {

    private double tmu_ = 1;
    private double lb_ = 0.95;
    private LjungBoxTest lbtest_;
    private double rvr_, rtval_;

    public ModelController(double lb, double tmu) {
        lb_ = lb;
        tmu_ = tmu;
    }

    public ModelController() {
    }

    public LjungBoxTest getLjungBoxTest() {
        return lbtest_;
    }

    @Override
    public boolean accept(ModellingContext context) {
        double[] coeff = context.estimation.getLikelihood().getB();
        DataBlock res = context.estimation.getRegArima().getDModel().calcRes(new DataBlock(coeff));
        // filter the residuals with the filter used in X12
        IArimaModel arma = context.estimation.getRegArima().getArma();
        ModifiedLjungBoxFilter filter = new ModifiedLjungBoxFilter();
        int nres = filter.initialize(arma, res.getLength());
        DataBlock fres = new DataBlock(nres);
        filter.filter(res, fres);
        int nobs = context.estimation.getStatistics().effectiveObservationsCount;
        rvr_=Math.sqrt(fres.ssq()/context.estimation.getLikelihood().getDegreesOfFreedom(false, 0));
        fres = fres.drop(nres - nobs, 0);
        calcResStat(fres);
        int sp = context.description.getFrequency();
        if (!calcLb(fres, calcLbLength(sp, nobs), context.description.getArimaComponent().getFreeParametersCount())) {
            return false;
        }
        if ((1 - lbtest_.getPValue()) > lb_) {
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
            double mu=res.sum()/res.getLength();
            res.sub(mu);
            lbtest_ = new LjungBoxTest();
            lbtest_.setK(nlb);
            lbtest_.setHyperParametersCount(nhp);
            lbtest_.test(res);
            return lbtest_.isValid();
        } catch (RuntimeException err) {
            return false;
        }
    }

    private void calcResStat(DataBlock res) {
        double rm = res.sum(), rv = res.ssq();
        int n=res.getLength();
        rm /= n;
        rv = rv / n - rm * rm;
        double rstd = Math.sqrt(rv/n);
        rtval_ = rm / rstd;
    }

    public double getRTval() {
        return rtval_;
    }

    public double getRvr() {
        return rvr_;
    }
}
