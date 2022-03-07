/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package demetra.tramoseats;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsDictionariesTest {
    
    public TramoSeatsDictionariesTest() {
    }

    public static void regsarima() {
       TramoSeatsDictionaries.TRAMOSEATSDICTIONARY.entries().forEach(entry
                -> System.out.println(entry.display()));
     }
    
    public static void main(String[] arg){
        regsarima();
    }
}
