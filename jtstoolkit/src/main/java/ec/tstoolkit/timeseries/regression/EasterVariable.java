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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.calendars.Utilities;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.GregorianCalendar;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class EasterVariable extends AbstractSingleTsVariable implements IMovingHolidayVariable {

    public static enum Type {

        Tramo,
        Uscb,
        Theoretical
    }
    private static final double[] EMeans_Feb = new double[]{0.00368E0, 0.002083333E0, 0.001130435E0, 0.0002727273E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0};
    private static final double[] EMeans_Mar = new double[]{0.6576E0, 0.6450833E0, 0.6311304E0, 0.6162727E0, 0.5999048E0,
        0.583E0, 0.5661053E0, 0.549E0, 0.5318824E0, 0.514625E0,
        0.4973333E0, 0.4807143E0, 0.4643077E0, 0.4476667E0, 0.4305455E0,
        0.4136E0, 0.3975556E0, 0.382E0, 0.3654286E0, 0.3483333E0,
        0.3304E0, 0.3125E0, 0.2966667E0, 0.281E0, 0.266E0};
    private static final double[] EMeans_Apr = new double[]{0.33872E0, 0.3528333E0, 0.3677391E0, 0.3834545E0, 0.4000952E0,
        0.417E0, 0.4338947E0, 0.451E0, 0.4681176E0, 0.485375E0,
        0.5026667E0, 0.5192857E0, 0.5356923E0, 0.5523333E0, 0.5694545E0,
        0.5864E0, 0.6024444E0, 0.618E0, 0.6345714E0, 0.6516667E0,
        0.6696E0, 0.6875E0, 0.7033333E0, 0.719E0, 0.734E0};
    private int dur_ = 6;
    private Type type_ = Type.Tramo;
    private boolean m_e, m_m;

    public EasterVariable() {
    }

    public int getDuration() {
        return dur_;
    }

    public void setDuration(int value) {
        dur_ = value;
    }

    public Type getType() {
        return type_;
    }

    public void setType(Type value) {
        type_ = value;
    }

    public boolean hasEaster() {
        return m_e;
    }

    public boolean hasEasterMonday() {
        return m_m;
    }

    public void includeEaster(boolean included) {
        m_e = included;
        if (!m_e) {
            m_m = false;
        }
    }

    public void includeEasterMonday(boolean included) {
        m_m = included;
        if (m_m) {
            m_e = true;
        }
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("Easter [").append(dur_).append(']');
        return builder.toString();
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        data.set(0);
        int freq = start.getFrequency().intValue();
        if ((freq != 12 && freq != 4) || dur_ < 1 || dur_ > 25) {
            return;
        }
        int n = data.getLength();
        int c = 12 / freq;
        int y0 = start.getYear();
        int p0 = start.getPosition() * c;
        if (p0 > 3) {
            p0 -= 12;
            ++y0;
        }
        // first april
        int idx = (3 - p0) / c;
        if (idx > n) {
            return;
        }

        double dur = dur_;


        GregorianCalendar easter = new GregorianCalendar();
        for (int y = y0; idx <= n; ++y, idx += freq) {
            easter.setTime(Utilities.easter(y).getTime());
            // DAY_OF_MONTH is 1-based
            int day = easter.get(GregorianCalendar.DAY_OF_MONTH);
            int month = easter.get(GregorianCalendar.MONTH);
            if (!m_e) {
                --day;
            } else if (m_m) {
                if (day == 31) {
                    day = 1;
                    ++month;
                } else {
                    ++day;
                }
            }

            // MONTH is 0-based
            double m = 0, a = 0;
            if (type_ == Type.Tramo) {
                if (month == 2 || day == 0) {
                    m = .5;
                    a = -.5;
                } else if (day >= dur_) {
                    m = -.5;
                    a = .5;
                } else {
                    m = (dur - day) / dur - .5;
                    a = -m;
                }
            } else // use long mean correction. Mean correction should be adapted
            // to take into account the new solution proposed in tramo (easter, easter monday)
            // included in the holiday
            {
                double m_av = 0, a_av = 0;
                if (type_ == Type.Uscb) {
                    m_av = EMeans_Mar[25 - dur_]; //(21 + dur) / 70.0;
                    a_av = EMeans_Apr[25 - dur_];//(49 - dur) / 70.0;
                } else {
                    /*
                     * Raw estimation of the probability to get Easter at a specific date is defined below:
                     * 22/3 (1/7)*1/LUNARY
                     * 23/3 (2/7)*1/LUNARY
                     * ...
                     * 27/3 (6/7)*1/LUNARY
                     * 28/3 1/LUNARY
                     * ...
                     * 18/4 1/LUNARY
                     * 19/4 1/LUNARY + (1/7) * DEC_LUNARY/LUNARY = (7 + DEC_LUNARY)/(7 * LUNARY)
                     * 20/4 (6/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY = (6 + DEC_LUNARY)/(7 * LUNARY)
                     * 21/4 (5/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
                     * 22/4 (4/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
                     * 23/4 (3/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
                     * 24/4 (2/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
                     * 25/4 (1/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
                     * 
                     * In comparison with long term (5700000 years) computations based on several algorithms,
                     * the error is small (< 0.0025)
                     * 
                     * The average number in march, folliwng dur (<= 15 !!!) is computed as follow:
                     * + for period 22/3 to 27/3: always in march
                     *    dur * (1/7 + ... + 6/7)/LUNARY = dur * 3 / LUNARY 
                     * + for period 28/3 to 1/4: always in march
                     *    dur * 5 / LUNARY
                     * + for period 2/4 to dur/4
                     *    (dur-1 + dur-2 + 1) / LUNARY = dur * (dur-1) / (2 * LUNARY)
                     * + for other dates: 0
                     * 
                     * Total:
                     *   dur * (6+10+dur-1)/ (2* LUNARY) = dur * 15 / (2 * LUNARY)
                     * Relative part:
                     *   15 / (2* LUNARY)
                     */
                    m_av = (15 + dur) / (2 * Utilities.LUNARY);
                    a_av = 1 - m_av;
                }
                if (month == 2) {
                    m = 1 - m_av;
                    a = -a_av;
                } else if (day >= dur_) {
                    m = -m_av;
                    a = 1 - a_av;
                } else {
                    m = (dur - day) / dur - m_av;
                    a = day / dur - a_av;
                }
            }

            if (idx - 1 >= 0) {
                data.set(idx - 1, m);
            }
            if (idx < n) {
                data.set(idx, a);
            }
        }
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.getFrequency() != TsFrequency.Yearly;
    }
}
