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
package jdplus.modelling.regression;

import demetra.design.Algorithm;
import java.time.LocalDate;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author PALATEJ
 */
@Algorithm
@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
public interface MovingHolidayProvider {

    String identifier();

    /**
     * holidays contained in the given time range
     *
     * @param start Included
     * @param end Excluded
     * @return
     */
    LocalDate[] holidays(LocalDate start, LocalDate end);
}
