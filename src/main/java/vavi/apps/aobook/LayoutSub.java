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

import vavi.apps.aobook.PvLayout.CharItem;
import vavi.apps.aobook.PvLayout.RubyItem;

/**
 * レイアウト:サブ関数
 */
class LayoutSub {

    private LayoutSub() {
    }

    /**
     * UTF-32 文字列内に c が存在するか
     * <p>
     * 値は小さい順に並んでいる。
     */
    static boolean layout_find_utf32(int c, String uc) {
        int mc;
        int low, high, mid;

        if (uc == null) return false;

        low = 0;
        high = uc.length() - 1;

        while (low <= high) {
            mid = (low + high) / 2;
            mc = uc.charAt(mid);

            if (mc == c)
                return true;
            else if (mc < c)
                low = mid + 1;
            else
                high = mid - 1;
        }

        return false;
    }

    // 縦中横の対象文字か
    static boolean layout_ischar_tatetyuyoko(int c) {
        return ((c >= '0' && c <= '9') || c == '!' || c == '?');
    }

    /** pi が行末に来てはならない文字か */
    static boolean layout_is_nobottom(LayoutWork p, CharItem pi) {
        int c = pi.code;

        // 行末禁則

        if (layout_find_utf32(c, p.u32_nobottom))
            return true;

        // 分割禁止

        if (layout_find_utf32(c, p.u32_nosep)
                && p.list_char.listIterator().hasNext()
                && p.list_char.listIterator().next().code == c)
            return true;

        // くの字点上

        if (c == 0x3033 || c == 0x3034) return true;

        return false;
    }

    /**
     * 文字を置き換え
     * <p>
     * pdst: 対象となる文字のポインタ
     * return: 置き換えが行われたか
     */
    static boolean layout_replace_char(LayoutWork p, int pdst) {
        int pmid, c;
        int low, high, mid;

        if (p.u32_replace == null) return false;

        c = pdst;

        low = 0;
        high = p.u32_replace.length() - 1;

        while (low <= high) {
            mid = (low + high) / 2;
            pmid = p.u32_replace.charAt(mid * 2);

            if (pmid == c) {
                pdst = p.u32_replace.charAt(pmid + 1);
                return true;
            } else if (pmid < c)
                low = mid + 1;
		    else
                high = mid - 1;
        }

        return false;
    }

    /**
     * ルビ位置設定時の情報取得
     * <p>
     * return: 親文字列が折り返しているか
     */
    static int layout_getrubyinfo(LayoutWork p, RubyItem pi) {
        CharItem pic;
        int i, wrap;

        wrap = 0;

        for (pic = pi.char_top, i = pi.charlen; i > 0; i--, pic = p.list_char.listIterator().next()) {
            if (pic.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)) {
                if (pic == pi.char_top)
                    // 親文字列の先頭が、折り返しの先頭
                    wrap |= 2;
                else
                    wrap |= 1;
            }
        }

        return wrap;
    }

    /** pi と next のルビの親文字列が連続しているか */
    static boolean layout_is_ruby_connect(LayoutWork p, RubyItem pi, RubyItem next) {
        CharItem pic;
        int i;

        if (pi == null || next == null) return false;

        // pi の親文字列の次の位置
        for (pic = pi.char_top, i = pi.charlen; i > 0; i--, pic = p.list_char.listIterator().next()) ;

        // next の先頭と等しいか
        return (pic == next.char_top);
    }

    /** 見出しの終わりコマンド時、現在の文字列を見出しリストに追加 */
    static void layout_append_titlelist(LayoutWork p) {
        Layout.TitleItem pi;
        int ps;
        byte[] buf;
        int len, type;

        if (p.buf_title == null) return;

        ps = 0;

        // 1byte 目は見出しタイプ

        type = p.buf_title.charAt(ps++);

        // UTF32.UTF8

        // アイテム追加

        pi = new Layout.TitleItem();
        pi.type = type;
        pi.pageno = p.pfirst.pagenum;

        pi.text = p.buf_title.substring(1);

        // 各タイプの個数を加算

        (p.title_num[type])++;
        p.plist_title.add(pi);

        // 文字列バッファリセット

        p.buf_title = null;
    }
}