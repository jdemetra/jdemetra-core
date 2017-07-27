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

package demetra.arima.estimation;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.ssf.FastFilter;
import ec.tstoolkit.ssf.FastInitializer;
import ec.tstoolkit.ssf.PredictionErrorDecomposition;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastArimaForecasts {

    private final SsfArima ssf_;
    private final BackFilter bar_;
    private double mean, ssqErr;
    private boolean bmean;

    public double getSsqErr(){
        return ssqErr;
    }

    public double getMean(){
        return mean;
    }
    /**
     * 
     * @param model
     * @param mean
     */
    public FastArimaForecasts(final IArimaModel model, boolean mean)
    {
        bmean=mean;
	IArimaModel cmodel = model;
	if (mean) {
	    BackFilter ar = model.getStationaryAR(), ur = model
		    .getNonStationaryAR(), ma = model.getMA();
            bar_=ar.times(ur);
	    double var = model.getInnovationVariance();
	    BackFilter D = BackFilter.D1;
	    cmodel = new ArimaModel(ar, ur.times(D), ma.times(D), var);
	}
        else
            bar_=model.getAR();
	ssf_ = new SsfArima(cmodel);
    }

    private double[] fcasts(SsfData sd, int nf) {
	FastInitializer<SsfArima> initializer = new FastInitializer<>(
		new SsfArima.Initializer());
	FastFilter<SsfArima> filter = new FastFilter<>();
	filter.setInitializer(initializer);
	filter.setSsf(ssf_);
        PredictionErrorDecomposition perr=new PredictionErrorDecomposition(false);
	filter.process(sd, perr);
        ssqErr=perr.getSsqErr();
	// the first forecasts are produced by the state vector...
	double[] f = new double[nf];
	if (nf >= ssf_.getStateDim()) {
	    DataBlock a = filter.getState().A;
	    a.copyTo(f, 0);
	    // complete the forecasts....
	    int last = a.getLength() - 1;
	    for (int i = ssf_.getStateDim(); i < nf; ++i) {
		ssf_.TX(a);
		f[i] = a.get(last);
	    }
	} else
	    filter.getState().A.range(0, nf).copyTo(f, 0);
        if (bmean){
            DataBlock s=new DataBlock(f.length-bar_.getDegree());
            bar_.filter(new DataBlock(f), s);
            mean=s.get(s.getLength()-1);
        }
	return f;
    }

    /**
     * 
     * @param data
     * @param nf
     * @return
     */
    public double[] forecasts(double[] data, int nf)
    {
	SsfData sd = new SsfData(data, null);
	return fcasts(sd, nf);
    }

    /**
     * 
     * @param data
     * @param nf
     * @return
     */
    public double[] forecasts(IReadDataBlock data, int nf)
    {
	SsfData sd = new SsfData(data, null);
	return fcasts(sd, nf);
    }

}
