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
package demetra.x13.io.xml.legacy;

import demetra.modelling.RegressionTestSpec;
import demetra.regarima.TradingDaysSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for StockTradingDaysSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="StockTradingDaysSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}StockTradingDaysSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Test" type="{ec/eurostat/jdemetra/sa/x13}TradingDaysTestEnum" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StockTradingDaysSpecType", propOrder = {})
public class XmlStockTradingDaysSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlStockTradingDaysSpec {

    public static final boolean marshal(TradingDaysSpec v, XmlTradingDaysSpec xml) {
        if (xml.stock != null) {
            xml.setTest(v.getRegressionTestType());
            xml.stock = new XmlStockTradingDaysSpec();
            xml.stock.setW(v.getStockTradingDays());
            return true;
        } else {
            return false;
        }
    }

    public static TradingDaysSpec unmarshal(XmlTradingDaysSpec xml) {
        if (xml.stock == null) {
            return null;
        }
        RegressionTestSpec test = xml.test == null ? RegressionTestSpec.None : xml.test;
        return TradingDaysSpec.stockTradingDays(xml.stock.w, test);
    }
}
