/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.soso.aozora.boot.AozoraLog;
import com.soso.aozora.core.AozoraEnv;
import com.soso.aozora.core.AozoraUtil;


public class AozoraPremierID {
    private static class PremierXMLBuilder {

        private void build(AozoraPremierID premier, String enc) throws IOException {
            builder.startDocument(enc);
            builder.startElement(ELM_PREMIER);
            builder.startElement(ELM_ACCOUNT);
            builder.attribute(ATT_ACCOUNT_ID, premier.id);
            builder.attribute(ATT_ACCOUNT_STATUS, String.valueOf(premier.status));
            builder.endElement();
            builder.startElement(ELM_COUNT);
            builder.attribute(ATT_COUNT_CHECK, String.valueOf(premier.checkCount));
            builder.attribute(ATT_COUNT_TRIAL, String.valueOf(premier.trialCount));
            builder.endElement();
            builder.startElement(ELM_CREATED);
            builder.attribute(ATT_CREATED_TIMESTAMP, String.valueOf(premier.createdTimestamp));
            builder.endElement();
            builder.startElement(ELM_LASTCHECK);
            builder.attribute(ATT_LASTCHECK_TIMESTAMP, String.valueOf(premier.lastCheckTimestamp));
            builder.endElement();
            builder.startElement(ELM_STORED);
            builder.attribute(ATT_STORED_TIMESTAMP, String.valueOf(premier.storedTimestamp));
            builder.endElement();
            builder.endElement();
            builder.endDocument();
        }

        private final XMLBuilder builder;

        private PremierXMLBuilder(Appendable out) {
            builder = new XMLBuilder(out);
        }
    }

    private static class PremierXMLHandler extends DefaultHandler {

