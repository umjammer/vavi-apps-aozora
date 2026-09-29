[![Release](https://jitpack.io/v/umjammer/vavi-apps-aozora.svg)](https://jitpack.io/#umjammer/vavi-apps-aozora)
[![Java CI](https://github.com/umjammer/vavi-apps-aozora/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-aozora/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-aozora/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-aozora/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-apps-aozora

Japanese Vertical (縦書き) Text Viewer.

<img alt="ss" src="https://user-images.githubusercontent.com/493908/201278429-2dbb7ec9-a4fb-4c43-bf9a-6c68f9f44c03.png" width="320" />

## Install

 * [maven](https://jitpack.io/#umjammer/vavi-apps-aozora)

## Usage

 * [sample](src/test/java/vavi/text/aozora/viewer/MyTextViewerPaneTest.java)

## References

 * https://github.com/weimingtom/AozoraParser
 * https://github.com/iWumboUWumbo2/AozoraToHTML
 * http://x0213.org/codetable/
 * https://www.kabipan.com/computer/mobi/aozora_kanji.html
 * https://github.com/aozorahack/aozora-parser.js (peg syntax)
    * peg
        * ~~https://github.com/adammurdoch/java-peg-tools~~ (buggy, wip?)
        * https://github.com/sirthias/parboiled2 (scala, dsl on scala?)
        * https://github.com/jeronimonunes/PEG (use pegjs by nashorn) 🎯
 * https://github.com/taizan/vjap
 * https://www.aozora.gr.jp/aozora-manual/index-input.html

## TODO

 * ~~ruby~~
 * proofreading
   * https://qiita.com/kaz-utashiro/items/2f199409bdb1e08dc473
 * ~~proportional western words still has monospaced length, so there are unnatural spacing before/after the words~~
 * ~~`MyTextViewerPane` reports "full '<<', '>>' are not rotated". where?~~
 * ~~`Ⅰ` `Ⅱ` `Ⅲ` is rotated, maybe those are detected as western char, is this defined in w3c specs?~~
 * ~~aozora directives [4-2 見出し, 4-3 字下げ, 4-4 改ページ, 4-10 画像](https://www.aozora.gr.jp/aozora-manual/index-input.html)~~
 * ~~aozora directives 4-5 ページの左右中央, 4-8 文字サイズ・太字・斜体~~
 * aozora directives 4-9 表組み・罫囲み, 4-11 その他
