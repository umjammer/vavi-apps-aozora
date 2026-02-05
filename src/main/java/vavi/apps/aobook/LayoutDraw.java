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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

import org.apache.tools.ant.util.StreamUtils;
import vavi.apps.aobook.PvLayout.CharItem;
import vavi.apps.aobook.PvLayout.RubyItem;

import static vavi.apps.aobook.Layout.gdat;
import static vavi.apps.aobook.Layout.mFontGetVertHeight;
import static vavi.apps.aobook.PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_NORMAL;
import static vavi.apps.aobook.PvLayout.RUBYITEM_CHARH_NO_PADDING;


/**
 * Layout - Drawing
 */
class LayoutDraw {

    private LayoutDraw() {
    }

    /** Emphasis dot characters */
    private static final char[] g_bouten_char = {'﹅', '﹆', '◉', '・', '○', '▲', '△', '◎', '×'};

    private static final int FONT_DRAW_F_PRINT = 0;
    private static final int FONT_DRAW_F_DAKUTEN_VERT = 1;
    private static final int FONT_DRAW_F_ROTATE = 2;

    // Emphasis line drawing

    /** Draw mPixbuf 1xN pattern */
    private static void _draw_pixbuf_pattern(Graphics2D img, int x, int y, int h, Color col, int pat, int patw) {
        int iy, pcnt;
        int f;

        img.setColor(col);
        Polygon p = new Polygon();
        for (iy = 0, f = 0x80, pcnt = patw; iy < h; iy++) {
            if ((pat & f) != 0)
                p.addPoint(x, y + iy);

            //

            pcnt--;
            if (pcnt != 0)
                f >>= 1;
            else {
                f = 0x80;
                pcnt = patw;
            }
        }
        img.fillPolygon(p);
    }

    /* Draw mPixbuf wavy line */
    private static void _draw_pixbuf_wave(Graphics2D img, int x, int y, int h, Color col) {
        int iy, py;
        byte[] pat = {1, 1, 2, 2, 2, 1, 1, 0, 0, 0};

        for (iy = 0, py = 0; iy < h; iy++) {
            img.setColor(col);
            img.drawLine(x + pat[py], y + iy, x + pat[py] + 1, y + iy + 1);

            py++;
            if (py == 10) py = 0;
        }
    }

    /* Draw emphasis line */
    private static void _draw_pixbuf_bousen(Graphics2D img, int x, int y, int h, Color col, DefStyle.BOUSEN_TYPE type) {
        img.setColor(col);
        switch (type) {
        // Normal emphasis line
        case BOUSEN_TYPE_NORMAL:
            img.drawLine(x, y, x, y + h);
            break;
        // Double emphasis line
        case BOUSEN_TYPE_DOUBLE:
            img.drawLine(x, y, x, y + h);
            img.drawLine(x + 2, y, x + 2, y + h);
            break;
        // Chain line
        case BOUSEN_TYPE_KUSARI:
            _draw_pixbuf_pattern(img, x, y, h, col, 0xc0, 4);
            break;
        // Dashed line
        case BOUSEN_TYPE_HASEN:
            _draw_pixbuf_pattern(img, x, y, h, col, 0xf8, 8);
            break;
        // Wavy line
        case BOUSEN_TYPE_NAMISEN:
            _draw_pixbuf_wave(img, x, y, h, col);
            break;
        }
    }

    // Draw 1 line

    /** Draw body text */
    private static void _draw_text(LayoutWork p, int xtop, int ytop) {
        Graphics2D img;
        Font[] font = new Font[2];
        int flags_base;
        Color col;
        EnumSet<PvLayout.CHARITEM_F> charflags;
        int x, y, n1;
        int fontno;
        int[] fonth = new int[2];
        boolean fdash_to_line;
        DefStyle.STYLE_DAKUTEN dakuten_type;

        font[0] = p.font_main;
        font[1] = p.font_bold;
        fonth[0] = p.fontmain_h;
        fonth[1] = p.fontbold_h;

        col = p.stdef.col_text;
        img = p.img;

        fdash_to_line = p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_DASH_TO_LINE);
        dakuten_type = p.stdef.dakuten_type;

