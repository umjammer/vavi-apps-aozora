/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.text.aozora.site;

import java.io.Serializable;

import org.klab.commons.csv.CsvColumn;
import org.klab.commons.csv.CsvEntity;


/**
 * AozoraData.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-27 nsano initial version <br>
 */
@CsvEntity(hasTitle = true)
public class AozoraData implements Serializable {

    /** 人物ID */
    @CsvColumn(sequence = 1)
    String authorId;
    /** 著者名 */
    @CsvColumn(sequence = 2)
    String authorName;
    /** 作品ID */
    @CsvColumn(sequence = 3)
    String titleId;
    /** 作品名 */
    @CsvColumn(sequence = 4)
    String title;
    /** 仮名遣い種別 */
    @CsvColumn(sequence = 5)
    String kanaType;
    /** 翻訳者名等 */
    @CsvColumn(sequence = 6)
    String translator;
    /** 入力者名 */
    @CsvColumn(sequence = 7)
    String editor;
    /** 校正者名 */
    @CsvColumn(sequence = 8)
    String proofreading;
    /** 状態 */
    @CsvColumn(sequence = 9)
    String status;
    /** 状態の開始日 */
    @CsvColumn(sequence = 10)
    String date;
    /** 底本名 */
    @CsvColumn(sequence = 11)
    String original;
    /** 出版社名 */
    @CsvColumn(sequence = 12)
    String publisher;
    /** 入力に使用した版 */
    @CsvColumn(sequence = 13)
    String targetRevision;
    /** 校正に使用した版 */
    @CsvColumn(sequence = 14)
    String proofreadingRevision;

    @Override
    public String toString() {
        return "AozoraData{" +
                "authorId='" + authorId + '\'' +
                ", authorName='" + authorName + '\'' +
                ", titleId='" + titleId + '\'' +
                ", title='" + title + '\'' +
                ", kanaType='" + kanaType + '\'' +
                ", translator='" + translator + '\'' +
                ", editor='" + editor + '\'' +
                ", proofreading='" + proofreading + '\'' +
                ", status='" + status + '\'' +
                ", date='" + date + '\'' +
                ", original='" + original + '\'' +
                ", publisher='" + publisher + '\'' +
                ", targetRevision='" + targetRevision + '\'' +
                ", proofreadingRevision='" + proofreadingRevision + '\'' +
                '}';
    }
}
