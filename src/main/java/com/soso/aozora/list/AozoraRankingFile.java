/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;

import java.net.URL;
import java.util.Date;


class AozoraRankingFile {

    AozoraRankingFile(URL url, Date start, Date end) {
        this.url = url;
        this.start = start;
        this.end = end;
    }

    URL getURL() {
        return url;
    }

    Date getStart() {
        return start;
    }

    Date getEnd() {
        return end;
    }

    private final URL url;
    private final Date start;
    private final Date end;
}
