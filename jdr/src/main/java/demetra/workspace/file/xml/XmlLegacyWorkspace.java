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
package demetra.workspace.file.xml;

import ec.tstoolkit.modelling.arima.Method;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlLegacyWorkspace.RNAME)
@XmlType(name = XmlLegacyWorkspace.NAME)
public final class XmlLegacyWorkspace {

    static final String NAME = "demetraWorkspaceType";
    static final String RNAME = "demetraWorkspace";

    @XmlElementWrapper()
    @XmlElement(name = "tramoseatsSpec")
    public XmlLegacyWorkspaceItem[] tramoseatsSpecs;

    @XmlElementWrapper()
    @XmlElement(name = "x12Spec")
    public XmlLegacyWorkspaceItem[] x12Specs;

    @XmlElementWrapper()
    @XmlElement(name = "tramoseatsDoc")
    public XmlLegacyWorkspaceItem[] tramoseatsDocs;

    @XmlElementWrapper()
    @XmlElement(name = "x12Doc")
    public XmlLegacyWorkspaceItem[] x12Docs;

    @XmlElementWrapper()
    @XmlElement(name = "processing")
    public XmlLegacyWorkspaceItem[] saProcessing;

    @XmlElement
    public XmlLegacyWorkspaceItem calendars;

    @XmlElement
    public XmlLegacyWorkspaceItem variables;

    @XmlAttribute
    public String defaultSpec;

    @XmlAttribute
    public Method defaultMethod;
}
