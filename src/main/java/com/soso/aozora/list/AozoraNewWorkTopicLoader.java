/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.soso.aozora.boot.AozoraContext;


class AozoraNewWorkTopicLoader implements AozoraTopicLoader {

    AozoraNewWorkTopicLoader(AozoraContext context, String id, URL curIndexURL, URL oldIndexURL) {
        this.context = context;
        this.id = id;
        this.curIndexURL = curIndexURL;
        this.oldIndexURL = oldIndexURL;
    }

    String getID() {
        return id;
    }

    public AozoraTopicNode[] loadTopics() {
        List<AozoraTopicNode> topics = new ArrayList<AozoraTopicNode>();
        try {
            AozoraIndex oldIndex = AozoraIndex.getIndex(oldIndexURL);
            AozoraIndex curIndex = AozoraIndex.getIndex(curIndexURL);
            for (String curWorkID: curIndex.getWorkIDs()) {
                if (!oldIndex.isContainsWorkID(curWorkID))
                    topics.add(new AozoraNewWorkTopicNode(context, curWorkID));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return topics.toArray(new AozoraTopicNode[topics.size()]);
    }

    private final AozoraContext context;
    private final String id;
    private final URL curIndexURL;
    private final URL oldIndexURL;
}
