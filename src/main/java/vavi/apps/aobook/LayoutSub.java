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
 * Layout: Sub functions
 */
class LayoutSub {

    private LayoutSub() {
    }

    /**
     * Check if c exists in UTF-32 string
     * <p>
     * Values are sorted in ascending order.
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

    // Is it a target character for horizontal within vertical
    static boolean layout_ischar_tatetyuyoko(int c) {
        return ((c >= '0' && c <= '9') || c == '!' || c == '?');
    }

    /** Should pi not be at the end of a line? */
    static boolean layout_is_nobottom(LayoutWork p, CharItem pi) {
        int c = pi.code;

        // Line end restriction

        if (layout_find_utf32(c, p.u32_nobottom))
            return true;

        // Separation prohibited

        if (layout_find_utf32(c, p.u32_nosep)) {
            int idx = p.list_char.indexOf(pi);
            if (idx != -1 && idx < p.list_char.size() - 1 && p.list_char.get(idx + 1).code == c)
                return true;
        }

        // On kunojiten

        if (c == 0x3033 || c == 0x3034) return true;

        return false;
    }

    /**
     * Replace character
     * <p>
     * pdst: Pointer to target character
     * return: Whether replacement was performed
     */
    static boolean layout_replace_char(LayoutWork p, int pdst) {
        int pmid, c;
        int low, high, mid;

        if (p.u32_replace == null) return false;

        c = pdst;

        low = 0;
        high = p.u32_replace.length() / 2 - 1;

        while (low <= high) {
            mid = (low + high) / 2;
            pmid = p.u32_replace.charAt(mid * 2);

            if (pmid == c) {
                pdst = p.u32_replace.charAt(mid * 2 + 1);
                return true;
            } else if (pmid < c)
                low = mid + 1;
		    else
                high = mid - 1;
        }

        return false;
    }

    /**
     * Get information when setting ruby position
     * <p>
     * return: Whether parent string wraps
     */
    static int layout_getrubyinfo(LayoutWork p, RubyItem pi) {
        CharItem pic;
        int i, wrap;

        wrap = 0;
        int idx = p.list_char.indexOf(pi.char_top);
        if (idx == -1) return 0;

        for (i = 0; i < pi.charlen && idx + i < p.list_char.size(); i++) {
            pic = p.list_char.get(idx + i);
            if (pic.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)) {
                if (i == 0)
                    // Head of parent string is head of wrap
                    wrap |= 2;
                else
                    wrap |= 1;
            }
        }

        return wrap;
    }

    /** Do the ruby parent strings of pi and next continue? */
    static boolean layout_is_ruby_connect(LayoutWork p, RubyItem pi, RubyItem next) {
        if (pi == null || next == null) return false;

        int idx = p.list_char.indexOf(pi.char_top);
        if (idx == -1) return false;

        // Next position of pi's parent string
        int next_idx = idx + pi.charlen;
        if (next_idx >= p.list_char.size()) return false;

        // Equal to head of next
        return (p.list_char.get(next_idx) == next.char_top);
    }

    /** Add current string to title list at end of title command */
    static void layout_append_titlelist(LayoutWork p) {
        Layout.TitleItem pi;
        int ps;
        byte[] buf;
        int len, type;

        if (p.buf_title == null) return;

        ps = 0;

        // 1st byte is title type

        type = p.buf_title.charAt(ps++);

        // UTF32.UTF8

        // Add item

        pi = new Layout.TitleItem();
        pi.type = type;
        pi.pageno = p.pfirst.pagenum;

        pi.text = p.buf_title.substring(1);

        // Add to count of each type

        (p.title_num[type])++;
        p.plist_title.add(pi);

        // Reset string buffer

        p.buf_title = null;
    }
}
