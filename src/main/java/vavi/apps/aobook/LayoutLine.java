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

import java.awt.Font;
import java.util.EnumSet;
import java.util.List;

import vavi.apps.aobook.PvLayout.CharItem;

import static vavi.apps.aobook.Layout.mFontGetVertHeight;
import static vavi.apps.aobook.LayoutSub.layout_ischar_tatetyuyoko;
import static vavi.apps.aobook.PvLayout.CHARITEM_F.CHARITEM_F_BOLD;


/**
 * Layout
 * <p>
 * Get layout data for one line from internal data
 */
class LayoutLine {

    private LayoutLine() {
    }

    /**
     * Annotation commands
     * START+1 = END must be satisfied
     */
    enum COMMAND {
        COMMAND_NONE,
        /** Picture (during conversion only) */
        COMMAND_PICTURE,
        /** New signature */
        COMMAND_KAITYO,
        /** New page */
        COMMAND_KAIPAGE,
        /** New spread */
        COMMAND_KAIMIHIRAKI,
        /** Center horizontally */
        COMMAND_PAGE_CENTER,
        /** Title [val1=type] */
        COMMAND_TITLE_START,
        COMMAND_TITLE_END,
        /** n-character indent (1 line) [val1=indent count, val2=wrap indent count] */
        COMMAND_JISAGE_LINE,
        /** n-character indent */
        COMMAND_JISAGE_START,
        /** Indent end (common) */
        COMMAND_JISAGE_END,
        /** n-character indent, wrap and n-character indent */
        COMMAND_JISAGE_WRAP_START,
        /** Newline at top, wrap and n-character indent */
        COMMAND_JISAGE_TEN_WRAP_START,
        /** Bottom aligned / n-character raise from bottom (1 line) [val1=raise count (1=bottom aligned, 2~=n-character raise)] */
        COMMAND_JIAGE_LINE,
        /** Start */
        COMMAND_JIAGE_START,
        /** End */
        COMMAND_JIAGE_END,
        /** Bold */
        COMMAND_BOLD_START,
        COMMAND_BOLD_END,
        /** Horizontal within vertical */
        COMMAND_TATETYUYOKO_START,
        COMMAND_TATETYUYOKO_END,
        /** Horizontal layout */
        COMMAND_YOKOGUMI_START,
        COMMAND_YOKOGUMI_END,
        /** Emphasis dot [val1=type] */
        COMMAND_BOUTEN_START,
        COMMAND_BOUTEN_END,
        /** Emphasis line [val1=type] */
        COMMAND_BOUSEN_START,
        COMMAND_BOUSEN_END
    }

    // Command processing

    /**
     * Processing commands without values
     * <p>
     * cmd: Command number
     * return: Command number if page-related command. Otherwise -1.
     */
    private static COMMAND _proc_command_noval(LayoutWork p, COMMAND cmd) {
        PvLayout.LineState lst = p.linestate;
        PvLayout.BlockState bst = p.blockstate;

        switch (cmd) {
        // New page/New signature/New spread
        case COMMAND_KAIPAGE:
        case COMMAND_KAITYO:
        case COMMAND_KAIMIHIRAKI:
            return cmd;
        // Indent end [block]
        case COMMAND_JISAGE_END:
            bst.jisage = bst.jisage_wrap = 0;
            break;
        // Horizontal within vertical [line]
        case COMMAND_TATETYUYOKO_START:
            lst.tatetyuyoko_num = 1;
            break;
        case COMMAND_TATETYUYOKO_END:
            lst.tatetyuyoko_num = 0;
            break;
        // Horizontal layout
        case COMMAND_YOKOGUMI_START:
            bst.flags.add(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_YOKOGUMI);
            break;
        case COMMAND_YOKOGUMI_END:
            bst.flags.remove(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_YOKOGUMI);
            break;
        // Bold
        case COMMAND_BOLD_START:
            bst.flags.add(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_BOLD);
            break;
        case COMMAND_BOLD_END:
            bst.flags.remove(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_BOLD);
            break;
        // Emphasis dot end
        case COMMAND_BOUTEN_END:
            lst.bouten = 0;
            break;
        // Emphasis line end
        case COMMAND_BOUSEN_END:
            lst.bousen = DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE;
            break;
        // Bottom aligned / raise end
        case COMMAND_JIAGE_END:
            bst.jiage = 0;
            break;
        // Title end
        case COMMAND_TITLE_END:
            bst.title = 0;

            if (p.plist_title != null)
                LayoutSub.layout_append_titlelist(p);
            break;
        // Center horizontally
        case COMMAND_PAGE_CENTER:
            p.pagestate.fcenter = true;
            break;
        }

        return COMMAND.COMMAND_NONE;
    }

