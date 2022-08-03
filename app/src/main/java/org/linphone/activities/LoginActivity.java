package org.linphone.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.linphone.LinphoneContext;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.assistant.EchoCancellerCalibrationAssistantActivity;
import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.DialPlan;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;
import org.linphone.dialer.DialerActivity;
import org.linphone.settings.LinphonePreferences;

public class LoginActivity extends AppCompatActivity {

    String username = "", password = "";
    EditText user, pass;
    Button login, btnregister, btnforget;
    ImageButton btnqrcode;

    private ProgressDialog pDialog;
    private static final String PREFS_NAME = "BBTVOIP";
    private static final String DATA_TAG = "CALLERID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = (EditText) findViewById(R.id.txtUsername);
        pass = (EditText) findViewById(R.id.txtPassword);
        login = (Button) findViewById(R.id.btnLogin);
        btnregister = (Button) findViewById(R.id.btnregister);
        btnforget = (Button) findViewById(R.id.btnforget);
        btnqrcode = (ImageButton) findViewById(R.id.btnScanQRCode);

        btnregister.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

        btnqrcode.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(
                                        getApplicationContext(),
                                        "This feature will available soon.",
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });

        btnforget.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(getApplicationContext(), WebviewActivity.class);
                        i.putExtra("title", "Reset Password");
                        i.putExtra("siteurl", "https://sip.bbtvoip.com/forgotpassword/");
                        startActivity(i);

                        /*Intent i = new Intent(login.this, ForgetActivity.class);
                        startActivity(i);
                        finish();*/
                    }
                });

        login.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Start the Signup activity

                        check_login();
                    }
                });
    }

    @SuppressLint("WrongConstant")
    private boolean isNetworkConnected() {
        return ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo()
                != null;
    }

    public void check_login() {

        if (!isNetworkConnected()) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please check your internet connection first!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        username = user.getText().toString();
        password = pass.getText().toString();

        if (username.isEmpty()) {

            Toast.makeText(getApplicationContext(), "Enter your username!", Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (password.isEmpty()) {

            Toast.makeText(getApplicationContext(), "Enter your password!", Toast.LENGTH_LONG)
                    .show();

            return;
        }

        LoginActivity.this.pDialog = new ProgressDialog(LoginActivity.this);
        LoginActivity.this.pDialog.setMessage("Processing...");
        LoginActivity.this.pDialog.setIndeterminate(false);
        LoginActivity.this.pDialog.setCancelable(false);
        LoginActivity.this.pDialog.setCanceledOnTouchOutside(false);
        LoginActivity.this.pDialog.show();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        // this is the url where you want to send the request
        // TODO: replace with your own url to send request, as I am using my own localhost for this
        // tutorial
        String url = "https://sip.bbtvoip.com/api_app_login";

        // Request a string response from the provided URL.
        StringRequest stringRequest =
                new StringRequest(
                        Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the response string.

                                pDialog.dismiss();

                                String result = response.trim();

                                try {

                                    JSONObject jObject = new JSONObject(result);

                                    if (jObject.getString("code").equals("200")) {

                                        SharedPreferences mSettings =
                                                getApplicationContext()
                                                        .getSharedPreferences(PREFS_NAME, 0);
                                        SharedPreferences.Editor editor = mSettings.edit();
                                        editor.putString(DATA_TAG, jObject.getString("callerid"));
                                        editor.putString("USERNAME", jObject.getString("username"));
                                        editor.putString("PASSWORD", jObject.getString("password"));
                                        editor.commit();

                                        create_sip(
                                                jObject.getString("username"),
                                                jObject.getString("password"),
                                                jObject.getString("server"));

                                    } else {
                                        Toast.makeText(
                                                        getApplicationContext(),
                                                        jObject.getString("message"),
                                                        Toast.LENGTH_LONG)
                                                .show();
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(
                                                    getApplicationContext(),
                                                    "Something went wrong. Please contact with support.",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {}
                        }) {
                    // adding parameters to the request
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username.trim());
                        params.put("password", password.trim());
                        return params;
                    }
                };
        // Add the request to the RequestQueue.
        // Add the request to the RequestQueue.
        int socketTimeout = 10000; // 30 seconds - change to what you want
        RetryPolicy policy =
                new DefaultRetryPolicy(
                        socketTimeout,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        queue.add(stringRequest);
    }

    private DialPlan getDialPlanFromCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) return null;

        for (DialPlan c : Factory.instance().getDialPlans()) {
            if (countryCode.equalsIgnoreCase(c.getIsoCountryCode())) return c;
        }
        return null;
    }

    DialPlan getDialPlanForCurrentCountry() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String countryIso = tm.getNetworkCountryIso();
            return getDialPlanFromCountryCode(countryIso);
        } catch (Exception e) {
            Log.e("[Assistant] " + e);
        }
        return null;
    }

    public AccountCreator getAccountCreator() {
        return LinphoneManager.getInstance().getAccountCreator();
    }

    protected void createProxyConfigAndLeaveAssistant(boolean isGenericAccount) {
        Core core = LinphoneManager.getCore();
        boolean useLinphoneDefaultValues =
                getString(R.string.default_domain).equals(getAccountCreator().getDomain());

        if (isGenericAccount) {
            if (useLinphoneDefaultValues) {
                Log.i(
                        "[Assistant] Default domain found for generic connection, reloading configuration");
                core.loadConfigFromXml(
                        LinphonePreferences.instance().getLinphoneDynamicConfigFile());
            } else {
                Log.i("[Assistant] Third party domain found, keeping default values");
            }
        }

        ProxyConfig proxyConfig = getAccountCreator().createProxyConfig();

        if (isGenericAccount) {
            if (useLinphoneDefaultValues) {
                // Restore default values
                Log.i("[Assistant] Restoring default assistant configuration");
                core.loadConfigFromXml(
                        LinphonePreferences.instance().getDefaultDynamicConfigFile());
            } else {
                // If this isn't a sip.linphone.org account, disable push notifications and enable
                // service notification, otherwise incoming calls won't work (most probably)
                if (proxyConfig != null) {
                    proxyConfig.setPushNotificationAllowed(false);
                }
                Log.w(
                        "[Assistant] Unknown domain used, push probably won't work, enable service mode");
                LinphonePreferences.instance().setServiceNotificationVisibility(true);
                LinphoneContext.instance().getNotificationManager().startForeground();
            }
        }

        if (proxyConfig == null) {
            Log.e("[Assistant] Account creator couldn't create proxy config");
            // TODO: display error message
        } else {
            if (proxyConfig.getDialPrefix() == null) {
                DialPlan dialPlan = getDialPlanForCurrentCountry();
                if (dialPlan != null) {
                    proxyConfig.setDialPrefix(dialPlan.getCountryCallingCode());
                }
            }

            LinphonePreferences.instance().firstLaunchSuccessful();
            goToLinphoneActivity();
        }
    }

    void goToLinphoneActivity() {
        boolean needsEchoCalibration =
                LinphoneManager.getCore().isEchoCancellerCalibrationRequired();
        boolean echoCalibrationDone =
                LinphonePreferences.instance().isEchoCancellationCalibrationDone();
        Log.i(
                "[Assistant] Echo cancellation calibration required ? "
                        + needsEchoCalibration
                        + ", already done ? "
                        + echoCalibrationDone);

        Intent intent;
        if (needsEchoCalibration && !echoCalibrationDone) {
            intent = new Intent(this, EchoCancellerCalibrationAssistantActivity.class);
        } else {
            /*boolean openH264 = LinphonePreferences.instance().isOpenH264CodecDownloadEnabled();
            boolean codecFound =
                    LinphoneManager.getInstance().getOpenH264DownloadHelper().isCodecFound();
            boolean abiSupported =
                    Version.getCpuAbis().contains("armeabi-v7a")
                            && !Version.getCpuAbis().contains("x86");
            boolean androidVersionOk = Version.sdkStrictlyBelow(Build.VERSION_CODES.M);

            if (openH264 && abiSupported && androidVersionOk && !codecFound) {
                intent = new Intent(this, OpenH264DownloadAssistantActivity.class);
            } else {*/
            intent = new Intent(this, DialerActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            // }
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {}

    public void create_sip(String username, String password, String server) {

        AccountCreator accountCreator = getAccountCreator();
        accountCreator.setUsername(username);
        accountCreator.setDomain(server);
        accountCreator.setPassword(password);
        accountCreator.setDisplayName(username);
        accountCreator.setTransport(TransportType.Udp);

        createProxyConfigAndLeaveAssistant(true);
    }
}
