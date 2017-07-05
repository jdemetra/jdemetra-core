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
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;

/**
 * Definition of the orders of the polynomials of Box-Jenkins models.
 * Corresponds to (P, D, Q)(BP, BD, BQ)s
 *
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaSpecification implements Cloneable {

    // Optimization. Default differencing filters
    private static final BackFilter m10 = differencingFilter(12, 1, 0), m20 = differencingFilter(12, 2, 0), m01 = differencingFilter(12, 0, 1),
            m11 = differencingFilter(12, 1, 1), q10 = differencingFilter(4, 1, 0), q20 = differencingFilter(4, 2, 0), q01 = differencingFilter(4, 0, 1), q11 = differencingFilter(4, 1, 1);

    public static BackFilter differencingFilter(int freq, int d, int bd) {
        Polynomial X = null;
        if (d > 0) {
            X = UnitRoots.D(1, d);
        }
        if (bd > 0) {
            Polynomial XD = UnitRoots.D(freq, bd);
            if (X == null) {
                X = XD;
            } else {
                X = X.times(XD);
            }
        }
        if (X == null) {
            X = Polynomial.ONE;
        }
        return new BackFilter(X);

    }

    /**
     * Frequency
     */
    int frequency;

    /**
     * Regular auto-regressive order
     */
    int P;

    /**
     * Regular differencing order
     */
    int D;

    /**
     * Regular moving average order
     */
    int Q;

    /**
     * Seasonal auto-regressive order
     */
    int BP;

    /**
     * Seasonal differencing order
     */
    int BD;

    /**
     * Seasonal moving average order
     */
    int BQ;

    /**
     * Default specification (0, 0, 0). Unused seasonal part.
     */
    public SarimaSpecification() {
    }

    /**
     * Default seasonal specification (0, 0, 0)(0, 0, 0)s.
     *
     * @param freq Frequency of the model (s).
     */
    public SarimaSpecification(final int freq) {
        frequency = freq;
        P = D = Q = BP = BD = BQ = 0;
    }

    /**
     * Converts a Sarma specification into a Sarima specification, (D=0, BD=0)
     *
     * @param spec Given Sarma specification
     */
    public SarimaSpecification(final SarmaSpecification spec) {
        frequency = spec.getFrequency();
        P = spec.getP();
        Q = spec.getQ();
        BP = spec.getBP();
        BQ = spec.getBQ();
    }

    /**
     * Generates an airline model (0, 1, 1)(0, 1, 1) or (0, 1, 1) if the
     * frequency is higher than 1.
     */
    public void airline() {
        airline(frequency > 1);
    }

    /**
     * Generates an airline model (0, 1, 1)(0, 1, 1) or (0, 1, 1), following the
     * parameter.
     *
     * @param seas Model with or without a seasonal part.
     */
    public void airline(boolean seas) {
        P = 0;
        D = 1;
        Q = 1;
        BP = 0;
        if (seas) {
            BD = 1;
            BQ = 1;
        } else {
            BD = 0;
            BQ = 0;
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
        P = arma.getP();
        Q = arma.getQ();
        BP = arma.getBP();
        BQ = arma.getBQ();
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
        arma.setFrequency(frequency);
        arma.setP(P);
        arma.setQ(Q);
        arma.setBP(BP);
        arma.setBQ(BQ);
        return arma;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SarimaSpecification && equals((SarimaSpecification) obj));
    }

    private boolean equals(SarimaSpecification other) {
        return other.frequency == frequency && other.P == P
                && other.D == D && other.Q == Q && other.BP == BP
                && other.BD == BD && other.BQ == BQ;
    }

    /**
     *
     * @return
     */
    public int getBD() {
        return BD;
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
    public int getD() {
        return D;
    }

    /**
     *
     * @return
     */
    public int getDifferenceOrder() {
        int d = D;
        if (frequency > 1 && BD > 0) {
            d += frequency * BD;
        }
        return d;
    }

    /**
     *
     * @return
     */
    public BackFilter getDifferencingFilter() {
        // search in the pre-specified filters
        if (D == 0 && BD == 0) {
            return BackFilter.ONE;
        }
        if (frequency == 12) {
            if (BD == 0) {
                if (D == 1) {
                    return m10;
                } else if (D == 2) {
                    return m20;
                }
            } else if (BD == 1) {
                if (D == 0) {
                    return m01;
                } else if (D == 1) {
                    return m11;
                }
            } else if (frequency == 4) {
                if (BD == 0) {
                    if (D == 1) {
                        return q10;
                    } else if (D == 2) {
                        return q20;
                    }
                } else if (BD == 1) {
                    if (D == 0) {
                        return q01;
                    } else if (D == 1) {
                        return q11;
                    }
                }
            }
        }
        return differencingFilter(frequency, D, BD);
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return frequency;
    }

    // / <summary>
    // /
    // / </summary>
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

    // / <summary>
    // /
    // / </summary>
    /**
     *
     * @return
     */
    public int getQ() {
        return Q;
    }

    /**
     *
     * @return
     */
    public UnitRoots getUnitRoots() {
        UnitRoots ur = new UnitRoots();
        if (frequency > 1) {
            for (int i = 0; i < getBD(); ++i) {
                ur.add(frequency);
            }
        }
        for (int i = 0; i < getD(); ++i) {
            ur.add(1);
        }
        return ur;
    }

    @Override
    public int hashCode() {
        return frequency + P + D + Q + BP + BD + BQ;
    }

    /**
     *
     * @param freq
     */
    public void initialize(final int freq) {
        frequency = freq;
        P = D = Q = BP = BD = BQ = 0;
    }

    /**
     *
     * @return
     */
    public boolean isAirline(boolean seas) {
        boolean ok = P == 0 && Q == 1 && D == 1;
        if (!ok || !seas) {
            return ok;
        }
        return BP == 0 && BQ == 1 && BD == 1;
    }

    public boolean hasSeasonalPart() {
        return BP > 0 || BQ > 0 || BD == 1;
    }

    /**
     *
     * @param value
     */
    public void setBD(final int value) throws ArimaException {
//	if (value < 0 || value > SarimaModel.BDMax)
//	    throw new ArimaException(ArimaException.SArimaOutofRange);
        BD = value;
    }

    /**
     *
     * @param value
     */
    public void setBP(final int value) throws ArimaException {
//	if (value < 0 || value > SarimaModel.BPMax)
//	    throw new ArimaException(ArimaException.SArimaOutofRange);
        BP = value;
    }

    /**
     *
     * @param value
     */
    public void setBQ(final int value) throws ArimaException {
//	if (value < 0 || value > SarimaModel.BQMax)
//	    throw new ArimaException(ArimaException.SArimaOutofRange);
        BQ = value;
    }

    /**
     *
     * @param value
     */
    public void setD(final int value) throws ArimaException {
//	if (value < 0 || value > SarimaModel.DMax)
//	    throw new ArimaException(ArimaException.SArimaOutofRange);
        D = value;
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
     */
    public void setP(final int value) throws ArimaException {
//	if (value < 0 || value > SarimaModel.PMax)
//	    throw new ArimaException(ArimaException.SArimaOutofRange);
        P = value;
    }

    /**
     *
     * @param value
     */
    public void setQ(final int value) throws ArimaException {
//	if (value < 0 || value > SarimaModel.QMax)
//	    throw new ArimaException(ArimaException.SArimaOutofRange);
        Q = value;
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
        builder.append(getP()).append(',');
        builder.append(getD()).append(',');
        builder.append(getQ()).append(')');
        if (getFrequency() > 1) {
            builder.append('(');
            builder.append(getBP()).append(',');
            builder.append(getBD()).append(',');
            builder.append(getBQ()).append(')');
        }
        return builder.toString();
    }

}
