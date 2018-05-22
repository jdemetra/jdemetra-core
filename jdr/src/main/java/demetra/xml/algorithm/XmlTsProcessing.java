/*
 * Copyright 2013-2014 National Bank of Belgium
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

import demetra.xml.XmlTsData;
import demetra.xml.information.XmlInformationSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTsProcessing.RNAME)
@XmlType(name = XmlTsProcessing.NAME)
public class XmlTsProcessing {
    static final String NAME = "tsProcessingType";
    static final String RNAME = "tsProcessing";
    
    @XmlElement
    public XmlProcessingContext context;
    @XmlElement
    public XmlTsData input;
    @XmlElement
    public XmlInformationSet specification;
    @XmlElement
    public String[] filter;

}
