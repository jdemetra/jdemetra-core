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

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.modelling.arima.tramo.seriestest.OverSeasTest2;
import ec.tstoolkit.modelling.arima.tramo.seriestest.SerType;
import ec.tstoolkit.modelling.arima.tramo.spectrum.PeaksEnum;
import ec.tstoolkit.modelling.arima.tramo.spectrum.Spect;
import ec.tstoolkit.modelling.arima.tramo.spectrum.TPeaks;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.reflect.Array;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@Deprecated
public class ModelController2 implements IPreprocessingModule {

    private double eps_ = 1e-5;
    //private final IOutliersDetectionModule outliers;
    //private final IRegArimaProcessor<SarimaModel> processor;

    public ModelController2() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (!context.automodelling) {
            return ProcessingResult.Unprocessed;
        }
        ProcessingResult rslt = ProcessingResult.Unchanged;
        if (testXLseas(context)) {
            IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
            ModelDescription model = context.description;
            context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
            int ndim = mapping.getDim();
            TramoModelEstimator monitor = new TramoModelEstimator(mapping);
            monitor.setPrecision(eps_);
            context.estimation.compute(monitor, ndim);
            context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
            double[] score = monitor.getScore();
            if (score != null) {
                context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, score);
            }
            rslt = ProcessingResult.Changed;
        }

        SarimaModel arima = context.estimation.getRegArima().getArima();
//        if (SarimaMapping2.stabilize(arima, 1, .99)) {
//            context.estimation.computeLikelihood(context.description.getArimaComponent().getFreeParametersCount());
//            rslt=ProcessingResult.Changed;
//        }
        return rslt;
    }

    private int calcLinSeas(PreprocessingModel model) {
        TsData lin = model.linearizedSeries(false);
        TsData dlin = lin.delta(1);
        PeaksEnum[] peaks = Spect.SpectrumComputation(dlin);
        int d = model.description.getArimaComponent().getD(),
                bd = model.description.getArimaComponent().getBD();
        OverSeasTest2 seas = new OverSeasTest2(lin, peaks, d + bd, SerType.Xlin);
        return seas.getCheckOverSeasTest();
    }

    private static double FdetSeas(ModellingContext context) //
    // Implements the test in the paper:
    //      Determining Seasonality: A comparison diagnostic from X12-Arima
    //          Demetra P. Lytras, Roxanne M. Feldpausch, William R. Bell
    //
    {
        PreprocessingModel model = context.current(true);
        ModelDescription md = model.description.clone();
        //
        // Add Seasonal dummy variables
//        
        SeasonalDummies sd = new SeasonalDummies(context.description.getEstimationDomain().getFrequency());
        Variable tvar = Variable.userVariable(sd, ComponentType.SeasonallyAdjusted, RegStatus.Prespecified);
        md.addVariable(tvar);
        SarimaSpecification spec = md.getSpecification();
        spec.setBP(0);
        spec.setBD(0);
        spec.setBQ(0);
        md.setSpecification(spec);
        int diffvalue = spec.getDifferenceOrder();
        //
        // Model Estimation
//       
        ModellingContext mc = new ModellingContext();
        mc.description = md;
        OutliersDetector outliers = new OutliersDetector();
        outliers.setDefault();
        outliers.process(mc);
        RegArimaModel<SarimaModel> regarima = md.buildRegArima();
        IParametricMapping<SarimaModel> mapping = md.defaultMapping();
        ModelEstimation estim = new ModelEstimation(regarima, md.getLikelihoodCorrection());
        TramoModelEstimator estimator = new TramoModelEstimator(mapping);
        estim.compute(estimator, mapping.getDim());
        estim.updateParametersCovariance(estimator.getParametersCovariance());
        double[] score = estimator.getScore();
        if (score != null) {
            context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, score);
        }
        mc.estimation = estim;
        //
        // Retrieve the Seasonal dummy coefficient + Innovation Variance Matrix
        //        
        TsVariableList ts = md.buildRegressionVariables();
        TsVariableSelection sel = ts.select(SeasonalDummies.class);
        TsVariableSelection.Item<SeasonalDummies>[] items = sel.elements();
        int pos = items[0].position;
        int dim = items[0].variable.getDim();
        int istart = md.getRegressionVariablesStartingPosition();
        double[] coeffs = mc.estimation.getLikelihood().getB();
        double[] Beta = new double[dim];
        for (int j = 0; j < dim; j++) {
            Beta[j] = coeffs[istart + pos + j];
        }
        Matrix mat = mc.estimation.getLikelihood().getBVar();
        int imreg = mat.getColumnsCount() - dim;
        Matrix Bvar = new Matrix(dim, dim);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Bvar.set(i, j, mat.get(i, j));
            }
        }
        Matrix IBvar;
        try {
            IBvar = SymmetricMatrix.inverse(Bvar);

        } catch (Exception e) {
            return -3.0;
        }
        double[] IBvarxtB = new double[dim];
        Array.setDouble(IBvarxtB, 0, 0.0);
        //
        // Compute IBvar x tBeta x tBeta
