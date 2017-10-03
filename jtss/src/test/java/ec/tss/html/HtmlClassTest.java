/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.html;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe
 */
public class HtmlClassTest {

    @Test
    public void test() {
        assertThat(HtmlClass.NO_CLASS.toString()).isEqualTo("");
        assertThat(HtmlClass.of("hello").toString()).isEqualTo("hello");
        assertThat(HtmlClass.of("hello").with(HtmlClass.NO_CLASS).toString()).isEqualTo("hello");
        assertThat(HtmlClass.NO_CLASS.with(HtmlClass.of("hello")).toString()).isEqualTo("hello");
        assertThat(HtmlClass.of("hello").with(HtmlClass.of("world")).toString()).isEqualTo("hello world");
    }
}
