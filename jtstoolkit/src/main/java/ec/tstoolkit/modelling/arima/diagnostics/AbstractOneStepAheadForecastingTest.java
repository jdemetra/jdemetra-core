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

package ec.tstoolkit.modelling.arima.diagnostics;

import ec.tstoolkit.arima.estimation.IRegArimaProcessor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.stats.MeanTest;
import ec.tstoolkit.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractOneStepAheadForecastingTest implements IOneStepAheadForecastingTest {

    private IRegArimaProcessor<SarimaModel> processor_ = new GlsSarimaMonitor();
    private final int nback_;
    // results
    private DataBlock res_;
    private double min_, mout_, vin_, vout_;
    private boolean mean_;

    protected AbstractOneStepAheadForecastingTest(int nback) {
        nback_ = nback;
    }

    public IRegArimaProcessor<SarimaModel> getProcessor() {
        return processor_;
    }

    @Override
    public int getOutOfSampleLength() {
        return nback_;
    }

    @Override
    public int getInSampleLength() {
        return res_.getLength() - nback_;
    }

    public void setProcessor(IRegArimaProcessor<SarimaModel> processor) {
        processor_ = processor;
    }

    @Override
    public boolean test(RegArimaModel<SarimaModel> regarima) {
        RegArimaModel<SarimaModel> model = linearize(regarima);
        res_ = computeResiduals(model);
        if (res_ == null) {
            return false;
        }

        int n = res_.getLength();
        DataBlock in = res_.drop(0, nback_), out = res_.range(in.getLength(), n);
        int nsample = mean_ ? in.getLength() - 1 : in.getLength();
        min_ = in.sum() / in.getLength();
        vin_ = in.ssq() / nsample;
        mout_ = out.sum() / nback_;
        vout_ = out.ssq() / nback_;
        return true;
    }

    @Override
    public MeanTest outOfSampleMeanTest() {
        int n = res_.getLength();
        DataBlock in = res_.drop(0, nback_), out = res_.range(in.getLength(), n);
        MeanTest test = new MeanTest();
        int nsample = mean_ ? in.getLength() - 1 : in.getLength();
        test.sampleMean(out, 0, Math.sqrt(in.ssq() / nsample), nsample, TestType.TwoSided);
        return test;
    }

    @Override
    public IReadDataBlock getInSampleResiduals() {
        return res_.drop(0, nback_);
    }

    @Override
    public double getInSampleME() {
        return min_;
    }

    @Override
    public double getOutOfSampleME() {
        return mout_;
    }

    @Override
    public double getInSampleMSE() {
        return vin_;
    }

    @Override
    public double getOutOfSampleMSE() {
        return vout_;
    }

    @Override
    public IReadDataBlock getOutOfSampleResiduals() {
        int n = res_.getLength();
        return res_.range(n - nback_, n);
    }
    
    @Override
    public StatisticalTest mseTest(){
        int n=res_.getLength();
        int nin=n-nback_;
        if ( mean_)
            --nin;
        F f=new F();
        f.setDFNum(nback_);
        f.setDFDenom(nin);
        return new StatisticalTest(f, vout_/vin_, TestType.Upper, false);
    }

    @Override
    public MeanTest inSampleMeanTest() {
        DataBlock in = res_.drop(0, nback_);
        MeanTest test = new MeanTest();
        int nsample = mean_ ? in.getLength() - 1 : in.getLength();
        test.sampleMean(in, 0, Math.sqrt(in.ssq() / nsample), nsample, TestType.TwoSided);
        return test;
    }

    private RegArimaModel<SarimaModel> linearize(RegArimaModel<SarimaModel> regarima) {
        if (regarima.getVarsCount() == 0) {
            return regarima;
        }

        DataBlock z = regarima.getY().deepClone();
        double[] b = regarima.computeLikelihood().getB();
        if (b != null) {
            // handle missing values:
            int start = regarima.isMeanCorrection() ? 1 : 0;
            int[] missings = regarima.getMissings();
            if (missings != null) {
                for (int i = 0; i < missings.length; ++i) {
                    z.add(missings[i], -b[start + i]);
                }
                start += missings.length;
            }
            if (b != null) {
                for (int i = start; i < b.length; ++i) {
                    z.addAY(-b[i], regarima.X(i - start));
                }
            }
        }

        mean_ = regarima.isMeanCorrection();
        RegArimaModel<SarimaModel> lmodel = new RegArimaModel<>();
        lmodel.setY(z);
        lmodel.setArima(regarima.getArima());
        lmodel.setMeanCorrection(mean_);
        return lmodel;
    }

    protected RegArimaEstimation<SarimaModel> inSampleEstimate(RegArimaModel<SarimaModel> regarima) {
        // shorten the model
        if (regarima.getObsCount() <= nback_) {
            return null;
        }
        RegArimaModel<SarimaModel> model = new RegArimaModel<>();
        model.setMeanCorrection(regarima.isMeanCorrection());
        model.setY(regarima.getY().drop(0, nback_));
        model.setArima(regarima.getArima());
        return processor_.optimize(model);
    }

    protected abstract DataBlock computeResiduals(RegArimaModel<SarimaModel> regarima);
}
