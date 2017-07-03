/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import static data.DataFixCoef.tsvUY4712;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.TsVariableDescriptor.UserComponentType;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.NameManager;

/**
 *
 * @author Chris
 */
public class X13SpecAirlineSeveral {
    
    public static final X13Specification getX13Spec(DecompositionMode mode) {

        X13Specification x13Spec =  X13Specification.RSA0.clone();
        x13Spec.getRegArimaSpecification().setArima(makeFixArimaSpec());
        x13Spec.getRegArimaSpecification().getOutliers().clearTypes();
        if (mode != DecompositionMode.Additive) {
            x13Spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        } else {
            x13Spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
        }
        x13Spec.getX11Specification().setMode(mode);
        
        return x13Spec;
    }
    
    public static final X13Specification getX13SpecOutlier(DecompositionMode mode, OutlierType outlierType) {
        X13Specification x13Spec = getX13Spec(mode);
        SingleOutlierSpec o = new SingleOutlierSpec();
        o.setType(outlierType);
        o.setCriticalValue(2.0);
        x13Spec.getRegArimaSpecification().getOutliers().add(o);
        return x13Spec;
    }
    
      public static final X13Specification getX13SpecOutlier(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        SingleOutlierSpec o = new SingleOutlierSpec();
        o.setType(OutlierType.AO);
        o.setCriticalValue(2.0);
        x13Spec.getRegArimaSpecification().getOutliers().add(o);
        o = new SingleOutlierSpec();
        o.setType(OutlierType.LS);
        o.setCriticalValue(2.0);
        x13Spec.getRegArimaSpecification().getOutliers().add(o);
             o = new SingleOutlierSpec();
        o.setType(OutlierType.TC);
        o.setCriticalValue(2.0);
        x13Spec.getRegArimaSpecification().getOutliers().add(o);
              o = new SingleOutlierSpec();
        o.setType(OutlierType.SO);
        o.setCriticalValue(2.0);
        x13Spec.getRegArimaSpecification().getOutliers().add(o);
        
        return x13Spec;
    }
    
    public static final X13Specification getX13SpecTD(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        TradingDaysSpec daysSpec = new TradingDaysSpec();
        daysSpec.setTest(RegressionTestSpec.None);
        daysSpec.setTradingDaysType(TradingDaysType.TradingDays);
        x13Spec.getRegArimaSpecification().getRegression().setTradingDays(daysSpec);
        return x13Spec;
    }
    
    public static final X13Specification getX13SpecTdUserDefined(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedTradingdaySpec();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }    
    
