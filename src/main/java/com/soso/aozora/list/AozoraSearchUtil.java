/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraWork;


class AozoraSearchUtil {

    private AozoraSearchUtil() {
    }

    static boolean searchAuthor(AozoraAuthor author, String search) {
        return indexOf(author.getID(), search) ||
               indexOf(author.getName(), search) ||
               indexOf(author.getKana(), search) ||
               indexOf(author.getRomanName(), search) ||
               indexOf(author.getNote(), search) ||
               indexOf(author.getBirthDate(), search) ||
               indexOf(author.getDeadDate(), search);
    }

    static boolean searchWork(AozoraWork work, String search) {
        return indexOf(work.getID(), search) ||
               indexOf(work.getTitleName(), search) ||
               indexOf(work.getTitleKana(), search) ||
               indexOf(work.getTitleOriginal(), search) ||
               indexOf(work.getNote(), search) ||
               indexOf(work.getOrginalBook(), search) ||
               indexOf(work.getPublisher(), search) ||
               indexOf(work.getFirstDate(), search) ||
               indexOf(work.getCompleteOrginalBook(), search) ||
               indexOf(work.getCompleteFirstDate(), search) ||
               indexOf(work.getCompletePublisher(), search) ||
               indexOf(work.getContentURL(), search) ||
               indexOf(work.getInputBase(), search) ||
               indexOf(work.getKanaType(), search) ||
               indexOf(work.getAuthorID(), search) ||
               indexOf(work.getTranslatorID(), search) ||
               indexOf(work.getMetaURL(), search) ||
               indexOf(work.getProofBase(), search);
    }

    private static boolean indexOf(String src, String search) {
        if (src != null)
            return src.indexOf(search) != -1;
        else
            return false;
    }
}
