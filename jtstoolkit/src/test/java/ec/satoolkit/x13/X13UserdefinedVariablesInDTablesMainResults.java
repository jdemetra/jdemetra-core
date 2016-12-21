/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.TsVariableDescriptor.UserComponentType;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.NameManager;
import org.junit.Assert;
import org.junit.Test;
import utilities.CompareTsData;


/*These tests compare check the results if user-definfined variables are used*
 *
 * @author Christiane Hofer
 */
public class X13UserdefinedVariablesInDTablesMainResults {

    public X13UserdefinedVariablesInDTablesMainResults() {
    }

    @Test
    public void UserUndefinedTest() {
        X13Specification x13spec = makeX13Spec(UserComponentType.Undefined);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(tso);
        Assert.assertTrue("Forecast is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("y_f", TsData.class), 0.000000001));
        Assert.assertTrue("A9 is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a9u", TsData.class), 0.000000001));
        //Resultvalues are not stored
    }

    @Test
    public void UserTrendTest() {
        X13Specification x13spec = makeX13Spec(UserComponentType.Trend);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(tso);
        Assert.assertTrue("Forecast is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("y_f", TsData.class), 0.000000001));
        Assert.assertTrue("A8 is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a8", TsData.class), 0.000000001));
        Assert.assertTrue("A8t is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a8t", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted is wrong", CompareTsData.compareTS(tsseasonaladjusted.plus(tsReg), comprest.getData("sa", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted forecast is wrong", CompareTsData.compareTS(tsseasonaladjusted_f.plus(tsReg_f), comprest.getData("sa_f", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("s", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal forecast is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("s_f", TsData.class), 0.000000001));
        Assert.assertTrue("Irregular is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("i", TsData.class), 0.000000001));
        Assert.assertTrue("Trend is wrong", CompareTsData.compareTS(tstrend.plus(tsReg), comprest.getData("t", TsData.class), 0.000000001));
        Assert.assertTrue("Trend forecast is wrong", CompareTsData.compareTS(tstrend_f.plus(tsReg_f), comprest.getData("t_f", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsseasonaladjusted.plus(tsReg), comprest.getData("d-tables.d11", TsData.class), 0.000000001));
        Assert.assertTrue("D11a is wrong", CompareTsData.compareTS(tsseasonaladjusted_f.plus(tsReg_f), comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
        Assert.assertTrue("D12 is wrong", CompareTsData.compareTS(tstrend.plus(tsReg), comprest.getData("d-tables.d12", TsData.class), 0.000000001));
        Assert.assertTrue("D12a is wrong", CompareTsData.compareTS(tstrend_f.plus(tsReg_f), comprest.getData("d-tables.d12a", TsData.class), 0.000000001));
        Assert.assertTrue("D13 is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("d-tables.d13", TsData.class), 0.000000001));

    }

    @Test
    public void UserSeriesTest() {
        X13Specification x13spec = makeX13Spec(UserComponentType.Series);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(tso);
        Assert.assertTrue("Forecast is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("y_f", TsData.class), 0.000000001));
        Assert.assertTrue("A9 is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a9", TsData.class), 0.000000001));
        Assert.assertTrue("A9ser is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a9ser", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted is wrong", CompareTsData.compareTS(tsseasonaladjusted, comprest.getData("sa", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted forecast is wrong", CompareTsData.compareTS(tsseasonaladjusted_f, comprest.getData("sa_f", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("s", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal forecast is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("s_f", TsData.class), 0.000000001));
        Assert.assertTrue("Irregular is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("i", TsData.class), 0.000000001));
        Assert.assertTrue("Trend is wrong", CompareTsData.compareTS(tstrend, comprest.getData("t", TsData.class), 0.000000001));
        Assert.assertTrue("Trend forecast is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("t_f", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsseasonaladjusted, comprest.getData("d-tables.d11", TsData.class), 0.000000001));
        Assert.assertTrue("D11a is wrong", CompareTsData.compareTS(tsseasonaladjusted_f, comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
        Assert.assertTrue("D12 is wrong", CompareTsData.compareTS(tstrend, comprest.getData("d-tables.d12", TsData.class), 0.000000001));
        Assert.assertTrue("D12a is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("d-tables.d12a", TsData.class), 0.000000001));
        Assert.assertTrue("D13 is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("d-tables.d13", TsData.class), 0.000000001));

    }

    @Test
    public void UserSeasonallyAdjustedTest() {
        X13Specification x13spec = makeX13Spec(UserComponentType.SeasonallyAdjusted);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(tso);
        Assert.assertTrue("Forecast is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("y_f", TsData.class), 0.000000001));
        Assert.assertTrue("A9 is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a9", TsData.class), 0.000000001));
        Assert.assertTrue("A9sa is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a9sa", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted is wrong", CompareTsData.compareTS(tsseasonaladjusted.plus(tsReg), comprest.getData("sa", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted forecast is wrong", CompareTsData.compareTS(tsseasonaladjusted_f.plus(tsReg_f), comprest.getData("sa_f", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("s", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal forecast is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("s_f", TsData.class), 0.000000001));
        Assert.assertTrue("Irregular is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("i", TsData.class), 0.000000001));
        Assert.assertTrue("Trend is wrong", CompareTsData.compareTS(tstrend, comprest.getData("t", TsData.class), 0.000000001));
        Assert.assertTrue("Trend forecast is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("t_f", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsseasonaladjusted.plus(tsReg), comprest.getData("d-tables.d11", TsData.class), 0.000000001));
        Assert.assertTrue("D11a is wrong", CompareTsData.compareTS(tsseasonaladjusted_f.plus(tsReg_f), comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
        Assert.assertTrue("D12 is wrong", CompareTsData.compareTS(tstrend, comprest.getData("d-tables.d12", TsData.class), 0.000000001));
        Assert.assertTrue("D12a is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("d-tables.d12a", TsData.class), 0.000000001));
        Assert.assertTrue("D13 is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("d-tables.d13", TsData.class), 0.000000001));

    }

    @Test
    public void UserSeasonalTest() {
        X13Specification x13spec = makeX13Spec(UserComponentType.Seasonal);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(tso);
        Assert.assertTrue("Forecast is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("y_f", TsData.class), 0.000000001));
        Assert.assertTrue("A8 is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a8", TsData.class), 0.000000001));
        Assert.assertTrue("A8s is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a8s", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted is wrong", CompareTsData.compareTS(tsseasonaladjusted, comprest.getData("sa", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted forecast is wrong", CompareTsData.compareTS(tsseasonaladjusted_f, comprest.getData("sa_f", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal is wrong", CompareTsData.compareTS(tsseasonal.plus(tsReg), comprest.getData("s", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal forecast is wrong", CompareTsData.compareTS(tsseasonal_f.plus(tsReg_f), comprest.getData("s_f", TsData.class), 0.000000001));
        Assert.assertTrue("Irregular is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("i", TsData.class), 0.000000001));
        Assert.assertTrue("Trend is wrong", CompareTsData.compareTS(tstrend, comprest.getData("t", TsData.class), 0.000000001));
        Assert.assertTrue("Trend forecast is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("t_f", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsseasonal.plus(tsReg), comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(tsseasonal_f.plus(tsReg_f), comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsseasonaladjusted, comprest.getData("d-tables.d11", TsData.class), 0.000000001));
        Assert.assertTrue("D11a is wrong", CompareTsData.compareTS(tsseasonaladjusted_f, comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
        Assert.assertTrue("D12 is wrong", CompareTsData.compareTS(tstrend, comprest.getData("d-tables.d12", TsData.class), 0.000000001));
        Assert.assertTrue("D12a is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("d-tables.d12a", TsData.class), 0.000000001));
        Assert.assertTrue("D13 is wrong", CompareTsData.compareTS(tsirregular, comprest.getData("d-tables.d13", TsData.class), 0.000000001));
    }

    @Test
    public void UserIrregularTest() {
        X13Specification x13spec = makeX13Spec(UserComponentType.Irregular);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, makeContext());
        CompositeResults comprest = processing.process(tso);
        Assert.assertTrue("Forecast is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("y_f", TsData.class), 0.000000001));
        Assert.assertTrue("A1a is wrong", CompareTsData.compareTS(tsForecast, comprest.getData("a-tables.a1a", TsData.class), 0.000000001));

        Assert.assertTrue("A8 is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a8", TsData.class), 0.000000001));
        Assert.assertTrue("A8i is wrong", CompareTsData.compareTS(tsReg_a, comprest.getData("a-tables.a8i", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted is wrong", CompareTsData.compareTS(tsseasonaladjusted.plus(tsReg), comprest.getData("sa", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonally adjusted forecast is wrong", CompareTsData.compareTS(tsseasonaladjusted_f.plus(tsReg_f), comprest.getData("sa_f", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("s", TsData.class), 0.000000001));
        Assert.assertTrue("Seasonal forecast is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("s_f", TsData.class), 0.000000001));
        Assert.assertTrue("Irregular is wrong", CompareTsData.compareTS(tsirregular.plus(tsReg), comprest.getData("i", TsData.class), 0.000000001));
        Assert.assertTrue("Trend is wrong", CompareTsData.compareTS(tstrend, comprest.getData("t", TsData.class), 0.000000001));
        Assert.assertTrue("Trend forecast is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("t_f", TsData.class), 0.000000001));
        Assert.assertTrue("D10 is wrong", CompareTsData.compareTS(tsseasonal, comprest.getData("d-tables.d10", TsData.class), 0.000000001));
        Assert.assertTrue("D10a is wrong", CompareTsData.compareTS(tsseasonal_f, comprest.getData("d-tables.d10a", TsData.class), 0.000000001));
        Assert.assertTrue("D11 is wrong", CompareTsData.compareTS(tsseasonaladjusted.plus(tsReg), comprest.getData("d-tables.d11", TsData.class), 0.000000001));
        Assert.assertTrue("D11a is wrong", CompareTsData.compareTS(tsseasonaladjusted_f.plus(tsReg_f), comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
        Assert.assertTrue("D12 is wrong", CompareTsData.compareTS(tstrend, comprest.getData("d-tables.d12", TsData.class), 0.000000001));
        Assert.assertTrue("D12a is wrong", CompareTsData.compareTS(tstrend_f, comprest.getData("d-tables.d12a", TsData.class), 0.000000001));
        Assert.assertTrue("D13 is wrong", CompareTsData.compareTS(tsirregular.plus(tsReg), comprest.getData("d-tables.d13", TsData.class), 0.000000001));
    }


    private static ProcessingContext context;

    ProcessingContext makeContext() {

        if (context == null) {
            context = ProcessingContext.getActiveContext();
            NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
            TsVariables mgr = new TsVariables();
            mgr.set("x_1", tsvUser); //ok
            activeMgr.set("Vars-1", mgr);//ok
            activeMgr.resetDirty();
        }

        return context;
    }

    ;

    X13Specification makeX13Spec() {
        X13Specification x13Spec = new X13Specification();
        X11Specification x11spec = new X11Specification();
        x11spec.setMode(DecompositionMode.Additive);
        x13Spec.setX11Specification(x11spec);

        RegArimaSpecification regArimaspec = new RegArimaSpecification();
        regArimaspec.setUsingAutoModel(false);
        ArimaSpec arimaSpec = new ArimaSpec();
        arimaSpec.setP(0);
        arimaSpec.setD(1);
        arimaSpec.setQ(1);
        arimaSpec.setBP(0);
        arimaSpec.setBD(1);
        arimaSpec.setBQ(1);
        arimaSpec.setMean(true);
        Parameter[] paraTheta = Parameter.create(1);
        paraTheta[0].setType(ParameterType.Undefined);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Undefined);
        arimaSpec.setBTheta(paraBTheta);
        regArimaspec.setArima(arimaSpec);
        regArimaspec.getEstimate().setTol(1.0E-6);
        x13Spec.setRegArimaSpecification(regArimaspec);

        return x13Spec;
    }

    X13Specification makeX13Spec(UserComponentType useCT) {
        X13Specification x13Spec = makeX13Spec();
        RegressionSpec regSpec = x13Spec.getRegArimaSpecification().getRegression();
        regSpec.clearMovingHolidays();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");//ok
        tsVariablesDescriptor.setEffect(useCT);
        regSpec.add(tsVariablesDescriptor);
        return x13Spec;
    }

    //Values for the input series
    private static final double[] o = {2.05, 2.42, 2.78, 2.31, 2.08, 1.66, 1.67, 1.4, 0.87, 1.04, 1.19, 2.35, 2.8, 3.49, 3.25, 2.83, 2.79, 2.43, 2.19, 1.93, 1.5, 1.47, 1.57, 2.54, 3.58, 3.65, 3.86, 3.27, 2.76, 2.7, 2.67, 2.12, 1.89, 2, 2.3, 2.99, 3.72, 3.96, 4.04, 3.67, 3.17, 3.09, 2.8, 2.4, 2.04, 2.11, 2.55, 2.91, 4.07, 4.31, 4.24, 3.97, 3.68, 3.31, 3, 2.73, 2.34, 2.17, 2.64, 3.53, 4.3, 4.62, 4.62, 3.94, 3.9, 2.29, 3.37, 2.9, 2.34, 2.37, 3.01, 3.05, 5.35, 4.92, 4.77, 4.41, 3.9, 3.78, 3.39, 2.97, 2.77, 2.72, 2.86, 3.74, 4.54, 5.02, 4.71, 4.35, 4.03, 3.93, 3.66, 3.21, 2.81, 2.64, 3.08, 4.05, 4.63, 4.99, 5, 4.56, 4.32, 2.77, 2.82, 2.46, 1.96, 2.02, 2.29, 3.15, 3.68, 4.43, 4.24, 3.77, 3.58, 3.47, 2.86, 2.52, 2.32, 2.21, 2.39, 3.51, 4.12, 4.66, 4.37, 3.97, 3.74, 3.29, 3.14, 2.89, 2.52, 2.41, 2.64, 3.53, 4.29, 4.69, 4.66, 4.18, 3.93, 3.56, 3.15, 3.13, 2.73, 2.66, 3.02, 3.44, 4.53, 4.85, 4.71, 4.55, 4.21, 3.81, 3.6, 3.26, 2.81, 2.71, 2.91, 3.7, 4.62, 5.02, 4.94, 4.62, 4.11, 3.89, 3.76, 3.25, 3.04, 3.05, 3.29, 4.07, 4.72, 5.23, 4.97, 4.8, 4.22, 4.13, 3.66, 3.47, 3.15, 2.91, 3.51, 4.04, 4.79, 5.37, 5.09, 4.75, 4.43, 4.37, 3.98, 3.74, 3.46, 3.1, 3.57, 4.33, 5.19, 5.44, 5.35, 4.84, 4.7, 4.36, 4.06, 3.85, 3.35, 3.15, 3.55, 4.41, 5.31, 5.77, 5.25, 5.09, 4.79, 4.44, 4.17, 3.84, 3.25, 3.43, 3.99, 4.68, 5.23, 5.81, 5.5, 5.28, 4.75, 4.7, 4.34, 3.87, 3.59, 3.5, 4.05, 4.64, 5.34, 5.83, 5.62, 5.2, 4.88, 4.88, 4.37, 3.93, 3.65, 3.66, 3.81, 4.72, 5.49, 5.88, 6.08, 5.23, 5.14, 4.75, 4.6, 4.24, 3.79, 3.55, 4.1, 4.93, 5.6, 5.94, 6.02, 5.24, 5.31, 4.73, 4.65, 4.34, 3.76, 3.69, 4.19, 4.88, 5.64, 6.03, 6.08, 5.78, 5.12, 4.97, 4.78, 4.41, 4, 3.74, 4.2, 4.84, 5.7, 6.03, 5.92, 5.73, 5.39, 4.92, 4.84, 4.47, 3.95, 4.02, 4.25, 5.11, 5.84, 6.05, 6.04, 5.5, 5.25, 5.05, 4.99, 4.57, 4.09, 5.58};
    private static final TsData tso = new TsData(TsFrequency.Monthly, 1991, 0, o, false);
// User defined regressor, which is used with different types
    private static final double[] user = {1.23, 1.39, 1.52, 1.62, 1.71, 1.79, 1.87, 1.93, 2.0, 2.05, 2.11, 2.16, 2.21, 2.25, 2.3, 2.34, 2.38, 2.42, 2.46, 2.49, 2.53, 2.56, 2.59, 2.63, 2.66, 2.69, 2.72, 2.75, 2.77, 2.8, 2.83, 2.85, 2.88, 2.91, 2.93, 2.95, 2.98, 3.0, 3.02, 3.05, 3.07, 3.09, 3.11, 3.13, 3.15, 3.17, 3.19, 3.21, 3.23, 3.25, 3.27, 3.29, 3.31, 3.33, 3.35, 3.36, 3.38, 3.4, 3.42, 3.43, 3.45, 3.47, 3.48, 3.5, 3.51, 3.53, 3.55, 3.56, 3.58, 3.59, 3.61, 3.62, 3.64, 3.65, 3.67, 3.68, 3.7, 3.71, 3.72, 3.74, 3.75, 3.76, 3.78, 3.79, 3.8, 3.82, 3.83, 3.84, 3.86, 3.87, 3.88, 3.9, 3.91, 3.92, 3.93, 3.94, 3.96, 3.97, 3.98, 3.99, 4.0, 2.98, 3.0, 3.02, 3.05, 3.07, 3.09, 3.11, 3.13, 3.15, 3.17, 3.19, 3.21, 3.23, 3.25, 3.27, 3.29, 3.31, 3.33, 3.35, 3.36, 3.38, 3.4, 3.42, 3.43, 3.45, 3.47, 3.48, 3.5, 3.51, 3.53, 3.55, 3.56, 3.58, 3.59, 3.61, 3.62, 3.64, 3.65, 3.67, 3.68, 3.7, 3.71, 3.72, 3.74, 3.75, 3.76, 3.78, 3.79, 3.8, 3.82, 3.83, 3.84, 3.86, 3.87, 3.88, 3.9, 3.91, 3.92, 3.93, 3.94, 3.96, 3.97, 3.98, 3.99, 4.0, 4.02, 4.03, 4.04, 4.05, 4.06, 4.07, 4.09, 4.1, 4.11, 4.12, 4.13, 4.14, 4.15, 4.16, 4.17, 4.18, 4.19, 4.2, 4.22, 4.23, 4.24, 4.25, 4.26, 4.27, 4.28, 4.29, 4.3, 4.31, 4.32, 4.33, 4.34, 4.35, 4.36, 4.37, 4.38, 4.38, 4.39, 4.4, 4.41, 4.42, 4.43, 4.44, 4.45, 4.46, 4.47, 4.48, 4.49, 4.5, 4.5, 4.51, 4.52, 4.53, 4.54, 4.55, 4.56, 4.57, 4.58, 4.58, 4.59, 4.6, 4.61, 4.62, 4.63, 4.63, 4.64, 4.65, 4.66, 4.67, 4.68, 4.68, 4.69, 4.7, 4.71, 4.72, 4.72, 4.73, 4.74, 4.75, 4.76, 4.76, 4.77, 4.78, 4.79, 4.8, 4.8, 4.81, 4.82, 4.83, 4.83, 4.84, 4.85, 4.86, 4.86, 4.87, 4.88, 4.89, 4.89, 4.9, 4.91, 4.92, 4.92, 4.93, 4.94, 4.94, 4.95, 4.96, 4.97, 4.97, 4.98, 4.99, 4.99, 5.0, 5.01, 5.02, 5.02, 5.03, 5.04, 5.04, 5.05, 5.06, 5.06, 5.07, 5.08, 5.08, 5.09, 5.1, 5.1, 5.11, 5.12, 5.12, 5.13, 5.14, 5.14, 5.15, 5.16, 5.16, 5.17, 5.18, 5.18, 5.19, 5.2, 5.2, 5.21, 5.22};
    private static final TsData tsuser = new TsData(TsFrequency.Monthly, 1991, 0, user, false);
    private static final TsVariable tsvUser = new TsVariable("a1", tsuser);

// Results without
    private static final double[] forecast = {4.404419318, 5.157138346, 5.98295636, 6.335074142, 6.235046963, 5.815837294, 5.493387928, 5.170312731, 4.973007542, 4.614828232, 4.211122312, 4.203841422};
    private static final TsData tsForecast = new TsData(TsFrequency.Monthly, 2015, 10, forecast, false);

    private static final double[] reg_a = {1.175503358, 1.328414364, 1.452654557, 1.548223936, 1.634236376, 1.710691879, 1.787147382, 1.84448901, 1.911387575, 1.959172264, 2.016513891, 2.064298581, 2.11208327, 2.150311022, 2.198095711, 2.236323462, 2.274551214, 2.312778965, 2.351006717, 2.379677531, 2.417905282, 2.446576096, 2.475246909, 2.513474661, 2.542145474, 2.570816288, 2.599487102, 2.628157915, 2.647271791, 2.675942605, 2.704613418, 2.723727294, 2.752398108, 2.781068921, 2.800182797, 2.819296673, 2.847967486, 2.867081362, 2.886195238, 2.914866051, 2.933979927, 2.953093803, 2.972207679, 2.991321554, 3.01043543, 3.029549306, 3.048663182, 3.067777057, 3.086890933, 3.106004809, 3.125118685, 3.14423256, 3.163346436, 3.182460312, 3.201574188, 3.211131126, 3.230245001, 3.249358877, 3.268472753, 3.278029691, 3.297143566, 3.316257442, 3.32581438, 3.344928256, 3.354485194, 3.373599069, 3.392712945, 3.402269883, 3.421383759, 3.430940697, 3.450054572, 3.45961151, 3.478725386, 3.488282324, 3.5073962, 3.516953138, 3.536067013, 3.545623951, 3.555180889, 3.574294765, 3.583851703, 3.59340864, 3.612522516, 3.622079454, 3.631636392, 3.650750268, 3.660307206, 3.669864143, 3.688978019, 3.698534957, 3.708091895, 3.727205771, 3.736762709, 3.746319646, 3.755876584, 3.765433522, 3.784547398, 3.794104336, 3.803661274, 3.813218212, 3.822775149, 2.847967486, 2.867081362, 2.886195238, 2.914866051, 2.933979927, 2.953093803, 2.972207679, 2.991321554, 3.01043543, 3.029549306, 3.048663182, 3.067777057, 3.086890933, 3.106004809, 3.125118685, 3.14423256, 3.163346436, 3.182460312, 3.201574188, 3.211131126, 3.230245001, 3.249358877, 3.268472753, 3.278029691, 3.297143566, 3.316257442, 3.32581438, 3.344928256, 3.354485194, 3.373599069, 3.392712945, 3.402269883, 3.421383759, 3.430940697, 3.450054572, 3.45961151, 3.478725386, 3.488282324, 3.5073962, 3.516953138, 3.536067013, 3.545623951, 3.555180889, 3.574294765, 3.583851703, 3.59340864, 3.612522516, 3.622079454, 3.631636392, 3.650750268, 3.660307206, 3.669864143, 3.688978019, 3.698534957, 3.708091895, 3.727205771, 3.736762709, 3.746319646, 3.755876584, 3.765433522, 3.784547398, 3.794104336, 3.803661274, 3.813218212, 3.822775149, 3.841889025, 3.851445963, 3.861002901, 3.870559839, 3.880116777, 3.889673715, 3.90878759, 3.918344528, 3.927901466, 3.937458404, 3.947015342, 3.95657228, 3.966129218, 3.975686155, 3.985243093, 3.994800031, 4.004356969, 4.013913907, 4.033027783, 4.042584721, 4.052141658, 4.061698596, 4.071255534, 4.080812472, 4.09036941, 4.099926348, 4.109483286, 4.119040224, 4.128597161, 4.138154099, 4.147711037, 4.157267975, 4.166824913, 4.176381851, 4.185938789, 4.185938789, 4.195495727, 4.205052664, 4.214609602, 4.22416654, 4.233723478, 4.243280416, 4.252837354, 4.262394292, 4.27195123, 4.281508167, 4.291065105, 4.300622043, 4.300622043, 4.310178981, 4.319735919, 4.329292857, 4.338849795, 4.348406733, 4.35796367, 4.367520608, 4.377077546, 4.377077546, 4.386634484, 4.396191422, 4.40574836, 4.415305298, 4.424862235, 4.424862235, 4.434419173, 4.443976111, 4.453533049, 4.463089987, 4.472646925, 4.472646925, 4.482203863, 4.491760801, 4.501317738, 4.510874676, 4.510874676, 4.520431614, 4.529988552, 4.53954549, 4.549102428, 4.549102428, 4.558659366, 4.568216304, 4.577773241, 4.587330179, 4.587330179, 4.596887117, 4.606444055, 4.616000993, 4.616000993, 4.625557931, 4.635114869, 4.644671807, 4.644671807, 4.654228744, 4.663785682, 4.67334262, 4.67334262, 4.682899558, 4.692456496, 4.702013434, 4.702013434, 4.711570372, 4.72112731, 4.72112731, 4.730684247, 4.740241185, 4.749798123, 4.749798123, 4.759355061, 4.768911999, 4.768911999, 4.778468937, 4.788025875, 4.797582813, 4.797582813, 4.80713975, 4.816696688, 4.816696688, 4.826253626, 4.835810564, 4.835810564, 4.845367502, 4.85492444, 4.85492444, 4.864481378, 4.874038316, 4.874038316, 4.883595253, 4.893152191, 4.893152191, 4.902709129, 4.912266067, 4.912266067, 4.921823005, 4.931379943, 4.931379943, 4.940936881, 4.950493819, 4.950493819, 4.960050756, 4.969607694, 4.969607694, 4.979164632, 4.98872157};
    private static final double[] reg = {1.175503358, 1.328414364, 1.452654557, 1.548223936, 1.634236376, 1.710691879, 1.787147382, 1.84448901, 1.911387575, 1.959172264, 2.016513891, 2.064298581, 2.11208327, 2.150311022, 2.198095711, 2.236323462, 2.274551214, 2.312778965, 2.351006717, 2.379677531, 2.417905282, 2.446576096, 2.475246909, 2.513474661, 2.542145474, 2.570816288, 2.599487102, 2.628157915, 2.647271791, 2.675942605, 2.704613418, 2.723727294, 2.752398108, 2.781068921, 2.800182797, 2.819296673, 2.847967486, 2.867081362, 2.886195238, 2.914866051, 2.933979927, 2.953093803, 2.972207679, 2.991321554, 3.01043543, 3.029549306, 3.048663182, 3.067777057, 3.086890933, 3.106004809, 3.125118685, 3.14423256, 3.163346436, 3.182460312, 3.201574188, 3.211131126, 3.230245001, 3.249358877, 3.268472753, 3.278029691, 3.297143566, 3.316257442, 3.32581438, 3.344928256, 3.354485194, 3.373599069, 3.392712945, 3.402269883, 3.421383759, 3.430940697, 3.450054572, 3.45961151, 3.478725386, 3.488282324, 3.5073962, 3.516953138, 3.536067013, 3.545623951, 3.555180889, 3.574294765, 3.583851703, 3.59340864, 3.612522516, 3.622079454, 3.631636392, 3.650750268, 3.660307206, 3.669864143, 3.688978019, 3.698534957, 3.708091895, 3.727205771, 3.736762709, 3.746319646, 3.755876584, 3.765433522, 3.784547398, 3.794104336, 3.803661274, 3.813218212, 3.822775149, 2.847967486, 2.867081362, 2.886195238, 2.914866051, 2.933979927, 2.953093803, 2.972207679, 2.991321554, 3.01043543, 3.029549306, 3.048663182, 3.067777057, 3.086890933, 3.106004809, 3.125118685, 3.14423256, 3.163346436, 3.182460312, 3.201574188, 3.211131126, 3.230245001, 3.249358877, 3.268472753, 3.278029691, 3.297143566, 3.316257442, 3.32581438, 3.344928256, 3.354485194, 3.373599069, 3.392712945, 3.402269883, 3.421383759, 3.430940697, 3.450054572, 3.45961151, 3.478725386, 3.488282324, 3.5073962, 3.516953138, 3.536067013, 3.545623951, 3.555180889, 3.574294765, 3.583851703, 3.59340864, 3.612522516, 3.622079454, 3.631636392, 3.650750268, 3.660307206, 3.669864143, 3.688978019, 3.698534957, 3.708091895, 3.727205771, 3.736762709, 3.746319646, 3.755876584, 3.765433522, 3.784547398, 3.794104336, 3.803661274, 3.813218212, 3.822775149, 3.841889025, 3.851445963, 3.861002901, 3.870559839, 3.880116777, 3.889673715, 3.90878759, 3.918344528, 3.927901466, 3.937458404, 3.947015342, 3.95657228, 3.966129218, 3.975686155, 3.985243093, 3.994800031, 4.004356969, 4.013913907, 4.033027783, 4.042584721, 4.052141658, 4.061698596, 4.071255534, 4.080812472, 4.09036941, 4.099926348, 4.109483286, 4.119040224, 4.128597161, 4.138154099, 4.147711037, 4.157267975, 4.166824913, 4.176381851, 4.185938789, 4.185938789, 4.195495727, 4.205052664, 4.214609602, 4.22416654, 4.233723478, 4.243280416, 4.252837354, 4.262394292, 4.27195123, 4.281508167, 4.291065105, 4.300622043, 4.300622043, 4.310178981, 4.319735919, 4.329292857, 4.338849795, 4.348406733, 4.35796367, 4.367520608, 4.377077546, 4.377077546, 4.386634484, 4.396191422, 4.40574836, 4.415305298, 4.424862235, 4.424862235, 4.434419173, 4.443976111, 4.453533049, 4.463089987, 4.472646925, 4.472646925, 4.482203863, 4.491760801, 4.501317738, 4.510874676, 4.510874676, 4.520431614, 4.529988552, 4.53954549, 4.549102428, 4.549102428, 4.558659366, 4.568216304, 4.577773241, 4.587330179, 4.587330179, 4.596887117, 4.606444055, 4.616000993, 4.616000993, 4.625557931, 4.635114869, 4.644671807, 4.644671807, 4.654228744, 4.663785682, 4.67334262, 4.67334262, 4.682899558, 4.692456496, 4.702013434, 4.702013434, 4.711570372, 4.72112731, 4.72112731, 4.730684247, 4.740241185, 4.749798123, 4.749798123, 4.759355061, 4.768911999, 4.768911999, 4.778468937, 4.788025875, 4.797582813, 4.797582813, 4.80713975, 4.816696688, 4.816696688, 4.826253626, 4.835810564, 4.835810564, 4.845367502, 4.85492444, 4.85492444, 4.864481378, 4.874038316, 4.874038316, 4.883595253, 4.893152191, 4.893152191, 4.902709129, 4.912266067};

    private static final double[] reg_f = {4.912266067, 4.921823005, 4.931379943, 4.931379943, 4.940936881, 4.950493819, 4.950493819, 4.960050756, 4.969607694, 4.969607694, 4.979164632, 4.98872157};
    private static final TsData tsReg_f = new TsData(TsFrequency.Monthly, 2015, 10, reg_f, false);
    private static final TsData tsReg_a = new TsData(TsFrequency.Monthly, 1991, 0, reg_a, false);
    private static final TsData tsReg = new TsData(TsFrequency.Monthly, 1991, 0, reg, false);

    private static final double[] seasonal = {0.751915607, 0.975513277, 1.103810486, 0.578127381, 0.315169697, -0.057154514, -0.218371188, -0.616649582, -1.046546225, -0.995324239, -0.75468223, -0.04008208, 0.757543917, 0.981636501, 1.092457728, 0.590003522, 0.311487812, -0.04859098, -0.229683955, -0.622587671, -1.049322832, -1.013755136, -0.748970281, -0.033191225, 0.775260527, 1.005801271, 1.080657125, 0.594850539, 0.297144732, -0.035404788, -0.250973595, -0.633332242, -1.058356629, -1.048707253, -0.727552203, -0.023603554, 0.803799985, 1.043026223, 1.074192212, 0.602387703, 0.271860618, -0.029352284, -0.276388302, -0.654156945, -1.060916722, -1.074854207, -0.714721819, -0.003008421, 0.820018568, 1.091804711, 1.060030405, 0.605439985, 0.238199983, -0.010386567, -0.292550471, -0.665787158, -1.064603064, -1.104289589, -0.710040149, 0.014623353, 0.823565923, 1.131701263, 1.047406172, 0.611207778, 0.227649198, -0.00459714, -0.295641759, -0.666806198, -1.066485309, -1.12199478, -0.736268397, 0.03722682, 0.810064859, 1.171959954, 1.038888247, 0.602043426, 0.237378371, -0.00030612, -0.294008172, -0.659855088, -1.066167635, -1.135731854, -0.771542826, 0.057442407, 0.794975985, 1.200541267, 1.033357612, 0.593267974, 0.259613129, -0.015155201, -0.28963172, -0.658792115, -1.048047964, -1.122181254, -0.814394338, 0.067780025, 0.775867905, 1.214119056, 1.029582701, 0.5835447, 0.280905248, -0.029339354, -0.289970601, -0.649035204, -1.025521612, -1.1077711, -0.837228941, 0.067179185, 0.759416981, 1.199926653, 1.01667083, 0.590759383, 0.31276788, -0.048689984, -0.290905803, -0.627068282, -1.007381602, -1.096813634, -0.857047155, 0.045234489, 0.755009038, 1.184745708, 1.01835404, 0.6046189, 0.324641707, -0.064441561, -0.296760348, -0.60699995, -0.994607319, -1.090037544, -0.853579828, 0.02491182, 0.749175395, 1.168840807, 1.006734046, 0.634462468, 0.311081832, -0.063385352, -0.30079866, -0.590429169, -0.978144562, -1.083157034, -0.845066561, -0.006684444, 0.741701702, 1.164033277, 0.994951891, 0.654834253, 0.279913335, -0.037677817, -0.302852524, -0.580708056, -0.959042911, -1.094736928, -0.823803437, -0.021966886, 0.73567169, 1.151595419, 0.976422147, 0.660984734, 0.262076153, -0.004296955, -0.297205731, -0.568341123, -0.95502543, -1.121939075, -0.812866283, -0.036426482, 0.745404912, 1.16547139, 0.968561975, 0.649914835, 0.252394453, 0.015310066, -0.301016725, -0.575536869, -0.953949858, -1.145290949, -0.794250844, -0.021355834, 0.751530627, 1.185341091, 0.952181856, 0.635303695, 0.247464467, 0.029118873, -0.307054936, -0.591228174, -0.961497874, -1.163153518, -0.770212702, -0.004497807, 0.75554516, 1.210697506, 0.947188998, 0.614942486, 0.245063884, 0.031450842, -0.310869121, -0.617727734, -0.970102235, -1.162520462, -0.744930718, 0.014050495, 0.75508217, 1.208600837, 0.948001418, 0.604068881, 0.257639075, 0.027377845, -0.299296514, -0.636574904, -0.991762873, -1.166374061, -0.736061857, 0.023300015, 0.757111774, 1.202222029, 0.97000219, 0.605349088, 0.270006554, 0.000575049, -0.280256668, -0.646500791, -1.015376563, -1.162099108, -0.728170008, 0.030972374, 0.743507904, 1.175983085, 1.000771255, 0.627152088, 0.265976925, -0.017210295, -0.258441658, -0.639686156, -1.029987015, -1.168603541, -0.722834606, 0.020459561, 0.731646495, 1.147082361, 1.027651552, 0.648871214, 0.26813649, -0.037396744, -0.238881328, -0.621412329, -1.037424847, -1.164448761, -0.731081737, 0.00943089, 0.733748934, 1.107420854, 1.044501374, 0.659444339, 0.26554752, -0.054070977, -0.219087738, -0.597603831, -1.034712076, -1.157424358, -0.736937032, -0.001469863, 0.742282261, 1.08022816, 1.042893198, 0.657297496, 0.267948202, -0.075138897, -0.211802286, -0.582286862, -1.030899698, -1.125400833, -0.734199649, -0.003228739, 0.743588921, 1.061661017, 1.028072324, 0.652047284, 0.248506887, -0.077501375, -0.208309804, -0.581939209, -1.014328465, -1.090027446, -0.720628244, -0.010158179, 0.739740807, 1.05336836, 1.005618382, 0.640985012, 0.242695924, -0.07572571, -0.21032911, -0.587923213, -1.004480032, -1.050048869,};
    private static final double[] seasonal_f = {-0.720080095, -0.016019876, 0.740902538, 1.049523222, 0.986664388, 0.633345903, 0.243695325, -0.073041916, -0.209382436, -0.592497465, -0.997773132, -1.030141179};

    private static final double[] seasonaladjusted = {0.122581035, 0.116072359, 0.223534957, 0.183648683, 0.130593927, 0.006462635, 0.101223806, 0.172160572, 0.00515865, 0.076151975, -0.071831661, 0.325783499, -0.069627187, 0.358052477, -0.040553438, 0.003673016, 0.203960974, 0.165812015, 0.068677238, 0.17291014, 0.13141755, 0.03717904, -0.156276629, 0.059716564, 0.262593999, 0.073382441, 0.179855774, 0.046991546, -0.184416523, 0.059462183, 0.216360177, 0.029604948, 0.195958521, 0.267638332, 0.227369406, 0.194306882, 0.068232529, 0.049892414, 0.079612551, 0.152746245, -0.035840545, 0.166258481, 0.104180623, 0.062835391, 0.090481292, 0.155304901, 0.216058637, -0.154768636, 0.163090499, 0.11219048, 0.054850911, 0.220327454, 0.278453581, 0.137926255, 0.090976284, 0.184656032, 0.174358062, 0.024930712, 0.081567397, 0.237346956, 0.179290511, 0.172041295, 0.246779448, -0.016136034, 0.317865608, -1.079001929, 0.272928814, 0.164536315, -0.01489845, 0.061054083, 0.296213824, -0.44683833, 1.061209755, 0.259757722, 0.223715554, 0.291003436, 0.126554616, 0.234682169, 0.128827283, 0.055560324, 0.252315933, 0.262323213, 0.019020309, 0.060478139, 0.113387623, 0.168708465, 0.016335183, 0.086867883, 0.081408852, 0.246620244, 0.241539825, 0.141586345, 0.121285255, 0.015861608, 0.138517753, 0.216786453, 0.069584697, -0.018223392, 0.166756025, 0.163237088, 0.216319603, -0.048628133, 0.242889239, 0.222839966, 0.070655561, 0.193791173, 0.174135138, 0.110613136, -0.070738536, 0.219637916, 0.193779864, 0.130577435, 0.199455063, 0.431799051, 0.044900994, 0.021949598, 0.183149042, 0.143467198, 0.064586843, 0.263191323, 0.153859837, 0.245009291, 0.102287083, 0.096908348, 0.137328602, 0.057297995, 0.120502906, 0.17118557, 0.169679063, 0.14555235, 0.119980758, 0.112375235, 0.138554722, 0.099775435, 0.222325257, 0.095482959, 0.159306658, 0.144659966, -0.037483664, 0.21303297, 0.191191425, 0.207090021, 0.319442609, -0.108496445, 0.214003534, 0.10211502, 0.121639469, 0.28264323, 0.308007211, 0.216041425, 0.252102256, 0.18040085, 0.099178768, 0.115758909, 0.03526848, 0.013874991, 0.15712254, 0.131641872, 0.217258206, 0.203138682, 0.082490325, 0.109749557, 0.263101395, 0.01467985, 0.181807218, 0.349163925, 0.260977258, 0.254980519, 0.113592187, 0.193968771, 0.121321248, 0.260411451, 0.058817957, 0.196345406, 0.033115259, 0.108078465, 0.156934516, 0.098718669, 0.338121626, 0.085669679, 0.053226279, 0.189858878, 0.133461174, 0.100782398, 0.149507751, 0.298296406, 0.234913277, 0.269529578, 0.350242339, 0.182341046, 0.249843292, 0.234571459, 0.324971554, 0.110262271, 0.274213841, 0.086903415, 0.307225079, 0.171281183, 0.204044208, 0.291345883, 0.134163446, 0.126581673, 0.099434991, 0.190896841, 0.340308227, 0.337232622, 0.068275104, 0.242650703, 0.279523571, 0.150227863, 0.197345285, 0.195066736, -0.049302232, 0.295752018, 0.425439814, 0.346521004, 0.153152307, 0.278485114, 0.191148015, 0.32624418, 0.122029775, 0.331904343, 0.243179122, 0.139423245, 0.218742079, 0.265907686, 0.372421648, 0.193722328, 0.171629861, 0.22915468, 0.184809571, 0.1288718, 0.160490026, 0.434120308, 0.155794733, 0.097039231, 0.197783152, 0.336842741, 0.031516868, 0.188665762, 0.247478828, 0.212486025, 0.522359896, 0.041583296, 0.322761082, 0.238294316, 0.280221962, 0.293196025, 0.249651605, 0.127118582, 0.243751558, 0.323681993, 0.259807011, 0.216578153, 0.359497633, -0.045002269, 0.409337612, 0.139399171, 0.224415932, 0.283375087, 0.130926394, 0.174081738, 0.253594412, 0.198570305, 0.205261243, 0.247758406, 0.335093369, 0.411132132, 0.130924489, 0.324011588, 0.261118038, 0.252045677, 0.281101574, 0.11560271, 0.174844588, 0.07431674, 0.18749908, 0.189870047, 0.103901801, 0.280369904, 0.343910301, 0.190361624, 0.231613116, 0.235242521, 0.138074839, 0.274216882, 0.13481768, 0.274790677, 0.245334753, 0.141707201, 0.16990024, -0.015023327, 0.133265761, 0.242130456, 0.307176918, 0.264771021, 0.191770903, 1.717782802};
    private static final double[] seasonaladjusted_f = {0.212233347, 0.251335218, 0.31067388, 0.354170977, 0.307445694, 0.231997572, 0.299198784, 0.283303891, 0.212782284, 0.237718003, 0.229730812, 0.245261031};
    private static final double[] trend = {0.170383618, 0.162178857, 0.147560492, 0.135562502, 0.121564301, 0.106529116, 0.088272923, 0.067173127, 0.04662305, 0.028630253, 0.016599372, 0.012849473, 0.017536615, 0.028554821, 0.043546605, 0.059634372, 0.075396211, 0.089463298, 0.100586796, 0.106828821, 0.108678605, 0.107254972, 0.103270294, 0.098523338, 0.094688613, 0.094269743, 0.098569031, 0.107755328, 0.119593425, 0.132278678, 0.143906963, 0.152745724, 0.157528136, 0.158117887, 0.154359277, 0.14583166, 0.133713408, 0.119561386, 0.106104473, 0.095704825, 0.08977111, 0.087905112, 0.089965243, 0.095503091, 0.103585537, 0.112030484, 0.120434873, 0.128872401, 0.136616245, 0.143164764, 0.147834642, 0.150135166, 0.151254419, 0.15246826, 0.153682581, 0.154979075, 0.15485798, 0.152385925, 0.146938668, 0.137705888, 0.125406297, 0.11054093, 0.09474997, 0.079932121, 0.067763385, 0.061613419, 0.064010485, 0.075864591, 0.096683204, 0.123829313, 0.153079548, 0.180265944, 0.201911864, 0.215916705, 0.221013558, 0.218299884, 0.209879169, 0.197223163, 0.181321482, 0.16330196, 0.145301753, 0.130003368, 0.118837321, 0.112072726, 0.109620084, 0.110800252, 0.115113836, 0.12120299, 0.127464446, 0.132606217, 0.1356279, 0.136059691, 0.133554461, 0.129158074, 0.125069607, 0.123423161, 0.124985462, 0.129887471, 0.137556669, 0.147074531, 0.156653293, 0.16439985, 0.169180978, 0.171843108, 0.173613011, 0.174274592, 0.17337743, 0.17060449, 0.165770854, 0.159743886, 0.152427038, 0.144359747, 0.136977153, 0.131308197, 0.128480149, 0.128420291, 0.130929434, 0.135237157, 0.139859014, 0.144078731, 0.147133214, 0.147971244, 0.146381794, 0.142488576, 0.137439642, 0.132374286, 0.128877913, 0.127991236, 0.129052807, 0.130959293, 0.133205483, 0.134906123, 0.135646511, 0.136373303, 0.138428953, 0.143283739, 0.15028534, 0.158144703, 0.16614454, 0.173275247, 0.17949183, 0.185626496, 0.192253865, 0.199632725, 0.207297526, 0.213442989, 0.216301975, 0.214162614, 0.206832562, 0.195111131, 0.180620872, 0.164296025, 0.147448848, 0.132190945, 0.120063361, 0.1126818, 0.111560464, 0.11773136, 0.130580191, 0.147741205, 0.165611084, 0.182350143, 0.196198223, 0.206421264, 0.212869834, 0.215230753, 0.2135004, 0.207968668, 0.19875533, 0.186295532, 0.171346591, 0.155201037, 0.140035874, 0.127396898, 0.116957469, 0.108639412, 0.102991338, 0.099803086, 0.099935151, 0.104589879, 0.114534494, 0.129355808, 0.148344408, 0.169909293, 0.192614815, 0.214390349, 0.232928716, 0.246860848, 0.254314966, 0.255566047, 0.252144646, 0.24580825, 0.237831099, 0.228372862, 0.217635473, 0.207627789, 0.199513534, 0.193466388, 0.190084552, 0.189989112, 0.192909713, 0.198177188, 0.204871502, 0.211128277, 0.215945682, 0.220002547, 0.224078087, 0.228186485, 0.232194507, 0.2360411, 0.239806096, 0.243347976, 0.247469648, 0.252717482, 0.257909451, 0.260881693, 0.260386282, 0.256666967, 0.251106737, 0.24539627, 0.240572338, 0.237587714, 0.236634125, 0.236715456, 0.236781071, 0.235037124, 0.22972609, 0.221296332, 0.210891814, 0.199612433, 0.188053135, 0.177444127, 0.168917229, 0.163477657, 0.161401827, 0.163498686, 0.169422315, 0.178027364, 0.188920608, 0.201352174, 0.214355283, 0.226515435, 0.236978205, 0.2449474, 0.250435208, 0.253377369, 0.254921233, 0.255979739, 0.256556708, 0.256922576, 0.257604667, 0.258609853, 0.258810576, 0.257569495, 0.254420337, 0.248589694, 0.23980722, 0.229679775, 0.220069343, 0.212697937, 0.209351069, 0.211063655, 0.218274917, 0.229444002, 0.243039322, 0.256668645, 0.267302981, 0.272739873, 0.271615755, 0.262924482, 0.248222429, 0.229539137, 0.210082861, 0.193722732, 0.1822136, 0.17704536, 0.178194068, 0.184216845, 0.193446949, 0.204010437, 0.214641946, 0.223931417, 0.229057256, 0.228642106, 0.222731465, 0.212335282, 0.199759895, 0.187412948, 0.178569983, 0.176022011, 0.181762248, 0.196285441, 0.218546287, 0.246810979, 0.278568574, 0.310267983, 0.338288354, 0.359288363};
    private static final double[] trend_f = {0.370367438, 0.370364455, 0.359394357, 0.340645134, 0.318196366, 0.296590047, 0.27815761, 0.262503746, 0.24908638, 0.235174047, 0.222758991, 0.212398649};
    private static final double[] irregular = {-0.047802583, -0.046106498, 0.075974465, 0.048086181, 0.009029626, -0.100066481, 0.012950882, 0.104987445, -0.0414644, 0.047521722, -0.088431033, 0.312934026, -0.087163803, 0.329497655, -0.084100043, -0.055961356, 0.128564763, 0.076348717, -0.031909558, 0.066081319, 0.022738945, -0.070075931, -0.259546923, -0.038806774, 0.167905385, -0.020887302, 0.081286742, -0.060763782, -0.304009948, -0.072816495, 0.072453214, -0.123140776, 0.038430385, 0.109520445, 0.073010129, 0.048475222, -0.065480879, -0.069668971, -0.026491922, 0.05704142, -0.125611655, 0.078353369, 0.01421538, -0.0326677, -0.013104245, 0.043274418, 0.095623764, -0.283641037, 0.026474254, -0.030974284, -0.092983731, 0.070192288, 0.127199162, -0.014542004, -0.062706297, 0.029676957, 0.019500082, -0.127455213, -0.065371271, 0.099641068, 0.053884214, 0.061500364, 0.152029478, -0.096068155, 0.250102222, -1.140615348, 0.208918329, 0.088671725, -0.111581653, -0.06277523, 0.143134276, -0.627104275, 0.859297891, 0.043841016, 0.002701996, 0.072703552, -0.083324554, 0.037459006, -0.0524942, -0.107741636, 0.10701418, 0.132319845, -0.099817012, -0.051594587, 0.00376754, 0.057908213, -0.098778653, -0.034335107, -0.046055594, 0.114014027, 0.105911925, 0.005526654, -0.012269206, -0.113296466, 0.013448146, 0.093363292, -0.055400765, -0.148110863, 0.029199356, 0.016162557, 0.05966631, -0.213027983, 0.073708261, 0.050996857, -0.10295745, 0.019516581, 0.000757708, -0.059991354, -0.236509389, 0.05989403, 0.041352826, -0.013782312, 0.06247791, 0.300490853, -0.083579155, -0.106470694, 0.052219608, 0.008230041, -0.075272171, 0.119112592, 0.006726623, 0.097038047, -0.044094711, -0.045580229, -0.00011104, -0.075076291, -0.008375007, 0.043194334, 0.040626255, 0.014593057, -0.013224725, -0.022530889, 0.002908212, -0.036597868, 0.083896304, -0.04780078, 0.009021318, -0.013484737, -0.203628203, 0.039757723, 0.011699595, 0.021463525, 0.127188745, -0.30812917, 0.006706008, -0.111327969, -0.094662506, 0.068480617, 0.101174649, 0.020930294, 0.071481384, 0.016104825, -0.04827008, -0.016432036, -0.084794881, -0.098806809, 0.045562076, 0.013910512, 0.086678015, 0.055397477, -0.083120759, -0.072600586, 0.066903172, -0.191741414, -0.031062616, 0.133933172, 0.047476858, 0.047011851, -0.085163143, 0.007673238, -0.050025343, 0.105210414, -0.081217917, 0.068948508, -0.083842211, -0.000560947, 0.053943178, -0.001084417, 0.238186476, -0.0189202, -0.061308214, 0.06050307, -0.014883234, -0.069126895, -0.043107064, 0.083906057, 0.001984561, 0.02266873, 0.095927373, -0.073225001, -0.002301354, -0.01123679, 0.087140455, -0.118110591, 0.056578368, -0.120724375, 0.107711545, -0.022185205, 0.013959656, 0.101356771, -0.058746267, -0.071595515, -0.105436511, -0.020231436, 0.124362545, 0.117230076, -0.155802983, 0.014464218, 0.047329064, -0.085813237, -0.042460811, -0.048281239, -0.296771879, 0.043034535, 0.167530364, 0.085639311, -0.107233976, 0.021818147, -0.059958721, 0.08084791, -0.118542563, 0.094316629, 0.006544997, -0.097292211, -0.018038992, 0.030870562, 0.142695558, -0.027574003, -0.039261953, 0.029542247, -0.003243564, -0.048572327, -0.008427203, 0.270642651, -0.005607094, -0.066459455, 0.028360837, 0.158815377, -0.15740374, -0.012686411, 0.033123545, -0.01402941, 0.285381691, -0.203364104, 0.072325874, -0.015083053, 0.025300729, 0.037216286, -0.006905103, -0.129803994, -0.013853109, 0.065072141, 0.000996435, -0.040991342, 0.105077296, -0.293591963, 0.169530392, -0.090280604, 0.004346589, 0.07067715, -0.078424676, -0.036981917, 0.035319496, -0.030873697, -0.037778079, -0.008910239, 0.067790388, 0.138392259, -0.140691266, 0.061087106, 0.012895609, 0.02250654, 0.071018713, -0.078120022, -0.007369012, -0.10272862, 0.009305012, 0.005653202, -0.089545149, 0.076359467, 0.129268355, -0.033569793, 0.00255586, 0.006600415, -0.084656626, 0.061881599, -0.064942215, 0.087377729, 0.06676477, -0.034314811, -0.011862008, -0.211308768, -0.085280526, -0.004680523, 0.028608345, -0.045496962, -0.146517451, 1.35849444};

    private static final TsData tsseasonal = new TsData(TsFrequency.Monthly, 1991, 0, seasonal, false);
    private static final TsData tsseasonal_f = new TsData(TsFrequency.Monthly, 2015, 10, seasonal_f, false);
    private static final TsData tsseasonaladjusted = new TsData(TsFrequency.Monthly, 1991, 0, seasonaladjusted, false);
    private static final TsData tsseasonaladjusted_f = new TsData(TsFrequency.Monthly, 2015, 10, seasonaladjusted_f, false);

    private static final TsData tstrend = new TsData(TsFrequency.Monthly, 1991, 0, trend, false);
    private static final TsData tstrend_f = new TsData(TsFrequency.Monthly, 2015, 10, trend_f, false);
    private static final TsData tsirregular = new TsData(TsFrequency.Monthly, 1991, 0, irregular, false);

}
