/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.timeseries.regression;

import nbbrd.design.Development;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class Window implements ModifiedTsVariable.Modifier {

    @lombok.NonNull
    private final LocalDate start, end;

    @Override
    public int redim(int dim) {
        return dim;
    }

    @Override
    public int dim() {
        return 1;
    }

    @Override
    public String description() {
        StringBuilder builder=new StringBuilder();
        return builder.append('[').append(start).append(';').append(end).append(']').toString();
     }

    @Override
    public String description(int idx) {
        return description();
    }

}
