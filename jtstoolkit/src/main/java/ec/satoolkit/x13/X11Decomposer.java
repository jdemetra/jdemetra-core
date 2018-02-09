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
package ec.satoolkit.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.IPreprocessingFilter;
import ec.satoolkit.x11.*;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class X11Decomposer implements IDefaultSeriesDecomposer<X11Results> {

    private X11Specification spec_;
    private X11Results results_;

    public X11Decomposer(X11Specification spec) {
        spec_ = spec;
    }

    @Override
    public boolean decompose(TsData s) {
        X11Specification spec = spec_.clone();
        X11Toolkit toolkit = X11Toolkit.create(spec);
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        results_ = kernel.process(s);
        return results_ != null;
    }

    @Override
    public boolean decompose(PreprocessingModel model, IPreprocessingFilter filter) {
        X11Specification spec = prepareSpec(model);
        X11Toolkit toolkit = X11Toolkit.create(spec);
        toolkit.setPreprocessor(new DefaultPreprocessor(model, filter));
        X11Kernel kernel = new X11Kernel();
        kernel.setToolkit(toolkit);
        results_ = kernel.process(model.interpolatedSeries(false));
        return results_ != null;
    }

    @Override
    public X11Results getDecomposition() {
        return results_;
    }

    private X11Specification prepareSpec(PreprocessingModel model) {
        X11Specification spec = spec_.clone();
        boolean mul = model.isMultiplicative();
        if (mul) {
            if (!spec.getMode().isMultiplicative()) {
                spec.setMode(DecompositionMode.Multiplicative);
            }
        } else {
            spec.setMode(DecompositionMode.Additive);
        }
        return spec;
    }

    private X11Specification prepareSpec(TsData s) {
        X11Specification spec = spec_.clone();
        DescriptiveStatistics stats = new DescriptiveStatistics(s);
        boolean add = stats.isNegativeOrNull();
        if (add) {
            spec.setMode(DecompositionMode.Additive);
        }
        return spec;
    }
}
