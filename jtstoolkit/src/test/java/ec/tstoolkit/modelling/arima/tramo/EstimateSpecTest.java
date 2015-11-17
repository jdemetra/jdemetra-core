/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import java.text.DateFormat;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class EstimateSpecTest {

    public EstimateSpecTest() {
    }

    @Test
    public void testEquals() {
        EstimateSpec expected = new EstimateSpec();
        EstimateSpec actual = new EstimateSpec();
        assertEquals(expected, actual);

        expected.setEML(false);
        assertNotEquals(expected, actual);
        actual.setEML(false);
        assertEquals(expected, actual);

        expected.setTol(1e-4);
        assertNotEquals(expected, actual);
        actual.setTol(1e-4);
        assertEquals(expected, actual);

        expected.setUbp(1.0);
        assertNotEquals(expected, actual);
        actual.setUbp(1.0);
        assertEquals(expected, actual);

        TsPeriodSelector p = new TsPeriodSelector();
        p.setType(PeriodSelectorType.Between);
        expected.setSpan(p);
        assertNotEquals(expected, actual);
        actual.setSpan(p);
        assertEquals(expected, actual);
    }

    @Test
    public void testInformation() {
        EstimateSpec spec = new EstimateSpec();
        InformationSet info;
        EstimateSpec nspec = new EstimateSpec();
        
        info = spec.write(true);
        nspec.read(info);
        assertEquals(nspec, spec);

        // Test Default values
        assertEquals(spec.isDefault(), nspec.isDefault());
        assertEquals(new TsPeriodSelector(), nspec.getSpan());
        assertEquals(1e-7, nspec.getTol(), 0.0);
        assertTrue(nspec.isEML());
        assertEquals(0.96, nspec.getUbp(), 0.0);
        
        // Test Model span
        TsPeriodSelector perSel = new TsPeriodSelector();
        perSel.setType(PeriodSelectorType.From);
        perSel.from(Day.toDay());
        spec.setSpan(perSel);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(PeriodSelectorType.From, nspec.getSpan().getType());
        assertEquals(Day.toDay(), nspec.getSpan().getD0());

        perSel.setType(PeriodSelectorType.To);
        perSel.to(Day.toDay());
        spec.setSpan(perSel);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(PeriodSelectorType.To, nspec.getSpan().getType());
        assertEquals(Day.toDay(), nspec.getSpan().getD1());
        
        perSel.setType(PeriodSelectorType.Between);
        perSel.between(Day.toDay(), Day.toDay().plus(1));
        spec.setSpan(perSel);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(PeriodSelectorType.Between, nspec.getSpan().getType());
        assertEquals(Day.toDay(), nspec.getSpan().getD0());
        assertEquals(Day.toDay().plus(1), nspec.getSpan().getD1());
        
        perSel.setType(PeriodSelectorType.First);
        perSel.first(1);
        spec.setSpan(perSel);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(PeriodSelectorType.First, nspec.getSpan().getType());
        assertEquals(1, nspec.getSpan().getN0());
        
        perSel.setType(PeriodSelectorType.Last);
        perSel.last(1);
        spec.setSpan(perSel);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(PeriodSelectorType.Last, nspec.getSpan().getType());
        assertEquals(1, nspec.getSpan().getN1());
        
        perSel.setType(PeriodSelectorType.Excluding);
        perSel.excluding(1, 2);
        spec.setSpan(perSel);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(PeriodSelectorType.Excluding, nspec.getSpan().getType());
        assertEquals(1, nspec.getSpan().getN0());
        assertEquals(2, nspec.getSpan().getN1());
        
        spec.setTol(1e-4);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(1e-4, nspec.getTol(), 0.0);

        spec.setEML(false);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(false, nspec.isEML());

        spec.setUbp(1.2);
        info = spec.write(true);
        nspec.read(info);
        assertEquals(1.2, nspec.getUbp(), 0.0);

        // Reset values
        spec.reset();
        assertEquals(new EstimateSpec(), spec);
        assertTrue(spec.isDefault());
    }

    @Test(expected = TramoException.class)
    public void testSetTolUpperBound() {
        EstimateSpec spec = new EstimateSpec();
        spec.setTol(1.0);
    }

    @Test(expected = TramoException.class)
    public void testSetTolLowerBound() {
        EstimateSpec spec = new EstimateSpec();
        spec.setTol(-0.1);
    }
}
