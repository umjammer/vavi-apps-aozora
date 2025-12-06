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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import vavi.apps.aobook.PvLayout.CharItem;

import static vavi.apps.aobook.Layout.mFontGetVertHeight;
import static vavi.apps.aobook.LayoutSub.layout_ischar_tatetyuyoko;
import static vavi.apps.aobook.PvLayout.CHARITEM_F.CHARITEM_F_BOLD;


/**
 * レイアウト
 * <p>
 * 内部データから1行分のレイアウト用データ取得
 */
class LayoutLine {

    private LayoutLine() {
    }

    /**
     * 注記のコマンド
     * START+1 = END にすること
     */
    enum COMMAND {
        COMMAND_NONE,
        /** 挿絵 (変換中のみ) */
        COMMAND_PICTURE,
        /** 改丁 */
        COMMAND_KAITYO,
        /** 改ページ */
        COMMAND_KAIPAGE,
        /** 改見開き */
        COMMAND_KAIMIHIRAKI,
        /** 左右中央 */
        COMMAND_PAGE_CENTER,
        /** 見出し [val1=type] */
        COMMAND_TITLE_START,
        COMMAND_TITLE_END,
        /** n字下げ (1行) [val1=字下げ数, val2=折り返し下げ数] */
        COMMAND_JISAGE_LINE,
        /** n字下げ */
        COMMAND_JISAGE_START,
        /** 字下げ終了(共通) */
        COMMAND_JISAGE_END,
        /** n字下げ、折り返してn字下げ */
        COMMAND_JISAGE_WRAP_START,
        /** 改行天付き、折り返してn字下げ */
        COMMAND_JISAGE_TEN_WRAP_START,
        /** 地付き/地からn字上げ (1行) [val1=地上げ数 (1=地付き、2〜=n字上げ)] */
        COMMAND_JIAGE_LINE,
        /** 開始 */
        COMMAND_JIAGE_START,
        /** 終了 */
        COMMAND_JIAGE_END,
        /** 太字 */
        COMMAND_BOLD_START,
        COMMAND_BOLD_END,
        /** 縦中横 */
        COMMAND_TATETYUYOKO_START,
        COMMAND_TATETYUYOKO_END,
        /** 横組み */
        COMMAND_YOKOGUMI_START,
        COMMAND_YOKOGUMI_END,
        /** 傍点 [val1=type] */
        COMMAND_BOUTEN_START,
        COMMAND_BOUTEN_END,
        /** 傍線 [val1=type] */
        COMMAND_BOUSEN_START,
        COMMAND_BOUSEN_END
    }

    // コマンド処理

    /**
     * 値なしのコマンド処理
     * <p>
     * cmd: コマンド番号
     * return: ページ関連のコマンドなら、コマンド番号。ほかは -1。
     */
    private static COMMAND _proc_command_noval(LayoutWork p, COMMAND cmd) {
        PvLayout.LineState lst = p.linestate;
        PvLayout.BlockState bst = p.blockstate;

        switch (cmd) {
        // 改ページ/改丁/改見開き
        case COMMAND_KAIPAGE:
        case COMMAND_KAITYO:
        case COMMAND_KAIMIHIRAKI:
            return cmd;
        // 字下げ終わり [block]
        case COMMAND_JISAGE_END:
            bst.jisage = bst.jisage_wrap = 0;
            break;
        // 縦中横 [line]
        case COMMAND_TATETYUYOKO_START:
            lst.tatetyuyoko_num = 1;
            break;
        case COMMAND_TATETYUYOKO_END:
            lst.tatetyuyoko_num = 0;
            break;
        // 横組み
        case COMMAND_YOKOGUMI_START:
            bst.flags.add(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_YOKOGUMI);
            break;
        case COMMAND_YOKOGUMI_END:
            bst.flags.remove(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_YOKOGUMI);
            break;
        // 太字
        case COMMAND_BOLD_START:
            bst.flags.add(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_BOLD);
            break;
        case COMMAND_BOLD_END:
            bst.flags.remove(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_BOLD);
            break;
        // 傍点終わり
        case COMMAND_BOUTEN_END:
            lst.bouten = 0;
            break;
        // 傍線終わり
        case COMMAND_BOUSEN_END:
            lst.bousen = DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE;
            break;
        // 地付き/地上げ終わり
        case COMMAND_JIAGE_END:
            bst.jiage = 0;
            break;
        // 見出し終わり
        case COMMAND_TITLE_END:
            bst.title = 0;

            if (p.plist_title != null)
                LayoutSub.layout_append_titlelist(p);
            break;
        // ページの左右中央
        case COMMAND_PAGE_CENTER:
            p.pagestate.fcenter = true;
            break;
        }

        return COMMAND.COMMAND_NONE;
    }

