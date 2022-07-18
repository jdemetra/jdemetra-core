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
package jdplus.ssf.extractors;

import demetra.data.DoubleSeq;
import demetra.information.InformationMapping;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.StateStorage;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.information.GenericExplorable;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.ssf.StateInfo;
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.univariate.SsfData;
import jdplus.ssf.univariate.StateFilteringResults;
import jdplus.ssf.arima.SsfUcarima;
import demetra.math.matrices.Matrix;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
public class SsfUcarimaEstimation implements GenericExplorable {

    private UcarimaModel ucarima;
    private final CompositeSsf ssf;
    private final SsfData data;
    private StateStorage smoothedStates, filteredStates, filteringStates;

    public SsfUcarimaEstimation(final UcarimaModel ucarima, final DoubleSeq data) {
        this.ucarima=ucarima;
        this.ssf = SsfUcarima.of(ucarima);
        this.data = new SsfData(data);
    }

    public StateStorage getSmoothedStates() {
        if (smoothedStates == null) {
            try {
                StateStorage ss = AkfToolkit.smooth(ssf, data, true, false, false);
                smoothedStates = ss;
            } catch (OutOfMemoryError err) {
                DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
                StateStorage ss = StateStorage.light(StateInfo.Smoothed);
                int n = data.length();
                ss.prepare(ssf.getStateDim(), 0, n);
                for (int i = 0; i < n; ++i) {
                    ss.save(i, ds.block(i), null);
                }
                smoothedStates = ss;

            } catch (Exception err) {
                StateStorage ss = AkfToolkit.smooth(ssf, data, false, false, false);
                smoothedStates = ss;
            }
        }
        return smoothedStates;
    }

