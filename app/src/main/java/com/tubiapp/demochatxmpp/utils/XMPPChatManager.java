package com.tubiapp.demochatxmpp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tubiapp.demochatxmpp.apis.ExecutorManager;
import com.tubiapp.demochatxmpp.apis.model.User;
import com.tubiapp.demochatxmpp.utils.interfaces.IncomingChatCallback;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Copyright © 2015 AsianTech inc.
 * Created by Justin on 7/30/15.
 */
public class XMPPChatManager {
    private static final String TAG = "XMPPChatManager";
    private static final String HOST = "172.16.20.133";
    private static final String SERVICE_NAME = "binhcq-imac.local";
    private Context context;
    private User user;
    private ChatManager chatManager;
    private AbstractXMPPConnection connection;
    private MyMessageListener messageListener;
    private IncomingChatCallback incomingChatCallback;
    private Roster roster;

    public XMPPChatManager(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    public void setIncomingChatCallback(IncomingChatCallback incomingChatCallback) {
        this.incomingChatCallback = incomingChatCallback;
    }

    public void initXMPPConnection() {
        connection = new XMPPTCPConnection(buildXMPPTCPConnectionConfig(user.getName(), user.getPassword()));
        SASLAuthentication.registerSASLMechanism(new SASLPlainMechanism());
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
    }

    private XMPPTCPConnectionConfiguration buildXMPPTCPConnectionConfig(String email, String password) {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration
                .builder()
                .setHost(HOST);
        configBuilder.setUsernameAndPassword(email, password);
        configBuilder.setServiceName(SERVICE_NAME);
        configBuilder.setDebuggerEnabled(true);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        return configBuilder.build();
    }

    public void connectToChatServerAndLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.connect();
                    Log.i("XMPPClient", "Connected to " + connection.getHost());
                    initManagersDependOnConnection();
                    connection.login();
                    Log.i("XMPPClient", "Log in to " + connection.getHost());
                    listenRosterChange();
                    listenIncomingChat();
                    setStatus(true, "I'm come back");
                } catch (SmackException | IOException | XMPPException e) {
                    e.printStackTrace();
                }
            }

            private void initManagersDependOnConnection() {
                chatManager = ChatManager.getInstanceFor(connection);
                messageListener = new MyMessageListener();
                roster = Roster.getInstanceFor(connection);
            }
        }).start();
    }

    private void listenIncomingChat() {
        org.jivesoftware.smack.chat.ChatManager chatManager = org.jivesoftware.smack.chat.ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(
                new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {
                        if (incomingChatCallback != null) {
                            incomingChatCallback.onChatArrived(chat, createdLocally);
                        }

                    }
                });
    }

    private void listenRosterChange() {
        Roster roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(new RosterListener() {
                                     public void entriesAdded(Collection<String> addresses) {
                                     }

                                     public void entriesDeleted(Collection<String> addresses) {
                                     }

                                     public void entriesUpdated(Collection<String> addresses) {
                                     }

                                     public void presenceChanged(final Presence presence) {
                                         ExecutorManager.getMainExecutor().execute(new Runnable() {
                                             @Override
                                             public void run() {
                                                 String toastMessage = "Presence changed: " + presence.getFrom() + " - " + presence;
                                                 Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
                                             }
                                         });
                                     }
                                 }

        );
    }

    public void sendMessage(String sendingMessage) {
        sendMessage(sendingMessage, getFriendEmail());
    }

    public void sendMessage(String message, String buddyJID) {
        Log.w(TAG, String.format("Sending message '%1$s' to user %2$s", message, buddyJID));
        Chat chat = chatManager.createChat(buddyJID, messageListener);
        try {
            chat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

//    public void createEntry(String user, String name) throws Exception {
//        Log.w(TAG, String.format("Creating entry for buddy '%1$s' with name %2$s", user, name));
//        Roster roster = connection.get
//        roster.createEntry(user, name, null);
//    }

    public void setStatus(boolean available, String status) {

        Presence.Type type = available ? Presence.Type.available : Presence.Type.unavailable;
        Presence presence = new Presence(type);

        presence.setStatus(status);
        try {
            connection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    public String getFriendEmail() {
        if (user.getEmail().contains("admin")) {
            return "foo@" + SERVICE_NAME;
        }
        return "admin@" + SERVICE_NAME;
    }

    class MyMessageListener implements ChatMessageListener {
        @Override
        public void processMessage(Chat chat, Message message) {
            String from = message.getFrom();
            String body = message.getBody();
            Log.w(TAG, String.format("Received message '%1$s' from %2$s", body, from));
        }
    }

    public void loadOldMessage() {
//        connection.pre
    }

    public void getContactList() {
        Set<RosterEntry> rosterEntries = roster.getEntries();

        try {
            if (!roster.isLoaded()) {
                roster.reloadAndWait();
            }
        } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Presence presence = roster.getPresence(user.getEmail());
    }
}
