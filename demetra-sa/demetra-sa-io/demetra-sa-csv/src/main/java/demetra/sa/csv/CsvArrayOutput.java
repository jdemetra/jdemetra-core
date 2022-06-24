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

import demetra.information.formatters.StringFormatter;
import demetra.processing.Output;
import demetra.sa.SaDocument;
import demetra.timeseries.TsData;
import demetra.util.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Kristof Bayens
 */
public class CsvArrayOutput implements Output<SaDocument> {

    private final CsvArrayOutputConfiguration config;
    private DefaultArraysSummary summary;
    private File folder;

    public CsvArrayOutput(CsvArrayOutputConfiguration config) {
        this.config = (CsvArrayOutputConfiguration) config.clone();
    }

    @Override
    public void process(SaDocument document) {
        List<String> series = config.getArrays();
        summary.add(series.toArray(String[]::new), document);
    }

    @Override
    public void start(Object context) {
        summary = new DefaultArraysSummary();
        folder = Paths.folderFromContext(config.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {

        for (String item : summary.getItems()) {
            List<DoubleArray> arrays = summary.getArrays(item);
            Optional<DoubleArray> any = arrays.stream().filter(s -> s != null && !s.isEmpty()).findAny();
            if (any.isPresent()) {
                String nfile = config.getFilePrefix();
                String c = item.replaceAll("[?*.]", "_");
                nfile += "_" + StringFormatter.cleanup(c);
                nfile = Paths.changeExtension(nfile, "csv");
                write(new File(Paths.folder(folder), nfile), summary.getNames(), arrays);
            }
        }
        summary = null;
    }

    @Override
    public String getName() {
        return "Csv Arrays";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void write(File file, List<String> names, List<DoubleArray> s) throws Exception {
        try ( FileOutputStream matrix = new FileOutputStream(file)) {
            try ( OutputStreamWriter writer = new OutputStreamWriter(matrix, StandardCharsets.ISO_8859_1)) {
                ArraysCsvFormatter fmt = new ArraysCsvFormatter();
                fmt.setFullName(config.isFullName());
                fmt.setPresentation(config.getPresentation());
                fmt.write(s, names, writer);
            }
        }
    }
}
