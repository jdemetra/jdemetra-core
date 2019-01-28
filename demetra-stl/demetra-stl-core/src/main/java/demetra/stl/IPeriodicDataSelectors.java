package demetra.stl;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IPeriodicDataSelectors {
    
    IDataSelector get(int period);
}
