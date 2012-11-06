/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;


class AozoraNewWorkTopicNode extends AozoraTopicNode implements AozoraAuthorParserHandler, AozoraWorkParserHandler {

    AozoraNewWorkTopicNode(AozoraContext context, String workID) {
        super("ロード中...");
        this.context = context;
        context.getRootMediator().getAozoraWorkAsynchronous(workID, this);
    }

    void showTopic() {
        if (author != null && work != null)
            context.getRootMediator().showViewer(author, work);
    }

    public void work(AozoraWork work) {
        this.work = work;
        String personID = work.getAuthorID();
        if (personID == null || personID.length() == 0)
            personID = work.getTranslatorID();
        if (personID != null && personID.length() != 0) {
            context.getRootMediator().getAozoraAuthorAsynchronous(personID, this);
        } else {
            setUserObject(this.work.getTitleName() + " - 著者情報がありません");
            fireNodeChanged();
        }
    }

    public void author(AozoraAuthor author) {
        this.author = author;
        setUserObject(work.getTitleName() + " - " + this.author.getName());
        fireNodeChanged();
    }

    private final AozoraContext context;
    private AozoraAuthor author;
    private AozoraWork work;
}
