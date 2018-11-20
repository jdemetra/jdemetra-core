/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.tramo.internal;

import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.IArmaModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */
public class ArmaModule implements IArmaModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ArmaModule.class)
    public static class Builder {

        private boolean wn = false;

        private Builder() {
        }

        public Builder acceptWhiteNoise(boolean ok) {
            this.wn = ok;
            return this;
        }

        public ArmaModule build() {
            return new ArmaModule(wn);
        }
    }

    // returns the first inic value that can be estimated
    static int comespa(final int freq, final int n, final int inic, final int d, final int bd, final boolean seas) {
        for (int i = inic; i > 1; --i) {
            if (checkespa(freq, n, i, d, bd, seas)) {
                return i;
            }
        }
        return 0;
    }

    static boolean checkespa(final int freq, final int nz, final int inic, final int d, final int bd, final boolean seas) {
        SarimaSpecification spec = checkmaxspec(freq, inic, d, bd, seas);
        if (TramoUtility.autlar(nz, spec) < 0) {
            return false;
        }
        int n = nz - spec.getP() - spec.getPeriod() * spec.getBp();
        spec.setP(0);
        spec.setBp(0);
        return TramoUtility.autlar(n, spec) >= 0;
    }

    static SarimaSpecification calcmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        if (seas) {
            spec.setBd(bd);
        }
        switch (inic) {
            case 1:
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
                break;
            case 2:
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
                break;
            case 3:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
                break;
            case 4:
                spec.setP(3);
                spec.setQ(3);
                spec.setBp(2);
                spec.setBq(2);
                break;
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBp(0);
//        }
        return spec;
    }
    
    static int maxInic(int period){
        switch (period){
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                return 3;
        }
    }

    static SarimaSpecification checkmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        if (seas) {
            spec.setBd(bd);
        }
        switch (inic) {
            case 1:
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
                break;
            case 2:
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
                break;
            case 3:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
                break;
            case 4:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBp(2);
                    spec.setBq(2);
                }
                break;
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBp(0);
//        }
        return spec;
    }

    private final boolean wn;

    private ArmaModule(boolean wn) {
        this.wn = wn;
    }

    private ArmaModuleImpl createModule(SarimaSpecification maxspec) {
        return ArmaModuleImpl.builder()
                .acceptWhiteNoise(wn)
                .maxP(maxspec.getP())
                .maxQ(maxspec.getQ())
                .maxBp(maxspec.getBp())
                .maxBq(maxspec.getBq())
                .build();

    }

    @Override
    public ProcessingResult process(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        SarimaSpecification curspec = desc.getSpecification();
        int inic = comespa(curspec.getPeriod(), desc.regarima().getObservationsCount(),
                maxInic(curspec.getPeriod()), curspec.getD(), curspec.getBd(), context.isSeasonal());
        if (inic == 0) {
            if (!curspec.isAirline(context.isSeasonal())) {
                curspec.airline(context.isSeasonal());
                desc.setSpecification(curspec);
                context.setEstimation(null);
                return ProcessingResult.Changed;
            } else {
                return ProcessingResult.Unprocessed;
            }
        }
        SarimaSpecification maxspec = calcmaxspec(desc.getAnnualFrequency(),
                inic, curspec.getD(), curspec.getBd(), context.isSeasonal());
        DoubleSequence res = RegArimaUtility.olsResiduals(desc.regarima());
        ArmaModuleImpl impl = createModule(maxspec);
        SarmaSpecification nspec = impl.process(res, desc.getAnnualFrequency(), maxspec.getD(), maxspec.getBd(), context.isSeasonal());
        if (nspec.equals(curspec.doStationary())) {
            return ProcessingResult.Unchanged;
        }
        curspec = SarimaSpecification.of(nspec, curspec.getD(), curspec.getBd());
        desc.setSpecification(curspec);
        context.setEstimation(null);
        return ProcessingResult.Changed;
    }

    public SarimaSpecification process(RegArimaModel<SarimaModel> regarima, boolean seas) {
        SarimaSpecification curSpec = regarima.arima().specification();
        int inic = comespa(curSpec.getPeriod(), regarima.getObservationsCount(),  maxInic(curSpec.getPeriod()), curSpec.getD(), curSpec.getBd(), seas);
        if (inic == 0) {
            curSpec.airline(seas);
            return curSpec;
        }
        SarimaSpecification maxspec = calcmaxspec(curSpec.getPeriod(), inic, curSpec.getD(), curSpec.getBd(), seas);
        DoubleSequence res = RegArimaUtility.olsResiduals(regarima);
        ArmaModuleImpl impl = createModule(maxspec);
        SarmaSpecification spec = impl.process(res, curSpec.getPeriod(), curSpec.getD(), curSpec.getBd(), curSpec.getPeriod() > 1);
        if (spec == null) {
            curSpec.airline(seas);
            return curSpec;
        } else {
            return SarimaSpecification.of(spec, curSpec.getD(), curSpec.getBd());
        }
    }
}
