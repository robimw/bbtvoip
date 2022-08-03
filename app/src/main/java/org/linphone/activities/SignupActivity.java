package org.linphone.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import org.linphone.R;

public class SignupActivity extends AppCompatActivity {

    Button btnback, btnsignup;
    EditText fullname, email, pass, pass1;
    String user_fullname = "", user_email = "", user_pass = "", user_pass1 = "";
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btnback = (Button) findViewById(R.id.btnback);
        btnsignup = (Button) findViewById(R.id.btnsignup);
        fullname = (EditText) findViewById(R.id.txtName);
        email = (EditText) findViewById(R.id.txtEmail);
        pass = (EditText) findViewById(R.id.txtPass);
        pass1 = (EditText) findViewById(R.id.txtpass1);

        btnback.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

        btnsignup.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        signup();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @SuppressLint("WrongConstant")
    private boolean isNetworkConnected() {
        return ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo()
                != null;
    }

    public enum PasswordStrength {
        WEAK(0, Color.RED),
        MEDIUM(1, Color.argb(255, 220, 185, 0)),
        STRONG(2, Color.GREEN),
        VERY_STRONG(3, Color.BLUE);

        // --------REQUIREMENTS--------
        static int REQUIRED_LENGTH = 8;
        static int MAXIMUM_LENGTH = 100;
        static boolean REQUIRE_SPECIAL_CHARACTERS = true;
        static boolean REQUIRE_DIGITS = true;
        static boolean REQUIRE_LOWER_CASE = true;
        static boolean REQUIRE_UPPER_CASE = true;

        int resId;
        int color;

        PasswordStrength(int resId, int color) {
            this.resId = resId;
            this.color = color;
        }

        public int getValue() {
            return resId;
        }

        public int getColor() {
            return color;
        }

        public static PasswordStrength calculateStrength(String password) {
            int currentScore = 0;
            boolean sawUpper = false;
            boolean sawLower = false;
            boolean sawDigit = false;
            boolean sawSpecial = false;

            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);

                if (!sawSpecial && !Character.isLetterOrDigit(c)) {
                    currentScore += 1;
                    sawSpecial = true;
                } else {
                    if (!sawDigit && Character.isDigit(c)) {
                        currentScore += 1;
                        sawDigit = true;
                    } else {
                        if (!sawUpper || !sawLower) {
                            if (Character.isUpperCase(c)) sawUpper = true;
                            else sawLower = true;
                            if (sawUpper && sawLower) currentScore += 1;
                        }
                    }
                }
            }

            if (password.length() > REQUIRED_LENGTH) {
                if ((REQUIRE_SPECIAL_CHARACTERS && !sawSpecial)
                        || (REQUIRE_UPPER_CASE && !sawUpper)
                        || (REQUIRE_LOWER_CASE && !sawLower)
                        || (REQUIRE_DIGITS && !sawDigit)) {
                    currentScore = 1;
                } else {
                    currentScore = 2;
                    if (password.length() > MAXIMUM_LENGTH) {
                        currentScore = 3;
                    }
                }
            } else {
                currentScore = 0;
            }

            switch (currentScore) {
                case 0:
                    return WEAK;
                case 1:
                    return MEDIUM;
                case 2:
                    return STRONG;
                case 3:
                    return VERY_STRONG;
                default:
            }

            return VERY_STRONG;
        }
    }

    public void signup() {

        if (!isNetworkConnected()) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please check your internet connection first!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        user_fullname = fullname.getText().toString();
        user_email = email.getText().toString();
        user_pass = pass.getText().toString();
        user_pass1 = pass1.getText().toString();

        if (user_fullname.isEmpty()) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please enter your full name.",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (user_email.isEmpty()) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please enter your email address.",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (!user_email.matches(emailPattern)) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please enter a valid email address.",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (user_pass.isEmpty()) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please enter your password.",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (PasswordStrength.calculateStrength(user_pass).getValue()
                < PasswordStrength.STRONG.getValue()) {
            Toast.makeText(
                            getApplicationContext(),
                            "Password should be at least 8 characters in length and should include at least one upper case letter, one number, and one special character.",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (user_pass1.isEmpty()) {

            Toast.makeText(
                            getApplicationContext(),
                            "Please enter your confirm password.",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        if (!user_pass.equals(user_pass1)) {
            Toast.makeText(
                            getApplicationContext(),
                            "Password and confirm password not mached!",
                            Toast.LENGTH_LONG)
                    .show();

            return;
        }

        SignupActivity.this.pDialog = new ProgressDialog(SignupActivity.this);
        SignupActivity.this.pDialog.setMessage("Processing...");
        SignupActivity.this.pDialog.setIndeterminate(false);
        SignupActivity.this.pDialog.setCancelable(false);
        SignupActivity.this.pDialog.setCanceledOnTouchOutside(false);
        SignupActivity.this.pDialog.show();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(SignupActivity.this);
        // this is the url where you want to send the request
        // TODO: replace with your own url to send request, as I am using my own localhost for this
        // tutorial
        String url = "https://sip.bbtvoip.com/api_app_signup";

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

                                        fullname.setText("");
                                        email.setText("");
                                        pass.setText("");
                                        pass1.setText("");

                                        new AlertDialog.Builder(SignupActivity.this)
                                                .setTitle("Account Created!")
                                                .setMessage(jObject.getString("message"))
                                                .setPositiveButton(
                                                        "Login Now",
                                                        new DialogInterface.OnClickListener() {

                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                Intent i =
                                                                        new Intent(
                                                                                SignupActivity.this,
                                                                                LoginActivity
                                                                                        .class);
                                                                startActivity(i);
                                                                finish();
                                                            }
                                                        })
                                                .show();

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
                                                    "Something went wrong. Please contact with support."
                                                            + e.toString(),
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
                        params.put("fullname", user_fullname.trim());
                        params.put("email", user_email.trim());
                        params.put("password", user_pass.trim());
                        params.put("cpassword", user_pass1.trim());
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
}
