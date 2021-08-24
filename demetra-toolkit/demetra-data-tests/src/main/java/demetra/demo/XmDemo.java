/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.demo;

import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import java.util.Arrays;

/**
 *
 * @author palatej
 */
public class XmDemo {
    public static void main(String[] cmd){
        TsData[] all=Data.xm();
        TsDataTable table=TsDataTable.of(Arrays.asList(all));
        System.out.println(table);
        
    }
}
