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
package demetra.xml.algorithm;

import demetra.xml.IXmlConverter;
import demetra.xml.calendar.XmlCalendars;
import demetra.xml.regression.XmlTsVariables;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.regression.TsVariables;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlProcessingContext.RNAME)
@XmlType(name = XmlProcessingContext.NAME)
public class XmlProcessingContext implements IXmlConverter<ProcessingContext> {
    
    static final String NAME = "processingContextType";
    static final String RNAME = "processingContext";
    @XmlElement(name = "group")
    @XmlElementWrapper(name = "variables")
    public XmlTsVariables[] tsVariables;
    @XmlElement
    public XmlCalendars calendars;
    
    @Override
    public ProcessingContext create() {
        ProcessingContext context = new ProcessingContext();
        if (calendars != null) {
            calendars.copyTo(context.getGregorianCalendars());
        }
        if (tsVariables != null) {
            for (int i = 0; i < tsVariables.length; ++i) {
                TsVariables var = tsVariables[i].create();
                if (var != null && tsVariables[i].name != null) {
                    context.getTsVariableManagers().set(tsVariables[i].name, var);
                }
            }
        }
        return context;
    }
    
    @Override
    public void copy(ProcessingContext t) {
        calendars=new XmlCalendars();
        calendars.copy(t.getGregorianCalendars());
        int count = t.getTsVariableManagers().getCount();
        if (count > 0){
            tsVariables=new XmlTsVariables[count];
            int i=0;
            for (String n : t.getTsVariableManagers().getNames()){
                TsVariables cur = t.getTsVariables(n);
                XmlTsVariables xcur=new XmlTsVariables();
                xcur.name=n;
                xcur.copy(cur);
                tsVariables[i++]=xcur;
            }
        }
    }
}
