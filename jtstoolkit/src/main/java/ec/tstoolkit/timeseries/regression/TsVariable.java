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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationConverter;
import ec.tstoolkit.information.InformationSet;
import static ec.tstoolkit.timeseries.regression.TsVariables.LINKER;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsVariable extends AbstractSingleTsVariable implements
        IUserSource {

    private String desc_ = "";
    private TsData tsdata_;

    /**
     *
     * @param tsdata
     */
    public TsVariable(TsData tsdata) {
        tsdata_ = tsdata;
    }

    /**
     *
     * @param desc
     * @param tsdata
     */
    public TsVariable(String desc, TsData tsdata) {
        desc_ = desc;
        tsdata_ = tsdata;
    }

    /**
     *
     * @param start
     * @param data
     */
    @Override
    public void data(TsPeriod start, DataBlock data) {
        if (tsdata_ == null) {
            // should data be cleared?
            return;
        }
        TsDomain domain = tsdata_.getDomain();
        // position of the first data (in m_ts)
        int istart = start.minus(domain.getStart());
        // position of the last data (excluded)
        int iend = istart + data.getLength();

        // indexes in data
        int jstart = 0, jend = data.getLength();
        // not enough data at the beginning
        if (istart < 0) {
            data.range(0, -istart).set(0);
            jstart = -istart;
            istart = 0;
        }
        // not enough data at the end
        int n = domain.getLength();
        if (iend > n) {
            data.range(jend - iend + n, jend).set(0);
            jend = jend - iend + n;
            iend = n;
        }

        data.range(jstart, jend).copy(
                new DataBlock(tsdata_.internalStorage(), istart,
                        iend, 1));
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return tsdata_ != null ? tsdata_.getFrequency() : null;
    }

    @Override
    public String getDescription(TsFrequency context) {
        return desc_;
    }

    /**
     *
     * @return
     */
    @Override
    public TsDomain getDefinitionDomain() {
        return tsdata_ != null ? tsdata_.getDomain() : null;
    }

    /**
     *
     * @return
     */
    public TsData getTsData() {
        return tsdata_;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        if (tsdata_ == null) {
            return false;
        }
        if (domain.getFrequency() != tsdata_.getFrequency()) {
            return false;
        }
        TsDomain idom = domain.intersection(tsdata_.getDomain());
        if (idom.isEmpty()) {
            return false;
        }
        int start = idom.getStart().minus(tsdata_.getStart());
        return !new DataBlock(tsdata_.internalStorage(), start,
                start + idom.getLength(), 1).isZero();
    }

    /**
     *
     * @param desc
     */
    protected void setDescription(String desc) {
        desc_ = desc;
    }

    protected void setData(TsData s) {
        tsdata_ = s;
    }

    private static class TsVariableConverter implements InformationConverter<TsVariable> {

        @Override
        public TsVariable decode(InformationSet info) {
            TsData data = info.get(DATA, TsData.class);
            String desc = info.get(DESC, String.class);
            return new TsVariable(desc, data);
        }

        @Override
        public InformationSet encode(TsVariable t, boolean verbose) {
            InformationSet info = new InformationSet();
            info.set(DATA, t.getTsData());
            info.set(DESC, t.getDescription());
            return info;
        }

        @Override
        public Class<TsVariable> getInformationType() {
            return TsVariable.class;
        }

        @Override
        public String getTypeDescription() {
            return TYPE;
        }
        static final String TYPE = "static time series", DATA = "data", DESC = "description";
    };

    private static final InformationConverter<TsVariable> tsvar = new TsVariableConverter();

    public static void register() {
        LINKER.register(TsVariableConverter.TYPE, TsVariable.class, tsvar);
    }
}
