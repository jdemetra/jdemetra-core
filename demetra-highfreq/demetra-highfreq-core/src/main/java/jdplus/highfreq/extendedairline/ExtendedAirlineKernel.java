/*
 * Copyright 2023 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.highfreq.extendedairline;

import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.modelling.highfreq.EasterSpec;
import demetra.highfreq.ExtendedAirline;
import demetra.highfreq.ExtendedAirlineModellingSpec;
import demetra.highfreq.ExtendedAirlineSpec;
import demetra.modelling.highfreq.HolidaysSpec;
import demetra.modelling.highfreq.OutlierSpec;
import demetra.modelling.highfreq.RegressionSpec;
import demetra.math.matrices.Matrix;
import demetra.modelling.OutlierDescriptor;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.SaVariable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.HolidaysVariable;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.JulianEasterVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.ModellingUtility;
import demetra.timeseries.regression.SwitchOutlier;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.highfreq.regarima.HighFreqRegArimaModel;
import jdplus.highfreq.regarima.ModelDescription;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.GenericOutliersDetection;
import jdplus.regarima.ami.OutliersDetectionModule;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ExtendedAirlineKernel {

    public static final String EA = "extended airline";

    private final ExtendedAirlineModellingSpec spec;
    private final ModellingContext modellingContext;

    public ExtendedAirlineKernel(ExtendedAirlineModellingSpec spec, ModellingContext context) {
        this.spec = spec;
        this.modellingContext = context;
    }

    public HighFreqRegArimaModel process(TsData y, ProcessingLog log) {

        if (log == null) {
            log = ProcessingLog.dummy();
        }
        log.push(EA);
        ModelDescription desc = build(y, log);
        if (desc == null) {
            throw new ExtendedAirlineException("Initialization failed");
        }
        ExtendedRegAirlineModelling modelling = ExtendedRegAirlineModelling.of(desc, log);
        HighFreqRegArimaModel rslt = exec(modelling, log);
        log.pop();
        // step 1. Build the model

        return rslt;
    }

    private ModelDescription<ArimaModel, ExtendedAirlineDescription> build(TsData originalTs, ProcessingLog log) {
        TsData y = originalTs.select(spec.getSeries().getSpan());
        ModelDescription<ArimaModel, ExtendedAirlineDescription> desc = new ModelDescription(y, y.getDomain().select(spec.getEstimate().getSpan()));
        // regression variables
        desc.setMean(spec.getStochastic().isMean());
        // calendar
        buildCalendar(desc);
        buildOutliers(desc);
        buildInterventionVariables(desc);
        buildUsers(desc);

        desc.setStochasticSpec(new ExtendedAirlineDescription(spec.getStochastic()));
        return desc;
    }

    private void buildCalendar(ModelDescription desc) {
        RegressionSpec regression = spec.getRegression();
        HolidaysSpec calendar = regression.getHolidays();
        if (calendar.isUsed()) {
            HolidaysVariable hvar = HolidaysVariable.of(calendar.getHolidays(),
                    calendar.getHolidaysOption(), calendar.getNonWorkingDays(), calendar.isSingle(), modellingContext);
            add(desc, hvar, "holidays", ComponentType.CalendarEffect, calendar.getCoefficients());
        }
        EasterSpec easter = regression.getEaster();
        if (easter.isUsed()) {
            IEasterVariable ev;
            if (easter.isJulian()) {
                ev = new JulianEasterVariable(easter.getDuration(), true);
            } else {
                ev = EasterVariable.builder()
                        .duration(easter.getDuration())
                        .meanCorrection(EasterVariable.Correction.Simple)
                        .endPosition(-1)
                        .build();
            }
            Parameter ec = easter.getCoefficient();
            add(desc, ev, "easter", ComponentType.CalendarEffect, ec == null ? null : new Parameter[]{ec});
        }
    }

    private void buildOutliers(ModelDescription desc) {
        RegressionSpec regression = spec.getRegression();
        List<Variable<IOutlier>> outliers = regression.getOutliers();
        for (Variable<IOutlier> outlier : outliers) {
            IOutlier cur = outlier.getCore();
            String code = cur.getCode();
            LocalDateTime pos = cur.getPosition();
            IOutlier v;
            ComponentType cmp = ComponentType.Undefined;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROSTARTED.make(pos);
                    cmp = ComponentType.Trend;
                    break;
                case SwitchOutlier.CODE:
                    v = SwitchOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                default:
                    v = null;
            }
            if (v != null) {
                Variable nvar = outlier.withCore(v);
                if (!nvar.hasAttribute(SaVariable.REGEFFECT)) {
                    nvar = nvar.setAttribute(SaVariable.REGEFFECT, cmp.name());
                }
                desc.addVariable(nvar);
            }
        }
    }

    private void addOutliers(ModelDescription desc, int[][] io) {
        OutlierSpec ospec = spec.getOutlier();
        String[] outliers = ospec.allOutliers();
        TsDomain edom = desc.getEstimationDomain();
        for (int i = 0; i < io.length; ++i) {
            int[] cur = io[i];
            TsPeriod pos = edom.get(cur[0]);
            IOutlier o = outlier(outliers[cur[1]], pos);
            desc.addVariable(Variable.variable(IOutlier.defaultName(o.getCode(), pos), o, attributes(o)));
        }
    }

    private void buildInterventionVariables(ModelDescription desc) {
        for (Variable<InterventionVariable> iv : spec.getRegression().getInterventionVariables()) {
            desc.addVariable(iv);
        }
    }

    private void buildUsers(ModelDescription desc) {
        for (Variable<TsContextVariable> user : spec.getRegression().getUserDefinedVariables()) {
            String name = user.getName();
            ITsVariable var = user.getCore().instantiateFrom(modellingContext, name);
            desc.addVariable(user.withCore(var));
        }
    }

    private HighFreqRegArimaModel exec(ExtendedRegAirlineModelling modelling, ProcessingLog log) {
        // step 1: log/level
        execTransform(modelling, log);
        // step 2: outliers
        if (spec.getOutlier().isUsed()) {
            if (modelling.needEstimation()) {
                modelling.estimate(1e-5);
            }
            execOutliers(modelling, log);

        }
        // step 3: final estimation
        modelling.estimate(spec.getEstimate().getPrecision());

        return HighFreqRegArimaModel.of(modelling.getDescription(), modelling.getEstimation(), log);
    }

    private void add(@NonNull ModelDescription model, ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, Parameter[] c) {
        if (v == null) {
            return;
        }
        Variable var = Variable.builder()
                .name(name)
                .core(v)
                .coefficients(c)
                .attribute(SaVariable.REGEFFECT, cmp.name())
                .build();
        model.addVariable(var);
    }

    public static ExtendedAirlineEstimation fastProcess(DoubleSeq y, Matrix X, boolean mean, String[] outliers, double cv, ExtendedAirlineSpec spec, double eps) {
        final ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        //
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .addX(FastMatrix.of(X))
                .arima(mapping.getDefault())
                .meanCorrection(mean);
        OutlierDescriptor[] o = null;
        if (outliers != null && outliers.length > 0) {
            GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                    .precision(1e-5)
                    .build();
            IOutlierFactory[] factories = factories(outliers);
            OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                    .maxOutliers(100)
                    .addFactories(factories)
                    .processor(processor)
                    .build();

            cv = Math.max(cv, GenericOutliersDetection.criticalValue(y.length(), 0.01));
            od.setCriticalValue(cv);

            RegArimaModel regarima = builder.build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, mapping);
            int[][] io = od.getOutliers();
            o = new OutlierDescriptor[io.length];
            for (int i = 0; i < io.length; ++i) {
                int[] cur = io[i];
                DataBlock xcur = DataBlock.make(y.length());
                factories[cur[1]].fill(cur[0], xcur);
                o[i] = new OutlierDescriptor(factories[cur[1]].getCode(), cur[0]);
                builder.addX(xcur);
            }
        } else {
            o = new OutlierDescriptor[0];
        }
        RegArimaModel regarima = builder.build();
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(eps)
                .computeExactFinalDerivatives(true)
                .build();
        RegArimaEstimation rslt = finalProcessor.process(regarima, mapping);
        LogLikelihoodFunction.Point max = rslt.getMax();
        DoubleSeq parameters = max.getParameters();

        ExtendedAirline ea = ExtendedAirline.of(spec)
                .toBuilder()
                .p(parameters)
                .build();

        return ExtendedAirlineEstimation.builder()
                .y(regarima.getY().toArray())
                .x(regarima.variables())
                .model(ea)
                .coefficients(rslt.getConcentratedLikelihood().coefficients())
                .coefficientsCovariance(rslt.getConcentratedLikelihood().covariance(mapping.getDim(), true))
                .likelihood(rslt.statistics())
                .residuals(rslt.getConcentratedLikelihood().e())
                .outliers(o)
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .build();
    }

    private void execTransform(ExtendedRegAirlineModelling modelling, ProcessingLog log) {
        log.push("log/level");
        switch (spec.getTransform().getFunction()) {
            case Auto:
                log.warning("not implemented yet. log used");
            case Log:
                if (modelling.getDescription().getSeries().getValues().allMatch(x -> x > 0)) {
                    modelling.getDescription().setLogTransformation(true);
                } else {
                    log.warning("non positive values; log disabled");
                }
                break;

        }
        log.pop();
    }

    private void execOutliers(ExtendedRegAirlineModelling modelling, ProcessingLog log) {
        log.push("outliers");
        OutlierSpec ospec = spec.getOutlier();
        String[] outliers = ospec.allOutliers();
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-5)
                .build();
        IOutlierFactory[] factories = factories(outliers);
        OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                .maxOutliers(spec.getOutlier().getMaxOutliers())
                .maxRound(spec.getOutlier().getMaxRound())
                .addFactories(factories)
                .processor(processor)
                .build();
        double cv = ospec.getCriticalValue();

        RegArimaModel<ArimaModel> regarima = modelling.getDescription().regarima();
        TsDomain edom = modelling.getDescription().getEstimationDomain();
        cv = Math.max(cv, GenericOutliersDetection.criticalValue(edom.getLength(), 0.01));
        od.setCriticalValue(cv);
        od.prepare(edom.getLength());
        TsDomain odom = edom.select(ospec.getSpan());
        int nb = edom.getStartPeriod().until(odom.getStartPeriod());
        od.setBounds(nb, nb + odom.getLength());
        // remove missing values
        int[] missing = modelling.getDescription().getMissingInEstimationDomain();
        if (missing != null) {
            for (int i = 0; i < missing.length; ++i) {
                for (int j = 0; j < outliers.length; ++j) {
                    od.exclude(missing[i], j);
                }
            }
        }
        // current outliers ([fixed], pre-specified, identified)
        modelling.getDescription().variables()
                .filter(var -> var.getCore() instanceof IOutlier)
                .map(var -> (IOutlier) var.getCore()).forEach(
                o -> od.exclude(edom.indexOf(o.getPosition()), outlierType(outliers, o.getCode())));
        
        ExtendedAirlineMapping mapping = (ExtendedAirlineMapping) modelling.getDescription().mapping();

        od.process(regarima, mapping);
        int[][] io = od.getOutliers();
        if (io.length > 0) {
            addOutliers(modelling.getDescription(), io);
            modelling.clearEstimation();
        }
        log.pop();
    }

    private static int outlierType(String[] all, String cur) {
        for (int i = 0; i < all.length; ++i) {
            if (cur.equals(all[i])) {
                return i;
            }
        }
        return -1;
    }

    private static IOutlierFactory[] factories(String[] code) {
        List<IOutlierFactory> fac = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            switch (code[i]) {
                case "ao", "AO" -> fac.add(AdditiveOutlierFactory.FACTORY);
                case "wo", "WO" -> fac.add(SwitchOutlierFactory.FACTORY);
                case "ls", "LS" -> fac.add(LevelShiftFactory.FACTORY_ZEROENDED);
            }
        }

        return fac.toArray(IOutlierFactory[]::new);
    }

    private static IOutlier outlier(String code, TsPeriod p) {
        LocalDateTime pos = p.start();
        return switch (code) {
            case "ao", "AO" -> AdditiveOutlierFactory.FACTORY.make(pos);
            case "wo", "WO" -> SwitchOutlierFactory.FACTORY.make(pos);
            case "ls", "LS" -> LevelShiftFactory.FACTORY_ZEROENDED.make(pos);
            default -> null;
        };
    }

    private Map<String, String> attributes(IOutlier o) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(ModellingUtility.AMI, "tramo");
        attributes.put(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(o).name());
        return attributes;
    }

    public static ArimaModel estimate(DoubleSeq s, double period) {
        ExtendedAirlineMapping mapping = new ExtendedAirlineMapping(new double[]{period});

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class);
        builder.minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-12)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.<ArimaModel>builder()
                        .y(s)
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima, mapping);
        return rslt.getModel().arima();
    }

}
