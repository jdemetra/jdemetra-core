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

import nbbrd.design.Development;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
@Development(status=Development.Status.Release)
public class PeriodicContrasts implements IUserTsVariable {
    private int period;
    private LocalDateTime reference;
    
    public PeriodicContrasts(int period){
        this.period=period;
        this.reference=TsPeriod.DEFAULT_EPOCH;
    }

    @Override
    public int dim() {
        return period-1;
    }
    
}
