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
package ec.demetra.xml.regression;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlRegression.RNAME)
@XmlType(name = XmlRegression.NAME)
public class XmlRegression {

    static final String RNAME = "Regression", NAME = RNAME + "Type";
    
    @XmlElement(name="Item")
    public List<XmlRegressionItem> variables=new ArrayList<>();
    
    public static List<Class> xmlClasses(){
        List<Class> xmlclvar = TsVariableAdapters.getDefault().getXmlClasses();
        List<Class> xmlclmod = TsModifierAdapters.getDefault().getXmlClasses();
        xmlclvar.addAll(xmlclmod);
        return xmlclvar;
    }
    
    public static synchronized JAXBContext context() throws JAXBException{ 
    
        List<Class> xmlClasses = xmlClasses();
        xmlClasses.add(XmlRegression.class);
        JAXBContext jaxb = JAXBContext.newInstance(xmlClasses.toArray(new Class[xmlClasses.size()]));
        return jaxb;
    }
}
