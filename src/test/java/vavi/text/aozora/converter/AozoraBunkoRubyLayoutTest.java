/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.converter;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * The layout annotations of the aozora text, 4-2 to 4-4 and 4-10 of
 * https://www.aozora.gr.jp/aozora-manual/index-input.html
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-09-29 nsano initial version <br>
 */
class AozoraBunkoRubyLayoutTest {

    static String parse(String text) {
        AozoraBunkoRuby converter = new AozoraBunkoRuby();
        converter.readText(new StringReader(text));
        return converter.parse();
    }

    @Test
    void testHeading() {
        String html = parse("""
                ［＃２字下げ］上　先生と私［＃「上　先生と私」は大見出し］
                ［＃５字下げ］一［＃「一」は中見出し］
                独《ひと》り寝《ね》の別《わか》れ［＃「独り寝の別れ」は小見出し］
                ［＃中見出し］亜細亜《アジア》の曙《あけぼの》［＃中見出し終わり］
                青空文庫《あおぞらぶんこ》［＃「青空文庫」は同行中見出し］　本文
                """);
        System.out.println(html);
        assertTrue(html.contains("<div class=\"jisage_2\" style=\"margin-left: 2em\"><h3 class=\"o-midashi\">上　先生と私</h3></div>\n"));
        assertTrue(html.contains("<div class=\"jisage_5\" style=\"margin-left: 5em\"><h4 class=\"naka-midashi\">一</h4></div>\n"));
        assertTrue(html.contains("<h5 class=\"ko-midashi\"><ruby><rb>独</rb><rt>ひと</rt></ruby>り<ruby><rb>寝</rb><rt>ね</rt></ruby>の<ruby><rb>別</rb><rt>わか</rt></ruby>れ</h5>\n"));
        assertTrue(html.contains("<h4 class=\"naka-midashi\"><ruby><rb>亜細亜</rb><rt>アジア</rt></ruby>の<ruby><rb>曙</rb><rt>あけぼの</rt></ruby></h4>\n"));
        assertTrue(html.contains("<h4 class=\"dogyo-naka-midashi\"><ruby><rb>青空文庫</rb><rt>あおぞらぶんこ</rt></ruby></h4>　本文<br/>\n"));
        assertFalse(html.contains("［＃"));
    }

    @Test
    void testIndent() {
        String html = parse("""
                …ここでもっと大事なのは論述のスタイルである。
                ［＃３字下げ］灰いろの抽象の世に住まんには濃きに過ぎたる煩悩の色
                ［＃ここから５字下げ］
                白は大正七年一月十四日の夜半病死し、
                　　大正十二年二月九日追記
                ［＃ここで字下げ終わり］
                ［＃ここから２字下げ、折り返して３字下げ］
                年代　「記事」（出典）
                ［＃ここから改行天付き、折り返して１字下げ］
                天付き
                ［＃ここで字下げ終わり］
                ［＃地付き］（この日記終り）
                ［＃地から２字上げ］長谷川辰之助
                　二人はそれぎり黙って風呂へはいった。［＃地付き］（掲載誌不詳）
                ［＃ここから地から１字上げ］
                以上
                ［＃ここで字上げ終わり］
                ［＃ここから５字下げ］
                北海若曰く
                ［＃ここで字下げ終わり］［＃地から７字上げ］『荘子』第十七篇「秋水」
                """);
        System.out.println(html);
        assertTrue(html.contains("<div class=\"jisage_3\" style=\"margin-left: 3em\">灰いろの抽象の世に住まんには濃きに過ぎたる煩悩の色</div>\n"));
        assertTrue(html.contains("<div class=\"jisage_5\" style=\"margin-left: 5em\">\n白は大正七年一月十四日の夜半病死し、<br/>\n　　大正十二年二月九日追記<br/>\n</div>\n"));
        assertTrue(html.contains("<div class=\"burasage\" style=\"margin-left: 3em; text-indent: -1em;\">\n年代　「記事」（出典）<br/>\n</div>\n"));
        assertTrue(html.contains("<div class=\"burasage\" style=\"margin-left: 1em; text-indent: -1em;\">\n天付き<br/>\n</div>\n"));
        assertTrue(html.contains("<div class=\"chitsuki_0\" style=\"text-align:right; margin-right: 0em\">（この日記終り）</div>\n"));
        assertTrue(html.contains("<div class=\"chitsuki_2\" style=\"text-align:right; margin-right: 2em\">長谷川辰之助</div>\n"));
        assertTrue(html.contains("　二人はそれぎり黙って風呂へはいった。<span class=\"chitsuki_0\" style=\"text-align:right; margin-right: 0em\">（掲載誌不詳）</span><br/>\n"));
        assertTrue(html.contains("<div class=\"chitsuki_1\" style=\"text-align:right; margin-right: 1em\">\n以上<br/>\n</div>"));
        assertTrue(html.contains("<div class=\"jisage_5\" style=\"margin-left: 5em\">\n北海若曰く<br/>\n</div>\n<div class=\"chitsuki_7\" style=\"text-align:right; margin-right: 7em\">『荘子』第十七篇「秋水」</div>"));
        assertFalse(html.contains("［＃"));
    }

