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
package demetra.xml;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.utilities.StringFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 */
public class XmlDayAdapter extends XmlAdapter<String, Day> {

    @Override
    public Day unmarshal(String v) throws Exception {
        return StringFormatter.convertDay(v);
    }

    @Override
    public String marshal(Day v) throws Exception {
        return StringFormatter.convert((Day) v);
    }

}
