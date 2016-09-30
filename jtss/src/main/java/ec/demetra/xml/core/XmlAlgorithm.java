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

package ec.demetra.xml.core;
import ec.tss.xml.*;

import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Jean Palate
 */
//@XmlRootElement(name = XmlAlgorithm.RNAME)
@XmlType(name = XmlAlgorithm.NAME)
public class XmlAlgorithm implements IXmlConverter<AlgorithmDescriptor> {
    static final String NAME = "AlgorithmDescriptorType";
    static final String RNAME = "AlgorithmDescriptor";

    /**
     *
     */
    @XmlAttribute
    public String version;

    /**
     *
     */
    @XmlElement(name="Name")
    public String name;

    /**
     *
     */
    @XmlElement(name="Family")
    public String family;
    /**
     * 
     * @param alg
     */
    @Override
    public void copy(AlgorithmDescriptor alg)
    {
        family=alg.family;
	name = alg.name;
	version = alg.version;
    }

    @Override
    public AlgorithmDescriptor create() {
        return new AlgorithmDescriptor(family, name, version);
    }

 }
