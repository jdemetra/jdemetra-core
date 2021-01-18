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
package jdplus.sa.modelling;

import demetra.data.Range;
import demetra.sa.ComponentType;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.SwitchOutlier;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.Variable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegArimaDecomposer {

    /**
     * Gets all the deterministic effect corresponding to a given component
     * type.It includes the pre-specified (fixed) regression effects and the
     * estimated
     * regression effects
     *
     * @param model
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
    public TsData deterministicEffect(ModelEstimation model, TsDomain domain, ComponentType type, boolean transformed) {
        TsData f = model.deterministicEffect(domain, v -> v.isAttribute(SaDictionary.REGEFFECT, type.name()));
        if (!transformed) {
            f = model.backTransform(f, type == ComponentType.CalendarEffect);
        }
        return f;
    }

    /**
     * Gets all the deterministic effect corresponding to a given component
     * type and verifying the given test.It includes the pre-specified
 (fixed) regression effects and the estimated regression effects
     *
     * @param model
     * @param domain The requested time domain
     * @param type The type of the component
     * @param transformed If true, the deterministic effect corresponds to the
     * model after transformation (log only). If false, the estimated
     * deterministic effects are back-transformed (exp). The series is never
     * corrected for length of period pre-adjustment.
     * @param test The selection test
     * @return
     */
    public TsData deterministicEffect(ModelEstimation model, TsDomain domain, ComponentType type, boolean transformed, Predicate<Variable> test) {
        TsData f = model.deterministicEffect(domain, v -> test.test(v) && v.isAttribute(SaDictionary.REGEFFECT, type.name()));
        if (!transformed) {
            f = model.backTransform(f, false);
        }
        return f;
    }

    public ComponentType componentTypeOf(IOutlier v) {
        if (v instanceof AdditiveOutlier || v instanceof TransitoryChange || v instanceof SwitchOutlier) {
            return ComponentType.Irregular;
        } else if (v instanceof LevelShift) {
            return ComponentType.Trend;
        } else if (v instanceof PeriodicOutlier) {
            return ComponentType.Seasonal;
        } else {
            return ComponentType.Undefined;
        }
    }

    public ComponentType componentTypeOf(InterventionVariable var) {
        if (var.getDeltaSeasonal() > 0 && var.getDelta() > 0) {
            return ComponentType.Undefined;
        }
        Range<LocalDateTime>[] sequences = var.getSequences();
        int maxseq = 0;
        for (int i = 0; i < sequences.length; ++i) {
            int len = (int) sequences[i].start().until(sequences[i].end(), ChronoUnit.DAYS) / 365;
            if (len > maxseq) {
                maxseq = len;
            }
        }
        if (maxseq > 0) {
            return var.getDeltaSeasonal() == 0 ? ComponentType.Trend : ComponentType.Undefined;
        }
        if (var.getDeltaSeasonal() > 0) {
            return ComponentType.Seasonal;
        }
        if (var.getDelta() > .8) {
            return ComponentType.Trend;
        }
        return ComponentType.Irregular;
    }

}