    /** Processing commands with values */
    private static void _proc_command_val(LayoutWork p, String text, int[] pp) {
        int ps;
        COMMAND cmd;
        int val1, val2;
        PvLayout.LineState lst = p.linestate;
        PvLayout.BlockState bst = p.blockstate;

        ps = pp[0];

        cmd = COMMAND.values()[text.charAt(ps + 0)];
        val1 = text.charAt(ps + 1);
        val2 = text.charAt(ps + 2);

        pp[0] = ps + 3;

        switch (cmd) {
        // Indent [line]
        case COMMAND_JISAGE_LINE:
            lst.jisage = val1;
            break;
        // Indent [block]
        case COMMAND_JISAGE_START:
            bst.jisage = val1;
            bst.jisage_wrap = val1;
            break;
        // Indent, wrap
        case COMMAND_JISAGE_WRAP_START:
            bst.jisage = val1;
            bst.jisage_wrap = val2;
            break;
        // Indent, top aligned/wrap
        case COMMAND_JISAGE_TEN_WRAP_START:
            bst.jisage = 0;
            bst.jisage_wrap = val1;
            break;
        // Emphasis dot start
        case COMMAND_BOUTEN_START:
            lst.bouten = val1;
            break;
        // Emphasis line start
        case COMMAND_BOUSEN_START:
            lst.bousen = DefStyle.BOUSEN_TYPE.values()[val1];
            break;
        // Bottom aligned / n-character raise from bottom [line]
        case COMMAND_JIAGE_LINE:
            lst.jiage = val1;
            break;
        // Bottom aligned / n-character raise from bottom [block]
        case COMMAND_JIAGE_START:
            bst.jiage = val1;
            break;
        // Title
        case COMMAND_TITLE_START:
            bst.title = val1;
            break;
        }
    }

    /**
     * Picture processing
     * <p>
     * [uint16] String length, UTF-8 filename (including null)
     */
    private static void _proc_picture(LayoutWork p, String text, int[] pp) {
        int ps;
        int len;

        ps = pp[0];

        p.pagestate.picture = ps;

        len = ps;

        pp[0] = ps + 2 + len;
    }

    // Add normal text string without ruby

    /**
     * [When adding body text] Apply current annotation state to CharItem
     * <p>
     * [!] For horizontal within vertical, pi is deleted from the 2nd character onwards.
     */
    private static void _set_char_state(LayoutWork p, PvLayout.CharItem pi) {
        PvLayout.CharItem pitop;
        PvLayout.BlockState bst = p.blockstate;
        PvLayout.LineState lst = p.linestate;
        int n;

        // [block/line] Bottom aligned / Raise

        if (lst.jiage != 0 || bst.jiage != 0)
            pi.flags.add(PvLayout.CHARITEM_F.CHARITEM_F_JIAGE);

        // [block] Bold

        if (bst.flags.contains(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_BOLD))
            pi.flags.add(CHARITEM_F_BOLD);

        // [line] Emphasis dot / Emphasis line

        pi.bouten = lst.bouten;
        pi.bousen = lst.bousen;

        // Title string
        //  :Add characters to buf_title until title ends
        //  :※Only during initial layout

        if (bst.title != 0 && p.plist_title != null) {
            // For the first character, set the title type in the 1st byte
            if (p.buf_title.isEmpty())
                p.buf_title += bst.title;

            // Add 1 character
            if (p.buf_title.length() <= 256 * 4)
                p.buf_title += pi.code;
        }

        // How to arrange characters

        if (bst.flags.contains(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_YOKOGUMI))
            // [block] Horizontal layout
            pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_ROTATE;
        else if (lst.tatetyuyoko_num != 0) {
            // [line] Horizontal within vertical
            n = lst.tatetyuyoko_num; // +1 added

            if (n < 4 && pi.code < 127) {
                if (n == 1) {
                    // First character
                    pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_HORZ;
                    pi.horzcnt = 1;
                    pi.horzchar[0] = (byte) pi.code;

                    lst.tatetyuyoko_top = pi;
                } else {
                    // 2nd to 3rd characters are combined into the first character, CharItem deleted
                    pitop = lst.tatetyuyoko_top;

                    pitop.horzcnt++;
                    pitop.horzchar[n - 1] = (byte) pi.code;

                    p.list_char.remove(pi);
                }

                lst.tatetyuyoko_num++;
            }
        }
    }

