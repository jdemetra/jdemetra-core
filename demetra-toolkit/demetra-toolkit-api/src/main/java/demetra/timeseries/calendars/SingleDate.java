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
package demetra.timeseries.calendars;

import nbbrd.design.Development;
import demetra.timeseries.ValidityPeriod;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class SingleDate implements Holiday {

    private LocalDate date;
    private double weight;
   

     public SingleDate(LocalDate date, double weight) {
        this.date=date;
        this.weight=weight;
    }

 
    @Override
    public SingleDate reweight(double nweight) {
        if (weight == this.weight) {
            return this;
        }
        return new SingleDate(date, nweight);
    }
    
    @Override
    public String display(){
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public ValidityPeriod getValidityPeriod() {
        return ValidityPeriod.ALWAYS;
    }

    @Override
    public Holiday forPeriod(LocalDate start, LocalDate end) {
        return this;
    }

}
