/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa;

import demetra.sa.ComponentType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ITsVariable;
import java.util.function.Predicate;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */
public class RegArimaDecomposer {

    private final ModelEstimation model;
    private final SaVariablesMapping mapping;

    public static RegArimaDecomposer of(final ModelEstimation model,
            final SaVariablesMapping mapping) {
        return new RegArimaDecomposer(model, mapping);
    }

    private RegArimaDecomposer(final ModelEstimation model,
            final SaVariablesMapping mapping) {
        this.model = model;
        this.mapping = mapping;
    }

    /**
     * Gets all the deterministic effect corresponding to a given component
     * type.
     * It includes the pre-specified (fixed) regression effects and the
     * estimated
     * regression effects
     *
     * @param domain The requested time domain
     * @param type The type of the component
     * @param transformed If true, the deterministic effect corresponds to the
     * model
     * after transformation (log and length of period). If false, the estimated
     * deterministic effects are back-transformed (exp). Only the regression
     * effect
     * corresponding to the calendar effect will be "back-corrected" for
     * length of period pre-adjustment.
     * @return
     */
    public TsData deterministicEffect(TsDomain domain, ComponentType type, boolean transformed) {
        final ITsVariable[] vars = mapping.forComponentType(type);
        TsData f = getModel().deterministicEffect(domain, v -> {
            for (int i = 0; i < vars.length; ++i) {
                if (v == vars[i]) {
                    return true;
                }
            }
            return false;
        });
        if (!transformed) {
            f = getModel().backTransform(f, type == ComponentType.CalendarEffect);
        }
        return f;
    }

    /**
     * Gets all the deterministic effect corresponding to a given component
     * type and verifying the given test. It includes the pre-specified 
     * (fixed) regression effects and the estimated regression effects
     *
     * @param domain The requested time domain
     * @param type The type of the component
     * @param transformed If true, the deterministic effect corresponds to the
     * model after transformation (log only). If false, the estimated
     * deterministic effects are back-transformed (exp). The series is never
     * corrected for length of period pre-adjustment.
     * @param test The selection test
     * @return
     */
    public TsData deterministicEffect(TsDomain domain, ComponentType type, boolean transformed, Predicate<ITsVariable> test) {
        final ITsVariable[] vars = mapping.forComponentType(type);
        TsData f = getModel().deterministicEffect(domain, v -> {
            if (!test.test(v)) {
                return false;
            }
            for (int i = 0; i < vars.length; ++i) {
                if (v == vars[i]) {
                    return true;
                }
            }
            return false;
        });
        if (!transformed) {
            f = getModel().backTransform(f, false);
        }
        return f;
    }

    /**
     * @return the model
     */
    public ModelEstimation getModel() {
        return model;
    }
}