    /**
     * [When adding body text] Adjustment after adding string
     * <p>
     * top: Added start position
     * return: Actually added character count (1 character on display)
     */
    private static int _text_adjust(LayoutWork p, CharItem top) {
        CharItem pi, next, prev, third;
        int c;
        int len = 0;

        for (int i = 0; i < p.list_char.size(); i++) {
            pi = p.list_char.get(i);
            prev = i > 0 ?  p.list_char.get(i - 1) : null;
            next = i < p.list_char.size() - 1 ? p.list_char.get(i + 1) : null;

            c = pi.code;
            len++;

            // Replace character

            if (LayoutSub.layout_replace_char(p, c))
                pi.code = c;

            //

            if (c == '゛' || c == '゜') {
                // For dakuten/handakuten, combine into 1 character

                if (prev != null && !prev.flags.containsAll(EnumSet.of(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN, PvLayout.CHARITEM_F.CHARITEM_F_HANDAKUTEN))) {
                    prev.flags.add(c == '゛' ? PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN : PvLayout.CHARITEM_F.CHARITEM_F_HANDAKUTEN);

                    p.list_char.remove(pi);
                    len--;
                }
            } else if (c == '／') {
                // Replace kunojiten

                if (next != null && next.code == '＼') {
                    // Normal

                    pi.code = 0x3033;
                    next.code = 0x3035;
                } else if (next != null && next.code == '″'
                        && p.list_char.get(i + 2) != null && p.list_char.get(i + 2).code == '＼') {
                    // With dakuten

                    third = p.list_char.get(i + 2);

                    p.list_char.remove(next);
                    len--;

                    pi.code = 0x3034;
                    third.code = 0x3035;

                    next = third;
                }
            }
        }

        return len;
    }

    // Get 1 line

    /**
     * Add normal text string without ruby
     * <p>
     * [uint16] Length [uint16 or uint32 x Length] String
     * <p>
     * is32bit: true for 32bit, false for 16bit string
     * ppchar: If not null, returns the first character data added
     * return: Actually added character count
     */
    private static int _append_text(LayoutWork p, String text, int[] pp, boolean is32bit, PvLayout.CharItem[] ppchar) {
        int ps;
        int len, i, charsize;
        PvLayout.CharItem pi, pitop;
        List<PvLayout.CharItem> list = p.list_char;
        int pibottom = -1;

        ps = pp[0];

        charsize = (is32bit) ? 4 : 2;

        // Length - read from text at position ps
        len = text.charAt(ps);
        ps += 1; // Simplified: assume length fits in one char

        // Next source position
        pp[0] = ps + len;

        // Current end position
        pibottom = list.size() - 1;

        // Add to list 1 character at a time
        // (Combined into one data in case of horizontal within vertical)
        for (i = 0; i < len && ps < text.length(); i++, ps++) {
            pi = new CharItem();
            pi.flags = java.util.EnumSet.noneOf(PvLayout.CHARITEM_F.class);
            pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_NORMAL;
            list.add(pi);
            // Read actual character from text
            pi.code = text.charAt(ps);

            // Apply current annotation state
            _set_char_state(p, pi);
        }

        // Update position
        pp[0] = ps;

        // Added start position
        pitop = (pibottom >= 0 && pibottom + 1 < list.size()) ? list.get(pibottom + 1) : (list.isEmpty() ? null : list.get(0));

        // Adjustment
        if (pitop != null) {
            len = _text_adjust(p, pitop);
        }

        // Start position
        if (ppchar != null && pitop != null) ppchar[0] = pitop;

        return len;
    }

    /**
     * Add string with ruby
     * <p>
     * [uint16] Body length, UTF-32 string, [uint16] Ruby length, UTF-32 string
     */
    private static void _append_ruby(LayoutWork p, String text, int[] pp) {
        int ps;
        CharItem[] pitop = new CharItem[1];
        PvLayout.RubyItem pi;
        int clen, rlen;

        // Add body string

        clen = _append_text(p, text, pp, true, pitop);

        // Add ruby item

        ps = pp[0];

        rlen = ps;
        ps += 2;

        pi = new PvLayout.RubyItem();
        p.list_ruby.add(pi);
        if (pi != null) {
            pi.char_top = pitop[0];
            pi.rubytxt = text.substring(ps);
            pi.rubylen = rlen;
            pi.charlen = clen;
            pi.ruby_h = mFontGetVertHeight(p.img, p.font_ruby, String.valueOf(ps)).height; // FONT_DRAW_F_RUBY
        }

        // Next data position

        pp[0] = ps + (rlen << 2);
    }

