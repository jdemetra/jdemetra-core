/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.math.Complex;
import jdplus.seats.SeatsTests;
import jdplus.ucarima.WienerKolmogorovDiagnostics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class SeatsDiagnosticsExtractor extends InformationMapping<SeatsTests> {

    public static final String CUTOFF = "parameters_cutoff", CHANGED = "model_changed", SEAS = "seasonality", AR_ROOT = "ar_root", MA_ROOT = "ma_root";
    public static final String TVAR_ESTIMATE = "tvar-estimate",
            TVAR_ESTIMATOR = "tvar-estimator",
            TVAR_PVALUE = "tvar-pvalue",
            SAVAR_ESTIMATE = "savar-estimate",
            SAVAR_ESTIMATOR = "savar-estimator",
            SAVAR_PVALUE = "savar-pvalue",
            SVAR_ESTIMATE = "svar-estimate",
            SVAR_ESTIMATOR = "svar-estimator",
            SVAR_PVALUE = "svar-pvalue",
            IVAR_ESTIMATE = "ivar-estimate",
            IVAR_ESTIMATOR = "ivar-estimator",
            IVAR_PVALUE = "ivar-pvalue",
            SAAC1_ESTIMATE = "saac1-estimate",
            SAAC1_ESTIMATOR = "saac1-estimator",
            SAAC1_PVALUE = "saac1-pvalue",
            IAC1_ESTIMATE = "iac1-estimate",
            IAC1_ESTIMATOR = "iac1-estimator",
            IAC1_PVALUE = "iac1-pvalue",
            TICORR_ESTIMATE = "ticorr-estimate",
            TICORR_ESTIMATOR = "ticorr-estimator",
            TICORR_PVALUE = "ticorr-pvalue",
            SICORR_ESTIMATE = "sicorr-estimate",
            SICORR_ESTIMATOR = "sicorr-estimator",
            SICORR_PVALUE = "sicorr-pvalue",
            TSCORR_ESTIMATE = "tscorr-estimate",
            TSCORR_ESTIMATOR = "tscorr-estimator",
            TSCORR_PVALUE = "tscorr-pvalue";

    private static final int T_CMP = 0, SA_CMP = 1, I_CMP = 3, S_CMP = 2;

    public SeatsDiagnosticsExtractor() {
        set(CUTOFF, Boolean.class, source -> source.isParametersCutOff());
        set(CHANGED, Boolean.class, source -> source.isModelChanged());
        set(SEAS, Boolean.class, source -> !source.ucarimaModel().getComponent(1).isNull());
        setArray(AR_ROOT, 1, 3, Complex.class, (source, i) -> {
            Complex[] ar = source.finalModel().getRegularAR().roots();
            if (i > ar.length) {
                return null;
            } else {
                return ar[i - 1].inv();
            }
        });
        setArray(MA_ROOT, 1, 3, Complex.class, (source, i) -> {
            Complex[] ma = source.finalModel().getRegularMA().roots();
            if (i > ma.length) {
                return null;
            } else {
                return ma[i - 1].inv();
            }
        });

        set(TVAR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorVariance(T_CMP);
        });
        set(TVAR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateVariance(T_CMP);
        });
        set(TVAR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(T_CMP);
        });
        set(SAVAR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorVariance(SA_CMP);
        });
        set(SAVAR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateVariance(SA_CMP);
        });
        set(SAVAR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(SA_CMP);
        });
        set(SVAR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorVariance(S_CMP);
        });
        set(SVAR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateVariance(S_CMP);
        });
        set(SVAR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(S_CMP);
        });
        set(IVAR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorVariance(I_CMP);
        });
        set(IVAR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateVariance(I_CMP);
        });
        set(IVAR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(I_CMP);
        });
        set(TSCORR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorCrossCorrelation(T_CMP, S_CMP);
        });
        set(TSCORR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateCrossCorrelation(T_CMP, S_CMP);
        });
        set(TSCORR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(T_CMP, S_CMP);
        });
        set(TICORR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorCrossCorrelation(T_CMP, I_CMP);
        });
        set(TICORR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateCrossCorrelation(T_CMP, I_CMP);
        });
        set(TICORR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(T_CMP, I_CMP);
        });
        set(SICORR_ESTIMATOR, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimatorCrossCorrelation(S_CMP, I_CMP);
        });
        set(SICORR_ESTIMATE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getEstimateCrossCorrelation(S_CMP, I_CMP);
        });
        set(SICORR_PVALUE, Double.class, source
                -> {
            WienerKolmogorovDiagnostics diag = source.wkDiagnostics();
            return diag == null ? null : diag.getPValue(S_CMP, I_CMP);
        });
    }

    @Override
    public Class<SeatsTests> getSourceClass() {
        return SeatsTests.class;
    }

}
