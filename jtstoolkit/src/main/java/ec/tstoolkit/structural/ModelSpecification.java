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
package ec.tstoolkit.structural;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelSpecification implements IProcSpecification, Cloneable {

    public static final String LUSE = "luse", SUSE = "suse", NUSE = "nuse", SEASMODEL = "seasmodel", CUSE = "cuse", CDUMP = "cdump", CLENGTH = "clength";

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

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (lUse != ComponentUse.Unused || verbose) {
            info.set(LUSE, lUse.name());
        }
        if (sUse != ComponentUse.Unused || verbose) {
            info.set(SUSE, sUse.name());
        }
        if (cUse != ComponentUse.Unused || verbose) {
            info.set(CUSE, cUse.name());
        }
        if (nUse != ComponentUse.Unused || verbose) {
            info.set(NUSE, nUse.name());
        }
        if (seasModel != SeasonalModel.Unused || verbose) {
            info.set(SEASMODEL, seasModel.name());
        }
        if (cUse != ComponentUse.Unused) {
            if (!Parameter.isDefault(cdump)) {
                info.set(CDUMP, cdump);
            }
            if (!Parameter.isDefault(clength)) {
                info.set(CLENGTH, clength);
            }
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        String s = info.get(LUSE, String.class);
        if (s != null) {
            lUse = ComponentUse.valueOf(s);
        }
        s = info.get(SUSE, String.class);
        if (s != null) {
            sUse = ComponentUse.valueOf(s);
        }
        s = info.get(CUSE, String.class);
        if (s != null) {
            cUse = ComponentUse.valueOf(s);
        }
        s = info.get(NUSE, String.class);
        if (s != null) {
            nUse = ComponentUse.valueOf(s);
        }
        s = info.get(SEASMODEL, String.class);
        if (s != null) {
            seasModel = SeasonalModel.valueOf(s);
        }
        if (cUse != ComponentUse.Unused) {
            Parameter p = info.get(CDUMP, Parameter.class);
            if (p != null) {
                cdump = p;
            }
            p = info.get(CLENGTH, Parameter.class);
            if (p != null) {
                clength = p;
            }
        }
        return true;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, LUSE), String.class);
        dic.put(InformationSet.item(prefix, SUSE), String.class);
        dic.put(InformationSet.item(prefix, NUSE), String.class);
        dic.put(InformationSet.item(prefix, CUSE), String.class);
        dic.put(InformationSet.item(prefix, CDUMP), Parameter.class);
        dic.put(InformationSet.item(prefix, CLENGTH), Parameter.class);
        dic.put(InformationSet.item(prefix, SEASMODEL), String.class);
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

    private boolean equals(ModelSpecification spec) {
        return spec.lUse == lUse && spec.sUse == sUse && spec.nUse == nUse && spec.cUse == cUse
                && spec.seasModel == seasModel && Objects.deepEquals(spec.cdump, cdump)
                && Objects.deepEquals(spec.clength, clength);
    }

}