        // Replace with standard print fonts
        flags_base = p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_REPLACE_PRINT) ? FONT_DRAW_F_PRINT : 0;

        //
        for (int i = 0; i < p.list_char.size(); i++) {
            CharItem pi = p.list_char.get(i);
            CharItem next = i + 1 < p.list_char.size() ? p.list_char.get(i + 1) : null;

            if (pi.x < 0) break;
            if (pi.x >= p.pageW) continue; // Skip the part before wrapping on the previous page

            x = xtop + pi.x;
            y = ytop + pi.y + pi.padding;
            charflags = pi.flags;

            fontno = charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_BOLD) ? 1 : 0;

            // Character
            if (pi.chartype == PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_HORZ) {
                // [Horizontal within vertical]
                img.setFont(font[fontno]);
                img.setColor(col);
                img.drawString(new String(pi.horzchar, 0, pi.horzcnt), x + (fonth[0] - pi.width) / 2, y + fonth[fontno]);
            } else {
                // [Normal or horizontal layout]

                // Character
                if (fdash_to_line
                        && (pi.code == 0x2015 || pi.code == 0x2500) // em dash and box drawing character
                        && pi.chartype == PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_NORMAL) {
                    // Draw horizontal line character as a straight line

                    n1 = pi.height;

                    if (next != null
                            && (next.code == 0x2015 || next.code == 0x2500)
                            && next.chartype == CHARITEM_TYPE_NORMAL
                            && !next.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP))
                        // If next character is also a horizontal line, add character space
                        n1 += p.stdef.char_space;

                    img.setColor(p.pixcol_text);
                    img.drawLine(x + (fonth[0] >> 1), y, x + (fonth[0] >> 1), y + n1);
                } else {
                    // Normal
                    img.setFont(font[fontno]);
                    img.setColor(col);
                    drawStringV(img, new String(Character.toChars(pi.code)), x, y, pi.chartype == PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_ROTATE);
                }

                // Dakuten/Handakuten

                if (charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN) || charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_HANDAKUTEN)) {
                    int c = dakuten_type.ordinal() < DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE.ordinal() ?
                            (charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN) ? '゛' : '゜') :
                            (charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN) ? 0x3099 : 0x309a);

                    // Position
                    n1 = 0;
                    int n2 = 0;

                    if (dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_NORMAL_HORZ
                            || dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE_HORZ)
                        n1 = fonth[fontno];
                    else if (dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_NORMAL_VERT
                            || dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE_VERT) {
                        n2 = -fonth[fontno];
                    }

                    img.setFont(font[fontno]);
                    img.setColor(col);
                    img.drawString(String.valueOf((char) c), x + n1, y + n2 + fonth[fontno]);
                }
            }

            // Emphasis dot

            if (pi.bouten != 0) {
                img.setFont(p.font_kenten);
                img.setColor(col);
                img.drawString(String.valueOf(g_bouten_char[pi.bouten - 1]), x + fonth[fontno], y + (pi.height - p.fontkenten_h) / 2 + p.fontkenten_h);
            }
        }
    }

    private static void drawStringV(Graphics2D img, String s, int x, int y, boolean rotate) {
        int ascent = img.getFontMetrics().getAscent();
        if (rotate) {
            img.translate(x + ascent, y);
            img.rotate(Math.PI / 2);
            img.drawString(s, 0, 0);
            img.rotate(-Math.PI / 2);
            img.translate(-(x + ascent), -y);
        } else {
            img.drawString(s, x, y + ascent);
        }
    }

    /* Draw ruby */
    private static void _draw_ruby(LayoutWork p, int xtop, int ytop, int pagepos) {
        RubyItem pi;
        CharItem pichar;
        Font font;
        int x, y, addx, rlen, pad, padtop, pad2 = 0, n, overh, last_x, last_y;
        Color col;

        // Add character width
        xtop += p.fontmain_h;

        font = p.font_ruby;
        col = p.stdef.col_ruby;
        overh = p.fontruby_h / 2;
        last_x = last_y = -1;

        for (int ri = 0; ri < p.list_ruby.size(); ri++) {
            pi = p.list_ruby.get(ri);
            // Wrap to next page
            if (pi.x < 0) break;

            x = pi.x;
            y = pi.y;
            rlen = pi.rubylen;

            // If the previous ruby exceeds the start of the current ruby after wrapping, continue from there

            if (x == last_x && y < last_y)
                y = last_y;

            // Head of parent string
            pichar = pi.char_top;

            // x+2 if there is an emphasis line
            addx = pichar.bousen != DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE ? 2 : 0;

            // Ruby padding
            pad = pi.char_h - pi.ruby_h;

            if (pi.char_h == RUBYITEM_CHARH_NO_PADDING) {
                // From the head of parent string, no padding
                padtop = 0;
                pad2 = 0;
            } else if (rlen == 1)
                // If ruby is 1 character
                padtop = pad / 2;
            else {
                // If ruby is 2 or more characters
                padtop = pad / (rlen * 2);
                pad2 = pad - padtop * 2;
                pad = 0;
            }

            // Each ruby character
            for (int i = 0; i < rlen; i++) {
                // Add padding
                if (i == 0)
                    y += padtop;
                else if (pad2 != 0) {
                    // If ruby is 2 or more characters, fixed interval values won't result in equal spacing if there are many characters,
                    // so calculate padding width based on position.
                    // pad = previous padding position, n = current ruby padding position
                    n = (int) ((double) i / (rlen - 1) * pad2 + 0.5);
                    y += n - pad;
                    pad = n;
                }

                // If it exceeds the current parent character, go to the next parent character
                //  : For the last ruby, draw even at parent's bottom -1 px
                //  : Otherwise, it can protrude up to half the height of the ruby
                n = (i == rlen - 1) ? 0 : overh;

                if (pichar != null && y >= pichar.y + pichar.height - n) {
                    int charIdx = p.list_char.indexOf(pichar);
                    if (charIdx != -1) {
                        for (int j = charIdx + 1; j < p.list_char.size(); j++) {
                            pichar = p.list_char.get(j);
                            if (pichar.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)
                                    || y <= pichar.y + pichar.height - pichar.height / 4)
                                break;
                        }
                    }

                    // If next is wrapping, ruby also wraps according to parent character
                    if (pichar != null && pichar.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)) {
                        x -= p.line_width;
                        y = p.jisage_wrap_y;

                        // If it's the last line of the right page of a spread, move to left page position

                        if (pagepos == 0 && x < 0)
                            x -= p.stdef.page_space;
                    }
                }

                // Draw ruby
                //  Because characters wrapped from the previous page may exist,
                //  judge x range character by character (x is position of body character)
                if (x >= 0 && x < p.pageW) {
                    p.img.setFont(font);
                    p.img.setColor(col);
                    String s = String.valueOf(pi.rubytxt.charAt(i));
                    drawStringV(p.img, s, x + xtop + addx, y + ytop, false);
                    y += mFontGetVertHeight(p.img, font, s).height;
                } else {
                    // In case of position wrapped from the previous page, only add height
                    y += mFontGetVertHeight(p.img, font, String.valueOf(pi.rubytxt.charAt(i))).height;
                }
            }

            last_x = x;
            last_y = y;
        }
    }

    /** Draw emphasis line */
    private static void _draw_bousen(LayoutWork p, int xtop, int ytop) {
        CharItem pi, next;
        int h, x, y, charspace;
        DefStyle.BOUSEN_TYPE type;
        Color col;

        col = p.pixcol_text;

        charspace = p.stdef.char_space;

        for (int i = 0; i < p.list_char.size(); i++) {
            pi = p.list_char.get(i);
            if (pi.bousen == DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE) continue;

            // Draw until emphasis line ends

            x = pi.x;
            y = pi.y;
            h = 0;
            type = pi.bousen;

            int j;
            for (j = i; j < p.list_char.size(); j++) {
                pi = p.list_char.get(j);
                if (pi.bousen != type || (j > i && pi.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP))) break;

                h += pi.height;
                if (j < p.list_char.size() - 1) {
                    next = p.list_char.get(j + 1);
                    if (next.bousen == type && !next.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)) {
                        h += charspace;
                    }
                }
            }

            if (x >= 0 && x < p.pageW) {
                // Draw collectively at the end of emphasis line or end of one line on drawing
                _draw_pixbuf_bousen(p.img, x + p.fontmain_h + xtop, y + ytop, h, col, type);
            }

            i = j - 1; // Skip processed characters
        }
    }

    /** Draw 1 line */
    static void layout_draw_line(LayoutWork p, int pagepos) {
        int xtop, ytop;

        xtop = p.stdef.margin.left;
        ytop = p.stdef.margin.top;

        // If right page in spread view
        if (pagepos == 0)
            xtop += p.stdef.page_space + p.pageW;

        // Body text
        _draw_text(p, xtop, ytop);

        // Emphasis line
        _draw_bousen(p, xtop, ytop);

        // Ruby
        _draw_ruby(p, xtop, ytop, pagepos);
    }


    /**
     * Draw page information
     * <p>
     * pos: [-1] Single page [0] Right side [1] Left side
     */
    static void layout_draw_pageinfo(LayoutWork p, int pageno, int pos) {
        String m;
        int x;

        if (pos == -1 || pos == 0)
            // Right side
            m = String.format("%d/%d", pageno + 1, p.pagenum);
        else
            // Left
            m = String.format("%d", pageno + 1);

        //

        if (pos == -1 || pos == 1)
            // Single page or left side
            x = 4;
        else {
            // Right side of spread

            x = p.stdef.margin.left + p.stdef.margin.right
                    + p.pageW * 2 + p.stdef.page_space
                    - 4 - mFontGetVertHeight(p.img, p.style.font_info, m).width;
        }

        p.img.setFont(p.style.font_info);
        p.img.setColor(p.stdef.col_info);
        p.img.drawString(m, x, 4);
    }

    /** Load image */
    private static BufferedImage _load_image(String fname, boolean fzip) throws IOException {
        InputStream open;
        BufferedImage img;

        if (fzip) {
            ZipFile zf = new ZipFile(fname);
            Optional<? extends ZipEntry> oe = StreamUtils.enumerationAsStream(zf.entries()).filter(e -> e.getName().contains(gdat.strFileName)).findFirst();
            if (!oe.isPresent()) {
                return null;
            }

            open = zf.getInputStream(oe.get());
        } else {
            // Normal file: base path is text file path
            open = Files.newInputStream(Paths.get(fname));
        }

        img = ImageIO.read(open);

        return img;
    }

    /** Draw picture */
    static void layout_draw_picture(LayoutWork p, int pagepos) throws IOException {
        String str;
        BufferedImage img;
        Rectangle box = new Rectangle();
        int x, y;

        // Filename
        if (gdat.is_file_zip) {
            // ZIP
            str = String.valueOf(p.pagestate.picture + 2);
        } else {
            // Normal file: Base path is the text file's path
            str = gdat.strFileName;
            str += String.valueOf(p.pagestate.picture + 2);
        }

        // Load
        img = _load_image(str, gdat.is_file_zip);

        // Position/Size
        box.x = box.y = 0;
        box.width = img.getWidth();
        box.height = img.getHeight();

        mBoxResize_keepaspect(box, p.pageW, p.pageH, true);

        x = p.stdef.margin.left + box.x;
        y = p.stdef.margin.right + box.y;

        if (pagepos == 0)
            x += p.stdef.page_space + p.pageW;

        // Draw
        p.img.drawImage(img, x, y, box.width, box.height, null);
    }

    private static void mBoxResize_keepaspect(Rectangle box, int pageW, int pageH, boolean b) {
        double sx, sy, s;

        if (box.width == 0 || box.height == 0) return;

        if (!b) {
            // Do not enlarge

            if (box.width > pageW) box.width = pageW;
            if (box.height > pageH) box.height = pageH;
        } else {
            sx = (double) pageW / box.width;
            sy = (double) pageH / box.height;
            s = Math.min(sx, sy);

            box.width = (int) (box.width * s);
            box.height = (int) (box.height * s);
        }

        box.x = (pageW - box.width) / 2;
        box.y = (pageH - box.height) / 2;
    }
}
