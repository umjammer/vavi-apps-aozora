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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import vavi.apps.aobook.PvLayout.LayoutFirst;
import vavi.apps.aobook.PvLayout.PageInfo;

import static vavi.apps.aobook.LayoutMain.layout_drawpage;
import static vavi.apps.aobook.LayoutMain.layout_page;


/**
 * Layout operation functions
 */
class Layout {

    private LayoutDat ldat;

    List<TitleItem> titleItems;

    public void layout(String text) {
        gdat = new GDAT();
        gdat.style = new StyleWork();
        gdat.style.b = new StyleDef("default");
        try {
            gdat.style.StyleWork_readStyle(new String[]{"default"});
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        
        // Convert plain text to internal format
        String internalData = convertToInternalFormat(text);
        gdat.textbuf = internalData.getBytes(StandardCharsets.UTF_8);

        ldat = LayoutAlloc();
        LayoutRunFirst(ldat, p -> {
        });
    }

    /**
     * Convert plain text to internal layout format.
     * The internal format expects type markers (DATATYPE_*) for parsing.
     */
    private String convertToInternalFormat(String text) {
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n", -1);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.isEmpty()) {
                // Add each character as a normal character
                // DATATYPE_NORMAL_TEXT_16 = 3, followed by length (2 bytes), then characters (2 bytes each)
                sb.append((char) DefStyle.DATATYPE.DATATYPE_NORMAL_TEXT_16.ordinal());
                sb.append((char) line.length()); // Length as single char (simplified)
                for (int j = 0; j < line.length(); j++) {
                    sb.append(line.charAt(j));
                }
            }
            // Add newline marker except for last line
            if (i < lines.length - 1) {
                sb.append((char) DefStyle.DATATYPE.DATATYPE_ENTER.ordinal());
            }
        }
        
        // Add end marker
        sb.append((char) DefStyle.DATATYPE.DATATYPE_END.ordinal());
        
        return sb.toString();
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

        int w = lw.pageW + lw.stdef.margin.left + lw.stdef.margin.right;
        int h = lw.pageH + lw.stdef.margin.top + lw.stdef.margin.bottom;
        if (lw.stdef.pages == 2) w = (lw.pageW * 2) + lw.stdef.page_space + lw.stdef.margin.left + lw.stdef.margin.right;

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

