/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.extendedairline.decomposiiton;

import demetra.highfreq.ExtendedAirlineDecompositionSpec;
import demetra.processing.DefaultProcessingLog;
import demetra.timeseries.AbstractTsDocument;
import demetra.timeseries.Ts;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import jdplus.highfreq.extendedairline.ExtendedAirlineResults;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecompositionDocument extends AbstractTsDocument<ExtendedAirlineDecompositionSpec, ExtendedAirlineResults> {

    private final ModellingContext context;

    public ExtendedAirlineDecompositionDocument() {
        super(ExtendedAirlineDecompositionSpec.DEFAULT);
        context = ModellingContext.getActiveContext();
    }

    public ExtendedAirlineDecompositionDocument(ModellingContext context) {
        super(ExtendedAirlineDecompositionSpec.DEFAULT);
        this.context = context;
    }

    @Override
    public void set(ExtendedAirlineDecompositionSpec spec, Ts s) {
        if (s != null) {
            super.set(spec.withPeriod(s.getData().getTsUnit()), s);
        } else {
            super.set(spec, s);
        }
    }

    @Override
    public void set(ExtendedAirlineDecompositionSpec spec) {
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
            ExtendedAirlineDecompositionSpec spec = getSpecification();
            super.set(spec.withPeriod(s.getData().getTsUnit()), s);
        }
    }

    @Override
    protected ExtendedAirlineResults internalProcess(ExtendedAirlineDecompositionSpec spec, TsData data) {
        return new ExtendedAirlineDecompositionKernel(spec, context).process(data, new DefaultProcessingLog());
    }

}
