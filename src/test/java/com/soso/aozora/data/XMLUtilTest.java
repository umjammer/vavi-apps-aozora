package com.soso.aozora.data;

import org.junit.jupiter.api.Test;


class XMLUtilTest {

    @Test
    void test() throws Exception {
        XMLUtil.getEntityMap().forEach((k, v) -> System.err.printf("%10s = %c%n", ('&' + k + ";"), v));
    }
}