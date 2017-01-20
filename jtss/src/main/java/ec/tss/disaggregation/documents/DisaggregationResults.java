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
package ec.tss.disaggregation.documents;

import ec.benchmarking.simplets.TsDisaggregation;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class DisaggregationResults implements IProcResults {

    public static final String DISAGGREGATION = "disaggregation",
            LDISAGGREGATION = "lower bound", UDISAGGREGATION = "upper bound",
            EDISAGGREGATION = "disggregationError", RESIDUALS = "residuals",
            REGEFFECT = "regression effect", SMOOTHING = "smoothing effect";
    private final TsDisaggregation<? extends ISsf> result;
    private final int nindicators;

    public DisaggregationResults(TsDisaggregation<? extends ISsf> disaggregation, int nindics) {
        result = disaggregation;
        this.nindicators = nindics;
    }

    public DiffuseConcentratedLikelihood getLikelihood() {
        return result.getLikelihood();
    }

    public LikelihoodStatistics getLikelihoodStatistics() {
        DiffuseConcentratedLikelihood ll = result.getLikelihood();
        IParametricMapping<? extends ISsf> mapping = result.getMapping();
        return LikelihoodStatistics.create(ll, ll.getN(), mapping == null ? 0 : mapping.getDim(), 0);
    }

    public IFunction getEstimationFunction() {
        return result.getEstimationFunction();
    }

    public IFunctionInstance getMin() {
        return result.getMin();
    }

    public Parameter getEstimatedParameter() {
        Matrix i = result.getObservedInformation();
        if (i == null) {
            return null;
        }
        IParametricMapping<ISsf> mapping = (IParametricMapping<ISsf>) result.getMapping();
        IReadDataBlock p = mapping.map(result.getEstimatedSsf());
        Parameter x = new Parameter(p.get(0), ParameterType.Estimated);
        x.setStde(Math.sqrt(1 / i.get(0, 0)));
        return x;
    }

    public ISsf getEstimatedSsf() {
        return result.getEstimatedSsf();
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (MAPPING.contains(id)) {
            return MAPPING.getData(this, id, tclass);
        } else {
            return null;
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    // MAPPING
    public static InformationMapping<DisaggregationResults> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<DisaggregationResults, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<DisaggregationResults, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<DisaggregationResults> MAPPING = new InformationMapping<>(DisaggregationResults.class);

    static {
        MAPPING.set(DISAGGREGATION, source -> source.result.getSmoothedSeries());
        MAPPING.set(EDISAGGREGATION, source -> source.result.getSmoothedSeriesVariance().sqrt());
        MAPPING.set(LDISAGGREGATION, source -> {
            TsData s = source.result.getSmoothedSeries();
            TsData e = source.result.getSmoothedSeriesVariance().sqrt();
            e.apply(x -> -2 * x);
            return TsData.add(s, e);
        });
        MAPPING.set(UDISAGGREGATION, source -> {
            TsData s = source.result.getSmoothedSeries();
            TsData e = source.result.getSmoothedSeriesVariance().sqrt();
            e.apply(x -> 2 * x);
            return TsData.add(s, e);
        });
        MAPPING.set(RESIDUALS, source -> source.result.getFullResiduals());
        MAPPING.set(SMOOTHING, source -> {
            if (source.nindicators == 0) {
                return null;
            }
            TsData y = source.result.getSmoothedSeries();
            TsData regs = source.getData(REGEFFECT, TsData.class);
            return TsData.subtract(y, regs);
        });

        MAPPING.set(REGEFFECT, source -> {
            if (source.nindicators == 0) {
                return null;
            }
            TsDomain dom = source.result.getData().hEDom;
            DataBlock d = new DataBlock(dom.getLength());
            double[] b = source.getLikelihood().getB();
            Matrix matrix = source.result.getModel().getX().all().matrix(dom);
            for (int i = b.length - source.nindicators, j = matrix.getColumnsCount() - source.nindicators;
                    i < b.length; ++i, ++j) {
                d.addAY(b[i], matrix.column(j));
            }
            return new TsData(dom.getStart(), d);
        });
    }
}
