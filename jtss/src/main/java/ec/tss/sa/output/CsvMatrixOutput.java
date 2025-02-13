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

import ec.satoolkit.ISaSpecification;
import ec.tss.formatters.CsvInformationFormatter;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IOutput;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.utilities.NamedObject;
import ec.tstoolkit.utilities.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CsvMatrixOutput implements IOutput<SaDocument<ISaSpecification>> {

    CsvMatrixOutputConfiguration config_;
    List<NamedObject<IProcResults>> infos_;
    private File folder_;
    private boolean fullName;

    public CsvMatrixOutput(CsvMatrixOutputConfiguration config) {
        config_ = (CsvMatrixOutputConfiguration) config.clone();
        this.fullName = config_.isFullName();
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
        infos_ = new ArrayList<>();
        folder_ = BasicConfiguration.folderFromContext(config_.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        CsvInformationFormatter fmt = new CsvInformationFormatter();
        fmt.setFullName(fullName);
        String file = Paths.concatenate(folder_.getAbsolutePath(), config_.getFileName());
        file = Paths.changeExtension(file, "csv");
        try (Writer writer = Files.newBufferedWriter(java.nio.file.Paths.get(file), StandardCharsets.ISO_8859_1)) {
            fmt.formatResults(writer, infos_, config_.getItems(), true);
        }
        infos_ = null;
    }

    @Override
    public void process(SaDocument<ISaSpecification> document) {
        infos_.add(new NamedObject<>(document.getInput().getRawName(), document.getResults()));
    }
}
