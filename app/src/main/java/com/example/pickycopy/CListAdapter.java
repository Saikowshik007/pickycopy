package com.example.pickycopy;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableList;


import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class CListAdapter extends RecyclerView.Adapter implements Filterable {
    private List<CUser> userList;
    ImmutableList<CUser> complete;
    private Context contex;
    Dialog mDialouge;
    FloatingActionButton upload, message,rate;
    TextView name;
    String token;
    int pos;

    public CListAdapter(Context context, List<CUser> messageList) {
        userList = messageList;
        contex = context;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    @Override
    public int getItemViewType(int position) {
        CUser message = userList.get(position);

        return position;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.owners_layout, parent, false);
        return new userHolder(view);
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        final CUser details = userList.get(position);
        ((userHolder) holder).bind(details);
        Toast.makeText(contex, "Unable to create user", Toast.LENGTH_SHORT);
        mDialouge = new Dialog(contex);
        mDialouge.setContentView(R.layout.fragment);
        mDialouge.getWindow().getAttributes().windowAnimations=R.anim.zoomout;
        name = mDialouge.findViewById(R.id.textView11);
        upload = mDialouge.findViewById(R.id.imageButton2);
        message = mDialouge.findViewById(R.id.imageButton4);
        Toast.makeText(contex, "Unable to create user", Toast.LENGTH_SHORT);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setText(userList.get(holder.getAdapterPosition()).getName());
                token=userList.get(holder.getAdapterPosition()).token();
                mDialouge.show();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialouge.hide();
                JSONArray regArray = new JSONArray();
                regArray.put(" " + token);
                        ((CMainActivity) contex).uploader(regArray);


            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CUser> filteredlist = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredlist.addAll(complete);
            } else {
                String filterpattern = constraint.toString().toLowerCase().trim();
                for (CUser item : complete) {
                    if (item.getName().toLowerCase().equals(filterpattern)) {
                        filteredlist.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredlist;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            userList.addAll((Collection<? extends CUser>) results.values);
            notifyDataSetChanged();

        }
    };


    public class userHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView sample;
        public userHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
        }

        void bind(CUser message) {
            name.setText(message.getName());

        }
    }

    public void clear() {
        userList.clear();
    }


}