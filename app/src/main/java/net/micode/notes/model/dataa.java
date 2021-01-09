package net.micode.notes.model;
import org.litepal.crud.LitePalSupport;
public class dataa extends LitePalSupport {

    // 用户标识
    private long userid;

    // MIME的类别
    private String mime_type;

    // 对应着note表中的id
    private long note_id;

    // note的创建时间
    private long created_date;

    // 最近的修改时间
    private long modified_date;

    // note的内容
    private String content;

    //
    private long data1;
    private long data2;
    private String data3;
    private String data4;
    private String data5;

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public long getNote_id() {
        return note_id;
    }

    public void setNote_id(long note_id) {
        this.note_id = note_id;
    }

    public long getCreated_date() {
        return created_date;
    }

    public void setCreated_date(long created_date) {
        this.created_date = created_date;
    }

    public long getModified_date() {
        return modified_date;
    }

    public void setModified_date(long modified_date) {
        this.modified_date = modified_date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getData1() {
        return data1;
    }

    public void setData1(long data1) {
        this.data1 = data1;
    }

    public long getData2() {
        return data2;
    }

    public void setData2(long data2) {
        this.data2 = data2;
    }

    public String getData3() {
        return data3;
    }

    public void setData3(String data3) {
        this.data3 = data3;
    }

    public String getData4() {
        return data4;
    }

    public void setData4(String data4) {
        this.data4 = data4;
    }

    public String getData5() {
        return data5;
    }

    public void setData5(String data5) {
        this.data5 = data5;
    }

    @Override
    public String toString() {
        return "dataa{" +
                "userid=" + userid +
                ", mime_type='" + mime_type + '\'' +
                ", note_id=" + note_id +
                ", created_date=" + created_date +
                ", modified_date=" + modified_date +
                ", content='" + content + '\'' +
                ", data1=" + data1 +
                ", data2=" + data2 +
                ", data3='" + data3 + '\'' +
                ", data4='" + data4 + '\'' +
                ", data5='" + data5 + '\'' +
                '}';
    }
}