    public StateStorage getFilteredStates() {
        if (filteredStates == null) {
            try {
                StateFilteringResults fr = new StateFilteringResults(StateInfo.Concurrent, true);
                int n = data.length();
                fr.prepare(ssf.getStateDim(), 0, n);
                DkToolkit.sqrtFilter(ssf, data, fr, true);
                StateStorage ss = StateStorage.full(StateInfo.Concurrent);
                ss.prepare(ssf.getStateDim(), 0, n);
                for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i), fr.P(i));
                }
                filteredStates = ss;
            } catch (java.lang.OutOfMemoryError err) {
                StateFilteringResults fr = new StateFilteringResults(StateInfo.Concurrent, false);
                int n = data.length();
                fr.prepare(ssf.getStateDim(), 0, n);
                DkToolkit.sqrtFilter(ssf, data, fr, false);
                StateStorage ss = StateStorage.light(StateInfo.Concurrent);
                ss.prepare(ssf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition();
                for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i), null);
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                }
                 filteredStates = ss;
            }
        }
        return filteredStates;
    }

    public StateStorage getFilteringStates() {
        if (filteringStates == null) {
            try {
                DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(ssf, data, true);
                StateStorage ss = StateStorage.full(StateInfo.Forecast);
                int n = data.length();
                ss.prepare(ssf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition();
               for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i), fr.P(i));
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                    ss.P(i).set(Double.NaN);
                }
                filteringStates = ss;
            } catch (java.lang.OutOfMemoryError err) {
                // Just computes the states
                DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(ssf, data, false);
                StateStorage ss = StateStorage.light(StateInfo.Forecast);
                int n = data.length();
                ss.prepare(ssf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition();
                for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i), null);
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                }
                filteringStates = ss;
            }
        }
        return filteringStates;
    }
 
    private static final InformationMapping<SsfUcarimaEstimation> MAPPING = new InformationMapping<SsfUcarimaEstimation>() {
        @Override
        public Class getSourceClass() {
            return SsfUcarimaEstimation.class;
        }
    };

    static {
        MAPPING.delegate("ucarima", UcarimaModel.class, source -> source.ucarima);
        MAPPING.set("ncmps", Integer.class, source -> source.ssf.componentsPosition().length);
        MAPPING.set("cmppos", int[].class, source -> source.ssf.componentsPosition());
        MAPPING.set("T", Matrix.class, source -> {
            int dim = source.ssf.getStateDim();
            FastMatrix T = FastMatrix.square(dim);
            source.ssf.dynamics().T(0, T);
            return T;
        });
        MAPPING.set("V", Matrix.class, source -> {
            int dim = source.ssf.getStateDim();
            FastMatrix V = FastMatrix.square(dim);
            source.ssf.dynamics().V(0, V);
            return V;
        });
        MAPPING.set("Z", double[].class, source -> {
            int dim = source.ssf.getStateDim();
            DataBlock z = DataBlock.make(dim);
            source.ssf.loading().Z(0, z);
            return z.getStorage();
        });
        MAPPING.set("P0", Matrix.class, source -> {
            int dim = source.ssf.getStateDim();
            FastMatrix V = FastMatrix.square(dim);
            source.ssf.initialization().Pf0(V);
            return V;
        });
        MAPPING.set("B0", Matrix.class, source -> {
            int dim = source.ssf.getStateDim();
            int nd = source.ssf.initialization().getDiffuseDim();
            FastMatrix V = FastMatrix.make(dim, nd);
            source.ssf.initialization().diffuseConstraints(V);
            return V;
        });
        MAPPING.setArray("smoothing.array", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage smoothedStates = source.getSmoothedStates();
            return smoothedStates.getComponent(p).toArray();
        });
        MAPPING.setArray("smoothing.varray", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage smoothedStates = source.getSmoothedStates();
            return smoothedStates.getComponentVariance(p).toArray();
        });
        MAPPING.setArray("smoothing.cmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage smoothedStates = source.getSmoothedStates();
            return smoothedStates.getComponent(source.ssf.componentsPosition()[p]).toArray();
        });
        MAPPING.setArray("smoothing.vcmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage smoothedStates = source.getSmoothedStates();
            return smoothedStates.getComponentVariance(source.ssf.componentsPosition()[p]).toArray();
        });
        MAPPING.setArray("smoothing.state", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage smoothedStates = source.getSmoothedStates();
            return smoothedStates.a(p).toArray();
        });
        MAPPING.setArray("smoothing.vstate", 0, Integer.MAX_VALUE, Matrix.class, (source, p) -> {
            StateStorage smoothedStates = source.getSmoothedStates();
            return smoothedStates.P(p).unmodifiable();
        });
        MAPPING.set("smoothing.states", Matrix.class, source -> {
            int n = source.data.length(), m = source.ssf.getStateDim();
            double[] z = new double[n * m];
            StateStorage smoothedStates = source.getSmoothedStates();
            for (int i = 0, j = 0; i < m; ++i, j += n) {
                smoothedStates.getComponent(i).copyTo(z, j);
            }
            return Matrix.of(z, n, m);
        });
        MAPPING.set("smoothing.vstates", Matrix.class, source -> {
            int n = source.data.length(), m = source.ssf.getStateDim();
            double[] z = new double[n * m];
            StateStorage smoothedStates = source.getSmoothedStates();
            for (int i = 0, j = 0; i < m; ++i, j += n) {
                smoothedStates.getComponentVariance(i).copyTo(z, j);
            }
            return Matrix.of(z, n, m);
        });
        MAPPING.setArray("filtering.array", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteringStates();
            return fStates.getComponent(p).toArray();
        });
        MAPPING.setArray("filtering.varray", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteringStates();
            return fStates.getComponentVariance(p).toArray();
        });
        MAPPING.setArray("filtering.cmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteringStates();
            return fStates.getComponent(source.ssf.componentsPosition()[p]).toArray();
        });
        MAPPING.setArray("filtering.vcmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteringStates();
            return fStates.getComponentVariance(source.ssf.componentsPosition()[p]).toArray();
        });
        MAPPING.setArray("filtering.state", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteringStates();
            return fStates.a(p).toArray();
        });
        MAPPING.set("filtering.states", Matrix.class, source -> {
            int n = source.data.length(), m = source.ssf.getStateDim();
            double[] z = new double[n * m];
            StateStorage fStates = source.getFilteringStates();
            for (int i = 0, j = 0; i < m; ++i, j += n) {
                fStates.getComponent(i).copyTo(z, j);
            }
            return Matrix.of(z, n, m);
        });
        MAPPING.set("filtering.vstates", Matrix.class, source -> {
            int n = source.data.length(), m = source.ssf.getStateDim();
            double[] z = new double[n * m];
            StateStorage fStates = source.getFilteringStates();
            for (int i = 0, j = 0; i < m; ++i, j += n) {
                fStates.getComponentVariance(i).copyTo(z, j);
            }
            return Matrix.of(z, n, m);
        });
        MAPPING.setArray("filtering.vstate", 0, Integer.MAX_VALUE, Matrix.class, (source, p) -> {
            StateStorage fStates = source.getFilteringStates();
            return fStates.P(p).unmodifiable();
        });

        MAPPING.setArray("filtered.array", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteredStates();
            return fStates.getComponent(p).toArray();
        });
        MAPPING.setArray("filtered.varray", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteredStates();
            return fStates.getComponentVariance(p).toArray();
        });
        MAPPING.setArray("filtered.cmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteredStates();
            return fStates.getComponent(source.ssf.componentsPosition()[p]).toArray();
        });
        MAPPING.setArray("filtered.vcmp", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteredStates();
            return fStates.getComponentVariance(source.ssf.componentsPosition()[p]).toArray();
        });
        MAPPING.setArray("filtered.state", 0, Integer.MAX_VALUE, double[].class, (source, p) -> {
            StateStorage fStates = source.getFilteredStates();
            return fStates.a(p).toArray();
        });
        MAPPING.setArray("filtered.vstate", 0, Integer.MAX_VALUE, Matrix.class, (source, p) -> {
            StateStorage fStates = source.getFilteredStates();
            return fStates.P(p).unmodifiable();
        });
        MAPPING.set("filtered.states", Matrix.class, source -> {
            int n = source.data.length(), m = source.ssf.getStateDim();
            double[] z = new double[n * m];
            StateStorage fStates = source.getFilteredStates();
            for (int i = 0, j = 0; i < m; ++i, j += n) {
                fStates.getComponent(i).copyTo(z, j);
            }
            return Matrix.of(z, n, m);
        });
        MAPPING.set("filtered.vstates", Matrix.class, source -> {
            int n = source.data.length(), m = source.ssf.getStateDim();
            double[] z = new double[n * m];
            StateStorage fStates = source.getFilteredStates();
            for (int i = 0, j = 0; i < m; ++i, j += n) {
                fStates.getComponentVariance(i).copyTo(z, j);
            }
            return Matrix.of(z, n, m);
        });

    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    public static final InformationMapping<SsfUcarimaEstimation> getMapping() {
        return MAPPING;
    }

}
