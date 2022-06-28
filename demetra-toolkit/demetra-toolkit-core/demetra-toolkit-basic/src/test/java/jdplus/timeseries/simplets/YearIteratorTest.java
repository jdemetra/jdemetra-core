/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.timeseries.simplets;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class YearIteratorTest {
    
    public YearIteratorTest() {
    }

 //   @Test
    public static void testFull() {
        TsPeriod start=TsPeriod.monthly(2000, 1);
        TsData s=TsData.of(start, DoubleSeq.onMapping(36, i->i));
        YearIterator iter=new YearIterator(s);
        while (iter.hasMoreElements()){
            TsDataView view = iter.nextElement();
            System.out.println(view.getStart().display());
            System.out.print('\t');
            System.out.println(view.getData());
        }
    }
    
//    @Test
    public static void testPartial() {
        TsPeriod start=TsPeriod.monthly(2000, 4);
        TsData s=TsData.of(start, DoubleSeq.onMapping(35, i->i));
        YearIterator iter=new YearIterator(s);
        while (iter.hasMoreElements()){
            TsDataView view = iter.nextElement();
            System.out.println(view.getStart().display());
            System.out.print('\t');
            System.out.println(view.getData());
        }
    }
    
       public static void main(String[] args){
        testFull();
        System.out.println("");
        testPartial();
    }

}
