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
public class ModelSpecification implements Cloneable {

    ComponentUse lUse, sUse, cUse, nUse;
    SeasonalModel seasModel;
    Parameter cdump, clength;

    /**
     *
     */
    public ModelSpecification() {
        lUse = ComponentUse.Free;
        sUse = ComponentUse.Free;
        cUse = ComponentUse.Unused;
        nUse = ComponentUse.Free;
        seasModel = SeasonalModel.Trigonometric;
    }

    @Override
    public ModelSpecification clone() {
        try {
            ModelSpecification clone = (ModelSpecification) super.clone();
            if (cdump != null) {
                clone.cdump = cdump.clone();
            }
            if (clength != null) {
                clone.clength = clength.clone();
            }
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public Parameter getCyclicalDumpingFactor() {
        return cdump;
    }

    public Parameter getCyclicalPeriod() {
        return clength;
    }

    public void setCyclicalDumpingFactor(Parameter value) {
        cdump = value;
    }

    public void setCyclicalPeriod(Parameter value) {
        clength = value;
    }

    /**
     *
     * @param cmp
     */
    public void fixComponent(Component cmp) {
        switch (cmp) {
            case Level:
                lUse = ComponentUse.Fixed;
                return;
            case Slope:
                sUse = ComponentUse.Fixed;
                return;
            case Seasonal:
                seasModel = SeasonalModel.Fixed;
                return;
            case Cycle:
                cUse = ComponentUse.Fixed;
                return;
            case Noise:
                nUse = ComponentUse.Fixed;
        }
    }

    /**
     *
     * @return
     */
    public ComponentUse getCycleUse() {
        return cUse;
    }

    /**
     *
     * @return
     */
    public ComponentUse getLevelUse() {
        return lUse;
    }

    /**
     *
     * @return
     */
    public ComponentUse getNoiseUse() {
        return nUse;
    }

    /**
     *
     * @return
     */
    public SeasonalModel getSeasonalModel() {
        return seasModel;
    }

    /**
     *
     * @return
     */
    public ComponentUse getSeasUse() {
        if (seasModel == SeasonalModel.Unused) {
            return ComponentUse.Unused;
        } else if (seasModel == SeasonalModel.Fixed) {
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
        return sUse;
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
                return nUse == ComponentUse.Free;
            case Level:
                return lUse == ComponentUse.Free;
            case Cycle:
                return cUse == ComponentUse.Free;
            case Slope:
                return sUse == ComponentUse.Free;
            case Seasonal:
                 return seasModel != SeasonalModel.Fixed && seasModel != SeasonalModel.Unused;
           default:
                return false;
        }
    }
    /**
     *
     * @return
     */
    public boolean hasCycle() {
        return cUse != ComponentUse.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasLevel() {
        return lUse != ComponentUse.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasNoise() {
        return nUse != ComponentUse.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasSeasonal() {
        return seasModel != SeasonalModel.Unused;
    }

    /**
     *
     * @return
     */
    public boolean hasSlope() {
        return sUse != ComponentUse.Unused;
    }

    /**
     *
     * @param value
     */
    public void setSeasonalModel(SeasonalModel value) {
        if (value != seasModel) {
            seasModel = value;
        }
    }

    /**
     * @param value
     */
    public void useCycle(ComponentUse value) {
        cUse = value;
        if (cUse != ComponentUse.Unused) {
            if (cdump == null) {
                cdump = new Parameter();
            }
            if (clength == null) {
                clength = new Parameter();
            }
        }
    }

    /**
     *
     * @param value
     */
    public void useLevel(ComponentUse value) {
        lUse = value;
        if (value == ComponentUse.Unused) {
            sUse = ComponentUse.Unused;
        }
    }

    /**
     *
     * @param value
     */
    public void useNoise(ComponentUse value) {
        nUse = value;
    }

    /**
     * Sets the slope. Be sure to set the level use before calling this method.
     *
     * @param value
     */
    public void useSlope(ComponentUse value) {
        if (value != ComponentUse.Unused && lUse == ComponentUse.Unused) {
            return;
        }
        sUse = value;
    }

    public int getParametersCount() {
        int n = 0;
        if (lUse == ComponentUse.Free) {
            ++n;
        }
        if (sUse == ComponentUse.Free) {
            ++n;
        }
        if (nUse == ComponentUse.Free) {
            ++n;
        }
        if (seasModel != SeasonalModel.Fixed && seasModel != SeasonalModel.Unused) {
            ++n;
        }
        n += getCycleParametersCount();
        return n;
    }

    public int getCycleParametersCount() {
        if (cUse == ComponentUse.Unused) {
            return 0;
        }
        int n = cUse == ComponentUse.Fixed ? 0 : 1;

        if (cdump == null || !cdump.isFixed()) {
            ++n;
        }
        if (clength == null || !clength.isFixed()) {
            ++n;
        }
        return n;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ModelSpecification && equals((ModelSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.lUse);
        hash = 61 * hash + Objects.hashCode(this.sUse);
        hash = 61 * hash + Objects.hashCode(this.cUse);
        hash = 61 * hash + Objects.hashCode(this.nUse);
        hash = 61 * hash + Objects.hashCode(this.seasModel);
        return hash;
    }

    public boolean equals(ModelSpecification spec) {
        return spec.lUse == lUse && spec.sUse == sUse && spec.nUse == nUse && spec.cUse == cUse
                && spec.seasModel == seasModel && Objects.deepEquals(spec.cdump, cdump)
                && Objects.deepEquals(spec.clength, clength);
    }

}
