package org.linphone.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.linphone.NetworkStateReceiver;
import org.linphone.R;

public class WebviewActivity extends AppCompatActivity
        implements NetworkStateReceiver.NetworkStateReceiverListener {

    WebView webview;
    ProgressBar fade_loading;
    int load = 0, alert = 0, errorload = 0;
    AlertDialog alertDialog;

    String lasturl = "";

    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private static final int FCR = 1;

    private NetworkStateReceiver networkStateReceiver;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    String title, siteurl;
    ImageView back;
    TextView head_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {

            if (extras == null) {
                finish();
            } else {
                title = extras.getString("title");
                siteurl = extras.getString("siteurl");
            }
        } else {
            title = (String) savedInstanceState.getSerializable("title");
            siteurl = (String) savedInstanceState.getSerializable("siteurl");
        }

        setContentView(R.layout.activity_webview);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        registerReceiver(
                networkStateReceiver,
                new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        webview = (WebView) findViewById(R.id.webView);
        back = (ImageView) findViewById(R.id.back);
        head_title = (TextView) findViewById(R.id.head_title);
        head_title.setText(title);

        back.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        finish();
                    }
                });

        fade_loading = (ProgressBar) findViewById(R.id.fade_loading);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

    public void loadsite() {

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setHorizontalScrollBarEnabled(false);
        webSettings.setAllowFileAccess(true);

        webview.setWebViewClient(new Callback());

        webview.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {

                        if (!isNetworkConnected()) {

                            if (alert == 0) {
                                alert = 1;
                                Toast.makeText(
                                                getApplicationContext(),
                                                "Please check your internet connection.",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }

                        view.loadUrl(url);

                        return false;
                    }

                    @Override
                    public void onReceivedHttpError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceResponse errorResponse) {
                        // Toast.makeText(view.getContext(), "HTTP error",
                        // Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReceivedError(
                            WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        // Toast.makeText(view.getContext(), "HTTP error1"+errorCode,
                        // Toast.LENGTH_LONG).show();
                        if (errorCode == -2) {
                            lasturl = failingUrl;
                            errorload = 1;
                            view.loadUrl("file:///android_asset/nonet.html");
                        }
                    }

                    @Override
                    public void onLoadResource(WebView view, String url) {
                        // TODO Auto-generated method stub

                        if (!isNetworkConnected()) {

                            if (alert == 0) {
                                alert = 1;
                                Toast.makeText(
                                                getApplicationContext(),
                                                "Please check your internet connection.",
                                                Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                        }

                        super.onLoadResource(view, url);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {

                        // load = 1;

                        // TODO Auto-generated method stub
                        super.onPageFinished(view, url);

                        // fade_loading.setVisibility(View.GONE);
                        // webview.setVisibility(View.VISIBLE);

                    }
                });

        webview.loadUrl(siteurl);

        webview.setWebChromeClient(
                new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        if (progress == 100) { // ...page is fully loaded.
                            load = 1;
                            fade_loading.setVisibility(View.GONE);
                            // TODO - Add whatever code you need here based on web page load
                            // completion...
                        }
                    }
                });
    }

    @SuppressLint("WrongConstant")
    private boolean isNetworkConnected() {
        return ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo()
                != null;
    }

    @Override
    public void networkAvailable() {

        if (load == 0) loadsite();

        if (errorload == 1) {
            fade_loading.setVisibility(View.VISIBLE);
            errorload = 0;
            webview.loadUrl(lasturl);
        }

        if (this.alertDialog != null) {
            alert = 0;
            this.alertDialog.dismiss();
        }
    }

    @Override
    public void networkUnavailable() {

        if (alert == 0) {
            alert = 1;
            Toast.makeText(
                            getApplicationContext(),
                            "Please check your internet connection.",
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(
                WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
