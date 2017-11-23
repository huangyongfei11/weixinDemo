package com.weixinDemo.service;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.weixinDemo.pojo.*;
import com.weixinDemo.resp.Article;
import com.weixinDemo.resp.NewsMessage;
import com.weixinDemo.resp.TextMessage;
import com.weixinDemo.utils.MessageUtil;
import com.weixinDemo.utils.WeixinUtil2;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 核心服务类
 *
 * @author liufeng
 * @date 2013-05-20
 */
public class CoreService {
    /**
     * 处理微信发来的请求
     *
     * @param request
     * @return
     */
    public static String processRequest(HttpServletRequest request) {
        String respMessage = null;
        try {
            // 默认返回的文本消息内容
            String respContent = "请求处理异常，请稍候尝试！";

            // xml请求解析
            Map<String, String> requestMap = MessageUtil.parseXml(request);
            for (Map.Entry<String, String> entry : requestMap.entrySet()) {
                System.out.println(entry.getKey() + "   " + entry.getValue());
            }

            // 发送方帐号（open_id）
            String fromUserName = requestMap.get("FromUserName");
            // 公众帐号
            String toUserName = requestMap.get("ToUserName");
            // 消息类型
            String msgType = requestMap.get("MsgType");
            //消息内容
            String content = requestMap.get("Content");
            System.out.println(content + " 微信发过来的内容 ");
            // 回复文本消息
            TextMessage textMessage = new TextMessage();
            textMessage.setToUserName(fromUserName);
            textMessage.setFromUserName(toUserName);
            textMessage.setCreateTime(new Date().getTime());
            textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
            textMessage.setFuncFlag(0);
            // 文本消息
            if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
                if (content.equals("?")) {
                    respContent = getMainMenu();
                } else if (content.equals("11")) {
                    respContent = "[难过] /难过 /::(";
                } else if (content.equals("[Facepalm]")) {
                    respContent = content;
                } else if(content.equals("历史上的今天")){
                    respContent=TodayInHistoryService.getTodayInHistoryInfo();
                    System.out.println(respContent);
                } else {
                    NewsMessage newsMessage = getMessage(content,fromUserName,toUserName);
                    respMessage = MessageUtil.newsMessageToXml(newsMessage);
                    return respMessage;
                }
            }
            // 图片消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
                respContent = "您发送的是图片消息！";
            }
            // 地理位置消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
                respContent = "您发送的是地理位置消息！";
            }
            // 链接消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
                respContent = "您发送的是链接消息！";
            }
            // 音频消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
                respContent = "您发送的是音频消息！";
            }
            // 事件推送
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
                // 事件类型
                String eventType = requestMap.get("Event");
                // 订阅
                if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
                    respContent = "谢谢您的关注！";
                }
                // 取消订阅
                else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
                    // TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
                }
                // 自定义菜单点击事件
                else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
                    // TODO 自定义菜单权没有开放，暂不处理该类消息
                }
            }

            textMessage.setContent(respContent);
            respMessage = MessageUtil.textMessageToXml(textMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return respMessage;
    }

    /**
     * 在微信公众帐号的文本消息中，换行符仍然是“\n”
     *
     * @return
     */
    public static String getMainMenu() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("您好，我是小黄鱼，请回复数字选择服务：").append("\n\n");
        buffer.append("1  天气预报").append("\n");
        buffer.append("2  公交查询").append("\n");
        buffer.append("3  周边搜索").append("\n");
        buffer.append("4  歌曲点播").append("\n");
        buffer.append("5  经典游戏").append("\n");
        buffer.append("6  美女电台").append("\n");
        buffer.append("7  人脸识别").append("\n");
        buffer.append("8  <a href=\"http://blog.csdn.net/lyq8479\">柳峰的博客</a> ").append("\n\n");
        buffer.append("回复“?”显示此帮助菜单");
        return buffer.toString();
    }

    /**
     * 判断是否是QQ表情
     *
     * @param content
     * @return
     */
    public static boolean isQqFace(String content) {
        boolean result = false;
        // 判断QQ表情的正则表达式
        String qqfaceRegex = "/::\\)|/::~|/::B|/::\\||/:8-\\)|/::<|/::$|/::X|/::Z|/::'\\(|/::-\\||/::@|/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P|/:,@-D|/::d|/:,@o|/::g|/:\\|-\\)|/::!|/::L|/::>|/::,@|/:,@f|/::-S|/:\\?|/:,@x|/:,@@|/::8|/:,@!|/:!!!|/:xx|/:bye|/:wipe|/:dig|/:handclap|/:&-\\(|/:B-\\)|/:<@|/:@>|/::-O|/:>-\\||/:P-\\(|/::'\\||/:X-\\)|/::\\*|/:@x|/:8\\*|/:pd|/:<W>|/:beer|/:basketb|/:oo|/:coffee|/:eat|/:pig|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/:footb|/:ladybug|/:shit|/:moon|/:sun|/:gift|/:hug|/:strong|/:weak|/:share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:love|/:<L>|/:jump|/:shake|/:<O>|/:circle|/:kotow|/:turn|/:skip|/:oY|/:#-0|/:hiphot|/:kiss|/:<&|/:&>";
        Pattern p = Pattern.compile(qqfaceRegex);
        Matcher m = p.matcher(content);
        if (m.matches()) {
            result = true;
        }
        return result;
    }

    /**
     * 将微信消息中的CreateTime转换成标准格式的时间（yyyy-MM-dd HH:mm:ss）
     *
     * @param createTime 消息创建时间
     * @return
     */
    public static String formatTime(String createTime) {
        // 将微信传入的CreateTime转换成long类型，再乘以1000
        long msgCreateTime = Long.parseLong(createTime) * 1000L;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(msgCreateTime));
    }

    public static NewsMessage getMessage(String content, String fromUserName, String toUserName) {
        // 创建图文消息
        NewsMessage newsMessage = new NewsMessage();
        newsMessage.setToUserName(fromUserName);
        newsMessage.setFromUserName(toUserName);
        newsMessage.setCreateTime(new Date().getTime());
        newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
        newsMessage.setFuncFlag(0);
        try {
            List<Article> articleList = new ArrayList<Article>();
            // 单图文消息
            if ("1".equals(content)) {
                Article article = new Article();
                article.setTitle("微信公众帐号开发教程Java版");
                article.setDescription("柳峰，80后，微信公众帐号开发经验4个月。为帮助初学者入门，特推出此系列教程，也希望借此机会认识更多同行！");
                article.setPicUrl("http://0.xiaoqrobot.duapp.com/images/avatar_liufeng.jpg");
                article.setUrl("http://blog.csdn.net/lyq8479");
                articleList.add(article);
                // 设置图文消息个数
                newsMessage.setArticleCount(articleList.size());
                // 设置图文消息包含的图文集合
                newsMessage.setArticles(articleList);
                // 将图文消息对象转换成xml字符串
                System.out.println("1111111111111111");
                return newsMessage;
            }
            // 单图文消息---不含图片
            else if ("2".equals(content)) {
                Article article = new Article();
                article.setTitle("微信公众帐号开发教程Java版");
                // 图文消息中可以使用QQ表情、符号表情
                article.setDescription("柳峰，80后，" + emoji(0x1F6B9)
                        + "，微信公众帐号开发经验4个月。为帮助初学者入门，特推出此系列连载教程，也希望借此机会认识更多同行！\n\n目前已推出教程共12篇，包括接口配置、消息封装、框架搭建、QQ表情发送、符号表情发送等。\n\n后期还计划推出一些实用功能的开发讲解，例如：天气预报、周边搜索、聊天功能等。");
                // 将图片置为空
                article.setPicUrl("");
                article.setUrl("http://blog.csdn.net/lyq8479");
                articleList.add(article);
                newsMessage.setArticleCount(articleList.size());
                newsMessage.setArticles(articleList);
                System.out.println("22222222222222222");
                return newsMessage;
            }
            // 多图文消息
            else if ("3".equals(content)) {
                Article article1 = new Article();
                article1.setTitle("微信公众帐号开发教程\n引言");
                article1.setDescription("");
                article1.setPicUrl("http://0.xiaoqrobot.duapp.com/images/avatar_liufeng.jpg");
                article1.setUrl("http://blog.csdn.net/lyq8479/article/details/8937622");

                Article article2 = new Article();
                article2.setTitle("第2篇\n微信公众帐号的类型");
                article2.setDescription("");
                article2.setPicUrl("http://avatar.csdn.net/1/4/A/1_lyq8479.jpg");
                article2.setUrl("http://blog.csdn.net/lyq8479/article/details/8941577");

                Article article3 = new Article();
                article3.setTitle("第3篇\n开发模式启用及接口配置");
                article3.setDescription("");
                article3.setPicUrl("http://avatar.csdn.net/1/4/A/1_lyq8479.jpg");
                article3.setUrl("http://blog.csdn.net/lyq8479/article/details/8944988");

                articleList.add(article1);
                articleList.add(article2);
                articleList.add(article3);
                newsMessage.setArticleCount(articleList.size());
                newsMessage.setArticles(articleList);
                System.out.println("33333333333333333333");
                return newsMessage;
            }
            // 多图文消息---首条消息不含图片
            else if ("4".equals(content)) {
                Article article1 = new Article();
                article1.setTitle("微信公众帐号开发教程Java版");
                article1.setDescription("");
                // 将图片置为空
                article1.setPicUrl("");
                article1.setUrl("http://blog.csdn.net/lyq8479");

                Article article2 = new Article();
                article2.setTitle("第4篇\n消息及消息处理工具的封装");
                article2.setDescription("");
                article2.setPicUrl("http://avatar.csdn.net/1/4/A/1_lyq8479.jpg");
                article2.setUrl("http://blog.csdn.net/lyq8479/article/details/8949088");

                Article article3 = new Article();
                article3.setTitle("第5篇\n各种消息的接收与响应");
                article3.setDescription("");
                article3.setPicUrl("http://avatar.csdn.net/1/4/A/1_lyq8479.jpg");
                article3.setUrl("http://blog.csdn.net/lyq8479/article/details/8952173");

                Article article4 = new Article();
                article4.setTitle("第6篇\n文本消息的内容长度限制揭秘");
                article4.setDescription("");
                article4.setPicUrl("http://avatar.csdn.net/1/4/A/1_lyq8479.jpg");
                article4.setUrl("http://blog.csdn.net/lyq8479/article/details/8967824");

                articleList.add(article1);
                articleList.add(article2);
                articleList.add(article3);
                articleList.add(article4);
                newsMessage.setArticleCount(articleList.size());
                newsMessage.setArticles(articleList);
                return newsMessage;
            }
            // 多图文消息---最后一条消息不含图片
            else if ("5".equals(content)) {
                Article article1 = new Article();
                article1.setTitle("第7篇\n文本消息中换行符的使用");
                article1.setDescription("");
                article1.setPicUrl("http://0.xiaoqrobot.duapp.com/images/avatar_liufeng.jpg");
                article1.setUrl("http://blog.csdn.net/lyq8479/article/details/9141467");

                Article article2 = new Article();
                article2.setTitle("第8篇\n文本消息中使用网页超链接");
                article2.setDescription("");
                article2.setPicUrl("http://avatar.csdn.net/1/4/A/1_lyq8479.jpg");
                article2.setUrl("http://blog.csdn.net/lyq8479/article/details/9157455");

                Article article3 = new Article();
                article3.setTitle("如果觉得文章对你有所帮助，请通过博客留言或关注微信公众帐号xiaoqrobot来支持柳峰！");
                article3.setDescription("");
                // 将图片置为空
                article3.setPicUrl("");
                article3.setUrl("http://blog.csdn.net/lyq8479");
                articleList.add(article1);
                articleList.add(article2);
                articleList.add(article3);
                newsMessage.setArticleCount(articleList.size());
                newsMessage.setArticles(articleList);
               return newsMessage;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsMessage;
    }

    public static String emoji(int hexEmoji) {
        return String.valueOf(Character.toChars(hexEmoji));
    }

    /**
     * 菜单管理器类
     *
     * @author liufeng
     * @date 2013-08-08
     */
//        private static Logger log = LoggerFactory.getLogger(MenuManager.class);

        public static void main(String[] args) {
            // 第三方用户唯一凭证
            String appId = "000000000000000000";
            // 第三方用户唯一凭证密钥
            String appSecret = "00000000000000000000000000000000";

            // 调用接口获取access_token
            AccessToken at = WeixinUtil2.getAccessToken(appId, appSecret);

            if (null != at) {
                // 调用接口创建菜单
                int result = WeixinUtil2.createMenu(getMenu(), at.getToken());

                // 判断菜单创建结果
//                if (0 == result)
//                    log.info("菜单创建成功！");
//                else
//                    log.info("菜单创建失败，错误码：" + result);
            }
        }
        /**
         * 组装菜单数据
         *
         * @return
         */
        private static Menu getMenu() {
            CommonButton btn11 = new CommonButton();
            btn11.setName("天气预报");
            btn11.setType("click");
            btn11.setKey("11");

            CommonButton btn12 = new CommonButton();
            btn12.setName("公交查询");
            btn12.setType("click");
            btn12.setKey("12");

            CommonButton btn13 = new CommonButton();
            btn13.setName("周边搜索");
            btn13.setType("click");
            btn13.setKey("13");

            CommonButton btn14 = new CommonButton();
            btn14.setName("历史上的今天");
            btn14.setType("click");
            btn14.setKey("14");

            CommonButton btn21 = new CommonButton();
            btn21.setName("歌曲点播");
            btn21.setType("click");
            btn21.setKey("21");

            CommonButton btn22 = new CommonButton();
            btn22.setName("经典游戏");
            btn22.setType("click");
            btn22.setKey("22");

            CommonButton btn23 = new CommonButton();
            btn23.setName("美女电台");
            btn23.setType("click");
            btn23.setKey("23");

            CommonButton btn24 = new CommonButton();
            btn24.setName("人脸识别");
            btn24.setType("click");
            btn24.setKey("24");

            CommonButton btn25 = new CommonButton();
            btn25.setName("聊天唠嗑");
            btn25.setType("click");
            btn25.setKey("25");

            CommonButton btn31 = new CommonButton();
            btn31.setName("Q友圈");
            btn31.setType("click");
            btn31.setKey("31");

            CommonButton btn32 = new CommonButton();
            btn32.setName("电影排行榜");
            btn32.setType("click");
            btn32.setKey("32");

            CommonButton btn33 = new CommonButton();
            btn33.setName("幽默笑话");
            btn33.setType("click");
            btn33.setKey("33");

            ComplexButton mainBtn1 = new ComplexButton();
            mainBtn1.setName("生活助手");
            mainBtn1.setSub_button(new CommonButton[] { btn11, btn12, btn13, btn14 });

            ComplexButton mainBtn2 = new ComplexButton();
            mainBtn2.setName("休闲驿站");
            mainBtn2.setSub_button(new CommonButton[] { btn21, btn22, btn23, btn24, btn25 });

            ComplexButton mainBtn3 = new ComplexButton();
            mainBtn3.setName("更多体验");
            mainBtn3.setSub_button(new CommonButton[] { btn31, btn32, btn33 });

            /**
             * 这是公众号xiaoqrobot目前的菜单结构，每个一级菜单都有二级菜单项<br>
             *
             * 在某个一级菜单下没有二级菜单的情况，menu该如何定义呢？<br>
             * 比如，第三个一级菜单项不是“更多体验”，而直接是“幽默笑话”，那么menu应该这样定义：<br>
             * menu.setButton(new Button[] { mainBtn1, mainBtn2, btn33 });
             */
            Menu menu = new Menu();
            menu.setButton(new Button[] { mainBtn1, mainBtn2, mainBtn3 });
            return menu;
        }
    }