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
 * Layout internal usage
 */
class PvLayout {

    private PvLayout() {
    }

    static class StringItem {}

    /** 1 character item of body text */
    static class CharItem extends StringItem {
        /** Unicode */
        int code;
        /** Drawing position (including padding) */
        int x, y;
        /** Character width of horizontal within vertical (px) */
        int width;
        /** Character height (px). Including padding */
        int height;
        /** Y padding when drawing text */
        int padding;
        /** Character arrangement type */
        PvLayout.CHARITEM_TYPE chartype;
        EnumSet<CHARITEM_F> flags;
        /** Emphasis dot type */
        int bouten;
        /** Emphasis line type */
        DefStyle.BOUSEN_TYPE bousen = DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE;
        /** Number of characters for horizontal within vertical */
        int horzcnt;
        /** ASCII characters for horizontal within vertical (no null) */
        final byte[] horzchar = new byte[4];
    }

    enum CHARITEM_TYPE {
        /** Normal vertical writing */
        CHARITEM_TYPE_NORMAL,
        /** Horizontal layout of European text */
        CHARITEM_TYPE_ROTATE,
        /** Horizontal within vertical */
        CHARITEM_TYPE_HORZ
    }

    enum CHARITEM_F {
        /** Is the first character of wrapping */
        CHARITEM_F_WRAP_TOP(1 << 0),
        /** Dakuten combination (If next character is dakuten, delete dakuten character and turn ON flag) */
        CHARITEM_F_DAKUTEN(1 << 1),
        /** Handakuten combination */
        CHARITEM_F_HANDAKUTEN(1 << 2),
        /** Bottom aligned / Raise */
        CHARITEM_F_JIAGE(1 << 3),
        /** Bold */
        CHARITEM_F_BOLD(1 << 4);
        final int v;

        CHARITEM_F(int v) {
            this.v = v;
        }
    }

    /** Ruby item (Separate list from parent string) */
    static class RubyItem {
        /** Head position of parent string */
        CharItem char_top;
        /** Head position of ruby string (position in internal data) */
        String rubytxt;
        /** Ruby length */
        int rubylen;
        /** Parent string length */
        int charlen;
        int x;
        /** Drawing position (Basically same position as parent string) */
        int y;
        /** Total height of ruby string (px) */
        int ruby_h;
        /** Height of parent string */
        int char_h;
    }

    /** No padding between ruby */
    static final int RUBYITEM_CHARH_NO_PADDING = -1;

    /** Line state (Affects current line only) */
    static class LineState {
        /** Head character of horizontal within vertical */
        CharItem tatetyuyoko_top;
        /** Current number of characters for horizontal within vertical (0:None, 1:Start, 2~:Count+1) */
        int tatetyuyoko_num;
        /** Indent count */
        int jisage;
        /** Bottom aligned / Raise count (+1) */
        int jiage;
        /** Emphasis dot (0 for none) */
        int bouten;
        /** Emphasis line */
        DefStyle.BOUSEN_TYPE bousen = DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE;
    }

    /** Block annotation state (Value 0 for none) */
    static class BlockState {
        EnumSet<BLOCKSTATE_F> flags;
        /** Indent */
        int jisage;
        /** Indent after wrapping */
        int jisage_wrap;
        /** Bottom aligned / Raise (1=Bottom aligned, 2~=n-character raise from bottom) */
        int jiage;
        /** Title (0:None 1:Large 2:Medium 3:Small) */
        int title;
    }

    enum BLOCKSTATE_F {
        /** Horizontal layout */
        BLOCKSTATE_F_YOKOGUMI(1 << 0),
        /** Bold */
        BLOCKSTATE_F_BOLD(1 << 1);
        final int v;

        BLOCKSTATE_F(int v) {
            this.v = v;
        }
    }

    /** Page state */
    static class PageState {
        /** Center horizontally */
        boolean fcenter;
        /** Data position of picture command (position next to command type) */
        int picture;
    }

    List<PageInfo> pageInfos;

    /** Page information */
    static class PageInfo {
        /** Head position of internal data */
        String src;
        /** Current block annotation state */
        BlockState blockstate;
        /** Page position */
        int pageno;
        /** Line number of the source text at the beginning */
        int lineno;
        /** Wrap line count of previous line on previous page (Shift amount on next page) */
        int wrap_num;
        /** Head X position when centered horizontally */
        int diffx;
        int flags;
    }

    /** Blank page */
    static final int PAGEINFO_F_BLANK = 1;

    /** Work data for initial layout */
    static class LayoutFirst {
        /** Current page info */
        PageInfo curpage;
        /** Next page info */
        PageInfo nextpage;
        /** Total page count */
        int pagenum;
    }
}
