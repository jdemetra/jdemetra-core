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
package ec.tss.sa.output;

import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsDataTableInfo;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class TsCollectionCsvFormatter {

    private CsvLayout layout_ = CsvLayout.VTable;
    private final char comma;
    private static final String newLine = System.lineSeparator();
    private final DecimalFormat fmt;
    private final NumberFormat ifmt;
    private boolean fullName;

    public TsCollectionCsvFormatter() {
        ifmt = NumberFormat.getIntegerInstance();
        ifmt.setGroupingUsed(false);
        comma = BasicConfiguration.getCsvSeparator();
        fmt = (DecimalFormat) DecimalFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
    }

    public CsvLayout getPresentation() {
        return layout_;
    }

    public void setPresentation(CsvLayout layout) {
        layout_ = layout;
    }

    public void setFullName(boolean fullName) {
        this.fullName = fullName;
    }

    public boolean write(List<TsData> coll, List<String> names, Writer writer) throws IOException {
        if (coll.isEmpty() || names.size() != coll.size()) {
            return false;
        }
        if (layout_ == CsvLayout.List) {
            return writeList(coll, names, writer);
        }

        TsDataTable table = new TsDataTable();
        for (TsData s : coll) {
            table.insert(-1, s);
        }
        if (table.isEmpty()) {
            return false;
        }
        TsDomain domain = table.getDomain();
        int ndata = domain.getLength();
        int nseries = table.getSeriesCount();

        if (layout_ == CsvLayout.VTable) {
            writer.write(comma);
            for (int i = 0; i < nseries; ++i) {
                write(names.get(i), writer);
                if (i != nseries - 1) {
                    writer.write(comma);
                } else {
                    writer.write(newLine);
                }
            }

            for (int j = 0; j < ndata; ++j) {
                writer.write(domain.get(j).lastday().toString());
                for (int i = 0; i < nseries; ++i) {
                    writer.write(comma);
                    if (table.getDataInfo(j, i) == TsDataTableInfo.Valid) {
                        write(fmt.format(table.getData(j, i)), writer);
                    }
                }
                writer.write(newLine);
            }
        } else {
            writer.write(comma);
            for (int i = 0; i < ndata; ++i) {
                writer.write(domain.get(i).lastday().toString());
                if (i != ndata - 1) {
                    writer.write(comma);
                } else {
                    writer.write(newLine);
                }
            }

            for (int j = 0; j < nseries; ++j) {
                write(names.get(j), writer);
                for (int i = 0; i < ndata; ++i) {
                    writer.write(comma);
                    if (table.getDataInfo(i, j) == TsDataTableInfo.Valid) {
                        write(fmt.format(table.getData(i, j)), writer);
                    }
                }
                writer.write(newLine);
            }
        }
        return true;
    }

    private boolean writeList(List<TsData> coll, List<String> names, Writer writer) throws IOException {
        int nseries = names.size();
        for (int j = 0; j < nseries; ++j) {
            write(names.get(j), writer);
            writer.write(comma);
            TsData cur = coll.get(j);
            if (cur != null) {
                // header: freq, start, pos, length
                TsPeriod start = cur.getStart();
                writer.write(ifmt.format(start.getFrequency().intValue()));
                writer.write(comma);
                writer.write(ifmt.format(start.getYear()));
                writer.write(comma);
                writer.write(ifmt.format(start.getPosition() + 1));
                writer.write(comma);
                writer.write(ifmt.format(cur.getLength()));
                for (int i = 0; i < cur.getLength(); ++i) {
                    writer.write(comma);
                    double val = cur.get(i);
                    if (!Double.isNaN(val)) {
                        write(fmt.format(val), writer);
                    }
                }
            }
            writer.write(newLine);
        }
        return true;
    }

    private void write(String txt, Writer writer) throws IOException {

        if (txt == null) {
            return;
        }
        if (fullName) {
            txt = MultiLineNameUtil.join(txt, " * ");
        } else {
            txt = MultiLineNameUtil.last(txt);
        }

        if (txt.indexOf(comma) >= 0) {
            if (txt.indexOf('\"') >= 0) {
                writer.write("\"\"");
                writer.write(txt);
                writer.write("\"\"");
            } else {
                writer.write('\"');
                writer.write(txt);
                writer.write('\"');
            }
        } else {
            if (txt.indexOf('\"') >= 0) {
                writer.write("\"\"");
                writer.write(txt);
                writer.write("\"\"");
            } else {
                writer.write(txt);
            }
        }
    }
}