        private AozoraPremierID getResult() {
            return premier;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (ELM_PREMIER.equals(qName))
                return;
            try {
                if (ELM_ACCOUNT.equals(qName)) {
                    startAccount(attributes);
                } else {
                    if (premier == null)
                        throw new IllegalStateException("id がありません");
                    if (ELM_COUNT.equals(qName))
                        startCount(attributes);
                    else if (ELM_CREATED.equals(qName))
                        startCreated(attributes);
                    else if (ELM_LASTCHECK.equals(qName))
                        startLastCheck(attributes);
                    else if (ELM_STORED.equals(qName))
                        startStored(attributes);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        public void startAccount(Attributes attributes) {
            String id = attributes.getValue(ATT_ACCOUNT_ID);
            premier = new AozoraPremierID(id, lock);
            String status = attributes.getValue(ATT_ACCOUNT_STATUS);
            premier.setStatus(Status.valueOf(status));
        }

        public void startCount(Attributes attributes) {
            String checkCount = attributes.getValue(ATT_COUNT_CHECK);
            premier.checkCount = Integer.parseInt(checkCount);
            String trialCount = attributes.getValue(ATT_COUNT_TRIAL);
            premier.trialCount = Integer.parseInt(trialCount);
        }

        public void startCreated(Attributes attributes) {
            String timestamp = attributes.getValue(ATT_CREATED_TIMESTAMP);
            premier.createdTimestamp = Long.parseLong(timestamp);
        }

        public void startLastCheck(Attributes attributes) {
            String timestamp = attributes.getValue(ATT_LASTCHECK_TIMESTAMP);
            premier.lastCheckTimestamp = Long.parseLong(timestamp);
        }

        public void startStored(Attributes attributes) {
            String timestamp = attributes.getValue(ATT_STORED_TIMESTAMP);
            premier.storedTimestamp = Long.parseLong(timestamp);
        }

        private final AozoraFileLock lock;
        private AozoraPremierID premier;

        private PremierXMLHandler(AozoraFileLock lock) {
            this.lock = lock;
        }
    }

    public enum Status {
        TRIAL("試用"),
        ACCOUNTING("入金確認中"),
        CONFIRMED("入金確認済"),
        INVALID("入金無効");

        private String desc;

        public String getDesc() {
            return desc;
        }

        private Status(String desc) {
            this.desc = desc;
        }
    }

    private AozoraPremierID(String id, AozoraFileLock lock) {
        if (id == null)
            throw new IllegalArgumentException("id cannot be null");
        if (lock == null) {
            throw new IllegalArgumentException("lock cannot be null");
        }
        this.id = id;
        this.lock = lock;
    }

    public String getID() {
        return id;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public int getCheckCount() {
        return checkCount;
    }

    public int getTrialCount() {
        return trialCount;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public long getLastCheckTimestamp() {
        return lastCheckTimestamp;
    }

    public long getStoredTimestamp() {
        return storedTimestamp;
    }

    public boolean isValid() {
        switch (getStatus()) {
        case TRIAL:
            return canTrialSkip();
        case ACCOUNTING:
            return isAccountingTerm();
        case CONFIRMED:
            return true;
        case INVALID:
            return false;
        }
        throw new IllegalStateException("Unknown status " + getStatus());
    }

    public void checkOnline() throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(AozoraUtil.getInputStream(AozoraEnv.getPremierCheckURL(getID())), "utf-8"));
            String line = in.readLine();
            String[] st = line.split(",");
            AozoraLog.getInstance().log("premier | " + Arrays.toString(st));
            Status status = Status.valueOf(st[1]);
            setStatus(status);
            checkCount++;
            lastCheckTimestamp = System.currentTimeMillis();
            if (status == Status.TRIAL && "SYSTEM".equals(st[3]))
                trialCount = 0;
        } finally {
            in.close();
        }
    }

    public boolean isAccountingTerm() {
        if (status != Status.ACCOUNTING)
            return false;
        long current = System.currentTimeMillis();
        return current - getLastCheckTimestamp() < ACCOUNTING_TERM_MAX;
    }

    public boolean canTrialSkip() {
        if (status != Status.TRIAL)
            return false;
        if (trialCounted)
            return true;
        return getTrialRemainderCount() > 0;
    }

    public int getTrialRemainderCount() {
        return TRYAL_COUNT_MAX - getTrialCount();
    }

    public void doTrialSkip() {
        if (trialCounted)
            return;
        if (!canTrialSkip()) {
            throw new IllegalStateException("試用回数を越えました。");
        }
        trialCount++;
        trialCounted = true;
    }

    private void releaseLock() throws IOException {
        if (lock != null && !lock.isReleased())
            lock.release();
    }

    protected void finalize() throws Throwable {
        try {
            releaseLock();
        } finally {
            super.finalize();
        }
    }

    private static File getPremierFile() {
        return new File(AozoraEnv.getUserHomeDir(), "aozora/premier.id");
    }

    private static File getLockFile() {
        return new File(AozoraEnv.getUserHomeDir(), "aozora/premier.lock");
    }

    public static AozoraFileLock lock() throws IOException {
        return new AozoraFileLock(getLockFile());
    }

    public static AozoraPremierID create(AozoraFileLock lock) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(AozoraUtil.getInputStream(AozoraEnv.getPremierNewURL()), "utf-8"));
            AozoraPremierID aozorapremierid;
            String line = in.readLine();
            String[] st = line.split(",");
            String id = st[0];
            Status status = Status.valueOf(st[1]);
            AozoraPremierID premier = new AozoraPremierID(id, lock);
            premier.setStatus(status);
            premier.createdTimestamp = System.currentTimeMillis();
            aozorapremierid = premier;
            return aozorapremierid;
        } finally {
            in.close();
        }
    }

    public static AozoraPremierID load(AozoraFileLock lock) throws IOException, SAXException, GeneralSecurityException {
        ByteArrayOutputStream baos;
        InputStream in = null;
        File file = getPremierFile();
        if (file.exists()) {
            try {
                baos = new ByteArrayOutputStream();
                in = new FileInputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1)
                    baos.write(buf, 0, len);
            } finally {
                in.close();
            }
            byte[] encripted = baos.toByteArray();
            byte[] plain = CipherUtil.decript(encripted);
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                PremierXMLHandler handler = new PremierXMLHandler(lock);
                parser.parse(new ByteArrayInputStream(plain), handler);
                return handler.getResult();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    public void store() throws IOException, GeneralSecurityException {
        ByteArrayOutputStream baos;
        OutputStreamWriter writer = null;
        try {
            storedTimestamp = System.currentTimeMillis();
            String enc = "UTF-8";
            baos = new ByteArrayOutputStream();
            writer = new OutputStreamWriter(baos, enc);
            PremierXMLBuilder builder = new PremierXMLBuilder(writer);
            builder.build(this, enc);
        } finally {
            writer.close();
        }
        byte[] encripted;
        OutputStream out = null;
        try {
            byte[] plain = baos.toByteArray();
            encripted = CipherUtil.encript(plain);
            File file = getPremierFile();
            if (!file.exists() && !file.getParentFile().exists())
                file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            out.write(encripted);
            out.flush();
        } finally {
            out.close();
        }
    }

    AozoraPremierID(String x0, AozoraFileLock x1, Status x2) {
        this(x0, x1);
    }

    private static final int TRYAL_COUNT_MAX = 13;
    private static final long ACCOUNTING_TERM_MAX = 0x240c8400L;

    private final AozoraFileLock lock;
    private final String id;
    private Status status;
    private int checkCount;
    private int trialCount;
    private long createdTimestamp;
    private long lastCheckTimestamp;
    private long storedTimestamp;
    private boolean trialCounted;

    private static final String ELM_PREMIER = "premier";
    private static final String ELM_ACCOUNT = "account";
    private static final String ATT_ACCOUNT_ID = "id";
    private static final String ATT_ACCOUNT_STATUS = "status";
    private static final String ELM_COUNT = "count";
    private static final String ATT_COUNT_CHECK = "check";
    private static final String ATT_COUNT_TRIAL = "trial";
    private static final String ELM_CREATED = "created";
    private static final String ATT_CREATED_TIMESTAMP = "timestamp";
    private static final String ELM_LASTCHECK = "lastcheck";
    private static final String ATT_LASTCHECK_TIMESTAMP = "timestamp";
    private static final String ELM_STORED = "stored";
    private static final String ATT_STORED_TIMESTAMP = "timestamp";
}
