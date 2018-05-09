///*
//* Copyright 2013 National Bank of Belgium
//*
//* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
//* by the European Commission - subsequent versions of the EUPL (the "Licence");
//* You may not use this work except in compliance with the Licence.
//* You may obtain a copy of the Licence at:
//*
//* http://ec.europa.eu/idabc/eupl
//*
//* Unless required by applicable law or agreed to in writing, software 
//* distributed under the Licence is distributed on an "AS IS" basis,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the Licence for the specific language governing permissions and 
//* limitations under the Licence.
//*/
//
//package demetra.x12;
//
//import ec.tstoolkit.design.Development;
//import ec.tstoolkit.eco.ConcentratedLikelihood;
//import ec.tstoolkit.information.InformationSet;
//import ec.tstoolkit.modelling.arima.IPreprocessingModule;
//import ec.tstoolkit.modelling.arima.IRegressionTest;
//import ec.tstoolkit.modelling.arima.ModellingContext;
//import ec.tstoolkit.modelling.arima.ProcessingResult;
//import ec.tstoolkit.modelling.RegStatus;
//import ec.tstoolkit.modelling.arima.SeparateRegressionTest;
//import ec.tstoolkit.modelling.Variable;
//import ec.tstoolkit.modelling.arima.*;
//import ec.tstoolkit.timeseries.regression.ICalendarVariable;
//import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
//import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
//import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
//import ec.tstoolkit.timeseries.regression.ITsVariable;
//import ec.tstoolkit.timeseries.regression.TsVariableList;
//import ec.tstoolkit.timeseries.regression.TsVariableSelection;
//import java.util.ArrayList;
//
///**
// * See the Fortran routine pass0.f
// * @author Jean Palate
// */
//@Development(status = Development.Status.Preliminary)
//public class TradingDaysSelectionModule implements IPreprocessingModule {
//
//    public static final double CVAL = 1.96, F_PROB = 0.05;
//    public static final double TSIG = 1;
//    private IRegressionTest test_, mu_;
//    private DerivedRegressionTest derived_;
//
//    public TradingDaysSelectionModule(boolean join, double cval, boolean mu) {
//        if (join) {
//            test_ = new JointRegressionTest(F_PROB);
//        } else {
//            test_ = new SeparateRegressionTest(cval);
//            derived_ = new DerivedRegressionTest(cval);
//        }
//        if (mu) {
//            mu_ = new SeparateRegressionTest(cval);
//        }
//    }
//
//    public TradingDaysSelectionModule(double cval) {
//        test_ = new SeparateRegressionTest(cval);
//        derived_ = new DerivedRegressionTest(cval);
//    }
//
//    public TradingDaysSelectionModule(double cval, double tsig) {
//        test_ = new SeparateRegressionTest(cval);
//        derived_ = new DerivedRegressionTest(cval);
//        mu_ = new SeparateRegressionTest(tsig);
//    }
//    public void reset() {
//    }
//
//    @Override
//    public ProcessingResult process(ModellingContext context) {
//
//        ConcentratedLikelihood ll = context.estimation.getLikelihood();
//
//        boolean changed = false;
//        int start = context.description.getRegressionVariablesStartingPosition();
//        // td
//        InformationSet tdsubset = context.information.subSet(PreprocessingDictionary.CALENDAR);
//        InformationSet esubset = context.information.subSet(PreprocessingDictionary.EASTER);
//        TsVariableList x = context.description.buildRegressionVariables();
//        TsVariableSelection sel = x.selectCompatible(ITradingDaysVariable.class);
//        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
//        int nregs = context.description.countRegressors(var->var.status.isSelected() && var.getVariable() instanceof ICalendarVariable);
//        int ntd = context.description.countRegressors(var->var.status.isSelected() && var.getVariable() instanceof ITradingDaysVariable);
//        boolean usetd = false;
//        ArrayList<Variable> toreject = new ArrayList<>();
//        for (int i = 0; i < items.length; ++i) {
//            Variable search = context.description.searchVariable(items[i].variable);
//            if (search.status.needTesting()) {
//                if (!test_.accept(ll, -1, start + items[i].position, items[i].variable.getDim(), tdsubset)
//                        && (nregs <= 1 || (derived_ != null && !derived_.accept(ll, -1, start, ntd, null)))) {
//                    toreject.add(search);
//                } else {
//                    usetd = true;
//                }
//            }
//        }
//
//        sel = x.selectCompatible(ILengthOfPeriodVariable.class);
//        items = sel.elements();
//        boolean uselp = false;
//        for (int i = 0; i < items.length; ++i) {
//            Variable search = context.description.searchVariable(items[i].variable);
//            if (search.status.needTesting()) {
//                if (!test_.accept(ll, -1, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
//                    toreject.add(search);
//                } else {
//                    uselp = true;
//                }
//            }
//        }
//        if (!toreject.isEmpty() && !uselp && !usetd) {
//            changed = true;
//            for (Variable var : toreject) {
//                var.status = RegStatus.Rejected;
//            }
//        }
//        sel = x.selectCompatible(IMovingHolidayVariable.class);
//        items = sel.elements();
//        for (int i = 0; i < items.length; ++i) {
//            Variable search = context.description.searchVariable(items[i].variable);
//            if (search.status.needTesting()) {
//                if (!test_.accept(ll, -1, start + items[i].position, items[i].variable.getDim(), esubset)) {
//                    search.status = RegStatus.Rejected;
//                    changed = true;
//                }
//            }
//        }
//
//        if (mu_ != null && context.description.isEstimatedMean()) {
//            if (!mu_.accept(ll, -1, 0, 1, null)) {
//                context.description.setMean(false);
//                changed = true;
//            }
//        }
//
//        if (changed) {
//            context.estimation = null;
//            return ProcessingResult.Changed;
//        } else {
//            return ProcessingResult.Unchanged;
//        }
//
//    }
//}
