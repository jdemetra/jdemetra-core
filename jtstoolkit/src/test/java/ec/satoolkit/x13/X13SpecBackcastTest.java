/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import data.DataBackCast;
import static data.DataFixCoef.ABC1;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.DefaultArimaSpec;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.NameManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import utilities.CompareTsData;

/**
 *
 * @author Christiane Hofer
 */
public class X13SpecBackcastTest {

    @Test
    public void Backcast_3x5() {
        X13Specification x13spec = makeX13Spec_3X5();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(DataBackCast.Back3X5_B1, comprest.getData("b-tables.b1", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(DataBackCast.Back3X5_D10, comprest.getData("d-tables.d10", TsData.class), 0.000000001));

    }

    @Test
    public void Backcast_3x5_SigmaLow() {
        X13Specification x13spec = makeX13Spec_3X5_SigmaLow();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertEquals("D9 12-2013 is wrong", 8195.677119306143, comprest.getData("d-tables.d9", TsData.class).get(12), 0.000000001);
        Assert.assertEquals("D9 7-2005 is wrong", -4139.947593785415, comprest.getData("d-tables.d9", TsData.class).get(31), 0.000000001);
        Assert.assertEquals("D9 9-2005 is wrong", 3542.8697987858905, comprest.getData("d-tables.d9", TsData.class).get(33), 0.000000001);
        Assert.assertEquals("D9 10-2005 is wrong", 2708.272140861285, comprest.getData("d-tables.d9", TsData.class).get(34), 0.000000001);
        Assert.assertEquals("D9 12-2007 is wrong", 18459.69776353598, comprest.getData("d-tables.d9", TsData.class).get(60), 0.000000001);
        Assert.assertEquals("D9 9-2008 is wrong", -6689.254946141038, comprest.getData("d-tables.d9", TsData.class).get(69), 0.000000001);
        Assert.assertEquals("D9 12-2008 is wrong", 18521.997617661516, comprest.getData("d-tables.d9", TsData.class).get(72), 0.000000001);

    }

    @Test
    public void Backcast_3x3_SigmaUpper_CalendarSigma_All() {
        X13Specification x13spec = makeX13Spec_3X3_SigmaUpper_CalendarSigma_All();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);

        Assert.assertEquals("D9 5-2005 is wrong", -4619.011000156344, comprest.getData("d-tables.d9", TsData.class).get(29), 0.000000001);
        Assert.assertEquals("D9 6-2005 is wrong", -4181.903250704898, comprest.getData("d-tables.d9", TsData.class).get(30), 0.000000001);
        Assert.assertEquals("D9 7-2005 is wrong", -470.0293349807762, comprest.getData("d-tables.d9", TsData.class).get(31), 0.000000001);
        Assert.assertEquals("D9 9-200 is wrong", -1139.0538984394516, comprest.getData("d-tables.d9", TsData.class).get(33), 0.000000001);
        Assert.assertEquals("D9 10-2005 is wrong", -3025.744186129319, comprest.getData("d-tables.d9", TsData.class).get(34), 0.000000001);
        Assert.assertEquals("D9 4-2006 is wrong", -3400.5376276774623, comprest.getData("d-tables.d9", TsData.class).get(40), 0.000000001);
        Assert.assertEquals("D9 9-2008 is wrong", -1930.4990253761207, comprest.getData("d-tables.d9", TsData.class).get(69), 0.000000001);
        Assert.assertEquals("D9 5-2009 is wrong", -4332.26607745338, comprest.getData("d-tables.d9", TsData.class).get(77), 0.000000001);
        Assert.assertEquals("D9 3-2010 is wrong", -3760.432691580405, comprest.getData("d-tables.d9", TsData.class).get(87), 0.000000001);

    }

    @Test
    public void Backcast_3x3_SigmaUpper_CalendarSigma_Default() {
        X13Specification x13spec = makeX13Spec_3X3_SigmaUpper_CalendarDefault();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertEquals("D9 11-2003 is wrong", 9006.503900784679, comprest.getData("d-tables.d9", TsData.class).get(12), 0.000000001);
        Assert.assertEquals("D9 7-2005 is wrong", -549.8873440484167, comprest.getData("d-tables.d9", TsData.class).get(31), 0.000000001);
        Assert.assertEquals("D9 9-2005 is wrong", 562.6752891606593, comprest.getData("d-tables.d9", TsData.class).get(33), 0.000000001);
        Assert.assertEquals("D9 10-2005 is wrong", 194.7344253224146, comprest.getData("d-tables.d9", TsData.class).get(34), 0.000000001);
    }

    
    
