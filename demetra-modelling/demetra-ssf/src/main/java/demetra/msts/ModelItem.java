/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import java.util.List;

/**
 *
 * @author palatej
 */
public interface ModelItem {
    String getName();
    
    void addTo(MstsMapping model);
    
    List<IMstsParametersBlock> parameters();
}
