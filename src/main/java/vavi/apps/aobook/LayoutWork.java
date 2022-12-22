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


/** レイアウト作業用データ */
class LayoutWork {

    /** 内部データの現在位置 */
    String text;
    int textP;
    /** 本文高さ用フラグ */
    int[] buf_hflags;
    /** スタイルデータ */
    StyleWork style;
    StyleDef stdef;
    /** 描画時の描画先 */
    Graphics2D img;
    /** 最初のレイアウト用データ */
    PvLayout.LayoutFirst pfirst;

    /** 文字列挙 (Unicode 小さい順に並んでいる) */
    String u32_nohead;
    String u32_nobottom;
    String u32_hanging;
    String u32_nosep;
    String u32_replace;

    Font font_main;
    Font font_ruby;
    Font font_bold;
    Font font_kenten;

    /** 本文の色 */
    Color pixcol_text;

    /** フォントの高さ */
    int fontmain_h;
    int fontbold_h;
    int fontruby_h;
    int fontkenten_h;
    /** 全ページ数 (描画時) */
    int pagenum;
    /** 1ページ分のテキスト描画部分のサイズ (余白部分は除く) */
    int pageW, pageH;
    /** 1ページの先頭行の X 位置 */
    int text_right_x;
    /** 行幅 (次の行までの px 数) */
    int line_width;

    /** 先頭行の字下げY位置 (px) */
    int jisage_y;
    /** 折り返し時の字下げ Y位置 */
    int jisage_wrap_y;
    /** 地からn字上げの下端 Y位置 */
    int jiage_bottom;
    /** 現在のテキスト行位置 (内部データで行情報が見つかるたびに更新) */
    int curlineno;

    /** 見出しの各個数 */
    int[] title_num = new int[3];

    /** 本文文字データ (1行分の作業用) */
    List<PvLayout.CharItem> list_char;
    /** ルビデータ */
    List<PvLayout.RubyItem> list_ruby;
    /** 見出しアイテムの出力リスト (最初のレイアウト時) */
    List<PvLayout.StringItem> plist_title;
    /** 見出しの文字列用 (最初のレイアウト時) */
    String buf_title;

    PvLayout.PageState pagestate;
    PvLayout.BlockState blockstate;
    PvLayout.LineState linestate;
}
