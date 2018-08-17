package com.gt.datingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
 * Created by Hitesh on 18/10/17.
 */

public class SendUserListAdapter extends RecyclerView.Adapter<SendUserListAdapter.UserlistHolder> {

    private Context mContext;
    List<UserModel> userListModels;

    public class UserlistHolder extends RecyclerView.ViewHolder {

        public TextView txtUserName, txtUseEmail;
        public ImageView leftImage1;

        public UserlistHolder(View view) {
            super(view);
            txtUserName = (TextView) view.findViewById(R.id.txtUserName);
            leftImage1 = (ImageView) view.findViewById(R.id.leftImage1);
            txtUseEmail = (TextView) view.findViewById(R.id.txtUseEmail);
        }

    }

    public SendUserListAdapter(Context mContext, List<UserModel> userListModels) {
        this.mContext = mContext;
        this.userListModels = userListModels;
    }

    @Override
    public UserlistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.send_user_list_layout, parent, false);

        return new UserlistHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SendUserListAdapter.UserlistHolder holder, int position) {

        final UserModel user = userListModels.get(position);

        holder.leftImage1.setVisibility(View.GONE);

        holder.txtUserName.setText(user.getName());
        holder.txtUseEmail.setText("Call later on " + user.getPhone());

        holder.txtUseEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+user.getPhone()));
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userListModels.size();
    }

}
