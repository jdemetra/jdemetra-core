/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.NameManager;

/**
 *
 * @author Christiane Hofer
 */
public class x13SpecSeveralCalendarRegressionVariables {

    public static final X13Specification getX13SpecTdUserDefinedAllnotFix(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedTradingdaySpec_allnotFix();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecTdUserDefinedAllFix(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedTradingdaySpec_3();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecUserDefinedVariablesAllnotFix(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedVariablesSpec_allnotfix();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecUserDefinedVariablesAllFix(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedVariablesSpec_allfix();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecCalendar_3_1_fix(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeCalendarSpec_3_1_fix();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecCalendar_fix_first_last(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeCalendarSpec_fix_first_last();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecUserDefinedVariables_3_1_fix(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedVariablesSpec_3_1_fix();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecUserDefinedVariables_fix_first_and_last(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedVariablesSpec_fix_first_and_last();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    public static final X13Specification getX13SpecUserDefinedVariables_3_1_fixlast(DecompositionMode mode) {
        X13Specification x13Spec = getX13Spec(mode);
        RegressionSpec rs = makeUserDefinedVariablesSpec_3_1_fixlast();
        x13Spec.getRegArimaSpecification().setRegression(rs);
        return x13Spec;
    }

    private static X13Specification getX13Spec(DecompositionMode mode) {

        X13Specification x13Spec = X13Specification.RSA0.clone();
        x13Spec.getRegArimaSpecification().setArima(makeFixArimaSpec());
        x13Spec.getRegArimaSpecification().getOutliers().clearTypes();
        if (mode != DecompositionMode.Additive) {
            x13Spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        } else {
            x13Spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
        }
        x13Spec.getX11Specification().setMode(mode);

        //    x13Spec.getRegArimaSpecification().setRegression(makeUserDefinedTradingdaySpec_3());
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
        paraTheta[0].setValue(-0.4221);
        arimaSpec.setTheta(paraTheta);
        Parameter[] paraBTheta = Parameter.create(1);
        paraBTheta[0].setType(ParameterType.Fixed);
        paraBTheta[0].setValue(-0.6373);
        arimaSpec.setBTheta(paraBTheta);
        return arimaSpec;
    }

    private static RegressionSpec makeCalendarSpec_3_1_fix() {
        RegressionSpec rs = new RegressionSpec();
        TradingDaysSpec tds = new TradingDaysSpec();
        tds.setTest(RegressionTestSpec.None);
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        tds.setUserVariables(name);
        rs.setTradingDays(tds);
        double[] c = new double[3];
        c[0] = 0.08;
//        c[1] = 0.00; // probably the notation will change
//        c[2] = 0.00;
        //  rs.setFixedCoefficients("td", c);
        rs.setFixedCoefficients("td|" + "Vars-1@x_t", c);
        return rs;
    }

    private static RegressionSpec makeCalendarSpec_fix_first_last() {
        RegressionSpec rs = new RegressionSpec();
        TradingDaysSpec tds = new TradingDaysSpec();
        tds.setTest(RegressionTestSpec.None);
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        tds.setUserVariables(name);
        rs.setTradingDays(tds);
        double[] c = new double[1];
        c[0] = 0.07;
        rs.setFixedCoefficients("td|" + "Vars-1@x_t", c);
        double[] c3 = new double[1];
        c3[0] = 0.01;
        rs.setFixedCoefficients("td|" + "Vars-1@x_e_a", c3);
        return rs;
    }

    private static RegressionSpec makeUserDefinedTradingdaySpec_3() {
        RegressionSpec rs = new RegressionSpec();
        TradingDaysSpec tds = new TradingDaysSpec();
        tds.setTest(RegressionTestSpec.None);
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        tds.setUserVariables(name);
        rs.setTradingDays(tds);
        //  double[] c = new double[3];
//        c[0] = 0.08;
//        c[1] = 0.01;
//        c[2] = 0.02;
//        rs.setFixedCoefficients("td", c);// alt fix alle auf  einmal
        // neu fix mit dem Namen der Variable
        double[] c = new double[1];
        c[0] = 0.08;
        rs.setFixedCoefficients("td|" + "Vars-1@x_t", c);

        double[] c2 = new double[1];
        c2[0] = 0.01;
        rs.setFixedCoefficients("td|" + "Vars-1@x_e_m", c2);

        double[] c3 = new double[1];
        c3[0] = 0.02;
        rs.setFixedCoefficients("td|" + "Vars-1@x_e_a", c3);

        return rs;
    }

    private static RegressionSpec makeUserDefinedTradingdaySpec_allnotFix() {
        RegressionSpec rs = new RegressionSpec();
        TradingDaysSpec tds = new TradingDaysSpec();
        tds.setTest(RegressionTestSpec.None);
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        tds.setUserVariables(name);
        rs.setTradingDays(tds);
        return rs;
    }

    private static RegressionSpec makeUserDefinedVariablesSpec_allnotfix() {
        RegressionSpec rs = new RegressionSpec();
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        TsVariableDescriptor tsVariablesDescriptor1 = new TsVariableDescriptor(name[0]);
        tsVariablesDescriptor1.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor1);

        TsVariableDescriptor tsVariablesDescriptor2 = new TsVariableDescriptor(name[1]);
        tsVariablesDescriptor2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor2);

        TsVariableDescriptor tsVariablesDescriptor3 = new TsVariableDescriptor(name[2]);
        tsVariablesDescriptor3.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor3);
        return rs;
    }

    private static RegressionSpec makeUserDefinedVariablesSpec_allfix() {
        RegressionSpec rs = new RegressionSpec();
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        TsVariableDescriptor tsVariablesDescriptor1 = new TsVariableDescriptor(name[0]);
        tsVariablesDescriptor1.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor1);

        TsVariableDescriptor tsVariablesDescriptor2 = new TsVariableDescriptor(name[1]);
        tsVariablesDescriptor2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor2);

        TsVariableDescriptor tsVariablesDescriptor3 = new TsVariableDescriptor(name[2]);
        tsVariablesDescriptor3.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor3);

        double[] c1 = new double[1];
        c1[0] = 0.08;
        double[] c2 = new double[1];
        c2[0] = 0.01;
        double[] c3 = new double[1];
        c3[0] = 0.02;
        rs.setFixedCoefficients("Vars-1@x_t", c1);

        rs.setFixedCoefficients("Vars-1@x_e_m", c2);
        rs.setFixedCoefficients("Vars-1@x_e_a", c3);

        return rs;
    }

    private static RegressionSpec makeUserDefinedVariablesSpec_3_1_fix() {
        RegressionSpec rs = new RegressionSpec();
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        TsVariableDescriptor tsVariablesDescriptor1 = new TsVariableDescriptor(name[0]);
        tsVariablesDescriptor1.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor1);

        TsVariableDescriptor tsVariablesDescriptor2 = new TsVariableDescriptor(name[1]);
        tsVariablesDescriptor2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor2);

        TsVariableDescriptor tsVariablesDescriptor3 = new TsVariableDescriptor(name[2]);
        tsVariablesDescriptor3.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor3);

