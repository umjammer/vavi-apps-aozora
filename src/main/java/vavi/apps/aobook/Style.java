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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;

import vavi.apps.aobook.DefStyle.STYLE_FLAGS;

import static vavi.apps.aobook.DefStyle.STYLE_CHARS_DEFAULT;


/**
 * Style functions
 */
class Style {

    private Style() {
    }

    private static final Logger logger = System.getLogger(Style.class.getName());

    static final String STYLE_CONFIGNAME = "styles.conf";

    static final String g_u32_nohead = "）〕］｝〉》」』】〙〗、。，．・：；!?！？‼⁇⁈⁉ヽヾゝゞ々〻゜’”゛〟ーぁぃぅぇぉっゃゅょゎァィゥェォッャュョヮヵヶ";
    static final String g_u32_nobottom = "［｛〔〈《「『【〘〖〝‘“";
    static final String g_u32_hanging = "、。，．";
    static final String g_u32_nosep = "…‥";

    /** Load image */
    static BufferedImage _load_image(String str) throws IOException {
logger.log(Level.TRACE, "str: " + str);
        return ImageIO.read(Paths.get(str).toFile());
    }

    /**
     * Character enumeration: Changed?
     *
     * @return true if changed
     */
    static boolean _ischange_chars(int p1, int p2) {
        if (p1 == STYLE_CHARS_DEFAULT || p2 == STYLE_CHARS_DEFAULT)
            return (p1 != p2);
        else
            return p1 == p2;
    }

    /**
     * Create character enumeration for layout
     * (Sort by Unicode in ascending order)
     */
    static String _create_layout_chars(int src, String def) {
        if (src == -1) return null;
        if (def == null || def.isEmpty()) return null;

        // Collect characters and sort by Unicode value
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < def.length(); i++) {
            chars.add(def.charAt(i));
        }
        Collections.sort(chars);

