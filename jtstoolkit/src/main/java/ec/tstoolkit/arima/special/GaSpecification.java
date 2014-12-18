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

package ec.tstoolkit.arima.special;

import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.data.SubTableOfInt;
import ec.tstoolkit.data.TableOfInt;
import ec.tstoolkit.design.Development;

/**
 * Specifications for the estimation of a generalized airline model The
 * specifications don't apply to a given generalized airline model but to the
 * complete processing (identification, estimation, decomposition).
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class GaSpecification implements Cloneable {

    public enum EstimationMode {

        Exhaustive, Iterative, Selective
    }

    public enum Criterion {

        AIC, BIC, GMAIC
    }

    /**
     * Creates new specifications with default options
     * <p>
     * See also : SetDefault Method
     */
    public GaSpecification() {
        setDefault();
    }

    /**
     * Set the specifications to their default values
     * <p>
     * The default value is specified in the documentation of each property.
     */
    public final void setDefault() {
        m_f0 = false;
        m_fmin = 1;
        m_fmax = 2;
        m_bfgs = false;
        m_criterion = Criterion.AIC;
        m_urBound = .999;
        m_fixUR = true;
        m_emode = EstimationMode.Selective;
    }

    // / <summary>
    // / Are the parameters that correspond to "quasi-unit root" fixed on their
    // boundary
    // / (=1) ?
    // / </summary>
    // / <remarks>True by default.</remarks>
    public boolean isFixingUnitRoots() {
        return m_fixUR;
    }

    public void fixUnitRoots(final boolean value) {
        m_fixUR = value;
    }

    // / <summary>Is the parameter corresponding to the zero-frequency free
    // ?</summary>
    // / <remarks>
    // / A value "True" corresponds to a 4-parameters model, while a value
    // "False"
    // / corresponds to a 3-parameters model.
    // / </remarks>
    public boolean isFreeZeroFrequencyParameter() {
        return m_f0;
    }

    public void setFreeZeroFrequencyParameter(final boolean value) {
        m_f0 = value;
    }

    // / <summary>
    // / Minimal number of coefficients that belong to the smallest group of
    // / parameters
    // / </summary>
    // / <remarks>
    // / We must have that MinFrequencyGroup &lt;= MaxFrequencyGroup. The
    // default value is
    // / 1.
    // / </remarks>
    // / <seealso cref="MaxFrequencyGroup">MaxFrequencyGroup Property</seealso>
    // / <example>
    // / <para>For monthly series, we can have the following values:</para>
    // / <para>
    // / <table cols="4">
    // / <tbody>
    // / <tr>
    // / <td><strong>Model</strong></td>
    // / <td><strong>FreeZeroFrequencyParameter</strong></td>
    // / <td><strong>MinFrequencyGroup</strong></td>
    // / <td><strong>MaxFrequencyGroup</strong></td>
    // / </tr>
    // / <tr>
    // / <td><strong>5-1(3)</strong></td>
    // / <td>false</td>
    // / <td>1</td>
    // / <td>1</td>
    // / </tr>
    // / <tr>
    // / <td><strong>4-2(3)</strong></td>
    // / <td>false</td>
    // / <td>2</td>
    // / <td>2</td>
    // / </tr>
    // / <tr>
    // / <td><strong>3-3(3)</strong></td>
    // / <td>false</td>
    // / <td>3</td>
    // / <td>3</td>
    // / </tr>
    // / <tr>
    // / <td><strong>All (3)</strong></td>
    // / <td>false</td>
    // / <td>1</td>
    // / <td>3</td>
    // / </tr>
    // / <tr>
    // / <td><strong>All(4)</strong></td>
    // / <td>true</td>
    // / <td>1</td>
    // / <td>3</td>
    // / </tr>
    // / </tbody>
    // / </table>
    // / </para>
    // / </example>
    public int getMinFrequencyGroup() {
        return m_fmin;
    }

    public void setMinFrequencyGroup(final int value) {
        m_fmin = value;
    }

    // / <summary>
    // / Maximal number of coefficients that belong to the smallest group of
    // / parameters
    // / </summary>
    // / <remarks>
    // / We must have that MinFrequencyGroup &lt;= MaxFrequencyGroup. The
    // default value is
    // / 1. Should be less or equal the frequency of the series divided by 4.
    // / </remarks>
    // / <seealso cref="MinFrequencyGroup">MinFrequencyGroup Property</seealso>
    public int getMaxFrequencyGroup() {
        return m_fmax;
    }

    public void setMaxFrequencyGroup(final int value) {
        m_fmax = value;
    }

    // / <summary>Is BFGS chosen as optimzation method</summary>
    // / <remarks>True by default.</remarks>
    public boolean isUsingBFGS() {
        return m_bfgs;
    }

    public void useBFGS(final boolean value) {
        m_bfgs = value;
    }

    // / <summary>The chosen criterion for the selection of the preferred
    // model</summary>
    // / <value>AIC by default (GMAIC is currently not supported).</value>
    public Criterion getCriterion() {
        return m_criterion;
    }

    public void setCriterion(final Criterion value) {
        m_criterion = value;
    }

    // / <summary>
    // / The upper bound for the inverse of the roots of the moving average
    // / polynomial.
    // / </summary>
    // / <remarks>The given boundary is used only in the decomposition
    // phase.</remarks>
    // / <seealso cref="GeneralizedAirline.UCModel">UCModel Method
    // (Nbb.GeneralAirline.GeneralizedAirline)</seealso>
    public double getURBound() {
        return m_urBound;
    }

    public void setURBound(final double value) {
        m_urBound = value;
    }

    public EstimationMode getEstimationMode() {
        return m_emode;
    }

    public void setEstimationMode(final EstimationMode emode) {
        m_emode = emode;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    public String[] generateModelTypes(final int freq) {
        TableOfInt table = generateParameters(freq);
        String[] rslt = new String[table.getRowsCount() + 1];
        rslt[0] = "Airline (2)";
        int nparams = m_f0 ? 4 : 3;
        for (int i = 0; i < table.getRowsCount(); ++i) {
            SubArrayOfInt c = table.row(i);
            int gcount = 0;
            for (int j = 0; j < table.getColumnsCount(); ++j) {
                if (c.get(j) == nparams - 1) {
                    ++gcount;
                }
            }
            StringBuilder builder = new StringBuilder();
            builder.append(freq / 2 - gcount);
            builder.append('-');
            builder.append(gcount);
            builder.append(" (").append(nparams).append("){");
            int j = 0;
            do {
                if (c.get(j) == nparams - 1) {
                    builder.append(j + 1);
                    if (--gcount > 0) {
                        builder.append('-');
                    } else {
                        break;
                    }
                }
                ++j;
            } while (gcount > 0);
            builder.append('}');
            rslt[i + 1] = builder.toString();
        }
        return rslt;
    }

    public TableOfInt generateParameters(final int freq) {
        // computes the number of rows
        int nrows = 0; // airline is excluded
        int freq2 = freq / 2, freq4 = freq2 / 2;
        int imin = Math.min(freq4, getMinFrequencyGroup());
        int imax = Math.min(freq4, getMaxFrequencyGroup());
        if (imin > 0) {
            for (int i = imin; i <= imax; ++i) {
                if (2 * i == freq2 && isFreeZeroFrequencyParameter()) {
                    nrows += C(freq2, i) / 2;
                } else {
                    nrows += C(freq2, i);
                }
            }
        }
        int ncols = freq2;

        TableOfInt rslt = new TableOfInt(nrows, ncols);

        int idx = 0;
        int k = 0;
        if (isFreeZeroFrequencyParameter()) {
            rslt.extract(0, nrows, 0, ncols).set(2);
            k = 3;
        } else {
            rslt.extract(0, nrows, 0, ncols).set(1);
            k = 2;
        }
        if (imin > 0) {
            for (int i = imin; i <= imax; ++i) {
                // SubTableOfInt m = rslt.Extract(idx, rslt.getRowsCount(), 2,
                // rslt.getColumnsCount());
                if (2 * i == freq2 && isFreeZeroFrequencyParameter()) {
                    SubTableOfInt sm = rslt.extract(idx, rslt.getRowsCount(),
                            1, rslt.getColumnsCount());
                    int nidx = fill(sm, k, i);
                    idx += nidx;
                } else {
                    SubTableOfInt sm = rslt.extract(idx, rslt.getRowsCount(),
                            0, rslt.getColumnsCount());
                    idx += fill(sm, k, i);
                }
            }
        }
        return rslt;
    }

    private int C(final int n, final int p) {
        int c = n;
        for (int j = n - 1; j > n - p; --j) {
            c *= j;
        }
        for (int j = p; j > 1; --j) {
            c /= j;
        }
        return c;
    }

    private int fill(final SubTableOfInt rslt, final int k, final int i) {
        int ncols = rslt.getColumnsCount();
        if (i == 1) {
            for (int b = 0; b < ncols; ++b) {
                rslt.set(b, b, k);
            }
            return ncols;
        } else {
            int r = 0;
            for (int b = 0; b <= ncols - i; ++b) {
                int dr = fill(rslt.extract(r, rslt.getRowsCount(), b + 1, ncols), k,
                        i - 1);
                for (int j = 0; j < dr; ++j) {
                    rslt.set(r++, b, k);
                }
            }
            return r;
        }
    }

    // / <summary>Creates a copy of this object</summary>
    // / <returns>The copy object.</returns>
    @Override
    public GaSpecification clone() {
        try {
            return (GaSpecification) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
    private boolean m_f0;
    private int m_fmin;
    private int m_fmax;
    private boolean m_bfgs;
    private EstimationMode m_emode = EstimationMode.Exhaustive;
    private Criterion m_criterion = Criterion.AIC;
    private double m_urBound = .999;
    private boolean m_fixUR;
}
