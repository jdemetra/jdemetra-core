/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package demetra.x13;

/**
 *
 * @author PALATEJ
 */
public class X13DictionariesTest {
    
    public X13DictionariesTest() {
    }

    public static void regsarima() {
       X13Dictionaries.X13DICTIONARY.entries().forEach(entry
                -> System.out.println(entry.display()));
    }
    
    public static void main(String[] arg){
        regsarima();
    }
}
