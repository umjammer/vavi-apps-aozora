/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.converter;

import java.io.Reader;
import java.io.Writer;


/**
 * Converter.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-14 nsano initial version <br>
 */
public interface Converter {

    /** */
    void readText(Reader reader);

    /** */
    void printHtml(Writer writer);
}
