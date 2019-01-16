/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.msts.ModelItem;

/**
 *
 * @author palatej
 */
public abstract class AbstractModelItem implements ModelItem{
    protected final String name;
    
    protected AbstractModelItem(String name){
        this.name=name;
    }
    
    @Override
    public String getName(){
        return name;
    }
}
