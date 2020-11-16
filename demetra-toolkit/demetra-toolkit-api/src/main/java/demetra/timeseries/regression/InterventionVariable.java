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
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import demetra.timeseries.TsException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
@lombok.Builder
public class InterventionVariable implements ISystemVariable{
    
    public static Builder builder() {
        return new Builder();
    }
    
    @BuilderPattern(InterventionVariable.class)
    public static class Builder {
        
        private double delta, deltaSeasonal;
        private final List<Range<LocalDateTime>> sequences = new ArrayList<>();
        private Double coefficient;
        
        public Builder coefficient(double cfixed) {
            this.coefficient = cfixed;
            return this;
        }
        
        public Builder delta(double delta) {
            this.delta = delta;
            return this;
        }
        
        public Builder deltaSeasonal(double delta) {
            this.deltaSeasonal = delta;
            return this;
        }
        
        public Builder add(LocalDateTime start, LocalDateTime end) {
            this.sequences.add( Range.of(start, end));
            return this;
        }
        
        public InterventionVariable build() {
            if (sequences.isEmpty()) {
                throw new TsException(TsException.INVALID_DEFINITION);
            }
            return new InterventionVariable(delta, deltaSeasonal,
                    sequences.toArray(new Range[sequences.size()]), coefficient);
        }
    }
    
    private double delta, deltaSeasonal;
    private Range<LocalDateTime>[] sequences;
    private Double coefficient;
    
    @Override
    public int dim()
    {return 1;}

    public boolean isFixed() {
        return coefficient != null;
    }
}
