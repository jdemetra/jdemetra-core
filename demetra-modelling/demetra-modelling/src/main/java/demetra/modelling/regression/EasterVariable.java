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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.Easter;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class EasterVariable implements IEasterVariable {

    public static enum Correction {
        Simple,
        PreComputed,
        Theoretical
    }
    private static final double[] MEANS_FEB = new double[]{0.00368E0, 0.002083333E0, 0.001130435E0, 0.0002727273E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0,
        0E0, 0E0, 0E0, 0E0, 0E0};
    private static final double[] MEANS_MAR = new double[]{0.6576E0, 0.6450833E0, 0.6311304E0, 0.6162727E0, 0.5999048E0,
        0.583E0, 0.5661053E0, 0.549E0, 0.5318824E0, 0.514625E0,
        0.4973333E0, 0.4807143E0, 0.4643077E0, 0.4476667E0, 0.4305455E0,
        0.4136E0, 0.3975556E0, 0.382E0, 0.3654286E0, 0.3483333E0,
        0.3304E0, 0.3125E0, 0.2966667E0, 0.281E0, 0.266E0};
    private static final double[] MEANS_APR = new double[]{0.33872E0, 0.3528333E0, 0.3677391E0, 0.3834545E0, 0.4000952E0,
        0.417E0, 0.4338947E0, 0.451E0, 0.4681176E0, 0.485375E0,
        0.5026667E0, 0.5192857E0, 0.5356923E0, 0.5523333E0, 0.5694545E0,
        0.5864E0, 0.6024444E0, 0.618E0, 0.6345714E0, 0.6516667E0,
        0.6696E0, 0.6875E0, 0.7033333E0, 0.719E0, 0.734E0};

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(EasterVariable.class)
    public static class Builder {

        private int duration = 6, endPosition = 0;
        private Correction meanCorrection = Correction.Simple;
        private String name;

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder meanCorrection(Correction correction) {
            this.meanCorrection = correction;
            return this;
        }

        /**
         * Position of the end of the Easter effect, relatively to Easter
         *
         * @param endpos
         * @return
         */
        public Builder endPosition(int endpos) {
            if (endpos < -1 || endpos > 1) {
                throw new TsException("Not supported yet");
            }
            this.endPosition = endpos;
            return this;
        }

        public EasterVariable build() {
            return new EasterVariable(duration, endPosition, meanCorrection, name != null ? name : NAME);
        }
    }


    private final int duration, endPosition;
    private final Correction meanCorrection;
    private final String name;

    private EasterVariable(int duration, int endPosition, Correction meanCorrection, String name) {
        this.duration = duration;
        this.endPosition = endPosition;
        this.meanCorrection = meanCorrection;
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public Correction getMeanCorrection() {
        return meanCorrection;
    }

    public int getEndPosition() {
        return endPosition;
    }

    @Override
    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Easter [").append(duration).append(']');
        return builder.toString();
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> ldata) {
        DataBlock data = ldata.get(0);
        data.set(0);
        TsPeriod start = domain.getStartPeriod();
        int freq = domain.getTsUnit().getAnnualFrequency();
        if ((freq != 12 && freq != 4) || duration < 1 || duration > 25) {
            return;
        }
        int n = data.length();
        int c = 12 / freq;
        int y0 = start.start().getYear();
        int p0 = start.start().getMonthValue();
        if (p0 > 4) {
            p0 -= 12;
            ++y0;
        }
        // first april
        int idx = (4 - p0) / c;
        if (idx > n) {
            return;
        }

        double dur = duration;

        for (int y = y0; idx <= n; ++y, idx += freq) {
            LocalDate easter = Easter.easter(y);
            // DAY_OF_MONTH is 1-based
            int day = easter.getDayOfMonth();
            int month = easter.getMonthValue();
            if (endPosition == -1) {
                --day;
            } else if (endPosition == 1) {
                if (day == 31) {
                    day = 1;
                    ++month;
                } else {
                    ++day;
                }
            }

            // MONTH is 1-based
            double m = 0, a = 0;
            if (meanCorrection == Correction.Simple) {
                if (month == 3 || day == 0) {
                    m = .5;
                    a = -.5;
                } else if (day >= duration) {
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
                if (meanCorrection == Correction.PreComputed) {
                    m_av = MEANS_MAR[25 - duration]; //(21 + dur) / 70.0;
                    a_av = MEANS_APR[25 - duration];//(49 - dur) / 70.0;
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
                    m_av = (15 + dur) / (2 * Easter.LUNARY);
                    a_av = 1 - m_av;
                }
                if (month == 3) {
                    m = 1 - m_av;
                    a = -a_av;
                } else if (day >= duration) {
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
    public EasterVariable rename(String nname) {
        return new EasterVariable(duration, endPosition, meanCorrection, nname);
    }

    @Override
    public boolean equals(Object other){
        if (this == other)
            return true;
        if (other instanceof EasterVariable){
            EasterVariable x=(EasterVariable) other;
            return x.duration==duration && x.endPosition==endPosition 
                    && Objects.equals(x.meanCorrection, meanCorrection);
         }else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.duration;
        hash = 53 * hash + this.endPosition;
        hash = 53 * hash + Objects.hashCode(this.meanCorrection);
        return hash;
    }

}
