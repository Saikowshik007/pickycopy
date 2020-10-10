package com.example.pickycopy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
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
    ImageButton upload, message, rate;
    String token, userId;

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

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CUser details = userList.get(position);
        upload=holder.itemView.findViewById(R.id.imageButton2);
        message=holder.itemView.findViewById(R.id.imageButton4);
        ((userHolder) holder).bind(details);
        Toast.makeText(contex, "Unable to create user", Toast.LENGTH_SHORT);
        boolean isExpanded = userList.get(position).isExpanded();
        ((userHolder) holder).cl.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        Toast.makeText(contex, "Unable to create user", Toast.LENGTH_SHORT);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                token = userList.get(holder.getAdapterPosition()).token();
                userId = userList.get(holder.getAdapterPosition()).getUserId();
                Log.d("Id", "" + userId);
                changeStateOfItemsInLayout(holder.getAdapterPosition());
                notifyItemChanged(holder.getAdapterPosition());

            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray regArray = new JSONArray();
                regArray.put(" " + token);
                ((CMainActivity) contex).uploader(regArray);


            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                contex.startActivity(new Intent(contex, MessageListActivity.class).putExtra("userId", userId).putExtra("user", token));

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
        ConstraintLayout cl;

        public userHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            cl = itemView.findViewById(R.id.expand);
        }

        void bind(CUser message) {
            name.setText(message.getName());

        }
    }

    public void clear() {
        userList.clear();
    }

    private void changeStateOfItemsInLayout(int p) {
        for (int x = 0; x < userList.size(); x++) {
            if (x == p) {
                userList.get(x).setExpanded(!userList.get(x).isExpanded());
                continue;
            }
            userList.get(x).setExpanded(false);
            notifyItemChanged(x);
        }

    }
}