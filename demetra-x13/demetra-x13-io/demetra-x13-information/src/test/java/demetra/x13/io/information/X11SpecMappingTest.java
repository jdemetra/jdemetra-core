/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.information;

import demetra.information.InformationSet;
import demetra.x11.X11Spec;
import static demetra.x13.io.information.X11SpecMapping.FCASTS;
import static demetra.x13.io.information.X11SpecMapping.SIGMAVEC;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class X11SpecMappingTest {

    public X11SpecMappingTest() {
    }

    ;

    @Test
    public void testReadLegacy() {
        X11Spec specInput = X11Spec.builder().build();
        InformationSet infoV2 = X11SpecMapping.write(specInput, true);
        String[] vsig = {"Group1", "Group2", "Group1", "Group1"};
        infoV2.add(SIGMAVEC, vsig);
        infoV2.remove(FCASTS);// The default value could be 0 vor X11 or -1 for X13 this is not saved in the SA Processiong of Version 2

        X11Spec specV3 = X11SpecMapping.readlegacy(infoV2, -1);
        Assert.assertNull("Sigmavec is not null and Calendarsigma is default", specV3.getSigmaVec());
        Assert.assertEquals("Forecast hoizon is wrong: ", -1, specV3.getForecastHorizon());

    }

}
