/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.x11;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christiane.Hofer@bundesbank.de
 */
public class x11SpecTest {

@Test
    public void testCalendarSigmaDefault() {
        X11Spec spec = X11Spec.builder()
                .build();
        assertEquals("Default Calendar Sigma Option", CalendarSigmaOption.None, spec.getCalendarSigma());
    }

    @Test
    public void testCalendarSigmaSelect() {
        SigmaVecOption[] svo = new SigmaVecOption[2];
        X11Spec spec = X11Spec.builder()
                .calendarSigma(CalendarSigmaOption.Select)
                .sigmaVec(svo)
                .build();
        assertEquals("Calendar Sigma Option", CalendarSigmaOption.Select, spec.getCalendarSigma());
    }

    @Test
    public void testCalendarSigmaDefaultSigmaVec() {
        try {
            SigmaVecOption[] svo = new SigmaVecOption[2];
            X11Spec spec = X11Spec.builder()
                    .sigmaVec(svo)
                    .build();
        } catch (X11Exception e) {
            assertEquals(e.getMessage(), "Sigmavec mustn't be used without CalendarSigmaOption Select");
        }

    }
    
}