    /**
     * Get 1 line of data from internal data
     * <p>
     * Set character data to list_char/list_ruby list,
     * Annotation info to line/block state.
     */
    private static void _get_line(LayoutWork p, COMMAND[] cmdret) {
        int[] ps = new int[]{p.textP};
        boolean floop = true;
        COMMAND cmd;

        cmdret[0] = COMMAND.COMMAND_NONE;

        while (floop && ps[0] < p.text.length()) {
            // Data type (1byte)
            DefStyle.DATATYPE type = DefStyle.DATATYPE.values()[p.text.charAt(ps[0]++)];

            switch (type) {
            // Line number info
            case DATATYPE_LINEINFO:
                p.curlineno = p.text.charAt(ps[0]);
                ps[0] += 1;
                break;
            // Normal string
            case DATATYPE_NORMAL_TEXT_16:
            case DATATYPE_NORMAL_TEXT_32:
                _append_text(p, p.text, ps, (type == DefStyle.DATATYPE.DATATYPE_NORMAL_TEXT_32), null);
                break;
            // String with ruby
            case DATATYPE_RUBY_TEXT:
                _append_ruby(p, p.text, ps);
                break;
            // Newline
            case DATATYPE_ENTER:
                floop = false;
                break;
            // Command without value
            case DATATYPE_COMMAND:
                cmd = _proc_command_noval(p, COMMAND.values()[p.text.charAt(ps[0]++)]);

                // If command changes page
                if (cmd != COMMAND.COMMAND_NONE) {
                    floop = false;

                    if (!p.list_char.isEmpty()) {
                        // If body characters remain, process them first,
                        // so return position
                        ps[0] -= 2;
                    } else
                        cmdret[0] = cmd;
                }
                break;
            // Command with value
            case DATATYPE_COMMAND_VAL:
                _proc_command_val(p, p.text, ps);
                break;
            // Picture
            case DATATYPE_PICTURE:
                floop = false;

                if (!p.list_char.isEmpty())
                    // If body characters remain, process first
                    ps[0]--;
                else
                    _proc_picture(p, p.text, ps);
                break;
            // End
            case DATATYPE_END:
                ps[0]--;
                floop = false;
                break;
            }
        }

        p.textP = ps[0];
    }

    // Adjustment after getting 1 line

    /** Automatically determine arrangement of half-width characters */
    private static void _set_chartype_auto(LayoutWork p) {
        CharItem pi, next, pitop, pi2;
        int cnt;

        for (int i = 0; i < p.list_char.size(); i++) {
            pi = p.list_char.get(i);
            next = i < p.list_char.size() - 2 ? p.list_char.get(i + 1) : null;

            // If character type is already specified or not half-width, leave as is

            if (pi.chartype != PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_NORMAL || pi.code >= 128)
                continue;

            // ------- Automatic determination of half-width characters

            // Number of consecutive target characters for horizontal within vertical

            for (cnt = 0, pi2 = pi;
                 cnt < 4 && pi2 != null && layout_ischar_tatetyuyoko(pi2.code);
                 cnt++, pi2 = p.list_char.get(i + 1))
                ;

            //

            if (cnt >= 1 && cnt <= 3) {
                // [Horizontal within vertical]
                //  If within 3 characters, automatically make horizontal within vertical.
                //  Leave only the first character and combine into 1 character.

                pitop = pi;

                pitop.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_HORZ;
                pitop.horzcnt = cnt;
                pitop.horzchar[0] = (byte) pitop.code;

                for (int j = 1; j < cnt; j++) {
                    pi = p.list_char.get(i + 1);
                    pitop.horzchar[j] = (byte) pi.code;
                    p.list_char.remove(i + 1);
                }
            } else {
                // [Horizontal layout]
                //  If 4 or more characters consecutive, horizontal layout.
                //  Continue until non-half-width character appears.

                while (i < p.list_char.size()) {
                    pi = p.list_char.get(i);
                    if (pi.code >= 128) break;
                    pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_ROTATE;
                    i++;
                }
                i--;
            }

            next = pi;
        }
    }

