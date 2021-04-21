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
import nbbrd.design.BuilderPattern;
import demetra.timeseries.TsException;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class EasterVariable implements IEasterVariable, ISystemVariable{
    
    public static enum Correction {
        None,
        Simple,
        PreComputed,
        Theoretical
    }
    
    public static Builder builder(){
        return new Builder();
    }

    @BuilderPattern(EasterVariable.class)
    public static class Builder {

        private int duration = 6, endPosition = 0;
        private Correction meanCorrection = Correction.Simple;
        private String name;

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder meanCorrection(Correction correction) {
            this.meanCorrection = correction;
            return this;
        }

        /**
         * Position of the end of the Easter effect, relatively to Easter
         *
         * @param endpos
         * @return
         */
        public Builder endPosition(int endpos) {
            if (endpos < -1 || endpos > 1) {
                throw new TsException("Not supported yet");
            }
            this.endPosition = endpos;
            return this;
        }

        public EasterVariable build() {
            return new EasterVariable(duration, endPosition, meanCorrection);
        }
    }


    private final int duration, endPosition;
    private final Correction meanCorrection;
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return "easter";
    }
}
