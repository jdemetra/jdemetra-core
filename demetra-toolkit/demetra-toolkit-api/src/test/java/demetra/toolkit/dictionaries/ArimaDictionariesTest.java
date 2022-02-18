/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package demetra.toolkit.dictionaries;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class ArimaDictionariesTest {

    public ArimaDictionariesTest() {
    }

    public static void ucarima() {
        ArimaDictionaries.UCARIMA.entries().forEach(entry
                -> {
            System.out.print(entry.getName());
            System.out.print('\t');
            System.out.print(entry.getDescription());
            System.out.print('\t');
            System.out.println(entry.getType().getCanonicalName());
        }
        );
    }
    
    public static void main(String[] arg){
        ucarima();
    }
}
