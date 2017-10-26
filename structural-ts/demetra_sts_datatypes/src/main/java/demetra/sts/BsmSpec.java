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

import java.util.Map;
import java.util.Objects;
import demetra.data.Parameter;
import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Data
public final class BsmSpec implements Cloneable {

    @lombok.NonNull
    ComponentUse levelUse = ComponentUse.Free,
            slopeUse = ComponentUse.Free,
            cycleUse = ComponentUse.Unused,
            noiseUse = ComponentUse.Free;
    @lombok.NonNull
    SeasonalModel seasonalModel = SeasonalModel.Crude;
    double cycleDumpingFactor, cycleLength;

    @Override
    public BsmSpec clone() {
        try {
            return (BsmSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param cmp
     */
    public void fixComponent(Component cmp) {
        switch (cmp) {
            case Level:
                levelUse = ComponentUse.Fixed;
                return;
            case Slope:
                slopeUse = ComponentUse.Fixed;
                return;
            case Seasonal:
                seasonalModel = SeasonalModel.Fixed;
                return;
            case Cycle:
                cycleUse = ComponentUse.Fixed;
                return;
            case Noise:
                noiseUse = ComponentUse.Fixed;
        }
    }

    /**
     *
     * @return
     */
    public ComponentUse getCycleUse() {
        return cycleUse;
    }

    /**
     *
     * @return
     */
    public ComponentUse getLevelUse() {
        return levelUse;
    }

    /**
     *
     * @return
     */
    public ComponentUse getNoiseUse() {
        return noiseUse;
    }

    /**
     *
     * @return
     */
    public SeasonalModel getSeasonalModel() {
        return seasonalModel;
    }

    /**
     *
     * @return
     */
    public ComponentUse getSeasUse() {
        if (seasonalModel == SeasonalModel.Unused) {
            return ComponentUse.Unused;
        } else if (seasonalModel == SeasonalModel.Fixed) {
            return ComponentUse.Fixed;
        } else {
            return ComponentUse.Free;
        }
    }

    /**
     *
     * @return
     */
    public ComponentUse getSlopeUse() {
        return slopeUse;
    }
    
    public boolean hasComponent(Component cmp){
        switch (cmp){
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

    public boolean hasFreeComponent(Component cmp){
        switch (cmp){
            case Noise:
                return noiseUse == ComponentUse.Free;
            case Level:
                return levelUse == ComponentUse.Free;
            case Cycle:
                return cycleUse == ComponentUse.Free;
            case Slope:
                return slopeUse == ComponentUse.Free;
            case Seasonal:
                 return seasonalModel != SeasonalModel.Fixed && seasonalModel != SeasonalModel.Unused;
           default:
                return false;
        }
    }
    /**
     *
     * @return
     */
    public boolean hasCycle() {
        return cycleUse != ComponentUse.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasLevel() {
        return levelUse != ComponentUse.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasNoise() {
        return noiseUse != ComponentUse.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasSeasonal() {
        return seasonalModel != SeasonalModel.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasSlope() {
        return slopeUse != ComponentUse.Unused;
    }

    /**
     * @param value
     */
    public void setCycleUse(ComponentUse value) {
        cycleUse = value;
        if (cycleUse != ComponentUse.Unused) {
            cycleDumpingFactor=0;
            cycleLength=0;
        }
    }

    /**
     *
     * @param value
     */
    public void setLevelUse(ComponentUse value) {
        levelUse = value;
        if (value == ComponentUse.Unused) {
            slopeUse = ComponentUse.Unused;
        }
    }

    public int getParametersCount() {
        int n = 0;
        if (levelUse == ComponentUse.Free) {
            ++n;
        }
        if (slopeUse == ComponentUse.Free) {
            ++n;
        }
        if (noiseUse == ComponentUse.Free) {
            ++n;
        }
        if (seasonalModel != SeasonalModel.Fixed && seasonalModel != SeasonalModel.Unused) {
            ++n;
        }
        n += getCycleParametersCount();
        return n;
    }

    public int getCycleParametersCount() {
        if (cycleUse == ComponentUse.Unused) {
            return 0;
        }
        int n = cycleUse == ComponentUse.Fixed ? 0 : 1;

        if (cycleDumpingFactor != 0) {
            ++n;
        }
        if (cycleLength  != 0) {
            ++n;
        }
        return n;
    }
    
    
}
