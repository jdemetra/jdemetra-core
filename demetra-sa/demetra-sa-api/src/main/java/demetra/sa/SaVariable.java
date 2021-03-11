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
package demetra.sa;

import demetra.data.Range;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.SwitchOutlier;
import demetra.timeseries.regression.TransitoryChange;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaVariable {

    public ComponentType defaultComponentTypeOf(IOutlier v) {
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

    public ComponentType defaultComponentTypeOf(InterventionVariable var) {
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

    /**
     * Specific attributes
     */
    public final String REGEFFECT = "regeffect";
}
