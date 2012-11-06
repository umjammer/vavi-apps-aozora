/*
 * http://www.35-35.net/aozora/
 */

package com.soso.aozora.data;


public class AozoraAuthor {

    AozoraAuthor() {
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getDeadDate() {
        return deadDate;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public String getKana() {
        return kana;
    }

    public String getRomanName() {
        return romanName;
    }

    void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    void setDeadDate(String deadDate) {
        this.deadDate = deadDate;
    }

    void setID(String id) {
        this.id = id;
    }

    void setName(String name) {
        this.name = name;
    }

    void setNote(String note) {
        this.note = note;
    }

    void setKana(String pronunciation) {
        kana = pronunciation;
    }

    void setRomanName(String romanName) {
        this.romanName = romanName;
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof AozoraAuthor)) {
            String id = getID();
            return id != null && id.equals(((AozoraAuthor) obj).getID());
        } else {
            return super.equals(obj);
        }
    }

    public String toString() {
        return super.toString() + "[" +
            getID() + "|" +
            getName() + "|" +
            getKana() + "|" +
            getRomanName() + "|" +
            getBirthDate() + "|" +
            getDeadDate() + "|" +
            getNote() + "]";
    }

    private String id;
    private String name;
    private String kana;
    private String romanName;
    private String birthDate;
    private String deadDate;
    private String note;
}