        StringBuilder buf = new StringBuilder();
        for (Character c : chars) {
            buf.append(c);
        }
        return buf.toString();
    }

    // Configuration file

    /** Read character enumeration from configuration file */
    private void _readconfig_chars(Preferences ini, final String key, DefStyle[] ppdst) {
        final String txt;

        txt = ini.get(key, null);

        if (txt == null)
            // Default if key is missing
		    ppdst[0] = null;
	    else if (txt.isEmpty())
            // Empty string
            ppdst[0] = null;
        else
            ppdst[0] = new DefStyle(txt);
    }

    /** Write character enumeration */
    static private void _writeconfig_chars(Preferences fp, final String key, int str) {
        if (str != STYLE_CHARS_DEFAULT) {
            if (str != 0)
                fp.put(key, null);
            else {
                fp.put(key, String.valueOf(str));
            }
        }
    }

    /**
     * Open style configuration file
     * <p>
     * return: Number of styles
     */
    static int StyleConf_openRead() {
        Preferences ini = Preferences.userNodeForPackage(Style.class);
        int num;

//        mIniRead_loadFile_join(ini, mGuiGetPath_config_text(), STYLE_CONFIGNAME);

        //

        String group = "styles.";

        if (ini.getInt(group + "ver", 0) != 1)
            ini.put(group + "ver", "");

        num = ini.getInt("num", 0);

        //

        return num;
    }

    /** Read specified style from configuration file */
    static void StyleConf_readStyle(StyleDef p, final String[] name) {
        Preferences ini = Preferences.userNodeForPackage(Style.class);
        int i, num;

        num = StyleConf_openRead();

        // Search by style name and read

        for (i = 0; i < num; i++) {
            name[0] = ini.get("name", "");
            if (!name[0].isEmpty()) {
                StyleConf_readDefine(ini, p);
                break;
            }
        }

        // Default if not found

        if (i == num)
            p.StyleDef_setDefault("default");
    }

    /**
     * Read values from configuration file
     * <p>
     * (Group is already set, StyleDef is empty)
     */
    static void StyleConf_readDefine(Preferences ini, StyleDef p) {
        p.str_stylename = ini.get("name", null);

        p.pages = ini.getInt("pages", 1);
        p.chars = ini.getInt("chars", 32);
        p.lines = ini.getInt("lines", 15);
        p.char_space = ini.getInt("charspace", 0);
        p.line_space = ini.getInt("linespace", 80);
        p.page_space = ini.getInt("pagespace", 30);

        p.flags = STYLE_FLAGS.valueOf(ini.getInt("flags", DefStyle.STYLE_FLAGS.STYLE_F_HANGING.v | DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE.v));

        p.dakuten_type = DefStyle.STYLE_DAKUTEN.values()[ini.getInt("dakuten", DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE_HORZ.ordinal())];

        p.margin.left = ini.getInt("mgleft", 30);
        p.margin.right = ini.getInt("mgright", 30);
        p.margin.top = ini.getInt("mgtop", 30);
        p.margin.bottom = ini.getInt("mgbottom", 30);

        p.col_text = new Color(ini.getInt("coltext", 0));
        p.col_ruby = new Color(ini.getInt("colruby", 0x400000));
        p.col_info = new Color(ini.getInt("colinfo", (0 << 16) | (114 << 8) | 0));
        p.col_bkgnd = new Color(ini.getInt("colbkgnd", 0xfaf2e3));

        p.str_fontmain = ini.get("fontmain", "size=13");
        p.str_fontruby = ini.get("fontruby", "size=7");
        p.str_fontbold = ini.get("fontbold", "size=13");
        p.str_fontinfo = ini.get("fontinfo", "size=9");
        p.str_bkgndimg = ini.get("bkgndfile", null);

        p.u32_nohead = ini.getInt("nohead", 0);
        p.u32_nobottom = ini.getInt("nobottom", 0);
        p.u32_hanging = ini.getInt("hanging", 0);
        p.u32_nosep = ini.getInt("nosep", 0);
        p.u32_replace = ini.getInt("replace", 0);

        // Character replacement defaults to none

        if (p.u32_replace == STYLE_CHARS_DEFAULT)
            p.u32_replace = 0;
    }

    /**
     * Open configuration file for writing
     * <p>
     * num: Number of styles
     */
    static Preferences StyleConf_openWrite(int num) {
        Preferences prefs = Preferences.userNodeForPackage(Style.class);

        String group = "styles.";
        prefs.putInt(group + "ver", 1);
        prefs.putInt(group + "num", num);

        return prefs;
    }

    /** Write style */
    static void StyleConf_writeDefine(Preferences fp, int no, StyleDef p) {
        String group = no + ".";

        fp.put(group + "name", p.str_stylename);

        fp.putInt(group + "pages", p.pages);
        fp.putInt(group + "chars", p.chars);
        fp.putInt(group + "lines", p.lines);
        fp.putInt(group + "charspace", p.char_space);
        fp.putInt(group + "linespace", p.line_space);
        fp.putInt(group + "pagespace", p.page_space);
        fp.putInt(group + "flags", STYLE_FLAGS.valueOf(p.flags));

        fp.putInt(group + "dakuten", p.dakuten_type.ordinal());

        fp.putInt(group + "mgleft", p.margin.left);
        fp.putInt(group + "mgright", p.margin.right);
        fp.putInt(group + "mgtop", p.margin.top);
        fp.putInt(group + "mgbottom", p.margin.bottom);

        fp.putInt(group + "coltext", p.col_text.getRGB());
        fp.putInt(group + "colruby", p.col_ruby.getRGB());
        fp.putInt(group + "colinfo", p.col_info.getRGB());
        fp.putInt(group + "colbkgnd", p.col_bkgnd.getRGB());

        fp.put(group + "fontmain", p.str_fontmain);
        fp.put(group + "fontruby", p.str_fontruby);
        fp.put(group + "fontbold", p.str_fontbold);
        fp.put(group + "fontinfo", p.str_fontinfo);
        fp.put(group + "bkgndfile", p.str_bkgndimg);

        fp.putInt("nohead", p.u32_nohead);
        fp.putInt("nobottom", p.u32_nobottom);
        fp.putInt("hanging", p.u32_hanging);
        fp.putInt("nosep", p.u32_nosep);
        fp.putInt("replace", p.u32_replace);
    }
}
