/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import static data.DataFixCoef.*;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.NameManager;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import utilities.CompareTsData;

/**
 *
 * @author Christiane Hofer
 */
public class X13SpecFixedUserDefinedRegressorsTest {

    @Test
    public void UserVar_fixed_1() throws IOException {

        context = makeContext();
        X13Specification x13spec = makeX13Spec_fixed_1();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(PD2824F);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(PD2824FB1, comprest.getData("b-tables.b1", TsData.class), 0.0000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(PD2824FD10, comprest.getData("d-tables.d10", TsData.class), 0.0000001));

    }

//    @Ignore
    @Test
    public void UserVar_fixed_1_AO() throws IOException {

        context = makeContext();
        X13Specification x13spec = makeX13Spec_fixed_1_AO();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(PD2824F);
        System.out.println("B1_neu");
        System.out.println(   comprest.getData("b-tables.b1", TsData.class));
        PreprocessingModel preprocessingModel;
        preprocessingModel = GenericSaResults.getPreprocessingModel(comprest);
        ConcentratedLikelihood concentratedLikelihood = preprocessingModel.estimation.getLikelihood();
        double[] b;
        b = concentratedLikelihood.getB();
        TsVariableList tsVariableList;
        tsVariableList = preprocessingModel.description.buildRegressionVariables();
        TsVariableSelection<IOutlierVariable> regs = tsVariableList.select(IOutlierVariable.class);

        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();

        for (int i = 0; i < regs.elements().length; i++) {
            System.out.println(b[start + regs.elements()[i].position]);
            System.out.println(regs.elements()[i].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        }
  

        //results form X13 order might be wrong
        Assert.assertEquals("Expected Number of Outliers ist wrong", 2, preprocessingModel.description.getOutliers().size());
        Assert.assertEquals("value outlier 1 is wrong: ", -0.2212, b[start + regs.elements()[0].position], 0.0001);
        Assert.assertEquals("outlier description 1 is wrong: ", "AO (1-2001)", regs.elements()[0].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        Assert.assertEquals("value outlier 2 is wrong: ", 0.1821, b[start + regs.elements()[1].position], 0.0001);
        Assert.assertEquals("outlier description 2 is wrong: ", "AO (3-2008)", regs.elements()[1].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        System.out.println("B1: soll");
        System.out.println(PD2824FB1_1f_AO);
        System.out.println("B1 ist");
        System.out.println(comprest.getData("b-tables.b1", TsData.class));
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(PD2824FB1_1f_AO, comprest.getData("b-tables.b1", TsData.class), 0.000001));
    }

    X13Specification makeX13Spec_fixed_1_AO() {

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
        OutlierSpec o = new OutlierSpec();
        o.add(OutlierType.AO);
o.setDefaultCriticalValue(3.9);
        regArimaSpecification.setOutliers(o);

//Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        rs.clearMovingHolidays();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");//ok
        tsVariablesDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor);
        double[] c = new double[1];
        c[0] = 0.08;
        rs.setFixedCoefficients("Vars-1@x_1", c);
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
//            OutlierSpec o = new OutlierSpec();
//        o.add(OutlierType.AO);
//
//        regArimaSpecification.setOutliers(o);

//Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        rs.clearMovingHolidays();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");//ok
        tsVariablesDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor);
        double[] c = new double[1];
        c[0] = 0.08;
        rs.setFixedCoefficients("Vars-1@x_1", c);
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

    X13Specification makeX13Spec_fixed_0_AO() {

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
        OutlierSpec o = new OutlierSpec();
        o.add(OutlierType.AO);

        regArimaSpecification.setOutliers(o);

//Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        rs.clearMovingHolidays();
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

    @Test
    public void UserVar_fixed_0_AO() {

        context = makeContext();
        X13Specification x13spec = makeX13Spec_fixed_0_AO();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(PD2824F);
        PreprocessingModel preprocessingModel;
        preprocessingModel = GenericSaResults.getPreprocessingModel(comprest);
        ConcentratedLikelihood concentratedLikelihood = preprocessingModel.estimation.getLikelihood();
        double[] b;
        b = concentratedLikelihood.getB();
        TsVariableList tsVariableList;
        tsVariableList = preprocessingModel.description.buildRegressionVariables();
        TsVariableSelection<IOutlierVariable> regs = tsVariableList.select(IOutlierVariable.class);

        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();

//        for (int i = 0; i < regs.elements().length; i++) {
//            System.out.println(b[start + regs.elements()[i].position]);
//            System.out.println(regs.elements()[i].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
//        }
        Assert.assertEquals("value outlier 1 is wrong: ", -0.26513869666377704, b[start + regs.elements()[0].position], 0.000000000001);
        Assert.assertEquals("outlier description 1 is wrong: ", "AO (5-2016)", regs.elements()[0].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        Assert.assertEquals("value outlier 2 is wrong: ", 0.32697791950990107, b[start + regs.elements()[1].position], 0.000000000001);
        Assert.assertEquals("outlier description 2 is wrong: ", "AO (5-2000)", regs.elements()[1].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        Assert.assertEquals("value outlier 3 is wrong: ", 0.292342605331966, b[start + regs.elements()[2].position], 0.000000000001);
        Assert.assertEquals("outlier description 3 is wrong: ", "AO (5-2011)", regs.elements()[2].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));
        Assert.assertEquals("value outlier 4 is wrong: ", -0.28258801602911093, b[start + regs.elements()[3].position], 0.000000000001);
        Assert.assertEquals("outlier description 4 is wrong: ", "AO (4-2006)", regs.elements()[3].variable.getDescription(preprocessingModel.description.getEstimationDomain().getFrequency()));

    }

    private static ProcessingContext context;

    ProcessingContext makeContext() {

        if (context == null) {
            context = new ProcessingContext();
            NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
            TsVariables mgr = new TsVariables();
            mgr.set("x_1", tsvUY4712); //ok
            activeMgr.set("Vars-1", mgr);//ok
            activeMgr.resetDirty();
            ProcessingContext.setActiveContext(context);
        }

        return context;
    }
}
