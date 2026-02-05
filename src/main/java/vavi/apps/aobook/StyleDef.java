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
import java.awt.Insets;
import java.util.EnumSet;

import static vavi.apps.aobook.DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE_HORZ;


/** Style Definition */

class StyleDef {

    public StyleDef(String name) {
        StyleDef_setDefault(name);
    }

    /** Number of characters */
    int chars;
    /** Number of lines */
    int lines;
    /** Character spacing (px) */
    int char_space;
    /** Line spacing (% of font height) */
    int line_space;
    /** Spacing between pages */
    int page_space;
    EnumSet<DefStyle.STYLE_FLAGS> flags;
    /** Number of pages (1or2) */
    int pages;
    /** Dakuten/Handakuten processing */
    DefStyle.STYLE_DAKUTEN dakuten_type;
    /** Screen margin */
    Insets margin = new Insets(0, 0, 0, 0);
    /** Body text color */
    Color col_text;
    /** Ruby text color */
    Color col_ruby;
    /** Page info color */
    Color col_info;
    /** Background color */
    Color col_bkgnd;
    /** Line head restriction (0: None, 1: Default) */
    int u32_nohead;
    /** Line end restriction */
    int u32_nobottom;
    /** Characters subject to hanging */
    int u32_hanging;
    /** Separation prohibited */
    int u32_nosep;
    /** Replacement */
    int u32_replace;
    /** Style name */
    String str_stylename;
    /** Body font */
    String str_fontmain;
    /** Ruby font */
    String str_fontruby;
    /** Bold font */
    String str_fontbold;
    /** Page info font */
    String str_fontinfo;
    /** Background image file name */
    String str_bkgndimg = "tmp/Tate.iconset/icon_128x128.png";

    /** Copy settings */
    void StyleDef_copy(StyleDef dst) {
        // Copy values

        dst.chars = this.chars;
        dst.lines = this.lines;
        dst.char_space = this.char_space;
        dst.line_space = this.line_space;
        dst.page_space = this.page_space;
        dst.flags = this.flags;

        dst.pages = this.pages;
        dst.dakuten_type = this.dakuten_type;

        dst.margin = this.margin;

        dst.col_text = this.col_text;
        dst.col_ruby = this.col_ruby;
        dst.col_info = this.col_info;
        dst.col_bkgnd = this.col_bkgnd;

        // Character enumeration
        int[] ptr_dst = new int[5];
        dst.StyleDef_getCharsArray(ptr_dst);
        int[] ptr_src = new int[5];
        this.StyleDef_getCharsArray(ptr_src);

        System.arraycopy(ptr_src, 0, ptr_dst, 0, 5);

        // String

        dst.str_stylename = this.str_stylename;
        dst.str_fontmain = this.str_fontmain;
        dst.str_fontruby = this.str_fontruby;
        dst.str_fontbold = this.str_fontbold;
        dst.str_fontinfo = this.str_fontinfo;
        dst.str_bkgndimg = this.str_bkgndimg;
    }

    /**
     * Get default string for character enumeration
     * <p>
     * no: 0~3
     */
    String StyleDef_getCharsDefault(int no) {
        return switch (no) {
            case 0 -> Style.g_u32_nohead;
            case 1 -> Style.g_u32_nobottom;
            case 2 -> Style.g_u32_hanging;
            default -> Style.g_u32_nosep;
        };
    }

    // Create character enumeration for layout

    String StyleDef_createLayoutChars_nohead() {
        return Style._create_layout_chars(this.u32_nohead, Style.g_u32_nohead);
    }

    String StyleDef_createLayoutChars_nobottom() {
        return Style._create_layout_chars(this.u32_nobottom, Style.g_u32_nobottom);
    }

    String StyleDef_createLayoutChars_hanging() {
        return Style._create_layout_chars(this.u32_hanging, Style.g_u32_hanging);
    }

    String StyleDef_createLayoutChars_nosep() {
        return Style._create_layout_chars(this.u32_nosep, Style.g_u32_nosep);
    }

    /**
     * Create replacement string for layout
     * <p>
     * Exclude half-width spaces. Remove unpaired ones.
     * Sort by source character in ascending order.
     * <p>
     * len: Count of pairs (2 characters per set)
     */
    String StyleDef_createLayoutChars_replace() {
        StringBuilder buf;
        int src;
        int pins, ptmp, c, csrc, cdst;
        int len;

        if (this.u32_replace == -1) return null;

        src = this.u32_replace;

        len = src;

        buf = new StringBuilder();

        //

        buf = new StringBuilder();
        len = 0;

        while (true) {
            // Source and destination characters

            csrc = cdst = 0;

            while (src != 0) {
                c = (src++);

                if (c == ' ')
                    continue;
                else if (csrc == 0)
                    csrc = c;
                else if (cdst == 0) {
                    cdst = c;
                    break;
                }
            }

            if (csrc == 0 || cdst == 0) break;

            // Insertion position

            for (pins = 0; pins != 0 && csrc > pins; pins += 2) ;

            // Shift after insertion position

            if (buf.charAt(pins) != 0)
            {
                for (ptmp = len * 2; ptmp > pins; ptmp -= 2) {
                    buf.insert(ptmp + 0, buf.charAt(ptmp -2));
                    buf.insert(ptmp + 1, buf.charAt(ptmp -1));
                }
            }

            // Set

            buf.insert(pins + 0, csrc);
            buf.insert(pins + 1, cdst);

            len++;
            buf.insert(len * 2, 0);
        }

        return buf.toString();
    }

    /** Set array of pointers to character enumeration */
    void StyleDef_getCharsArray(int[] dst) {
        dst[0] = this.u32_nohead;
        dst[1] = this.u32_nobottom;
        dst[2] = this.u32_hanging;
        dst[3] = this.u32_nosep;
        dst[4] = this.u32_replace;
    }

    /**
     * Set default settings
     * <p>
     * name: Style name
     */
    void StyleDef_setDefault(final String name) {
        this.chars = 32;
        this.lines = 15;
        this.char_space = 2;
        this.line_space = 100;
        this.page_space = 30;
        this.flags = EnumSet.of(DefStyle.STYLE_FLAGS.STYLE_F_HANGING, DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE);

        this.pages = 1;
        this.dakuten_type = STYLE_DAKUTEN_COMBINE_HORZ;

        this.margin.left = this.margin.right = this.margin.top = this.margin.bottom = 30;

        this.col_text = new Color(0, 0, 0);
        this.col_ruby = new Color(0x30, 0x09, 0x09);
        this.col_info = new Color(0x24, 0x4D, 0x26);
        this.col_bkgnd = new Color(0xfa, 0xf2, 0xe3);

        this.u32_nohead = DefStyle.STYLE_CHARS_DEFAULT;
        this.u32_nobottom = DefStyle.STYLE_CHARS_DEFAULT;
        this.u32_hanging = DefStyle.STYLE_CHARS_DEFAULT;
        this.u32_nosep = DefStyle.STYLE_CHARS_DEFAULT;
        this.u32_replace = -1;

        this.str_stylename = name;
        this.str_fontmain = "size=13";
        this.str_fontruby = "size=7";
        this.str_fontbold = "size=13";
        this.str_fontinfo = "size=9";
    }
}
