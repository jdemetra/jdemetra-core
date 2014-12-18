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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Preliminary)
@Deprecated
public class RangeMeanTest implements IPreprocessingModule {

    private PreprocessingModel model_;
    private boolean log_;
    private double tlog_ = 2;
    private int isj_, itrim_;

    @Override
    public ProcessingResult process(ModellingContext context) {

        double[] data = context.description.getY();
        for (int i = 0; i < data.length; ++i) {
            if (data[i] <= 0) {
                return ProcessingResult.Unprocessed;
            }
        }

        int ifreq = context.description.getFrequency();
        log_ = useLogs(ifreq, data);

        context.description.setTransformation(getTransformation());
        context.estimation=null;
        return ProcessingResult.Changed;
    }

    /**
     *
     */
    public RangeMeanTest() {
    }

    // / <summary>
    // / Compute isj_ the group length which is a multiple of freq. Also compute
    // itrim_
    // / </summary>
    // / <param name="freq"></param>
    // / <param name="n"></param>
    private void computeisj(int freq, int n) {
        itrim_ = 1;
        if (freq == 12) {
            isj_ = 12;
        } else if (freq == 6) {
            isj_ = 12;
            if (n <= 165) {
                itrim_ = 2;
            }
        } else if (freq == 4) {
            if (n > 165) {
                isj_ = 8;
            } else {
                isj_ = 12;
                itrim_ = 2;
            }
        } else if (freq == 3) {
            if (n > 165) {
                isj_ = 6;
            } else {
                isj_ = 12;
                itrim_ = 2;
            }
        } else if (freq == 2) {
            if (n > 165) {
                isj_ = 6;
            } else {
                isj_ = 12;
                itrim_ = 2;
            }
        } else if (freq == 1) {
            if (n > 165) {
                isj_ = 5;
            } else {
                isj_ = 9;
                itrim_ = 2;
            }
        } else if (freq == 5) {
            if (n > 165) {
                isj_ = 5;
            } else {
                isj_ = 15;
                itrim_ = 2;
            }
        } else {
            isj_ = freq;
        }
    }

    /**
     *
     * @return
     */
    public double getTLog() {
        return tlog_;
    }

    /**
     *
     * @param value
     */
    public void setTLog(double value) {
        tlog_ = value;
    }

    /**
     *
     * @param freq
     * @param data
     * @return
     */
    public boolean useLogs(int freq, double[] data) {
        int n = data.length;
        isj_ = 0;
        itrim_ = 0;
        computeisj(freq, n);
        int npoints = n / isj_;
        if (npoints <= 3) {
            return false;
        }
        double[] range = new double[npoints], smean = new double[npoints], srt = new double[isj_];

        for (int i = 0; i < npoints; ++i) {
            // fill srt;
            for (int j = 0; j < isj_; ++j) {
                srt[j] = data[j + i * isj_];
            }
            Arrays.sort(srt);
            range[i] = srt[isj_ - itrim_ - 1] - srt[itrim_];
            double s = srt[itrim_];
            for (int j = itrim_ + 1; j < isj_ - itrim_; ++j) {
                s += srt[j];
            }
            s /= (isj_ - 2 * itrim_);
            smean[i] = s;
        }

        Ols ols = new Ols();
        RegModel model = new RegModel();
        model.setY(new DataBlock(range));
        model.addX(new DataBlock(smean));
        model.setMeanCorrection(true);
        if (ols.process(model) && ols.getLikelihood().getTStats()[1] > tlog_) {
            return true;
        } else {
            return false;
        }
    }

    public DefaultTransformationType getTransformation() {
        return log_ ? DefaultTransformationType.Log : DefaultTransformationType.None;
    }

    public boolean hasChangedModel() {
        return true;
    }

    public PreprocessingModel retrieveModel() {
        PreprocessingModel model=model_;
        model_=null;
        return model;
    }
}
