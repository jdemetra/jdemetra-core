/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.highfreq.extractors.FractionalAirlineModelExtractor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.modelling.OutlierDescriptor;
import demetra.processing.ProcResults;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Low-level results. Should be refined
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class FractionalAirlineEstimation implements ProcResults{

    double[] y;
    MatrixType x;

    FractionalAirline model;

    OutlierDescriptor[] outliers;

    DoubleSeq coefficients;
    MatrixType coefficientsCovariance;

    private DoubleSeq parameters, score;
    private MatrixType parametersCovariance;

    LikelihoodStatistics likelihood;

    public double[] linearized() {

        double[] l = y.clone();
        DoubleSeqCursor acur = coefficients.cursor();
        for (int j = 0; j < x.getColumnsCount(); ++j) {
            double a = acur.getAndNext();
            if (a != 0) {
                DoubleSeqCursor cursor = x.column(j).cursor();
                for (int k = 0; k < l.length; ++k) {
                    l[k] -= a * cursor.getAndNext();
                }
            }
        }
        return l;
    }

    public double[] tstats() {
        double[] t = coefficients.toArray();
        if (t == null) {
            return null;
        }
        DoubleSeqCursor v = coefficientsCovariance.diagonal().cursor();
        for (int i = 0; i < t.length; ++i) {
            t[i] /= Math.sqrt(v.getAndNext());
        }
        return t;
    }

    @Override
    public boolean contains(String id) {
        return FractionalAirlineModelExtractor.getMapping().contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        FractionalAirlineModelExtractor.getMapping().fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return FractionalAirlineModelExtractor.getMapping().getData(this, id, tclass);
    }
    
    public static InformationMapping<FractionalAirlineEstimation> getMapping(){
        return FractionalAirlineModelExtractor.getMapping();
    }

    public int getNx() {
        return coefficients == null ? 0 : coefficients.length();
    }
}