//        
        for (int i = 0; i < IBvar.getRowsCount(); i++) {
            for (int j = 0; j < IBvar.getColumnsCount(); j++) {
                IBvarxtB[i] += IBvar.get(i, j) * Beta[j];
            }
        }
        double xvalue = 0.0d;
        for (int i = 0; i < dim; i++) {
            xvalue += IBvarxtB[i] * Beta[i];
        }
        double df1 = mc.description.getFrequency() - 1;
        double df2 = mc.description.getEstimationDomain().getLength() - diffvalue - imreg;
        xvalue = xvalue * df2 / (df1 * (mc.description.getEstimationDomain().getLength() - diffvalue));
        double result = TPeaks.Fcdf(xvalue, df1, df2);
        return result;

    }
    //
    // signLevel parameter 0 => Q criticals at 95%
    //                     1 => Q criticals at 99%
    //

    private static boolean isBetter(ModellingContext m1, ModellingContext m2, int signLevel) {
        double[] chi299 = {6.6349, 9.2103, 11.3449, 13.2767, 15.0863, 16.8119,
            18.4753, 20.0902, 21.666, 23.2093, 24.725, 26.217,
            27.6882, 29.1412, 30.5779, 31.9999, 33.4087, 34.8053,
            36.1909, 37.5662, 38.9322, 40.2894, 41.6384, 42.9798,
            44.3141, 45.6417, 46.9629, 48.2782, 49.5879, 50.8922,
            52.1914, 53.4858, 54.7755, 56.0609, 57.3421, 58.6192,
            59.8925, 61.1621, 62.4281, 63.6907, 64.9501, 66.2062,
            67.4593, 68.7095, 69.9568, 71.2014, 72.4433, 73.6826,
            74.9195, 76.1539};
        double[] chi295 = {3.8415, 5.9915, 7.8147, 9.4877, 11.0705,
            12.5916, 14.0671, 15.5073, 16.919, 18.307, 19.6751,
            21.0261, 22.362, 23.6848, 24.9958, 26.2962,
            27.5871, 28.8693, 30.1435, 31.4104, 32.6706,
            33.9244, 35.1725, 36.415, 37.6525, 38.8851,
            40.1133, 41.3371, 42.557, 43.773, 44.9853, 46.1943,
            47.3999, 48.6024, 49.8018, 50.9985, 52.1923,
            53.3835, 54.5722, 55.7585, 56.9424, 58.124,
            59.3035, 60.4809, 61.6562, 62.8296, 64.0011,
            65.1708, 66.3386, 67.5048};

        boolean rslt = true;
        double xlQm1, xlQm2, QScri;
        NiidTests stats1 = new NiidTests(new ReadDataBlock(m1.estimation.getLikelihood().getResiduals()), m1.description.getFrequency(), 0, true);
        NiidTests stats2 = new NiidTests(new ReadDataBlock(m2.estimation.getLikelihood().getResiduals()), m2.description.getFrequency(), 0, true);
        if (signLevel == 0) {
            xlQm1 = chi295[stats1.getLjungBox().getK() - m1.description.getArimaComponent().getFreeParametersCount() - 1];
            xlQm2 = chi295[stats2.getLjungBox().getK() - m2.description.getArimaComponent().getFreeParametersCount() - 1];
            QScri = chi295[1];
        } else {
            xlQm1 = chi299[stats1.getLjungBox().getK() - m1.description.getArimaComponent().getFreeParametersCount() - 1];
            xlQm2 = chi299[stats2.getLjungBox().getK() - m2.description.getArimaComponent().getFreeParametersCount() - 1];
            QScri = chi299[1];

        }
        if (((m2.estimation.getStatistics().BIC - m1.estimation.getStatistics().BIC) < Math.abs((m1.estimation.getStatistics().BIC)) * 0.05d)
                && (stats1.getLjungBox().getValue() > xlQm1) && (stats2.getLjungBox().getValue() < xlQm2)
                && ((stats1.getLjungBox().getValue() - stats2.getLjungBox().getValue()) > 2.0d)
                && ((m1.description.getOutliers().size() - m2.description.getOutliers().size()) < 4)
                && (m2.description.getOutliers().size() < 0.06 * m2.description.getEstimationDomain().getLength())) {
            rslt = false;
        } else if ((m1.description.getOutliers().size() > 0.05 * m1.description.getEstimationDomain().getLength())
                && (m2.description.getOutliers().size() < 0.05 * m2.description.getEstimationDomain().getLength())
                && ((m1.description.getOutliers().size() - m2.description.getOutliers().size()) > 2)
                && ((stats2.getLjungBox().getValue() < xlQm2) || ((stats1.getLjungBox().getValue() < xlQm1)
                && ((stats2.getLjungBox().getValue() - stats1.getLjungBox().getValue()) < 2.0d)))
                && ((m2.estimation.getStatistics().BIC - m1.estimation.getStatistics().BIC) < Math.abs(m1.estimation.getStatistics().BIC * 1.05d))) {
            rslt = false;
        } else if ((m2.estimation.getStatistics().BIC - m1.estimation.getStatistics().BIC) < Math.abs(m1.estimation.getStatistics().BIC * 1.05d)
                && ((stats2.getLjungBox().getValue() < xlQm2) || ((stats1.getLjungBox().getValue() < xlQm1)
                && ((stats2.getLjungBox().getValue() - stats1.getLjungBox().getValue()) < 2.0d)))
                && (stats1.getLjungBoxOnSquare().getValue() > QScri) && (stats2.getLjungBoxOnSquare().getValue() < QScri)
                && ((stats1.getLjungBoxOnSquare().getValue() - stats2.getLjungBoxOnSquare().getValue()) > 0.5d)
                && ((Math.abs(stats2.getSkewness().getValue()) < 2.0d)
                || ((Math.abs(stats2.getSkewness().getValue()) < Math.abs(stats1.getSkewness().getValue()) + 1.0d)))
                && ((m2.description.getOutliers().size() < m2.description.getEstimationDomain().getLength() * 0.05)
                || ((m2.description.getOutliers().size() - m1.description.getOutliers().size()) < 4))) {
            rslt = false;
        }
        return rslt;

    }

    private static ModellingContext CompModel(ModellingContext c1, ModellingContext c2) {
        if (c2.estimation.getStatistics().BIC < c1.estimation.getStatistics().BIC) {
            if (isBetter(c2, c1, 0)) {
                return c2;
            } else {
                return c1;
            }
        } else if (isBetter(c1, c2, 0)) {
            return c1;
        } else {
            return c2;
        }
    }

    //
    // Return values :
    //                  true : model description is changed
    //                  false : model description is NOT changed
