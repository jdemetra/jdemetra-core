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
import ec.tss.xml.sa.AbstractXmlSaSpecification;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.StringWriter;
import java.util.*;

/**
 *
 * @author Kristof Bayens
 */
public class DefaultSummary {
    private LinkedHashMap<String, TsData> series_=new LinkedHashMap<>();
    private String name_;
    private String xmlmodel_;

    public DefaultSummary(String name, IProcResults results, List<String> items) {
        name_ = name;
        if (results != null) {
            items.forEach(o -> series_.put(o, results.getData(o, TsData.class)));
        }
    }

    public void setModel(ISaSpecification spec) {
        AbstractXmlSaSpecification xspec = AbstractXmlSaSpecification.create(spec);
        if (xspec != null) {
            StringWriter writer = new StringWriter();
            xspec.serialize(writer);
            writer.flush();
            xmlmodel_ = writer.toString();
        }
    }

    public TsData getSeries(String item) {
        return series_.get(item);
    }
    
    public Map<String, TsData> getAllSeries(){
        return Collections.unmodifiableMap(series_);
    }

    public String getName() {
        return name_;
    }

    public String getXmlModel() {
        return xmlmodel_;
    }
    public void setXmlModel(String value) {
        xmlmodel_ = value;
    }
}
