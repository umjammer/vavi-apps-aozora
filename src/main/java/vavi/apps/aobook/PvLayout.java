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
import java.util.List;


/**
 * レイアウト内部用
 */
class PvLayout {

    static class StringItem {}

    /** 本文1文字のアイテム */
    static class CharItem extends StringItem {
        /** Unicode */
        int code;
        /** 描画位置(余白含む) */
        int x, y;
        /** 縦中横の文字幅 (px) */
        int width;
        /** 文字高さ (px)。余白含む */
        int height;
        /** テキスト描画時のy余白 */
        int padding;
        /** 文字の配置タイプ */
        PvLayout.CHARITEM_TYPE chartype;
        EnumSet<CHARITEM_F> flags;
        /** 傍点タイプ */
        int bouten;
        /** 傍線タイプ */
        DefStyle.BOUSEN_TYPE bousen;
        /** 縦中横の文字数 */
        int horzcnt;
        /** 縦中横のASCII文字 (null なし) */
        byte[] horzchar = new byte[3];
    }

    enum CHARITEM_TYPE {
        /** 通常縦書き */
        CHARITEM_TYPE_NORMAL,
        /** 欧文横組み */
        CHARITEM_TYPE_ROTATE,
        /** 縦中横 */
        CHARITEM_TYPE_HORZ
    }

    enum CHARITEM_F {
        /** 折り返しの先頭文字である */
        CHARITEM_F_WRAP_TOP(1 << 0),
        /** 濁点結合 (次の文字が濁点の場合、濁点文字を削除して、フラグをON) */
        CHARITEM_F_DAKUTEN(1 << 1),
        /** 半濁点結合 */
        CHARITEM_F_HANDAKUTEN(1 << 2),
        /** 地付き/地上げ */
        CHARITEM_F_JIAGE(1 << 3),
        /** 太字 */
        CHARITEM_F_BOLD(1 << 4);
        final int v;

        CHARITEM_F(int v) {
            this.v = v;
        }
    }

    /** ルビアイテム (親文字列とは別のリストに) */
    static class RubyItem {
        /** 親文字列の先頭位置 */
        CharItem char_top;
        /** ルビ文字列先頭位置 (内部データの位置) */
        String rubytxt;
        /** ルビ文字数 */
        int rubylen;
        /** 親文字列数 */
        int charlen;
        int x;
        /** 描画位置 (基本的に親文字列と同じ位置) */
        int y;
        /** ルビ文字列全体の高さ (px) */
        int ruby_h;
        /** 親文字列の高さ */
        int char_h;
    }

    /** ルビの間に余白なし */
    static final int RUBYITEM_CHARH_NO_PADDING = -1;

    /** 行の状態 (現在行のみに影響する) */
    static class LineState {
        /** 縦中横の先頭文字 */
        CharItem tatetyuyoko_top;
        /** 縦中横の現在の文字数 (0:なし、1で開始、2〜で文字数+1) */
        int tatetyuyoko_num;
        /** 字下げ数 */
        int jisage;
        /** 地付き/字上げ数 (+1) */
        int jiage;
        /** 傍点 (0 でなし) */
        int bouten;
        /** 傍線 */
        DefStyle.BOUSEN_TYPE bousen;
    }

    /** ブロック型注記の状態 (値はそれぞれ 0 でなし) */
    static class BlockState {
        EnumSet<BLOCKSTATE_F> flags;
        /** 字下げ */
        int jisage;
        /** 折り返し以降の字下げ */
        int jisage_wrap;
        /** 地付き/字上げ (1=地付き, 2〜=地からn字上げ) */
        int jiage;
        /** 見出し (0:なし 1:大 2:中 3:小) */
        int title;
    }

    enum BLOCKSTATE_F {
        /** 横組み */
        BLOCKSTATE_F_YOKOGUMI(1 << 0),
        /** 太字 */
        BLOCKSTATE_F_BOLD(1 << 1);
        final int v;

        BLOCKSTATE_F(int v) {
            this.v = v;
        }
    }

    /** ページの状態 */
    static class PageState {
        /** ページの左右中央 */
        boolean fcenter;
        /** 挿絵コマンドのデータ位置 (コマンドタイプの次の位置) */
        int picture;
    }

    List<PageInfo> pageInfos;

    /** ページ情報 */
    static class PageInfo {
        /** 内部データの先頭位置 */
        String src;
        /** 現在のブロック型注記の状態 */
        BlockState blockstate;
        /** ページ位置 */
        int pageno;
        /** 先頭のソーステキストの行番号 */
        int lineno;
        /** 前ページの前行の折り返し行数 (次ページでずらす分) */
        int wrap_num;
        /** ページの左右中央時の先頭 X 位置 */
        int diffx;
        int flags;
    }

    /** 空白ページ */
    static final int PAGEINFO_F_BLANK = 1;

    /** 最初のレイアウト時用の作業データ */
    static class LayoutFirst {
        /** 現在のページ情報 */
        PageInfo curpage;
        /** 次のページの情報 */
        PageInfo nextpage;
        /** 全ページ数 */
        int pagenum;
    }
}