//            
    public static boolean testXLseas(ModellingContext context) {
        try {
            ModelDescription md = context.description;
            TsData lin = new TsData(context.description.getEstimationDomain());
            context.estimation.getRegArima().getY().copyTo(lin.internalStorage(), 0);
            TsData dlin = lin.delta(1);
            PeaksEnum[] peaks = Spect.SpectrumComputation(dlin);
            int d = md.getSpecification().getD();
            int bd = md.getSpecification().getBD();
            //
            // Add new constructor to OverSeasTest2 added the parameter PSeas
//       
            double Pseas = FdetSeas(context);
            OverSeasTest2 seas = new OverSeasTest2(lin, peaks, d + bd, SerType.Xlin, Pseas);
            int OST = seas.getCheckOverSeasTest();
            SarimaSpecification spec = md.getSpecification();
            if (spec.hasSeasonalPart() && (OST == 0)) {
                spec.airline(false);
                spec.setBQ(1); //?
                md.setSpecification(spec);
                md.setMean(true);
                context.estimation = null;
                return true;
            } else if (!spec.hasSeasonalPart() && (OST >= 1)) {
                spec.airline(false);
                spec.setBQ(1); //?
                md.setSpecification(spec);
                context.estimation = null;
                return true;
            }
            return false;
        } catch (RuntimeException err) {
            return false;
        }
    }
}
