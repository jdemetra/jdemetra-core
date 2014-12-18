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

package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * Moving seasonality table.
 * This class generates information that corresponds to the table D9a
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class MsrTable {

    private static double[] C = {1.00000e0, 1.02584e0, 1.01779e0, 1.01383e0,
        1.00000e0, 3.00000e0, 1.55291e0, 1.30095e0};

    /**
     *
     * @param ss
     * @param is
     * @param mul
     * @return
     */
    public static MsrTable create(TsData ss, TsData is, boolean mul) {

        MsrTable rms = new MsrTable();
        int freq = ss.getFrequency().intValue();
        // should be improved by means of partial iterators
        rms.m_rs = new double[freq];
        rms.m_ri = new double[freq];
        rms.m_n = new int[freq];
        PeriodIterator bs = new PeriodIterator(ss);
        PeriodIterator bi = new PeriodIterator(is);
        int tell = 0;
        while (bi.hasMoreElements()) {
            double ci = 0.0, cs = 0;
            DataBlock bdi = bi.nextElement().data;
            DataBlock bds = bs.nextElement().data;
            int nc = bdi.getLength();
            for (int i = 1; i < nc; i++) {
                double x0 = bdi.get(i - 1);
                double x1 = bdi.get(i);
                double d = x1 - x0;
                if (mul) {
                    d /= x0;
                }

                ci += Math.abs(d);
                x0 = bds.get(i - 1);
                x1 = bds.get(i);
                d = x1 - x0;
                if (mul) {
                    d /= x0;
                }

                cs += Math.abs(d);
            }
            rms.m_ri[tell] = ci / (nc - 1) * fis(nc - 1);
            rms.m_rs[tell] = cs / (nc - 1) * cs(nc - 1);
            rms.m_n[tell++] = nc - 1;
        }
        return rms;
    }

    private static double cs(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n + 2];
        } else {
            return n * 1.732051e0 / (8.485281e0 + (n - 6) * 1.732051e0);
        }
    }

//    private static double CS(int n) {
//
//	switch (n) {
//	case 3:
//	    return 1.0;
//	case 4:
//	    return CS_ct[0];
//	case 5:
//	    return CS_ct[1];
//	case 6:
//	    return CS_ct[2];
//	default:
//	    return ((Math.sqrt(3.0) * n) / (6.0 * Math.sqrt(2.0) + Math
//		    .sqrt(3.0)
//		    * (n - 6)));
//	}
//    }
    private static double fis(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n - 2];
        } else {
            return n * 12.247449e0 / (73.239334e0 + (n - 6) * 12.247449e0);
        }
    }
//    private static double FIS(int n) {
//
//	switch (n) {
//	case 3:
//	    return 1.0;
//	case 4:
//	    return FIS_ct[0];
//	case 5:
//	    return FIS_ct[1];
//	case 6:
//	    return FIS_ct[2];
//	default:
//	    return ((5.0 * Math.sqrt(6.0) * n) / (6.0 * Math.sqrt(149.0) + 5.0
//		    * Math.sqrt(6.0) * (n - 6)));
//	}
//    }
    private double[] m_rs;
    private double[] m_ri;
    private int[] m_n;

    private MsrTable() {
    }

    private MsrTable(double[] rs, double[] ri, int[] n) {
        m_rs = rs;
        m_ri = ri;
        if (n != null) {
            m_n = n;
        }
    }

    // / <summary>
    // / The number of elements per array. Also the frequency
    // / </summary>
    /**
     *
     * @return
     */
    public int getCount() {
        return m_ri.length;
    }

    // / <summary>
    // / The property returns the global Moving Seasonality Ratio. This is a
    // weighted sum
    // / of the per-period ratios. Weighting is done using the number of years
    // per period.
    // / </summary>
    /**
     *
     * @return
     */
    public double getGlobalMsr() {

        double ri = 0.0, rs = 0.0;
        for (int i = 0; i < m_ri.length; i++) {
            ri += m_ri[i] * m_n[i];
            rs += m_rs[i] * m_n[i];
        }
        return ri / rs;
    }

    // / <summary>
    // / The property returns the mean evolutions of the irregular component
    // / </summary>
    /**
     *
     * @return
     */
    public double[] getMeanIrregularEvolutions() {
        return m_ri;
    }

    // / <summary>
    // / The property returns the mean evolutions of the seasonal component
    // / </summary>
    /**
     *
     * @return
     */
    public double[] getMeanSeasonalEvolutions() {
        return m_rs;
    }

    // / <summary>
    // / The method returns the Moving Seasonality Ratio for the given period.
    // (I/S)
    // / </summary>
    // / <param name="idx">The position of the period (between 0 (inc) and
    // Frequency (exc))</param>
    /**
     *
     * @param idx
     * @return
     */
    public double getRMS(int idx) {
        return m_ri[idx] / m_rs[idx];
    }
}
