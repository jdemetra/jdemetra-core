/*
 * Copyright 2013 National Bank of Belgium
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
package ec.satoolkit.seats;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultModelDecomposer implements IArimaDecomposer {

    private double epsphi = SeatsSpecification.DEF_EPSPHI;
    private double rmod = SeatsSpecification.DEF_RMOD;
    private boolean noisyModel = true;

    /**
     *
     */
    public DefaultModelDecomposer(boolean noisy) {
        noisyModel = noisy;
    }

    /**
     *
     * @param sarima
     * @param info
     * @param context
     * @return
     */
    @Override
    public UcarimaModel decompose(SeatsModel model, InformationSet info,
            SeatsContext context) {
        UcarimaModel ucm = null;
        try {
            // tackle special cases: ma unit roots 
            SarimaModel sarima = model.getSarima();
            SarimaSpecification spec = sarima.getSpecification();
            addStartInfo(context);
            int rar = spec.getD() + spec.getBD(), sar = spec.getBD(), rma = 0, sma = 0;
            Polynomial pr = sarima.getRegularMA(), ps = sarima.getSeasonalMA();
            while (ps.getDegree() > 0) {
                Polynomial.Division division = Polynomial.divide(ps, UnitRoots.D1);
                if (division.isExact()) {
                    ++sma;
                    ++rma;
                    ps = division.getQuotient();
                } else {
                    break;
                }
            }
            while (pr.getDegree() > 0) {
                Polynomial.Division division = Polynomial.divide(pr, UnitRoots.D1);
                if (division.isExact()) {
                    ++rma;
                    pr = division.getQuotient();
                } else {
                    break;
                }
            }
            int rc = Math.min(rma, rar);
            int sc = Math.min(sma, sar);

            ArimaModel arima;
            if (rc > 0 || sc > 0) {

                Polynomial rur = Polynomial.ONE;
                for (int i = 0; i < rc; ++i) {
                    rur = rur.times(UnitRoots.D1);
                }
                for (int i = 0; i < sc; ++i) {
                    rur = rur.times(UnitRoots.S(spec.getFrequency(), 1));
                }
                BackFilter ur = new BackFilter(rur);
                arima = new ArimaModel(sarima.getStationaryAR(),
                        sarima.getNonStationaryAR().divide(ur),
                        sarima.getMA().divide(ur), sarima.getInnovationVariance());
            } else {
                arima = ArimaModel.create(sarima);
            }

            TrendCycleSelector tsel = new TrendCycleSelector(rmod);
            SeasonalSelector ssel = new SeasonalSelector(sarima.getSpecification().getFrequency(), epsphi);

            ModelDecomposer decomposer = new ModelDecomposer();
            decomposer.add(tsel);
            decomposer.add(ssel);

            ucm = decomposer.decompose(ArimaModel.create(arima));
            double var = ucm.setVarianceMax(-1, noisyModel);
            if (var >= 0) {
                return complete(spec.getFrequency(), sarima, ucm, rc, sc);
            } else if (noisyModel) {
                ucm.normalize();
                model.setNoisyModel(ucm.getModel());
                return complete(spec.getFrequency(), null, ucm, rc, sc);
            } else {
                ucm = null;
                return null;
            }
        } catch (BaseException err) {
            return null;
        } finally {
            addEndInfo(context, ucm != null);
        }
    }

    /**
     * @return the epsphi
     */
    public double getEpsphi() {
        return epsphi;
    }

    /**
     * @return the rmod
     */
    public double getRmod() {
        return rmod;
    }

    /**
     * @param epsphi the epsphi to set
     */
    public void setEpsphi(double epsphi) {
        this.epsphi = epsphi;
    }

    /**
     * @param rmod the rmod to set
     */
    public void setRmod(double rmod) {
        this.rmod = rmod;
    }

    private UcarimaModel complete(int frequency, IArimaModel arima, UcarimaModel ucm, int rc, int sc) {
        if (rc == 0 && sc == 0) {
            return ucm;
        }
        ArimaModel[] cmps = new ArimaModel[ucm.getComponentsCount()];
        Polynomial rur = Polynomial.ONE;
        for (int i = 0; i < rc; ++i) {
            rur = rur.times(UnitRoots.D1);
        }
        BackFilter pr = new BackFilter(rur);
        Polynomial sur = Polynomial.ONE;
        for (int i = 0; i < sc; ++i) {
            sur = sur.times(UnitRoots.S(frequency, 1));
        }
        BackFilter ps = new BackFilter(sur);
        ArimaModel t = ucm.getComponent(0);
        if (rc > 0) {
            cmps[0] = new ArimaModel(t.getStationaryAR(), t.getNonStationaryAR().times(pr), t.getMA().times(pr), t.getInnovationVariance());
        } else {
            cmps[0] = t;
        }
        ArimaModel s = ucm.getComponent(1);
        if (sc > 0) {
            cmps[1] = new ArimaModel(s.getStationaryAR(), s.getNonStationaryAR().times(ps), s.getMA().times(ps), s.getInnovationVariance());
        } else {
            cmps[1] = s;
        }

        for (int i = 2; i < ucm.getComponentsCount(); ++i) {
            cmps[i] = ucm.getComponent(i);
        }
        return new UcarimaModel(arima, cmps);
    }

    private void addEndInfo(SeatsContext context, boolean ok) {
//        if (context.processingLog != null) {
//            if (ok) {
//                context.processingLog.add(ProcessingInformation.success(MODEL_DECOMPOSER,
//                        DefaultModelDecomposer.class.getName()));
//            } else {
//                context.processingLog.add(ProcessingInformation.failure(MODEL_DECOMPOSER,
//                        DefaultModelDecomposer.class.getName()));
//            }
//
//        }
    }

    private void addStartInfo(SeatsContext context) {
//        if (context.processingLog != null) {
//            context.processingLog.add(ProcessingInformation.start(MODEL_DECOMPOSER,
//                    DefaultModelDecomposer.class.getName()));
//        }
    }

}
