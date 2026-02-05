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

import java.awt.Point;
import java.io.IOException;

import static vavi.apps.aobook.Layout.gdat;
import static vavi.apps.aobook.LayoutSub.layout_find_utf32;
import static vavi.apps.aobook.LayoutSub.layout_is_nobottom;


/**
 * Layout: Main processing (for work)
 */
class LayoutMain {

    private LayoutMain() {
    }

    private static final int VIEWFLAGS_PAGENO = 1;

    // Set body character position

    /**
     * Calculate current Y position of indentation/bottom alignment
     * <p>
     * [LayoutWork]
     * jisage_y      : Indentation of the first line
     * jisage_wrap_y : Indentation after wrapping
     * jiage_bottom  : Bottom of bottom alignment
     */
    private static void _set_jisage_jiage_pos(LayoutWork p) {
        StyleDef st;
        int top, wrap, bottom, charh, max;

        st = p.stdef;

        // Number of indented characters (prioritize line unit)
        if (p.linestate.jisage != 0)
            top = wrap = p.linestate.jisage;
        else {
            top = p.blockstate.jisage;
            wrap = p.blockstate.jisage_wrap;
        }

        // Number of bottom aligned characters
        if (p.linestate.jiage != 0 || p.blockstate.jiage != 0)
            bottom = (p.blockstate.jiage!= 0 ? p.blockstate.jiage : p.linestate.jiage) - 1;
        else
            bottom = 0;

        // Character count adjustment
        max = st.chars - 1;

        if (top > max) top = max;
        if (wrap > max) wrap = max;
        if (bottom > max) bottom = max;

        if (top + bottom > st.chars) top = 0;
        if (wrap + bottom > st.chars) wrap = 0;

        // Set
        charh = p.fontmain_h + st.char_space;

        if (bottom != 0)
            bottom = bottom * p.fontmain_h + (bottom - 1) * st.char_space;

        p.jisage_y = top * charh;
        p.jisage_wrap_y = wrap * charh;
        p.jiage_bottom = p.pageH - bottom;
    }

