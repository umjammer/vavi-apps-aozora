/*
 * http://www.35-35.net/aozora/
 */

package com.soso.sgui.text;

import java.util.Arrays;


public class CharacterUtil {
    
    /** f */
    private static final class LineTailForbidden {

        static boolean contains(char c) {
            return Arrays.binarySearch(table, c) >= 0;
        }

        private static final char[] table = {
            '(', '[', '{', '〈', '《', '「', '『', '【',
            '〔', '（', '［', '｛'
        };
    }

    /** j */
    private static final class LineHeadForbidden {

        static boolean contains(char c) {
            return Arrays.binarySearch(table, c) >= 0;
        }

        private static final char[] table = {
            ')', ',', '.', ']', '}', '、', '。', '〉',
            '》', '」', '』', '】', '〕', '）', '，', '．',
            '］', '｝'
        };
    }

    /** h */
    private static final class PunctuateKana {

        static boolean contains(char c) {
            return Arrays.binarySearch(table, c) >= 0;
        }

        private static final char[] table = {
            '、', '。', '，', '．'
        };
    }

    /** l */
    private static final class SmallKana {

        static boolean contains(char c) {
            return Arrays.binarySearch(table, c) >= 0;
        }

        private static final char[] table = {
            'ぁ', 'ぃ', 'ぅ', 'ぇ', 'ぉ', 'っ', 'ゃ', 'ゅ',
            'ょ', 'ァ', 'ィ', 'ゥ', 'ェ', 'ォ', 'ッ', 'ャ',
            'ュ', 'ョ'
        };
    }

    /** d */
    private static final class ToRotateLRMirrorKana {

        static boolean contains(char c) {
            return Arrays.binarySearch(table_a, c) >= 0 ||
                   Arrays.binarySearch(openParens, c) >= 0 ||
                   Arrays.binarySearch(closeParens, c) >= 0 ||
                   isKeisen(c) ||
                   isArrow(c);
        }

        static boolean b(char c) {
            return Arrays.binarySearch(table_d, c) >= 0;
        }

        private static boolean isKeisen(char c) {
            return '─' <= c && c <= '╿';
        }

        private static boolean isArrow(char c) {
            return '←' <= c && c <= '⇿';
        }

        static float getHrizontalAlign(char c) {
            if (Arrays.binarySearch(table_a, c) >= 0)
                return 0.5F;
            if (Arrays.binarySearch(openParens, c) >= 0)
                return 1.0F;
            if (Arrays.binarySearch(closeParens, c) >= 0)
                return 0.0F;
            if (isKeisen(c))
                return 0.5F;
            if (isArrow(c))
                return 0.5F;
            else
                throw new IllegalArgumentException("this is not KANA char to rotate.");
        }

        private static final char[] table_a = {
            '‐', '‑', '‒', '–', '—', '―', '‖', '‗',
            '․', '‥', '…', '⁓', '⁔', '⁗', '⁽', '⁾',
            '₍', '₎', '〈', '〉', '⎴', '⎵', '⎶', '❨',
            '❩', '❪', '❫', '❬', '❭', '❮', '❯', '❰',
            '❱', '❲', '❳', '❴', '❵', '⟦', '⟧', '⟨',
            '⟩', '⟪', '⟫', '⦃', '⦄', '⦅', '⦆', '⦇',
            '⦈', '⦉', '⦊', '⦋', '⦌', '⦍', '⦎', '⦏',
            '⦐', '⦑', '⦒', '⦓', '⦔', '⦕', '⦖', '⦗',
            '⦘', '⧘', '⧙', '⧚', '⧛', '⧼', '⧽', '〈',
            '〉', '《', '》', '【', '】', '〔', '〕',
            '〖', '〗', '〘', '〙', '〚', '〛', '〜',
            '〰', '〽', '゠', 'ー', '﴾', '﴿', '︱',
            '︲', '﹉', '﹊', '﹋', '﹌', '﹍', '﹎',
            '﹏', '﹘', '﹙', '﹚', '﹛', '﹜', '﹝', '﹞',
            '﹘', '﹣', '（', '）', '－', '［', '］', '｛',
            '｝', '～', '｟', '｠'
        };

        private static final char[] openParens = {
            '「', '『', '｢'
        };

        private static final char[] closeParens = {
            '」', '』', '｣'
        };

        private static final char[] table_d = {
            'ー'
        };
    }

    /** a */
    private static final class HalfWidth {

