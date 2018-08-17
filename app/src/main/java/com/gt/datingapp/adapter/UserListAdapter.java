package com.gt.datingapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gt.datingapp.R;
import com.gt.datingapp.model.User;

import java.util.List;

/**
 * Created by Hitesh on 18/10/17.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserlistHolder> {

    private Context mContext;
    List<User> userListModels;
    AdapterImagedapter imageListener;

    public class UserlistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView txtUserName;
        public ImageView leftImage1;

        public UserlistHolder(View view) {
            super(view);
            txtUserName = (TextView) view.findViewById(R.id.txtUserName);
            leftImage1 = (ImageView) view.findViewById(R.id.leftImage1);

            txtUserName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            imageListener.onImageClick(getAdapterPosition(), userListModels.get(getAdapterPosition()));

        }
    }

    public UserListAdapter(Context mContext, List<User> userListModels, AdapterImagedapter imageListener) {
        this.mContext = mContext;
        this.userListModels = userListModels;
        this.imageListener = imageListener;
    }

    @Override
    public UserlistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_layout, parent, false);

        return new UserlistHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserListAdapter.UserlistHolder holder, int position) {

        final User user = userListModels.get(position);

        holder.txtUserName.setText(user.getName());
        holder.leftImage1.setVisibility(View.GONE);
        //  Picasso.with(mContext).load(Constant.IMAGE_URL + user.getProfilePhoto()).placeholder(R.mipmap.ic_launcher).fit().centerCrop().into(holder.leftImage1);

    }

    @Override
    public int getItemCount() {
        return userListModels.size();
    }

    public interface AdapterImagedapter {

        public void onImageClick(int position, User user);

        // pass view as argument or whatever you want.
    }
}
