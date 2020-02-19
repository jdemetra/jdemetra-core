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
package demetra.x12;

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.AICcComparator;
import jdplus.regsarima.regular.IModelComparator;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.sarima.SarimaModel;
import java.util.ArrayList;
import java.util.List;
import demetra.timeseries.regression.IUserTsVariable;
import jdplus.regarima.RegArimaEstimation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class UserVariablesDetectionModule implements IRegressionModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(UserVariablesDetectionModule.class)
    public static class Builder {

        private final List<IUserTsVariable> users = new ArrayList<>();
        private IModelComparator comparator = new AICcComparator(0);
        private double eps = 1e-5;

        public Builder add(IUserTsVariable... vars) {
            for (int i = 0; i < vars.length; ++i) {
                users.add(vars[i]);
            }
            return this;
        }

        public Builder estimationPrecision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder modelComparator(IModelComparator comparator) {
            this.comparator = comparator;
            return this;
        }

        public UserVariablesDetectionModule build() {
            return new UserVariablesDetectionModule(this);
        }
    }

    private final IUserTsVariable[] users;
    private IModelComparator comparator = new AICcComparator(0);
    private final double eps;

    private UserVariablesDetectionModule(Builder builder) {
        this.comparator = builder.comparator;
        this.eps = builder.eps;
        this.users = builder.users.toArray(new IUserTsVariable[builder.users.size()]);
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {

        ModelDescription description = context.getDescription();
        RegArimaEstimation<SarimaModel> est = context.getEstimation();
        IRegArimaProcessor<SarimaModel> processor = RegArimaUtility.processor(true, eps);

        // builds models with and without user variables 
        for (int i = 0; i < users.length; ++i) {
            ModelDescription nudesc = ModelDescription.copyOf(description);
            final IUserTsVariable cur=users[i];
            boolean removed = nudesc.removeVariable(var->var.getVariable().equals(cur));
            ModelDescription udesc = ModelDescription.copyOf(nudesc);
            nudesc.addVariable(new Variable(users[i], "user-"+(i+1), false));

            RegArimaEstimation<SarimaModel> nuest, uest;
            if (removed || est == null) {
                nuest = nudesc.estimate(processor);
            } else {
                nuest = est;
            }
            if (!removed || est == null) {
                uest = udesc.estimate(processor);
            } else {
                uest = est;
            }
            if (comparator.compare(nuest, uest) == 0) {
                description = udesc;
                est = uest;
            } else {
                description = nudesc;
                est = nuest;
            }
        }

        boolean changed = false;

        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
