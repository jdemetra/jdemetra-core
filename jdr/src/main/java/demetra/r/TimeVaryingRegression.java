/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.arima.ssf.SsfArima;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.information.InformationMapping;
import demetra.maths.MatrixType;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.QuadraticForm;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.processing.IProcResults;
import demetra.r.mapping.DkLikelihoodInformationMapping;
import demetra.r.mapping.SarimaInformationMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.estimation.SarimaMapping;
import demetra.ssf.dk.DkConcentratedLikelihood;
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
import java.util.LinkedHashMap;
import java.util.Map;

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

    @lombok.Value
    @lombok.Builder
    public static class Results implements IProcResults {

        RegularDomain domain;
        MatrixType variables;
        MatrixType coefficients;
        MatrixType coefficientsStde;
        SarimaModel arima;
        DkConcentratedLikelihood ll;

        private static final String ARIMA = "arima", LL = "likelihood", STDCOEFF = "coefficients.stde", COEFF = "coefficients.value", TD = "td", TDEFFECT = "tdeffect";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(ARIMA, SarimaInformationMapping.getMapping(), r -> r.getArima());
            MAPPING.delegate(LL, DkLikelihoodInformationMapping.getMapping(), r -> r.getLl());
            MAPPING.set(COEFF, MatrixType.class, r -> r.getCoefficients());
            MAPPING.set(STDCOEFF, MatrixType.class, r -> r.getCoefficientsStde());
            MAPPING.set(TD, MatrixType.class, r -> r.getVariables());
            MAPPING.set(TDEFFECT, TsData.class, r
                    -> {
                DataBlock tmp = DataBlock.make(r.getDomain().length());
                DataBlock prod = DataBlock.make(r.getDomain().length());
                for (int i = 0; i < r.variables.getColumnsCount(); ++i) {
                    prod.set(r.getCoefficients().column(i), r.getVariables().column(i), (a, b) -> a * b);
                    tmp.add(prod);
                }
                return TsData.ofInternal(r.getDomain().getStartPeriod(), tmp);
            });
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }
    }

    public Results regarima(TsData s, String td, String svar) {
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        DayClustering dc = days(td);
        Matrix mtd = generate(s.domain(), dc);
        Matrix nvar = generateVar(dc, svar);
        SsfData data = new SsfData(s.values());
        TDvarMapping mapping = new TDvarMapping(mtd);
        SsfFunction<Airline, ISsf> fn = SsfFunction.builder(data, mapping,
                params
                -> {
            SarimaModel arima = SarimaModel.builder(spec)
                    .theta(params.getTheta())
                    .btheta(params.getBtheta())
                    .build();
            SsfArima ssf = SsfArima.of(arima);
            double nv = params.getRegVariance();
            if (nv != 0) {
                Matrix v = nvar.deepClone();
                v.mul(nv);
                return RegSsf.ofTimeVarying(ssf, mtd, v, nvar);
            } else {
                return RegSsf.of(ssf, mtd);
            }
        }).build();

        LevenbergMarquardtMinimizer min = new LevenbergMarquardtMinimizer();
        min.minimize(fn.ssqEvaluate(mapping.getDefault()));
        SsfFunctionPoint<Airline, ISsf> rfn = (SsfFunctionPoint<Airline, ISsf>) min.getResult();
        DefaultSmoothingResults fs = DkToolkit.sqrtSmooth(rfn.getSsf(), data, true);
        Matrix c = Matrix.make(mtd.getRowsCount(), mtd.getColumnsCount() + 1);
        Matrix ec = Matrix.make(mtd.getRowsCount(), mtd.getColumnsCount() + 1);

        int del = 14;
        double nwe = dc.getGroupCount(0);
        double[] z = new double[c.getColumnsCount() - 1];
        for (int i = 0; i < z.length; ++i) {
            c.column(i).copy(fs.getComponent(del + i));
            ec.column(i).copy(fs.getComponentVariance(del + i));
            z[i] = dc.getGroupCount(i + 1) / nwe;
            c.column(z.length).addAY(-z[i], c.column(i));
        }
        DataBlock Z = DataBlock.ofInternal(z);
        for (int i = 0; i < c.getRowsCount(); ++i) {
            Matrix var = fs.P(i).dropTopLeft(del, del);
            ec.set(i, z.length, QuadraticForm.apply(var, Z));
        }
        ec.apply(x -> x <= 0 ? 0 : Math.sqrt(x));

        SarimaModel arima = SarimaModel.builder(spec)
                .theta(rfn.getParameters().get(0))
                .btheta(rfn.getParameters().get(1))
                .build();
        return Results.builder()
                .domain(s.domain())
                .arima(arima)
                .ll(rfn.getLikelihood())
                .variables(mtd)
                .coefficients(c)
                .coefficientsStde(ec)
                .build();
    }

    private DayClustering days(String td) {
        DayClustering dc;
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
        return dc;
    }

    private Matrix generate(RegularDomain domain, DayClustering dc) {
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return RegressionUtility.data(Collections.singletonList(new GenericTradingDaysVariables(gtd)), domain);
    }

    private Matrix generateVar(DayClustering dc, String var) {
        int groupsCount = dc.getGroupsCount();
        Matrix full = Matrix.square(7);
        if (!var.equalsIgnoreCase("Contrasts")) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        Matrix Q = Matrix.make(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.XSXt(full, Q);
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
            return inparams.get(2) * .0001;
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
                return -10;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx < 2) {
                return airlineMapping.ubound(idx);
            } else {
                return 10;
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