        double[] c = new double[1];
        c[0] = 0.08;
//        c[1] = 0.07;
//        c[2] = 0.09;
        rs.setFixedCoefficients("Vars-1@x_t", c);
        //    rs.setFixedCoefficients("Vars-1@at", c);//glaube falsch
        return rs;
    }

    private static RegressionSpec makeUserDefinedVariablesSpec_3_1_fixlast() {
        RegressionSpec rs = new RegressionSpec();
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        TsVariableDescriptor tsVariablesDescriptor1 = new TsVariableDescriptor(name[0]);
        tsVariablesDescriptor1.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor1);

        TsVariableDescriptor tsVariablesDescriptor2 = new TsVariableDescriptor(name[1]);
        tsVariablesDescriptor2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor2);

        TsVariableDescriptor tsVariablesDescriptor3 = new TsVariableDescriptor(name[2]);
        tsVariablesDescriptor3.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor3);

        double[] c = new double[1];
        //       c[0] = 0.08;
//        c[1] = 0.07;
        c[0] = 0.09;
        rs.setFixedCoefficients("Vars-1@x_e_a", c);
        return rs;
    }

//     private static RegressionSpec makeUserDefinedVariablesSpec_3_1_fix() {
//        RegressionSpec rs = new RegressionSpec();
//        String[] name = new String[3];
//        name[0] = "Vars-1.x_t";
//        name[1] = "Vars-1.x_e_m";
//        name[2] = "Vars-1.x_e_a";
//        TsVariableDescriptor tsVariablesDescriptor1 = new TsVariableDescriptor(name[0]);
//        tsVariablesDescriptor1.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
//        rs.add(tsVariablesDescriptor1);
//
//        TsVariableDescriptor tsVariablesDescriptor2 = new TsVariableDescriptor(name[1]);
//        tsVariablesDescriptor2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
//        rs.add(tsVariablesDescriptor2);
//
//        TsVariableDescriptor tsVariablesDescriptor3 = new TsVariableDescriptor(name[2]);
//        tsVariablesDescriptor3.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
//        rs.add(tsVariablesDescriptor3);
//
//        double[] c = new double[1];
//        c[0] = 0.08;
////        c[1] = 0.07;
////        c[2] = 0.09;
//        rs.setFixedCoefficients("Vars-1@x_t", c);
//        //    rs.setFixedCoefficients("Vars-1@at", c);//glaube falsch
//        return rs;
//    }
    private static RegressionSpec makeUserDefinedVariablesSpec_fix_first_and_last() {
        RegressionSpec rs = new RegressionSpec();
        String[] name = new String[3];
        name[0] = "Vars-1.x_t";
        name[1] = "Vars-1.x_e_m";
        name[2] = "Vars-1.x_e_a";
        TsVariableDescriptor tsVariablesDescriptor1 = new TsVariableDescriptor(name[0]);
        tsVariablesDescriptor1.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor1);

        TsVariableDescriptor tsVariablesDescriptor2 = new TsVariableDescriptor(name[1]);
        tsVariablesDescriptor2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor2);

        TsVariableDescriptor tsVariablesDescriptor3 = new TsVariableDescriptor(name[2]);
        tsVariablesDescriptor3.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        rs.add(tsVariablesDescriptor3);

        double[] c1 = new double[1];
        c1[0] = 0.07;
        double[] c2 = new double[1];
        c2[0] = 0.01;
        //    c[2] = 0.00;
        rs.setFixedCoefficients("Vars-1@x_t", c1);

        rs.setFixedCoefficients("Vars-1@x_e_a", c2);
        return rs;
    }

    private static ProcessingContext context;

    public static final ProcessingContext makeContext() {

        if (context == null) {
            context = new ProcessingContext();
            NameManager<TsVariables> activeMgr = context.getTsVariableManagers();
            TsVariables mgr = new TsVariables();
            mgr.set("x_t", TSVRG_T); //ok
            mgr.set("x_e_m", TSVRG_E_M);
            mgr.set("x_e_a", TSVRG_E_A);
            activeMgr.set("Vars-1", mgr);//ok
            activeMgr.resetDirty();
            ProcessingContext.setActiveContext(context);
        }

        return context;
    }

    //Regressionvariables for calendar
    private static final double[] RG_T = {0.834146341, -0.253658537, 0.351219512, -0.829268293, 1.236585366, -1.090243902, -0.170731707, 0.795121951, -1.390243902, 0.507317073, -0.504878049, -1.512195122, 0.834146341, -0.253658537, -1.648780488, 1.170731707, -0.463414634, -0.390243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -1.504878049, -0.812195122, 0.434146341, 0.146341463, -0.048780488, 0.170731707, -0.763414634, -0.090243902, 0.829268293, -1.004878049, 0.609756098, 0.607317073, -0.804878049, 0.487804878, -1.565853659, -0.253658537, 1.351219512, 0.170731707, -0.763414634, 0.909756098, -0.170731707, -0.004878049, 0.609756098, -0.392682927, -0.504878049, 1.187804878, -0.565853659, -0.253658537, 1.351219512, -0.829268293, 0.236585366, 0.909756098, -1.170731707, 0.795121951, 0.609756098, -1.492682927, -0.504878049, 1.187804878, 0.434146341, -0.253658537, 1.351219512, -1.829268293, 1.236585366, -0.090243902, -1.170731707, 0.795121951, -0.390243902, -0.492682927, 0.495121951, -0.112195122, 0.834146341, 0.746341463, -0.648780488, 0.170731707, 0.236585366, -1.090243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -0.504878049, -0.812195122, 0.434146341, -0.253658537, -2.648780488, 2.170731707, -1.463414634, 0.609756098, 0.829268293, -1.204878049, 0.609756098, 0.507317073, -0.804878049, -0.112195122, -0.565853659, -0.253658537, 0.351219512, 0.170731707, -0.763414634, -0.090243902, 0.829268293, -1.004878049, 0.609756098, 0.607317073, 0.195121951, 0.487804878, -1.565853659, -0.253658537, 1.351219512, 0.170731707, -0.763414634, 0.909756098, -0.170731707, -0.004878049, 0.609756098, -0.392682927, 0.495121951, 1.187804878, -0.565853659, 1.146341463, 0.951219512, -1.829268293, 2.236585366, -1.090243902, -1.170731707, 0.795121951, -0.390243902, -0.492682927, 0.495121951, -0.112195122, 0.834146341, -0.253658537, 0.351219512, -0.829268293, 1.236585366, -1.090243902, -0.170731707, 0.795121951, -1.390243902, 0.507317073, 0.495121951, -1.512195122, 0.834146341, -0.253658537, -1.648780488, 1.170731707, -0.463414634, -0.390243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -0.504878049, -0.812195122, 0.434146341, 0.146341463, -1.048780488, 0.170731707, 0.236585366, -1.090243902, 0.829268293, -1.204878049, 0.609756098, 0.507317073, -0.804878049, -0.112195122, -0.565853659, -0.253658537, 1.351219512, 0.170731707, -0.763414634, 0.909756098, -0.170731707, -0.004878049, 0.609756098, -0.392682927, 0.495121951, 1.187804878, -0.565853659, -0.253658537, -0.648780488, 1.170731707, -0.463414634, 1.609756098, -1.170731707, 0.795121951, 0.609756098, -1.492682927, 0.495121951, 1.187804878, 0.434146341, -0.253658537, 1.351219512, -1.829268293, 1.236585366, -0.090243902, -1.170731707, 0.795121951, -0.390243902, -0.492682927, 0.495121951, -0.112195122, 0.834146341, -0.253658537, 0.351219512, -0.829268293, 0.236585366, -0.090243902, -0.170731707, 0.795121951, -1.390243902, 0.507317073, 0.495121951, -1.512195122, 0.834146341, 0.746341463, -2.648780488, 2.170731707, -0.463414634, 0.609756098, 0.829268293, -1.204878049, 0.609756098, 0.507317073, -0.804878049, -0.112195122, -0.565853659, -0.253658537, 0.351219512, 0.170731707, -0.763414634, -0.090243902, 0.829268293, -1.004878049, 0.609756098, 0.607317073, 0.195121951, 0.487804878, -1.565853659, -0.253658537, 1.351219512, 0.170731707, -0.763414634, 0.909756098, -0.170731707, -0.004878049, 0.609756098, -0.392682927, 0.495121951, 1.187804878, -0.565853659, 0.146341463, 0.951219512, -0.829268293, 2.236585366, -1.090243902, -1.170731707, 0.795121951, 0.609756098, -1.492682927, 0.495121951, 1.187804878, 0.434146341, 0.746341463, 0.351219512, -0.829268293, 0.236585366, -0.090243902, -0.170731707, 0.795121951, -1.390243902, 0.507317073, 0.495121951, -1.512195122, 0.834146341, -0.253658537, -1.648780488, 1.170731707, -0.463414634, -0.390243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -0.504878049, -0.812195122, 0.434146341, 0.146341463, -1.048780488, 0.170731707, 0.236585366, -1.090243902, 0.829268293, -1.204878049, 0.609756098, 0.507317073, -0.804878049, -0.112195122, -0.565853659, -0.253658537, 0.351219512, 0.170731707, -1.763414634, 0.909756098, 0.829268293, -1.004878049, 0.609756098, 0.607317073, 0.195121951, 0.487804878, -1.565853659, 0.746341463, -0.648780488, 1.170731707, -0.463414634, 1.609756098, -1.170731707, 0.795121951, 0.609756098, -1.492682927, 0.495121951, 1.187804878, 0.434146341, -0.253658537, 1.351219512, -1.829268293, 1.236585366, -0.090243902, -1.170731707, 0.795121951, -0.390243902, -0.492682927, 0.495121951, -0.112195122, 0.834146341, -0.253658537, -0.648780488, 0.170731707, -0.463414634, 0.609756098, -0.170731707, 0.795121951, -1.390243902, 0.507317073, 0.495121951, -1.512195122, 0.834146341, 0.146341463, -1.048780488, 0.170731707, 1.236585366, -2.090243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -0.504878049, -0.812195122, 0.434146341, -0.253658537, 0.351219512, 0.170731707, -0.763414634, -0.090243902, 0.829268293, -1.004878049, 0.609756098, 0.607317073, 0.195121951, 0.487804878, -1.565853659, -0.253658537, 1.351219512, 0.170731707, -0.763414634, 0.909756098, -0.170731707, -0.004878049, 0.609756098, -0.392682927, 0.495121951, 1.187804878, -0.565853659, -0.053658537, 1.151219512, -0.829268293, 1.236585366, -0.090243902, -1.170731707, 0.795121951, 0.609756098, -1.492682927, 0.495121951, 1.187804878, 0.434146341, -0.253658537, 1.351219512, -1.829268293, 0.236585366, 0.909756098, -1.170731707, 0.795121951, -0.390243902, -0.492682927, 0.495121951, -0.112195122, 0.834146341, 0.746341463, -1.648780488, 1.170731707, -0.463414634, -0.390243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -0.504878049, -0.812195122, 0.434146341, 0.146341463, -1.048780488, 0.170731707, 0.236585366, -1.090243902, 0.829268293, -1.204878049, 0.609756098, 0.507317073, -0.804878049, -0.112195122, -0.565853659, -0.253658537, 0.351219512, 0.170731707, -1.763414634, 0.909756098, 0.829268293, -1.004878049, 0.609756098, 0.607317073, 0.195121951, 0.487804878, -1.565853659, -0.253658537, -0.648780488, 2.170731707, -1.463414634, 1.609756098, -0.170731707, -0.004878049, 0.609756098, -0.392682927, 0.495121951, 1.187804878, -0.565853659, 0.746341463, 1.351219512, -1.829268293, 1.236585366, -0.090243902, -1.170731707, 0.795121951, -0.390243902, -0.492682927, 0.495121951, -0.112195122, 0.834146341, -0.253658537, -0.648780488, 0.170731707, -0.463414634, 0.609756098, -0.170731707, 0.795121951, -1.390243902, 0.507317073, 0.495121951, -1.512195122, 0.834146341, 0.146341463, -1.048780488, 0.170731707, 1.236585366, -2.090243902, 0.829268293, -0.204878049, -0.390243902, 0.507317073, -0.504878049, -0.812195122};
    private static final TsData TSRG_T = new TsData(TsFrequency.Monthly, 1990, 0, RG_T, false);
    private static final TsVariable TSVRG_T = new TsVariable("at", TSRG_T);
    private static final double[] RG_E_M = {0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final TsData TSRG_E_M = new TsData(TsFrequency.Monthly, 1990, 0, RG_E_M, false);
    private static final TsVariable TSVRG_E_M = new TsVariable("aem", TSRG_E_M);
    private static final double[] RG_E_A = {0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.780487805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.219512195, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final TsData TSRG_E_A = new TsData(TsFrequency.Monthly, 1990, 0, RG_E_A, false);
    private static final TsVariable TSVRG_E_A = new TsVariable("aea", TSRG_E_A);

    private static final double[] TS = {65.3, 63.5, 68.1, 69.7, 64.9, 70.2, 67.7, 61.1, 71, 72.9, 71.1, 67.1, 65, 66.5, 73.6, 69.3, 65.9, 70.5, 67, 59.7, 74.4, 71, 68.6, 68.6, 55.4, 59.3, 70.8, 63.3, 59.7, 66.6, 60.1, 58.9, 69.8, 65.1, 66, 67.6, 56.3, 59.3, 71.4, 62.4, 64.1, 69.6, 61.6, 61.8, 73.6, 67.4, 72, 72.1, 63.1, 64.4, 76.9, 64, 71.2, 71.7, 64.3, 64.8, 73.3, 69.2, 73.3, 68.6, 63.5, 64.2, 70.2, 68, 67.6, 68.7, 69, 62.6, 74.4, 73.2, 73.2, 68.6, 63.7, 64.8, 68.9, 75.2, 66.1, 76.9, 75.2, 64.2, 80.3, 77.6, 76.5, 74, 65.8, 69.6, 82, 74.5, 72.3, 79, 77.9, 67, 81.4, 79.5, 78.3, 75.8, 63.8, 68.5, 84.9, 73.9, 71.8, 81.7, 76.1, 71.3, 85.9, 80.4, 84.5, 81.6, 69.4, 78.6, 89.6, 75.7, 88.7, 83.9, 80.8, 81.2, 90, 87.7, 92.9, 84.1, 80.8, 82, 94.2, 82.4, 88.3, 85.5, 84, 82, 87, 89.2, 89.4, 77.1, 78.6, 78.6, 84.5, 86.2, 80.4, 85.5, 86.2, 78.1, 89.4, 89.2, 88.4, 80.8, 77.8, 80.1, 87.1, 84.6, 82, 82, 88.1, 72.8, 90.8, 89.8, 88.1, 84.9, 76.1, 81.9, 97.4, 88.2, 84.4, 95.3, 89.6, 81.3, 97.4, 92, 95.5, 90.7, 81, 84.2, 95.8, 93.2, 87.1, 100.9, 89.4, 86.5, 103.1, 93.6, 102.2, 96.4, 87.7, 91.6, 108.7, 91.8, 102.5, 103.5, 97.3, 95.4, 107.9, 105.2, 112.6, 100.1, 98.7, 100.4, 116.4, 101.7, 107.4, 111.4, 108.3, 101.9, 111.4, 117, 118.1, 101, 106.2, 111.2, 110.1, 120.3, 107.9, 118.3, 113.4, 98.9, 119.6, 115.2, 105.5, 93.3, 78.3, 80.1, 93.9, 83, 83, 91.2, 90.3, 78.2, 99.7, 95.7, 96.2, 89, 78.2, 86.6, 106.9, 95.4, 95.8, 107.7, 99, 93.1, 110.4, 107.2, 113, 106.9, 94.5, 103.3, 121.8, 105, 116.9, 109.1, 108.2, 106.2, 120.2, 108.8, 117.8, 108.2, 100.7, 108.8, 122.7, 105.4, 111.7, 114.5, 111.8, 106.8, 111.1, 114.8, 115.8, 98.4, 100.3, 102.3, 113.2, 114.3, 107.1, 112.1, 113.1, 102.4, 116.2, 116.6, 117.1, 104.6, 105.1, 108.6, 118.2, 112.6, 110.9, 112.6, 118.9, 98.3, 122.5, 120.1, 116.8, 109.7, 101.8, 108.8, 126.9, 116.1, 108.6, 124, 122.3, 102.5, 124.2, 123.3, 121.2, 113.3, 99.9, 114.1, 122.5, 120.3, 112.1, 125.7, 109.8, 111};
    public static final TsData TS_DATA = new TsData(TsFrequency.Monthly, 1991, 0, TS, false);
}
