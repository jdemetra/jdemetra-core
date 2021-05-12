/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
 /*
 */
package jdplus.sts;

import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.SeasonalModel;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class BsmData {

    int period;
    // LLT
    double levelVar, slopeVar;
    //SEAS
    double seasonalVar;
    SeasonalModel seasonalModel;
    //NOISE
    double noiseVar;
    // CYCLE
    double cycleVar, cycleDumpingFactor, cycleLength;

    public BsmData(BsmSpec spec, int period) {
        this.period = period;
        this.levelVar = BsmSpec.valueOf(spec.getLevelVar(), BsmSpec.DEF_VAR);
        this.slopeVar = BsmSpec.valueOf(spec.getSlopeVar(), BsmSpec.DEF_VAR);
        this.seasonalVar = BsmSpec.valueOf(spec.getSeasonalVar(), BsmSpec.DEF_VAR);
        this.seasonalModel = spec.getSeasonalModel();
        this.noiseVar = BsmSpec.valueOf(spec.getNoiseVar(), BsmSpec.DEF_VAR);
        this.cycleVar = BsmSpec.valueOf(spec.getCycleVar(), BsmSpec.DEF_VAR);
        this.cycleDumpingFactor = BsmSpec.valueOf(spec.getCycleDumpingFactor(), BsmSpec.DEF_CDUMP);
        this.cycleLength = BsmSpec.valueOf(spec.getCycleLength(), BsmSpec.DEF_CLENGTH);
    }
    
    @lombok.Value
    public static class ComponentVariance{
        Component component;
        double variance;
    }

    public ComponentVariance maxVariance() {
        double max = 0;
        Component cmp=Component.Undefined;
        if (levelVar > max) {
            max = levelVar;
            cmp=Component.Level;
        }
        if (slopeVar > max) {
            max = slopeVar;
            cmp=Component.Slope;
        }
        if (seasonalVar > max) {
            max = seasonalVar;
            cmp=Component.Seasonal;
        }
        if (noiseVar > max) {
            max = noiseVar;
            cmp=Component.Noise;
        }
        if (cycleVar > max) {
            max = cycleVar;
            cmp=Component.Cycle;
        }
        return new ComponentVariance(cmp, max);
    }

    public BsmData scaleVariances(double factor) {
        return new BsmData(period,
                levelVar > 0 ? levelVar * factor : levelVar,
                slopeVar > 0 ? slopeVar * factor : slopeVar,
                seasonalVar > 0 ? seasonalVar * factor : seasonalVar, seasonalModel,
                noiseVar > 0 ? noiseVar * factor : noiseVar,
                cycleVar > 0 ? cycleVar * factor : cycleVar, cycleDumpingFactor, cycleLength);
    }

//    private static ComponentUse of(double var) {
//        return var < 0 ? ComponentUse.Unused : ComponentUse.Free;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public BsmSpec specification() {
//        BsmSpec spec = new BsmSpec();
//        if (lFixed) {
//            spec.fixComponent(lVar, Component.Level);
//        } else {
//            spec.setLevelUse(of(lVar));
//        }
//        if (sFixed) {
//            spec.fixComponent(sVar, Component.Slope);
//        } else {
//            spec.setSlopeUse(of(sVar));
//        }
//        if (nFixed) {
//            spec.fixComponent(nVar, Component.Noise);
//        } else {
//            spec.setNoiseUse(nVar > 0 ? ComponentUse.Free : ComponentUse.Unused);
//        }
//        if (cFixed) {
//            spec.fixComponent(cVar, Component.Cycle);
//            spec.setCycleDumpingFactor(cDump);
//            spec.setCycleLength(cPeriod);
//        } else {
//            spec.setCycleUse(of(cVar));
//        }
//        if (seasFixed) {
//            spec.fixComponent(seasVar, Component.Seasonal);
//        } else {
//            spec.setSeasUse(of(seasVar));
//            spec.setSeasonalModel(seasModel);
//        }
//        return spec;
//    }
//    private static void svar(int freq, Matrix O) {
//        int n = freq - 1;
//        Matrix H = Matrix.make(freq, n);
//        // should be improved
//        for (int i = 0; i < freq; ++i) {
//            double z = 2 * Math.PI * (i + 1) / freq;
//            for (int j = 0; j < n / 2; ++j) {
//                H.set(i, 2 * j, Math.cos((j + 1) * z));
//                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
//            }
//            if (n % 2 == 1) {
//                H.set(i, n - 1, Math.cos((freq / 2) * z));
//            }
//        }
//
//        SymmetricMatrix.XXt(H, O);
//    }
//    /**
//     *
//     */
//    final int period;
//    double lVar, sVar, seasVar, cVar, nVar;
//    boolean lFixed, sFixed, seasFixed, cFixed, nFixed;
//    double cDump, cPeriod;
//    double ccos, csin;
//    SeasonalModel seasModel;
//
//    /**
//     *
//     * @param spec
//     * @param period
//     */
//    public BsmData(BsmSpec spec, int period) {
//        this.period = period;
//        seasModel = spec.getSeasonalModel();
//        switch (spec.getNoiseUse()) {
//            case Free:
//                nVar = 1;
//                nFixed = false;
//                break;
//            case Fixed:
//                nFixed = true;
//                nVar = spec.getNoiseVar();
//                break;
//            case Unused:
//                nFixed = false;
//                nVar = -1;
//                break;
//        }
//        switch (spec.getCycleUse()) {
//            case Free:
//                cycle(.5, period * 2);
//                cVar = 1;
//                cFixed = false;
//                break;
//            case Fixed:
//                cFixed = true;
//                cycle(spec.getCycleDumpingFactor(), spec.getCycleLength());
//                cVar = spec.getCycleVar();
//                break;
//            case Unused:
//                cFixed = false;
//                cVar = -1;
//                break;
//        }
//        switch (spec.getLevelUse()) {
//            case Free:
//                lVar = 1;
//                lFixed = false;
//                break;
//            case Fixed:
//                lFixed = true;
//                lVar = spec.getLevelVar();
//                break;
//            case Unused:
//                lFixed = false;
//                lVar = -1;
//                break;
//        }
//        switch (spec.getSlopeUse()) {
//            case Free:
//                sVar = 1;
//                sFixed = false;
//                break;
//            case Fixed:
//                sFixed = true;
//                sVar = spec.getSlopeVar();
//                break;
//            case Unused:
//                sFixed = false;
//                sVar = -1;
//                break;
//        }
//        switch (spec.getSeasUse()) {
//            case Free:
//                seasVar = 1;
//                seasFixed = false;
//                break;
//            case Fixed:
//                seasFixed = true;
//                seasVar = spec.getSeasVar();
//                break;
//            case Unused:
//                seasFixed = false;
//                seasVar = -1;
//                break;
//        }
//    }
//    
//    @Override
//    public BsmData clone(){
//        try {
//            return (BsmData) super.clone();
//        } catch (CloneNotSupportedException ex) {
//            return null;
//        }
//    }
//
//    /**
//     *
//     * @param factor
//     * @return
//     */
//
//    /**
//     *
//     * @param cmp
//     * @param var
//     */
//    public void setVariance(Component cmp, double var) {
//        switch (cmp) {
//            case Noise:
//                nVar = var;
//                return;
//            case Cycle:
//                if (cVar >= 0) {
//                    cVar = var;
//                }
//                return;
//            case Level:
//                if (lVar >= 0) {
//                    lVar = var;
//                }
//                return;
//            case Slope:
//                if (sVar >= 0) {
//                    sVar = var;
//                }
//                return;
//            case Seasonal:
//                if (seasVar >= 0) {
//                    seasVar = var;
//                    if (var == 0) {
//                        seasModel = SeasonalModel.Fixed;
//                    }
//                }
//        }
//    }
//
//    /**
//     *
//     * @param val
//     * @return
//     */
//    public Component fixMaxVariance(double val) {
//        Component min = getMaxVariance();
//        if (min != Component.Undefined) {
//            double vmax = getVariance(min);
//            if (vmax != val) {
//                if (scaleVariances(val / vmax)) {
//                    return min;
//                }
//            } else {
//                return min;
//            }
//        }
//        return Component.Undefined;
//    }
//
//    /**
//     *
//     * @param eps
//     * @return
//     */
//    public boolean fixSmallVariance(double eps) {
//        Component min = getMinVariance();
//        if (min != Component.Undefined && getVariance(min) < eps) {
//            setVariance(min, 0);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     *
//     * @return
//     */
//    public Component getMaxVariance() {
//        Component cmp = Component.Undefined;
//        double vmax = 0;
//        if (lVar > vmax) {
//            vmax = lVar;
//            cmp = Component.Level;
//        }
//        if (sVar > vmax) {
//            vmax = sVar;
//            cmp = Component.Slope;
//        }
//        if (seasVar > vmax) {
//            vmax = seasVar;
//            cmp = Component.Seasonal;
//        }
//        if (cVar > vmax) {
//            vmax = cVar;
//            cmp = Component.Cycle;
//        }
//        if (nVar > vmax) {
//            cmp = Component.Noise;
//        }
//        return cmp;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public Component getMinVariance() {
//        Component cmp = Component.Undefined;
//        double vmin = Double.MAX_VALUE;
//        if (lVar > 0 && lVar < vmin) {
//            vmin = lVar;
//            cmp = Component.Level;
//        }
//        if (sVar > 0 && sVar < vmin) {
//            vmin = sVar;
//            cmp = Component.Slope;
//        }
//        if (seasVar > 0 && seasVar < vmin) {
//            vmin = seasVar;
//            cmp = Component.Seasonal;
//        }
//        if (cVar > 0 && cVar < vmin) {
//            vmin = cVar;
//            cmp = Component.Cycle;
//        }
//        if (nVar > 0 && nVar < vmin) {
//            cmp = Component.Noise;
//        }
//        return cmp;
//    }
//
//    public int getComponentsCount() {
//        int n = 0;
//        if (nVar > 0) {
//            ++n;
//        }
//        if (cVar >= 0) {
//            ++n;
//        }
//        if (lVar >= 0) {
//            ++n;
//            if (sVar >= 0) {
//                ++n;
//            }
//        }
//        if (seasVar >= 0) {
//            ++n;
//        }
//        return n;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public Component[] getComponents() {
//        Component[] cmp = new Component[getComponentsCount()];
//        int idx = 0;
//        if (nVar > 0) {
//            cmp[idx++] = Component.Noise;
//        }
//        if (cVar >= 0) {
//            cmp[idx++] = Component.Cycle;
//        }
//        if (lVar >= 0) {
//            cmp[idx++] = Component.Level;
//            if (sVar >= 0) {
//                cmp[idx++] = Component.Slope;
//            }
//        }
//        if (seasVar >= 0) {
//            cmp[idx] = Component.Seasonal;
//        }
//
//        return cmp;
//    }
//
//    /**
//     *
//     * @param cmp
//     */
//    public double getVariance(Component cmp) {
//        switch (cmp) {
//            case Noise:
//                return nVar;
//            case Cycle:
//                return cVar;
//            case Level:
//                return lVar;
//            case Slope:
//                return sVar;
//            case Seasonal:
//                return seasVar;
//            default:
//                return -1;
//        }
//    }
//
//    public void setCycle(double cro, double cperiod) {
//        cycle(cro, cperiod);
//    }
//
//    private void cycle(double cro, double cperiod) {
//        cDump = cro;
//        cPeriod = cperiod;
//        double q = Math.PI * 2 / cperiod;
//        ccos = cDump * Math.cos(q);
//        csin = cDump * Math.sin(q);
//    }
//
//    public double getCyclicalDumpingFactor() {
//        return cDump;
//    }
//
//    public double getCyclicalPeriod() {
//        return cPeriod;
//    }
//
//    public int getPeriod() {
//        return period;
//    }
}
