/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;


public class AozoraComment implements Comparable<AozoraComment> {

    static AozoraComment newInstalce(long timestamp, String workID, int position, int length, String commentator, String data) {
        return new AozoraComment(timestamp, workID, position, length, commentator, data);
    }

    private AozoraComment(long timestamp, String workID, int position, int length, String commentator, String data) {
        this.timestamp = timestamp;
        this.workID = workID;
        this.position = position;
        this.length = length;
        this.commentator = commentator;
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getWorkID() {
        return workID;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public String getCommentator() {
        return commentator;
    }

    public String getData() {
        return data;
    }

    public int compareTo(AozoraComment another) {
        return Long.compare(getTimestamp(), another.getTimestamp());
    }

    public boolean equals(Object obj) {
        if (obj instanceof AozoraComment)
            return compareTo((AozoraComment) obj) == 0;
        else
            return super.equals(obj);
    }

    public String toString() {
        return super.toString() + "[" +
            "timestamp=" + getTimestamp() + "|" +
            "workID=" + getWorkID() + "|" +
            "position=" + getPosition() + "|" +
            "length=" + getLength() + "|" +
            "commentator=" + getCommentator() + "|" +
            "data=" + getData() + "]";
    }

    public static String NANASHISAN = "名無しさん";
    private final long timestamp;
    private final String workID;
    private final int position;
    private final int length;
    private final String commentator;
    private final String data;
}
