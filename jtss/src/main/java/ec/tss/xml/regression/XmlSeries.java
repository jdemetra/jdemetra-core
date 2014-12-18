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


package ec.tss.xml.regression;

import ec.tss.xml.XmlTsData;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlSeries.NAME)
public class XmlSeries {
    static final String NAME = "yType";

    @XmlElement
    public XmlTransformations transformations;

    @XmlElement
    public XmlTsData originalData;

    @XmlElement
    public XmlTsData transformedData;

    public XmlSeries() { }

//    public XmlSeries(Series y) {
//        originalData = new XmlTsData();
//        originalData.Copy(y.OriginalData);
//
//        transformations = new XmlTransformations();
//        if (y.Builder.Const != null)
//            transformations.constant = y.Builder.Const.Value;
//        if (y.Builder.PermanentPriorFactor != null)
//            transformations.permanentPrior = true;
//        if (y.Builder.TemporaryPriorFactor != null)
//            transformations.temporaryPrior = true;
//        if (y.Builder.Adjust != null)
//            transformations.adjust = y.Builder.Adjust.Type;
//        if (y.Builder.Units != null)
//            transformations.units = y.Builder.Units.Value;
//        if (y.Builder.Log != null)
//            transformations.logTransformed = true;
//        if (y.OriginalData.Values.HasMissingValues) {
//            AverageInterpolator linterp = y.Builder.Interpolator as AverageInterpolator;
//            if (linterp != null)
//                transformations.interpolation = XmlTransformations.Interpolation.linear;
//            else
//            {
//                ConstInterpolator cinterp = y.Builder.Interpolator as ConstInterpolator;
//                if (cinterp != null) {
//                    transformations.interpolation = XmlTransformations.Interpolation.byreplacement;
//                }
//            }
//        }
//
//        boolean transformed = ! transformations.isDefault();
//        if (!transformed)
//            transformations = null;
//        else {
//            transformedData = new XmlTsData();
//            transformedData.Copy(y.TransformedData);
//        }
//    }
}
