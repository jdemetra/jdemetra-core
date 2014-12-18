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
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IOutput;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kristof Bayens
 */
public class TxtOutput extends BasicConfiguration implements IOutput<SaDocument<ISaSpecification>> {

    public static final Logger LOGGER = LoggerFactory.getLogger(TxtOutputFactory.class);
    private int id_;
    private TxtOutputConfiguration config_;
    private File folder;

    public TxtOutput(TxtOutputConfiguration config) {
        config_ = config.clone();
    }

    @Override
    public void process(SaDocument<ISaSpecification> document) throws Exception {
        if (document.getResults() == null) {
            return;
        }
        String name = "s" + Integer.toString(++id_);

        for (String item : config_.getSeries()) {
            TsData s = document.getResults().getData(item, TsData.class);
            if (s != null) {
                write(folder, name + '_' + item, document.getTs().getName(), s);
            }
        }
    }

    @Override
    public void start(Object context) {
        id_ = 0;
        folder = BasicConfiguration.folderFromContext(config_.getFolder(), context);
    }

    @Override
    public void end(Object file) {
    }

    @Override
    public String getName() {
        return "txt";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public static void write(File folder, String name, String sname, TsData s) throws Exception {
        if (s == null) {
            return;
        }
        File file = new File(folder, Paths.changeExtension(name, "dta"));
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(sname);
            w.write("\n");
            w.write(s.getLength());
            w.write(" ");
            w.write(s.getStart().getYear());
            w.write(" ");
            w.write(s.getStart().getPosition() + 1);
            w.write(" ");
            w.write(s.getFrequency().intValue());

            for (int i = 0; i < s.getLength(); ++i) {
                w.write("\n");
                double value = s.get(i);
                w.write(Double.toString(Double.isNaN(value) ? -99999 : value));
            }
        }
    }
}
