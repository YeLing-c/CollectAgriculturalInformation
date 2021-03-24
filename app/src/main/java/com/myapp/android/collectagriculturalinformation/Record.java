package com.myapp.android.collectagriculturalinformation;

import java.util.Date;
import java.util.UUID;

public class Record {

    //测试
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mContacts;

    public Record() {
        this(UUID.randomUUID());
    }

    public Record(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getContacts() {
        return mContacts;
    }

    public void setContacts(String contacts) {
        mContacts = contacts;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

}
