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

package ec.tstoolkit.arima.special;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.SsfComposite;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class MixedAirlineModel {

    private double th_ = -.2, bth_ = -.2, svar_;
    private int[] np_;
    private int freq_ = 12;

    public void setNoisyPeriods(int[] periods) {
        np_ = periods.clone();
    }

    public int[] getNoisyPeriods() {
        return np_;
    }

    public boolean stabilize() {
        boolean changed = false;
        if (Math.abs(th_) > 1) {
            th_ = 1 / th_;
            changed = true;
        }
        if (Math.abs(bth_) > 1) {
            bth_ = 1 / bth_;
            changed = true;
        }
        return changed;
    }

    public SarimaModel getAirline() {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        return builder.createAirlineModel(freq_, th_, bth_);
    }

    /**
     * Copy the parameters of a given airline model. The model must be an
     * airline model (not checked by the current implementation !).
     * @param model
     */
    public void setAirline(SarimaModel model) {
        freq_ = model.getFrequency();
        th_ = model.theta(1);
        bth_ = model.btheta(1);
    }

    public double getNoisyPeriodsVariance() {
        return svar_;
    }

    public void setNoisyPeriodsVariance(double svar) {
        svar_ = svar;
    }

    public double getTheta() {
        return th_;
    }

    public void setTheta(double th) {
        th_ = th;
    }

    public double getBTheta() {
        return bth_;
    }

    public void setBTheta(double bth) {
        bth_ = bth;
    }

    public ISsf makeSsf() {
        return new SsfComposite(new MixedAirlineCompositeModel(this));
    }

    public void setFrequency(int freq) {
        freq_ = freq;
    }

    public int getFrequency() {
        return freq_;
    }

    @Override
    public String toString() {
        if (np_.length == 0) {
            return "airline";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(np_[0] + 1);
        for (int i = 1; i < np_.length; ++i) {
            builder.append("  ").append(np_[i] + 1);
        }
        builder.append(']');
        return builder.toString();
    }
}
