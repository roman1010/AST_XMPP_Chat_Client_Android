package com.tubiapp.demochatxmpp.utils.interfaces;

import org.jivesoftware.smack.chat.Chat;

/**
 * Copyright © 2015 AsianTech inc.
 * Created by Justin on 7/30/15.
 */
public interface IncomingChatCallback {
    void onChatArrived(Chat chat, boolean createdLocally);
}
