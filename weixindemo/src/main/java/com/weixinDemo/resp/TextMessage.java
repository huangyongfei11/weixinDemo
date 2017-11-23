package com.weixinDemo.resp;

/**
 * 文本消息
 */
public class TextMessage extends BaseMessage {
    //
    private String Content;
    public String getContent() {
        return Content;
    }
    public void setContent(String content) {
        Content = content;
    }
}
