/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling;

/**
 *
 * @author palatej
 */
@lombok.Value
public class OutlierDescriptor {
    private String code;
    private int position;
    
    @Override
    public String toString(){
        return code+"."+(position+1);
    }
}
