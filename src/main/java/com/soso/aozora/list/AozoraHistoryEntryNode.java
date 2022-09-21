/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;

import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraHistories;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParserHandler;


class AozoraHistoryEntryNode extends DefaultMutableTreeNode implements AozoraWorkParserHandler, AozoraAuthorParserHandler {

    AozoraHistoryEntryNode(AozoraHistories.AozoraHistoryEntry entry) {
        super("ロード中...", false);
        this.entry = entry;
    }

    void reset() {
        author = null;
        work = null;
        setUserObject("ロード中...");
    }

    AozoraHistories.AozoraHistoryEntry getEntry() {
        return entry;
    }

    AozoraWork getWork() {
        return work;
    }

    AozoraAuthor getAuthor() {
        return author;
    }

    String getToolTipText() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("表示位置：").append(getEntry().getPosition()).append("　");
            sb.append("時刻：").append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(getEntry().getTimestamp())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.length() != 0 ? sb.toString() : null;
    }

    public void work(AozoraWork work) {
        this.work = work;
        if (work == null)
            setUserObject("作品情報が見つかりません - " + getEntry().getBook());
        else
            setUserObject(work.getTitleName());
    }

    public void author(AozoraAuthor author) {
        this.author = author;
        if (author == null)
            setUserObject("作者情報が見つかりません - " + getWork().getTitleName());
        else
            setUserObject(getWork().getTitleName() + " - " + author.getName());
    }

    private final AozoraHistories.AozoraHistoryEntry entry;
    private AozoraWork work;
    private AozoraAuthor author;
}
