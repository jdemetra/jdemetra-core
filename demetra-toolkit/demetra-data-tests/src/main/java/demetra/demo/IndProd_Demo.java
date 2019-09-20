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
public class IndProd_Demo {
    public static void main(String[] cmd){
        TsData[] all=Data.indprod_de();
        System.out.println(all.length);
        System.out.println();
        System.out.println(all[0].length());
        System.out.println();
        System.out.println(all[0]);
        all=Data.indprod_fr();
        System.out.println();
        System.out.println(all.length);
        System.out.println();
        System.out.println(all[0].length());
        System.out.println();
        System.out.println(all[0]);
        
    }
}