    /** Set character width/height of body text */
    private static void _set_char_size(LayoutWork p) {
        Font[] font = new Font[2];
        int hbuf;
        int[] fonth = new int[ 2];
        int n;
        int f, h;
        int code;

        font[0] = p.font_main;
        font[1] = p.font_bold;

        fonth[0] = p.fontmain_h;
        fonth[1] = p.fontbold_h;

        for (int i = 0; i < p.list_char.size(); i++) {
            CharItem pi = p.list_char.get(i);
            code = pi.code;

            // [0] Normal [1] Bold
            n = pi.flags.contains(CHARITEM_F_BOLD) ? 1 : 0;

            switch (pi.chartype) {
            // Normal vertical writing
            case CHARITEM_TYPE_NORMAL:
                if (n != 0 || code > 0xffff)
                    // Always calculate if bold or code exceeds 16bit
                    pi.height = mFontGetVertHeight(p.img, font[n], new String(Character.toChars(code))).height;
                else {
                    // If normal and within 16bit, for speed up,
                    // turn on flag if same as full-width height.
                    // (Most characters are within 16bit range)

                    hbuf = p.buf_hflags[code >> 3];
                    f = 1 << (code & 7);

                    if ((hbuf & f) != 0)
                        // Fetched last time and same as full-width height
                        pi.height = fonth[0];
                    else {
                        h = mFontGetVertHeight(p.img, font[0], new String(Character.toChars(code))).height;

                        if (h == fonth[0])
                            p.buf_hflags[code >> 3] |= f;

                        pi.height = h;
                    }
                }
                break;
            // Horizontal layout (rotated 90 degrees)
            case CHARITEM_TYPE_ROTATE:
                pi.height = mFontGetVertHeight(p.img, font[n], new String(Character.toChars(code))).height; // FONT_DRAW_F_ROTATE
                break;
            // Horizontal within vertical
            case CHARITEM_TYPE_HORZ:
                pi.width = mFontGetVertHeight(p.img, font[n], new String(pi.horzchar, 0, pi.horzcnt)).width;
                pi.height = fonth[n];
                break;
            }
        }
    }

    /** Get parent string height (without wrapping) */
    private static int _get_char_height(LayoutWork p, CharItem pi, int len) {
        int h = 0;
        int idx = p.list_char.indexOf(pi);

        for (int i = 0; i < len && idx + i < p.list_char.size(); i++)
            h += p.list_char.get(idx + i).height;

        return h + (len - 1) * p.stdef.char_space;
    }

    /**
     * Adjustment of ruby and parent string
     * <p>
     * If ruby is larger than parent string, add padding to parent string of ruby
     */
    private static void _set_ruby_parent(LayoutWork p) {
        PvLayout.RubyItem pi;
        CharItem pic;
        int charh, diff, diffmax, clen, sp1, sp2;

        diffmax = p.fontmain_h;

        for (int ri = 0; ri < p.list_ruby.size(); ri++) {
            pi = p.list_ruby.get(ri);
            // Height of parent string

            charh = _get_char_height(p, pi.char_top, pi.charlen);

            //

            diff = pi.ruby_h - charh;

            if (diff <= diffmax) {
                pi.char_h = charh;
                continue;
            }

            // -------
            pi.char_h = pi.ruby_h;

            // Add padding to parent string height
            clen = pi.charlen;

            int idx = p.list_char.indexOf(pi.char_top);
            if (clen == 1) {
                // If parent string is 1 character
                pic = pi.char_top;

                pic.height = pi.ruby_h;
                pic.padding = diff / 2;
            } else {
                // If parent string is 2 or more characters
                sp1 = diff / (clen * 2);
                sp2 = diff / clen;

                for (int i = 0; i < clen && idx + i < p.list_char.size(); i++) {
                    pic = p.list_char.get(idx + i);
                    if (i == 0) {
                        // First character
                        pic.height += sp1;
                        pic.padding = sp1;
                        diff -= sp1;
                    } else if (i == clen - 1) {
                        // Last character
                        pic.height += diff;
                        pic.padding = sp2;
                    } else {
                        // Middle
                        pic.height += sp2;
                        pic.padding = sp2;
                        diff -= sp2;
                    }
                }
            }
        }
    }

    // main

    /**
     * Get 1 line of character data
     * <p>
     * cmd: If page-related command such as new page is included, that command number. Otherwise -1.
     * return: false for end of data
     */
    static boolean layout_getline(LayoutWork p, COMMAND[] cmd) {
        if (p.textP >= p.text.length() || p.text.charAt(p.textP) == DefStyle.DATATYPE.DATATYPE_END.ordinal()) return false;

        // Clear line state
        p.linestate = new PvLayout.LineState();
        p.list_char.clear();
        p.list_ruby.clear();

        // Get 1 line data
        _get_line(p, cmd);

        // Automatic determination of character type
        _set_chartype_auto(p);

        // Set body px size
        _set_char_size(p);

        // Adjustment of ruby and parent string
        _set_ruby_parent(p);

        return true;
    }
}
