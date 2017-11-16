/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.PreadjustmentVariable;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import utilities.x13SpecSeveralCalendarRegressionVariables;

/**
 *
 * @author Christiane Hofer
 */
public class x13SeveralCalendarRegressionVariablesTest {

    @Test
    public void First_test_Variable_calendar_fix_first_and_last() {
        X13Specification x13spec_c = x13SpecSeveralCalendarRegressionVariables.getX13SpecCalendar_fix_first_last(DecompositionMode.Additive);

        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing_c = X13ProcessingFactory.instance.generateProcessing(x13spec_c, context);
        CompositeResults comprest = processing_c.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model_c = GenericSaResults.getPreprocessingModel(comprest);
        ConcentratedLikelihood ll_ = model_c.estimation.getLikelihood();
        double[] b_c = ll_.getB();

        List<PreadjustmentVariable> regsf_c = model_c.description.selectPreadjustmentVariables(var -> var.isCalendar());
        // start user
        X13Specification x13spec_u = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariables_fix_first_and_last(DecompositionMode.Additive);

        SequentialProcessing<TsData> processing_u = X13ProcessingFactory.instance.generateProcessing(x13spec_u, context);
        CompositeResults comprest_u = processing_u.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model_u = GenericSaResults.getPreprocessingModel(comprest_u);

        TsVariableList x_u = model_u.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> regs_u = x_u.select(var -> var instanceof IUserTsVariable);
        ConcentratedLikelihood ll_u = model_u.estimation.getLikelihood();
        double[] b_u = ll_u.getB();
        List<PreadjustmentVariable> regsf_u = model_u.description.selectPreadjustmentVariables(var -> var.isUser());
        // fix variables
        //Comparison between user and calendar
        Assert.assertEquals("fixed variable", regsf_c.get(0).getCoefficients()[0], regsf_u.get(0).getCoefficients()[0], 0.00000000000001);
        Assert.assertEquals("fixed variable", regsf_c.get(1).getCoefficients()[0], regsf_u.get(1).getCoefficients()[0], 0.00000000000001);
        Assert.assertEquals("not fixed variable", b_u[0], b_c[0], 0.00000000000001);

    }

