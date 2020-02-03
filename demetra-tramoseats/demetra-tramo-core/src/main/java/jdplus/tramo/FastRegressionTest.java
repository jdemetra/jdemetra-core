/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramo;

import demetra.design.BuilderPattern;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.RegArimaModel;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.IRegressionTest;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegArimaModelling;
import jdplus.regsarima.regular.TRegressionTest;
import jdplus.sarima.SarimaModel;
import java.util.Optional;

/**
 * Remove non significant regression items. The model is not re-estimated
 *
 * @author palatej
 */
public class FastRegressionTest implements IRegressionModule {

    public static final double CVAL = 1.96;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(FastRegressionTest.class)
    public static class Builder {

        private double tmean = CVAL, tvar = CVAL;
        private boolean testMean = true;

        public Builder meanThreshold(double tmean) {
            this.tmean = tmean;
            return this;
        }

        public Builder varThreshold(double tvar) {
            this.tvar = tvar;
            return this;
        }

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public FastRegressionTest build() {
            return new FastRegressionTest(this);
        }

    }

    private final IRegressionTest tdTest, wdTest, lpTest, mhTest, meanTest;

    private FastRegressionTest(Builder builder) {
        tdTest = new TRegressionTest(builder.tvar, builder.tvar);
        wdTest = new TRegressionTest(builder.tvar);
        lpTest = new TRegressionTest(builder.tvar);
        mhTest = new TRegressionTest(builder.tvar);
        meanTest = builder.testMean ? new TRegressionTest(builder.tmean) : null;
    }

    @Override
    public ProcessingResult test(final RegArimaModelling context) {
        // estimate the model.
        ModelDescription currentModel = context.getDescription();
        // make a copy.
        ModelDescription tmpModel = ModelDescription.copyOf(currentModel);
        boolean changed = false;
        RegArimaModel<SarimaModel> regarima = tmpModel.regarima();
        ConcentratedLikelihoodWithMissing ll = context.getEstimation().getConcentratedLikelihood();

        int start = regarima.isMean() ? 1 : 0;
        int nhp = tmpModel.getArimaComponent().getFreeParametersCount();

        Optional<Variable> td = tmpModel.variables().filter(var -> var.isTradingDays() && !var.isPrespecified()).findFirst();
        Optional<Variable> lp = tmpModel.variables().filter(var -> var.isLengthOfPeriod() && !var.isPrespecified()).findFirst();
        Optional<Variable> easter = tmpModel.variables().filter(var -> var.isEaster() && !var.isPrespecified()).findFirst();
        // td
        boolean removetd = false;
        if (td.isPresent()) {
            Variable variable = td.get();
            int pos = tmpModel.findPosition(variable.getVariable());
            int dim = variable.getVariable().dim();
            IRegressionTest test = dim == 1 ? wdTest : tdTest;
            if (!test.accept(ll, nhp, pos, dim, null)) {
                removetd = true;
            }
        }
        if (removetd && lp.isPresent()) {
            Variable variable = lp.get();
            int pos = tmpModel.findPosition(variable.getVariable());
            if (lpTest.accept(ll, nhp, pos, 1, null)) {
                removetd = false;
            } else {
                currentModel.remove(variable.getVariable());
                changed = true;
            }
        }

        if (removetd) {
            currentModel.remove(td.get().getVariable());
            if (lp.isPresent())
                currentModel.remove(lp.get().getVariable());
            changed = true;
        }

        if (easter.isPresent()) {
            Variable variable = easter.get();
            int pos =  tmpModel.findPosition(variable.getVariable());
            if (!mhTest.accept(ll, nhp, pos, 1, null)) {
                currentModel.remove(variable.getVariable());
                changed = true;
            }
        }
        
        if (meanTest != null && regarima.isMean() && !meanTest.accept(ll, nhp, 0, 1, null)) {
            currentModel.setMean(false);
            changed = true;
        }

        if (changed) {
            context.clearEstimation();
        }

        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;

    }

}
