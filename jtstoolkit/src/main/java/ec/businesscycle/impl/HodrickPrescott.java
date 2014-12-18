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

package ec.businesscycle.impl;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfRefData;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
public class HodrickPrescott {

    private double lambda_ = 1600;
    private UcarimaModel ucm_;
    private double[] s_, n_;

    public HodrickPrescott() {
    }

    // usual smoother is more stable than disturbance filter !! 
    public boolean process(double[] x) {
        return process(new ReadDataBlock(x));
    }
         
      public boolean process(IReadDataBlock x) {
        if (ucm_ == null) {
            initModel();
        }
      SsfUcarima ssf = new SsfUcarima(ucm_);
        Smoother smoother = new Smoother();
        smoother.setSsf(ssf);
        SmoothingResults srslts = new SmoothingResults();
        if (!smoother.process(new SsfRefData(x, null), srslts)) //TSToolkit.Ssf.DisturbanceSmoother dsmoother = new TSToolkit.Ssf.DisturbanceSmoother();
        //dsmoother.Ssf = ssf;
        //if (!dsmoother.Process(new TSToolkit.Ssf.SsfRefData(new RC(x), new RC())))
        {
            return false;
        }

        //TSToolkit.Ssf.SmoothingResults srslts = dsmoother.CalcSmoothedStates();
        s_ = srslts.component(ssf.cmpPos(0));
        n_ = srslts.component(ssf.cmpPos(1));
        return true;
    }

    public double[] getSignal() {
        return s_;
    }

    public double[] getNoise() {
        return n_;
    }

    public double getLambda() {
        return lambda_;
    }

    public void setLambda(double value) {
        if (lambda_ != value) {
            lambda_ = value;
            ucm_ = null;
        }

    }

    private void initModel() {
        Polynomial D = UnitRoots.D(1), D2 = D.times(D);
        ArimaModel i2 = new ArimaModel(null,
                new BackFilter(D2), null, 1);
        ArimaModel wn = new ArimaModel(lambda_);
        ucm_ = new UcarimaModel();
        ucm_.addComponent(i2);
        ucm_.addComponent(wn);
    }
}
