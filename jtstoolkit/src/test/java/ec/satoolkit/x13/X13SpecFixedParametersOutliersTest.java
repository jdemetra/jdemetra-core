/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import static data.DataFixCoef.*;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.NameManager;
import org.junit.Test;
import ec.satoolkit.GenericSaResults;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.regression.TsVariableSelection.Item;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import utilities.CompareTsData;

/**
 *
 * @author Christiane Hofer
 */
public class X13SpecFixedParametersOutliersTest {

    @Test
    public void OutlierAO_notfixed() {
        X13Specification x13spec = makeX13Spec();
        // I have to set       tr.setFunction(DefaultTransformationType.Auto); I can' reproduce the results with log nor with level; It should haben been logs
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(ABC1);
        PreprocessingModel preprocessingModel;
        preprocessingModel = GenericSaResults.getPreprocessingModel(comprest);
        ConcentratedLikelihood concentratedLikelihood = preprocessingModel.estimation.getLikelihood();
        double[] b;
        b = concentratedLikelihood.getB();
        TsVariableList tsVariableList;
        tsVariableList = preprocessingModel.description.buildRegressionVariables();
        TsVariableSelection<IOutlierVariable> regs = tsVariableList.select(IOutlierVariable.class);
        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();
        Assert.assertEquals("Expected Number of Outliers ist wrong", 2, preprocessingModel.description.getOutliers().size());
        Assert.assertEquals("value outlier 1 is wrong: ", -0.042442334225138614, b[start + regs.elements()[0].position], 0.0000000000000001);
        Assert.assertEquals("outlier description 1 is wrong: ", "AO (12-2003)", regs.elements()[0].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        Assert.assertEquals("value outlier 2 is wrong: ", 0.0404503586051786, b[start + regs.elements()[1].position], 0.0000000000000001);
        Assert.assertEquals("outlier description 2 is wrong: ", "AO (12-2007)", regs.elements()[1].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(ABC1B1, comprest.getData("b-tables.b1", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(ABC1D10, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
    }

    @Test
    public void OutlierAO_fixed_1() {
        X13Specification x13spec = makeX13Spec_fixed_1();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertEquals("B1(0) is wrong", 50322336.55610, comprest.getData("b-tables.b1", TsData.class).get(0), 0.00001);
    }

    @Test
    public void OutlierAO_fixed_2() {
        X13Specification x13spec = makeX13Spec_fixed_2();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertEquals("B1(0) is wrong", 50322336.55610, comprest.getData("b-tables.b1", TsData.class).get(0), 0.00001);
        Assert.assertEquals("B1(12) is wrong", 22009633.08742, comprest.getData("b-tables.b1", TsData.class).get(12), 0.00001);
    }

    @Test
    public void OutlierAO_fixed_2_AO() {
        X13Specification x13spec = makeX13Spec_fixed_2_AO();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(ABC1);

        PreprocessingModel preprocessingModel;
        preprocessingModel = GenericSaResults.getPreprocessingModel(comprest);
        ConcentratedLikelihood concentratedLikelihood = preprocessingModel.estimation.getLikelihood();
        double[] b;
        b = concentratedLikelihood.getB();
        TsVariableList tsVariableList;
        tsVariableList = preprocessingModel.description.buildRegressionVariables();
        TsVariableSelection<IOutlierVariable> regs = tsVariableList.select(IOutlierVariable.class);

        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();

        Assert.assertEquals("Expected Number of Outliers ist wrong", 6, preprocessingModel.description.getOutliers().size());
        Assert.assertEquals("B1(0) is wrong", 50322336.55610, comprest.getData("b-tables.b1", TsData.class).get(0), 0.0001);
        Assert.assertEquals("B1(12) is wrong", 22009633.08742, comprest.getData("b-tables.b1", TsData.class).get(12), 0.0001);

//      The order of the outliers may be different ! be careful
        int pos = search(regs, "AO (2003-01-01)");
        assertTrue(pos >= 0);
        Assert.assertEquals("value outlier 1 is wrong: ", -4.0265, b[pos], 0.0001);
        pos = search(regs, "AO (2003-02-01)");
        assertTrue(pos >= 0);
        Assert.assertEquals("value outlier 2 is wrong: ", -1.5919, b[pos], 0.0001);
        pos = search(regs, "AO (2003-10-01)");
        assertTrue(pos >= 0);
        Assert.assertEquals("value outlier 3 is wrong: ", -1.4860, b[pos], 0.0001);
        pos = search(regs, "AO (2003-11-01)");
        assertTrue(pos >= 0);
        Assert.assertEquals("value outlier 4 is wrong: ", -3.7353, b[pos], 0.0001);
        pos = search(regs, "AO (2004-01-01)");
        assertTrue(pos >= 0);
        Assert.assertEquals("value outlier 4 is wrong: ", -3.7572, b[pos], 0.0001);
        pos = search(regs, "AO (2004-02-01)");
        assertTrue(pos >= 0);
        Assert.assertEquals("value outlier 4 is wrong: ", -1.5133, b[pos], 0.0001);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(ABC1B1_fixed_2_AO, comprest.getData("b-tables.b1", TsData.class).fittoDomain(ABC1B1_fixed_2_AO.getDomain()), 0.0001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(ABC1D10_fixed_2_AO.times(.01),
                comprest.getData("d-tables.d10", TsData.class).fittoDomain(ABC1B1_fixed_2_AO.getDomain()), 0.0001));
  }

    X13Specification makeX13Spec() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Multiplicative);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(5.0);
        x11spec.setUpperSigma(6.0);

        RegArimaSpecification arimaSpecification = new RegArimaSpecification();
        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.Log);

        arimaSpecification.setTransform(tr);
        OutlierSpec o = new OutlierSpec();
        o.add(OutlierType.AO);

        arimaSpecification.setOutliers(o);
        arimaSpecification.setUsingAutoModel(false);

        ArimaSpec arimaSpec = arimaSpecification.getArima();//new ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(1);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);
        Parameter[] paraTheta = Parameter.create(1);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.5);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.98);
        arimaSpec.setBTheta(paraBTheta);
        arimaSpecification.setArima(arimaSpec);

        X13Specification x13Spec = new X13Specification(arimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_fixed_1() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Multiplicative);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(5.0);
        x11spec.setUpperSigma(6.0);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        //transform
        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.Log);
        regArimaSpecification.setTransform(tr);

//Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        OutlierDefinition outlierDefinition = new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 2002, 11), OutlierType.AO);
        rs.add(outlierDefinition);

