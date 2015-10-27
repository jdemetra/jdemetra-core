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
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class StockTradingDaysVariables implements ITradingDaysVariable, Cloneable {

    // Wth day of the month. 0-based! See documentation of X12 Arima, for instance
    private final int w_;

    /**
     * Creates a new set of StockTradingDays variables
     * @param w The wth day of the month is considered. When w is negative, 
     * the (-w) day before the end of the month is considered
     * 
     */
    public StockTradingDaysVariables(int w) {
        w_=w;
     }

    @Override
    public StockTradingDaysVariables clone(){
        try{
            StockTradingDaysVariables td=(StockTradingDaysVariables) super.clone();
            return td;
        } catch (CloneNotSupportedException err){
            throw new AssertionError();
        }
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return TsFrequency.Monthly;
    }

    @Override
    public String getDescription() {
        StringBuilder builder=new StringBuilder();
        builder.append("TD Stock [").append(w_).append(']');
        return builder.toString();
     }

    @Override
    public int getDim() {
        return 6;
    }

    @Override
    public String getItemDescription(int idx) {
        return DayOfWeek.valueOf(idx+1).toString();
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.getFrequency()==TsFrequency.Monthly;
    }

    @Override
    @Deprecated
    public void data(TsDomain domain, List<DataBlock> data, int start) {
        data(domain, data.subList(start, start+getDim()));
    }
    
    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        int n = domain.getLength();
        TsPeriod d0 = domain.getStart();
        int conv = 12 / d0.getFrequency().intValue();
        int[] begin = new int[n + 1];
        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
        month.set(d0.getYear(), d0.getPosition()*conv);
        month.move(conv - 1);
        for (int i = 0; i < begin.length; ++i) {
            // begin contains the first day of the last month of each period
            begin[i] = Day.calc(month.getYear(), month.getPosition(), 0);
            month.move(conv);
        }
        double[] z0 = new double[7];
        for (int j = 0; j < n; j++) {
            java.util.Arrays.fill(z0, 0.0d);
            //
            // z0[0] = Sunday
            //
            int dayofweek = (begin[j] - 3) % 7;
            int monthlen = begin[j + 1] - begin[j];
            if (dayofweek < 0) {
                dayofweek += 7;
            }
            // 
            // w_ (like in Tramo ??) could be negative. (if we want to stock on the -w_ day before month-end
            // Example : 
            // w_ = -2 
            // 
            // Jan 29
            // Feb 26 or 27 (if LY)
            // .............
            // Apr 28
            // .............
            //
            if (this.w_ >= 0) {
                if (this.w_ < monthlen) {
                    monthlen = this.w_;
                }
            } else {
                monthlen += this.w_;
                if (monthlen <= 0) {
                    monthlen = 1;
                }
            }
            int Lastdayofweek = (dayofweek + (monthlen-1)) % 7;
            z0[Lastdayofweek] = 1.0d;
            for (int i = 0; i < 6; ++i) {
                DataBlock x = data.get(i);
                x.set(j, z0[i + 1] - z0[0]);
            }
        }
    }
}
        

