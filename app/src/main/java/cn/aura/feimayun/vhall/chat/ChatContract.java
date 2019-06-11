package cn.aura.feimayun.vhall.chat;

import com.vhall.business.ChatServer;

import java.util.List;

import cn.aura.feimayun.vhall.BasePresenter;
import cn.aura.feimayun.vhall.BaseView;
import cn.aura.feimayun.vhall.util.emoji.InputUser;

/**
 * 观看页的接口类 1121
 */
public class ChatContract {

    public interface ChatView extends BaseView<ChatPresenter> {
        void notifyDataChanged(ChatServer.ChatInfo data);

        void notifyDataChanged(int type, List<ChatServer.ChatInfo> list);

        void showToast(String content);

        void clearChatData();

        void performSend(String content, int chatEvent);
    }

    public interface ChatPresenter extends BasePresenter {

        void showChatView(boolean emoji, InputUser user, int limit);

        void sendChat(String text);

//        void sendCustom(JSONObject text);

//        void sendQuestion(String content);

        void onLoginReturn();

//        void onFreshData();

//        void showSurvey(String surveyid);

    }


}
