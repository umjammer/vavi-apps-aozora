/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.aobook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-04 nsano initial version <br>
 */
class TestCase {

    @Test
    void test1() throws Exception {
        StyleWork styleWork = new StyleWork();
        String[] name = new String[1];
        styleWork.StyleWork_readStyle(name);
    }
}
