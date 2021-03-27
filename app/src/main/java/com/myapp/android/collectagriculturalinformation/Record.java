package com.myapp.android.collectagriculturalinformation;
/**
 * Description 模型层Record类
 */
import java.util.Date;
import java.util.UUID;

public class Record {

    //标识ID
    private UUID mId;
    //标题
    private String mTitle;
    //日期
    private Date mDate;
    //是否解决
    private boolean mSolved;
    //联系人
    private String mContacts;

    public Record() {
        //产生随机唯一ID值
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
