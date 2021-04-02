/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.regarima.tests;

import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.dstats.F;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import jdplus.stats.tests.SampleMean;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;
import demetra.data.DoubleSeq;
import jdplus.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 * @param <M>
 */
public class OneStepAheadForecastingTest<M extends IArimaModel> {

    private final IRegArimaComputer<M> processor;
    private final int nback;
    // results
    private DoubleSeq residuals;
    private double meanIn, meanOut, mseIn, mseOut;
    private int inSampleSize;
    private boolean mean;

    public OneStepAheadForecastingTest(final IRegArimaComputer<M> processor, final int nback) {
        this.processor = processor;
        this.nback = nback;
    }

    public IRegArimaComputer<M> getProcessor() {
        return processor;
    }

    public int getOutOfSampleLength() {
        return nback;
    }

    public int getInSampleLength() {
        return residuals.length() - nback;
    }

    public boolean test(RegArimaModel<M> regarima) {
        try {
            RegArimaModel<M> model = linearize(regarima);
            residuals = computeResiduals(model);
            if (residuals == null) {
                return false;
            }
        } catch (Exception err) {
            return false;
        }

        int n = residuals.length();
        if (n <= nback + 2) {
            return false;
        }
        DoubleSeq in = residuals.drop(0, nback);
        DoubleSeq out = residuals.range(in.length(), n);
        inSampleSize = mean ? in.length() - 1 : in.length();
        meanIn = in.sum() / in.length();
        mseIn = in.ssq() / inSampleSize;
        meanOut = out.sum() / nback;
        mseOut = out.ssq() / nback;
        return true;
    }

    public StatisticalTest outOfSampleMeanTest() {
        return new SampleMean(meanOut, nback)
                .populationMean(0)
                .estimatedPopulationVariance(mseIn, inSampleSize)
                .normalDistribution(true)
                .build();
    }

    public DoubleSeq getInSampleResiduals() {
        return residuals.drop(0, nback);
    }

    public double getInSampleMean() {
        return meanIn;
    }

    public double getOutOfSampleMean() {
        return meanOut;
    }

    public double getInSampleMeanSquaredError() {
        return mseIn;
    }

    public double getOutOfSampleMeanSquaredError() {
        return mseOut;
    }

    public DoubleSeq getOutOfSampleResiduals() {
        int n = residuals.length();
        return residuals.range(n - nback, n);
    }

    public StatisticalTest sameVarianceTest() {
        F f = new F(nback, inSampleSize);
        return new StatisticalTest(f, mseOut / mseIn, TestType.Upper, false);
    }

    public StatisticalTest inSampleMeanTest() {
        int n = residuals.length();
        int nsample = n - nback;
        return new SampleMean(meanIn, nsample)
                .populationMean(0)
                .estimatedPopulationVariance(mseIn, inSampleSize)
                .normalDistribution(true)
                .build();
    }

    private RegArimaModel<M> linearize(RegArimaModel<M> regarima) {
        if (regarima.getVariablesCount() == 0) {
            return regarima;
        }

        ConcentratedLikelihoodWithMissing concentratedLikelihood = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
        DoubleSeq linearizedData = RegArimaUtility.linearizedData(regarima, concentratedLikelihood);

        mean = regarima.isMean();
        return RegArimaModel.<M>builder()
                .y(linearizedData)
                .arima(regarima.arima())
                .meanCorrection(mean)
                .build();
    }

    protected RegArimaEstimation<M> inSampleEstimate(RegArimaModel<M> regarima) {
        // shorten the model
        if (regarima.getObservationsCount() <= nback) {
            return null;
        }
        M arima = regarima.arima();
        RegArimaModel model = RegArimaModel.<M>builder()
                .y(regarima.getY().drop(0, nback))
                .arima(arima)
                .meanCorrection(mean)
                .build();
        return processor.optimize(model, null);
    }

    protected DoubleSeq computeResiduals(RegArimaModel<M> regarima) {
        try {
            RegArimaEstimation<M> est = inSampleEstimate(regarima);
            if (est == null) {
                return null;
            }
            DoubleSeq y = regarima.getY();
            if (regarima.isMean()) {
                DataBlock yc = DataBlock.of(regarima.getY());
                double[] m = RegArimaUtility.meanRegressionVariable(regarima.arima().getNonStationaryAr(), yc.length());
                yc.addAY(-est.getConcentratedLikelihood().coefficient(0), DataBlock.of(m));
                y = yc;
            }
            RegArimaModel model = RegArimaModel.<M>builder()
                    .y(y)
                    .arima(est.getModel().arima())
                    .build();
            return ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model).e();
        } catch (Exception err) {
            return null;
        }

    }
}
