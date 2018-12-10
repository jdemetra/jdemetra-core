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

import demetra.design.Development;

/**
 * Definition of the orders of the polynomials of Box-Jenkins models.
 * Corresponds to (P, D, Q)(BP, BD, BQ)s
 *
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Data
public final class SarimaSpecification implements Cloneable {

    private final int period;
    private int p, d, q, bp, bd, bq;
    
    public static SarimaSpecification stationary(SarimaSpecification spec){
        SarimaSpecification sspec=new SarimaSpecification(spec.getPeriod());
        sspec.p=spec.p;
        sspec.q=spec.q;
        sspec.bp=spec.bp;
        sspec.bq=spec.bq;
        return sspec;
        
    }
    
    public static SarimaSpecification of(SarmaSpecification sspec, int d, int bd){
        SarimaSpecification spec=new SarimaSpecification(sspec.getPeriod());
        spec.p=sspec.getP();
        spec.d=d;
        spec.q=sspec.getQ();
        spec.bp=sspec.getBp();
        spec.bd=bd;
        spec.bq=sspec.getBq();
        return spec;
    }
    
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
     
    @Override
    public SarimaSpecification clone() {
        try {
            return (SarimaSpecification) super.clone();
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
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
        SarmaSpecification arma = new SarmaSpecification(period);
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

    public boolean isStationary() {
        return d==0 && bd==0; 
    }

    public boolean isSeasonal() {
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
