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

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import nbbrd.design.Development;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class Ramp implements ISystemVariable, IUserVariable {

    @Override
    public int dim() {
        return 1;
    }

    @lombok.NonNull
    private LocalDateTime start, end;

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        int period = 0;
        if (context instanceof TsDomain dom) {
            period = dom.getAnnualFrequency();
        }
        StringBuilder builder = new StringBuilder();
        if (period <= 0) {
            builder.append("ramp ").append(start.format(DateTimeFormatter.ISO_DATE))
                    .append(" / ").append(end.format(DateTimeFormatter.ISO_DATE));
        } else {
            TsUnit unit = TsUnit.ofAnnualFrequency(period);
            TsPeriod pstart = TsPeriod.of(unit, start);
            TsPeriod pend = TsPeriod.of(unit, end);
            builder.append("ramp ").append(pstart.display())
                    .append(" / ").append(pend.display());

        }
        return builder.toString();
    }

}
