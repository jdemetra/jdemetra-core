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

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class BasicStructuralModel implements Cloneable {

    private static ComponentUse getUse(double var) {
        if (var < 0) {
            return ComponentUse.Unused;
        } else if (var == 0) {
            return ComponentUse.Fixed;
        } else {
            return ComponentUse.Free;
        }
    }

    private static double getVar(ComponentUse use) {
        switch (use) {
            case Unused:
                return -1;
            case Fixed:
                return 0;
            default:
                return 1;
        }
    }

    private static double getVar(SeasonalModel use) {
        switch (use) {
            case Unused:
                return -1;
            case Fixed:
                return 0;
            default:
                return 1;
        }
    }

    private SeasonalModel getSeas() {
        if (seasVar < 0) {
            return SeasonalModel.Unused;
        } else if (seasVar == 0) {
            return SeasonalModel.Fixed;
        } else {
            return seasModel;
        }
    }

    /**
     *
     * @return
     */
    public BsmSpec specification() {
        BsmSpec spec = new BsmSpec();
        spec.setSeasonalModel(getSeas());
        spec.setLevelUse(getUse(lVar));
        spec.setSlopeUse(getUse(sVar));
        spec.setCycleUse(getUse(cVar));
        spec.setNoiseUse(nVar <= 0 ? ComponentUse.Unused : ComponentUse.Free);
        return spec;
    }

    private static void svar(int freq, Matrix O) {
        int n = freq - 1;
        Matrix H = Matrix.make(freq, n);
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
    final int freq;
    double lVar, sVar, seasVar, cVar, nVar;
    double cDump, cPeriod;
    double ccos, csin;
    SeasonalModel seasModel;

    /**
     *
     * @param spec
     * @param freq
     */
    public BasicStructuralModel(BsmSpec spec, int freq) {
        this.freq = freq;
        seasModel = spec.getSeasonalModel();
        seasVar = getVar(seasModel);
        lVar = getVar(spec.getLevelUse());
        sVar = getVar(spec.getSlopeUse());
        cVar = getVar(spec.getCycleUse());
        nVar = getVar(spec.getNoiseUse());
        if (spec.getCycleUse() != ComponentUse.Unused) {
            cycle(.5, freq * 2);
        }
    }

    @Override
    public BasicStructuralModel clone() {
        try {
            return (BasicStructuralModel) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param factor
     */
    public void scaleVariances(double factor) {
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
                scaleVariances(val / vmax);
            }
        }
        return max;
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

    public int getFrequency() {
        return freq;
    }

}
