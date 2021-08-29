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

import demetra.information.Explorable;
import demetra.information.formatters.CsvInformationFormatter;
import demetra.processing.Output;
import demetra.sa.SaDocument;
import demetra.util.NamedObject;
import demetra.util.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CsvMatrixOutput implements Output<SaDocument> {

    CsvMatrixOutputConfiguration config;
    List<NamedObject<Explorable>> infos;
    private File folder;
    private boolean fullName;

    public CsvMatrixOutput(CsvMatrixOutputConfiguration config) {
        this.config = (CsvMatrixOutputConfiguration) config.clone();
        this.fullName = this.config.isFullName();
    }

    @Override
    public String getName() {
        return "Csv matrix";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void start(Object context) {
        infos = new ArrayList<>();
        folder = Paths.folderFromContext(config.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        String file = Paths.concatenate(folder.getAbsolutePath(), config.getFileName());
        file = Paths.changeExtension(file, "csv");
        FileOutputStream matrix = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(matrix, StandardCharsets.ISO_8859_1);
        CsvInformationFormatter.formatResults(writer, infos, config.getItems(), true, fullName);
        infos = null;
    }

    @Override
    public void process(SaDocument document) {
        infos.add(new NamedObject<>(document.getName(), document.getResults()));
    }
}
