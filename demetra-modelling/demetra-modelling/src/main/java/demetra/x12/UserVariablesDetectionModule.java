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
import demetra.modelling.Variable;
import demetra.modelling.regression.IUserTsVariable;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.ami.IRegressionModule;
import demetra.regarima.ami.ProcessingResult;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.AICcComparator;
import demetra.regarima.regular.IModelComparator;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaContext;
import demetra.sarima.SarimaModel;
import demetra.timeseries.TsDomain;
import java.util.ArrayList;
import java.util.List;

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

        private final List<IUserTsVariable<TsDomain>> users = new ArrayList<>();
        private IModelComparator comparator = new AICcComparator(0);
        private double eps = 1e-5;

        public Builder add(IUserTsVariable<TsDomain>... vars) {
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

    private final IUserTsVariable<TsDomain>[] users;
    private IModelComparator comparator = new AICcComparator(0);
    private final double eps;

    private UserVariablesDetectionModule(Builder builder) {
        this.comparator = builder.comparator;
        this.eps = builder.eps;
        this.users = builder.users.toArray(new IUserTsVariable[builder.users.size()]);
    }

    @Override
    public ProcessingResult test(RegArimaContext context) {

        ModelDescription description = context.getDescription();
        ModelEstimation est = context.getEstimation();
        IRegArimaProcessor<SarimaModel> processor = RegArimaUtility.processor(description.getArimaComponent().defaultMapping(), true, eps);

        // builds models with and without user variables 
        for (int i = 0; i < users.length; ++i) {
            ModelDescription nudesc = new ModelDescription(description);
            boolean removed = nudesc.remove(users[i].getName());
            ModelDescription udesc = new ModelDescription(nudesc);
            nudesc.addVariable(new Variable(users[i], false));

            ModelEstimation nuest, uest;
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