    @Test
    public void Backcast_userdefvariables_startWithBackcast() {
        makeContext(2001, 11, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_B1, comprest.getData("b-tables.b1", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_D10, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_D10a, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
    }

     @Test
    public void Backcast_userdefvariables_startWithBackcast_typeTrend() {
        makeContext(2001, 11, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Trend);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_B1, comprest.getData("b-tables.b1", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_D10, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_D10a, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
    }
    
    /**
     * B1 is not influenced from a change in the regression variable in the
     * backcast Horizon, the forcast and backcast calculation is done with the
     * corrected series
     */
    @Test
    public void Backcast_userdefvariables_differsInBackcast() {
        makeContext(2001, 11, DataBackCast.user_var_differ);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_B1, comprest.getData("b-tables.b1", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_D10, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(DataBackCast.StartWithBackcast_D10a, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
    }

    @Test
    public void Backcast_userdefvariables_startBeforeBackcast() {
        makeContext(2001, 0, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
        Assert.assertTrue("B1 is wrong", CompareTsData.compareTS(DataBackCast.StartBeforeBackcast_B1, comprest.getData("b-tables.b1", TsData.class), 0.000000001));
    }

    @Test
    public void Backcast_userdefvariables_endBeforeForCast() {
        makeContext(1980, 6, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
//        System.out.println("B1");
//        System.out.println(comprest.getData("b-tables.b1", TsData.class));
//        System.out.println("D10");
//        System.out.println(comprest.getData("d-tables.d10", TsData.class));
//        System.out.println("D10a");
//        System.out.println(comprest.getData("d-tables.d10a", TsData.class));
        System.out.println("Is not possible to calculate in WinX13");
    }

      @Test
    public void userdefvariables_startsAferSeries() {
        makeContext(2004, 6, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
//        System.out.println("B1");
//        System.out.println(comprest.getData("b-tables.b1", TsData.class));
//        System.out.println("D10");
//        System.out.println(comprest.getData("d-tables.d10", TsData.class));
//        System.out.println("D10a");
//        System.out.println(comprest.getData("d-tables.d10a", TsData.class));
        System.out.println("Is not possible to calculate in WinX13");
    }
    @Test
    public void Backcast_userdefvariables_startAfterBackcast() {
        makeContext(2002, 0, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(ABC1);
//        System.out.println("B1");
//        System.out.println(comprest.getData("b-tables.b1", TsData.class));
//        System.out.println("D10");
//        System.out.println(comprest.getData("d-tables.d10", TsData.class));
//        System.out.println("D10a");
//        System.out.println(comprest.getData("d-tables.d10a", TsData.class));
        System.out.println("Is not possible to calculate in WinX13");
    }

    @Test
    public void Backcast_userdefvariables_b_notfix_differsInBackcast() {
        makeContext(2001, 11, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables_b_notfix();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest1 = processing.process(ABC1);

        makeContext(2001, 11, DataBackCast.user_var_differ);
        X13Specification x13spec2 = makeX13Spec_userdefvariables_b_notfix();
        SequentialProcessing<TsData> processing2 = X13ProcessingFactory.instance.generateProcessing(x13spec2);
        CompositeResults comprest2 = processing2.process(ABC1);
        Assert.assertTrue("B1 sensitive against the user variable during the backcast horizont ", CompareTsData.compareTS(comprest1.getData("b-tables.b1", TsData.class), comprest2.getData("b-tables.b1", TsData.class), 0.000000001));

    }

    
    @Test
    public void Backcast_userdefvariables_automodel_differsInBackcast() {
        makeContext(2001, 11, DataBackCast.user_var);
        X13Specification x13spec = makeX13Spec_userdefvariables_automodel();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest1 = processing.process(ABC1);

        makeContext(2001, 11, DataBackCast.user_var_differ);
        X13Specification x13spec2 = makeX13Spec_userdefvariables_automodel();
        SequentialProcessing<TsData> processing2 = X13ProcessingFactory.instance.generateProcessing(x13spec2);
        CompositeResults comprest2 = processing2.process(ABC1);
        Assert.assertTrue("B1 sensitive against the user variable during the backcast horizont ", CompareTsData.compareTS(comprest1.getData("b-tables.b1", TsData.class), comprest2.getData("b-tables.b1", TsData.class), 0.000000001));

    }

    private X13Specification makeX13Spec_userdefvariables_automodel() {

        X11Specification x11spec = makeX11Spec();

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        //Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        rs.clearMovingHolidays();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");//ok
        tsVariablesDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor);

        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setUsingAutoModel(true);
        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    private X13Specification makeX13Spec_userdefvariables_b_notfix() {

        X11Specification x11spec = makeX11Spec();

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        //Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        rs.clearMovingHolidays();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");//ok
        tsVariablesDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor);
        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setArima(makeFixArimaSpec());

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    private X13Specification makeX13Spec_userdefvariables(TsVariableDescriptor.UserComponentType userComponentType) {

        X11Specification x11spec = makeX11Spec();

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();
        //Regression
        RegressionSpec rs = regArimaSpecification.getRegression();
        rs.clearMovingHolidays();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");//ok
        tsVariablesDescriptor.setEffect(userComponentType);
        rs.add(tsVariablesDescriptor);
        double[] c = new double[1];
        c[0] = 615.0;
        rs.setFixedCoefficients("Vars-1@x_1", c);

        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setArima(makeFixArimaSpec());

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_3X3_SigmaUpper_CalendarDefault() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X3);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(2.5);
        x11spec.setUpperSigma(3.0);
        x11spec.setForecastHorizon(-1);
        x11spec.setBackcastHorizon(-1);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();

        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setArima(makeFixArimaSpec());

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_3X3_SigmaUpper_CalendarSigma_All() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X3);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(1.99);
        x11spec.setUpperSigma(2.0);
        x11spec.setCalendarSigma(CalendarSigma.All);
        x11spec.setForecastHorizon(-1);
        x11spec.setBackcastHorizon(-1);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();

        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setArima(makeFixArimaSpec());

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_3X5_SigmaLow() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(2.0);
        x11spec.setUpperSigma(4.0);

        x11spec.setForecastHorizon(-1);
        x11spec.setBackcastHorizon(-1);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();

        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setArima(makeFixArimaSpec());

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    X13Specification makeX13Spec_3X5() {

        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(5.0);
        x11spec.setUpperSigma(6.0);

        x11spec.setForecastHorizon(-1);
        x11spec.setBackcastHorizon(-1);

        RegArimaSpecification regArimaSpecification = new RegArimaSpecification();

        regArimaSpecification.setTransform(makeTransformSpecNone());

        regArimaSpecification.setArima(makeFixArimaSpec());

        X13Specification x13Spec = new X13Specification(regArimaSpecification, x11spec);

        return x13Spec;
    }

    private X11Specification makeX11Spec() {
        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x11spec.setSeasonalFilter(SeasonalFilterOption.S3X3);
        x11spec.setHendersonFilterLength(13);
        x11spec.setLowerSigma(6.5);
        x11spec.setUpperSigma(7.0);
        x11spec.setForecastHorizon(-1);
        x11spec.setBackcastHorizon(-1);
        return x11spec;
    }

    private ArimaSpec makeFixArimaSpec() {
        ArimaSpec arimaSpec = new ArimaSpec();
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
        return arimaSpec;
    }

    private TransformSpec makeTransformSpecNone() {
        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.None);
        return tr;

    }

    void makeContext(int year, int period, double[] user_var) {
        ProcessingContext context;
        context = new ProcessingContext();
        NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
        TsVariables mgr = new TsVariables();
        mgr.set("x_1", DataBackCast.make_tsuservar(year, period, user_var)); 
        activeMgr.set("Vars-1", mgr);
        activeMgr.resetDirty();
        ProcessingContext.setActiveContext(context);
    }
}
