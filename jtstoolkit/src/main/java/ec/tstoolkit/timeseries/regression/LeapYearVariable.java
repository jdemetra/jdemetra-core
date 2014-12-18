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
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.Utilities;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LeapYearVariable extends AbstractSingleTsVariable implements ILengthOfPeriodVariable{

    private LengthOfPeriodType ltype_ = LengthOfPeriodType.None;

    public LeapYearVariable(LengthOfPeriodType type){
        ltype_=type;
    }

    public LengthOfPeriodType getType() {
        return ltype_;
    }
    
    public void setType(LengthOfPeriodType ltype) {
        ltype_=ltype;
    }
    
     @Override
    public void data(TsPeriod start, DataBlock data) {
        switch (ltype_) {
            case LeapYear:
                Utilities.leapYear(start, data);
                break;
            case LengthOfPeriod:
                Utilities.lengthofPeriod(start, data);
                break;
        }
    }

    @Override
    public String getDescription() {
        switch (ltype_) {
            case LeapYear:
                return "Leap year";
            case LengthOfPeriod:
                return "Length of period";
            default:
                return null;
        }
     }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return true;
    }
 }
