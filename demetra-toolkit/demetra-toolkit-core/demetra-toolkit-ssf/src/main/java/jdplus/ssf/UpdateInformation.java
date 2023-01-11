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
package jdplus.ssf;

import jdplus.data.DataBlock;

/**
 *
 * @author Jean Palate
 */
public class UpdateInformation {

    static public enum Status {
        OBSERVATION, CONSTRAINT, MISSING
    }

    private Status status;

    /**
     * e is the prediction error (=y(t)-Z(t)A(t))
     */
    private double e, f, stde;

    /**
     * M = P Z'
     */
    private final DataBlock M;

    /**
     *
     * @param dim
     */
    public UpdateInformation(final int dim) {
        M = DataBlock.make(dim);
    }

    /**
     *
     * @return
     */
    public double get() {
        return e;
    }

    /**
     *
     * @return
     */
    public double getVariance() {
        return f;
    }

    /**
     *
     * @return
     */
    public double getStandardDeviation() {
        return stde;
    }

    /**
     *
     * @return
     */
    public DataBlock M() {
        return M;
    }

    /**
     * =(ZPZ'+v) variance copyOf the prediction error
     *
     * @param f
     */
    public void setVariance(final double f) {
        this.f = f;
        this.stde = Math.sqrt(f);
    }

    ;
    
    public void setStandardDeviation(final double e) {
        this.f = e * e;
        this.stde = e;
    }

    public void set(final double val, final boolean constraint) {
        status = constraint ? Status.CONSTRAINT : Status.OBSERVATION;
        e = val;
    }

    /**
     *
     * @return
     */
    public boolean isMissing() {
        return status == Status.MISSING;
    }

    public void setMissing() {
        e = Double.NaN;
        status = Status.MISSING;
        M.set(() -> Double.NaN);
    }

    public Status getStatus() {
        return status;
    }

}