    @Test
    void testPageBreak() {
        String html = parse("""
                あ
                ［＃改丁］
                い
                ［＃改ページ］
                う
                ［＃改見開き］
                え
                ［＃改段］
                お
                """);
        System.out.println(html);
        for (String pageBreak : new String[] {"［＃改丁］", "［＃改ページ］", "［＃改見開き］", "［＃改段］"})
            assertTrue(html.contains("<span class=\"notes\">" + pageBreak + "</span><br/>\n"), pageBreak);
    }

    @Test
    void testIllustration() {
        String html = parse("""
                ［＃石鏃二つの図（fig42154_01.png、横321×縦123）入る］
                ［＃「大獅子金剛大ラマの水刑」のキャプション付きの図（fig49966_02.png、横353×縦514）入る］
                大獅子金剛大ラマの水刑［＃「大獅子金剛大ラマの水刑」はキャプション］
                ［＃挿絵（fig1_01.png）入る］
                ［＃ここからキャプション］
                第３図
                　本図は
                ［＃ここでキャプション終わり］
                """);
        System.out.println(html);
        assertTrue(html.contains("<img class=\"illustration\" width=\"321\" height=\"123\" src=\"fig42154_01.png\" alt=\"石鏃二つの図\" /><br/>\n"));
        assertTrue(html.contains("<img class=\"illustration\" width=\"353\" height=\"514\" src=\"fig49966_02.png\" alt=\"「大獅子金剛大ラマの水刑」のキャプション付きの図\" /><br/>\n"));
        assertTrue(html.contains("<span class=\"caption\">大獅子金剛大ラマの水刑</span><br/>\n"));
        assertTrue(html.contains("<img class=\"illustration\" src=\"fig1_01.png\" alt=\"挿絵\" /><br/>\n"));
        assertTrue(html.contains("<div class=\"caption\">\n第３図<br/>\n　本図は<br/>\n</div>"));
        assertFalse(html.contains("［＃"));
    }

    @Test
    void testPageCenter() {
        String html = parse("""
                ［＃改ページ］
                ［＃ページの左右中央］


                ［＃３字下げ］短章　その一［＃「短章　その一」は中見出し］


                ［＃改ページ］
                """);
        System.out.println(html);
        assertTrue(html.contains("<span class=\"notes\">［＃改ページ］</span><br/>\n<span class=\"notes\">［＃ページの左右中央］</span><br/>\n<br/>\n<br/>\n<div class=\"jisage_3\""));
    }

    @Test
    void testStyle() {
        String html = parse("""
                ［＃３字下げ］県立高女の怪事［＃「県立高女の怪事」は２段階大きな文字］
                青空［＃「青空」は１段階小さな文字］と青空《あおぞら》［＃「青空」は５段階大きな文字］
                ［＃１段階大きな文字］青空文庫であって責空文庫ではない。［＃大きな文字終わり］
                ［＃ここから１段階小さな文字］
                ★東北大学の
                ［＃ここで小さな文字終わり］
                「クリス、宇宙航行委員会が選考［＃「選考」は太字］するんだ。［＃太字］待つんだ［＃太字終わり］」
                ［＃ここから太字］
                ★東北大学の
                ［＃ここで太字終わり］
                Nothing from nothing ever yet was born.［＃「Nothing from nothing ever yet was born.」は斜体］
                「［＃斜体］クリス［＃斜体終わり］」
                ［＃ここから斜体］
                ★東北大学の
                ［＃ここで斜体終わり］
                """);
        System.out.println(html);
        assertTrue(html.contains("<div class=\"jisage_3\" style=\"margin-left: 3em\"><span class=\"dai2\" style=\"font-size: x-large;\">県立高女の怪事</span></div>\n"));
        assertTrue(html.contains("<span class=\"sho1\" style=\"font-size: small;\">青空</span>と<span class=\"dai5\" style=\"font-size: xx-large;\"><ruby><rb>青空</rb><rt>あおぞら</rt></ruby></span><br/>\n"));
        assertTrue(html.contains("<span class=\"dai1\" style=\"font-size: large;\">青空文庫であって責空文庫ではない。</span><br/>\n"));
        assertTrue(html.contains("<div class=\"sho1\" style=\"font-size: small;\">\n★東北大学の<br/>\n</div>\n"));
        assertTrue(html.contains("「クリス、宇宙航行委員会が<span class=\"futoji\">選考</span>するんだ。<span class=\"futoji\">待つんだ</span>」<br/>\n"));
        assertTrue(html.contains("<div class=\"futoji\">\n★東北大学の<br/>\n</div>\n"));
        assertTrue(html.contains("<span class=\"shatai\">Nothing from nothing ever yet was born.</span><br/>\n"));
        assertTrue(html.contains("「<span class=\"shatai\">クリス</span>」<br/>\n"));
        assertTrue(html.contains("<div class=\"shatai\">\n★東北大学の<br/>\n</div>"));
        assertFalse(html.contains("［＃"));
    }

    @Test
    void testToInt() {
        assertEquals(3, AozoraBunkoRuby.toInt("３"));
        assertEquals(12, AozoraBunkoRuby.toInt("１２"));
        assertEquals(12, AozoraBunkoRuby.toInt("十二"));
        assertEquals(20, AozoraBunkoRuby.toInt("二十"));
    }
}