        static boolean contains(char c) {
            return Arrays.binarySearch(table, c) >= 0;
        }

        private static final char[] table = {
            '\u20A9', '｡', '｢', '｣', '､', '･', 'ｦ', 'ｧ',
            'ｨ', 'ｩ', 'ｪ', 'ｫ', 'ｬ', 'ｭ', 'ｮ', 'ｯ',
            'ｰ', 'ｱ', 'ｲ', 'ｳ', 'ｴ', 'ｵ', 'ｶ', 'ｷ',
            'ｸ', 'ｹ', 'ｺ', 'ｻ', 'ｼ', 'ｽ', 'ｾ', 'ｿ',
            'ﾀ', 'ﾁ', 'ﾂ', 'ﾃ', 'ﾄ', 'ﾅ', 'ﾆ', 'ﾇ',
            'ﾈ', 'ﾉ', 'ﾊ', 'ﾋ', 'ﾌ', 'ﾍ', 'ﾎ', 'ﾏ',
            'ﾐ', 'ﾑ', 'ﾒ', 'ﾓ', 'ﾔ', 'ﾕ', 'ﾖ', 'ﾗ',
            'ﾘ', 'ﾙ', 'ﾚ', 'ﾛ', 'ﾜ', 'ﾝ', 'ﾞ', 'ﾟ',
            '\uFFA0', '\uFFA1', '\uFFA2', '\uFFA3', '\uFFA4', '\uFFA5', '\uFFA6', '\uFFA7',
            '\uFFA8', '\uFFA9', '\uFFAA', '\uFFAB', '\uFFAC', '\uFFAD', '\uFFAE', '\uFFAF', 
            '\uFFB0', '\uFFB1', '\uFFB2', '\uFFB3', '\uFFB4', '\uFFB5', '\uFFB6', '\uFFB7',
            '\uFFB8', '\uFFB9', '\uFFBA', '\uFFBB', '\uFFBC', '\uFFBD', '\uFFBE', '\uFFC2',
            '\uFFC3', '\uFFC4', '\uFFC5', '\uFFC6', '\uFFC7', '\uFFCA', '\uFFCB', '\uFFCC',
            '\uFFCD', '\uFFCE', '\uFFCF', '\uFFD2', '\uFFD3', '\uFFD4', '\uFFD5', '\uFFD6',
            '\uFFD7', '\uFFDA', '\uFFDB', '\uFFDC', '\uFFE8', '￩', '￪', '￫',
            '￬', '￭', '￮'
        };
    }

    /** c */
    private static final class Narrow {

        static boolean contains(char c) {
            return Arrays.binarySearch(table, c) >= 0;
        }

        private static final char[] table = {
            ' ', '!', '"', '#', '$', '%', '&', '\'', '(',
            ')', '*', '+', ',', '-', '.', '/', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9', ':',
            ';', '<', '=', '>', '?', '@', 'A', 'B', 'C',
            'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^',
            '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
            'z', '{', '|', '}', '~', '\242', '\243',
            '\245', '\246', '\254', '\257', '⟦', '⟧', '⟨', '⟩', '⟪', '⟫'
        };
    }

    public CharacterUtil() {
    }

    public static boolean isTab(char c) {
        return c == '\t';
    }

    public static boolean isLineSeparator(char c) {
        return c == '\n' || c == '\r';
    }

    public static boolean isPageSeparator(char c) {
        return c == '\f';
    }

    public static boolean isNarrow(char c) {
        return Narrow.contains(c);
    }

    public static boolean isHalfWidth(char c) {
        return HalfWidth.contains(c);
    }

    public static boolean isToRotateKana(char c) {
        return ToRotateLRMirrorKana.contains(c);
    }

    public static boolean isToRotateLRMirrorKana(char c) {
        return ToRotateLRMirrorKana.b(c);
    }

    public static boolean isToRotate(char c) {
        return isNarrow(c) ||
               isHalfWidth(c) ||
               isToRotateKana(c);
    }

    public static boolean isPunctuateKana(char c) {
        return PunctuateKana.contains(c);
    }

    public static boolean isSmallKana(char c) {
        return SmallKana.contains(c);
    }

    public static float getHorizontalAlign(char c) {
        return ToRotateLRMirrorKana.getHrizontalAlign(c);
    }

    public static boolean isLineHeadForbidden(char c) {
        return LineHeadForbidden.contains(c) || isLineSeparator(c);
    }

    public static boolean isLineTailForbidden(char c) {
        return LineTailForbidden.contains(c);
    }
}
