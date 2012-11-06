/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;


public class AozoraWork {

    private String authorID;
    private String translatorID;
    private String id;
    private String titleName;
    private String titleKana;
    private String titleOriginal;
    private String metaURL;
    private String contentURL;
    private String kanaType;
    private String note;
    private String orginalBook;
    private String publisher;
    private String firstDate;
    private String inputBase;
    private String proofBase;
    private String completeOrginalBook;
    private String completePublisher;
    private String completeFirstDate;

    AozoraWork() {
    }

    public String getAuthorID() {
        return authorID;
    }

    public String getCompleteFirstDate() {
        return completeFirstDate;
    }

    public String getCompleteOrginalBook() {
        return completeOrginalBook;
    }

    public String getCompletePublisher() {
        return completePublisher;
    }

    public String getContentURL() {
        return contentURL;
    }

    public String getFirstDate() {
        return firstDate;
    }

    public String getID() {
        return id;
    }

    public String getInputBase() {
        return inputBase;
    }

    public String getKanaType() {
        return kanaType;
    }

    public String getMetaURL() {
        return metaURL;
    }

    public String getNote() {
        return note;
    }

    public String getOrginalBook() {
        return orginalBook;
    }

    public String getProofBase() {
        return proofBase;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getTitleName() {
        return titleName;
    }

    public String getTitleKana() {
        return titleKana;
    }

    public String getTitleOriginal() {
        return titleOriginal;
    }

    public String getTranslatorID() {
        return translatorID;
    }

    void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    void setCompleteFirstDate(String completeFirstEditionDate) {
        completeFirstDate = completeFirstEditionDate;
    }

    void setCompleteOrginalBook(String completeOrginalWork) {
        completeOrginalBook = completeOrginalWork;
    }

    void setCompletePublisher(String completePublishingCompany) {
        completePublisher = completePublishingCompany;
    }

    void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }

    void setFirstDate(String firstEditionDate) {
        firstDate = firstEditionDate;
    }

    void setID(String id) {
        this.id = id;
    }

    void setInputBase(String inputBase) {
        this.inputBase = inputBase;
    }

    void setKanaType(String kanaType) {
        this.kanaType = kanaType;
    }

    void setMetaURL(String metaURL) {
        this.metaURL = metaURL;
    }

    void setNote(String note) {
        this.note = note;
    }

    void setOrginalBook(String orginalWork) {
        orginalBook = orginalWork;
    }

    void setProofBase(String proofBase) {
        this.proofBase = proofBase;
    }

    void setPublisher(String publishingCompany) {
        publisher = publishingCompany;
    }

    void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    void setTitleKana(String titlePronunciation) {
        titleKana = titlePronunciation;
    }

    void setTitleOriginal(String titleOriginal) {
        this.titleOriginal = titleOriginal;
    }

    void setTranslatorID(String translatorID) {
        this.translatorID = translatorID;
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof AozoraWork)) {
            String id = getID();
            return id != null && id.equals(((AozoraWork) obj).getID());
        } else {
            return super.equals(obj);
        }
    }

    public String toString() {
        return super.toString() + "["
                + getID() + "|"
                + getAuthorID() + "|"
                + getTranslatorID() + "|"
                + getTitleName() + "|"
                + getTitleKana() + "|"
                + getTitleOriginal() + "|"
                + getMetaURL() + "|"
                + getContentURL() + "|"
                + getKanaType() + "|"
                + getNote() + "|"
                + getOrginalBook() + "|"
                + getPublisher() + "|"
                + getFirstDate() + "|"
                + getInputBase() + "|"
                + getProofBase() + "|"
                + getCompleteOrginalBook() + "|"
                + getCompletePublisher() + "|"
                + getCompleteFirstDate() + "]";
    }
}
