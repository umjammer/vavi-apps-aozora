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


/**
 * スタイルデータ定義
 */
class DefStyle {

    static final int STYLE_CHARS_DEFAULT = 1;

    public DefStyle(String txt) {
    }

    enum DATATYPE {
        /** 終端 */
        DATATYPE_END,
        /** 文章としての改行 */
        DATATYPE_ENTER,
        /** ソースの行番号値 [(uint32)行番号] */
        DATATYPE_LINEINFO,
        /** 通常文字列(16bit) [(uint16)文字数, 16bit文字列:ヌル含まない] */
        DATATYPE_NORMAL_TEXT_16,
        /** 通常文字列(32bit) [(uint16)文字数, 32bit文字列] */
        DATATYPE_NORMAL_TEXT_32,
        /** ルビ付き文字列 [(uint16)親文字列の文字数, UTF-32親文字列, (uint16)ルビ文字数, UTF-32ルビ文字列] */
        DATATYPE_RUBY_TEXT,
        /** 注記コマンド [(uint8)コマンドタイプ] */
        DATATYPE_COMMAND,
        /** 注記コマンド [(uint8)コマンドタイプ, (uint8x2)値] */
        DATATYPE_COMMAND_VAL,
        /** 挿絵 [(uint16)文字列バイト数:ヌル含む, UTF-8ファイル名文字列:ヌル含む] */
        DATATYPE_PICTURE,
        /** 変換作業時用 */
        DATATYPE_CHAR
    }

    /** 濁点タイプ */
    enum STYLE_DAKUTEN {
        /** そのまま */
        STYLE_DAKUTEN_NORMAL,
        /** そのまま:横に全角幅ずらす */
        STYLE_DAKUTEN_NORMAL_HORZ,
        /** そのまま:縦に全角幅ずらす */
        STYLE_DAKUTEN_NORMAL_VERT,
        /** 結合文字 */
        STYLE_DAKUTEN_COMBINE,
        /** 結合文字:横に全角幅 */
        STYLE_DAKUTEN_COMBINE_HORZ,
        /** 結合文字:縦に全角幅 */
        STYLE_DAKUTEN_COMBINE_VERT
    }

    /** フラグ */
    enum STYLE_FLAGS {
        /** 背景画像はタイル状に並べる */
        STYLE_F_BKGND_TILE(1 << 0),
        /** ぶら下げ有効 */
        STYLE_F_HANGING(1 << 1),
        /** 挿絵表示 */
        STYLE_F_ENABLE_PICTURE(1 << 2),
        /** 横線文字を直線で描画 */
        STYLE_F_DASH_TO_LINE(1 << 3),
        /** 印刷標準字体に置換 */
        STYLE_F_REPLACE_PRINT(1 << 4);
        final int v;

        STYLE_FLAGS(int v) {
            this.v = v;
        }

        static EnumSet<STYLE_FLAGS> valueOf(int v) {
            EnumSet<STYLE_FLAGS> es = EnumSet.noneOf(STYLE_FLAGS.class);
            for (STYLE_FLAGS e : values())
                if ((e.v & v) != 0)
                    es.add(e);
            return es;
        }

        static int valueOf(EnumSet<STYLE_FLAGS> es) {
            int v = 0;
            for (STYLE_FLAGS e : values())
                if (es.contains(e))
                    v |= e.v;
            return v;
        }
    }

    enum BOUSEN_TYPE {
        BOUSEN_TYPE_NONE,
        /** 直線 */
        BOUSEN_TYPE_NORMAL,
        /** 二重線 */
        BOUSEN_TYPE_DOUBLE,
        /** 鎖線 */
        BOUSEN_TYPE_KUSARI,
        /** 破線 */
        BOUSEN_TYPE_HASEN,
        /** 波線 */
        BOUSEN_TYPE_NAMISEN,
    }

    enum BOUTEN_TYPE {
        BOUTEN_TYPE_NONE,
        /** ごま */
        BOUTEN_TYPE_GOMA,
        /** 白ごま */
        BOUTEN_TYPE_SIROGOMA,
        /** 蛇の目 */
        BOUTEN_TYPE_JYANOME,
        /** 丸 */
        BOUTEN_TYPE_MARU,
        /** 白丸 */
        BOUTEN_TYPE_SIROMARU,
        /** 黒三角 */
        BOUTEN_TYPE_KUROSANKAKU,
        /** 白三角 */
        BOUTEN_TYPE_SIROSANKAKU,
        /** 二重丸 */
        BOUTEN_TYPE_NIJUUMARU,
        /** バツ */
        BOUTEN_TYPE_BATU,
    }
}