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

import demetra.arima.estimation.IArimaMapping;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.regression.Variable;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.regular.IRegressionModule;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.AICcComparator;
import demetra.regarima.regular.IModelComparator;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.SarimaModel;
import demetra.modelling.regression.IEasterVariable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class EasterDetectionModule implements IRegressionModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(EasterDetectionModule.class)
    public static class Builder {

        private IEasterVariable[] easters;
        private IModelComparator comparator = new AICcComparator(0);
        private double eps = 1e-5;

        public Builder easters(IEasterVariable[] easters) {
            this.easters = easters;
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

        public EasterDetectionModule build() {
            return new EasterDetectionModule(this);
        }
    }

    private final IModelComparator comparator;
    private final IEasterVariable[] easters;
    private final double eps;

    public EasterDetectionModule(Builder builder) {
        this.comparator = builder.comparator;
        this.easters = builder.easters;
        this.eps = builder.eps;
    }

    @Override
    public ProcessingResult test(RegArimaModelling context) {
        ModelDescription description = context.getDescription();
        int n = easters.length;
        int icur = -1;
        ModelDescription[] desc = new ModelDescription[n];
        ModelEstimation[] est = new ModelEstimation[n];
        IArimaMapping<SarimaModel> mapping = description.getArimaComponent().defaultMapping();
        IRegArimaProcessor<SarimaModel> processor = RegArimaUtility.processor(mapping, true, eps);

        ModelDescription refdesc = new ModelDescription(description);
        refdesc.remove("easter");
        ModelEstimation refest = refdesc.estimate(processor);
        RegArimaEstimation<SarimaModel> process = processor.process(refdesc.regarima());

        for (int i = 0; i < n; ++i) {
            ModelDescription curDesc = new ModelDescription(refdesc);
            curDesc.addVariable(new Variable(easters[i], "easter", false));
            desc[i] = curDesc;
            est[i]=curDesc.estimate(processor);
         }

        // choose best model
        int imodel = comparator.compare(refest, est);
        if (imodel < 0) {
            context.setDescription(refdesc);
            context.setEstimation(refest);
        } else {
            context.setDescription(desc[imodel]);
            context.setEstimation(est[imodel]);
        }

        return icur == imodel ? ProcessingResult.Unchanged : ProcessingResult.Changed;
    }

}
