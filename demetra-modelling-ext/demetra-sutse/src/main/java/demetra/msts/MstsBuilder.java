/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MstsBuilder {
    
    private final StsBuilder[] components;
    
    public MstsBuilder(int ncomponents){
        components=new StsBuilder[ncomponents];
        for (int i=0; i<ncomponents; ++i){
            components[i]=new StsBuilder();
        }
    }
    
}
