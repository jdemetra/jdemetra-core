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

import demetra.processing.Output;
import demetra.sa.SaDocument;
import demetra.timeseries.TsData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CsvOutput implements Output<SaDocument> {

    CsvOutputConfiguration config_;
    DefaultCollectionSummary summary_;
    private File folder_;

    public CsvOutput(CsvOutputConfiguration config) {
        config_ = (CsvOutputConfiguration) config.clone();
    }

    @Override
    public void process(SaDocument document) {
//        summary_.add(Jdk6.Collections.toArray(config_.getSeries(), String.class), document);
    }

    @Override
    public void start(Object context) {
        summary_ = new DefaultCollectionSummary();
//        folder_ = BasicConfiguration.folderFromContext(config_.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        for (String item : summary_.getItems()) {
            String nfile = config_.getFilePrefix();
//            nfile += "_" + StringFormatter.cleanup(item.replace('.', '_'));
//            nfile = Paths.changeExtension(nfile, "csv");
//            write(new File(BasicConfiguration.folder(folder_), nfile), summary_.getNames(), summary_.getSeries(item));
        }
        summary_ = null;
    }

    @Override
    public String getName() {
        return "csv";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

//    private void write(File file, List<String> names, List<TsData> s) throws Exception {
//        try (FileOutputStream matrix = new FileOutputStream(file)) {
//            try (OutputStreamWriter writer = new OutputStreamWriter(matrix, StandardCharsets.ISO_8859_1)) {
//                TsCollectionCsvFormatter fmt = new TsCollectionCsvFormatter();
//                fmt.setFullName(config_.isFullName());
//                fmt.setPresentation(config_.getPresentation());
//                fmt.write(s, names, writer);
//            }
//        }
//    }
}
