/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatDelegate;
import org.linphone.LinphoneContext;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.chat.ChatActivity;
import org.linphone.contacts.ContactsActivity;
import org.linphone.core.Core;
import org.linphone.core.PayloadType;
import org.linphone.dialer.DialerActivity;
import org.linphone.history.HistoryActivity;
import org.linphone.service.LinphoneService;
import org.linphone.service.ServiceWaitThread;
import org.linphone.service.ServiceWaitThreadListener;
import org.linphone.settings.LinphonePreferences;

/** Creates LinphoneService and wait until Core is ready to start main Activity */
public class LinphoneLauncherActivity extends Activity implements ServiceWaitThreadListener {

    private LinphonePreferences mPrefs;
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = LinphonePreferences.instance();

        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (!getResources().getBoolean(R.bool.use_full_screen_image_splashscreen)) {
            setContentView(R.layout.launch_screen);
            web = (WebView) findViewById(R.id.webView);
            web.setBackgroundColor(Color.TRANSPARENT); // for gif without background
            web.loadUrl("file:///android_asset/loading.html");
        } // Otherwise use drawable/launch_screen layer list up until first activity starts
    }

    @Override
    public void onResume() {
        super.onResume();

        mPrefs = LinphonePreferences.instance();
    }

    public void setup_bbtvoip() {

        // theme dark
        mPrefs.enableDarkMode(true);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // video

        mPrefs.enableVideo(false);

        // advance background mode
        // mPrefs.setServiceNotificationVisibility(true);
        // LinphoneContext.instance().getNotificationManager().startForeground();

        // chat
        // mPrefs.setHideEmptyChatRooms(false);
        mPrefs.setHideRemovedProxiesChatRooms(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            try {

                startService(
                        new Intent()
                                .setClass(LinphoneLauncherActivity.this, LinphoneService.class));

                new ServiceWaitThread(this).start();
            } catch (IllegalStateException ise) {
                Log.e("Linphone", "Exception raised while starting service: " + ise);
            }
        }
    }

    public void setup_codec() {
        Core core = LinphoneManager.getCore();
        if (core != null) {
            for (final PayloadType pt : core.getAudioPayloadTypes()) {

                if (pt.getMimeType().equals("PCMA")) pt.enable(true);
                else if (pt.getMimeType().equals("PCMU")) pt.enable(true);
                else pt.enable(false);
            }
        }
    }

    @Override
    public void onServiceReady() {
        setup_bbtvoip();
        setup_codec();
        mPrefs.setServiceNotificationVisibility(true);
        LinphoneContext.instance().getNotificationManager().startForeground();
        LinphoneManager.getInstance().changeStatusToOnline();

        int nbAccounts = LinphonePreferences.instance().getAccountCount();

        final Class<? extends Activity> classToStart;

        if (nbAccounts < 1) {
            classToStart = LoginActivity.class;
        } else {
            if (getIntent().getExtras() != null) {
                String activity = getIntent().getExtras().getString("Activity", null);
                if (ChatActivity.NAME.equals(activity)) {
                    classToStart = ChatActivity.class;
                } else if (HistoryActivity.NAME.equals(activity)) {
                    classToStart = HistoryActivity.class;
                } else if (ContactsActivity.NAME.equals(activity)) {
                    classToStart = ContactsActivity.class;
                } else {
                    classToStart = DialerActivity.class;
                }
            } else {
                classToStart = DialerActivity.class;
            }
        }

        new Handler()
                .postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setClass(LinphoneLauncherActivity.this, classToStart);
                                if (getIntent() != null && getIntent().getExtras() != null) {
                                    intent.putExtras(getIntent().getExtras());
                                }
                                intent.setAction(getIntent().getAction());
                                intent.setType(getIntent().getType());
                                intent.setData(getIntent().getData());
                                startActivity(intent);

                                LinphoneManager.getInstance().changeStatusToOnline();
                                finish();
                            }
                        },
                        1800);
    }
}
