/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.utilities;

import com.google.common.base.Stopwatch;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * http://svn.apache.org/viewvc/commons/proper/codec/trunk/src/test/java/org/apache/commons/codec/net/URLCodecTest.java?revision=1352268&view=markup
 *
 * @author Philippe Charles
 */
public class URLEncoder2Test {

    static final String EX1_SRC = "hello world![]#&éè@/\\<>";
    static final String EX1_DST = "hello%20world!%5B%5D%23%26%C3%A9%C3%A8%40%2F%5C%3C%3E";

    @Test
    public void testEncoders() throws UnsupportedEncodingException {

        // FIXME: '%20' or '+' ? what about '@' and '!' ?
//        Assert.assertEquals(EX1_DST2, URLEncoder2.encode(EX1_SRC, Charsets.UTF_8));
        Assert.assertEquals(URLEncoder.encode(EX1_SRC, StandardCharsets.UTF_8.name()), URLEncoder2.encode(EX1_SRC, StandardCharsets.UTF_8));
    }

    @Test
    public void testStringBuilderNull() {
        assertThatNullPointerException().isThrownBy(() -> URLEncoder2.encode(null, EX1_SRC, StandardCharsets.UTF_8));
    }

    @Test
    public void testCharSequenceNull() {
        assertThatNullPointerException().isThrownBy(() -> URLEncoder2.encode(new StringBuilder(), null, StandardCharsets.UTF_8));
    }

    @Test
    public void testCharsetNull() {
        assertThatNullPointerException().isThrownBy(() -> URLEncoder2.encode(new StringBuilder(), EX1_SRC, null));
    }

    static class FasterImpl implements Runnable {

        @Override
        public void run() {
            try {
                URLEncoder2.encode(EX1_SRC, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {
            }
        }
    }

    static class JdkImpl implements Runnable {

        @Override
        public void run() {
            try {
                URLEncoder.encode(EX1_SRC, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {
            }
        }
    }

    @Test
    @Ignore
    public void testSpeed() {
        Runnable[] tmp = new Runnable[]{new FasterImpl(), new JdkImpl()};

        for (Runnable o : tmp) {
            for (int i = 0; i < 10000; i++) {
                o.run();
            }
        }

        Stopwatch stopwatch = Stopwatch.createUnstarted(ThreadTicker.getInstance());
        for (Runnable o : tmp) {
            stopwatch.start();
            for (int i = 0; i < 100000; i++) {
                o.run();
            }
            stopwatch.stop();
            System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }
}