        double[] c = new double[1];
        c[0] = -5.8802; //-0.042442334225138614;
        rs.setFixedCoefficients(ITsVariable.shortName("AO (2002-12-01)"), c);
//Outlier
//        OutlierSpec o = new OutlierSpec();
//        o.add(OutlierType.AO);
//        regArimaSpecification.setOutliers(o);
//Automodel        
        regArimaSpecification.setUsingAutoModel(false);

        ArimaSpec arimaSpec = regArimaSpecification.getArima();//new ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(1);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);
        Parameter[] paraTheta = Parameter.create(1);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.5);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.98);
        arimaSpec.setBTheta(paraBTheta);
        regArimaSpecification.setArima(arimaSpec);

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_fixed_2() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Multiplicative);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(5.0);
        x11spec.setUpperSigma(6.0);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        //transform
        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.Log);
        regArimaSpecification.setTransform(tr);

//Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        OutlierDefinition outlierDefinition1 = new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 2002, 11), OutlierType.AO);
        rs.add(outlierDefinition1);
        OutlierDefinition outlierDefinition2 = new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 2003, 11), OutlierType.AO);
        rs.add(outlierDefinition2);

        double[] c = new double[1];
        c[0] = -5.8802; 
        rs.setFixedCoefficients(ITsVariable.shortName("AO (2002-12-01)"), c);

        double[] d = new double[1];
        d[0] = -5.0435;
        rs.setFixedCoefficients(ITsVariable.shortName("AO (2003-12-01)"), d);

//Automodel        
        regArimaSpecification.setUsingAutoModel(false);

        ArimaSpec arimaSpec = regArimaSpecification.getArima();//new ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(1);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);
        Parameter[] paraTheta = Parameter.create(1);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.5);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.98);
        arimaSpec.setBTheta(paraBTheta);
        regArimaSpecification.setArima(arimaSpec);

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_fixed_2_AO() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Multiplicative);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(5.0);
        x11spec.setUpperSigma(6.0);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        //transform
        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.Log);
        regArimaSpecification.setTransform(tr);

//Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        OutlierDefinition outlierDefinition1 = new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 2002, 11), OutlierType.AO);
        rs.add(outlierDefinition1);
        OutlierDefinition outlierDefinition2 = new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 2003, 11), OutlierType.AO);
        rs.add(outlierDefinition2);

        double[] c = new double[2];
        c[0] = -5.8802; 
        rs.setFixedCoefficients(ITsVariable.shortName("AO (2002-12-01)"), c);

        double[] d = new double[1];
        d[0] = -5.0435; 
        rs.setFixedCoefficients(ITsVariable.shortName("AO (2003-12-01)"), d);

//Outlier
        OutlierSpec o = new OutlierSpec();
        o.add(OutlierType.AO);
        o.setDefaultCriticalValue(3.79);
        o.setMethod(OutlierSpec.Method.AddOne);
        regArimaSpecification.setOutliers(o);
//Automodel        
        regArimaSpecification.setUsingAutoModel(false);

        ArimaSpec arimaSpec = regArimaSpecification.getArima();//new ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(1);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(false);
        Parameter[] paraTheta = Parameter.create(1);
        paraTheta[0].setType(ParameterType.Fixed);
        paraTheta[0].setValue(0.5);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.98);
        arimaSpec.setBTheta(paraBTheta);
        regArimaSpecification.setArima(arimaSpec);

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    private static ProcessingContext context;

    ProcessingContext makeContext() {

        if (context == null) {
            context = new ProcessingContext();
            NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
            TsVariables mgr = new TsVariables();
            mgr.set("x_1", tsvUser); 
            activeMgr.set("Vars-1", mgr);
            activeMgr.resetDirty();
            ProcessingContext.setActiveContext(context);
        }

        return context;
    }

    private int search(TsVariableSelection<IOutlierVariable> regs, String name) {
        Item<IOutlierVariable>[] elements = regs.elements();
        for (int i = 0; i < elements.length; ++i) {
            if (elements[i].variable.getName().equals(name)) {
                return elements[i].position;
            }
        }
        return -1;
    }

}
