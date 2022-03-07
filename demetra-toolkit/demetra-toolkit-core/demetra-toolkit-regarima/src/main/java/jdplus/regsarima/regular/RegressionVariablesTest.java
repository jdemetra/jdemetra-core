/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.regsarima.regular;

import jdplus.regarima.FRegressionTest;
import jdplus.regarima.DerivedRegressionTest;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import demetra.timeseries.calendars.LengthOfPeriodType;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.timeseries.regression.Variable;
import demetra.timeseries.regression.ITsVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import demetra.timeseries.regression.ModellingUtility;

/**
 * See the Fortran routine pass0.f
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegressionVariablesTest {

    public static final double CVAL = 1.96, F_PROB = 0.05;
    public static final double TSIG = 1;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(RegressionVariablesTest.class)
    public static class Builder {

        private double ftd = 0;
        private double tmu = CVAL, ttd = CVAL, tmh = CVAL;
        private boolean derived;

        public Builder meanTest(double t) {
            tmu = t;
            return this;
        }

        public Builder movingHolidaysTest(double t) {
            tmh = t;
            return this;
        }

        public Builder tdTest(double t, boolean derived) {
            ttd = t;
            this.derived = derived;
            ftd = 0;
            return this;
        }

        public Builder tdJointTest(double fprob) {
            ttd = 0;
            ftd = fprob;
            return this;
        }

        public RegressionVariablesTest build() {
            return new RegressionVariablesTest(this);
        }
    }

    private final IRegressionTest meanTest, tdTest, mhTest;
    private final DerivedRegressionTest derivedTest;

    private RegressionVariablesTest(Builder builder) {
        if (builder.tmu > 0) {
            meanTest = new TRegressionTest(builder.tmu);
        } else {
            meanTest = null;
        }
        if (builder.tmh > 0) {
            mhTest = new TRegressionTest(builder.tmh);
        } else {
            mhTest = null;
        }
        if (builder.ttd > 0) {
            tdTest = new TRegressionTest(builder.ttd);
            if (builder.derived) {
                derivedTest = new DerivedRegressionTest(builder.ttd, true);
            } else {
                derivedTest = null;
            }
        } else {
            derivedTest = null;
            if (builder.ftd > 0) {
                tdTest = new FRegressionTest(builder.ftd);
            } else {
                tdTest = null;
            }
        }
    }

    public ProcessingResult process(RegSarimaModelling context) {

        ConcentratedLikelihoodWithMissing ll = context.getEstimation().getConcentratedLikelihood();
        ModelDescription desc = context.getDescription();

        boolean changed = false;
        // td
        List<ITsVariable> tdtoremove = new ArrayList<>();
        boolean usetd = false, uselp = false;
        if (tdTest != null) {
            List<Variable> ltd = desc.variables().filter(v -> ModellingUtility.isTradingDays(v)).collect(Collectors.toList());
            for (Variable cur : ltd) {
                ITsVariable var = cur.getCore();
                int pos = desc.findPosition(var);
                int nregs = var.dim();
                if (!tdTest.accept(ll, -1, pos, nregs)
                        && (nregs <= 1 || derivedTest == null || !derivedTest.accept(ll, -1, pos, nregs))) {
                    tdtoremove.add(var);
                } else {
                    usetd = true;
                }
            }
            List<Variable> llp = desc.variables().filter(v -> ModellingUtility.isLengthOfPeriod(v)).collect(Collectors.toList());
            for (Variable cur : llp) {
                ITsVariable var = cur.getCore();
                int pos = desc.findPosition(var);
                if (!tdTest.accept(ll, -1, pos, 1)) {
                    tdtoremove.add(var);
                } else {
                    uselp = true;
                }
            }
        }
        if (mhTest != null) {
            List<ITsVariable> mhtoremove = new ArrayList<>();
            List<Variable> lmh = desc.variables().filter(v -> ModellingUtility.isMovingHoliday(v)).collect(Collectors.toList());
            for (Variable cur : lmh) {
                ITsVariable var = cur.getCore();
                int pos = desc.findPosition(var);
                if (!mhTest.accept(ll, -1, pos, 1)) {
                    mhtoremove.add(var);
                    changed = true;
                }
            }
            for (ITsVariable var : mhtoremove) {
                desc.remove(var);
            }
        }

        if (!tdtoremove.isEmpty() && !uselp && !usetd) {
            changed = true;
            for (ITsVariable var : tdtoremove) {
                desc.remove(var);
            }
            if (desc.isAdjusted()) {
                desc.setPreadjustment(LengthOfPeriodType.None);
            }
        }

        if (meanTest != null && desc.isMean()) {
            if (!meanTest.accept(ll, -1, 0, 1)) {
                desc.setMean(false);
                changed = true;
            }
        }

        if (changed) {
            context.clearEstimation();
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }

    }
}
