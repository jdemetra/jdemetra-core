/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package demetra.toolkit.dictionaries;

/**
 *
 * @author PALATEJ
 */
public class ResidualsDictionariesTest {

    public ResidualsDictionariesTest() {
    }

    public static void residuals() {
        ResidualsDictionaries.RESIDUALS_DEFAULT.entries().forEach(entry
                -> System.out.println(entry.display()));
    }

    public static void main(String[] arg) {
        residuals();
    }

}
