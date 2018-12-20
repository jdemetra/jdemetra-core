/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.design.BuilderPattern;
import demetra.timeseries.TsException;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class EasterVariable implements IEasterVariable{
    
    public static enum Correction {
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
    
}