    /**
     * Set drawing position of one line of body characters
     * <p>
     * [!] For bottom alignment/raise, Y position remains as position from top, so process at drawing time.
     * <p>
     * ptcur: px position of the current line. Position of the next line is entered.
     * prev_wrapnum: Wrap from the previous line on the previous page, number of lines for the previous page.
     * center_diffx: X position difference when page is centered horizontally (at drawing)
     * return: If the current line wraps to the next page, how many lines to shift from the beginning
     */
    private static int _set_line_char_pos(LayoutWork p, Point ptcur, int prev_wrapnum, int center_diffx) {
        StyleDef st;
        PvLayout.CharItem pinext = null, pinext2;
        int x, y, charh, nexth, charsp, wrap;
        int[] bottomY = new int[2];
        int wrapcnt = 0;    // Number of wrapped lines
        int next_wrapnum = 0;    // Number of lines to skip wrapping on the next page
        boolean fnext_hanging = false; // Is the next character hanging
        int bottom_type = 0;

        // Y position of indentation/bottom alignment

        _set_jisage_jiage_pos(p);

        // Bottom position ([0] Normal [1] Bottom aligned/Raised)

        bottomY[0] = p.pageH;
        bottomY[1] = p.jiage_bottom;

        // ------------

        x = ptcur.x + p.line_width * prev_wrapnum; // Shift by the wrap amount of the previous page
        y = p.jisage_y;

        st = p.stdef;
        charsp = st.char_space;

        //
        for (int i = 0; i < p.list_char.size(); i++) {
            PvLayout.CharItem pi = p.list_char.get(i);
            if (i < p.list_char.size() - 1)
                pinext = p.list_char.get(i + 1);

            charh = pi.height;
            nexth = pinext != null ? pinext.height : 0;

            // Start bottom alignment/raise
            if (bottom_type == 0 && pi.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_JIAGE))
                bottom_type = 1;

            // Wrap/Hanging judgment
            //  wrap: [0] Normal [1] Wrap current character to next line [2] Hang next character
            //        [3] (Hanging processing) After setting position as is, wrap to next line
            wrap = 0;

            if (fnext_hanging)
            // If the previous character specified the next as hanging
                wrap = 3;
            else if (y + charh > bottomY[bottom_type])
            // Current character exceeds bottom => Wrap
                wrap = 1;
            else if (y + charh + charsp + nexth > bottomY[bottom_type]) {
                // Next character exceeds bottom (Current character becomes end of line)

                if (layout_is_nobottom(p, pi))
                // If current character must not be placed at end of line, move current character to next line
                    wrap = 1;
                else if (pinext != null && layout_find_utf32(pinext.code, p.u32_nohead)) {
                    // Next character is subject to line head restriction

                    if (st.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_HANGING)
                            && layout_find_utf32(pinext.code, p.u32_hanging))
                    // Subject to hanging and hanging is enabled
                        wrap = 2;
                    else
                    // Wrap from current character
                        wrap = 1;
                }
            } else if (pinext != null && i < p.list_char.size() - 2 && p.list_char.get(i + 2) != null) {
                // If the character after next exceeds bottom, and is subject to line head restriction (not hanging),
                // and current character must not be at end of line, wrap current character
                // [Example] "……」" where "…」" wraps to next line due to line head restriction, but "…" cannot be split,
                //      so wrap at the first "…".
                pinext2 = (PvLayout.CharItem) p.list_char.get(i + 2);

                if (y + charh + charsp + nexth + charsp + pinext2.height > bottomY[bottom_type]
                        && layout_find_utf32(pinext2.code, p.u32_nohead)
                        && !(st.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_HANGING) && layout_find_utf32(pinext2.code, p.u32_hanging))
                        && layout_is_nobottom(p, pi))
                    wrap = 1;
            }

            if (wrap == 2)
                // Reserve next character as hanging
                fnext_hanging = true;
            else {
                fnext_hanging = false;

                // Wrap from current character
                if (wrap == 1) {
                    x -= p.line_width;
                    y = p.jisage_wrap_y;
                    wrapcnt++;
                }
            }

            // Set character drawing position

            pi.x = x - center_diffx;
            pi.y = y;

            // If it is the first character of wrapping, add flag
            if (y == p.jisage_wrap_y && i > 0 && p.list_char.get(i - 1) != null)
                pi.flags.add(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP);

            // Y position of next character
            y += charh + charsp;

            // If current X position is out of page range
            //  (When wrapping across to the next page)
            //  :Remember the current wrap count when it first goes out of range
            if (x < 0 && next_wrapnum == 0)
                next_wrapnum = wrapcnt;

            // Wrapping by hanging (if there is a next character)
            if (wrap == 3 && pinext == null) {
                x -= p.line_width;
                y = p.jisage_wrap_y;
                wrapcnt++;
            }
        }

        // Position of next line

        ptcur.x = x - p.line_width;
        ptcur.y = 0;

        return next_wrapnum;
    }

    // For drawing

    /**
     * [At drawing] Process bottom alignment / n-character raise from bottom for 1 line
     * <p>
     * Adjust Y position to actual position.
     */
    private static void _proc_draw_line_jiage(LayoutWork p) {
        PvLayout.CharItem pinext, pitop, piend, pistart, pi;
        int charspace, texth, toph, upper_h, jisage_h, n;
        boolean flag;

        charspace = p.stdef.char_space;

        // Number of raised characters (0 for bottom alignment)
        n = p.blockstate.jiage != 0 ? p.blockstate.jiage : p.linestate.jiage;
        n--;
        if (n < 0) return;

        // Height to raise (px)

        if (n == 0)
            upper_h = 0;
        else
            upper_h = n * p.fontmain_h + (n - 1) * charspace;

        // Shift Y position of each character
        jisage_h = p.jisage_y;
        flag = false;

        int j, k = p.list_char.size() - 1;
        for (int i = 0; i < p.list_char.size(); i = j) {
            pitop = p.list_char.get(i);
            // --- Get range and info of bottom alignment for 1 line on drawing
            //  pistart,piend: Range of bottom alignment
            //  toph: Height of text before bottom alignment
            //  texth: Height of bottom aligned text
            pistart = pitop;
            texth = toph = 0;

            for (j = i; j < p.list_char.size(); k = j) {
                pinext = p.list_char.get(j);
                piend = p.list_char.get(k);
                // Start bottom alignment
                if (!flag && piend.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_JIAGE)) {
                    pistart = piend;
                    toph = texth;
                    texth = 0;
                    flag = true;
                }

                texth += piend.height;

                if (pinext == null) break;
                if (pinext.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)) break;

                texth += charspace;
            }

            // If there is bottom alignment, shift Y position
            // (Because it is set with position from top, add margin)
            if (flag) {
                // Add width
                n = p.pageH - toph - texth - upper_h - jisage_h;
                if (n < 0) n = 0;

                // Apply
                for (pi = pistart; true; pi = p.list_char.get(i + 1)) {
                    pi.y += n;

                    if (pi == p.list_char.get(k)) break;
                }
            }

            // After 1st line, wrap indentation

            jisage_h = p.jisage_wrap_y;
        }
    }

    /*** [At drawing] Set drawing position of ruby for 1 line */
    private static void _set_draw_line_ruby_pos(LayoutWork p) {
        PvLayout.RubyItem piprev = null;
        int x, y, prev_bottom, wrap;

        int i = 0;
        for (PvLayout.RubyItem pi : p.list_ruby) {

            // Parent string info
            //  wrap : [0bit] Parent string wraps in middle [1bit] Head of parent string is head of wrap
            wrap = LayoutSub.layout_getrubyinfo(p, pi);

            // Parent string head position
            x = pi.char_top.x;
            y = pi.char_top.y;

            // Bottom Y position of previous ruby
            // (-1 if no ruby or different line)
            prev_bottom = (piprev != null && piprev.x == x) ? piprev.y + piprev.ruby_h : -1;

            //
            if (prev_bottom != -1 && y < prev_bottom) {
                // If previous ruby is on the same line and overlaps current parent string position,
                // start from end position of previous ruby
                y = prev_bottom;

                pi.char_h = PvLayout.RUBYITEM_CHARH_NO_PADDING;
            } else if (pi.ruby_h >= pi.char_h) {
                // If ruby > parent string, center align with parent string (protrude from parent string)
                //  :Including cases where padding is added between parent strings.
                //  :However, if parent string has wrapping or is head of wrapping or ruby continues before/after,
                //  :from head of parent string
                if (wrap == 0 && !LayoutSub.layout_is_ruby_connect(p, piprev, pi) && !LayoutSub.layout_is_ruby_connect(p, pi, p.list_ruby.get(i + 1))) {
                    // Centered position
                    y -= (pi.ruby_h - pi.char_h) / 2;

                    // If overlapping with previous ruby
                    if (prev_bottom != -1 && y < prev_bottom)
                        y = prev_bottom;
                }

                pi.char_h = PvLayout.RUBYITEM_CHARH_NO_PADDING;
            }

            // If parent string > ruby, maintain char_h and add padding to ruby
            // Set
            pi.x = x;
            pi.y = y;

            piprev = pi;
            i++;
        }
    }

    // sub

    /**
     * Process page-related commands
     * <p>
     * pageno: Current page position
     * return: [0] Page break [1] Make next page blank & page break [2] Ignore command
     */
    private static int _proc_command_page(LayoutWork p, LayoutLine.COMMAND cmd, Point ptcur, int pageno) {
        int ret = -1;
        boolean is_topline;

        // Is top line
        is_topline = (ptcur.x == p.text_right_x);

        // In case of spread (Always page break for single page)
        if (p.stdef.pages == 2) {
            switch (cmd) {
            // New signature
            //  :If right page, page break.
            //  :If left page, make next page blank and start from next.
            case COMMAND_KAITYO:
                ret = (pageno & 1) != 0 ? 1 : 0;
                break;
            // New spread
            //  :If right page, page break.
            //  :If left page, make next page blank and start from next.
            //  :(However, ignore if first page and first line)
            case COMMAND_KAIMIHIRAKI:
                if ((pageno & 1) != 0)
                    ret = 0;
                else
                // If first line of first page, ignore
                    ret = pageno == 0 && is_topline ? 2 : 1;
                break;
            }
        }

        //

        if (ret == -1)
        // Normal page break (ignore if first line of page)
            return is_topline ? 2 : 0;
        else
            return ret;
    }

    // Page layout/Drawing

    /**
     * Layout of 1 page
     * <p>
     * Set current page info to lf.curpage,
     * Set next page info to lf.nextpage.
     * <p>
     * return: false if data end
     */
    static boolean layout_page(LayoutWork p, PvLayout.LayoutFirst lf) {
        Point ptcur = new Point();
        PvLayout.BlockState top_blockstate = null;
        int top_text = 0;
        int wrapnum;
        LayoutLine.COMMAND[] cmd = new LayoutLine.COMMAND[1];
        int ret;

        p.pagestate = new PvLayout.PageState();
        p.pagestate.picture = -1;

        p.text = lf.curpage.src;
        p.textP = 0;
        p.blockstate = lf.curpage.blockstate;
        if (p.blockstate == null) {
            p.blockstate = new PvLayout.BlockState();
            p.blockstate.flags = java.util.EnumSet.noneOf(PvLayout.BLOCKSTATE_F.class);
        }
        p.curlineno = lf.curpage.lineno;

        wrapnum = lf.curpage.wrap_num;

        lf.curpage.diffx = 0;
        lf.nextpage.flags = 0;

        // If end of data, finish
        if (p.text == null || p.text.isEmpty() || p.text.charAt(0) == DefStyle.DATATYPE.DATATYPE_END.ordinal()) return false;

        // If blank page, proceed to next page processing
        if ((lf.curpage.flags & PvLayout.PAGEINFO_F_BLANK) == 0) {
            // Process current page line by line
            ptcur.x = p.text_right_x;
            ptcur.y = 0;

            while (ptcur.x >= 0) {
                // Save info at start of line for when wrapping to next page
                top_text = p.textP;
                top_blockstate = p.blockstate;

                // Get 1 line of character data and state
                //  cmd: If page-related command, command number. If none -1.
                if (!LayoutLine.layout_getline(p, cmd)) break;

                // Picture
                if (p.pagestate.picture != -1
                        && p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE))
                    break;

                // Process page-related commands
                if (cmd[0] != LayoutLine.COMMAND.COMMAND_NONE) {
                    ret = _proc_command_page(p, cmd[0], ptcur, lf.pagenum);

                    if (ret == 1)
                        // Make next page blank
                        lf.nextpage.flags |= PvLayout.PAGEINFO_F_BLANK;
                    else if (ret == 2)
                        // Ignore command
                        continue;

                    break;
                }

                // Set drawing position of body characters
                wrapnum = _set_line_char_pos(p, ptcur, wrapnum, 0);
            }

            // Horizontal center position of page
            // (Exclude if wrapping continues to next page)
            if (p.pagestate.fcenter && wrapnum == 0)
                lf.curpage.diffx = (ptcur.x + p.line_width) / 2;

        }
        // ------ Next page info

        // Line number of next page

        lf.nextpage.lineno = p.curlineno;

        // If there is a picture on current page and not first line,
        // start of next page is picture data

        if (p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE)
                && p.pagestate.picture != -1
                && ptcur.x != p.text_right_x)
            p.textP = p.pagestate.picture - 1;

        // Next page info

        lf.nextpage.wrap_num = wrapnum;

        if (wrapnum != 0) {
            // If there is wrapping, re-process from the beginning of that line
            lf.nextpage.src = p.text.substring(top_text);
            lf.nextpage.blockstate = top_blockstate;
        } else {
            // If no wrapping, set current state
            lf.nextpage.src = p.text.substring(p.textP);
            lf.nextpage.blockstate = p.blockstate;
            lf.nextpage.lineno++;
        }

        return true;
    }

    /**
     * Draw 1 page
     * <p>
     * page: Page info to draw
     * pagepos: -1 for single page, page position for spread (0:Right 1:Left)
     */
    static void layout_drawpage(LayoutWork p, PvLayout.PageInfo page, int pagepos) throws IOException {
        Point ptcur = new Point();
        int wrapnum;
        LayoutLine.COMMAND[] cmd = new LayoutLine.COMMAND[] {LayoutLine.COMMAND.COMMAND_NONE};
        int ret;

        // Draw page info
        if ((gdat.viewflags & VIEWFLAGS_PAGENO) != 0)
            LayoutDraw.layout_draw_pageinfo(p, page.pageno, pagepos);

        // If blank page, draw nothing
        if ((page.flags & PvLayout.PAGEINFO_F_BLANK) != 0) return;

        //

        p.pagestate = new PvLayout.PageState();
        p.pagestate.picture = -1;

        p.text = page.src;
        p.textP = 0;
        p.blockstate = page.blockstate;
        if (p.blockstate == null) {
            p.blockstate = new PvLayout.BlockState();
            p.blockstate.flags = java.util.EnumSet.noneOf(PvLayout.BLOCKSTATE_F.class);
        }

        ptcur.x = p.text_right_x;
        ptcur.y = 0;

        wrapnum = page.wrap_num;

        //
        while (ptcur.x >= 0) {
            // Get 1 line
            if (!LayoutLine.layout_getline(p, cmd))break;

            // Picture
            if (p.pagestate.picture != -1
                    && p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE)) {
                // If not page start, go to next page

                if (ptcur.x == p.text_right_x)
                    LayoutDraw.layout_draw_picture(p, pagepos);

                break;
            }

            // Page unit command
            if (cmd[0] != LayoutLine.COMMAND.COMMAND_NONE) {
                ret = LayoutMain._proc_command_page(p, cmd[0], ptcur, page.pageno);

                if (ret == 2)
                // Ignore command
                    continue;
                else
                    break;
            }

            // Set drawing position
            LayoutMain._set_line_char_pos(p, ptcur, wrapnum, page.diffx);

            _proc_draw_line_jiage(p);

            _set_draw_line_ruby_pos(p);

            wrapnum = 0;

            // Draw
            LayoutDraw.layout_draw_line(p, pagepos);
        }
    }
}
