/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.arima.ssf.SsfArima;
import demetra.data.DataBlock;
import demetra.data.DataBlockStorage;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.matrices.Matrix;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.estimation.SarimaMapping;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.dk.SsfFunction;
import demetra.ssf.dk.SsfFunctionPoint;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.calendar.DayClustering;
import demetra.timeseries.calendar.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariables;
import demetra.timeseries.regression.RegressionUtility;
import demetra.timeseries.simplets.TsData;
import java.util.Collections;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TimeVaryingRegression {

    @lombok.Data
    static class Airline {

        Matrix td;
        double regVariance;
        double theta, btheta;
    }

    public Matrix regarima(TsData s, String td) {
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        Matrix mtd = generate(s.domain(), td);
        SsfData data=new SsfData(s.values());
        TDvarMapping mapping=new TDvarMapping(mtd);
        SsfFunction<Airline, ISsf> fn=SsfFunction.builder(data, mapping, 
                params->
                {
        SarimaModel arima = SarimaModel.builder(spec)
                .theta(params.getTheta())
                .btheta(params.getBtheta())
                .build();
        SsfArima ssf = SsfArima.of(arima);
        Matrix var = Matrix.square(mtd.getColumnsCount());
        var.diagonal().set(params.getRegVariance());
                    
        return RegSsf.ofTimeVarying(ssf, mtd, var);
                }).build();

        LevenbergMarquardtMinimizer min=new LevenbergMarquardtMinimizer();
        min.minimize(fn.ssqEvaluate(mapping.getDefault()));
        SsfFunctionPoint<Airline, ISsf> rfn = (SsfFunctionPoint<Airline, ISsf>) min.getResult();
        DefaultSmoothingResults fs = DkToolkit.smooth(rfn.getSsf(), data, false);
        Matrix c = Matrix.make(mtd.getRowsCount(), mtd.getColumnsCount());

        for (int i = 14; i < 14+mtd.getColumnsCount(); ++i) {
            c.column(i - 14).copy(fs.getComponent(i));
        }
        return c;
    }

    private Matrix generate(RegularDomain domain, String td) {
        DayClustering dc = null;
        switch (td) {
            case "TD2":
                dc = DayClustering.TD2;
                break;
            case "TD3":
                dc = DayClustering.TD3;
                break;
            case "TD3c":
                dc = DayClustering.TD3c;
                break;
            case "TD4":
                dc = DayClustering.TD4;
                break;
            default:
                dc = DayClustering.TD7;
                break;
        }
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return RegressionUtility.data(Collections.singletonList(new GenericTradingDaysVariables(gtd)), domain);
    }

    private static class TDvarMapping implements IParametricMapping<Airline> {

        private final Matrix td;
        private static final SarimaMapping airlineMapping;

        static {
            SarimaSpecification spec = new SarimaSpecification();
            spec.airline(12);
            airlineMapping = SarimaMapping.of(spec);
        }

        TDvarMapping(Matrix td) {
            this.td = td;
        }

        public Matrix getTd() {
            return td;
        }

        @Override
        public Airline map(DoubleSequence p) {
            Airline airline = new Airline();
            airline.setTd(td);
            airline.setTheta(p.get(0));
            airline.setBtheta(p.get(1));
            airline.setRegVariance(p.get(2));
            return airline;
        }

        @Override
        public DoubleSequence map(Airline t) {
            double[] p = new double[3];
            p[0] = t.getTheta();
            p[1] = t.getBtheta();
            p[2] = t.getRegVariance();
            return DoubleSequence.ofInternal(p);
        }

        @Override
        public boolean checkBoundaries(DoubleSequence inparams) {
            return airlineMapping.checkBoundaries(inparams.extract(0, 2));
        }

        @Override
        public double epsilon(DoubleSequence inparams, int idx) {
            if (idx < 2) {
                return airlineMapping.epsilon(inparams, idx);
            }
            return inparams.get(2) * .001;
        }

        @Override
        public int getDim() {
            return 3;
        }

        @Override
        public double lbound(int idx) {
            if (idx < 2) {
                return airlineMapping.lbound(idx);
            } else {
                return -100;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx < 2) {
                return airlineMapping.ubound(idx);
            } else {
                return 100;
            }
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            ParamValidation pv = ParamValidation.Valid;
            if (ioparams.get(2) < -10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, -10);
            }
            if (ioparams.get(2) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, 10);
            }
            ParamValidation pv2 = airlineMapping.validate(ioparams.extract(0, 2));
            if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
                return ParamValidation.Valid;
            }
            if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
                return ParamValidation.Invalid;
            }
            return ParamValidation.Changed;
        }

        @Override
        public String getDescription(int idx) {
            if (idx < 2) {
                return airlineMapping.getDescription(idx);
            } else {
                return "noise stdev";
            }
        }

        @Override
        public DoubleSequence getDefault() {
            return DoubleSequence.of(-.6, -.6, 1);
        }
    }

}
