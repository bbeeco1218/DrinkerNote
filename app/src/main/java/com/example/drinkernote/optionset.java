package com.example.drinkernote;

public class optionset {
    boolean allAlam = true;
    boolean notelikeAlam = true;
    boolean comentAlam = true;
    boolean replyAlam = true;
    boolean followAlam = true;
    boolean msgAlam = true;

    public boolean isMsgAlam() {
        return msgAlam;
    }

    public void setMsgAlam(boolean msgAlam) {
        this.msgAlam = msgAlam;
    }

    public boolean isAllAlam() {
        return allAlam;
    }

    public void setAllAlam(boolean allAlam) {
        this.allAlam = allAlam;
    }

    public boolean isNotelikeAlam() {
        return notelikeAlam;
    }

    public void setNotelikeAlam(boolean notelikeAlam) {
        this.notelikeAlam = notelikeAlam;
    }

    public boolean isComentAlam() {
        return comentAlam;
    }

    public void setComentAlam(boolean comentAlam) {
        this.comentAlam = comentAlam;
    }

    public boolean isReplyAlam() {
        return replyAlam;
    }

    public void setReplyAlam(boolean replyAlam) {
        this.replyAlam = replyAlam;
    }

    public boolean isFollowAlam() {
        return followAlam;
    }

    public void setFollowAlam(boolean followAlam) {
        this.followAlam = followAlam;
    }
}
