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
package demetra.sa.csv;

import demetra.information.formatters.BasicConfiguration;
import demetra.information.formatters.CsvInformationFormatter;
import demetra.util.MultiLineNameUtil;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class ArraysCsvFormatter {

    private CsvLayout layout = CsvLayout.List;
    private final char comma;
    private static final String NEWLINE = System.lineSeparator();
    private final DecimalFormat fmt;
    private final NumberFormat ifmt;
    private boolean fullName;

    public ArraysCsvFormatter() {
        ifmt = NumberFormat.getIntegerInstance();
        ifmt.setGroupingUsed(false);
        comma = CsvInformationFormatter.getCsvSeparator();
        fmt = (DecimalFormat) DecimalFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
    }

    public CsvLayout getPresentation() {
        return layout;
    }

    public void setPresentation(CsvLayout layout) {
        this.layout = layout;
    }

    public void setFullName(boolean fullName) {
        this.fullName = fullName;
    }

    public boolean write(List<DoubleArray> coll, List<String> names, Writer writer) throws IOException {
        if (coll.isEmpty() || names.size() != coll.size()) {
            return false;
        }
//        if (layout == CsvLayout.List) {
        return writeList(coll, names, writer);
//        }

//        TsDataTable table = TsDataTable.of(coll);
//        TsDomain domain = table.getDomain();
//        if (domain.isEmpty()) {
//            return false;
//        }
//        int ndata = domain.getLength();
//        int nseries = coll.size();
//
//        TsDataTable.Cursor cursor = table.cursor(TsDataTable.DistributionType.LAST);
//        if (layout == CsvLayout.VTable) {
//            writer.write(comma);
//            for (int i = 0; i < nseries; ++i) {
//                write(names.get(i), writer);
//                if (i != nseries - 1) {
//                    writer.write(comma);
//                } else {
//                    writer.write(NEWLINE);
//                }
//            }
//
//            for (int j = 0; j < ndata; ++j) {
//                writer.write(domain.get(j).toString());
//                for (int i = 0; i < nseries; ++i) {
//                    writer.write(comma);
//                    cursor.moveTo(j, i);
//                    if (cursor.getStatus() == TsDataTable.ValueStatus.PRESENT) {
//                        write(fmt.format(cursor.getValue()), writer);
//                    }
//                }
//                writer.write(NEWLINE);
//            }
//        } else {
//            writer.write(comma);
//            for (int i = 0; i < ndata; ++i) {
//                writer.write(domain.get(i).start().toLocalDate().format(DateTimeFormatter.ISO_DATE));
//                if (i != ndata - 1) {
//                    writer.write(comma);
//                } else {
//                    writer.write(NEWLINE);
//                }
//            }
//            for (int j = 0; j < nseries; ++j) {
//                write(names.get(j), writer);
//                for (int i = 0; i < ndata; ++i) {
//                    writer.write(comma);
//                    cursor.moveTo(i, j);
//                    if (cursor.getStatus() == TsDataTable.ValueStatus.PRESENT) {
//                        write(fmt.format(cursor.getValue()), writer);
//                    }
//                }
//                writer.write(NEWLINE);
//            }
//        }
//        return true;
    }

    private boolean writeList(List<DoubleArray> coll, List<String> names, Writer writer) throws IOException {
        int nseries = names.size();
        for (int j = 0; j < nseries; ++j) {
            write(names.get(j), writer);
            writer.write(comma);
            DoubleArray cur = coll.get(j);
            if (cur != null) {
                // header: ndim, dim
                int[] dimensions = cur.getDimensions();
                writer.write(ifmt.format(dimensions.length));
                for (int n = 0; n < dimensions.length; ++n) {
                    writer.write(comma);
                    writer.write(ifmt.format(dimensions[n]));
                }
                double[] data = cur.getData();
                for (int n = 0; n < data.length; ++n) {
                    writer.write(comma);
                    double val = data[n];
                    if (!Double.isNaN(val)) {
                        write(fmt.format(val), writer);
                    }
                }
            }
            writer.write(NEWLINE);
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
