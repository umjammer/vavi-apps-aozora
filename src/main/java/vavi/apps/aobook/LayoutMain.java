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
 * レイアウト:メイン処理 (作業用)
 */
class LayoutMain {

    private static final int VIEWFLAGS_PAGENO = 1;

    // 本文文字位置セット

    /**
     * 現在の字下げ/地上げの Y 位置計算
     * <p>
     * [LayoutWork]
     * jisage_y      : 先頭行の字下げ
     * jisage_wrap_y : 折り返し後の字下げ
     * jiage_bottom  : 字上げの下端
     */
    private static void _set_jisage_jiage_pos(LayoutWork p) {
        StyleDef st;
        int top, wrap, bottom, charh, max;

        st = p.stdef;

        // 字下げ文字数 (行単位を優先)
        if (p.linestate.jisage != 0)
            top = wrap = p.linestate.jisage;
        else {
            top = p.blockstate.jisage;
            wrap = p.blockstate.jisage_wrap;
        }

        // 字上げ文字数
        if (p.linestate.jiage != 0 || p.blockstate.jiage != 0)
            bottom = (p.blockstate.jiage!= 0 ? p.blockstate.jiage : p.linestate.jiage) - 1;
        else
            bottom = 0;

        // 文字数調整
        max = st.chars - 1;

        if (top > max) top = max;
        if (wrap > max) wrap = max;
        if (bottom > max) bottom = max;

        if (top + bottom > st.chars) top = 0;
        if (wrap + bottom > st.chars) wrap = 0;

        // セット
        charh = p.fontmain_h + st.char_space;

        if (bottom != 0)
            bottom = bottom * p.fontmain_h + (bottom - 1) * st.char_space;

        p.jisage_y = top * charh;
        p.jisage_wrap_y = wrap * charh;
        p.jiage_bottom = p.pageH - bottom;
    }

    /**
     * 1行分の本文文字の描画位置をセット
     * <p>
     * [!] 地付き/地上げでは、Y 位置は上端からの位置のままになっているので、描画時に処理する。
     * <p>
     * ptcur: 現在の行の px 位置。次の行の位置が入る。
     * prev_wrapnum: 前ページの前行からの折り返しで、前ページの分の行数。
     * center_diffx: ページ左右中央の場合の X 位置差分 (描画時)
     * return: 現在の行が次ページに渡って折り返す場合、先頭から何行ずらすか
     */
    private static int _set_line_char_pos(LayoutWork p, Point ptcur, int prev_wrapnum, int center_diffx) {
        StyleDef st;
        PvLayout.CharItem pinext = null, pinext2;
        int x, y, charh, nexth, charsp, wrap;
        int[] bottomY = new int[2];
        int wrapcnt = 0;    // 折り返し行数
        int next_wrapnum = 0;    // 次ページで折り返しをスキップする行数
        boolean fnext_hanging = false; // 次の文字がぶら下げか
        int bottom_type = 0;

        // 字下げ/字上げの Y 位置

        _set_jisage_jiage_pos(p);

        // 下端位置 ([0] 通常時 [1] 地付き/字上げ時)

        bottomY[0] = p.pageH;
        bottomY[1] = p.jiage_bottom;

        // ------------

        x = ptcur.x + p.line_width * prev_wrapnum; // 前ページの折り返し分をずらす
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

            // 地付き/地上げ開始
            if (bottom_type == 0 && pi.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_JIAGE))
                bottom_type = 1;

            // 折り返し/ぶら下げ判定
            //  wrap: [0] 通常 [1] 現在文字を次行に折り返し [2] 次の文字をぶら下げ
            //        [3] (ぶら下げ処理) そのまま位置のセット後、次行に折り返し
            wrap = 0;

