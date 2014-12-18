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

package ec.satoolkit.x11;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.design.Development;
import java.util.ArrayList;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeriesDictionaries {

    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    public static final ArrayList<SeriesInfo> A = new ArrayList<>(),
	    B = new ArrayList<>(),
	    C = new ArrayList<>(),
	    D = new ArrayList<>(),
	    E = new ArrayList<>();

    static {
	// InitDictionaries
	SeriesInfo a1 = new SeriesInfo("a1", null, ComponentType.Series,
		ComponentInformation.Value);
	A.add(a1);
	SeriesInfo a1a = new SeriesInfo("a1a", null, ComponentType.Series,
		ComponentInformation.Forecast);
	A.add(a1a);
	SeriesInfo a2 = new SeriesInfo("a2", ComponentType.CalendarEffect);
	A.add(a2);
	SeriesInfo a6 = new SeriesInfo("a6", ComponentType.CalendarEffect);
	A.add(a6);
	SeriesInfo a7 = new SeriesInfo("a7", ComponentType.CalendarEffect);
	A.add(a7);
	SeriesInfo a8 = new SeriesInfo("a8", ComponentType.Undefined);
	A.add(a8);
	SeriesInfo a8ao = new SeriesInfo("a8ao", ComponentType.Irregular);
	A.add(a8ao);
	SeriesInfo a8ls = new SeriesInfo("a8ls", ComponentType.Trend);
	A.add(a8ls);
	SeriesInfo a8tc = new SeriesInfo("a8tc", ComponentType.Irregular);
	A.add(a8tc);
	SeriesInfo a9 = new SeriesInfo("a9", ComponentType.Series);
	A.add(a9);
	SeriesInfo b1 = new SeriesInfo("b1", ComponentType.Series);
	B.add(b1);
	SeriesInfo b2 = new SeriesInfo("b2", ComponentType.Trend);
	B.add(b2);
	SeriesInfo b3 = new SeriesInfo("b3", ComponentType.Seasonal);
	B.add(b3);
	SeriesInfo b4 = new SeriesInfo("b4", ComponentType.Irregular);
	B.add(b4);
	SeriesInfo b5 = new SeriesInfo("b5", ComponentType.Seasonal);
	B.add(b5);
	SeriesInfo b6 = new SeriesInfo("b6",
		ComponentType.SeasonallyAdjusted);
	B.add(b6);
	SeriesInfo b7 = new SeriesInfo("b7", ComponentType.Trend);
	B.add(b7);
	SeriesInfo b8 = new SeriesInfo("b8", ComponentType.Seasonal);
	B.add(b8);
	SeriesInfo b9 = new SeriesInfo("b9", ComponentType.Irregular);
	B.add(b9);
	SeriesInfo b10 = new SeriesInfo("b10", ComponentType.Seasonal);
	B.add(b10);
	SeriesInfo b11 = new SeriesInfo("b11",
		ComponentType.SeasonallyAdjusted);
	B.add(b11);
	SeriesInfo b13 = new SeriesInfo("b13", ComponentType.Irregular);
	B.add(b13);
	SeriesInfo b17 = new SeriesInfo("b17", ComponentType.Irregular);
	B.add(b17);
	SeriesInfo b19 = new SeriesInfo("b19", ComponentType.Trend);
	B.add(b19);
	SeriesInfo b20 = new SeriesInfo("b20", ComponentType.Irregular);
	B.add(b20);

	// /////////////////////////
	SeriesInfo c1 = new SeriesInfo("c1", ComponentType.Series);
	C.add(c1);
	SeriesInfo c2 = new SeriesInfo("c2", ComponentType.Trend);
	C.add(c2);
	SeriesInfo c4 = new SeriesInfo("c4", ComponentType.Seasonal);
	C.add(c4);
	SeriesInfo c5 = new SeriesInfo("c5", ComponentType.Seasonal);
	C.add(c5);
	SeriesInfo c6 = new SeriesInfo("c6",
		ComponentType.SeasonallyAdjusted);
	C.add(c6);
	SeriesInfo c7 = new SeriesInfo("c7", ComponentType.Trend);
	C.add(c7);
	SeriesInfo c9 = new SeriesInfo("c9", ComponentType.Seasonal);
	C.add(c9);
	SeriesInfo c10 = new SeriesInfo("c10", ComponentType.Seasonal);
	C.add(c10);
	SeriesInfo c11 = new SeriesInfo("c11",
		ComponentType.SeasonallyAdjusted);
	C.add(c11);
	SeriesInfo c13 = new SeriesInfo("c13", ComponentType.Irregular);
	C.add(c13);
	SeriesInfo c19 = new SeriesInfo("c19", ComponentType.Series);
	C.add(c19);
	SeriesInfo c20 = new SeriesInfo("c20", ComponentType.Irregular);
	C.add(c20);

	// ////////////////////////
	SeriesInfo d1 = new SeriesInfo(
		"d1",
		"original series modified for outliers, trading day and prior factors",
		ComponentType.Series);
	D.add(d1);
	SeriesInfo d2 = new SeriesInfo("d2", "preliminary trend-cycle",
		ComponentType.Trend);
	D.add(d2);
	SeriesInfo d4 = new SeriesInfo("d4",
		"modified SI-ratios (differences)", ComponentType.Seasonal);
	D.add(d4);
	SeriesInfo d5 = new SeriesInfo("d5",
		"preliminary seasonal factors", ComponentType.Seasonal);
	D.add(d5);
	SeriesInfo d6 = new SeriesInfo("d6",
		"preliminary seasonally adjusted series",
		ComponentType.SeasonallyAdjusted);
	D.add(d6);
	SeriesInfo d7 = new SeriesInfo("d7", "preliminary trend-cycle",
		ComponentType.Trend);
	D.add(d7);
	SeriesInfo d8 = new SeriesInfo("d8", "final unmodified SI-ratios",
		ComponentType.Seasonal);
	D.add(d8);
	SeriesInfo d9 = new SeriesInfo("d9",
		"final replacement values for extreme si-ratios (differences)",
		ComponentType.Irregular);
	D.add(d9);
	SeriesInfo d10 = new SeriesInfo("d10", "final seasonal factors",
		ComponentType.Seasonal);
	D.add(d10);
	SeriesInfo d10a = new SeriesInfo("d10a",
		"final seasonal factors, forecasts", ComponentType.Seasonal,
		ComponentInformation.Forecast);
	D.add(d10a);
	SeriesInfo d10u = new SeriesInfo("d10u", "seasonal factors",
		ComponentType.Seasonal);
	D.add(d10u);
	SeriesInfo d11 = new SeriesInfo("d11",
		"final seasonally adjusted series",
		ComponentType.SeasonallyAdjusted);
	D.add(d11);
	SeriesInfo d11a = new SeriesInfo("d11a",
		"seasonally adjusted series, forecasts",
		ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
	D.add(d11a);
	SeriesInfo d11u = new SeriesInfo("d11u",
		"seasonally adjusted series", ComponentType.SeasonallyAdjusted);
	D.add(d11u);
	SeriesInfo d12 = new SeriesInfo("d12", "final trend-cycle",
		ComponentType.Trend);
	D.add(d12);
	SeriesInfo d12a = new SeriesInfo("d12a", "trend-cycle",
		ComponentType.Trend, ComponentInformation.Forecast);
	D.add(d12a);
	SeriesInfo d12u = new SeriesInfo("d12u", "trend-cycle",
		ComponentType.Trend);
	D.add(d12u);
	SeriesInfo d13 = new SeriesInfo("d13", "final irregular component",
		ComponentType.Irregular);
	D.add(d13);
	SeriesInfo d13u = new SeriesInfo("d13u", "irregular component",
		ComponentType.Irregular);
	D.add(d13u);
	SeriesInfo d16 = new SeriesInfo("d16",
		"combined seasonal and trading day factors",
		ComponentType.Seasonal);
	D.add(d16);
	SeriesInfo d16a = new SeriesInfo("d16a",
		"combined seasonal and trading day factors",
		ComponentType.Seasonal, ComponentInformation.Forecast);
	D.add(d16a);
	SeriesInfo d18 = new SeriesInfo("d18",
		"combined holiday and trading day factors",
		ComponentType.CalendarEffect);
	D.add(d18);

	// ////////////////////////
	SeriesInfo e1 = new SeriesInfo("e1", ComponentType.Series);
	E.add(e1);
	SeriesInfo e2 = new SeriesInfo("e2",
		ComponentType.SeasonallyAdjusted);
	E.add(e2);
	SeriesInfo e3 = new SeriesInfo("e3", ComponentType.Irregular);
	E.add(e3);
	SeriesInfo e11 = new SeriesInfo("e11",
		ComponentType.SeasonallyAdjusted);
	E.add(e11);

    }
}
