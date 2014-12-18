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
package ec.tss.tsproviders.sdmx.engine;

import com.google.common.base.Strings;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author Kristof Bayens
 */
public class CunningPlanFactory implements ISdmxSourceFactory {

    public static final String NAME = "Cunning plan";
    final AbstractDocumentFactory[] strategies = {new GenericDocFactory(), new GuessingCompactFactory()};

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SdmxSource create(File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        return find(doc).create(doc);
    }

    public AbstractDocumentFactory find(Document doc) throws IOException {
        for (AbstractDocumentFactory o : strategies) {
            if (o.isValid(doc)) {
                return o;
            }
        }
        throw new IOException("Cannot find a suitable SDMX strategy for '" + Strings.nullToEmpty(doc.getDocumentURI()) + "'");
    }
}
