/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.tdvar;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.jdr.mapping.DiffuseLikelihoodInfo;
import ec.tstoolkit.jdr.mapping.SarimaInfo;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.maths.realfunctions.minpack.LevenbergMarquardtMinimizer;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.RegSsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.ssf.SsfModelData;
import ec.tstoolkit.ssf.TimeVaryingRegSsf;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.regression.GenericTradingDaysVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TimeVaryingRegression {

    @lombok.Value
    @lombok.Builder
    public static class Results implements IProcResults {

        TsDomain domain;
        Matrix variables;
        Matrix coefficients;
        Matrix coefficientsStde;
        SarimaModel arima, arima0;
        double nvar;
        DiffuseConcentratedLikelihood ll0, ll;

        private static final String ARIMA = "arima", LL = "likelihood", STDCOEFF = "coefficients.stde", COEFF = "coefficients.value", TD = "td", TDEFFECT = "tdeffect";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(ARIMA, SarimaInfo.getMapping(), r -> r.getArima());
            MAPPING.delegate("arima0", SarimaInfo.getMapping(), r -> r.getArima0());
            MAPPING.delegate(LL, DiffuseLikelihoodInfo.getMapping(), r -> r.getLl());
            MAPPING.delegate("likelihood0", DiffuseLikelihoodInfo.getMapping(), r -> r.getLl0());
            MAPPING.set("aic0", Double.class, r -> r.ll0.AIC(2));
            MAPPING.set("aic", Double.class, r -> r.ll.AIC(3));
            MAPPING.set("tdvar", Double.class, r -> r.getNvar());
            MAPPING.set(COEFF, Matrix.class, r -> r.getCoefficients());
            MAPPING.set(STDCOEFF, Matrix.class, r -> r.getCoefficientsStde());
            MAPPING.set(TD, Matrix.class, r -> r.getVariables());
            MAPPING.set(TDEFFECT, TsData.class, r
                    -> {
                DataBlock tmp = new DataBlock(r.getDomain().getLength());
                DataBlock prod = new DataBlock(r.getDomain().getLength());
                for (int i = 0; i < r.variables.getColumnsCount(); ++i) {
                    prod.set(r.getCoefficients().column(i), r.getVariables().column(i), (a, b) -> a * b);
                    tmp.add(prod);
                }
                return new TsData(r.getDomain().getStart(), tmp);
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

    public Results regarima(TsData s, String td, String svar, double diffAic) {
        DayClustering dc = days(td);
        Matrix mtd = generate(s.getDomain(), dc);
        Matrix nvar = generateVar(dc, svar);
        SsfData data = new SsfData(s, null);
        // step 0 fixed model
        TDvarMapping mapping0 = new TDvarMapping(s.getFrequency().intValue(), mtd, null);
        ReadDataBlock p0 = new ReadDataBlock(new double[]{-.6, -.6});
        // Create the function
        SsfModel<ISsf> model = new SsfModel<>(mapping0.map(p0), new SsfData(s, null), null, null);
        SsfFunction<ISsf> fn0= new SsfFunction<>(model, mapping0, new SsfAlgorithm());
        SarimaSpecification spec = new SarimaSpecification(s.getFrequency().intValue());
        spec.airline();

        LevenbergMarquardtMethod min = new LevenbergMarquardtMethod();
        min.setConvergenceCriterion(1e-12);
        min.minimize(fn0, p0);
        SsfFunctionInstance<ISsf> rfn0 = (SsfFunctionInstance<ISsf>) min.getResult();
        DiffuseConcentratedLikelihood ll0 = rfn0.getLikelihood();
        IReadDataBlock ep0 = rfn0.getParameters();
        SarimaModel arima0 = new SarimaModel(spec);
        arima0.setTheta(1, ep0.get(0));
        arima0.setBTheta(1, ep0.get(1));

        TDvarMapping mapping = new TDvarMapping(s.getFrequency().intValue(), mtd, nvar);
        ReadDataBlock p = new ReadDataBlock(new double[]{ep0.get(0), ep0.get(1), 0.0001});
        // Create the function
        model = new SsfModel<>(mapping.map(p), new SsfData(s, null), null, null);
        SsfFunction<ISsf> fn = new SsfFunction<>(model, mapping, new SsfAlgorithm());
        min.minimize(fn, p);
        SsfFunctionInstance<ISsf> rfn = (SsfFunctionInstance<ISsf>) min.getResult();
        DiffuseConcentratedLikelihood ll = rfn.getLikelihood();
        IReadDataBlock ep = rfn.getParameters();
        SarimaModel arima = new SarimaModel(spec);
        arima.setTheta(1, ep.get(0));
        arima.setBTheta(1, ep.get(1));
        double tdvar=ep.get(2);

        ISsf ssf;
        double aic0=ll0.AIC(2), aic=ll.AIC(3);
        if (aic+diffAic < aic0){
            ssf=rfn.ssf;
        }
        else
            ssf=rfn0.ssf;

        Smoother smoother = new Smoother();
        smoother.setSsf(ssf);
        smoother.setCalcVar(true);
        SmoothingResults fs = new SmoothingResults(true, true);
        smoother.process(data, fs);
        Matrix c = new Matrix(mtd.getRowsCount(), mtd.getColumnsCount() + 1);
        Matrix ec = new Matrix(mtd.getRowsCount(), mtd.getColumnsCount() + 1);

        int del = s.getFrequency().intValue()+2;
        double nwe = dc.getGroupCount(0);
        double[] z = new double[c.getColumnsCount() - 1];
        for (int i = 0; i < z.length; ++i) {
            c.column(i).copyFrom(fs.component(del + i), 0);
            ec.column(i).copyFrom(fs.componentVar(del + i), 0);
            z[i] = dc.getGroupCount(i + 1) / nwe;
            c.column(z.length).addAY(-z[i], c.column(i));
        }
        DataBlock Z = new DataBlock(z);
        double v=fs.getStandardError()*fs.getStandardError();
        
        for (int i = 0; i < c.getRowsCount(); ++i) {
            SubMatrix var = fs.P(i);
            int n = var.getRowsCount();
            var = var.extract(del, n, del, n);
            ec.set(i, z.length, SymmetricMatrix.quadraticForm(var, Z)*v);
        }

        double[] sec = ec.internalStorage();
        for (int i = 0; i < sec.length; ++i) {
            sec[i] = sec[i] <= 0 ? 0 : Math.sqrt(sec[i]);
        }

        return Results.builder()
                .domain(s.getDomain())
                .arima0(arima0)
                .ll0(ll0)
                .arima(arima)
                .ll(ll)
                .variables(mtd)
                .coefficients(c)
                .coefficientsStde(ec)
                .nvar(tdvar)
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

    private Matrix generate(TsDomain domain, DayClustering dc) {
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return ec.tstoolkit.timeseries.regression.RegressionUtilities.matrix(new GenericTradingDaysVariables(gtd), domain);
    }

    private Matrix generateVar(DayClustering dc, String var) {
        int groupsCount = dc.getGroupsCount();
        Matrix full = Matrix.square(7);
        if (!var.equalsIgnoreCase("Contrasts")) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        Matrix Q = new Matrix(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.quadraticFormT(full, Q);
    }

    private static class TDvarMapping implements IParametricMapping<ISsf> {

        private final int frequency;
        private final Matrix td; // regression variable
        private final Matrix nvar; // unscaled covariance matrix for var coefficients
        private static final SarimaMapping airlineMapping;

        static {
            SarimaSpecification spec = new SarimaSpecification(12);
            spec.airline();
            airlineMapping = new SarimaMapping(spec, true);
        }

        TDvarMapping(int freq, Matrix td, Matrix nvar) {
            this.frequency = freq;
            this.td = td;
            this.nvar = nvar;
        }

        @Override
        public ISsf map(IReadDataBlock p) {
            SarimaSpecification spec = new SarimaSpecification(frequency);
            spec.airline();
            SarimaModel arima = new SarimaModel(spec);
            arima.setTheta(1, p.get(0));
            arima.setBTheta(1, p.get(1));
            SsfArima ssf = new SsfArima(arima);
            if (nvar != null) {
                double nv = p.get(2);
                Matrix v = nvar.clone();
                v.mul(nv);
                return new TimeVaryingRegSsf(ssf, td.all(), v);
            } else {
                return new RegSsf(ssf, td.all());
            }
        }

        @Override
        public IReadDataBlock map(ISsf t) {
            if (t instanceof TimeVaryingRegSsf) {
                TimeVaryingRegSsf ssf = (TimeVaryingRegSsf) t;
                SsfArima ssfarima = (SsfArima) ssf.getCoreSsf();
                SarimaModel arima = (SarimaModel) ssfarima.getModel();
                Matrix fnv = ssf.getFullNoiseVar();
                double[] p = new double[]{arima.theta(1), arima.btheta(1), fnv.diagonal().sum() / nvar.diagonal().sum()};
                return new ReadDataBlock(p);
            } else {
                RegSsf ssf = (RegSsf) t;
                SsfArima ssfarima = (SsfArima) ssf.getCoreSsf();
                SarimaModel arima = (SarimaModel) ssfarima.getModel();
                double[] p = new double[]{arima.theta(1), arima.btheta(1)};
                return new ReadDataBlock(p);

            }
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            if (nvar != null) {
                return inparams.get(2) >= 0 && inparams.get(2) < 10 && airlineMapping.checkBoundaries(inparams.rextract(0, 2));
            } else {
                return airlineMapping.checkBoundaries(inparams.rextract(0, 2));
            }
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            if (idx < 2) {
                return airlineMapping.epsilon(inparams, idx);
            }
            return Math.max(inparams.get(2) * .001, 1e-9);
        }

        @Override
        public int getDim() {
            return nvar == null ? 2 : 3;
        }

        @Override
        public double lbound(int idx) {
            if (idx < 2) {
                return airlineMapping.lbound(idx);
            } else {
                return 0;
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
        public ParamValidation validate(IDataBlock ioparams) {
            ParamValidation pv = ParamValidation.Valid;
            if (nvar != null && ioparams.get(2) < 0) {
                pv = ParamValidation.Changed;
                ioparams.set(2, Math.min(10, -ioparams.get(2)));
            }
            if (nvar != null && ioparams.get(2) > 10) {
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
    }

}
