package edu.temple.mapchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.temple.mapchat.activities.MainActivity;

/**
 * Created by Jessica on 4/4/2018.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<ChatMessage> myMessagesList;
    private Context myContext;
    private int ITEM_TYPE_SENT = 0;
    private int ITEM_TYPE_RECEIVED = 1;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView theMessage;
        private TextView theUser;
        private View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            theMessage = v.findViewById(R.id.theMessage);
            theUser = v.findViewById(R.id.theUser);
        }
    }

    public void add(int position, ChatMessage message) {
        myMessagesList.add(position, message);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        myMessagesList.remove(position);
        notifyItemRemoved(position);
    }

    public MessagesAdapter(List<ChatMessage> myData, Context context) {
        myMessagesList = myData;
        myContext = context;
    }

    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (viewType == ITEM_TYPE_SENT) {
            v = LayoutInflater.from(myContext).inflate(R.layout.sent_msg_row, null);
        } else if (viewType == ITEM_TYPE_RECEIVED) {
            v = LayoutInflater.from(myContext).inflate(R.layout.received_msg_row, null);

        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ChatMessage message = myMessagesList.get(position);
        holder.theMessage.setText(message.getMessage());
        holder.theUser.setText(message.getSender());
    }

    @Override
    public int getItemCount() {
        return myMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (myMessagesList.get(position).getSender().equals(MainActivity.getUsername())) {
            Log.i("MessagesAdapter", "Item type sent");
            return ITEM_TYPE_SENT;
        } else {
            Log.i("MessagesAdapter", "Item type received");
            return ITEM_TYPE_RECEIVED;
        }
    }
}
