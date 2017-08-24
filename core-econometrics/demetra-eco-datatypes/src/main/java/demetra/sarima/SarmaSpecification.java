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
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarmaSpecification implements Cloneable {

    int frequency, P, Q, BP, BQ;

    /**
     *
     */
    public SarmaSpecification() {
    }

    /**
     *
     * @param freq
     */
    public SarmaSpecification(final int freq) {
        frequency = freq;
    }

    @Override
    public SarmaSpecification clone() {
        try {
            return (SarmaSpecification) super.clone();
        } catch (CloneNotSupportedException err) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SarmaSpecification && equals((SarmaSpecification) obj));
    }

    private boolean equals(SarmaSpecification other) {
        return other.frequency == frequency && other.P == P
                && other.Q == Q && other.BP == BP
                && other.BQ == BQ;
    }

    /**
     *
     * @return
     */
    public int getBP() {
        return BP;
    }

    /**
     *
     * @return
     */
    public int getBQ() {
        return BQ;
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     *
     * @return
     */
    public int getP() {
        return P;
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        return P + BP + Q + BQ;
    }

    /**
     *
     * @return
     */
    public int getQ() {
        return Q;
    }

    @Override
    public int hashCode() {
        return frequency + P + Q + BP + BQ;
    }

    /**
     *
     * @param freq
     */
    public void initialize(final int freq) {
        frequency = freq;
    }

    /**
     *
     * @param value
     * @throws ArimaException
     */
    public void setBP(final int value) {
        // if (value < 0 || value > SArimaModel.BPMax)
        // throw new ArimaException(ArimaException.SArimaOutofRange);
        BP = value;
    }

    /**
     *
     * @param value
     * @throws ArimaException
     */
    public void setBQ(final int value) {
        // if (value < 0 || value > SArimaModel.BQMax)
        // throw new ArimaException(ArimaException.SArimaOutofRange);
        BQ = value;
    }

    /**
     *
     * @param value
     */
    public void setFrequency(final int value) {
        frequency = value;
    }

    /**
     *
     * @param value
     * @throws ArimaException
     */
    public void setP(final int value) {
        // if (value < 0 || value > SArimaModel.PMax)
        // throw new ArimaException(ArimaException.SArimaOutofRange);
        P = value;
    }

    /**
     *
     * @param value
     * @throws ArimaException
     */
    public void setQ(final int value) {
        // if (value < 0 || value > SArimaModel.QMax)
        // throw new ArimaException(ArimaException.SArimaOutofRange);
        Q = value;
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
        builder.append(P).append(',');
        builder.append(Q).append(')');
        if (getFrequency() > 1) {
            builder.append('(');
            builder.append(BP).append(',');
            builder.append(BQ).append(')');
        }
        return builder.toString();
    }
}
