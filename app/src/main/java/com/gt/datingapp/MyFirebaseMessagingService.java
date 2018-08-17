/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gt.datingapp;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.gt.datingapp.constant.Constant;
import com.gt.datingapp.model.AcceptRequestModel;
import com.gt.datingapp.model.NotificationModel;
import com.gt.datingapp.model.SendRequestModel;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private SharedPreferences sharedPreferences;
    private String user_id;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Check if message contains a data payload.

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data : " + remoteMessage.getData().toString());

            NotificationModel model = new NotificationModel();

            SendRequestModel sendRequestModel = new SendRequestModel();
            AcceptRequestModel acceptRequestModel = new AcceptRequestModel();

            model.setType(remoteMessage.getData().get("type"));
            model.setData(remoteMessage.getData().get("data"));

            if (model.getType().equalsIgnoreCase("Send")) {
                try {
                    JSONObject object = new JSONObject(remoteMessage.getData().get("message").toString());
                    sendRequestModel.setUser_id(object.getString("user_id"));

                    user_id = object.getString("request_id");

                    sendRequestModel.setName(object.getString("name"));
                    sendRequestModel.setProfile_photo(object.getString("profile_photo"));
                    sendRequestModel.setEmail(object.getString("email"));
                    sendRequestModel.setPhone(object.getString("phone"));
                    sendRequestModel.setRequest_id(object.getString("request_id"));
                    model.setSendRequestModel(sendRequestModel);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (model.getType().equalsIgnoreCase("Accept")) {
                try {
                    JSONObject object = new JSONObject(remoteMessage.getData().get("message").toString());

                    user_id = object.getString("request_id");

                    acceptRequestModel.setUser_id(object.getString("user_id"));
                    acceptRequestModel.setName(object.getString("name"));
                    acceptRequestModel.setProfile_photo(object.getString("profile_photo"));
                    acceptRequestModel.setEmail(object.getString("email"));
                    acceptRequestModel.setPhone(object.getString("phone"));
                    acceptRequestModel.setRequest_id(object.getString("request_id"));
                    acceptRequestModel.setAccept_type(object.getString("accept_type"));
                    model.setAcceptRequestModel(acceptRequestModel);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!isAppIsInBackground(getApplicationContext())) {
                if (sharedPreferences.getBoolean("isLogin", false)) {
                    if (sharedPreferences.getString("user_id", "").equalsIgnoreCase(user_id)) {
                        Intent intent = new Intent(Constant.ACTION_MESSAGE_RECEIVE);
                        if (model.getType().equals("Send")) {
                            intent = new Intent(Constant.ACTION_REQUEST_SEND);
                        } else if (model.getType().equals("Accept")) {
                            intent = new Intent(Constant.ACTION_REQUEST_RECEIVE);
                        }
                        intent.putExtra("notification", model);
                        sendBroadcast(intent);
                    }
                }
//                if (sharedPreferences.getBoolean("isLogin", false)) {
//                    if (sharedPreferences.getString("user_id", "").equalsIgnoreCase(user_id)) {
//                        String message = (remoteMessage.getData().get("data"));
//                        sendNotification(message, model);
//                    }
//                }
            } else {
                if (sharedPreferences.getBoolean("isLogin", false)) {
                    if (sharedPreferences.getString("user_id", "").equalsIgnoreCase(user_id)) {
                        String message = (remoteMessage.getData().get("data"));
                        sendNotification(message, model);
                    }
                }
            }
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                sendNotification(remoteMessage.getNotification().getBody(), model);
            }
        }

    }

    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.

    private void sendNotification(String message, NotificationModel notificationModel) {

        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notification", notificationModel);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, m /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(m /* ID of notification */, notificationBuilder.build());

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */

    private void sendNotificationSend(String messageBody, SendRequestModel sendRequestModel) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationAccept(String messageBody, AcceptRequestModel acceptRequestModel) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public static boolean isAppSentToBackground(final Context context) {

        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();// get the top fore ground activity
            PackageManager pm = context.getPackageManager();
            PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
                    .loadLabel(pm).toString();

            Log.e("", foregroundTaskAppName + "----------" + foregroundTaskPackageName);
            if (!foregroundTaskAppName.equals(context.getResources().getString(R.string.app_name))) {
                return true;
            }
        } catch (Exception e) {
            Log.e("isAppSentToBackground", "" + e);
        }
        return false;
    }


    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
