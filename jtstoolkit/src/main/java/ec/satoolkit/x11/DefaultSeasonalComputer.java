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
package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
class DefaultSeasonalComputer extends DefaultX11Algorithm implements
        ISeasonalComputer {

    private IFiltering initialFilter, finalFilter;
    private boolean useMsr;
 private DefaultSeasonalFilteringStrategy[] finalComplexSeasonalFilteringStrategy;
    /**
     *
     * @param x11
     */
    DefaultSeasonalComputer() {
        initialFilter = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X3);
        finalFilter = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X5);
        useMsr = true;
    }

    /**
     *
     * @param step
     * @param s
     * @param info
     * @return
     */
    @Override
    public TsData doFinalFiltering(X11Step step, TsData s, InformationSet info) {
        IFiltering filtering;
        if (finalFilter == null) {
            filtering = new DummyFilter(context.isMultiplicative());
        } else if (step != X11Step.D || !useMsr) {
            filtering = finalFilter;
        } else {
            filtering = selectMsr(s, info);
        }
        generateMsr(s, info);
        if (step == X11Step.D) {
            SymmetricFilter c = filtering.getCentralFilter();
            if (c != null) {
                info.subSet(X11Kernel.D).set(X11Kernel.D9_SLEN, c.getLength());
            }
            info.subSet(X11Kernel.D).set(X11Kernel.D9_FILTER, filtering.getDescription());
        if ("Composite filter".endsWith(filtering.getDescription())) {
                info.subSet(X11Kernel.D).set(X11Kernel.D9_FILTER_COMPOSIT, finalComplexSeasonalFilteringStrategy);
            }
        }
        return filtering.process(s, s.getDomain());
    }

    @Override
    public TsData doInitialFiltering(X11Step step, TsData s, InformationSet info) {
        TsDomain rdomain = s.getDomain();
        IFiltering filtering;
        if (initialFilter == null) {
            filtering = new DummyFilter(context.isMultiplicative());
        } else {
            filtering = initialFilter;
        }
        return filtering.process(s, rdomain);
    }

    private IFiltering selectMsr(TsData s, InformationSet info) {
        // remove incomplete year
        TsDomain rdomain = s.getDomain().drop(0, context.getForecastHorizon());
        SymmetricFilter f7 = FilterFactory.makeSymmetricFilter(7);
        DefaultSeasonalFilteringStrategy fseas = new DefaultSeasonalFilteringStrategy(
                f7, new FilteredMeanEndPoints(f7));
        MsrTable rms = calculateMsr(fseas, rdomain, s);
        double grms = rms.getGlobalMsr();
        InformationSet dtables = info.subSet(X11Kernel.D);

        int ndrop = rdomain.getEnd().getPosition();
        if (ndrop != 0) {
            rdomain = rdomain.drop(0, ndrop);
            rms = calculateMsr(fseas, rdomain, s);
            grms = rms.getGlobalMsr();
        }

        int freq = context.getFrequency();
        IFiltering finalSeasonalFilter = null;
        if (!Double.isInfinite(grms)) {
            finalSeasonalFilter = SeasonalFilterFactory.getFilteringStrategyForGlobalRMS(grms);
        }

        int rmsrounds = 0;
        while (finalSeasonalFilter == null && rdomain.getLength() / freq >= 6) {
            ++rmsrounds;
            rdomain = rdomain.drop(0, freq);
            rms = calculateMsr(fseas, rdomain, s);
            grms = rms.getGlobalMsr();
            finalSeasonalFilter = SeasonalFilterFactory.getFilteringStrategyForGlobalRMS(grms);
        }

        if (finalSeasonalFilter == null) {
            finalSeasonalFilter = SeasonalFilterFactory.C_S3X5;
            dtables.set(X11Kernel.D9_DEFAULT, true);
        }
        dtables.set(X11Kernel.D9_RMSROUND, rmsrounds);
        return finalSeasonalFilter;
    }

    /**
     *
     * @param option
     */
    public void setFilter(SeasonalFilterOption option) {
        switch (option) {
            case X11Default:
                setX11Filters();
                break;
            case Msr:
                setMsrFilters();
                break;
            case Stable:
                setStableFilter();
                break;
            default:
                useMsr = false;
                IFiltering s = SeasonalFilterFactory.getDefaultFilteringStrategy(option);
                initialFilter = s;
                finalFilter = s;
        }
    }

    /**
     *
     * @param options
     */
    public void setFilters(SeasonalFilterOption[] options) {
        if (options == null) {
            setMsrFilters();
        } else if (options.length == 1 || allEqual(options)) {
            setFilter(options[0]);
        } else {
            useMsr = false;
            DefaultSeasonalFilteringStrategy[] s0 = new DefaultSeasonalFilteringStrategy[options.length];
            DefaultSeasonalFilteringStrategy[] s1 = new DefaultSeasonalFilteringStrategy[options.length];
            for (int i = 0; i < options.length; ++i) {
                if (options[i] == SeasonalFilterOption.Msr) {
                    s0[i] = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X3);
                    s1[i] = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X5);
                } else {
                    DefaultSeasonalFilteringStrategy filter = SeasonalFilterFactory.getDefaultFilteringStrategy(options[i]);
                    s0[i] = filter;
                    s1[i] = filter;
                }
            }
            if (options.length != 1) {
                finalComplexSeasonalFilteringStrategy = s1;
            }
            initialFilter = new ComplexSeasonalFilteringStrategy(s0);
            finalFilter = new ComplexSeasonalFilteringStrategy(s1);
        }
    }

    /**
     *
     */
    public void setMsrFilters() {
        useMsr = true;
        initialFilter = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X3);
        finalFilter = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X5);
    }

    public void setDummyFilter() {
        useMsr = false;
        initialFilter = null;
        finalFilter = null;
    }

    /**
     *
     */
    public void setStableFilter() {
        useMsr = false;
        initialFilter = new StableSeasonalFilteringStrategy();
        finalFilter = new StableSeasonalFilteringStrategy();
    }

    /**
     *
     */
    public void setX11Filters() {
        useMsr = false;
        initialFilter = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X3);
        finalFilter = SeasonalFilterFactory.getDefaultFilteringStrategy(SeasonalFilterOption.S3X5);
    }

    private MsrTable calculateMsr(DefaultSeasonalFilteringStrategy fseas, TsDomain rdomain, TsData s) {
        TsData s1 = fseas.process(s, rdomain);
        TsData s2 = op(s, s1);
        return MsrTable.create(s1, s2, isMultiplicative());
    }

    private void generateMsr(TsData s, InformationSet info) {
        TsDomain rdomain = s.getDomain().drop(0, context.getForecastHorizon());
        SymmetricFilter f7 = FilterFactory.makeSymmetricFilter(7);
        DefaultSeasonalFilteringStrategy fseas = new DefaultSeasonalFilteringStrategy(
                f7, new FilteredMeanEndPoints(f7));
        MsrTable rms = calculateMsr(fseas, rdomain, s);
        InformationSet dtables = info.subSet(X11Kernel.D);
        dtables.set(X11Kernel.D9_RMS, rms);
    }
    
     private boolean allEqual(SeasonalFilterOption[] options) {
        SeasonalFilterOption option1 = options[0];
        for (SeasonalFilterOption option : options) {
            if (!option.equals(option1)) {
                return false;
            }
        }
        return true;
    }
}
