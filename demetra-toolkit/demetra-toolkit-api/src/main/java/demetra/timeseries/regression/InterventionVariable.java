/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.timeseries.regression;

import demetra.data.Range;
import demetra.timeseries.TimeSeriesDomain;
import nbbrd.design.Development;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName="Builder", toBuilder=true)
public class InterventionVariable implements IUserVariable, ISystemVariable{
    
   
    private double delta, deltaSeasonal;
    @lombok.Singular
    private List<Range<LocalDateTime> >sequences;
    
    @Override
    public int dim()
    {return 1;}

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return "iv";
    }

    private static final InterventionVariable EMPTY=new Builder().build();
    public static InterventionVariable empty(){
        return EMPTY;
    }

    
}
