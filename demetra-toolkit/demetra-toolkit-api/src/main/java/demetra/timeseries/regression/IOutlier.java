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
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public interface IOutlier extends ITsVariable {
    
    @Override
    default int dim(){
        return 1;
    }
    
    @Override
    default <D extends TimeSeriesDomain<?>> String description(D context){
        String code=getCode();
        LocalDateTime pos = getPosition();
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(" (");
        if (context == null || !(context instanceof TsDomain)) {
            builder.append(pos);
        } else {
            TsPeriod p = ((TsDomain) context).get(0);
            p=p.withDate(pos);
            builder.append(p.display());
        }
        builder.append(')');
        return builder.toString();
    }

    public static <D extends TimeSeriesDomain<?>> String defaultName(String code, LocalDateTime pos, D context) {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(" (");
        if (context == null || !(context instanceof TsDomain)) {
            builder.append(pos);
        } else {
            TsPeriod p = ((TsDomain) context).get(0);
            p=p.withDate(pos);
            builder.append(p.start().toLocalDate().format(DateTimeFormatter.ISO_DATE));
        }
        builder.append(')');
        return builder.toString();
    }

    public static <D extends TimeSeriesDomain<?>> String defaultName(String code, TsPeriod pos) {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(" (");
             builder.append(pos.start().toLocalDate().format(DateTimeFormatter.ISO_DATE));
        builder.append(')');
        return builder.toString();
    }

    String getCode();

    LocalDateTime getPosition();
    
}
