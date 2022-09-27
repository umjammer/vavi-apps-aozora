package vavi.text;

import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.*;


class UnicodeUtilTest {

    @Test
    void test1() throws Exception {
        assertEquals("瘵", UnicodeUtil.toUnicodeChar("1-88-56"));
        assertEquals("ǽ", UnicodeUtil.toUnicodeChar("1-11-37"));
        assertEquals("è", UnicodeUtil.toUnicodeChar("1-9-62"));
        assertEquals("𢌞", UnicodeUtil.toUnicodeChar(2, 12, 11));
        assertNull(UnicodeUtil.toUnicodeChar("1-09-62"));
    }

    @Test
    void test2() {
        String a = "𢌞";
Debug.printf("%d, %04x, %04x", a.length(), (int) a.charAt(0), (int) a.charAt(1));
    }
}