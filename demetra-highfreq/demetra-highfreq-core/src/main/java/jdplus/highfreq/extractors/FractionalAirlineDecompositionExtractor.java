/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.extractors;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import jdplus.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.SeriesComponent;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.math.matrices.Matrix;
import java.util.List;
import jdplus.ucarima.UcarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class FractionalAirlineDecompositionExtractor extends InformationMapping<FractionalAirlineDecomposition> {

    static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa", T_E = "t_stde", S_E = "s_stde", I_E = "i_stde",
            NCMPS = "ncmps", CMP = "cmp", CMP_E = "cmp_stde",
            UCARIMA = "ucarima", ARIMA = "arima",
            PARAMETERS = "parameters", LL = "likelihood", PCOV = "pcov", SCORE = "score";

    public double[] data(FractionalAirlineDecomposition decomp, String cmp) {
        SeriesComponent c = decomp.component(cmp);
        if (c == null) {
            return null;
        } else {
            return c.getData().toArray();
        }
    }

    public double[] sa(FractionalAirlineDecomposition decomp) {
        SeriesComponent n = decomp.component("n");
        if (n != null) {
            return n.getData().toArray();
        }
        SeriesComponent s = decomp.component("s");
        if (s != null) {
            return DoublesMath.subtract(decomp.getY(), s.getData()).toArray();
        }

        List<SeriesComponent> all = decomp.getComponents();
        // first component is the trend
        // last is the irregular, except if the model is noisy

        DoubleSeq cur = all.get(0).getData();
        if (!isNoisy(decomp)) {
            cur = DoublesMath.add(cur, all.get(all.size() - 1).getData());
        }
        return cur.toArray();
    }

    public boolean isNoisy(FractionalAirlineDecomposition decomp) {
        return decomp.getComponents().size() == 1 + decomp.getModel().getPeriodicities().length;
    }

    public double[] stde(FractionalAirlineDecomposition decomp, String cmp) {
        SeriesComponent c = decomp.component(cmp);
        if (c == null) {
            return null;
        } else {
            DoubleSeq e = c.getStde();
            if (!e.isEmpty()) {
                return e.toArray();
            } else {
                return null;
            }
        }
    }

    public FractionalAirlineDecompositionExtractor() {
        delegate(LL, LikelihoodStatistics.class, r -> r.getLikelihood());
        set(PCOV, Matrix.class, source -> source.getParametersCovariance());
        set(PARAMETERS, double[].class, source -> source.getParameters().toArray());
        set(SCORE, double[].class, source -> source.getScore().toArray());
        set(Y, double[].class, source -> source.getY().toArray());
        set(T, double[].class, source -> data(source, "t"));
        set(S, double[].class, source -> data(source, "s"));
        set(I, double[].class, source -> data(source, "i"));
        set(SA, double[].class, source -> sa(source));
        set(T_E, double[].class, source -> stde(source, "t"));
        set(S_E, double[].class, source -> stde(source, "s"));
        set(I_E, double[].class, source -> stde(source, "i"));
        set(NCMPS, Integer.class, source -> source.getComponents().size());
        setArray(CMP, 1, 10, double[].class, (source, i)->data(source, "cmp"+i));
        setArray(CMP_E, 1, 10, double[].class, (source, i)->stde(source, "cmp"+i));
        delegate(UCARIMA, UcarimaModel.class, source -> source.getUcarima());
    }

    @Override
    public Class getSourceClass() {
        return FractionalAirlineDecomposition.class;
    }
}
