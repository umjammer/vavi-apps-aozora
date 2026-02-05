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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumSet;

import static vavi.apps.aobook.Layout.gdat;
import static vavi.apps.aobook.Layout.mFontGetVertHeight;


/** Style information currently in use */
class StyleWork {

    StyleDef b;

    Font font_main;
    Font font_ruby;
    Font font_bold;
    Font font_info;
    BufferedImage img_bkgnd;

    // StyleWork (Current style data)
    private final int fontCreateFRuby = 10;

    /** Create fonts */
    void StyleWork_createFont() {
        this.font_main = new Font(this.b.str_fontmain, Font.PLAIN, 16);
        this.font_ruby = new Font(this.b.str_fontruby, Font.PLAIN, fontCreateFRuby);
        this.font_bold = new Font(this.b.str_fontbold, Font.PLAIN, 16);

        // For page info, use GUI font because it's not used in thread
        this.font_info = new Font(Font.DIALOG, Font.PLAIN, fontCreateFRuby);

        // Set kenten font size (same as ruby)
        if (gdat != null) {
            gdat.font_kenten = new Font(this.b.str_fontruby, Font.PLAIN, fontCreateFRuby);
        }
    }

    /** Get screen size */
    void StyleWork_getScreenSize(Dimension dst) {
        int fonth, linesp;

        Graphics2D g = img_bkgnd.createGraphics();
        fonth = mFontGetVertHeight(g, this.font_main).height;
        linesp = (int) (fonth * (this.b.line_space / 100.0) + 0.5);

        dst.width = (fonth + linesp) * this.b.lines * this.b.pages +
                this.b.page_space * (this.b.pages - 1) +
                this.b.margin.left + this.b.margin.right;

        dst.height = (fonth + this.b.char_space) * this.b.chars - this.b.char_space
                + this.b.margin.top + this.b.margin.bottom;
    }

    /** Read specified style */
    void StyleWork_readStyle(final String[] name) throws IOException {
        // Initialize b if null
        if (this.b == null) {
            this.b = new StyleDef(name != null && name.length > 0 && name[0] != null ? name[0] : "default");
        }

        // Read style
        Style.StyleConf_readStyle(this.b, name);

        // Create each component
        this.StyleWork_createFont();

        this.img_bkgnd = Style._load_image(this.b.str_bkgndimg);
    }

    /**
     * Apply values to current style
     * <p>
     *
     * @return 0: No update, 1: Screen update only, 2: Relayout
     */
    int StyleWork_apply(StyleDef ps) {
        // Did font change?
        boolean change_font = (!ps.str_fontmain.equals(this.b.str_fontmain) ||
                !ps.str_fontruby.equals(this.b.str_fontruby) ||
                !ps.str_fontbold.equals(this.b.str_fontbold) ||
                !ps.str_fontinfo.equals(this.b.str_fontinfo));

        // Did character enumeration change?
        boolean change_chars = (Style._ischange_chars(ps.u32_nohead, this.b.u32_nohead) ||
                Style._ischange_chars(ps.u32_nobottom, this.b.u32_nobottom) ||
                Style._ischange_chars(ps.u32_hanging, this.b.u32_hanging) ||
                Style._ischange_chars(ps.u32_nosep, this.b.u32_nosep) ||
                Style._ischange_chars(ps.u32_replace, this.b.u32_replace));

        // Did background image change?
        boolean change_bkgndimg = !ps.str_bkgndimg.equals(this.b.str_bkgndimg);

        // Update judgment
        EnumSet<DefStyle.STYLE_FLAGS> f1 = EnumSet.of(DefStyle.STYLE_FLAGS.STYLE_F_HANGING, DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE);
        EnumSet<DefStyle.STYLE_FLAGS> f2 = EnumSet.of(DefStyle.STYLE_FLAGS.STYLE_F_BKGND_TILE, DefStyle.STYLE_FLAGS.STYLE_F_DASH_TO_LINE, DefStyle.STYLE_FLAGS.STYLE_F_REPLACE_PRINT);

        int ret;
        if (ps.pages != this.b.pages ||
                ps.chars != this.b.chars ||
                ps.lines != this.b.lines ||
                ps.flags.containsAll(f1) != this.b.flags.containsAll(f1) ||
                change_font ||
                change_chars) {
            // Relayout
            ret = 2;
        } else if (ps.char_space != this.b.char_space ||
                ps.line_space != this.b.line_space ||
                ps.page_space != this.b.page_space ||
                ps.flags.containsAll(f2) != this.b.flags.containsAll(f2) ||
                ps.dakuten_type != this.b.dakuten_type ||
                ps.margin.left != this.b.margin.left ||
                ps.margin.top != this.b.margin.top ||
                ps.margin.right != this.b.margin.right ||
                ps.margin.bottom != this.b.margin.bottom ||
                !ps.col_text.equals(this.b.col_text) ||
                !ps.col_ruby.equals(this.b.col_ruby) ||
                !ps.col_info.equals(this.b.col_info) ||
                !ps.col_bkgnd.equals(this.b.col_bkgnd) ||
                change_bkgndimg) {
            // Screen update only
            ret = 1;
        } else {
            ret = 0;
        }

        // Copy data
        this.b.StyleDef_copy(ps);

        // Recreate font
        if (change_font)
            this.StyleWork_createFont();

        // Load background image
        if (change_bkgndimg) {
            try {
                this.img_bkgnd = Style._load_image(ps.str_bkgndimg);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return ret;
    }
}
