/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.list;


class AozoraRankingEntry implements Comparable<AozoraRankingEntry> {

    AozoraRankingEntry(String id, int count) {
        this.id = id;
        this.count = count;
    }

    String getID() {
        return id;
    }

    int getCount() {
        return count;
    }

    public int compareTo(AozoraRankingEntry another) {
        if (getCount() < another.getCount())
            return 1;
        if (getCount() > another.getCount())
            return -1;
        else
            return getID().compareTo(another.getID());
    }

    private final String id;
    private final int count;
}
