/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.diagnostics.QSTest;
import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.satoolkit.diagnostics.StationaryVarianceDecomposition;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.jdr.tests.TradingDaysTests;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DifferencingResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;
import jdr.spec.ts.Utility;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Processor {

    public static TramoSeatsResults tramoseats(TsData s, TramoSeatsSpecification spec, Utility.Dictionary dic) {
        ProcessingContext context = null;
        if (dic != null) {
            context = dic.toContext();
        }
        return tramoseatsWithContext(s, spec, context);
    }

    public static TramoSeatsResults tramoseatsWithContext(TsData s, TramoSeatsSpecification spec, ProcessingContext context) {
        CompositeResults rslts = TramoSeatsProcessingFactory.process(s, spec, context);
        SeatsResults seats = rslts.get(TramoSeatsProcessingFactory.DECOMPOSITION, SeatsResults.class);

        TsData sa = seats.getSeriesDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        TsData seas = seats.getSeriesDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        TsData irr = seats.getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value);
        boolean mul = isMultiplicative(rslts);
        TsData si = mul ? TsData.multiply(seas, irr) : TsData.add(seas, irr);
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.last(si.getFrequency().intValue() * 3);
        TsData sic = si.select(sel);
        int lag = s.getFrequency().intValue() / 4;
        if (lag == 0) {
            lag = 1;
        }
        if (mul) {
            sa = sa.log();
        }
        TsData dsa = sa.delta(lag);
        TsData dsac = dsa.select(sel);
        StationaryVarianceDecomposition var = new StationaryVarianceDecomposition();
        var.process(rslts);

        SaDiagnostics diags = SaDiagnostics.builder()
                .qs(qs(rslts))
                .ftest(f(rslts))
                .combinedSeasonality(new CombinedSeasonalityTest(si, mul))
                .combinedSeasonalityOnEnd(new CombinedSeasonalityTest(sic, mul))
                .residualSeasonality(SeasonalityTest.stableSeasonality(dsa))
                .residualSeasonalityOnEnd(SeasonalityTest.stableSeasonality(dsac))
                .residualTradingDays(TradingDaysTests.ftest(sa, true, NY))
                .varDecomposition(var)
                .build();
        return new TramoSeatsResults(rslts, diags);
    }

    static final int NY = 8;

    public static X13Results x13(TsData s, X13Specification spec, Utility.Dictionary dic) {
        ProcessingContext context = null;
        if (dic != null) {
            context = dic.toContext();
        }
        return x13WithContext(s, spec, context);
    }

    public static X13Results x13WithContext(TsData s, X13Specification spec, ProcessingContext context) {
        CompositeResults rslts = X13ProcessingFactory.process(s, spec, context);
        X11Results x11 = rslts.get(X13ProcessingFactory.DECOMPOSITION, X11Results.class);
        TsData d8 = x11.getData("d-tables.d8", TsData.class);
        TsData sa = x11.getData("d-tables.d11", TsData.class);
        boolean mul = isMultiplicative(rslts);
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.last(d8.getFrequency().intValue() * 3);
        TsData d8c = d8.select(sel);
        int lag = s.getFrequency().intValue() / 4;
        if (lag == 0) {
            lag = 1;
        }
        if (mul) {
            sa = sa.log();
        }
        TsData dsa = sa.delta(lag);
        TsData dsac = dsa.select(sel);
        StationaryVarianceDecomposition var = new StationaryVarianceDecomposition();
        var.process(rslts);

        SaDiagnostics diags = SaDiagnostics.builder()
                .qs(qs(rslts))
                .ftest(f(rslts))
                .combinedSeasonality(new CombinedSeasonalityTest(d8, mul))
                .combinedSeasonalityOnEnd(new CombinedSeasonalityTest(d8c, mul))
                .residualSeasonality(SeasonalityTest.stableSeasonality(dsa))
                .residualSeasonalityOnEnd(SeasonalityTest.stableSeasonality(dsac))
                .residualTradingDays(TradingDaysTests.ftest(sa, true, NY))
                .varDecomposition(var)
                .build();
        return new X13Results(rslts, diags);

    }

    private static StatisticalTest qs(CompositeResults rslts) {
        TsData sa = rslts.getData(ModellingDictionary.SA_CMP, TsData.class);
        boolean mul = isMultiplicative(rslts);
        if (sa != null) {
            TsData sac = sa;
            if (mul) {
                sac = sac.log();
            }
            int ifreq = sac.getFrequency().intValue();
            DifferencingResults dsa = DifferencingResults.create(sac, -1, true);
            return StatisticalTest.create(QSTest.compute(dsa.getDifferenced().internalStorage(), ifreq, 2));
        } else {
            return null;
        }
    }

    private static StatisticalTest f(CompositeResults rslts) {
        TsData sa = rslts.getData(ModellingDictionary.SA_CMP, TsData.class);
        if (sa == null) {
            return null;
        }
        boolean mul = isMultiplicative(rslts);
        TsData sac = sa;
        if (mul) {
            sac = sac.log();
        }
        int ifreq = sac.getFrequency().intValue();
        TsData salast = sac;
        salast = sac.drop(Math.max(0, sac.getLength() - ifreq * NY - 1), 0);
        return StatisticalTest.create(processAr(salast));
    }

    private static boolean isMultiplicative(CompositeResults rslts) {
        DecompositionMode mul = rslts.getData(ModellingDictionary.MODE, DecompositionMode.class);
        return mul != null && mul.isMultiplicative();
    }

    private static ec.tstoolkit.stats.StatisticalTest processAr(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            TsDomain edomain = s.getDomain().drop(1, 0);
            SeasonalDummies dummies = new SeasonalDummies(edomain.getFrequency());
            List<DataBlock> regs = RegressionUtilities.data(dummies, edomain);
            reg.addX(y.drop(0, 1));
            for (DataBlock r : regs) {
                reg.addX(r);
            }
            reg.setMeanCorrection(true);
            int nseas = dummies.getDim();
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            Ols ols = new Ols();
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 2, nseas, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }
}
