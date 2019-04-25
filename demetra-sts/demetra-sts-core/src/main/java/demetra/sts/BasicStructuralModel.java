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
package demetra.sts;

import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class BasicStructuralModel {

    private static ComponentUse of(double var) {
        return var < 0 ? ComponentUse.Unused : ComponentUse.Free;
    }

    /**
     *
     * @return
     */
    public BsmSpec specification() {
        BsmSpec spec = new BsmSpec();
        if (lFixed) {
            spec.fixComponent(lVar, Component.Level);
        } else {
            spec.setLevelUse(of(lVar));
        }
        if (sFixed) {
            spec.fixComponent(sVar, Component.Slope);
        } else {
            spec.setSlopeUse(of(sVar));
        }
        if (nFixed) {
            spec.fixComponent(nVar, Component.Noise);
        } else {
            spec.setNoiseUse(nVar > 0 ? ComponentUse.Free : ComponentUse.Unused);
        }
        if (cFixed) {
            spec.fixComponent(cVar, Component.Cycle);
            spec.setCycleDumpingFactor(cDump);
            spec.setCycleLength(cPeriod);
        } else {
            spec.setCycleUse(of(cVar));
        }
        if (seasFixed) {
            spec.fixComponent(seasVar, Component.Seasonal);
        } else {
            spec.setSeasUse(of(seasVar));
            spec.setSeasonalModel(seasModel);
        }
        return spec;
    }

    private static void svar(int freq, FastMatrix O) {
        int n = freq - 1;
        FastMatrix H = FastMatrix.make(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }

        SymmetricMatrix.XXt(H, O);
    }

    /**
     *
     */
    final int period;
    double lVar, sVar, seasVar, cVar, nVar;
    boolean lFixed, sFixed, seasFixed, cFixed, nFixed;
    double cDump, cPeriod;
    double ccos, csin;
    SeasonalModel seasModel;

    /**
     *
     * @param spec
     * @param period
     */
    public BasicStructuralModel(BsmSpec spec, int period) {
        this.period = period;
        seasModel = spec.getSeasonalModel();
        switch (spec.getNoiseUse()) {
            case Free:
                nVar = 1;
                nFixed = false;
                break;
            case Fixed:
                nFixed = true;
                nVar = spec.getNoiseVar();
                break;
            case Unused:
                nFixed = false;
                nVar = -1;
                break;
        }
        switch (spec.getCycleUse()) {
            case Free:
                cycle(.5, period * 2);
                cVar = 1;
                cFixed = false;
                break;
            case Fixed:
                cFixed = true;
                cycle(spec.getCycleDumpingFactor(), spec.getCycleLength());
                cVar = spec.getCycleVar();
                break;
            case Unused:
                cFixed = false;
                cVar = -1;
                break;
        }
        switch (spec.getLevelUse()) {
            case Free:
                lVar = 1;
                lFixed = false;
                break;
            case Fixed:
                lFixed = true;
                lVar = spec.getLevelVar();
                break;
            case Unused:
                lFixed = false;
                lVar = -1;
                break;
        }
        switch (spec.getSlopeUse()) {
            case Free:
                sVar = 1;
                sFixed = false;
                break;
            case Fixed:
                sFixed = true;
                sVar = spec.getSlopeVar();
                break;
            case Unused:
                sFixed = false;
                sVar = -1;
                break;
        }
        switch (spec.getSeasUse()) {
            case Free:
                seasVar = 1;
                seasFixed = false;
                break;
            case Fixed:
                seasFixed = true;
                seasVar = spec.getSeasVar();
                break;
            case Unused:
                seasFixed = false;
                seasVar = -1;
                break;
        }
    }

    /**
     *
     * @param factor
     * @return
     */
    public boolean scaleVariances(double factor) {
        if ((lFixed && lVar != 0) || (sFixed && sVar != 0) || (seasFixed && seasVar != 0)
                || (cFixed && cVar != 0) || nFixed) {
            return false;
        }
        if (lVar > 0) {
            lVar *= factor;
        }
        if (cVar > 0) {
            cVar *= factor;
        }
        if (sVar > 0) {
            sVar *= factor;
        }
        if (seasVar > 0) {
            seasVar *= factor;
        }
        if (nVar > 0) {
            nVar *= factor;
        }
        return true;
    }

    /**
     *
     * @param cmp
     * @param var
     */
    public void setVariance(Component cmp, double var) {
        switch (cmp) {
            case Noise:
                nVar = var;
                return;
            case Cycle:
                if (cVar >= 0) {
                    cVar = var;
                }
                return;
            case Level:
                if (lVar >= 0) {
                    lVar = var;
                }
                return;
            case Slope:
                if (sVar >= 0) {
                    sVar = var;
                }
                return;
            case Seasonal:
                if (seasVar >= 0) {
                    seasVar = var;
                    if (var == 0) {
                        seasModel = SeasonalModel.Fixed;
                    }
                }
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public Component fixMaxVariance(double val) {
        Component max = getMaxVariance();
        if (max != Component.Undefined) {
            double vmax = getVariance(max);
            if (vmax != val) {
                if (scaleVariances(val / vmax)) {
                    return max;
                }
            } else {
                return max;
            }
        }
        return Component.Undefined;
    }

    /**
     *
     * @param eps
     * @return
     */
    public boolean fixSmallVariance(double eps) {
        Component min = getMinVariance();
        if (min != Component.Undefined && getVariance(min) < eps) {
            setVariance(min, 0);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public Component getMaxVariance() {
        Component cmp = Component.Undefined;
        double vmax = 0;
        if (lVar > vmax) {
            vmax = lVar;
            cmp = Component.Level;
        }
        if (sVar > vmax) {
            vmax = sVar;
            cmp = Component.Slope;
        }
        if (seasVar > vmax) {
            vmax = seasVar;
            cmp = Component.Seasonal;
        }
        if (cVar > vmax) {
            vmax = cVar;
            cmp = Component.Cycle;
        }
        if (nVar > vmax) {
            cmp = Component.Noise;
        }
        return cmp;
    }

    /**
     *
     * @return
     */
    public Component getMinVariance() {
        Component cmp = Component.Undefined;
        double vmin = Double.MAX_VALUE;
        if (lVar > 0 && lVar < vmin) {
            vmin = lVar;
            cmp = Component.Level;
        }
        if (sVar > 0 && sVar < vmin) {
            vmin = sVar;
            cmp = Component.Slope;
        }
        if (seasVar > 0 && seasVar < vmin) {
            vmin = seasVar;
            cmp = Component.Seasonal;
        }
        if (cVar > 0 && cVar < vmin) {
            vmin = cVar;
            cmp = Component.Cycle;
        }
        if (nVar > 0 && nVar < vmin) {
            cmp = Component.Noise;
        }
        return cmp;
    }

    public int getComponentsCount() {
        int n = 0;
        if (nVar > 0) {
            ++n;
        }
        if (cVar >= 0) {
            ++n;
        }
        if (lVar >= 0) {
            ++n;
            if (sVar >= 0) {
                ++n;
            }
        }
        if (seasVar >= 0) {
            ++n;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public Component[] getComponents() {
        Component[] cmp = new Component[getComponentsCount()];
        int idx = 0;
        if (nVar > 0) {
            cmp[idx++] = Component.Noise;
        }
        if (cVar >= 0) {
            cmp[idx++] = Component.Cycle;
        }
        if (lVar >= 0) {
            cmp[idx++] = Component.Level;
            if (sVar >= 0) {
                cmp[idx++] = Component.Slope;
            }
        }
        if (seasVar >= 0) {
            cmp[idx] = Component.Seasonal;
        }

        return cmp;
    }

    /**
     *
     * @param cmp
     * @param var
     */
    public double getVariance(Component cmp) {
        switch (cmp) {
            case Noise:
                return nVar;
            case Cycle:
                return cVar;
            case Level:
                return lVar;
            case Slope:
                return sVar;
            case Seasonal:
                return seasVar;
            default:
                return -1;
        }
    }

    public void setCycle(double cro, double cperiod) {
        cycle(cro, cperiod);
    }

    private void cycle(double cro, double cperiod) {
        cDump = cro;
        cPeriod = cperiod;
        double q = Math.PI * 2 / cperiod;
        ccos = cDump * Math.cos(q);
        csin = cDump * Math.sin(q);
    }

    public double getCyclicalDumpingFactor() {
        return cDump;
    }

    public double getCyclicalPeriod() {
        return cPeriod;
    }

    public int getPeriod() {
        return period;
    }

}
