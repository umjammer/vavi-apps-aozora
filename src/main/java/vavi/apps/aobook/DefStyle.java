/*
 * aobook
 * Copyright (c) 2014-2022 Azel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package vavi.apps.aobook;

import java.util.EnumSet;


/**
 * Style Data Definition
 */
class DefStyle {

    static final int STYLE_CHARS_DEFAULT = 1;

    public final String txt;

    public DefStyle(String txt) {
        this.txt = txt;
    }

    enum DATATYPE {
        /** End */
        DATATYPE_END,
        /** Newline as sentence */
        DATATYPE_ENTER,
        /** Source line number value [(uint32)line number] */
        DATATYPE_LINEINFO,
        /** Normal text (16bit) [(uint16)length, 16bit string: null excluded] */
        DATATYPE_NORMAL_TEXT_16,
        /** Normal text (32bit) [(uint16)length, 32bit string] */
        DATATYPE_NORMAL_TEXT_32,
        /** Ruby text [(uint16)parent length, UTF-32 parent, (uint16)ruby length, UTF-32 ruby] */
        DATATYPE_RUBY_TEXT,
        /** Annotation command [(uint8)command type] */
        DATATYPE_COMMAND,
        /** Annotation command [(uint8)command type, (uint8x2)value] */
        DATATYPE_COMMAND_VAL,
        /** Picture [(uint16)string bytes: null included, UTF-8 filename: null included] */
        DATATYPE_PICTURE,
        /** For conversion work */
        DATATYPE_CHAR
    }

    /** Dakuten Type */
    enum STYLE_DAKUTEN {
        /** As is */
        STYLE_DAKUTEN_NORMAL,
        /** As is: Shift horizontally by full-width */
        STYLE_DAKUTEN_NORMAL_HORZ,
        /** As is: Shift vertically by full-width */
        STYLE_DAKUTEN_NORMAL_VERT,
        /** Combined character */
        STYLE_DAKUTEN_COMBINE,
        /** Combined character: Full-width horizontally */
        STYLE_DAKUTEN_COMBINE_HORZ,
        /** Combined character: Full-width vertically */
        STYLE_DAKUTEN_COMBINE_VERT
    }

    /** Flags */
    enum STYLE_FLAGS {
        /** Tile background image */
        STYLE_F_BKGND_TILE(1 << 0),
        /** Hanging punctuation enabled */
        STYLE_F_HANGING(1 << 1),
        /** Show picture */
        STYLE_F_ENABLE_PICTURE(1 << 2),
        /** Draw horizontal line characters as straight lines */
        STYLE_F_DASH_TO_LINE(1 << 3),
        /** Replace with standard print fonts */
        STYLE_F_REPLACE_PRINT(1 << 4);
        final int v;

        STYLE_FLAGS(int v) {
            this.v = v;
        }

        static EnumSet<STYLE_FLAGS> valueOf(int v) {
            EnumSet<STYLE_FLAGS> es = EnumSet.noneOf(STYLE_FLAGS.class);
            for (STYLE_FLAGS e : values())
                if ((e.v & v) != 0)
                    es.add(e);
            return es;
        }

        static int valueOf(EnumSet<STYLE_FLAGS> es) {
            int v = 0;
            for (STYLE_FLAGS e : values())
                if (es.contains(e))
                    v |= e.v;
            return v;
        }
    }

    enum BOUSEN_TYPE {
        BOUSEN_TYPE_NONE,
        /** Straight line */
        BOUSEN_TYPE_NORMAL,
        /** Double line */
        BOUSEN_TYPE_DOUBLE,
        /** Chain line */
        BOUSEN_TYPE_KUSARI,
        /** Dashed line */
        BOUSEN_TYPE_HASEN,
        /** Wavy line */
        BOUSEN_TYPE_NAMISEN,
    }

    enum BOUTEN_TYPE {
        BOUTEN_TYPE_NONE,
        /** Sesame */
        BOUTEN_TYPE_GOMA,
        /** White sesame */
        BOUTEN_TYPE_SIROGOMA,
        /** Bullseye */
        BOUTEN_TYPE_JYANOME,
        /** Circle */
        BOUTEN_TYPE_MARU,
        /** White circle */
        BOUTEN_TYPE_SIROMARU,
        /** Black triangle */
        BOUTEN_TYPE_KUROSANKAKU,
        /** White triangle */
        BOUTEN_TYPE_SIROSANKAKU,
        /** Double circle */
        BOUTEN_TYPE_NIJUUMARU,
        /** Cross */
        BOUTEN_TYPE_BATU,
    }
}
