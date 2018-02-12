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
package demetra.sarima;

import demetra.arima.ArimaException;
import demetra.design.Development;

/**
 * Definition of the orders of the polynomials of Box-Jenkins models.
 * Corresponds to (P, D, Q)(BP, BD, BQ)s
 *
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Data
public final class SarimaSpecification implements Cloneable {

    private int period, p, d, q, bp, bd, bq;

    /**
     * Generates an airline model (0, 1, 1)(0, 1, 1) or (0, 1, 1), following the
     * parameter.
     *
     * @param seas Model with or without a seasonal part.
     */
    public void airline(boolean seas) {
        p = 0;
        d = 1;
        q = 1;
        bp = 0;
        if (seas) {
            bd = 1;
            bq = 1;
        } else {
            bd = 0;
            bq = 0;
        }
    }

    public void airline(int period) {
        p = 0;
        d = 1;
        q = 1;
        bp = 0;
        this.period=period;
        if (period>1) {
            bd = 1;
            bq = 1;
        } else {
            bd = 0;
            bq = 0;
        }
    }
    
    @Override
    public SarimaSpecification clone() {
        try {
            return (SarimaSpecification) super.clone();
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param arma
     */
    public void copy(final SarmaSpecification arma) {
        p = arma.getP();
        q = arma.getQ();
        bp = arma.getBp();
        bq = arma.getBq();
    }

    // / <summary>
    // /
    // / </summary>
    // / <returns></returns>
    /**
     *
     * @return
     */
    public SarmaSpecification doStationary() {
        SarmaSpecification arma = new SarmaSpecification();
        arma.setPeriod(period);
        arma.setP(p);
        arma.setQ(q);
        arma.setBp(bp);
        arma.setBq(bq);
        return arma;
    }

    /**
     *
     * @return
     */
    public int getDifferenceOrder() {
        int diff = d;
        if (period > 1 && bd > 0) {
            diff += period * bd;
        }
        return diff;
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        return p + bp + q + bq;
    }

     /**
     *
     * @param seas
     * @return
     */
    public boolean isAirline(boolean seas) {
        boolean ok = p == 0 && q == 1 && d == 1;
        if (!ok || !seas) {
            return ok;
        }
        return bp == 0 && bq == 1 && bd == 1;
    }

    public boolean hasSeasonalPart() {
        return bp > 0 || bq > 0 || bd == 1;
    }

    /**
     * Literal representation of the specification
     *
     * @return (P, D, Q)(BP, BD, BQ) or(P, D, Q) (non seasonal model)
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(p).append(',');
        builder.append(getD()).append(',');
        builder.append(q).append(')');
        if (period > 1) {
            builder.append('(');
            builder.append(getBp()).append(',');
            builder.append(getBd()).append(',');
            builder.append(getBq()).append(')');
        }
        return builder.toString();
    }


}
