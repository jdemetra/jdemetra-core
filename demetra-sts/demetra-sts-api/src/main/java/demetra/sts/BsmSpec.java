/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package demetra.sts;

import demetra.data.Parameter;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BsmSpec {
    
    public static final BsmSpec DEFAULT = BsmSpec.builder().build();

    public static Builder builder() {
        return new Builder();
    }

    private Parameter levelVar, slopeVar, noiseVar, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength;
    private SeasonalModel seasonalModel;

    public static class Builder {

        private Parameter levelVar = Parameter.undefined(), slopeVar = Parameter.undefined(),
                noiseVar = Parameter.undefined(), seasonalVar = Parameter.undefined(), cycleVar, cycleDumpingFactor, cycleLength;
        private SeasonalModel seasonalModel = SeasonalModel.Trigonometric;

        public Builder level(boolean level, boolean slope) {
            if (slope && !level) {
                throw new IllegalArgumentException();
            }
            if (level) {
                levelVar = Parameter.undefined();
            } else {
                levelVar = null;
            }
            if (slope) {
                slopeVar = Parameter.undefined();
            } else {
                slopeVar = null;
            }
            return this;
        }

        public Builder level(Parameter lvar, Parameter svar) {
            if (lvar == null && lvar != null) {
                throw new IllegalArgumentException();
            }
            levelVar = lvar;
            slopeVar = svar;
            return this;
        }

        public Builder seasonal(SeasonalModel model) {
            this.seasonalModel = model;
            if (model != null) {
                this.seasonalVar = Parameter.undefined();
            } else {
                this.seasonalVar = null;
            }
            return this;
        }

        public Builder seasonal(SeasonalModel model, Parameter svar) {
            if ((model == null && svar != null) || (model != null && svar == null)) {
                throw new IllegalArgumentException();
            }
            this.seasonalModel = model;
            this.seasonalVar = svar;
            return this;
        }

        public Builder noise(boolean n) {
            if (n) {
                this.noiseVar = Parameter.undefined();
            } else {
                this.noiseVar = null;
            }
            return this;
        }

        public Builder noise(Parameter nvar) {
            if (nvar != null && nvar.isFixed() && nvar.getValue() == 0) {
                this.noiseVar = null;
            } else {
                this.noiseVar = nvar;
            }
            return this;
        }

        public Builder cycle(boolean cycle) {
            if (cycle) {
                this.cycleVar = Parameter.undefined();
                this.cycleDumpingFactor = Parameter.undefined();
                this.cycleLength = Parameter.undefined();
            } else {
                this.cycleVar = null;
                this.cycleDumpingFactor = null;
                this.cycleLength = null;
            }
            return this;
        }

        public Builder cycle(Parameter cvar, Parameter cdumpingfactor, Parameter clength) {
            if (cvar == null && (cdumpingfactor != null || clength != null)) {
                throw new IllegalArgumentException();
            }
            cycleVar = cvar;
            if (cdumpingfactor != null && cdumpingfactor.getValue() <= 0) {
                cycleDumpingFactor = Parameter.undefined();
            } else {
                cycleDumpingFactor = cdumpingfactor;
            }
            if (clength != null && clength.getValue() <= 0) {
                this.cycleLength = Parameter.undefined();
            } else {
                cycleLength = clength;
            }
            return this;
        }

        public BsmSpec build() {
            return new BsmSpec(this);
        }
    }

    private BsmSpec(Builder builder) {
        this.levelVar = builder.levelVar;
        this.slopeVar = builder.slopeVar;
        this.noiseVar = builder.noiseVar;
        this.seasonalVar = builder.seasonalVar;
        this.seasonalModel = builder.seasonalModel;
        this.cycleVar = builder.cycleVar;
        this.cycleDumpingFactor = builder.cycleDumpingFactor;
        this.cycleLength = builder.cycleLength;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.levelVar = levelVar;
        builder.slopeVar = slopeVar;
        builder.noiseVar = noiseVar;
        builder.seasonalVar = seasonalVar;
        builder.seasonalModel = seasonalModel;
        builder.cycleVar = cycleVar;
        builder.cycleDumpingFactor = cycleDumpingFactor;
        builder.cycleLength = cycleLength;
        return builder;
    }

    /**
     *
     * @param var
     * @param cmp
     * @return
     */
    public BsmSpec fixComponent(Component cmp, double var) {
        switch (cmp) {
            case Level:
                return new BsmSpec(Parameter.fixed(var), slopeVar, noiseVar, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
            case Slope:
                return new BsmSpec(levelVar, Parameter.fixed(var), noiseVar, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
            case Seasonal:
                if (var == 0) {
                    return new BsmSpec(levelVar, slopeVar, noiseVar, Parameter.fixed(0), cycleVar, cycleDumpingFactor, cycleLength, SeasonalModel.Fixed);
                } else {
                    return new BsmSpec(levelVar, slopeVar, noiseVar, Parameter.fixed(var), cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
                }
            case Cycle:
                return new BsmSpec(levelVar, slopeVar, noiseVar, seasonalVar, Parameter.fixed(var), cycleDumpingFactor, cycleLength, seasonalModel);
            default: // Noise
                if (var == 0) {
                    return new BsmSpec(levelVar, slopeVar, null, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
                } else {
                    return new BsmSpec(levelVar, slopeVar, Parameter.fixed(var), seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
                }
        }
    }
    
    private boolean isScalable(Parameter p){
        return p == null || p.getValue() == 0 || p.isFree();
    }
    
    public boolean isScalable(){
        return isScalable(levelVar) && isScalable(slopeVar) && isScalable(seasonalVar) && isScalable(noiseVar) && isScalable(cycleVar);
    }

    private ComponentUse use(Parameter p) {
        if (p == null) {
            return ComponentUse.Unused;
        } else if (p.isFixed()) {
            return ComponentUse.Fixed;
        } else {
            return ComponentUse.Free;
        }
    }

    private boolean isFree(Parameter p) {
        if (p == null) {
            return false;
        } else {
            return !p.isFixed();
        }
    }

    /**
     *
     * @return
     */
    public ComponentUse getSlopeUse() {
        return use(slopeVar);
    }

    public boolean hasComponent(Component cmp) {
        switch (cmp) {
            case Noise:
                return hasNoise();
            case Level:
                return hasLevel();
            case Cycle:
                return hasCycle();
            case Slope:
                return hasSlope();
            case Seasonal:
                return hasSeasonal();
            default:
                return false;
        }
    }

    public boolean hasFreeComponent(Component cmp) {
        switch (cmp) {
            case Noise:
                return isFree(noiseVar);
            case Level:
                return isFree(levelVar);
            case Slope:
                return isFree(slopeVar);
            case Seasonal:
                return isFree(seasonalVar);
            case Cycle:
                return isFree(cycleVar);
            default:
                return false;
        }
    }

    /**
     *
     * @return
     */
    public boolean hasCycle() {
        return cycleVar != null;
    }

    /**
     *
     * @return
     */
    public boolean hasLevel() {
        return levelVar != null;
    }

    /**
     *
     * @return
     */
    public boolean hasNoise() {
        return noiseVar != null;
    }

    /**
     *
     * @return
     */
    public boolean hasSeasonal() {
        return seasonalVar != null;
    }

    /**
     *
     * @return
     */
    public boolean hasSlope() {
        return slopeVar != null;
    }

//    /**
//     * @param value
//     * @return
//     */
//    public BsmSpec setCycleUse(ComponentUse value) {
//        switch (value) {
//            case Unused:
//                return new BsmSpec(levelVar, slopeVar, noiseVar, seasonalVar, null, null, null, seasonalModel);
//            case Fixed:
//                return new BsmSpec(levelVar, slopeVar, noiseVar, seasonalVar,
//                        Parameter.fixed(cycleVar.getValue()), Parameter.fixed(cycleDumpingFactor.getValue()), Parameter.fixed(cycleLength.getValue()),
//                        seasonalModel);
//            default: // Free
//                return new BsmSpec(levelVar, slopeVar, noiseVar, seasonalVar, Parameter.undefined(), Parameter.undefined(), Parameter.undefined(), seasonalModel);
//        }
//    }
//
//    /**
//     *
//     * @param value
//     * @return
//     */
//    public BsmSpec setLevelUse(ComponentUse value) {
//        switch (value) {
//            case Unused:
//                return new BsmSpec(null, null, noiseVar, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
//            case Fixed:
//                return new BsmSpec(Parameter.fixed(levelVar.getValue()), slopeVar, noiseVar, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
//            default:
//                return new BsmSpec(Parameter.undefined(), slopeVar, noiseVar, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
//        }
//    }
//
//    /**
//     *
//     * @param value
//     * @return
//     */
//    public BsmSpec setNoiseUse(ComponentUse value) {
//        switch (value) {
//            case Unused:
//                return new BsmSpec(levelVar, slopeVar, null, seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
//            case Fixed:
//                double nvar = noiseVar.getValue();
//                return new BsmSpec(levelVar, slopeVar, nvar == 0 ? null : Parameter.fixed(nvar),
//                        seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
//            default:
//                return new BsmSpec(levelVar, slopeVar, Parameter.undefined(), seasonalVar, cycleVar, cycleDumpingFactor, cycleLength, seasonalModel);
//        }
//    }

    public int getFreeVariancesCount() {
        int n = 0;
        if (isFree(levelVar)) {
            ++n;
        }
        if (isFree(slopeVar)) {
            ++n;
        }
        if (isFree(noiseVar)) {
            ++n;
        }
        if (isFree(seasonalVar)) {
            ++n;
        }
        if (isFree(cycleVar)) {
            ++n;
        }
        return n;
    }

    public int getFreeParametersCount() {
        return getFreeVariancesCount() + getFreeCycleParametersCount();
    }

    public int getFreeCycleParametersCount() {
        int n = 0;
        if (isFree(cycleDumpingFactor)) {
            ++n;
        }
        if (isFree(cycleLength)) {
            ++n;
        }
        return n;
    }

    public double[] cycle() {
        if (cycleDumpingFactor == null || cycleLength == null) {
            return null;
        }
        double cro = cycleDumpingFactor.getValue();
        double cperiod = cycleLength.getValue();
        double q = Math.PI * 2 / cperiod;
        return new double[]{cro * Math.cos(q), cro * Math.sin(q)};
    }

    public static double valueOf(Parameter p) {
        return p == null ? -1 : p.getValue();
    }

}
