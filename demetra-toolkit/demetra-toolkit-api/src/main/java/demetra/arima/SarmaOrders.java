/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.arima;

import nbbrd.design.Development;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Data
public final class SarmaOrders implements Cloneable {

    private final int period;
    private int p, q, bp, bq;

    @Override
    public SarmaOrders clone() {
        try {
            return (SarmaOrders) super.clone();
        } catch (CloneNotSupportedException err) {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        return p + bp + q + bq;
    }

    /**
     * Literal representation of the specification
     *
     * @return (P, Q)(BP, BQ) or(P, Q) (non seasonal model)
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(p).append(',');
        builder.append(q).append(')');
        if (period > 1) {
            builder.append('(');
            builder.append(bp).append(',');
            builder.append(bq).append(')');
        }
        return builder.toString();
    }
}
