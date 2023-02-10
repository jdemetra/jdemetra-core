/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.extendedairline;

import demetra.highfreq.ExtendedAirlineModellingSpec;
import demetra.processing.DefaultProcessingLog;
import demetra.timeseries.AbstractTsDocument;
import demetra.timeseries.Ts;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import jdplus.highfreq.regarima.HighFreqRegArimaModel;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDocument extends AbstractTsDocument<ExtendedAirlineModellingSpec, HighFreqRegArimaModel> {

    private final ModellingContext context;

    public ExtendedAirlineDocument() {
        super(ExtendedAirlineModellingSpec.DEFAULT_ENABLED);
        context = ModellingContext.getActiveContext();
    }

    public ExtendedAirlineDocument(ModellingContext context) {
        super(ExtendedAirlineModellingSpec.DEFAULT_ENABLED);
        this.context = context;
    }

    @Override
    public void set(ExtendedAirlineModellingSpec spec, Ts s) {
        if (s != null) {
            super.set(spec.withPeriod(s.getData().getTsUnit()), s);
        } else {
            super.set(spec, s);
        }
    }

    @Override
    public void set(ExtendedAirlineModellingSpec spec) {
        Ts s = getInput();
        if (s != null) {
            super.set(spec.withPeriod(s.getData().getTsUnit()));
        } else {
            super.set(spec);
        }
    }

    @Override
    public void set(Ts s) {
        if (s == null) {
            set(s);
        } else {
            ExtendedAirlineModellingSpec spec = getSpecification();
            super.set(spec.withPeriod(s.getData().getTsUnit()), s);
        }
    }

    @Override
    protected HighFreqRegArimaModel internalProcess(ExtendedAirlineModellingSpec spec, TsData data) {
        return ExtendedAirlineKernel.of(spec, context).process(data, new DefaultProcessingLog());
    }

}
