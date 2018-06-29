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
package demetra.regarima.regular;

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.modelling.Variable;
import demetra.modelling.regression.ITsVariable;
import demetra.timeseries.TsDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    public ProcessingResult process(RegArimaModelling context) {
        
        ConcentratedLikelihood ll = context.getEstimation().getConcentratedLikelihood();
        ModelDescription desc = context.getDescription();
        
        boolean changed = false;
        // td
        List<ITsVariable<TsDomain>> tdtoremove = new ArrayList<>();
        boolean usetd = false, uselp = false;
        if (tdTest != null) {
            List<Variable> ltd = desc.variables().filter(v -> v.isTradingDays()).collect(Collectors.toList());
            for (Variable cur : ltd) {
                ITsVariable<TsDomain> var = cur.getVariable();
                int pos = desc.findPosition(var);
                int nregs = var.getDim();
                if (!tdTest.accept(ll, 0, pos, nregs, null)
                        && (nregs <= 1 || derivedTest == null || !derivedTest.accept(ll, pos, nregs, nregs, null))) {
                    tdtoremove.add(var);
                } else {
                    usetd = true;
                }
            }
            List<Variable> llp = desc.variables().filter(v -> v.isLengthOfPeriod()).collect(Collectors.toList());
            for (Variable cur : llp) {
                ITsVariable<TsDomain> var = cur.getVariable();
                int pos = desc.findPosition(var);
                if (!tdTest.accept(ll, 0, pos, 1, null)) {
                    tdtoremove.add(var);
                } else {
                    uselp = true;
                }
            }
        }
        if (mhTest != null) {
            List<ITsVariable<TsDomain>> mhtoremove = new ArrayList<>();
            List<Variable> lmh = desc.variables().filter(v -> v.isMovingHolidays()).collect(Collectors.toList());
            for (Variable cur : lmh) {
                ITsVariable<TsDomain> var = cur.getVariable();
                int pos = desc.findPosition(var);
                if (!mhTest.accept(ll, 0, pos, 1, null)) {
                    mhtoremove.add(var);
                    changed = true;
                }
            }
            for (ITsVariable<TsDomain> var : mhtoremove) {
                desc.remove(var);
            }
        }
        
        if (!tdtoremove.isEmpty() && !uselp && !usetd) {
            changed = true;
            for (ITsVariable<TsDomain> var : tdtoremove) {
                desc.remove(var);
            }
        }
        
        if (meanTest
                != null && desc.isEstimatedMean()) {
            if (!meanTest.accept(ll, -1, 0, 1, null)) {
                desc.setMean(false);
                changed = true;
            }
        }
        
        if (changed) {
            context.setEstimation(null);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
        
    }
}
