/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.data.AozoraAuthor;
import com.soso.aozora.data.AozoraAuthorParser;
import com.soso.aozora.data.AozoraAuthorParserHandler;
import com.soso.aozora.data.AozoraWork;
import com.soso.aozora.data.AozoraWorkParser;
import com.soso.aozora.data.AozoraWorkParserHandler;


class AozoraListLoader implements Runnable, AozoraAuthorParserHandler, AozoraWorkParserHandler {
    private static class WorkLoadQueueElement {

        AozoraAuthorNode getAuthorNode() {
            return authorNode;
        }

        void callback() {
            if (callback != null)
                SwingUtilities.invokeLater(callback);
        }

        private final AozoraAuthorNode authorNode;

        private final Runnable callback;

        WorkLoadQueueElement(AozoraAuthorNode authorNode, Runnable callback) {
            this.authorNode = authorNode;
            this.callback = callback;
        }
    }

    AozoraListLoader(AozoraListMediator mediator) {
        isAuthorLoaded = false;
        isStop = false;
        this.mediator = mediator;
    }

    public void run() {
        loadAuthor();
        while (!isStop) {
            WorkLoadQueueElement element = null;
            synchronized (workLoadQueue) {
                element = workLoadQueue.poll();
                if (element == null)
                    try {
                        workLoadQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            if (element != null) {
                loadWorksImmediate(element.getAuthorNode());
                syncEventDispatched();
                element.callback();
            }
        }
    }

    void stop() {
        isStop = true;
        synchronized (workLoadQueue) {
            workLoadQueue.notify();
        }
    }

    private void loadAuthor() {
        try {
            try (Reader authorsIn = new InputStreamReader(AozoraUtil.getInputStream(AozoraEnv.getAuthorListURL()), StandardCharsets.UTF_8)) {
                AozoraAuthorParser.parse(authorsIn, this);
                mediator.setAuthorLoaded(true);
            }
            mediator.setSearchEnabled(true);
            synchronized (this) {
                isAuthorLoaded = true;

                for (Runnable callback : authorLoadCallback) {
                    syncEventDispatched();
                    callback.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncEventDispatched() {
        if (!SwingUtilities.isEventDispatchThread())
            try {
                SwingUtilities.invokeAndWait(() -> {
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    void loadAuthorsAsynchronous(Runnable callback) {
        synchronized (this) {
            if (isAuthorLoaded)
                callback.run();
            else
                authorLoadCallback.add(callback);
        }
    }

    void loadWorksAsynchronous(AozoraAuthorNode authorNode, Runnable callback) {
        synchronized (workLoadQueue) {
            WorkLoadQueueElement element = new WorkLoadQueueElement(authorNode, callback);
            workLoadQueue.add(element);
            workLoadQueue.notify();
        }
    }

    void loadWorksImmediate(AozoraAuthorNode authorNode) {
        String authorID = authorNode.getAozoraAuthor().getID();
        if (!authorNode.isWorkLoaded()) {
            try (Reader worksIn = new InputStreamReader(AozoraUtil.getInputStream(AozoraEnv.getWorkURL(authorID)), StandardCharsets.UTF_8)) {
                AozoraWorkParser.parse(worksIn, this);
                authorNode.setWorkLoaded(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void author(AozoraAuthor author) {
        if (isStop)
            throw new IllegalStateException(new InterruptedException("stopped by user"));
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                final AozoraAuthor theAuthor = author;
                SwingUtilities.invokeAndWait(() -> author(theAuthor));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (author == null)
            throw new IllegalArgumentException("author null");
        String authorID = author.getID();
        if (authorID == null || authorID.length() == 0)
            throw new IllegalArgumentException("no authorID:" + author);
        AozoraAuthorNode authorNode = mediator.getAozoraAuthorNode(authorID);
        if (authorNode == null)
            mediator.addAozoraAuthorNode(new AozoraAuthorNode(author));
        else
            authorNode.fireAuthorChange(author);
    }

    public void work(AozoraWork work) {
        if (isStop)
            throw new IllegalStateException(new InterruptedException("stoped by user"));
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                final AozoraWork theWork = work;
                SwingUtilities.invokeAndWait(() -> work(theWork));
                Thread.sleep(10L);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (work == null)
            throw new IllegalArgumentException("work null");
        String workID = work.getID();
        if (workID == null || workID.length() == 0)
            throw new IllegalArgumentException("no workID:" + work);
        String authorID = work.getAuthorID();
        if (authorID != null && authorID.length() != 0) {
            AozoraAuthorNode authorNode = mediator.getAozoraAuthorNode(authorID);
            if (authorNode != null) {
                AozoraWorkNode workNode = authorNode.getAozoraWorkNode(workID);
                if (workNode == null)
                    authorNode.add(new AozoraWorkNode(work));
                else
                    workNode.fireWorkChange(work);
            }
        }
        String translatorID = work.getTranslatorID();
        if (translatorID != null && translatorID.length() != 0) {
            AozoraAuthorNode translatorNode = mediator.getAozoraAuthorNode(translatorID);
            if (translatorNode != null) {
                AozoraWorkNode workNode = translatorNode.getAozoraWorkNode(workID);
                if (workNode == null)
                    translatorNode.add(new AozoraWorkNode(work));
                else
                    workNode.fireWorkChange(work);
            }
        }
    }

    private final AozoraListMediator mediator;
    private boolean isAuthorLoaded;
    private final Queue<WorkLoadQueueElement> workLoadQueue = new ConcurrentLinkedQueue<>();
    private final List<Runnable> authorLoadCallback = new ArrayList<>();
    private boolean isStop;
}
