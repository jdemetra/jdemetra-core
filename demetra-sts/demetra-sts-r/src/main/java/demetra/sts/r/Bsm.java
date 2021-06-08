/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.likelihood.ParametersEstimation;
import demetra.math.matrices.MatrixType;
import demetra.sts.BasicStructuralModel;
import demetra.sts.BsmDecomposition;
import demetra.sts.BsmEstimation;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.LightBasicStructuralModel;
import demetra.sts.SeasonalModel;
import demetra.sts.io.protobuf.StsProtosUtility;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import demetra.timeseries.regression.UserVariable;
import demetra.timeseries.regression.Variable;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.Regression;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.BsmData;
import jdplus.sts.SsfBsm;
import jdplus.sts.internal.BsmKernel;
import jdplus.sts.internal.BsmMapping;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Bsm {

    public BasicStructuralModel process(TsData y, MatrixType X, int level, int slope, int cycle, int noise, String seasmodel, boolean diffuse, double tol) {
        SeasonalModel sm = seasmodel == null || seasmodel.equalsIgnoreCase("none") ? null : SeasonalModel.valueOf(seasmodel);
        BsmSpec mspec = BsmSpec.builder()
                .level(of(level), of(slope))
                .cycle(cycle != -1)
                .noise(of(noise))
                .seasonal(sm)
                .build();

        BsmEstimationSpec espec = BsmEstimationSpec.builder()
                .diffuseRegression(diffuse)
                .precision(tol)
                .build();
        BsmKernel kernel = new BsmKernel(espec);
        if (!kernel.process(y.getValues(), X, y.getAnnualFrequency(), mspec)) {
            return null;
        }
        
        int nhp = kernel.finalSpecification().getFreeParametersCount();
        BsmMapping mapping=new BsmMapping(kernel.finalSpecification(), y.getAnnualFrequency(), null);
        DoubleSeq params = mapping.map(kernel.getResult());
        ParametersEstimation parameters=new ParametersEstimation(params, "bsm");

        DoubleSeq coef = kernel.getLikelihood().coefficients();
        LightBasicStructuralModel.Estimation estimation = LightBasicStructuralModel.Estimation.builder()
                .y(y.getValues())
                .X(X)
                .coefficients(coef)
                .coefficientsCovariance(kernel.getLikelihood().covariance(nhp, true))
                .parameters(parameters)
                .residuals(kernel.getLikelihood().e())
                .statistics(kernel.getLikelihood().stats(0, nhp))
                .build();
        
        Variable[] vars= X == null ? new Variable[0] : new Variable[X.getColumnsCount()];
        TsPeriod start = y.getStart();
        for (int i=0; i<vars.length; ++i){
            UserVariable uvar=new UserVariable("var-"+(i+1), TsData.of(start, X.column(i)));
            vars[i]=Variable.variable("var-"+(i+1), uvar).withCoefficient(Parameter.estimated(coef.get(i)));
        }
        LightBasicStructuralModel.Description description = LightBasicStructuralModel.Description.builder()
                .series(y)
                .logTransformation(false)
                .lengthOfPeriodTransformation(LengthOfPeriodType.None)
                .specification(kernel.finalSpecification())
                .variables(vars)
                .build();
        
        return LightBasicStructuralModel.builder()
                .description(description)
                .estimation(estimation)
                .bsmDecomposition(kernel.decompose())
                .build();
    }
    
    public byte[] toBuffer(BsmEstimation estimation){
        return StsProtosUtility.convert(estimation).toByteArray();
    }

    public byte[] toBuffer(BasicStructuralModel bsm){
        return StsProtosUtility.convert(bsm).toByteArray();
    }

    private Parameter of(int p) {
        if (p == 0) {
            return Parameter.zero();
        } else if (p > 0) {
            return Parameter.undefined();
        } else {
            return null;
        }
    }

    public MatrixType forecast(TsData series, String model, int nf) {
        double[] y = extend(series, nf);
        Matrix X = variables(model, series.getDomain().extend(0, nf));

        // estimate the model
        BsmEstimationSpec espec = BsmEstimationSpec.builder()
                .diffuseRegression(true)
                .build();

        BsmKernel kernel = new BsmKernel(espec);
        boolean ok = kernel.process(series.getValues(), X == null ? null : X.extract(0, series.length(), 0, X.getColumnsCount()), series.getAnnualFrequency(), BsmSpec.DEFAULT);
        BsmData result = kernel.getResult();
        // create the final ssf
        SsfBsm bsm = SsfBsm.of(result);
        Ssf ssf;
        if (X == null) {
            ssf = bsm;
        } else {
            ssf = RegSsf.ssf(bsm, X);
        }
        DefaultDiffuseSquareRootFilteringResults frslts = DkToolkit.sqrtFilter(ssf, new SsfData(y), true);
        double[] fcasts = new double[nf * 2];
        DiffuseConcentratedLikelihood ll = kernel.getLikelihood();
        ISsfLoading loading = ssf.measurement().loading();
        for (int i = 0, j = series.length(); i < nf; ++i, ++j) {
            fcasts[i] = loading.ZX(j, frslts.a(j));
            double v = loading.ZVZ(j, frslts.P(j));
            fcasts[nf + i] = v <= 0 ? 0 : Math.sqrt(v);
        }
        return MatrixType.of(fcasts, nf, 2);
    }

    private double[] extend(TsData series, int nf) {
        int n = series.length();
        double[] y = new double[n + nf];
        series.getValues().copyTo(y, 0);
        for (int i = 0; i < nf; ++i) {
            y[n + i] = Double.NaN;
        }
        return y;
    }

    private ITsVariable[] variables(String model) {
        switch (model) {
            case "td2":
            case "TD2":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD2)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear)
                };
            case "td3":
            case "TD3":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear)
                };
            case "td7":
            case "TD7":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear)
                };

            case "full":
            case "Full":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear),
                    EasterVariable.builder()
                    .duration(6)
                    .endPosition(-1)
                    .meanCorrection(EasterVariable.Correction.Theoretical)
                    .build()
                };

            default:
                return null;
        }
    }

    private Matrix variables(String model, TsDomain domain) {
        ITsVariable[] variables = variables(model);
        if (variables == null) {
            return null;
        }
        return Regression.matrix(domain, variables);
    }
}