    /**
     * First Test befor the implementation, for user-defined variables, it is
     * possible to fix only a part of the variables this is not possible for the
     * calendar component therefore I receive false for the test result
     */
    @Test
    public void First_test_Variable_calendar_1_fix_other_not() {
        X13Specification x13spec_c = x13SpecSeveralCalendarRegressionVariables.getX13SpecCalendar_3_1_fix(DecompositionMode.Additive);

        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing_c = X13ProcessingFactory.instance.generateProcessing(x13spec_c, context);
        CompositeResults comprest = processing_c.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model_c = GenericSaResults.getPreprocessingModel(comprest);
        ConcentratedLikelihood ll_ = model_c.estimation.getLikelihood();
        double[] b_c = ll_.getB();

        List<PreadjustmentVariable> regsf_c = model_c.description.selectPreadjustmentVariables(var -> var.isCalendar());
        Assert.assertEquals("td|Vars-1.x_t", regsf_c.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.08, regsf_c.get(0).getCoefficients()[0], 0.00000000000001);

        // start user
        X13Specification x13spec_u = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariables_3_1_fix(DecompositionMode.Additive);

        SequentialProcessing<TsData> processing_u = X13ProcessingFactory.instance.generateProcessing(x13spec_u, context);
        CompositeResults comprest_u = processing_u.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model_u = GenericSaResults.getPreprocessingModel(comprest_u);

        TsVariableList x_u = model_u.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> regs_u = x_u.select(var -> var instanceof IUserTsVariable);
        ConcentratedLikelihood ll_u = model_u.estimation.getLikelihood();
        double[] b_u = ll_u.getB();
        Assert.assertEquals("Vars-1.x_e_m", regs_u.elements()[0].variable.getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", -6.191057289181003, b_u[0], 0.00000000000001);
        Assert.assertEquals("Vars-1.x_e_a", regs_u.elements()[1].variable.getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", -6.682880653466752, b_u[1], 0.00000000000001);

        List<PreadjustmentVariable> regsf_u = model_u.description.selectPreadjustmentVariables(var -> var.isUser());
        // fix variables
        Assert.assertEquals("Vars-1.x_t", regsf_u.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.08, regsf_u.get(0).getCoefficients()[0], 0.00000000000001);

        //Comparison between user and calendar
        Assert.assertEquals("fixed variable", regsf_c.get(0).getCoefficients()[0], regsf_u.get(0).getCoefficients()[0], 0.00000000000001);

        Assert.assertTrue("no object for not fix regression variable for calender, wird erst nach der Anpassung der Entwicklung funktionieren", b_c != null);
        Assert.assertEquals("not fixed variable", b_u[0], b_c[0], 0.00000000000001);
        Assert.assertEquals("not fixed variable", b_u[1], b_c[1], 0.00000000000001);

    }

    public x13SeveralCalendarRegressionVariablesTest() {
    }

    /**
     * First Test befor the implementation , for user-defined variables, it is
     * possible to fix only a part of the variables
     * the array of fix coefficents must have the same lenhth as the fixed
     * regressors
     */
    @Test
    public void First_test_Variable_fix_3_1last() {
        X13Specification x13spec = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariables_3_1_fixlast(DecompositionMode.Additive);

        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model = GenericSaResults.getPreprocessingModel(comprest);

        List<PreadjustmentVariable> regsf = model.description.selectPreadjustmentVariables(var -> var.isUser());
        Assert.assertEquals("Vars-1.x_e_a", regsf.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.09, regsf.get(0).getCoefficients()[0], 0.00000000000001);
    }

    /**
     * First Test befor the implementation , for user-defined variables, it is
     * possible to fix only a part of the variables
     * the array of fix coefficents must have the same lenhth as the fixed
     * regressors
     */
    @Test
    public void First_test_Variable_fix_first_and_last() {
        X13Specification x13spec = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariables_fix_first_and_last(DecompositionMode.Additive);

        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model = GenericSaResults.getPreprocessingModel(comprest);

        List<PreadjustmentVariable> regsf = model.description.selectPreadjustmentVariables(var -> var.isUser());
        Assert.assertEquals("Vars-1.x_t", regsf.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.07, regsf.get(0).getCoefficients()[0], 0.00000000000001);
        Assert.assertEquals("Vars-1.x_e_a", regsf.get(1).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.01, regsf.get(1).getCoefficients()[0], 0.00000000000001);

    }

    /**
     * First Test befor the implementation, for user-defined variables, it is
     * possible to fix only a part of the variables
     */
    @Test
    public void First_test_Variable() {
        X13Specification x13spec = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariables_3_1_fix(DecompositionMode.Additive);

        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model = GenericSaResults.getPreprocessingModel(comprest);

        TsVariableList x_ = model.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> regs = x_.select(var -> var instanceof IUserTsVariable);
//        System.out.println("not fixed variables:");
        ConcentratedLikelihood ll_ = model.estimation.getLikelihood();
        double[] b = ll_.getB();
//        int start = model.description.getRegressionVariablesStartingPosition();
//        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
//            for (int j = 0; j < reg.variable.getDim(); ++j) {
//                System.out.println(reg.variable.getItemDescription(j, TsFrequency.Monthly));
//                System.out.println(b[start + j + reg.position]);
//            }
//        }

        Assert.assertEquals("Vars-1.x_e_m", regs.elements()[0].variable.getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", -6.191057289181003, b[0], 0.00000000000001);
        Assert.assertEquals("Vars-1.x_e_a", regs.elements()[1].variable.getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", -6.682880653466752, b[1], 0.00000000000001);

        List<PreadjustmentVariable> regsf = model.description.selectPreadjustmentVariables(var -> var.isUser());
        // fix variables
//        for (PreadjustmentVariable reg : regsf) {
//            ITsVariable cur = reg.getVariable();
//            double[] c = reg.getCoefficients();
//            for (int j = 0; j < cur.getDim(); ++j) {
//                System.out.println(cur.getItemDescription(j, TsFrequency.Monthly) + " " + c[j]);
//            }
//        }
        Assert.assertEquals("Vars-1.x_t", regsf.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.08, regsf.get(0).getCoefficients()[0], 0.00000000000001);

    }

    /**
     * First TD calendar test befor the implementation, it is only possible to
     * fix one or all calendar variables
     */
    @Test
    public void First_Test_CalendarTD_allfix() {
        X13Specification x13spec = x13SpecSeveralCalendarRegressionVariables.getX13SpecTdUserDefinedAllFix(DecompositionMode.Additive);

        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model = GenericSaResults.getPreprocessingModel(comprest);

        TsVariableList x_ = model.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> regs = x_.select(var -> var instanceof ITradingDaysVariable);
//        System.out.println("not fixed variables:");
        ConcentratedLikelihood ll_ = model.estimation.getLikelihood();
        double[] b = ll_.getB();
//        int start = model.description.getRegressionVariablesStartingPosition();
//        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
//            for (int j = 0; j < reg.variable.getDim(); ++j) {
//                System.out.println(reg.variable.getItemDescription(j, TsFrequency.Monthly));
//                System.out.println(b[start + j + reg.position]);
//            }
//        }
        List<PreadjustmentVariable> regsf = model.description.selectPreadjustmentVariables(var -> var.isCalendar());
        // fix variables
//        for (PreadjustmentVariable reg : regsf) {
//            ITsVariable cur = reg.getVariable();
//            double[] c = reg.getCoefficients();
//            for (int j = 0; j < cur.getDim(); ++j) {
//                System.out.println(cur.getItemDescription(j, TsFrequency.Monthly) + " " + c[j]);
//            }
//        }
        //    Assert.assertEquals("at", regsf.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly)); // geändert auf Grund der Anpassung für TD, dass diese einzeln verarbeitet werden
        Assert.assertEquals("td|Vars-1.x_t", regsf.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.08, regsf.get(0).getCoefficients()[0], 0.00000000000001);
        Assert.assertEquals("td|Vars-1.x_e_m", regsf.get(1).getVariable().getItemDescription(0, TsFrequency.Monthly));
        //     Assert.assertEquals("aem", regsf.get(0).getVariable().getItemDescription(1, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.01, regsf.get(1).getCoefficients()[0], 0.00000000000001);
        //     Assert.assertEquals("aea", regsf.get(0).getVariable().getItemDescription(2, TsFrequency.Monthly));
        Assert.assertEquals("td|Vars-1.x_e_a", regsf.get(2).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.02, regsf.get(2).getCoefficients()[0], 0.00000000000001);
    }

    /**
     * user defined variables fix all
     */
    @Test
    public void First_Test_variable_fix_all() {
        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        X13Specification x13specU = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariablesAllFix(DecompositionMode.Additive);
        x13specU.getRegArimaSpecification().getRegression().clearAllFixedCoefficients();
        double[] c = new double[1];
//        c[0] = 3.191314584776395;
//        c[1] = 0.396997895651164;
//        c[2] = -0.5720861392524087;
        c[0] = 0.4;

        x13specU.getRegArimaSpecification().getRegression().setFixedCoefficients("Vars-1@x_t", c);
        double[] c2 = new double[1];
        c2[0] = 0.2;
        x13specU.getRegArimaSpecification().getRegression().setFixedCoefficients("Vars-1@x_e_m", c2);
        double[] c3 = new double[1];
        c3[0] = 0.5;
        x13specU.getRegArimaSpecification().getRegression().setFixedCoefficients("Vars-1@x_e_a", c3);

        SequentialProcessing<TsData> processingU = X13ProcessingFactory.instance.generateProcessing(x13specU, context);
        CompositeResults comprestU = processingU.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel modelU = GenericSaResults.getPreprocessingModel(comprestU);
        List<PreadjustmentVariable> regsf_u = modelU.description.selectPreadjustmentVariables(var -> var.isUser());

//        System.out.println(regsf_u.get(0).getCoefficients()[0]);
//        System.out.println(regsf_u.get(1).getCoefficients()[0]);
//        System.out.println(regsf_u.get(2).getCoefficients()[0]);
//        Assert.assertEquals("fixed variable", 0.08, regsf_u.get(0).getCoefficients()[0], 0.00000000000001);
//        Assert.assertEquals("fixed variable", 0.00, regsf_u.get(0).getCoefficients()[1], 0.00000000000001);
//        Assert.assertEquals("fixed variable", 0.00, regsf_u.get(0).getCoefficients()[2], 0.00000000000001);
    }

    /**
     * Before the implementation the results for tradingdays and user defined
     * variables-have to be the same if all variables are fixed, only the
     * specification and the componenten for the variabe should be different
     */
    @Test
    public void First_Test_CalendarTD_userDefinedVariable_fix() {
        X13Specification x13spec = x13SpecSeveralCalendarRegressionVariables.getX13SpecTdUserDefinedAllFix(DecompositionMode.Additive);
        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model = GenericSaResults.getPreprocessingModel(comprest);
        List<PreadjustmentVariable> regsf = model.description.selectPreadjustmentVariables(var -> var.isCalendar());
//        Assert.assertEquals("at", regsf.get(0).getVariable().getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.08, regsf.get(0).getCoefficients()[0], 0.00000000000001);
        //       Assert.assertEquals("aem", regsf.get(0).getVariable().getItemDescription(1, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.01, regsf.get(1).getCoefficients()[0], 0.00000000000001);
        //       Assert.assertEquals("aea", regsf.get(0).getVariable().getItemDescription(2, TsFrequency.Monthly));
        Assert.assertEquals("fixed variable", 0.02, regsf.get(2).getCoefficients()[0], 0.00000000000001);

        X13Specification x13specU = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariablesAllFix(DecompositionMode.Additive);
        SequentialProcessing<TsData> processingU = X13ProcessingFactory.instance.generateProcessing(x13specU, context);
        CompositeResults comprestU = processingU.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel modelU = GenericSaResults.getPreprocessingModel(comprestU);
        List<PreadjustmentVariable> regsf_u = modelU.description.selectPreadjustmentVariables(var -> var.isUser());

        TsVariableList x_ = modelU.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> regs = x_.select(var -> var instanceof IUserTsVariable);
        ConcentratedLikelihood ll_ = modelU.estimation.getLikelihood();
        double[] b = ll_.getB();
//        int start = modelU.description.getRegressionVariablesStartingPosition();
//        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
//            for (int j = 0; j < reg.variable.getDim(); ++j) {
//                System.out.println(reg.variable.getItemDescription(j, TsFrequency.Monthly));
//                System.out.println(b[start + j + reg.position]);
//            }
//        }

        Assert.assertEquals("fixed variable", regsf_u.get(0).getCoefficients()[0], regsf.get(0).getCoefficients()[0], 0.00000000000001);
        Assert.assertEquals("fixed variable", regsf_u.get(1).getCoefficients()[0], regsf.get(1).getCoefficients()[0], 0.00000000000001);
        Assert.assertEquals("fixed variable", regsf_u.get(2).getCoefficients()[0], regsf.get(2).getCoefficients()[0], 0.00000000000001);

    }

    /**
     * Before the implementation the results for tradingdays and user defined
     * variables-have to be the same if all variables are not fixed, only the
     * specification and the componenten for the variabe should be different
     */
    @Test
    public void First_Test_CalendarTD_userDefinedVariable_not_fix() {
        X13Specification x13spec = x13SpecSeveralCalendarRegressionVariables.getX13SpecTdUserDefinedAllnotFix(DecompositionMode.Additive);
        ProcessingContext context = x13SpecSeveralCalendarRegressionVariables.makeContext();
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec, context);
        CompositeResults comprest = processing.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel model = GenericSaResults.getPreprocessingModel(comprest);
        TsVariableList x_td = model.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> rgs_td = x_td.select(var -> var instanceof ITradingDaysVariable);
        ConcentratedLikelihood ll_td = model.estimation.getLikelihood();
        double[] b_td = ll_td.getB();
        int start_td = model.description.getRegressionVariablesStartingPosition();
//        for (TsVariableSelection.Item<ITsVariable> reg : rgs_td.elements()) {
//            for (int j = 0; j < reg.variable.getDim(); ++j) {
//                System.out.println(reg.variable.getItemDescription(j, TsFrequency.Monthly));
//                System.out.println(b_td[start_td + j + reg.position]);
//            }
//        }
        Assert.assertEquals("at", rgs_td.elements()[0].variable.getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", 3.191314584776395, b_td[0], 0.00000000000001);
        Assert.assertEquals("aem", rgs_td.elements()[0].variable.getItemDescription(1, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", 0.396997895651164, b_td[1], 0.00000000000001);
        Assert.assertEquals("aea", rgs_td.elements()[0].variable.getItemDescription(2, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", -0.5720861392524087, b_td[2], 0.00000000000001);

        X13Specification x13specU = x13SpecSeveralCalendarRegressionVariables.getX13SpecUserDefinedVariablesAllnotFix(DecompositionMode.Additive);
        SequentialProcessing<TsData> processingU = X13ProcessingFactory.instance.generateProcessing(x13specU, context);
        CompositeResults comprestU = processingU.process(x13SpecSeveralCalendarRegressionVariables.TS_DATA);
        PreprocessingModel modelU = GenericSaResults.getPreprocessingModel(comprestU);
        TsVariableList x_ = modelU.description.buildRegressionVariables();
        TsVariableSelection<ITsVariable> regsf_u = x_.select(var -> var instanceof IUserTsVariable);
        //    System.out.println("not fixed variables:");
        ConcentratedLikelihood ll_ = modelU.estimation.getLikelihood();
        double[] b_u = ll_.getB();
//        int start = modelU.description.getRegressionVariablesStartingPosition();
//        for (TsVariableSelection.Item<ITsVariable> reg : regsf_u.elements()) {
//            for (int j = 0; j < reg.variable.getDim(); ++j) {
//                System.out.println(reg.variable.getItemDescription(j, TsFrequency.Monthly));
//                System.out.println(b_u[start + j + reg.position]);
//            }
//        }

        Assert.assertEquals("Vars-1.x_t", regsf_u.elements()[0].variable.getItemDescription(0, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", 3.191314584776395, b_u[0], 0.00000000000001);
        Assert.assertEquals("Vars-1.x_e_m", regsf_u.elements()[1].variable.getItemDescription(1, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", 0.3969978956511644, b_u[1], 0.00000000000001);
        Assert.assertEquals("Vars-1.x_e_a", regsf_u.elements()[2].variable.getItemDescription(1, TsFrequency.Monthly));
        Assert.assertEquals("not fixed variable", -0.5720861392524087, b_u[2], 0.00000000000001);

        Assert.assertEquals("not fixed variable", b_u[0], b_td[0], 0.00000000000001);
        Assert.assertEquals("not fixed variable", b_u[1], b_td[1], 0.00000000000001);
        Assert.assertEquals("not fixed variable", b_u[2], b_td[2], 0.00000000000001);

    }

}
