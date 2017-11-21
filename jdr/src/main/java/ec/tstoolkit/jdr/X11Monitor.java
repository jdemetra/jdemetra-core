/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr;

import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x11.X11Toolkit;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11Monitor {
    public static X11Results process(TsData s, X11Specification spec){
        X11Toolkit toolkit = X11Toolkit.create(spec);
        X11Kernel kernel=new X11Kernel();
        kernel.setToolkit(toolkit);
        return kernel.process(s);
    }
}