            if (fnext_hanging)
            // 前の文字で、次がぶら下げとして指定されている場合
                wrap = 3;
            else if (y + charh > bottomY[bottom_type])
            // 現在の文字が下端を超える => 折り返し
                wrap = 1;
            else if (y + charh + charsp + nexth > bottomY[bottom_type]) {
                // 次の文字が下端を超える (現在の文字が行末になる)

                if (layout_is_nobottom(p, pi))
                // 現在の文字が行末に置いてはいけない文字の場合、現在文字を次行へ
                    wrap = 1;
                else if (pinext != null && layout_find_utf32(pinext.code, p.u32_nohead)) {
                    // 次の文字が行頭禁則の対象

                    if (st.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_HANGING)
                            && layout_find_utf32(pinext.code, p.u32_hanging))
                    // ぶら下げ対象で、ぶら下げが有効
                        wrap = 2;
                    else
                    // 現在文字から折り返し
                        wrap = 1;
                }
            } else if (pinext != null && i < p.list_char.size() - 2 && p.list_char.get(i + 2) != null) {
                // 次の次の文字が下端を超えて、かつ行頭禁則対象 (ぶら下げ対象外) で、
                // かつ現在文字が行末に来てはならない文字の場合、現在文字を折り返し
                // [例] "……」" で、行頭禁則によって "…」" が次行に折り返すが、"…" は分割禁止のため、
                //      最初の "…" で折り返す時。
                pinext2 = (PvLayout.CharItem) p.list_char.get(i + 2);

                if (y + charh + charsp + nexth + charsp + pinext2.height > bottomY[bottom_type]
                        && layout_find_utf32(pinext2.code, p.u32_nohead)
                        && !(st.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_HANGING) && layout_find_utf32(pinext2.code, p.u32_hanging))
                        && layout_is_nobottom(p, pi))
                    wrap = 1;
            }

            if (wrap == 2)
                // 次の文字をぶら下げとして予約
                fnext_hanging = true;
            else {
                fnext_hanging = false;

                // 現在文字から折り返し
                if (wrap == 1) {
                    x -= p.line_width;
                    y = p.jisage_wrap_y;
                    wrapcnt++;
                }
            }

            // 文字描画位置セット

            pi.x = x - center_diffx;
            pi.y = y;

            // 折り返しの先頭文字の場合、フラグを付加
            if (y == p.jisage_wrap_y && p.list_char.get(i - 1) != null)
                pi.flags.add(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP);

            // 次の文字のY位置
            y += charh + charsp;

            // 現在 X 位置がページ範囲外の場合
            //  (折り返しで次ページにまたがる時)
            //  :最初に範囲外になった時に、現在の折り返し数を記憶
            if (x < 0 && next_wrapnum == 0)
                next_wrapnum = wrapcnt;

            // ぶら下げによる折り返し (次の文字がある場合)
            if (wrap == 3 && pinext == null) {
                x -= p.line_width;
                y = p.jisage_wrap_y;
                wrapcnt++;
            }
        }

        // 次行の位置

        ptcur.x = x - p.line_width;
        ptcur.y = 0;

        return next_wrapnum;
    }

    // 描画時用

    /**
     * [描画時] 1行分の地付き/地からｎ字上げ処理
     * <p>
     * Y 位置を実際の位置に調整する。
     */
    private static void _proc_draw_line_jiage(LayoutWork p) {
        PvLayout.CharItem pinext, pitop, piend, pistart, pi;
        int charspace, texth, toph, upper_h, jisage_h, n;
        boolean flag;

        charspace = p.stdef.char_space;

        // 字上げ文字数 (0 で地付き)
        n = p.blockstate.jiage != 0 ? p.blockstate.jiage : p.linestate.jiage;
        n--;
        if (n < 0) return;

        // 上げる高さ (px)

        if (n == 0)
            upper_h = 0;
        else
            upper_h = n * p.fontmain_h + (n - 1) * charspace;

        // 各文字の Y 位置をずらす
        jisage_h = p.jisage_y;
        flag = false;

        int j, k = p.list_char.size() - 1;
        for (int i = 0; i < p.list_char.size(); i = j) {
            pitop = p.list_char.get(i);
            // --- 描画上での1行分の地上げの範囲と情報取得
            //  pistart,piend: 地上げの範囲
            //  toph: 地上げより前のテキストの高さ
            //  texth: 地上げテキストの高さ
            pistart = pitop;
            texth = toph = 0;

            for (j = i; j < p.list_char.size(); k = j) {
                pinext = p.list_char.get(j);
                piend = p.list_char.get(k);
                // 地上げ開始
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

            // 地上げありの場合、Y 位置をずらす
            // (上端からの位置でセットされているため、余白分を追加)
            if (flag) {
                // 加算幅
                n = p.pageH - toph - texth - upper_h - jisage_h;
                if (n < 0) n = 0;

                // 適用
                for (pi = pistart; true; pi = p.list_char.get(i + 1)) {
                    pi.y += n;

                    if (pi == p.list_char.get(k)) break;
                }
            }

            // 1行目以降は、折り返し字下げ

            jisage_h = p.jisage_wrap_y;
        }
    }

    /*** [描画時] 1行分のルビの描画位置セット */
    private static void _set_draw_line_ruby_pos(LayoutWork p) {
        PvLayout.RubyItem piprev = null;
        int x, y, prev_bottom, wrap;

        int i = 0;
        for (PvLayout.RubyItem pi : p.list_ruby) {

            // 親文字列の情報
            //  wrap : [0bit] 親文字列が途中で折り返し [1bit] 親文字列の先頭が折り返しの先頭
            wrap = LayoutSub.layout_getrubyinfo(p, pi);

            // 親文字列の先頭位置
            x = pi.char_top.x;
            y = pi.char_top.y;

            // 前のルビの下端 Y 位置
            // (-1 でルビがない、または行が違う)
            prev_bottom = (piprev != null && piprev.x == x) ? piprev.y + piprev.ruby_h : -1;

            //
            if (prev_bottom != -1 && y < prev_bottom) {
                // 前のルビが同じ行にあり、かつ現在の親文字列の位置に重なる場合は、
                // 前回ルビの終端位置から開始
                y = prev_bottom;

                pi.char_h = PvLayout.RUBYITEM_CHARH_NO_PADDING;
            } else if (pi.ruby_h >= pi.char_h) {
                // ルビ > 親文字列の場合は、親文字列に対して中央揃え (親文字列からはみ出す)
                //  :親文字列間に余白を付ける場合も含む。
                //  :ただし、親文字列が折り返しあり or 折り返しの先頭 or 前後どちらかにルビが続く場合は、
                //  :親文字列の先頭から
                if (wrap == 0 && !LayoutSub.layout_is_ruby_connect(p, piprev, pi) && !LayoutSub.layout_is_ruby_connect(p, pi, p.list_ruby.get(i + 1))) {
                    // 中央揃え位置
                    y -= (pi.ruby_h - pi.char_h) / 2;

                    // 前のルビと重なる場合
                    if (prev_bottom != -1 && y < prev_bottom)
                        y = prev_bottom;
                }

                pi.char_h = PvLayout.RUBYITEM_CHARH_NO_PADDING;
            }

            // 親文字列 > ルビの場合、char_h を維持して、ルビに余白を付ける
            // セット
            pi.x = x;
            pi.y = y;

            piprev = pi;
            i++;
        }
    }

    // sub

    /**
     * ページ関連のコマンド処理
     * <p>
     * pageno: 現在のページ位置
     * return: [0] 改ページ [1] 次ページを空白&改ページ [2] コマンドを無視
     */
    private static int _proc_command_page(LayoutWork p, LayoutLine.COMMAND cmd, Point ptcur, int pageno) {
        int ret = -1;
        boolean is_topline;

        // 先頭行か
        is_topline = (ptcur.x == p.text_right_x);

        // 見開きの場合 (単一ページなら常に改ページ)
        if (p.stdef.pages == 2) {
            switch (cmd) {
            // 改丁
            //  :右側のページなら、改ページ。
            //  :左側のページなら、次ページを空白にしてその次から開始
            case COMMAND_KAITYO:
                ret = (pageno & 1) != 0 ? 1 : 0;
                break;
            // 改見開き
            //  :右側のページなら、改ページ。
            //  :左側のページなら、次ページを空白にしてその次から。
            //  :(ただし、先頭ページで、かつ先頭行の場合は無視する)
            case COMMAND_KAIMIHIRAKI:
                if ((pageno & 1) != 0)
                    ret = 0;
                else
                // 先頭ページの先頭行の場合、無視
                    ret = pageno == 0 && is_topline ? 2 : 1;
                break;
            }
        }

        //

        if (ret == -1)
        // 通常改ページ (ページの先頭行なら無視)
            return is_topline ? 2 : 0;
        else
            return ret;
    }

    // ページレイアウト/描画

    /**
     * 1ページのレイアウト
     * <p>
     * lf.curpage に現在のページ情報をセットし、
     * lf.nextpage に次のページの情報をセット。
     * <p>
     * return: false でデータ終了
     */
    static boolean layout_page(LayoutWork p, PvLayout.LayoutFirst lf) {
        Point ptcur = new Point();
        PvLayout.BlockState top_blockstate = null;
        int top_text = 0;
        int wrapnum;
        LayoutLine.COMMAND cmd = null;
        int ret;

        p.pagestate = null;

        p.text = lf.curpage.src;
        p.blockstate = lf.curpage.blockstate;
        p.curlineno = lf.curpage.lineno;

        wrapnum = lf.curpage.wrap_num;

        lf.curpage.diffx = 0;
        lf.nextpage.flags = 0;

        // データの終端なら終了
        if (p.text.charAt(0) == DefStyle.DATATYPE.DATATYPE_END.ordinal())return false;

NEXT:
        {
            // 空白ページの場合は、次ページの処理へ
            if ((lf.curpage.flags & PvLayout.PAGEINFO_F_BLANK) != 0) break NEXT;

            // 現在のページを、各行ごとに処理
            ptcur.x = p.text_right_x;
            ptcur.y = 0;

            while (ptcur.x >= 0) {
                // 次ページに折り返す時用に、行開始時点の情報を保存
                top_text = p.textP;
                top_blockstate = p.blockstate;

                // 1行分の文字データと状態を取得
                //  cmd:ページ関連のコマンドなら、コマンド番号。なければ -1。
                if (!LayoutLine.layout_getline(p, cmd)) break;

                // 挿絵
                if (p.pagestate.picture != -1
                        && p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE))
                    break;

                // ページ関連のコマンド処理
                if (cmd != LayoutLine.COMMAND.COMMAND_NONE) {
                    ret = _proc_command_page(p, cmd, ptcur, lf.pagenum);

                    if (ret == 1)
                        // 次ページを空白に
                        lf.nextpage.flags |= PvLayout.PAGEINFO_F_BLANK;
                    else if (ret == 2)
                        // コマンドを無視
                        continue;

                    break;
                }

                // 本文文字の描画位置セット
                wrapnum = _set_line_char_pos(p, ptcur, wrapnum, 0);
            }

            // ページの左右中央位置
            // (次ページに折り返しが続く場合は除く)
            if (p.pagestate.fcenter && wrapnum == 0)
                lf.curpage.diffx = (ptcur.x + p.line_width) / 2;

        }
        // ------ 次のページ情報

        // 次のページの行番号

        lf.nextpage.lineno = p.curlineno;

        // 現在のページで挿絵があり、先頭行でない場合は、
        // 次ページの先頭は挿絵データ

        if (p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE)
                && p.pagestate.picture != -1
                && ptcur.x != p.text_right_x)
            p.textP = p.pagestate.picture - 1;

        // 次ページの情報

        lf.nextpage.wrap_num = wrapnum;

        if (wrapnum != 0) {
            // 折り返しがある場合、その行の先頭から再処理する
            lf.nextpage.src = p.text.substring(top_text);
            lf.nextpage.blockstate = top_blockstate;
        } else {
            // 折り返しがない場合、現在の状態をセット
            lf.nextpage.src = p.text;
            lf.nextpage.blockstate = p.blockstate;
            lf.nextpage.lineno++;
        }

        return true;
    }

    /**
     * 1ページの描画
     * <p>
     * page: 描画するページ情報
     * pagepos: 単ページの場合 -1、見開きの場合はページ位置 (0:右 1:左)
     */
    static void layout_drawpage(LayoutWork p, PvLayout.PageInfo page, int pagepos) throws IOException {
        Point ptcur = new Point();
        int wrapnum;
        LayoutLine.COMMAND cmd = LayoutLine.COMMAND.COMMAND_NONE;
        int ret;

        // ページ情報描画
        if ((gdat.viewflags & VIEWFLAGS_PAGENO) != 0)
            LayoutDraw.layout_draw_pageinfo(p, page.pageno, pagepos);

        // 空白ページの場合、何も描画しない
        if ((page.flags & PvLayout.PAGEINFO_F_BLANK) != 0) return;

        //

        p.pagestate = null;

        p.text = page.src;
        p.blockstate = page.blockstate;

        ptcur.x = p.text_right_x;
        ptcur.y = 0;

        wrapnum = page.wrap_num;

        //
        while (ptcur.x >= 0) {
            // 1行分取得
            if (!LayoutLine.layout_getline(p, cmd))break;

            // 挿絵
            if (p.pagestate.picture != -1
                    && p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_ENABLE_PICTURE)) {
                // ページ先頭でない場合、次ページへ

                if (ptcur.x == p.text_right_x)
                    LayoutDraw.layout_draw_picture(p, pagepos);

                break;
            }

            // ページ単位のコマンド
            if (cmd != LayoutLine.COMMAND.COMMAND_NONE) {
                ret = LayoutMain._proc_command_page(p, cmd, ptcur, page.pageno);

                if (ret == 2)
                // コマンド無視
                    continue;
                else
                    break;
            }

            // 描画位置セット
            LayoutMain._set_line_char_pos(p, ptcur, wrapnum, page.diffx);

            _proc_draw_line_jiage(p);

            _set_draw_line_ruby_pos(p);

            wrapnum = 0;

            // 描画
            LayoutDraw.layout_draw_line(p, pagepos);
        }
    }
}