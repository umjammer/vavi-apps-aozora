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


/** スタイル定義 */

class StyleDef {

    /** 文字数 */
    int chars;
    /** 行数 */
    int lines;
    /** 字間 (px) */
    int char_space;
    /** 行間 (フォント高さに対する%) */
    int line_space;
    /** ページ間の空白 */
    int page_space;
    EnumSet<DefStyle.STYLE_FLAGS> flags;
    /** ページ数 (1or2) */
    int pages;
    /** 濁点/半濁点の処理 */
    DefStyle.STYLE_DAKUTEN dakuten_type;
    /** 画面余白 */
    Insets margin = new Insets(0, 0, 0, 0);
    /** 本文文字色 */
    Color col_text;
    /** ルビ文字色 */
    Color col_ruby;
    /** ページ情報色 */
    Color col_info;
    /** 背景色 */
    Color col_bkgnd;
    /** 行頭禁則 (0 でなし。1 でデフォルト) */
    int u32_nohead;
    /** 行末禁則 */
    int u32_nobottom;
    /** ぶら下げ対象文字 */
    int u32_hanging;
    /** 分割禁止 */
    int u32_nosep;
    /** 置換 */
    int u32_replace;
    /** スタイル名 */
    String str_stylename;
    /** 本文フォント */
    String str_fontmain;
    /** ルビフォント */
    String str_fontruby;
    /** 太字フォント */
    String str_fontbold;
    /** ページ情報フォント */
    String str_fontinfo;
    /** 背景画像ファイル名 */
    String str_bkgndimg;

    /** 設定をコピー */
    void StyleDef_copy(StyleDef dst) {
        int[] ptr_src = new int[5];
        int[] ptr_dst = new int[5];
        int ppsrc;
        int i;

        // 値をコピー

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

        // 文字列挙
        dst.StyleDef_getCharsArray(ptr_dst);
        this.StyleDef_getCharsArray(ptr_src);

        for (i = 0; i < 5; i++) {
            ppsrc = ptr_src[i];

            if (ppsrc != 0 && ppsrc != DefStyle.STYLE_CHARS_DEFAULT)
                (ptr_dst[i]) = ppsrc;
            else
                (ptr_dst[i]) = ppsrc;
        }

        // String

        dst.str_stylename = this.str_stylename;
        dst.str_fontmain = this.str_fontmain;
        dst.str_fontruby = this.str_fontruby;
        dst.str_fontbold = this.str_fontbold;
        dst.str_fontinfo = this.str_fontinfo;
        dst.str_bkgndimg = this.str_bkgndimg;
    }

    /**
     * 文字列挙の文字列をセット (UTF-8 から)
     * <p>
     * text: 1 でデフォルト
     */
    void StyleDef_setCharsText(String ppdst, final byte[] text) {
        // セット
        if (text[0] == 1)
            ppdst = String.valueOf(DefStyle.STYLE_CHARS_DEFAULT);
	    else if (text != null || text[0] == 0)
            ppdst = null;
        else
            ppdst = new String(text);
    }

    /**
     * 文字列挙のデフォルト文字列を取得
     * <p>
     * no: 0〜3
     */
    String StyleDef_getCharsDefault(int no) {
        return switch (no) {
            case 0 -> Style.g_u32_nohead;
            case 1 -> Style.g_u32_nobottom;
            case 2 -> Style.g_u32_hanging;
            default -> Style.g_u32_nosep;
        };
    }

    // レイアウト時用の文字列挙作成

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
     * レイアウト時用の置き換え文字列作成
     * <p>
     * 半角空白は除外。対になっていないものは除去。
     * 置き換え元の小さい順に並べる。
     * <p>
     * len: 2文字1組での数
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
            // 置き換え元と置き換え先の文字

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

            // 挿入位置

            for (pins = 0; pins != 0 && csrc > pins; pins += 2) ;

            // 挿入位置以降をずらす

            if (buf.charAt(pins) != 0)
            {
                for (ptmp = len * 2; ptmp > pins; ptmp -= 2) {
                    buf.insert(ptmp + 0, buf.charAt(ptmp -2));
                    buf.insert(ptmp + 1, buf.charAt(ptmp -1));
                }
            }

            // セット

            buf.insert(pins + 0, csrc);
            buf.insert(pins + 1, cdst);

            len++;
            buf.insert(len * 2, 0);
        }

        return buf.toString();
    }

    /** 文字列挙のポインタの配列をセット */
    void StyleDef_getCharsArray(int[] dst) {
        dst[0] = this.u32_nohead;
        dst[1] = this.u32_nobottom;
        dst[2] = this.u32_hanging;
        dst[3] = this.u32_nosep;
        dst[4] = this.u32_replace;
    }

    /**
     * デフォルト設定をセット
     * <p>
     * name: スタイル名
     */
    void StyleDef_setDefault(final String name) {
        this.chars = 32;
        this.lines = 15;
        this.char_space = 0;
        this.line_space = 80;
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
