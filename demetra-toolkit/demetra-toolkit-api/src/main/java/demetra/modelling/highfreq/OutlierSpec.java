/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.modelling.highfreq;

import demetra.timeseries.TimeSelector;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true)
public class OutlierSpec {

    public static final int DEF_MAXOUTLIERS = 100, DEF_MAXROUND = 100;

    // automatic outliers detection
    public static final OutlierSpec DEFAULT_DISABLED = OutlierSpec.builder().build();
    public static final OutlierSpec DEFAULT_ENABLED = OutlierSpec.builder().ao(true).ls(true).build();

    private boolean ao, ls, wo;
    private double criticalValue;
    @lombok.NonNull
    private TimeSelector span;

    private int maxOutliers, maxRound;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .criticalValue(0)
                .maxOutliers(DEF_MAXOUTLIERS)
                .maxRound(DEF_MAXROUND)
                .span(TimeSelector.all());
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_DISABLED);
    }

    public boolean isUsed() {
        return ao || ls || wo;
    }

    private static final String[] NONE = new String[0];

    public String[] allOutliers() {
        int n = 0;
        if (ao) {
            ++n;
        }
        if (ls) {
            ++n;
        }
        if (wo) {
            ++n;
        }

        if (n == 0) {
            return NONE;
        }
        String[] all = new String[n];
        n = 0;
        if (ao) {
            all[n++] = "AO";
        }
        if (ls) {
            all[n++] = "LS";
        }
        if (wo) {
            all[n++] = "WO";
        }

        return all;
    }

}