    /** 値付きのコマンド処理 */
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
        // 字下げ [line]
        case COMMAND_JISAGE_LINE:
            lst.jisage = val1;
            break;
        // 字下げ [block]
        case COMMAND_JISAGE_START:
            bst.jisage = val1;
            bst.jisage_wrap = val1;
            break;
        // 字下げ、折り返し
        case COMMAND_JISAGE_WRAP_START:
            bst.jisage = val1;
            bst.jisage_wrap = val2;
            break;
        // 字下げ、天付き・折り返し
        case COMMAND_JISAGE_TEN_WRAP_START:
            bst.jisage = 0;
            bst.jisage_wrap = val1;
            break;
        // 傍点開始
        case COMMAND_BOUTEN_START:
            lst.bouten = val1;
            break;
        // 傍線開始
        case COMMAND_BOUSEN_START:
            lst.bousen = DefStyle.BOUSEN_TYPE.values()[val1];
            break;
        // 地付き/地からn字上げ [line]
        case COMMAND_JIAGE_LINE:
            lst.jiage = val1;
            break;
        // 地付き/地からn字上げ [block]
        case COMMAND_JIAGE_START:
            bst.jiage = val1;
            break;
        // 見出し
        case COMMAND_TITLE_START:
            bst.title = val1;
            break;
        }
    }

    /**
     * 挿絵処理
     * <p>
     * [uint16] 文字列長さ, UTF-8 ファイル名 (null 文字含む)
     */
    private static void _proc_picture(LayoutWork p, String text, int[] pp) {
        int ps;
        int len;

        ps = pp[0];

        p.pagestate.picture = ps;

        len = ps;

        pp[0] = ps + 2 + len;
    }

    // ルビなし本文文字列追加

    /**
     * [本文文字追加時] CharItem に、現在の注記状態を適用する
     * <p>
     * [!] 縦中横の場合、2文字目以降は pi が削除される。
     */
    private static void _set_char_state(LayoutWork p, PvLayout.CharItem pi) {
        PvLayout.CharItem pitop;
        PvLayout.BlockState bst = p.blockstate;
        PvLayout.LineState lst = p.linestate;
        int n;

        // [block/line] 地付き/地上げ

        if (lst.jiage != 0 || bst.jiage != 0)
            pi.flags.add(PvLayout.CHARITEM_F.CHARITEM_F_JIAGE);

        // [block] 太字

        if (bst.flags.contains(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_BOLD))
            pi.flags.add(CHARITEM_F_BOLD);

        // [line] 傍点/傍線

        pi.bouten = lst.bouten;
        pi.bousen = lst.bousen;

        // 見出し文字列
        //  :見出しが終わるまで、buf_title に文字を追加していく
        //  :※最初のレイアウト時のみ

        if (bst.title != 0 && p.plist_title != null) {
            // 最初の文字の場合、1byte目に見出しタイプをセット
            if (p.buf_title.length() == 0)
                p.buf_title += bst.title;

            // 1文字追加
            if (p.buf_title.length() <= 256 * 4)
                p.buf_title += pi.code;
        }

        // 文字の並べ方

        if (bst.flags.contains(PvLayout.BLOCKSTATE_F.BLOCKSTATE_F_YOKOGUMI))
            // [block] 横組み
            pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_ROTATE;
        else if (lst.tatetyuyoko_num != 0) {
            // [line] 縦中横
            n = lst.tatetyuyoko_num; // +1 されている

            if (n < 4 && pi.code < 127) {
                if (n == 1) {
                    // 最初の文字
                    pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_HORZ;
                    pi.horzcnt = 1;
                    pi.horzchar[0] = (byte) pi.code;

                    lst.tatetyuyoko_top = pi;
                } else {
                    // 2〜3文字目は、先頭文字に結合して、CharItem 削除
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
     * [本文文字追加時] 文字列追加後の調整
     * <p>
     * top: 追加された先頭位置
     * return: 実際に追加された文字数 (表示上での1文字)
     */
    private static int _text_adjust(LayoutWork p, CharItem top) {
        CharItem pi, next, prev, third;
        int c;
        int len = 0;

        for (int i = 0; i < p.list_char.size(); i++) {
            pi = p.list_char.get(i);
            prev = i >= 0 ?  p.list_char.get(i - 1) : null;
            next = i < p.list_char.size() - 2 ? p.list_char.get(i + 1) : null;

            c = pi.code;
            len++;

            // 文字置き換え

            if (LayoutSub.layout_replace_char(p, c))
                pi.code = c;

            //

            if (c == '゛' || c == '゜') {
                // 濁点/半濁点の場合、1文字にまとめる

                if (prev != null && !prev.flags.containsAll(EnumSet.of(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN, PvLayout.CHARITEM_F.CHARITEM_F_HANDAKUTEN))) {
                    prev.flags.add(c == '゛' ? PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN : PvLayout.CHARITEM_F.CHARITEM_F_HANDAKUTEN);

                    p.list_char.remove(pi);
                    len--;
                }
            } else if (c == '／') {
                // くの字点を置換え

                if (next != null && next.code == '＼') {
                    // 通常

                    pi.code = 0x3033;
                    next.code = 0x3035;
                } else if (next != null && next.code == '″'
                        && p.list_char.get(i + 2) != null && p.list_char.get(i + 2).code == '＼') {
                    // 濁点付き

                    third = (CharItem) p.list_char.get(i + 2);

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

    // 1行分取得

    /**
     * ルビなしの本文文字列を追加
     * <p>
     * [uint16] 文字数 [uint16 or uint32 x 文字数] 文字列
     * <p>
     * is32bit: true で 32bit、false で 16bit 文字列
     * ppchar: null 以外の場合、追加された先頭の文字データを返す
     * return: 実際に追加された文字数
     */
    private static int _append_text(LayoutWork p, String text, int[] pp, boolean is32bit, PvLayout.CharItem[] ppchar) {
        int ps;
        int len, i, charsize;
        PvLayout.CharItem pi, pitop;
        List<PvLayout.CharItem> list = p.list_char;
        int pibottom = -1;

        ps = pp[0];

        charsize = (is32bit) ? 4 : 2;

        // 文字数

        len = ps;
        ps += 2;

        // 次のソース位置

        pp[0] = ps + charsize * len;

        // 現在の終端位置

        pibottom = list.size() - 1;

        // リストに1文字ずつ追加
        // (縦中横の場合は一つのデータにまとめられる)

        for (i = len; i > 0; i--, ps += charsize) {
            pi = new CharItem();
            list.add(pi);
            if (is32bit)
                pi.code = ps;
            else
                pi.code = ps;

            // 現在の注記状態を適用

            _set_char_state(p, pi);
        }

        // 追加された先頭位置

        pitop = (pibottom != -1) ? list.get(pibottom + 1) : list.get(0);

        // 調整

        len = _text_adjust(p, pitop);

        // 先頭位置

        if (ppchar != null) ppchar[0] = pitop;

        return len;
    }

    /**
     * ルビ付き文字列を追加
     * <p>
     * [uint16] 本文文字数, UTF-32文字列, [uint16] ルビ文字数, UTF-32文字列
     */
    private static void _append_ruby(LayoutWork p, String text, int[] pp) {
        int ps;
        CharItem[] pitop = new CharItem[1];
        PvLayout.RubyItem pi;
        int clen, rlen;

        // 本文文字列を追加

        clen = _append_text(p, text, pp, true, pitop);

        // ルビアイテムを追加

        ps = pp[0];

        rlen = ((int) ps);
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

        // 次のデータ位置

        pp[0] = ps + (rlen << 2);
    }

    /**
     * 内部データから、1行分のデータを取得
     * <p>
     * list_char/list_ruby のリストに文字データ、
     * 注記の情報は、行/ブロック状態にセット。
     */
    private static void _get_line(LayoutWork p, COMMAND cmdret) {
        int[] ps = new int[]{0}; // p.text;
        boolean floop = true;
        COMMAND cmd;

        cmdret = null;

        while (floop) {
            // データタイプ (1byte)
            DefStyle.DATATYPE type = DefStyle.DATATYPE.values()[p.text.charAt(ps[0]++)];

            switch (type) {
            // 行番号の情報
            case DATATYPE_LINEINFO:
                p.curlineno = ((int) ps[0]);
                ps[0] += 4;
                break;
            // 通常文字列
            case DATATYPE_NORMAL_TEXT_16:
            case DATATYPE_NORMAL_TEXT_32:
                _append_text(p, p.text, ps, (type == DefStyle.DATATYPE.DATATYPE_NORMAL_TEXT_32), null);
                break;
            // ルビ付き文字列
            case DATATYPE_RUBY_TEXT:
                _append_ruby(p, p.text, ps);
                break;
            // 改行
            case DATATYPE_ENTER:
                floop = false;
                break;
            // 値なしコマンド
            case DATATYPE_COMMAND:
                cmd = _proc_command_noval(p, COMMAND.values()[ps[0]++]);

                // ページが変わるコマンドの場合
                if (cmd != null) {
                    floop = false;

                    if (p.list_char.get(0) != null) {
                        // 本文文字が残っている場合は先に処理させるため、
                        // 位置を戻す
                        ps[0] -= 2;
                    } else
                        cmdret = cmd;
                }
                break;
            // 値付きコマンド
            case DATATYPE_COMMAND_VAL:
                _proc_command_val(p, p.text, ps);
                break;
            // 挿絵
            case DATATYPE_PICTURE:
                floop = false;

                if (p.list_char.get(0) == null)
                    // 本文文字が残っている場合、先に処理
                    ps[0]--;
                else
                    _proc_picture(p, p.text, ps);
                break;
            // 終了
            case DATATYPE_END:
                ps[0]--;
                floop = false;
                break;
            }
        }

        p.textP = ps[0];
    }

    // 1行分取得後の調整

    /** 半角文字の並び方を自動判定 */
    private static void _set_chartype_auto(LayoutWork p) {
        CharItem pi, next, pitop, pi2;
        int cnt;

        for (int i = 0; i < p.list_char.size(); i++) {
            pi = p.list_char.get(i);
            next = i < p.list_char.size() - 2 ? p.list_char.get(i + 1) : null;

            // すでに文字種が指定されているか、半角文字以外なら、そのまま

            if (pi.chartype != PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_NORMAL || pi.code >= 128)
                continue;

            // ------- 半角文字の自動判定

            // 縦中横の対象文字が連続している数

            for (cnt = 0, pi2 = pi;
                 cnt < 4 && pi2 != null && layout_ischar_tatetyuyoko(pi2.code);
                 cnt++, pi2 = p.list_char.get(i + 1))
                ;

            //

            if (cnt >= 1 && cnt <= 3) {
                // [縦中横]
                //  3文字以内の場合は自動で縦中横にする。
                //  最初の文字だけ残して1文字にまとめる。

                pitop = pi;

                pitop.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_HORZ;
                pitop.horzcnt = cnt;
                pitop.horzchar[0] = (byte) pitop.code;

                for (pi = next, i = 1; pi != null && i < cnt; pi = pi2, i++) {
                    pi2 = p.list_char.listIterator().next();

                    pitop.horzchar[i] = (byte) pi.code;

                    p.list_char.remove(pi);
                }
            } else {
                // [横組み]
                //  4文字以上連続しているなら横組み。
                //  半角文字以外が出るまで続ける。

                for (; pi != null && pi.code < 127; pi = p.list_char.listIterator().next())
                    pi.chartype = PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_ROTATE;
            }

            next = pi;
        }
    }

    /** 本文文字の文字幅/高さをセット */
    private static void _set_char_size(LayoutWork p) {
        CharItem pi;
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

        for (pi = p.list_char.get(0); pi != null; pi = p.list_char.listIterator().next()) {
            code = pi.code;

            // [0] 通常 [1] 太字
            n = pi.flags.contains(CHARITEM_F_BOLD) ? 1 : 0;

            switch (pi.chartype) {
            // 通常縦書き
            case CHARITEM_TYPE_NORMAL:
                if (n != 0 || code > 0xffff)
                    // 太字 or コードが16bitを超える場合は常に計算
                    pi.height = mFontGetVertHeight(p.img, font[n], String.valueOf(code)).height;
                else {
                    // 通常で16bit以内の場合、高速化のため、
                    // 全角高さと同じ場合はフラグを ON にする。
                    // (ほとんどの文字は 16bit 範囲内にあるため)

                    hbuf = p.buf_hflags[code >> 3];
                    f = 1 << (code & 7);

                    if ((hbuf & f) != 0)
                        // 前回取得し、全角高さと同じ
                        pi.height = fonth[0];
                    else {
                        h = mFontGetVertHeight(p.img, font[0], String.valueOf(code)).height;

                        if (h == fonth[0])
                            hbuf |= f;

                        pi.height = h;
                    }
                }
                break;
            // 横組み (90度回転)
            case CHARITEM_TYPE_ROTATE:
                pi.height = mFontGetVertHeight(p.img, font[n], String.valueOf(code)).height; // FONT_DRAW_F_ROTATE
                break;
            // 縦中横
            case CHARITEM_TYPE_HORZ:
                pi.width = mFontGetVertHeight(p.img, font[n], Arrays.toString(pi.horzchar)).width; // pi.horzcnt
                pi.height = fonth[n];
                break;
            }
        }
    }

    /** 親文字列高さ取得 (折り返しなしでの状態) */
    private static int _get_char_height(LayoutWork p, CharItem pi, int len) {
        int h = 0, i;

        for (i = len; i != 0; i--, pi = p.list_char.get(i + 1))
            h += pi.height;

        return h + (len - 1) * p.stdef.char_space;
    }

    /**
     * ルビと親文字列の調整
     * <p>
     * 親文字列よりルビの方が大きい場合、ルビの親文字列に余白を追加
     */
    private static void _set_ruby_parent(LayoutWork p) {
        PvLayout.RubyItem pi;
        CharItem pic;
        int charh, diff, diffmax, clen, sp1, sp2, i;

        diffmax = p.fontmain_h;

        for (pi = p.list_ruby.get(0); pi != null; pi = p.list_ruby.listIterator().next()) {
            // 親文字列の高さ

            charh = _get_char_height(p, pi.char_top, pi.charlen);

            //

            diff = pi.ruby_h - charh;

            if (diff <= diffmax) {
                pi.char_h = charh;
                continue;
            }

            // -------
            pi.char_h = pi.ruby_h;

            // 親文字列の高さに余白を追加
            clen = pi.charlen;

            if (clen == 1) {
                // 親文字列が1文字の場合
                pic = pi.char_top;

                pic.height = pi.ruby_h;
                pic.padding = diff / 2;
            } else {
                // 親文字列が2文字以上の場合
                sp1 = diff / (clen * 2);
                sp2 = diff / clen;

                for (pic = pi.char_top, i = 0; i < clen; i++, pic = p.list_char.listIterator().next()) {
                    if (i == 0) {
                        // 先頭文字
                        pic.height += sp1;
                        pic.padding = sp1;
                        diff -= sp1;
                    } else if (i == clen - 1) {
                        // 終端文字
                        pic.height += diff;
                        pic.padding = sp2;
                    } else {
                        // 間
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
     * 1行分の文字データ取得
     * <p>
     * cmd: 改ページなどページ関連のコマンドが含まれる場合、そのコマンド番号。なければ -1。
     * return: false でデータの終端
     */
    static boolean layout_getline(LayoutWork p, COMMAND cmd) {
        if (p.text.charAt(0) == DefStyle.DATATYPE.DATATYPE_END.ordinal()) return false;

        // 行状態クリア
        p.linestate = new PvLayout.LineState();

        // 1行データ取得
        _get_line(p, cmd);

        // 文字タイプ自動判定
        _set_chartype_auto(p);

        // 本文pxサイズセット
        _set_char_size(p);

        // ルビと親文字列の調整
        _set_ruby_parent(p);

        return true;
    }
}