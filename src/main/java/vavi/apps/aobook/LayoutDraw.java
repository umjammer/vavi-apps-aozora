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
 * レイアウト - 描画
 */
class LayoutDraw {

    /** 傍点文字 */
    private static final char[] g_bouten_char = {'﹅', '﹆', '◉', '・', '○', '▲', '△', '◎', '×'};

    private static final int FONT_DRAW_F_PRINT = 0;
    private static final int FONT_DRAW_F_DAKUTEN_VERT = 1;
    private static final int FONT_DRAW_F_ROTATE = 2;

    // 傍線描画

    /** mPixbuf 1xN パターンを描画 */
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

    /* mPixbuf 波線を描画 */
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

    /* 傍線描画 */
    private static void _draw_pixbuf_bousen(Graphics2D img, int x, int y, int h, Color col, DefStyle.BOUSEN_TYPE type) {
        img.setColor(col);
        switch (type) {
        // 通常傍線
        case BOUSEN_TYPE_NORMAL:
            img.drawLine(x, y, x, y + h);
            break;
        // 二重傍線
        case BOUSEN_TYPE_DOUBLE:
            img.drawLine(x, y, x, y + h);
            img.drawLine(x + 2, y, x + 2, y + h);
            break;
        // 鎖線
        case BOUSEN_TYPE_KUSARI:
            _draw_pixbuf_pattern(img, x, y, h, col, 0xc0, 4);
            break;
        // 破線
        case BOUSEN_TYPE_HASEN:
            _draw_pixbuf_pattern(img, x, y, h, col, 0xf8, 8);
            break;
        // 波線
        case BOUSEN_TYPE_NAMISEN:
            _draw_pixbuf_wave(img, x, y, h, col);
            break;
        }
    }

    // 1行描画

