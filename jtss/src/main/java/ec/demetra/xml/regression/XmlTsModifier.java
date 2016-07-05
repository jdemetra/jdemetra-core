/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.regression;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class XmlTsModifier extends XmlVariable{
        
    @XmlElement
    public XmlVariable Core;
}
