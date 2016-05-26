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

package ec.tss.xml.information;

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tss.xml.DummyMapper;
import ec.tss.xml.IXmlMapper;
import ec.tss.xml.XmlAlgorithm;
import ec.tss.xml.XmlConverterMapper;
import ec.tss.xml.XmlDoubles;
import ec.tss.xml.XmlIntegers;
import ec.tss.xml.XmlMatrix;
import ec.tss.xml.XmlParameter;
import ec.tss.xml.XmlParameters;
import ec.tss.xml.XmlPeriodSelection;
import ec.tss.xml.XmlRegItem;
import ec.tss.xml.XmlStatisticalTest;
import ec.tss.xml.XmlStrings;
import ec.tss.xml.XmlTs;
import ec.tss.xml.XmlTsCollection;
import ec.tss.xml.XmlTsData;
import ec.tss.xml.XmlTsMoniker;
import ec.tss.xml.regression.XmlInterventionVariable;
import ec.tss.xml.regression.XmlRamp;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlInformation.RNAME)
@XmlType(name = XmlInformation.NAME)
public class XmlInformation {

    static final String NAME = "informationType";
    static final String RNAME = "information";
    static final HashMap<Type, IXmlMapper> fromXmlMap = new HashMap<>();
    static final HashMap<Type, IXmlMapper> toXmlMap = new HashMap<>();

    // default mapping
    static {
        DummyMapper dummy = new DummyMapper();
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

        XmlConverterMapper<String[], XmlStrings> strMapper =
                new XmlConverterMapper<>(XmlStrings.class);
        fromXmlMap.put(XmlStrings.class, strMapper);
        toXmlMap.put(String[].class, strMapper);

        XmlConverterMapper<double[], XmlDoubles> doublesMapper =
                new XmlConverterMapper<>(XmlDoubles.class);
        fromXmlMap.put(XmlDoubles.class, doublesMapper);
        toXmlMap.put(double[].class, doublesMapper);

        XmlConverterMapper<int[], XmlIntegers> intsMapper =
                new XmlConverterMapper<>(XmlIntegers.class);
        fromXmlMap.put(XmlIntegers.class, intsMapper);
        toXmlMap.put(int[].class, intsMapper);

        XmlConverterMapper<InformationSet, XmlInformationSet> infoMapper =
                new XmlConverterMapper<>(XmlInformationSet.class);
        fromXmlMap.put(XmlInformationSet.class, infoMapper);
        toXmlMap.put(InformationSet.class, infoMapper);

        XmlConverterMapper<AlgorithmDescriptor, XmlAlgorithm> algMapper =
                new XmlConverterMapper<>(XmlAlgorithm.class);

        fromXmlMap.put(XmlAlgorithm.class, algMapper);
        toXmlMap.put(AlgorithmDescriptor.class, algMapper);

        XmlConverterMapper<TsData, XmlTsData> tsdataMapper =
                new XmlConverterMapper<>(XmlTsData.class);

        fromXmlMap.put(XmlTsData.class, tsdataMapper);
        toXmlMap.put(TsData.class, tsdataMapper);

        XmlConverterMapper<TsPeriodSelector, XmlPeriodSelection> pselMapper =
                new XmlConverterMapper<>(XmlPeriodSelection.class);

        fromXmlMap.put(XmlPeriodSelection.class, pselMapper);
        toXmlMap.put(TsPeriodSelector.class, pselMapper);

        XmlConverterMapper<Parameter[], XmlParameters> paramsMapper =
                new XmlConverterMapper<>(XmlParameters.class);

        fromXmlMap.put(XmlParameters.class, paramsMapper);
        toXmlMap.put(Parameter[].class, paramsMapper);

        XmlConverterMapper<Parameter, XmlParameter> paramMapper =
                new XmlConverterMapper<>(XmlParameter.class);

        fromXmlMap.put(XmlParameter.class, paramMapper);
        toXmlMap.put(Parameter.class, paramMapper);

        //XmlConverterMapper<XmlDiagnostic, SADiagnostic> diagsMapper
        //     = new XmlConverterMapper<XmlDiagnostic, SADiagnostic>();

        //fromXmlMap.put(XmlDiagnostic.class, diagsMapper);
        //toXmlMap.put(SeasonalAdjustmentDiagnostic.class, diagsMapper);

        XmlConverterMapper<TsMoniker, XmlTsMoniker> monikerMapper =
                new XmlConverterMapper<>(XmlTsMoniker.class);

        fromXmlMap.put(XmlTsMoniker.class, monikerMapper);
        toXmlMap.put(TsMoniker.class, monikerMapper);

        XmlConverterMapper<TsInformation, XmlTs> tsMapper =
                new XmlConverterMapper<>(XmlTs.class);

        fromXmlMap.put(XmlTs.class, tsMapper);
        toXmlMap.put(TsInformation.class, tsMapper);
        
        XmlConverterMapper<TsCollectionInformation, XmlTsCollection> tsCollectionMapper =
                new XmlConverterMapper<>(XmlTsCollection.class);

        fromXmlMap.put(XmlTsCollection.class, tsCollectionMapper);
        toXmlMap.put(TsCollectionInformation.class, tsCollectionMapper);

        XmlConverterMapper<RegressionItem, XmlRegItem> regitemMapper =
                new XmlConverterMapper<>(XmlRegItem.class);

        fromXmlMap.put(XmlRegItem.class, regitemMapper);
        toXmlMap.put(RegressionItem.class, regitemMapper);

        XmlConverterMapper<StatisticalTest, XmlStatisticalTest> statsMapper =
                new XmlConverterMapper<>(XmlStatisticalTest.class);

        fromXmlMap.put(XmlStatisticalTest.class, statsMapper);
        toXmlMap.put(StatisticalTest.class, statsMapper);
        XmlConverterMapper<InterventionVariable, XmlInterventionVariable> ivarMapper =
                new XmlConverterMapper<>(XmlInterventionVariable.class);

        fromXmlMap.put(XmlInterventionVariable.class, ivarMapper);
        toXmlMap.put(InterventionVariable.class, ivarMapper);
        XmlConverterMapper<Ramp, XmlRamp> rampMapper =
                new XmlConverterMapper<>(XmlRamp.class);

        fromXmlMap.put(XmlRamp.class, rampMapper);
        toXmlMap.put(Ramp.class, rampMapper);
        
        XmlConverterMapper<Matrix, XmlMatrix> matrixMapper =
                new XmlConverterMapper<>(XmlMatrix.class);

        fromXmlMap.put(XmlMatrix.class, matrixMapper);
        toXmlMap.put(Matrix.class, matrixMapper);
        
    }
    
