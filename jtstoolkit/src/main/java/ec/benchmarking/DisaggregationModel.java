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

package ec.benchmarking;

import ec.tstoolkit.data.AbsMeanNormalizer;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DisaggregationModel implements Cloneable {

    private TsFrequency defFreq_ = TsFrequency.Quarterly;
    private int nFcast_;
    private TsData y_;
    private TsVariableList regressors_ = new TsVariableList();
    private TsAggregationType aType_ = TsAggregationType.Sum;
    private DisaggregationType dType_ = DisaggregationType.Level;

    public DisaggregationModel() {
    }

    /**
     *
     * @param deffreq
     */
    public DisaggregationModel(TsFrequency deffreq) {
        defFreq_ = deffreq;
    }

    @Override
    public DisaggregationModel clone() {
        try {
            DisaggregationModel model = (DisaggregationModel) super.clone();
            if (y_ != null) {
                model.y_ = y_.clone();
            }
            if (regressors_ != null) {
                model.regressors_ = regressors_.clone();
            }
            return model;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param domain
     * @param rescale
     * @return
     */
    public DisaggregationData data(TsDomain domain, boolean rescale) {
        DisaggregationData data = startDataPreparation(domain);
        if (data == null) {
            return null;
        }
        if (!prepare(data, rescale)) {
            return null;
        }
        return data;
    }

    /**
     *
     * @return
     */
    public TsAggregationType getAggregationType() {
        return aType_;
    }

    /**
     *
     * @return
     */
    public int getDefaultForecastsCount() {
        return nFcast_;
    }

    /**
     *
     * @return
     */
    public TsFrequency getDefaultFrequency() {
        return defFreq_;
    }

    /**
     *
     * @return
     */
    public DisaggregationType getDisaggregationType() {
        return dType_;
    }

    /**
     *
     * @return
     */
    public TsVariableList getX() {
        return regressors_;
    }

    /**
     *
     * @return
     */
    public TsData getY() {
        return y_;
    }

    private boolean prepare(DisaggregationData data, boolean rescale) {
        TsDomain lDom = y_.getDomain();
        int lN = lDom.getLength(), hN = data.hDom.getLength();
        if (lN == 0 || hN == 0) {
            return false;
        }
        TsPeriod lStart = lDom.getStart();
        TsPeriod hStart = data.hDom.getStart(), hEnd = data.hDom.getLast();

        // common periods in lFreq
        TsPeriod cStart = new TsPeriod(lStart.getFrequency(), hStart);
        TsPeriod cEnd = new TsPeriod(lStart.getFrequency(), hEnd);

        // adjust for complete periods...
        if (aType_ != TsAggregationType.Last) {
            if (hStart.getPosition() % data.FrequencyRatio != 0) {
                cStart.move(1);
            }
        }
        if (aType_ != TsAggregationType.First) {
            if (hEnd.getPosition() % data.FrequencyRatio != data.FrequencyRatio - 1) {
                cEnd.move(-1);
            }
        }

        int cn = cEnd.minus(cStart) + 1;
        // common domain
        TsDomain yDom = lDom.intersection(new TsDomain(cStart, cn));
        cStart = yDom.getStart();
        int ny = yDom.getLength();
        if (ny == 0) {
            return false;
        }

        int pos = (aType_ == TsAggregationType.Last) ? data.FrequencyRatio - 1 : 0;
        hStart.set(cStart.getYear(), cStart.getPosition() * data.FrequencyRatio + pos);
        int nxe = ny * data.FrequencyRatio;
        if (aType_ == TsAggregationType.Last
                || aType_ == TsAggregationType.First) {
            nxe -= data.FrequencyRatio - 1;
        }
        data.hEDom = new TsDomain(hStart, nxe);

        prepareY(data, yDom);
        if (!regressors_.isEmpty()) {
            prepareX(data, rescale);
        } else {
            data.scale(rescale ? new AbsMeanNormalizer() : null);
        }

        return true;
    }

    private void prepareX(DisaggregationData data, boolean rescale) {
        data.hX = regressors_.all().matrix(data.hDom);

        if (rescale) {
            data.scale(new AbsMeanNormalizer());
        } else {
            data.scale(null);
        }

        if (aType_ != TsAggregationType.Average
                && aType_ != TsAggregationType.Sum) {
            data.hEX = data.hX;
        } else {
            data.hEX = data.hX.clone();
            Cumulator cumul = new Cumulator(data.FrequencyRatio);
            DataBlockIterator cX = data.hEX.columns();
            DataBlock col = cX.getData();
            do {
                cumul.transform(col);
            } while (cX.next());
        }
    }

    private void prepareY(DisaggregationData data, TsDomain yDom) {
        double[] s = y_.internalStorage();
        if (dType_ != DisaggregationType.Level) {
            for (int i = 0; i < s.length; ++i) {
                s[i] = Math.log(s[i]);
            }
            if (aType_ == TsAggregationType.Sum) {
                double lc = data.FrequencyRatio * Math.log(data.FrequencyRatio);
                for (int i = 0; i < s.length; ++i) {
                    s[i] = s[i] * data.FrequencyRatio - lc;
                }
            }
        }

        int ny = yDom.getLength();
        int pos;
        if (aType_ == TsAggregationType.First
                || aType_ == TsAggregationType.Last) {
            pos = 0;
        } else {
            pos = data.FrequencyRatio - 1;
        }
        double[] y = new double[data.hDom.getLength()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = Double.NaN;
        }

        int xstart = data.hEDom.getStart().minus(data.hDom.getStart()), ystart = yDom.getStart().minus(y_.getStart());
        for (int i = 0, j = xstart + pos, k = ystart; i < ny; ++i, j += data.FrequencyRatio, ++k) {
            y[j] = s[k];
        }
        data.hY = y;
    }

    /**
     *
     * @param value
     */
    public void setAggregationType(TsAggregationType value) {
        aType_ = value;
    }

    /**
     *
     * @param value
     */
    public void setDefaultForecastCount(int value) {
        nFcast_ = value;
    }

    /**
     *
     * @param value
     */
    public void setDisaggregationType(DisaggregationType value) {
        dType_ = value;
    }

    /**
     *
     * @param value
     */
    public void setX(TsVariableList value) {
        regressors_ = value.clone();
    }

    /**
     *
     * @param value
     */
    public void setY(TsData value) {
        y_ = value.clone();
    }

    private DisaggregationData startDataPreparation(TsDomain domain) {
        if (y_ == null) {
            return null;
        }
        TsDomain lDom = y_.getDomain(), hDom = regressors_.getDomain();
        int lFreq = lDom.getFrequency().intValue(), hFreq = regressors_.getFrequency().intValue();
        if (hFreq == 0) {
            hFreq = defFreq_.intValue();
        }
        if (lFreq >= hFreq || hFreq % lFreq != 0) {
            return null;
        }
        int c = hFreq / lFreq;
        if (hDom == null && domain == null) {
            // creates a new domain that correspond to the ldom
            TsPeriod lstart = lDom.get(0);
            TsPeriod hstart = new TsPeriod(TsFrequency.valueOf(hFreq));
            hstart.set(lstart.getYear(), lstart.getPosition() * c);
            hDom = new TsDomain(hstart, nFcast_ + lDom.getLength() * c);
        } else if (hDom != null && domain != null) {
            if (hDom.getFrequency() != domain.getFrequency()) {
                return null;
            }
            hDom = hDom.intersection(domain);
            if (hDom == null || hDom.getLength() == 0) {
                return null;
            }
        } else if (hDom == null) {
            hDom = domain;
        }


        DisaggregationData data = new DisaggregationData();
        data.FrequencyRatio = c;
        data.hDom = hDom;
        //data.lDom = lDom;
        data.lowFrequency = lFreq;
        data.highFrequency = hFreq;
        return data;
    }
}
