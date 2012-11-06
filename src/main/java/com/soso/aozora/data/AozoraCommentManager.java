/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.soso.aozora.boot.AozoraContext;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;
import com.soso.aozora.html.TagReader;
import com.soso.aozora.html.TagUtil;


public class AozoraCommentManager {

    public AozoraCommentManager(AozoraContext context) throws IOException {
        this.context = context;
        allCommentList = new ArrayList<AozoraComment>();
    }

    private AozoraContext getAzContext() {
        return context;
    }

    public Object getMutex() {
        return MUTEX;
    }

    public AozoraComment[] getComments(String workID) {
        List<AozoraComment> workCommentList = new ArrayList<AozoraComment>();
        synchronized (getMutex()) {
            for (AozoraComment comment : allCommentList) {
                if (comment.getWorkID().equals(workID))
                    workCommentList.add(comment);
            }
        }
        return workCommentList.toArray(new AozoraComment[workCommentList.size()]);
    }

    public AozoraComment getComment(int index) {
        synchronized (getMutex()) {
            return allCommentList.get(index);
        }
    }

    public int getCommentCount() {
        synchronized (getMutex()) {
            return allCommentList.size();
        }
    }

    public void updateComments() throws IOException {
        if (!getAzContext().getLineMode().isConnectable()) {
            getAzContext().log("オフラインモードではコメントを取得できません。");
            return;
        }
        if (getCommentCount() == 0) {
            URL[] commentURLs = getCommentURLs();
            for (int i = 0; i < commentURLs.length; i++)
                loadComments(commentURLs[i]);

        } else {
            long beginTimestamp = getComment(getCommentCount() - 1).getTimestamp();
            for (URL commentURL : getCommentURLs(beginTimestamp)) {
                loadComments(commentURL);
            }
        }
    }

    private void loadComments(URL commentURL) throws IOException {
        getAzContext().log("comment|" + commentURL + " をロードします。");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(AozoraUtil.getInputStream(commentURL), "UTF-8"));
            List<AozoraComment> tmpList = new ArrayList<AozoraComment>();
            String line;
            while ((line = in.readLine()) != null) {
                AozoraComment comment = parseLine(line);
                if (comment != null)
                    tmpList.add(comment);
            }
            List<AozoraComment> addedList = new ArrayList<AozoraComment>();
            synchronized (getMutex()) {
                for (AozoraComment comment : tmpList) {
                    if (!allCommentList.contains(comment)) {
                        allCommentList.add(comment);
                        addedList.add(comment);
                    }
                }
                if (addedList.size() != 0)
                    Collections.sort(allCommentList);
            }
            for (AozoraComment comment : addedList) {
                getAzContext().getListenerManager().commentAdded(comment);
            }

            getAzContext().log("comment|" + commentURL + " の " + tmpList.size() + " 件のうち " + addedList.size() + " 件が新着です。");
        } finally {
            if (in != null)
                in.close();
        }
    }

    private URL[] getCommentURLs() throws IOException {
        return getCommentURLs(0L);
    }

    private URL[] getCommentURLs(long beginTimestamp) throws IOException {
        List<URL> commentURLList = new ArrayList<URL>();
        TagReader tin = null;
        try {
            URL commentListURL = new URL(AozoraEnv.getCommentDataURL(), "?C=N;O=D");
            tin = new TagReader(new InputStreamReader(AozoraUtil.getInputStream(commentListURL), "UTF-8"));
            Pattern pattern = Pattern.compile("(comment\\.)(\\d+)(\\.txt)");
            String tag;
label0:     do {
                long timesec;
label1:         do {
                    String href;
                    Matcher matcher;
                    do {
                        if ((tag = tin.readNextTag()) == null)
                            break label1;
                        if (!tag.startsWith("a") || tag.indexOf("href") == -1)
                            continue label0;
                        href = TagUtil.getAttValue(tag, "href");
                        matcher = pattern.matcher(href);
                    } while (!matcher.matches());
                    commentURLList.add(new URL(commentListURL, href));
                    timesec = Long.parseLong(matcher.group(2));
                } while (timesec >= beginTimestamp / 1000L);
                break;
            } while (!tag.equals("/html"));
        } finally {
            if (tin != null)
                tin.close();
        }
        Collections.reverse(commentURLList);
        return commentURLList.toArray(new URL[commentURLList.size()]);
    }

    private AozoraComment parseLine(String line) {
        try {
            String[] columns = line.split("\t", 6);
            long timestamp = Long.parseLong(columns[0]);
            String workID = columns[1];
            int position = Integer.parseInt(columns[2]);
            int length = Integer.parseInt(columns[3]);
            String commentator = columns[4];
            String data = columns[5];
            return AozoraComment.newInstalce(timestamp, workID, position, length, commentator, data);
        } catch (Exception e) {
            getAzContext().log(e);
            getAzContext().log("コメントのパースに失敗しました。: " + line);
            return null;
        }
    }

    public void postComment(String workID, int position, int length, String commentator, String data) throws IOException {
        if (!getAzContext().getLineMode().isConnectable()) {
            getAzContext().log("オフラインモードではコメントを投稿できません。");
            return;
        }
        getAzContext().log("コメント投稿: workID:" + workID + "; position:" + position + "; length:" + length + "; data:" + data);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) AozoraUtil.getURLConnection(AozoraEnv.getCommentCgiURL());
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                String post = "card=" + workID +
                              "&position=" + position +
                              "&length=" + length +
                              "&commentator=" + URLEncoder.encode(commentator, "UTF-8") +
                              "&data=" + URLEncoder.encode(data, "UTF-8");
                out.write(post.getBytes("UTF-8"));
                out.flush();
            } finally {
                if (out != null)
                    out.close();
            }
            int responseCode = con.getResponseCode();
            if (responseCode != 200)
                throw new IllegalStateException("ResponseCode:" + responseCode);
            InputStream in = null;
            try {
                in = con.getInputStream();
                TagReader tin = null;
                try {
                    tin = new TagReader(new InputStreamReader(in, "UTF-8"));
                    tin.skipToStartTag("error");
                    int error = Integer.parseInt(tin.readToEndTag());
                    tin.skipToStartTag("message");
                    String mssg = tin.readToEndTag();
                    mssg = "コメント投稿:" + mssg + "(" + error + ")";
                    if (error != 0)
                        throw new IllegalStateException(mssg);
                    getAzContext().log(mssg);
                } finally {
                    tin.close();
                }
            } finally {
                if (in != null)
                    in.close();
            }
        } finally {
            if (con != null)
                con.disconnect();
        }
    }

    private final AozoraContext context;
    private final Object MUTEX = new Object();
    private List<AozoraComment> allCommentList;
}