    @XmlAttribute
    public String name;
    @XmlElements(value = {
        @XmlElement(type = Boolean.class, name = "boolean"),
        @XmlElement(type = Double.class, name = "double"),
        @XmlElement(type = Integer.class, name = "integer"),
        @XmlElement(type = String.class, name = "string"),
        @XmlElement(type = XmlIntegers.class, name = "integers"),
        @XmlElement(type = XmlDoubles.class, name = "doubles"),
        @XmlElement(type = boolean[].class, name = "booleans"),
        @XmlElement(type = XmlStrings.class, name = "strings"),
        @XmlElement(type = XmlInformationSet.class, name = "subset"),
        @XmlElement(type = XmlAlgorithm.class, name = "method"),
        @XmlElement(type = XmlTsData.class, name = "tsdata"),
        @XmlElement(type = XmlTsMoniker.class, name = "moniker"),
        @XmlElement(type = XmlTs.class, name = "ts"),
        @XmlElement(type = XmlTsCollection.class, name = "tscollection"),
        @XmlElement(type = XmlPeriodSelection.class, name = "span"),
        @XmlElement(type = XmlStatisticalTest.class, name = "test"),
        @XmlElement(type = XmlInterventionVariable.class, name = "ivar"),
        @XmlElement(type = XmlRamp.class, name = "ramp"),
        @XmlElement(type = XmlRegItem.class, name = "regitem"),
        @XmlElement(type = XmlParameter.class, name = "param"),
        @XmlElement(type = XmlParameters.class, name = "params"),       
        @XmlElement(type = XmlMatrix.class, name = "matrix")})
    Object information;

    public Information<Object> toInformation() {
        if (information == null) {
            return null;
        }
        IXmlMapper mapper = fromXmlMap.get(information.getClass());
        if (mapper == null) {
            return null;
        } else {
            return new Information<>(name, mapper.fromXml(information));
        }
    }

    public XmlInformation() {
    }

    public XmlInformation(String name, Object value) {
        this.name = name;
        this.information = value;
    }

    public static <T> XmlInformation create(Information<T> info) {
        IXmlMapper mapper = toXmlMap.get(info.value.getClass());
        if (mapper != null) {
            return new XmlInformation(info.name, mapper.toXml(info.value));
        } else if (info.value.getClass().isEnum()) {
            return new XmlInformation(info.name, info.value.toString());
        } else {
            return null;
        }
    }
}