    public static final X13Specification getX13SpecRegression(DecompositionMode mode, UserComponentType componentType) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeRegressionSpec(componentType);
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }
    
    private static ArimaSpec makeFixArimaSpec() {
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

    //Regression
    private static RegressionSpec makeRegressionSpec(TsVariableDescriptor.UserComponentType userComponentType) {
        RegressionSpec rs = new RegressionSpec();
        TsVariableDescriptor tsVariablesDescriptor = new TsVariableDescriptor("Vars-1.x_1");
        tsVariablesDescriptor.setEffect(userComponentType);
        rs.add(tsVariablesDescriptor);
        double[] c = new double[1];
        c[0] = 0.08;
        rs.setFixedCoefficients("Vars-1@x_1", c);
        return rs;
    }
    
    private static RegressionSpec makeUserDefinedTradingdaySpec() {
        RegressionSpec rs = new RegressionSpec();
        TradingDaysSpec tds = new TradingDaysSpec();
        //  tds.setTradingDaysType(TradingDaysType.TradingDays);
        tds.setTest(RegressionTestSpec.None);
        String[] name = new String[1];
        name[0] = "Vars-1.x_1";
        tds.setUserVariables(name);
        rs.setTradingDays(tds);
        double[] c = new double[1];
        c[0] = 0.08;
        rs.setFixedCoefficients("td", c);
        return rs;
    }
    
    private static ProcessingContext context;
    
    public static final ProcessingContext makeContext() {
        
        if (context == null) {
            context = new ProcessingContext();
            NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
            TsVariables mgr = new TsVariables();
            mgr.set("x_1", tsvRg); //ok
            activeMgr.set("Vars-1", mgr);//ok
            activeMgr.resetDirty();
            ProcessingContext.setActiveContext(context);
        }
        
        return context;
    }
    
    private static final double[] rg = {20.6, 21, 22.6, 18, 22, 19.3, 21, 22.8, 21, 20.9, 21.3, 1, 22, 19.6, 22, 19, 21, 19.3, 22, 22.8, 20, 21.9, 21.3, 1, 22, 19.6, 20, 21, 19.3, 20, 23, 21.8, 21, 21.9, 20.3, 1, 21.6, 20, 20.6, 20, 20, 19.3, 23, 20.8, 22, 21.9, 20, 1, 20.6, 19.6, 23, 20, 19, 21.3, 22, 22, 22, 21, 21.3, 1, 20.6, 19.6, 21, 21, 19.3, 22, 21, 22.8, 22, 19.9, 21.3, 1, 21.6, 19.6, 23, 18, 21, 20.3, 21, 22.8, 21, 20.9, 21.3, 1, 22, 19.6, 22, 19, 20, 20.3, 22, 22.8, 20, 21.9, 21.3, 1, 22, 20.6, 19, 22, 19.3, 21, 23, 20.8, 22, 21.9, 20, 1, 20.6, 19.6, 22, 20, 19, 20.3, 23, 21, 22, 22, 21, 1, 19.6, 19.6, 23, 20, 19, 21.3, 22, 22, 22, 21, 21.3, 1, 20.6, 20, 22.6, 19, 22, 19.3, 21, 22.8, 22, 19.9, 21.3, 1, 21.6, 20.6, 22, 19, 20, 20.3, 22, 22.8, 20, 21.9, 21.3, 1, 22, 19.6, 20, 21, 19.3, 20, 23, 21.8, 21, 21.9, 20.3, 1, 21.6, 20, 20.6, 20, 20, 19.3, 23, 20.8, 22, 21.9, 20, 1, 20.6, 19.6, 22, 20, 18, 21.3, 23, 21, 22, 22, 21, 1, 19.6, 20.6, 21, 21, 19.3, 22, 21, 22.8, 22, 19.9, 21.3, 1, 21.6, 19.6, 23, 18, 21, 20.3, 21, 22.8, 21, 20.9, 21.3, 1, 22, 19.6, 21, 20, 19.3, 21, 22, 22.8, 20, 21.9, 21.3, 1, 22, 20, 20.6, 20, 21, 18.3, 23, 21.8, 21, 21.9, 20.3, 1, 21.6, 19.6, 22, 20, 19, 20.3, 23, 21, 22, 22, 21, 1, 19.6, 19.6, 23, 20, 19, 21.3, 22, 22, 22, 21, 21.3, 1, 20.6, 19.8, 22.8, 19, 21, 20.3, 21, 22.8, 22, 19.9, 21.3, 1, 21.6, 19.6, 23, 18, 20, 21.3, 21, 22.8, 21, 20.9, 21.3, 1, 22, 20.6, 20, 21, 19.3, 20, 23, 21.8, 21, 21.9, 20.3, 1, 21.6, 20, 20.6, 20, 20, 19.3, 23, 20.8, 22, 21.9, 20, 1, 20.6, 19.6, 22, 20, 18, 21.3, 23, 21, 22, 22, 21, 1, 19.6, 19.6, 21, 22, 18.3, 22, 22, 22, 22, 21, 21.3, 1, 20.6, 20.6, 23, 18, 21, 20.3, 21, 22.8, 21, 20.9, 21.3, 1, 22, 19.6, 21, 20, 19.3, 21, 22, 22.8, 20, 21.9, 21.3, 1, 22, 20, 20.6, 20, 21, 18.3, 23, 21.8, 21, 21.9, 20.3};
    private static final TsData tsRg = new TsData(TsFrequency.Monthly, 1989, 0, rg, false);
    public static final TsVariable tsvRg = new TsVariable("aF", tsRg);
    
}
