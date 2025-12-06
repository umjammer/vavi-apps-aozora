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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.List;
import java.util.function.Consumer;

import vavi.apps.aobook.PvLayout.LayoutFirst;
import vavi.apps.aobook.PvLayout.PageInfo;

import static vavi.apps.aobook.LayoutMain.layout_drawpage;
import static vavi.apps.aobook.LayoutMain.layout_page;


/**
 * レイアウト操作関数
 */
class Layout {

    private LayoutDat ldat;

    List<TitleItem> titleItems;

    public void layout(String text) {
        gdat = new GDAT();
        gdat.style = new StyleWork();
        gdat.style.b = new StyleDef();
        try {
            gdat.style.StyleWork_readStyle(new String[]{"default"});
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        gdat.textbuf = text.getBytes(StandardCharsets.UTF_8);

        ldat = LayoutAlloc();
        LayoutRunFirst(ldat, p -> {
        });
    }

    public BufferedImage getImage(int i) {
        if (ldat == null || i < 0 || i >= ldat.page_num) return null;

        PageInfo pi = LayoutGetPage_pageno(ldat, i);
        if (pi == null) return null;

        // get page size
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dummy.createGraphics();
        LayoutWork lw = _create_layoutwork(ldat, g);
        g.dispose();

        int w = lw.pageW;
        int h = lw.pageH;
        if (lw.stdef.pages == 2) w = w * 2 + lw.stdef.page_space;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        try {
            LayoutDrawPage(ldat, g, pi);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            g.dispose();
        }
        return image;
    }

    static class TitleItem extends PvLayout.StringItem {

        /** 0:大 1:中 2:小 */
        int type;
        /** ページ番号 */
        int pageno;
        /** タイトル文字列 */
        String text;
    }

    /** レイアウト情報 */
    static class LayoutDat {

        /** ページ数 */
        int page_num;
        /** 各見出しの個数 */
        int[] title_num = new int[3];
        /** ページのリスト */
        List<PageInfo> list_page;
        /** 見出しのリスト */
        List<PvLayout.StringItem> list_title;
        /**
         * 本文フォントの各文字(Unicode下位16bit分)の高さフラグ
         * ON=一度取得し、全角高さと同じ。OFF=高さが異なる、または横組みなど。
         */
        int[] buf_hflags;
    }

    static final int HEIGHTBUF_SIZE = 0x10000 / 8;

    // sub
    static class GDAT {

        public String strFileName;
        public int viewflags;
        Font font_kenten;
        StyleWork style;
        byte[] textbuf;
        boolean is_file_zip;
    }

    static GDAT gdat;

    /**
     * LayoutWork を作成
     * <p>
     * img: 描画時の描画先。null で最初のレイアウトのみ。
     */
    private LayoutWork _create_layoutwork(LayoutDat info, Graphics2D img) {
        LayoutWork p;
        StyleWork style;
        StyleDef st;
        int linesp;

        style = gdat.style;
        st = style.b;

        //

        p = new LayoutWork();

        p.text = null;
        p.textP = 0;
        p.buf_hflags = info.buf_hflags;
        p.img = img;
        p.style = style;
        p.stdef = st;

        p.u32_nohead = st.StyleDef_createLayoutChars_nohead();
        p.u32_nobottom = st.StyleDef_createLayoutChars_nobottom();
        p.u32_hanging = st.StyleDef_createLayoutChars_hanging();
        p.u32_nosep = st.StyleDef_createLayoutChars_nosep();
        p.u32_replace = st.StyleDef_createLayoutChars_replace();

        p.font_main = style.font_main;
        p.font_ruby = style.font_ruby;
        p.font_bold = style.font_bold;
        p.font_kenten = gdat.font_kenten != null ? gdat.font_kenten : p.font_ruby;

        p.fontmain_h = mFontGetVertHeight(img, p.font_main).height;
        p.fontbold_h = mFontGetVertHeight(img, p.font_bold).height;
        p.fontruby_h = mFontGetVertHeight(img, p.font_ruby).height;
        p.fontkenten_h = mFontGetVertHeight(img, p.font_kenten).height;

        if (img != null)
            p.pixcol_text = st.col_text;

        //

        linesp = (int) (p.fontmain_h * (st.line_space / 100.0) + 0.5);

        p.line_width = p.fontmain_h + linesp;

        p.pageW = st.lines * p.line_width;
        p.pageH = p.fontmain_h * st.chars + st.char_space * (st.chars - 1);

        p.text_right_x = p.pageW - p.line_width;

        // 最初のレイアウト時

        if (img == null)
            p.plist_title = info.list_title;

        return p;
    }

    static Dimension mFontGetVertHeight(Graphics g, Font font) {
        return mFontGetVertHeight(g, font, null);
    }

    static Dimension mFontGetVertHeight(Graphics g, Font font, String sample) {

        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontRenderContext frc = graphics.getFontRenderContext();

        AttributedString as = new AttributedString(sample != null ? sample : "sample");
        as.addAttribute(TextAttribute.FONT, font, 0, "sample".length());
        AttributedCharacterIterator aci = as.getIterator();

        TextLayout tl = new TextLayout(aci, frc);
        int sw = (int) tl.getBounds().getWidth();
        int sh = (int) tl.getBounds().getHeight();
        return new Dimension(sw, sh);
    }

    /** ページ位置の補正 */
    private PageInfo page_adjust(LayoutDat p, PageInfo pi) {
        if (pi == null)
            // null なら終端
            return LayoutGetPage_homeEnd(p, true);
        else if (gdat.style.b.pages == 2 && pi != null && (pi.pageno & 1) != 0)
            // 見開きの場合、奇数位置なら一つ戻る
            return p.list_page.listIterator().previous();
        else
            return pi;
    }

    // ページ操作

    /** 先頭/終端のページ取得 */
    PageInfo LayoutGetPage_homeEnd(LayoutDat p, boolean end) {
        PageInfo pi = end ? p.list_page.get(p.list_page.size() - 1) : p.list_page.get(0);

        return pi != null ? page_adjust(p, pi) : null;
    }

    /** ページ番号からページ取得 */
    PageInfo LayoutGetPage_pageno(LayoutDat p, int page) {
        return page_adjust(p, p.list_page.get(page));
    }

    /** 行番号からページを取得 */
    PageInfo LayoutGetPage_lineno(LayoutDat p, int line, boolean wrap_top) {

        // wrap_top: 行番号指定の場合 true、ページ位置の代わりとして使う場合は false。
        //  行番号指定の場合、その行の先頭からページが見えるように。
        //  ページ位置の代わりとして使う場合、その行が折り返している場合は、
        //  折り返しの先頭を含まない方がページ位置として正しい。
        //  (複数ページにまたがって折り返している場合は、折り返し後の最初のページとなる)
        PageInfo pi = null;
        for (int i = 0; i < p.list_page.size(); i++) {
            pi = p.list_page.get(i);
            PageInfo next = p.list_page.get(i + i);

            // 次がない = 最後のページ。
            // 各ページの先頭の行番号から範囲を検索

            if (next == null
                    || (pi.lineno <= line && line < next.lineno)
                    || (wrap_top && line == next.lineno && next.wrap_num != 0))
                // その行が次のページに折り返している場合、前のページを指定
                break;
        }

        return page_adjust(p, pi);
    }

    /** 指定方向に指定数移動したページを取得 */
    PageInfo LayoutGetPage_move(LayoutDat p, PageInfo pi, int dir) {
        int i;

        dir *= gdat.style.b.pages;

        if (dir < 0) {
            // 前方向
            for (i = -dir; p.list_page.get(i - 1) != null && i > 0; i--, pi = p.list_page.get(i - 1)) ;
        } else {
            // 次方向
            for (i = dir; p.list_page.get(i + 1) != null && i > 0; i--, pi = p.list_page.get(i + 1)) ;
        }

        return page_adjust(p, pi);
    }

    /** ページ番号取得 */
    int LayoutGetPageNo(PageInfo pi) {
        return pi != null ? pi.pageno : 0;
    }

    /** ページの先頭のテキスト行番号取得 */
    int LayoutGetPageLineNo(PageInfo pi) {
        return pi != null ? pi.lineno : 0;
    }

    /** レイアウトデータ確保 */
    LayoutDat LayoutAlloc() {
        LayoutDat p;

        p = new LayoutDat();

        p.buf_hflags = new int[HEIGHTBUF_SIZE];

        return p;
    }

    /**
     * 最初のレイアウト処理
     * <p>
     * ページの情報を作成 + 見出しの抽出
     */
    void LayoutRunFirst(LayoutDat info, Consumer<Integer> prog) {
        LayoutWork p;
        LayoutFirst lf = new LayoutFirst();
        PageInfo pi;
        int topbuf;
        int textsize;

        // LayoutWork 作成
        p = _create_layoutwork(info, null);

        p.pfirst = lf;

        // 見出し文字列用バッファ
//        mBufAlloc(p.buf_title, 1024, 1024);

        lf.curpage.src = new String(gdat.textbuf, StandardCharsets.UTF_8);

        // 各ページ情報セット
        topbuf = p.textP;
        textsize = gdat.textbuf.length;

        while (layout_page(p, lf)) {
            // ページ追加

            pi = new PageInfo();
            info.list_page.add(pi);

            lf.curpage = lf.nextpage;

            // 進捗
            prog.accept((int) ((double) (p.textP - topbuf) / textsize * 100 + 0.5));
        }

        // ページ数
        info.page_num = lf.pagenum;

        // 見出し個数
        System.arraycopy(p.title_num, 0, info.title_num, 0, Integer.BYTES * 3);
    }

    /**
     * ページを描画
     * <p>
     * page: null で先頭ページ
     */
    void LayoutDrawPage(LayoutDat info, Graphics2D img, PageInfo page) throws IOException {
        LayoutWork p;
        PageInfo next;

        if (page == null) page = info.list_page.get(0);

        p = _create_layoutwork(info, img);

        p.pagenum = info.page_num;

        // 描画

        if (p.stdef.pages == 1)
            // 単一ページ
            layout_drawpage(p, page, -1);
        else {
            // 見開き

            layout_drawpage(p, page, 0);

            next = info.list_page.get(info.page_num + 1);
            if (next != null) layout_drawpage(p, next, 1);
        }
    }
}