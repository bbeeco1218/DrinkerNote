package com.example.drinkernote;

public class NewsTypeString {
    Newspeed_item item;

    public NewsTypeString(Newspeed_item item) {
        this.item = item;
    }

    String getnotiString(){
        String noti="";
        if(item.getType()==0){ //댓글뉴스일때
            String contents = item.getContents();
            noti = item.getComentID() + "님이 회원님의 게시글에 댓글을 달았습니다.\""+subStrBytes(contents,10)+"\"";
        }else if(item.getType()==1){ //좋아요뉴스 일때
            noti = item.getLikeID() + "님이 회원님의 게시글을 좋아합니다.";
        }else if(item.getType()==2){ //팔로우 뉴스일때
            noti = item.getFollowerID() + "님이 회원님을 팔로우합니다.";
        }else{ //대댓글 뉴스일때
            String contents = item.getContents();
            noti = item.getReplyID()+"님이 회원님의 댓글에 답글을 달았습니다.\""+subStrBytes(contents,10)+"\"";
        }
        return noti;
    }

    private String subStrBytes(String source, int cutLength) {
        if (!source.isEmpty()) {
            source = source.trim();
            if (source.getBytes().length <= cutLength) {
                return source;
            } else {
                StringBuffer sb = new StringBuffer(cutLength);
                int cnt = 0;
                for (char ch : source.toCharArray()) {
                    cnt += String.valueOf(ch).getBytes().length;
                    if (cnt > cutLength) break;
                    sb.append(ch);
                }
                return sb.toString()+"...";
            }
        } else {
            return "";
        }
    }
}
