package com.tubiapp.demochatxmpp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.tubiapp.demochatxmpp.Items.ChatItem;
import com.tubiapp.demochatxmpp.R;
import com.tubiapp.demochatxmpp.abstracts.BaseArrayAdapter;
import com.tubiapp.demochatxmpp.cells.ChatCell;

import java.util.List;

/**
 * Copyright © 2015 AsianTech inc.
 * Created by Justin on 7/29/15.
 */
public class ChatAdapter extends BaseArrayAdapter<ChatItem> {
    public ChatAdapter(Context context, List<ChatItem> objects) {
        super(context, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatItem item = getItem(position);
        ChatCell cell;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_chat, parent, false);
            cell = new ChatCell(getContext(), convertView);
            convertView.setTag(cell);
        } else {
            cell = (ChatCell) convertView.getTag();
        }
        cell.setData(item);
        return convertView;
    }
}
