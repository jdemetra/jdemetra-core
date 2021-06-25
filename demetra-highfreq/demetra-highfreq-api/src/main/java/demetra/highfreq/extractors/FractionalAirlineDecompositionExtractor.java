/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.extractors;

import demetra.arima.UcarimaModel;
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import demetra.toolkit.extractors.UcarimaExtractor;
import java.util.LinkedHashMap;
import java.util.Map;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class FractionalAirlineDecompositionExtractor extends InformationMapping<FractionalAirlineDecomposition>{

    static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa", T_E = "t_stde", S_E = "s_stde", I_E = "i_stde",
            UCARIMA = "ucarima", ARIMA = "arima",
            PARAMETERS = "parameters", LL = "likelihood", PCOV = "pcov", SCORE = "score";

   public FractionalAirlineDecompositionExtractor() {
        delegate(LL, LikelihoodStatistics.class, r -> r.getLikelihood());
        set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
        set(PARAMETERS, double[].class, source -> source.getParameters().toArray());
        set(SCORE, double[].class, source -> source.getScore().toArray());
        set(Y, double[].class, source -> source.getY());
        set(T, double[].class, source -> source.getT());
        set(S, double[].class, source -> source.getS());
        set(I, double[].class, source -> source.getI());
        set(SA, double[].class, source -> source.getN() != null ? source.getN() : source.getSa());
        set(T_E, double[].class, source -> source.getStdeT());
        set(S_E, double[].class, source -> source.getStdeS());
        set(I_E, double[].class, source -> source.getStdeI());
        delegate(UCARIMA, UcarimaModel.class, source -> source.getUcarima());
    }

    @Override
    public Class getSourceClass() {
        return FractionalAirlineDecomposition.class;
    }
}
