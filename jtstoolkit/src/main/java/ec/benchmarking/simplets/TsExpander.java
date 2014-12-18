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
package ec.benchmarking.simplets;

import ec.benchmarking.DisaggregationModel;
import ec.benchmarking.DisaggregationType;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.arima.SsfAr1;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.ssf.arima.SsfRwAr1;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.Constant;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * Expands a time series to a higher frequency without external information This
 * class encapsulates functions implemented in previous Excel add-ins. The model
 * is estimated by means of DK smoothing method, with exact initialization
 * procedure. The coefficients of the regression are considered as fixed but
 * unknown.
 *
 * @author Jean Palate
 */
// / <summary>
// / </summary>
@Development(status = Development.Status.Preliminary)
public class TsExpander {

    /**
     *
     */
    public static enum Model {

        AR1,
        /**
         *
         */
        I1,
        /**
         *
         */
        I1AR1,
        /**
         *
         */
        I2,
        /**
         *
         */
        I3
    }
    private double param_ = 0;
    private boolean cnt_ = false, trend_ = false, estimate_ = true;
    private TsAggregationType type_ = TsAggregationType.Sum;
    private Model model_ = Model.I1;

    /**
     *
     */
    public TsExpander() {
    }

    /**
     *
     * @param value
     */
    public void estimateParameter(boolean value) {
        estimate_ = value;
    }

    /**
     *
     * @param s
     * @param ndom
     * @return
     */
    public TsData expand(TsData s, TsDomain ndom) {

        DisaggregationModel model = new DisaggregationModel(ndom.getFrequency());
        if (cnt_) {
            if (model_ == Model.AR1) {
                model.getX().add(new Constant());
            }
        }
        if (trend_) {
            if (model_ == Model.AR1 || model_ == Model.I1
                    || model_ == Model.I1AR1) {
                model.getX().add(new LinearTrend(ndom.getStart().firstday()));
            }
        }

        model.setDisaggregationType(DisaggregationType.Level);
        model.setAggregationType(type_);
        model.setY(s);

        // build the model and the mapper
        switch (model_) {
            case AR1: {
                TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
                disagg.setSsf(new SsfAr1(param_));
                if (estimate_) {
                    disagg.setMapping(new SsfAr1.Mapping(false));
                }
                if (disagg.process(model, ndom)) {
                    if (estimate_) {
                        param_ = disagg.getEstimatedSsf().getRho();
                    }
                    return disagg.getSmoothedSeries();
                }
                break;
            }
            case I1AR1: {
                TsDisaggregation<SsfRwAr1> disagg = new TsDisaggregation<>();
                disagg.setSsf(new SsfRwAr1(param_));
                if (estimate_) {
                    disagg.setMapping(new SsfRwAr1.Mapping(false));
                }
                if (disagg.process(model, ndom)) {
                    if (estimate_) {
                        param_ = disagg.getEstimatedSsf().getRho();
                    }
                    return disagg.getSmoothedSeries();
                }

                break;
            }
            case I1: {
                TsDisaggregation<SsfRw> disagg = new TsDisaggregation<>();
                disagg.setSsf(new SsfRw());
                if (disagg.process(model, ndom)) {
                    return disagg.getSmoothedSeries();
                }
                break;
            }
            case I2: {
                SarimaSpecification spec = new SarimaSpecification();
                spec.setD(2);
                TsDisaggregation<SsfArima> disagg = new TsDisaggregation<>();
                disagg.setSsf(new SsfArima(new SarimaModel(spec)));
                disagg.useDisturbanceSmoother(false);
                if (disagg.process(model, ndom)) {
                    return disagg.getSmoothedSeries();
                }
                break;
            }
            case I3: {
                SarimaSpecification spec = new SarimaSpecification();
                spec.setD(3);
                TsDisaggregation<SsfArima> disagg = new TsDisaggregation<>();
                disagg.setSsf(new SsfArima(new SarimaModel(spec)));
                disagg.useDisturbanceSmoother(false);
                if (disagg.process(model, ndom)) {
                    return disagg.getSmoothedSeries();
                }
                break;
            }
        }

        return null;

    }

    /**
     *
     * @param s
     * @param nfreq
     * @return
     */
    public TsData expand(TsData s, TsFrequency nfreq) {
        int infreq = nfreq.intValue(), isfreq = s.getFrequency().intValue();
        if (infreq < isfreq) {
            return null;
        } else if (nfreq == s.getFrequency()) {
            return s;
        } else if (infreq % isfreq != 0) {
            return null;
        }

        TsPeriod start = s.getStart();
        int c = infreq / isfreq;
        TsPeriod nstart = new TsPeriod(nfreq, start.getYear(), start.getPosition()
                * c);

        TsDomain ndom = new TsDomain(nstart, s.getLength() * c);
        return expand(s, ndom);
    }

    /**
     *
     * @return
     */
    public Model getModel() {
        return model_;
    }

    /**
     *
     * @return
     */
    public double getParameter() {
        return param_;
    }

    /**
     *
     * @return
     */
    public TsAggregationType getType() {
        return type_;
    }

    /**
     *
     * @return
     */
    public boolean isEstimatingParameter() {
        return estimate_;
    }

    /**
     *
     * @return
     */
    public boolean isUsingConst() {
        return cnt_;
    }

    /**
     *
     * @return
     */
    public boolean isUsingTrend() {
        return trend_;
    }

    /**
     *
     * @param value
     */
    public void setModel(Model value) {
        model_ = value;
    }

    /**
     *
     * @param value
     */
    public void setParameter(double value) {
        if (value <= -1 || value >= 1) {
            throw new TsException("Invalid parameter (should be in ]-1, 1[");
        }
        param_ = value;
    }

    /**
     *
     * @param value
     */
    public void setType(TsAggregationType value) {
        type_ = value;
    }

    /**
     *
     * @param value
     */
    public void useConst(boolean value) {
        cnt_ = value;
    }

    /**
     *
     * @param value
     */
    public void useTrend(boolean value) {
        trend_ = value;
    }
}
