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
package ec.tstoolkit.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jean Palate
 */
public class HttpPox<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPox.class);

    public HttpPox(String url, Class<S> sclass, Class<T> tclass) throws MalformedURLException {
        m_url = new URL(url);
        try {
            scontext_ = javax.xml.bind.JAXBContext.newInstance(sclass);
            if (sclass != tclass) {
                tcontext_ = javax.xml.bind.JAXBContext.newInstance(tclass);
            } else {
                tcontext_ = scontext_;
            }

        } catch (JAXBException ex) {
            LoggerFactory.getLogger(HttpPox.class).error("", ex);
        }

    }
    final URL m_url;
    private HttpURLConnection request_;
    private javax.xml.bind.JAXBContext scontext_, tcontext_;

    public T processXmlMessage(S source) throws IOException {
        if (m_url == null) {
            return null;
        }
        synchronized (m_url) {

            request_ = (HttpURLConnection) m_url.openConnection();
            request_.setRequestMethod("POST");
            request_.setDoInput(true);
            request_.setDoOutput(true);
            request_.addRequestProperty("Content-Type", "application/xml");
            try (OutputStream ostream = request_.getOutputStream()) {
                javax.xml.bind.Marshaller marshaller = scontext_.createMarshaller();
                marshaller.marshal(source, ostream);
            } catch (JAXBException ex) {
                LOGGER.error("While processing xml message", ex);
                return null;
            }

            String response = request_.getResponseMessage();
            try (InputStream istream = request_.getInputStream()) {
                javax.xml.bind.Unmarshaller unmarshaller = tcontext_.createUnmarshaller();
                return (T) unmarshaller.unmarshal(istream);
            } catch (JAXBException ex) {
                LOGGER.error("While processing xml message", ex);
                return null;
            }
        }
    }
}
