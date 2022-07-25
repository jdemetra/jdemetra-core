package jdplus.tempdisagg.univariate;

import demetra.tempdisagg.univariate.TemporalDisaggregationSpec;
import demetra.timeseries.AbstractMultiTsDocument;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.List;

public class TemporalDisaggregationDocument extends AbstractMultiTsDocument<TemporalDisaggregationSpec, TemporalDisaggregationResults> {

    private final ModellingContext context;

    public TemporalDisaggregationDocument() {
        super(TemporalDisaggregationSpec.CHOWLIN);
        context = ModellingContext.getActiveContext();
    }

    public TemporalDisaggregationDocument(ModellingContext context) {
        super(TemporalDisaggregationSpec.CHOWLIN);
        this.context = context;
    }
    
    public ModellingContext getContext(){
        return context;
    }

    @Override
    protected TemporalDisaggregationResults internalProcess(TemporalDisaggregationSpec spec, List<TsData> data) {
        TsData[] indicators = new TsData[data.size()-1];
        for (int i=1; i<data.size(); ++i)
            indicators[i]=data.get(i);
        return TemporalDisaggregationProcessor.process(data.get(0), indicators, spec);
    }

}
