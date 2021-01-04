package net.micode.notes.model;

public class user {

    // 用户id
    private String userid;

    //用户名
    private  String username;

    //用户密码
    private String userpassword;

    //用户生日
    private String userbirthday;

    //用户性别
    private String usersex;

    // 个性签名
  private String userSignature;
    //    // 保存头像的路径
    private String userimagePath;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }

    public String getUserbirthday() {
        return userbirthday;
    }

    public void setUserbirthday(String userbirthday) {
        this.userbirthday = userbirthday;
    }

    public String getUsersex() {
        return usersex;
    }

    public void setUsersex(String usersex) {
        this.usersex = usersex;
    }

    public String getUserSignature() {
        return userSignature;
    }

    public void setUserSignature(String userSignature) {
        this.userSignature = userSignature;
    }

    public String getUserimagePath() {
        return userimagePath;
    }

    public void setUserimagePath(String userimagePath) {
        this.userimagePath = userimagePath;
    }

    @Override
    public String toString() {
        return "user{" +
                "userid='" + userid + '\'' +
                ", username='" + username + '\'' +
                ", userpassword='" + userpassword + '\'' +
                ", userbirthday='" + userbirthday + '\'' +
                ", usersex='" + usersex + '\'' +
                ", userSignature='" + userSignature + '\'' +
                ", imagePath='" + userimagePath + '\'' +
                '}';
    }
}
