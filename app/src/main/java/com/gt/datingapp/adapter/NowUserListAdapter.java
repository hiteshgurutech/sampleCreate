package com.gt.datingapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gt.datingapp.R;
import com.gt.datingapp.model.UserModel;

import java.util.List;

/**
 * Created by Hitesh on 02/04/17.
 */

public class NowUserListAdapter extends RecyclerView.Adapter<NowUserListAdapter.UserlistHolder> {

    private Context mContext;
    List<UserModel> userListModels;

    public class UserlistHolder extends RecyclerView.ViewHolder {

        public TextView txtUserName, txtUseEmail;
        public ImageView userImage;

        public UserlistHolder(View view) {
            super(view);
            txtUserName = (TextView) view.findViewById(R.id.txtUserName);
        }

    }

    public NowUserListAdapter(Context mContext, List<UserModel> userListModels) {
        this.mContext = mContext;
        this.userListModels = userListModels;
    }

    @Override
    public UserlistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.now_user_list_layout, parent, false);

        return new UserlistHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NowUserListAdapter.UserlistHolder holder, int position) {

        final UserModel user = userListModels.get(position);

     //   Log.d("tag", "user email " + user.getEmail());
     //   holder.userImage.setVisibility(View.GONE);
        holder.txtUserName.setText(user.getName()+ " asking you to talk directly now");

    }

    @Override
    public int getItemCount() {
        return userListModels.size();
    }


}
