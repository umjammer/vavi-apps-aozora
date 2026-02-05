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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;


/** Data for layout work */
class LayoutWork {

    /** Current position of internal data */
    String text;
    int textP;
    /** Flags for body text height */
    int[] buf_hflags;
    /** Style data */
    StyleWork style;
    StyleDef stdef;
    /** Drawing destination */
    Graphics2D img;
    /** Data for initial layout */
    PvLayout.LayoutFirst pfirst;

    /** Character enumeration (Sorted in ascending Unicode order) */
    String u32_nohead;
    String u32_nobottom;
    String u32_hanging;
    String u32_nosep;
    String u32_replace;

    Font font_main;
    Font font_ruby;
    Font font_bold;
    Font font_kenten;

    /** Color of body text */
    Color pixcol_text;

    /** Height of font */
    int fontmain_h;
    int fontbold_h;
    int fontruby_h;
    int fontkenten_h;
    /** Total number of pages (at drawing) */
    int pagenum;
    /** Size of text drawing area for 1 page (excluding margins) */
    int pageW, pageH;
    /** X position of the first line of 1 page */
    int text_right_x;
    /** Line width (px count to the next line) */
    int line_width;

    /** Indentation Y position of the first line (px) */
    int jisage_y;
    /** Indentation Y position when wrapping */
    int jisage_wrap_y;
    /** Bottom Y position of n-character raise from bottom */
    int jiage_bottom;
    /** Current text line position (updated every time line info is found in internal data) */
    int curlineno;

    /** Count of each title */
    final int[] title_num = new int[3];

    /** Body character data (for 1 line work) */
    List<PvLayout.CharItem> list_char;
    /** Ruby data */
    List<PvLayout.RubyItem> list_ruby;
    /** Output list of title items (at initial layout) */
    List<PvLayout.StringItem> plist_title;
    /** For title string (at initial layout) */
    String buf_title;

    PvLayout.PageState pagestate;
    PvLayout.BlockState blockstate;
    PvLayout.LineState linestate;
}