    /** 本文描画 */
    private static void _draw_text(LayoutWork p, int xtop, int ytop) {
        Graphics2D img;
        Font[] font = new Font[2];
        int flags_base, flags;
        Color col;
        int c;
        EnumSet<PvLayout.CHARITEM_F> charflags;
        int x, y, n1, n2;
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

        // 印刷標準字体に置き換え
        flags_base = p.stdef.flags.contains(DefStyle.STYLE_FLAGS.STYLE_F_REPLACE_PRINT) ? FONT_DRAW_F_PRINT : 0;

        //
        for (int i = 0; i < p.list_char.size(); i++) {
            CharItem pi = p.list_char.get(i);
            CharItem next = i + 1 < p.list_char.size() - 1 ? p.list_char.get(i + 1) : null;

            if (pi.x < 0) break;
            if (pi.x >= p.pageW) continue; // 前ページの折り返し前部分はスキップ

            x = xtop + pi.x;
            y = ytop + pi.y + pi.padding;
            charflags = pi.flags;

            fontno = charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_BOLD) ? 1 : 0;

            // 文字
            if (pi.chartype == PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_HORZ) {
                // [縦中横]
                img.setFont(font[fontno]);
                img.setColor(col);
                img.drawString(Arrays.toString(pi.horzchar), x + (fonth[0] - pi.width) / 2, y); // pi.horzcnt
            } else {
                // [通常 or 横組み]

                // フラグ
                flags = flags_base;

                if (pi.chartype == PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_ROTATE)
                    flags |= FONT_DRAW_F_ROTATE;

                // 文字
                if (fdash_to_line
                        && (pi.code == 0x2015 || pi.code == 0x2500) // 全角ダッシュと罫線
                        && pi.chartype == PvLayout.CHARITEM_TYPE.CHARITEM_TYPE_NORMAL) {
                    // 横線文字を直線にして描画

                    n1 = pi.height;

                    if (next != null
                            && (next.code == 0x2015 || next.code == 0x2500)
                            && next.chartype == CHARITEM_TYPE_NORMAL
                            && !next.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP))
                        // 次の文字も横線なら、字間分を加算
                        n1 += p.stdef.char_space;

                    img.setColor(p.pixcol_text);
                    img.drawLine(x + (fonth[0] >> 1), y, x + (fonth[0] >> 1), y + n1);
                } else {
                    // 通常
                    img.setFont(font[fontno]);
                    img.setColor(col);
                    drawStringV(img, String.valueOf(pi.code), x, y); // flags
                }

                // 濁点/半濁点

                if (charflags.containsAll(EnumSet.of(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN, PvLayout.CHARITEM_F.CHARITEM_F_HANDAKUTEN))) {
                    if (dakuten_type.ordinal() < DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE.ordinal())
                        // そのまま
                        c = charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN) ? '゛' : '゜';
                    else
                        // 結合文字
                        c = charflags.contains(PvLayout.CHARITEM_F.CHARITEM_F_DAKUTEN) ? 0x3099 : 0x309a;

                    // 位置
                    n1 = n2 = 0;

                    if (dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_NORMAL_HORZ
                            || dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE_HORZ)
                        n1 = fonth[fontno];
                    else if (dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_NORMAL_VERT
                            || dakuten_type == DefStyle.STYLE_DAKUTEN.STYLE_DAKUTEN_COMBINE_VERT) {
                        n2 = -fonth[fontno];
                        flags |= FONT_DRAW_F_DAKUTEN_VERT;
                    }

                    img.setFont(font[fontno]);
                    img.setColor(col);
                    img.drawString(String.valueOf(c), x + n1, y + n2); // flags
                }
            }

            // 傍点

            if (pi.bouten != 0) {
                img.setFont(p.font_kenten);
                img.setColor(col);
                img.drawString(String.valueOf(g_bouten_char[pi.bouten - 1]), x + fonth[fontno], y + (pi.height - p.fontkenten_h) / 2);
            }
        }
    }

    private static void drawStringV(Graphics2D img, String code, int x, int y) {
    }

    /* ルビ描画 */
    private static void _draw_ruby(LayoutWork p, int xtop, int ytop, int pagepos) {
        RubyItem pi;
        CharItem pichar;
        Font font;
        int x, y, addx, rlen, pad, padtop, pad2 = 0, n, overh, last_x, last_y;
        Color col;

        // 文字幅分を加算
        xtop += p.fontmain_h;

        font = p.font_ruby;
        col = p.stdef.col_ruby;
        overh = p.fontruby_h / 2;
        last_x = last_y = -1;

        for (int i = 0; i < p.list_ruby.size(); i++) {
            pi = p.list_ruby.get(i);
            // 次ページへの折り返し
            if (pi.x < 0) break;

            x = pi.x;
            y = pi.y;
            rlen = pi.rubylen;

            // 前のルビが折り返し後、現在のルビ先頭を超えている場合は、続きから

            if (x == last_x && y < last_y)
                y = last_y;

            // 親文字列の先頭
            pichar = pi.char_top;

            // 傍線がある場合は x+2
            addx = pichar.bousen != DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE ? 2 : 0;

            // ルビ余白
            pad = pi.char_h - pi.ruby_h;

            if (pi.char_h == RUBYITEM_CHARH_NO_PADDING) {
                // 親文字列先頭から、余白なし
                padtop = 0;
                pad2 = 0;
            } else if (rlen == 1)
                // ルビが1文字の場合
                padtop = pad / 2;
            else {
                // ルビが2文字以上の場合
                padtop = pad / (rlen * 2);
                pad2 = pad - padtop * 2;
                pad = 0;
            }

            // 各ルビ文字
            //  :基本的に親文字列に合わせて描画するが、ルビの上端が親文字列の範囲内にあれば、
            //  :下ははみ出す場合もある。
            //  :また、ルビのない文字にかかった場合も、以降の本文文字は位置合わせの対象となる。
            for (i = 0; i < rlen; i++) {
                // 余白追加
                if (i == 0)
                    y += padtop;
                else if (pad2 != 0) {
                    // ルビ2文字以上の場合、一定間隔の値では、ルビ数が多いと等間隔にならないため、
                    // 位置に応じて余白幅を計算。
                    // pad = 前回の余白位置, n = 現在のルビの余白位置
                    n = (int) ((double) i / (rlen - 1) * pad2 + 0.5);
                    y += n - pad;
                    pad = n;
                }

                // 現在の親文字を超える場合、次の親文字へ
                //  : 最後のルビの場合は、親の下端 -1 px の位置でも描画
                //  : それ以外は、ルビの半分の高さまではみ出し可能
                n = (i == rlen - 1) ? 0 : overh;

                if (pichar != null && y >= pichar.y + pichar.height - n) {

                    for (int j = i + 1; j < p.list_ruby.size(); j++) {
                        pichar = p.list_char.get(j);
                        if (pichar.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)
                                || y <= pichar.y + pichar.height - pichar.height / 4)
                            break;
                    }

                    // 次が折り返しの場合、親文字に合わせてルビも折り返し
                    if (pichar != null && pichar.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP)) {
                        x -= p.line_width;
                        y = p.jisage_wrap_y;

                        // 見開き右ページの終端行の場合、左ページ位置へ

                        if (pagepos == 0 && x < 0)
                            x -= p.stdef.page_space;
                    }
                }

                // ルビ描画
                //  前ページからの折り返しの文字が存在する場合があるため、
                //  x の範囲は1文字ずつ判定する (x は本文文字の位置)
                if (x >= 0 && x < p.pageW) {
                    p.img.setFont(font);
                    p.img.setColor(col);
                    drawStringV(p.img, pi.rubytxt + i, x + xtop + addx, y + ytop); // FONT_DRAW_F_RUBY
                    y += 1;
                } else {
                    // 前ページからの折り返しの位置の場合、高さのみ加算
                    y += mFontGetVertHeight(p.img, font, pi.rubytxt + i).height; // FONT_DRAW_F_RUBY
                }
            }

            last_x = x;
            last_y = y;
        }
    }

    /** 傍線を描画 */
    private static void _draw_bousen(LayoutWork p, int xtop, int ytop) {
        CharItem pi = null, top, next;
        int h, x, y, charspace;
        DefStyle.BOUSEN_TYPE type;
        Color col;

        col = p.pixcol_text;

        charspace = p.stdef.char_space;

        for (int i = 0; i < p.list_char.size(); i++) {
            top = p.list_char.get(i);
            // 傍線開始位置

            int j;
            for (j = i; j < p.list_char.size(); j++) {
                pi = p.list_char.get(j);
                if (pi.bousen != DefStyle.BOUSEN_TYPE.BOUSEN_TYPE_NONE) {
                    break;
                }
            }

            if (j == p.list_char.size()) break;

            // 傍線終了まで描画

            x = pi.x;
            y = pi.y;
            h = 0;
            type = pi.bousen;

            for (; pi != null && pi.bousen == type; pi = next) {
                next = p.list_char.listIterator().next();

                h += pi.height;

                if (next == null || (next.bousen != type || next.flags.contains(PvLayout.CHARITEM_F.CHARITEM_F_WRAP_TOP))) {
                    // 傍線の終端、または描画上の1行の終端時にまとめて描画

                    if (x >= 0 && x < p.pageW) {
                        _draw_pixbuf_bousen(p.img, x + p.fontmain_h + xtop, y + ytop, h, col, type);
                    }

                    if (next != null) {
                        x = next.x;
                        y = next.y;
                    }

                    h = 0;
                } else
                    h += charspace;
            }
        }
    }

    /** 1行分を描画 */
    static void layout_draw_line(LayoutWork p, int pagepos) {
        int xtop, ytop;

        xtop = p.stdef.margin.left;
        ytop = p.stdef.margin.top;

        // 見開きで右ページの場合
        if (pagepos == 0)
            xtop += p.stdef.page_space + p.pageW;

        // 本文
        _draw_text(p, xtop, ytop);

        // 傍線
        _draw_bousen(p, xtop, ytop);

        // ルビ
        _draw_ruby(p, xtop, ytop, pagepos);
    }


    /**
     * ページ情報を描画
     * <p>
     * pos: [-1] 単ページ [0] 右側 [1] 左側
     */
    static void layout_draw_pageinfo(LayoutWork p, int pageno, int pos) {
        String m;
        int x;

        if (pos == -1 || pos == 0)
            // 右側
            m = String.format("%d/%d", pageno + 1, p.pagenum);
        else
            // 左
            m = String.format("%d", pageno + 1);

        //

        if (pos == -1 || pos == 1)
            // 単ページまたは左側
            x = 4;
        else {
            // 見開きの右側

            x = p.stdef.margin.left + p.stdef.margin.right
                    + p.pageW * 2 + p.stdef.page_space
                    - 4 - mFontGetVertHeight(p.img, p.style.font_info, m).width;
        }

        p.img.setFont(p.style.font_info);
        p.img.setColor(p.stdef.col_info);
        p.img.drawString(m, x, 4);
    }

    /** 画像読み込み */
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
            open = Files.newInputStream(Paths.get(fname));
        }

        img = ImageIO.read(open);

        return img;
    }

    /** 挿絵描画 */
    static void layout_draw_picture(LayoutWork p, int pagepos) throws IOException {
        String str;
        BufferedImage img;
        Rectangle box = new Rectangle();
        int x, y;

        // ファイル名
        if (gdat.is_file_zip) {
            // ZIP
            str = String.valueOf(p.pagestate.picture + 2);
        } else {
            // 通常ファイル:テキストファイルのパスを基点とする
            str = gdat.strFileName;
            str += String.valueOf(p.pagestate.picture + 2);
        }

        // 読み込み
        img = _load_image(str, gdat.is_file_zip);

        // 位置・サイズ
        box.x = box.y = 0;
        box.width = img.getWidth();
        box.height = img.getHeight();

        mBoxResize_keepaspect(box, p.pageW, p.pageH, true);

        x = p.stdef.margin.left + box.x;
        y = p.stdef.margin.right + box.y;

        if (pagepos == 0)
            x += p.stdef.page_space + p.pageW;

        // 描画
        p.img.drawImage(img, x, y, box.width, box.height, null);
    }

    private static void mBoxResize_keepaspect(Rectangle box, int pageW, int pageH, boolean b) {
        double sx, sy, s;

        if (box.width == 0 || box.height == 0) return;

        if (!b) {
            // 拡大しない

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