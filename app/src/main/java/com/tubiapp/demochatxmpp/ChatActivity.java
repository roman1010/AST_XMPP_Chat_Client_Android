package com.tubiapp.demochatxmpp;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tubiapp.demochatxmpp.Items.ChatItem;
import com.tubiapp.demochatxmpp.abstracts.BaseActivity;
import com.tubiapp.demochatxmpp.adapters.ChatAdapter;
import com.tubiapp.demochatxmpp.apis.ExecutorManager;
import com.tubiapp.demochatxmpp.apis.model.User;
import com.tubiapp.demochatxmpp.utils.XMPPChatManager;
import com.tubiapp.demochatxmpp.utils.interfaces.IncomingChatCallback;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends BaseActivity implements View.OnClickListener, IncomingChatCallback {
    public static final String KEY_EMAIL = "key_email";
    public static final String KEY_PASSWORD = "key_password";
    private ListView lvChatContent;
    private ChatAdapter adapter;
    private EditText edtChat;
    private Button btnSend;
    private String sendingMessage;
    private XMPPChatManager chatManager;

    @Override
    protected void initDatas() {
        initChatManager();
    }

    @Override
    protected void initRootViews() {
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void initUIComponents() {
        lvChatContent = (ListView) findViewById(R.id.lvChatContent);
        edtChat = (EditText) findViewById(R.id.edtChat);
        btnSend = (Button) findViewById(R.id.btnSend);
    }

    @Override
    protected void initListeners() {
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        List<ChatItem> listItems = new ArrayList<>();
        adapter = new ChatAdapter(this, listItems);
        lvChatContent.setAdapter(adapter);
    }

    private void initChatManager() {
        String email = getIntent().getExtras().getString(KEY_EMAIL);
        String password = getIntent().getExtras().getString(KEY_PASSWORD);
        User user = new User(email, password);
        chatManager = new XMPPChatManager(this, user);
        chatManager.setIncomingChatCallback(this);
        chatManager.initXMPPConnection();
        chatManager.connectToChatServerAndLogin();
    }

    @Override
    public void onClick(View view) {
        sendingMessage = edtChat.getText().toString().trim();
        edtChat.setText(null);
        chatManager.sendMessage(sendingMessage);
    }

    @Override
    protected void onDestroy() {
        disconnectToChatServer();
        super.onDestroy();
    }

    private void disconnectToChatServer() {
        chatManager.setStatus(false, "Gone fishing");
        chatManager.destroy();
    }

    @Override
    public void onChatArrived(Chat chat, boolean createdLocally) {
        if (createdLocally) {
            showMyMessage();
        } else {
            chat.addMessageListener(new ChatMessageListener() {
                @Override
                public void processMessage(Chat chat, Message message) {
                    showNewMessage(chat, message);
                }
            });
        }
    }

    private void showNewMessage(final Chat chat, final Message message) {
        ExecutorManager.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Log.w("XMPPChat", "received message: " + message.getBody() + " from: " + chat.getParticipant());
                String senderEmail = message.getFrom().split("/")[0];
                User user = new User(senderEmail, null);
                ChatItem item = new ChatItem(user, message.getBody());
                adapter.add(item);
            }
        });
    }

    private void showMyMessage() {
        User user = new User("Me", null);
        ChatItem item = new ChatItem(user, sendingMessage);
        adapter.add(item);
    }
}
