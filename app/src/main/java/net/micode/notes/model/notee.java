package net.micode.notes.model;
import org.litepal.crud.LitePalSupport;
public class notee extends LitePalSupport {

    // 用户标识
    private long userid;

    // 父类的id
    private long parent_id;

    // 插入时间，指的是在文件夹中创建note的时间？
    private long alert_data;

    // 背景颜色
    private long bg_color_id;

    // 文件夹或者便签的创建时间
    private long created_date;

    // For text note, it doesn't has attachment, for multi-media note, it has at least one attachment
    private long has_attachment;

    // 最近修改的时间
    private long modified_date;

    // 文件夹中的文件数量
    private long notes_count;

    // 文件夹或者便签的标题
    private String snippet;

    // 类别，note，folder,system_file
    private long type;

    // widget(小窗口)的id
    private long widget_id;

    // widget的type
    private long widget_type;

    // 同步的id
    private long sync_id;

    // 本地修改的标志
    private long local_modified;

    // Original parent id before moving into temporary folder
    private long origin_parent_id;

    //gtask的id
    private String gtask_id;

    // code的版本
    private long version;

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public long getParent_id() {
        return parent_id;
    }

    public void setParent_id(long parent_id) {
        this.parent_id = parent_id;
    }

    public long getAlert_data() {
        return alert_data;
    }

    public void setAlert_data(long alert_data) {
        this.alert_data = alert_data;
    }

    public long getBg_color_id() {
        return bg_color_id;
    }

    public void setBg_color_id(long bg_color_id) {
        this.bg_color_id = bg_color_id;
    }

    public long getCreated_date() {
        return created_date;
    }

    public void setCreated_date(long created_date) {
        this.created_date = created_date;
    }

    public long getHas_attachment() {
        return has_attachment;
    }

    public void setHas_attachment(long has_attachment) {
        this.has_attachment = has_attachment;
    }

    public long getModified_date() {
        return modified_date;
    }

    public void setModified_date(long modified_date) {
        this.modified_date = modified_date;
    }

    public long getNotes_count() {
        return notes_count;
    }

    public void setNotes_count(long notes_count) {
        this.notes_count = notes_count;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public long getWidget_id() {
        return widget_id;
    }

    public void setWidget_id(long widget_id) {
        this.widget_id = widget_id;
    }

    public long getWidget_type() {
        return widget_type;
    }

    public void setWidget_type(long widget_type) {
        this.widget_type = widget_type;
    }

    public long getSync_id() {
        return sync_id;
    }

    public void setSync_id(long sync_id) {
        this.sync_id = sync_id;
    }

    public long getLocal_modified() {
        return local_modified;
    }

    public void setLocal_modified(long local_modified) {
        this.local_modified = local_modified;
    }

    public long getOrigin_parent_id() {
        return origin_parent_id;
    }

    public void setOrigin_parent_id(long origin_parent_id) {
        this.origin_parent_id = origin_parent_id;
    }

    public String getGtask_id() {
        return gtask_id;
    }

    public void setGtask_id(String gtask_id) {
        this.gtask_id = gtask_id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "notee{" +
                "userid=" + userid +
                ", parent_id=" + parent_id +
                ", alert_data=" + alert_data +
                ", bg_color_id=" + bg_color_id +
                ", created_date=" + created_date +
                ", has_attachment=" + has_attachment +
                ", modified_date=" + modified_date +
                ", notes_count=" + notes_count +
                ", snippet='" + snippet + '\'' +
                ", type=" + type +
                ", widget_id=" + widget_id +
                ", widget_type=" + widget_type +
                ", sync_id=" + sync_id +
                ", local_modified=" + local_modified +
                ", origin_parent_id=" + origin_parent_id +
                ", gtask_id='" + gtask_id + '\'' +
                ", version=" + version +
                '}';
    }
}
