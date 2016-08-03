/*
 * Copyright 2016 National Bank of Belgium
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
package xml;

import ec.demetra.xml.core.XmlTsData;
import java.io.InputStream;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author Jean Palate
 */
public class ResourceResolver  implements LSResourceResolver {

@Override
public LSInput resolveResource(String type, String namespaceURI,
        String publicId, String systemId, String baseURI) {

     // note: in this sample, the XSD's are expected to be in the root of the classpath
    InputStream resourceAsStream = XmlTsData.class.getClassLoader()
            .getResourceAsStream(systemId);
    return new Input(publicId, systemId, resourceAsStream);
}

 }