        /** 0:Large 1:Medium 2:Small */
        int type;
        /** Page number */
        int pageno;
        /** Title text */
        String text;
    }

    /** Layout information */
    static class LayoutDat {

        /** Page number */
        int page_num;
        /** Count of each title */
        final int[] title_num = new int[3];
        /** List of pages */
        List<PageInfo> list_page;
        /** List of titles */
        List<PvLayout.StringItem> list_title;
        /**
         * Height flags for each character of the body font (for lower 16 bits of Unicode)
         * ON=Fetched once and same as full-width height. OFF=Different height, or horizontal layout, etc.
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
     * Create LayoutWork
     * <p>
     * img: Destination for drawing. null for initial layout only.
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

        // At first layout

        if (img == null)
            p.plist_title = info.list_title;

        // Initialize working lists
        p.list_char = new ArrayList<>();
        p.list_ruby = new ArrayList<>();

        return p;
    }

    static Dimension mFontGetVertHeight(Graphics g, Font font) {
        return mFontGetVertHeight(g, font, null);
    }

    private static Graphics2D measurementGraphics;
    private static BufferedImage measurementImage;

    static Dimension mFontGetVertHeight(Graphics g, Font font, String sample) {
        // If no Graphics provided, create a temporary one for font measurement
        Graphics2D graphics;
        if (g == null) {
            if (measurementGraphics == null) {
                measurementImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                measurementGraphics = measurementImage.createGraphics();
            }
            graphics = measurementGraphics;
        } else {
            graphics = (Graphics2D) g;
        }
        
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontRenderContext frc = graphics.getFontRenderContext();

        String sampleText = sample != null && !sample.isEmpty() ? sample : "sample";
        AttributedString as = new AttributedString(sampleText);
        as.addAttribute(TextAttribute.FONT, font, 0, sampleText.length());
        AttributedCharacterIterator aci = as.getIterator();

        TextLayout tl = new TextLayout(aci, frc);
        int sw = (int) tl.getBounds().getWidth();
        int sh = (int) tl.getBounds().getHeight();
        return new Dimension(sw, sh);
    }

    /** Adjust page position */
    private PageInfo page_adjust(LayoutDat p, PageInfo pi) {
        if (pi == null)
            // If null, it is end
            return LayoutGetPage_homeEnd(p, true);
        else if (gdat.style.b.pages == 2 && pi != null && (pi.pageno & 1) != 0)
            // If spread view, if it is odd position, go back one
            return p.list_page.listIterator().previous();
        else
            return pi;
    }

    // Page operation

    /** Get first/last page */
    PageInfo LayoutGetPage_homeEnd(LayoutDat p, boolean end) {
        PageInfo pi = end ? p.list_page.get(p.list_page.size() - 1) : p.list_page.get(0);

        return pi != null ? page_adjust(p, pi) : null;
    }

    /** Get page from page number */
    PageInfo LayoutGetPage_pageno(LayoutDat p, int page) {
        return page_adjust(p, p.list_page.get(page));
    }

    /** Get page from line number */
    PageInfo LayoutGetPage_lineno(LayoutDat p, int line, boolean wrap_top) {

        // wrap_top: true for line number specification, false for use as page position substitute.
        //  For line number specification, page should be visible from the beginning of that line.
        //  For use as page position substitute, if that line wraps,
        //  it is correct as page position not to include the beginning of wrapping.
        //  (If it wraps across multiple pages, it becomes the first page after wrapping)
        PageInfo pi = null;
        for (int i = 0; i < p.list_page.size(); i++) {
            pi = p.list_page.get(i);
            PageInfo next = p.list_page.get(i + i);

            // No next = Last page.
            // Search range from start line number of each page

            if (next == null
                    || (pi.lineno <= line && line < next.lineno)
                    || (wrap_top && line == next.lineno && next.wrap_num != 0))
                // If that line wraps to the next page, specify the previous page
                break;
        }

        return page_adjust(p, pi);
    }

    /** Get page moved by specified number in specified direction */
    PageInfo LayoutGetPage_move(LayoutDat p, PageInfo pi, int dir) {
        int i;

        dir *= gdat.style.b.pages;

        if (dir < 0) {
            // Backward
            for (i = -dir; p.list_page.get(i - 1) != null && i > 0; i--, pi = p.list_page.get(i - 1)) ;
        } else {
            // Forward
            for (i = dir; p.list_page.get(i + 1) != null && i > 0; i--, pi = p.list_page.get(i + 1)) ;
        }

        return page_adjust(p, pi);
    }

    /** Get page number */
    int LayoutGetPageNo(PageInfo pi) {
        return pi != null ? pi.pageno : 0;
    }

    /** Get text line number at the beginning of the page */
    int LayoutGetPageLineNo(PageInfo pi) {
        return pi != null ? pi.lineno : 0;
    }

    /** Allocate layout data */
    LayoutDat LayoutAlloc() {
        LayoutDat p;

        p = new LayoutDat();

        p.buf_hflags = new int[HEIGHTBUF_SIZE];
        p.list_page = new ArrayList<>();
        p.list_title = new ArrayList<>();

        return p;
    }

    /**
     * Initial layout processing
     * <p>
     * Create page information + Extract titles
     */
    void LayoutRunFirst(LayoutDat info, Consumer<Integer> prog) {
        LayoutWork p;
        LayoutFirst lf = new LayoutFirst();
        lf.curpage = new PageInfo();
        lf.nextpage = new PageInfo();
        PageInfo pi;
        int topbuf;
        int textsize;

        // Create LayoutWork
        p = _create_layoutwork(info, null);

        p.pfirst = lf;

        // Buffer for title string
//        mBufAlloc(p.buf_title, 1024, 1024);

        lf.curpage.src = new String(gdat.textbuf, StandardCharsets.UTF_8);

        // Set each page info
        topbuf = p.textP;
        textsize = gdat.textbuf.length;

        while (layout_page(p, lf)) {
            // Add page
            lf.curpage.pageno = lf.pagenum;
            info.list_page.add(lf.curpage);

            lf.pagenum++;
            lf.curpage = lf.nextpage;
            lf.nextpage = new PageInfo();

            // Progress
            prog.accept((int) ((double) (p.textP - topbuf) / textsize * 100 + 0.5));
        }

        // Page number
        info.page_num = lf.pagenum;

        // Title count
        System.arraycopy(p.title_num, 0, info.title_num, 0, 3);
    }

    /**
     * Draw page
     * <p>
     * page: null for first page
     */
    void LayoutDrawPage(LayoutDat info, Graphics2D img, PageInfo page) throws IOException {
        LayoutWork p;
        PageInfo next;

        if (page == null) page = info.list_page.get(0);

        p = _create_layoutwork(info, img);

        p.pagenum = info.page_num;

        // Drawing

        if (p.stdef.pages == 1)
            // Single page
            layout_drawpage(p, page, -1);
        else {
            // Spread

            layout_drawpage(p, page, 0);

            next = info.list_page.get(info.page_num + 1);
            if (next != null) layout_drawpage(p, next, 1);
        }
    }
}
