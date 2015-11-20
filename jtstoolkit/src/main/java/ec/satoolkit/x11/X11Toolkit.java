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
import java.util.Set;

/**
 * An X11Toolkit contains the different modules used during the execution of an
 * X11 processing. The execution of the different steps is provided by an
 * X11Kernel object
 *
 * The following modules are considered: - IX11Preprocessor: extension of the
 * series by forecasts/backcasts - ITrendCycleComputer: computation of the
 * trend-cycle - ISeasonalComputer: computation of the seasonal component -
 * IExtremeValuesCorrector: correction of the extreme values -
 * ISeasonalNormalizer: normalisation of the seasonal component - IX11Utilities:
 * several specific corrections (trend bias...)
 *
 * The class is able to generate the default implementations of those modules,
 * using a given X11 specification. However, developers could replace each of
 * the different components of the toolkit by other implementations (mainly for
 * research).
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class X11Toolkit extends BaseX11Algorithm implements
        IX11Toolkit {

    /**
     * Creates a default toolkit (corresponding to the implementation of the
     * US-Census Bureau)
     *
     * @param spec The specifications of the processing
     * @return A default toolkit, to be used by an X11Kernel
     */
    public static X11Toolkit create(X11Specification spec) {
        X11Context context = new X11Context(spec.getMode(), spec
                .getForecastHorizon());

        X11Toolkit toolkit = new X11Toolkit(context);

        if (spec.getForecastHorizon() != 0) {
            toolkit.setPreprocessor(new AirlinePreprocessor());
        }

        DefaultSeasonalComputer sprovider = new DefaultSeasonalComputer();
        if (spec.isSeasonal()) {
            sprovider.setFilters(spec.getSeasonalFilters());
        } else {
            sprovider.setDummyFilter();
        }
        toolkit.setSeasonalFilterprovider(sprovider);

    toolkit.setExcludefcas(spec.isExcludefcst());
        
        
        if (spec.isAutoHenderson()) {
            toolkit.setTrendCycleFilterprovider(new AutomaticTrendCycleComputer());
        } else {
            toolkit.setTrendCycleFilterprovider(new DefaultTrendCycleComputer(
                    spec.getHendersonFilterLength()));
        }
        
        /* Define which ExtremeExtremeValuesCorrector has to be used */
        if (spec.getCalendarSigma().equals(CalendarSigma.Select)) {
            GroupSpecificExtremeValuesCorrector xcorrector = new GroupSpecificExtremeValuesCorrector();
            xcorrector.setSigma(spec.getLowerSigma(), spec.getUpperSigma());
            xcorrector.setSigmavecOption(spec.getSigmavec());
            toolkit.setExtremeValuescorrector(xcorrector);
        } else if (spec.getCalendarSigma().equals(CalendarSigma.All)) {
            PeriodSpecificExtremeValuesCorrector xcorrector = new PeriodSpecificExtremeValuesCorrector();
            xcorrector.setSigma(spec.getLowerSigma(), spec.getUpperSigma());
            toolkit.setExtremeValuescorrector(xcorrector);
        } else if (spec.getCalendarSigma().equals(CalendarSigma.Signif)) {
            CochranDependentExtremeValuesCorrector xcorrector = new CochranDependentExtremeValuesCorrector();
            xcorrector.setSigma(spec.getLowerSigma(), spec.getUpperSigma());
            toolkit.setExtremeValuescorrector(xcorrector);
        } else {
            DefaultExtremeValuesCorrector xcorrector = new DefaultExtremeValuesCorrector();
            xcorrector.setSigma(spec.getLowerSigma(), spec.getUpperSigma());
            toolkit.setExtremeValuescorrector(xcorrector);
        }

        /*In Case that one or more and not all of the filters are stable the normalizer needs this information*/
        if (spec.isSeasonal()) {
            DefaultSeasonalNormalizer nprovider = new DefaultSeasonalNormalizer();
            nprovider.setNormalizer(spec.getSeasonalFilters());
            toolkit.setSeasonalnormalizer(nprovider);
        } else {
            toolkit.setSeasonalnormalizer(DummySeasonalNormalizer.instance);
        }

        /*toolkit.setSeasonalnormalizer(new DefaultSeasonalNormalizer());*/
        return toolkit;
    }

    public static X11Toolkit create() {
        return create(new X11Specification());
    }
    private IX11Preprocessor preprocessor;
    private ITrendCycleComputer tcprovider;
    private ISeasonalComputer sprovider;
    private IExtremeValuesCorrector xcorrector;
    private ISeasonalNormalizer snormalizer;
    private IX11Utilities utilities = new DefaultX11Utilities();
    private boolean excludefcst=false;

    private X11Toolkit(X11Context context) {
        this.context = context;
    }

    /**
     * Gets the current module for the correction of extreme values.
     *
     * @return
     */
    @Override
    public IExtremeValuesCorrector getExtremeValuesCorrector() {
        return xcorrector;
    }

    /**
     * Gets the current module for pre-processing (forecasts)
     *
     * @return
     */
    @Override
    public IX11Preprocessor getPreprocessor() {
        return preprocessor;
    }

    /**
     * Gets the current module for the computation of the seasonal component
     *
     * @return
     */
    @Override
    public ISeasonalComputer getSeasonalComputer() {
        return sprovider;
    }

    /**
     * Gets the current module for the normalisation of the seasonal component
     *
     * @return
     */
    @Override
    public ISeasonalNormalizer getSeasonalNormalizer() {
        return snormalizer;
    }

    /**
     * Gets the current module for the computation of the trend-cycle
     *
     * @return
     */
    @Override
    public ITrendCycleComputer getTrendCycleComputer() {
        return tcprovider;
    }

    /**
     * Gets the current module for the computation of some utilities functions
     *
     * @return
     */
    @Override
    public IX11Utilities getUtilities() {
        return utilities;
    }

    @Override
    public boolean isExcludefcst() {
        return excludefcst;
    }

    /**
     *
     * @param context
     */
    @Override
    public X11Context getContext() {
        return context;
    }

    /**
     * @param preprocessor the preprocessor to set
     */
    public void setPreprocessor(final IX11Preprocessor preprocessor) {
        if (preprocessor != null) {
            preprocessor.setContext(context);
        }
        this.preprocessor = preprocessor;

    }

    /**
     * @param tcprovider the tcprovider to set
     */
    public void setTrendCycleFilterprovider(final ITrendCycleComputer tcprovider) {
        if (tcprovider != null) {
            tcprovider.setContext(context);
        }
        this.tcprovider = tcprovider;
    }

    /**
     * @param sprovider the sprovider to set
     */
    public void setSeasonalFilterprovider(final ISeasonalComputer sprovider) {
        if (sprovider != null) {
            sprovider.setContext(context);
        }
        this.sprovider = sprovider;
    }

    /**
     * @param xcorrector the xcorrector to set
     */
    public void setExtremeValuescorrector(IExtremeValuesCorrector xcorrector) {
        if (xcorrector != null) {
            xcorrector.setContext(context);
        }
        this.xcorrector = xcorrector;
    }

    /**
     * @param snormalizer the snormalizer to set
     */
    public void setSeasonalnormalizer(ISeasonalNormalizer snormalizer) {
        if (snormalizer != null) {
            snormalizer.setContext(context);
        }
        this.snormalizer = snormalizer;
    }

    public void setExcludefcas(boolean excldudefcast) {
        this.excludefcst = excldudefcast;
    }

    /**
     * @param utilities the utilities to set
     */
    public void setUtilities(IX11Utilities utilities) {
        if (utilities != null) {
            utilities.setContext(context);
        }
        this.utilities = utilities;
    }
}
