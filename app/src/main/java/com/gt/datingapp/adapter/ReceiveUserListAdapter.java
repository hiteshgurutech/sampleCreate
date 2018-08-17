package com.gt.datingapp.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
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

public class ReceiveUserListAdapter extends RecyclerView.Adapter<ReceiveUserListAdapter.UserlistHolder> {

    private Context mContext;
    List<UserModel> userListModels;

    public class UserlistHolder extends RecyclerView.ViewHolder {

        public TextView txtUserName, txtUseEmail;
        public ImageView userImage;

        public UserlistHolder(View view) {
            super(view);
            txtUserName = (TextView) view.findViewById(R.id.txtUserName);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            txtUseEmail = (TextView) view.findViewById(R.id.txtUseEmail);

        }

    }

    public ReceiveUserListAdapter(Context mContext, List<UserModel> userListModels) {
        this.mContext = mContext;
        this.userListModels = userListModels;
    }

    @Override
    public UserlistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receive_user_list_layout, parent, false);

        return new UserlistHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReceiveUserListAdapter.UserlistHolder holder, int position) {

        final UserModel user = userListModels.get(position);

        Log.d("tag", "user email " + user.getEmail());
        holder.userImage.setVisibility(View.GONE);
        holder.txtUserName.setText(user.getName());

        String emailText = "Email later on " + "<a href=" + user.getEmail() + ">" + user.getEmail() + "</a>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.txtUseEmail.setText(Html.fromHtml(emailText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.txtUseEmail.setText(Html.fromHtml(emailText));
        }

        holder.txtUseEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent share = new Intent();
//                share.setAction(Intent.ACTION_SEND);
//                share.setType("application/text");
//                share.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
//                mContext.startActivity(share);

//                Intent mailClient = new Intent(Intent.ACTION_VIEW);
//                mailClient.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
//                mailClient.setAction(Intent.ACTION_SEND);
//                mailClient.setType("application/text");
//                mailClient.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
//                mContext.startActivity(mailClient);

                String mailto = "mailto:" + user.getEmail();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));
                try {
                    mContext.startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    //TODO: Handle case where no email app is available
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return userListModels.size();
    }


}
