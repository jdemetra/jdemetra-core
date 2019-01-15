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
package ec.tstoolkit.algorithm;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.EasterRelatedDay;
import ec.tstoolkit.timeseries.calendars.FixedDay;
import ec.tstoolkit.timeseries.calendars.NationalCalendar;
import ec.tstoolkit.timeseries.calendars.NationalCalendarProvider;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class ProcessingContextTest {

    static {
        ProcessingContext active = ProcessingContext.getActiveContext();
        TsVariables vars = new TsVariables();
        vars.set("Exports", new TsVariable(data.Data.X));
        active.getTsVariableManagers().set("Test", vars);

    }

    public ProcessingContextTest() {
    }

    @Test
    public void testRegressionVariables() {
        ProcessingContext active = ProcessingContext.getActiveContext();
        assertTrue(active.getTsVariable("Test", "Exports") != null);
        assertTrue(active.getTsVariable("Test.Exports") != null);
        assertTrue(active.getTsVariable("Test", "Exports") == active.getTsVariable("Test.Exports"));
    }

    @Test
    public void testCalendar() {
        // create a new context
        ProcessingContext context = new ProcessingContext();
        // create the national calendar with the different Holidays
        NationalCalendar be = new NationalCalendar();
        be.add(new FixedDay(20, Month.July));
        be.add(EasterRelatedDay.EasterMonday);
        // add the calendar to the context (give it a name)
        context.getGregorianCalendars().set("BE", new NationalCalendarProvider(be));

        // generate a new specification with the new holidays 
        TramoSeatsSpecification spec1 = TramoSeatsSpecification.RSA5.clone();
        spec1.getTramoSpecification().getRegression().getCalendar().getTradingDays().setHolidays("BE");
        // computes with the right context
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.P, spec1, context);
        assertTrue(rslt != null);
    }
}
