/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class X11SpecificationTest {

    public X11SpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        X11Specification expected = new X11Specification();
        X11Specification actual = new X11Specification();
        InformationSet info;
        assertEquals(expected, actual);

        expected.setMode(DecompositionMode.Multiplicative);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DecompositionMode.Multiplicative, actual.getMode());

        assertTrue(expected.isSeasonal());
        expected.setSeasonal(false);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isSeasonal());

        expected.setSigma(2, 2.2);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getLowerSigma(), .0);
        assertEquals(2.2, actual.getUpperSigma(), .0);

        assertTrue(expected.isAutoHenderson());
        expected.setHendersonFilterLength(21);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(21, actual.getHendersonFilterLength());
        assertFalse(actual.isAutoHenderson());

        expected.setForecastHorizon(4);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(4, actual.getForecastHorizon());

        expected.setBackcastHorizon(11);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(11, actual.getBackcastHorizon());

        expected.setCalendarSigma(CalendarSigma.Signif);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(CalendarSigma.Signif, actual.getCalendarSigma());

        expected.setSeasonalFilter(SeasonalFilterOption.Stable);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeasonalFilterOption.Stable, actual.getSeasonalFilters()[0]);

        SeasonalFilterOption[] sfO = new SeasonalFilterOption[]{
            SeasonalFilterOption.S3X1,
            SeasonalFilterOption.S3X5,
            SeasonalFilterOption.X11Default
        };
        expected.setSeasonalFilters(sfO);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(3, actual.getSeasonalFilters().length);

        expected.setSigmavec(new SigmavecOption[]{SigmavecOption.Group2});
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1, actual.getSigmavec().length);
        assertEquals(SigmavecOption.Group2, actual.getSigmavec()[0]);
    }

    @Test(expected = X11Exception.class)
    public void testsetSigmaLowerBound() {
        X11Specification expected = new X11Specification();
        expected.setSigma(0.2, 2.5);
    }

    @Test(expected = X11Exception.class)
    public void testSetHendersonFilterLengthEvenNumber() {
        X11Specification expected = new X11Specification();
        expected.setHendersonFilterLength(12);
    }

    @Test(expected = X11Exception.class)
    public void testSetHendersonFilterLengthMinZero() {
        X11Specification expected = new X11Specification();
        expected.setHendersonFilterLength(-1);
    }

    @Test(expected = X11Exception.class)
    public void testSetHendersonFilterLengthUpperBound() {
        X11Specification expected = new X11Specification();
        expected.setHendersonFilterLength(103);
    }

}
