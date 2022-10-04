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
package demetra.sa.advanced;

import demetra.modelling.TransformationType;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true,  builderClassName="Builder")
public class PreprocessingSpec  {

    public static enum Method{
        NONE, TRAMO, REGARIMA;
    }
    
    public static final PreprocessingSpec TRAMO=tramo().build(), REGARIMA=regarima().build();
    
    public static Builder tramo(){
        return new Builder()
                .method(Method.TRAMO)
                .transform(TransformationType.Auto)
                .dtype(TradingDaysType.TD3)
                .ltype(LengthOfPeriodType.LeapYear)
                .easter(true)
                .pretest(true)
                .ao(true)
                .ls(true);
    }
 
    public static Builder regarima(){
        return new Builder()
                .method(Method.REGARIMA)
                .span(TimeSelector.all())
                .transform(TransformationType.Auto)
                .dtype(TradingDaysType.TD3)
                .ltype(LengthOfPeriodType.LeapYear)
                .easter(true)
                .pretest(true)
                .ao(true)
                .ls(true);
    }

    public Method method;
    public TimeSelector span;
    public TransformationType transform;
    public TradingDaysType dtype;
    public LengthOfPeriodType ltype;
    public boolean easter;
    public boolean pretest;
    public boolean ao;
    public boolean ls;
    public boolean tc;
    public boolean so;

}
