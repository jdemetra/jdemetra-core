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

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tss.xml.DummyAdapter;
import ec.tss.xml.XmlConverterAdapter;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlInformation.RNAME)
@XmlType(name = XmlInformation.NAME)
public class XmlInformation {

    static final String NAME = "informationType";
    static final String RNAME = "information";
    static final HashMap<Type, XmlAdapter> fromXmlMap = new HashMap<>();
    static final HashMap<Type, XmlAdapter> toXmlMap = new HashMap<>();

    // default mapping
    static {
        DummyAdapter dummy = new DummyAdapter();
        fromXmlMap.put(Double.class, dummy);
        fromXmlMap.put(Integer.class, dummy);
        fromXmlMap.put(String.class, dummy);
        fromXmlMap.put(Boolean.class, dummy);
        fromXmlMap.put(boolean[].class, dummy);
        //fromXmlMap.put(String[].class, dummy);
        toXmlMap.put(Double.class, dummy);
        toXmlMap.put(Integer.class, dummy);
        toXmlMap.put(String.class, dummy);
        toXmlMap.put(Boolean.class, dummy);
        toXmlMap.put(boolean[].class, dummy);
        //toXmlMap.put(String[].class, dummy);

        XmlConverterAdapter<XmlStrings, String[]> strMapper =
                new XmlConverterAdapter<>(XmlStrings.class);
        fromXmlMap.put(XmlStrings.class, strMapper);
        toXmlMap.put(String[].class, strMapper);

        XmlConverterAdapter<XmlDoubles, double[]> doublesMapper =
                new XmlConverterAdapter<>(XmlDoubles.class);
        fromXmlMap.put(XmlDoubles.class, doublesMapper);
        toXmlMap.put(double[].class, doublesMapper);

        XmlConverterAdapter<XmlIntegers, int[]> intsMapper =
                new XmlConverterAdapter<>(XmlIntegers.class);
        fromXmlMap.put(XmlIntegers.class, intsMapper);
        toXmlMap.put(int[].class, intsMapper);

        XmlConverterAdapter<XmlInformationSet, InformationSet> infoMapper =
                new XmlConverterAdapter<>(XmlInformationSet.class);
        fromXmlMap.put(XmlInformationSet.class, infoMapper);
        toXmlMap.put(InformationSet.class, infoMapper);

        XmlConverterAdapter<XmlAlgorithm, AlgorithmDescriptor> algMapper =
                new XmlConverterAdapter<>(XmlAlgorithm.class);

        fromXmlMap.put(XmlAlgorithm.class, algMapper);
        toXmlMap.put(AlgorithmDescriptor.class, algMapper);

        fromXmlMap.put(XmlTsData.class, XmlTsData.getAdapter());
        toXmlMap.put(TsData.class, XmlTsData.getAdapter());

        fromXmlMap.put(XmlPeriodSelection.class, XmlPeriodSelection.getAdapter());
        toXmlMap.put(TsPeriodSelector.class, XmlPeriodSelection.getAdapter());

        fromXmlMap.put(XmlParameters.class, XmlParameters.getAdapter());
        toXmlMap.put(Parameter[].class, XmlParameters.getAdapter());

        fromXmlMap.put(XmlParameter.class, XmlParameter.getAdapter());
        toXmlMap.put(Parameter.class, XmlParameter.getAdapter());

        fromXmlMap.put(XmlTsMoniker.class, XmlTsMoniker.getAdapter());
        toXmlMap.put(TsMoniker.class, XmlTsMoniker.getAdapter());

        fromXmlMap.put(XmlTs.class, XmlTs.getTsAdapter());
        toXmlMap.put(TsInformation.class, XmlTs.getTsAdapter());
        
        fromXmlMap.put(XmlTsCollection.class, XmlTsCollection.getTsAdapter());
        toXmlMap.put(TsCollectionInformation.class, XmlTsCollection.getTsAdapter());

        XmlConverterAdapter<XmlRegItem, RegressionItem> regitemMapper =
                new XmlConverterAdapter<>(XmlRegItem.class);

        fromXmlMap.put(XmlRegItem.class, regitemMapper);
        toXmlMap.put(RegressionItem.class, regitemMapper);

        XmlConverterAdapter<XmlStatisticalTest, StatisticalTest> statsMapper =
                new XmlConverterAdapter<>(XmlStatisticalTest.class);

        fromXmlMap.put(XmlStatisticalTest.class, statsMapper);
        toXmlMap.put(StatisticalTest.class, statsMapper);
        
        fromXmlMap.put(XmlMatrix.class, XmlMatrix.getAdapter());
        toXmlMap.put(Matrix.class, XmlMatrix.getAdapter());
        
    }
    
    @XmlAttribute
    public String name;
    @XmlElements(value = {
        @XmlElement(type = Boolean.class, name = "Boolean"),
        @XmlElement(type = Double.class, name = "Double"),
        @XmlElement(type = Integer.class, name = "Integer"),
        @XmlElement(type = String.class, name = "String"),
        @XmlElement(type = XmlIntegers.class, name = "Integers"),
        @XmlElement(type = XmlDoubles.class, name = "Doubles"),
        @XmlElement(type = boolean[].class, name = "Booleans"),
        @XmlElement(type = XmlStrings.class, name = "Strings"),
        @XmlElement(type = XmlInformationSet.class, name = "Subset"),
        @XmlElement(type = XmlAlgorithm.class, name = "Method"),
        @XmlElement(type = XmlTsData.class, name = "TsData"),
        @XmlElement(type = XmlTs.class, name = "Ts"),
        @XmlElement(type = XmlTsCollection.class, name = "TsCollection"),
        @XmlElement(type = XmlStatisticalTest.class, name = "StatisticalTest"),
        @XmlElement(type = XmlRegItem.class, name = "RegressionItem"),
        @XmlElement(type = XmlParameter.class, name = "Parameter"),
        @XmlElement(type = XmlParameters.class, name = "Parameters"),       
        @XmlElement(type = XmlMatrix.class, name = "Matrix")})
    Object information;

    public Information<Object> toInformation() throws Exception {
        if (information == null) {
            return null;
        }
        XmlAdapter mapper = fromXmlMap.get(information.getClass());
        if (mapper == null) {
            return null;
        } else {
            return new Information<>(name, mapper.unmarshal(information));
        }
    }

    public XmlInformation() {
    }

    public XmlInformation(String name, Object value) {
        this.name = name;
        this.information = value;
    }

    public static <T> XmlInformation create(Information<T> info) throws Exception {
        XmlAdapter mapper = toXmlMap.get(info.value.getClass());
        if (mapper != null) {
            return new XmlInformation(info.name, mapper.marshal(info.value));
        } else if (info.value.getClass().isEnum()) {
            return new XmlInformation(info.name, info.value.toString());
        } else {
            return null;
        }
    }
}
