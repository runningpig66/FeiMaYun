package cn.aura.feimayun.vhall.chat;

import android.content.Context;

import com.vhall.business.ChatServer;

import java.util.List;

import cn.aura.feimayun.vhall.BasePresenter;
import cn.aura.feimayun.vhall.BaseView;
import cn.aura.feimayun.vhall.util.emoji.InputUser;

/**
 * 观看页的接口类
 */
public class ChatContract {

    public interface ChatView extends BaseView<ChatPresenter> {
        void notifyDataChangedChat(MessageChatData data);

        void notifyDataChangedQe(ChatServer.ChatInfo data);

        void notifyDataChangedChat(int type, List<MessageChatData> list);

        void notifyDataChangedQe(int type, List<ChatServer.ChatInfo> list);

        void showToast(String content);

        void clearChatData();

        Context getContext();

        void performSend(String content, int chatEvent);
    }

    public interface ChatPresenter extends BasePresenter {

        void showChatView(boolean emoji, InputUser user, int limit);

        void sendChat(String text);

//        void sendCustom(JSONObject text);

//        void sendQuestion(String content);

        void onLoginReturn();

//        void showSurvey(String url, String title);

//        void showSurvey(String surveyid);
    }

}
