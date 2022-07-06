package com.example.drinkernote;

public class Newspeed_item {
    int type; //0 댓글뉴스 1좋아요뉴스 2팔로우뉴스 3대댓글뉴스
    int NoteKey;
    String likeID;
    String comentID;
    int comentKey;
    String followerID,followingID;
    String toID;
    String replyID;
    String contents;
    int replyKey;


    public Newspeed_item(int type, int NoteKey, String comentID, int comentKey, String toID,String contents) { //0 댓글뉴스
        this.type = type;
        this.NoteKey = NoteKey;
        this.comentID = comentID;
        this.comentKey = comentKey;
        this.toID = toID;
        this.contents = contents;
    }

    public Newspeed_item(int type,int NoteKey,String likeID,String toID) { //1 좋아요뉴스
        this.type = type;
        this.NoteKey = NoteKey;
        this.likeID = likeID;
        this.toID = toID;
    }

    public Newspeed_item(int type,String followingID,String followerID,String toID) { //2 팔로우뉴스
        this.type = type;
        this.followingID = followingID;
        this.followerID = followerID;
        this.toID = toID;
    }

    public Newspeed_item(int type,int comentKey,String replyID,String toID,int replyKey,String contents,int NoteKey) { //3 대댓글뉴스
        this.type = type;
        this.comentKey = comentKey;
        this.replyID = replyID;
        this.toID = toID;
        this.contents = contents;
        this.replyKey = replyKey;
        this.NoteKey = NoteKey;
    }

    public int getReplyKey() {
        return replyKey;
    }

    public void setReplyKey(int replyKey) {
        this.replyKey = replyKey;
    }

    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
    }


    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getToID() {
        return toID;
    }

    public void setToID(String toID) {
        this.toID = toID;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNoteKey() {
        return NoteKey;
    }

    public void setNoteKey(int noteKey) {
        NoteKey = noteKey;
    }

    public String getLikeID() {
        return likeID;
    }

    public void setLikeID(String likeID) {
        this.likeID = likeID;
    }

    public String getComentID() {
        return comentID;
    }

    public void setComentID(String comentID) {
        this.comentID = comentID;
    }

    public int getComentKey() {
        return comentKey;
    }

    public void setComentKey(int comentKey) {
        this.comentKey = comentKey;
    }

    public String getFollowerID() {
        return followerID;
    }

    public void setFollowerID(String followerID) {
        this.followerID = followerID;
    }

    public String getFollowingID() {
        return followingID;
    }

    public void setFollowingID(String followingID) {
        this.followingID = followingID;
    }
}
