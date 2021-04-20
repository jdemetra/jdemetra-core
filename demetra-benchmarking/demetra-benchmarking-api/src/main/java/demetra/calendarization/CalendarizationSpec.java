/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.calendarization;

import nbbrd.design.Development;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.timeseries.TsUnit;
import demetra.util.Validatable;
import java.time.LocalDate;

/**
 * Specification containing parameters for the Calendarization process
 *
 * @author Mats Maggi
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder(toBuilder=true,  buildMethodName="buildWithoutValidation")
public class CalendarizationSpec implements ProcSpecification, Validatable<CalendarizationSpec> {

    public static final String FAMILY = "Benchmarking";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Calendarization", null);
    
    /**
     * Weights of each week day, starting from Monday
     */
    private double[] dailyWeights;
    private boolean stdev;
    private TsUnit aggregationUnit;
    private LocalDate start, end;

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public CalendarizationSpec validate() throws IllegalArgumentException {
        if (start != null && end != null && !start.isBefore(end))
            throw new IllegalArgumentException("Invalide span");
        return this;
    }
    
    public static class Builder implements Validatable.Builder<CalendarizationSpec>{
          
    }
    
    public static Builder builder(){
        return new Builder()
                .stdev(true)
                .aggregationUnit(TsUnit.UNDEFINED);
    }

}
