package button.danger.kz.dangerbutton;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    TextView info;
    String code_number;
    Button submit;
    EditText code_input;
    Context context;
    Activity activity;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    JSONObject data;
    String name;
    public static String url = "http://test10.intermake.ru/mp/index.php";

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    final Runnable okDialog = new Runnable() {
        @Override
        public void run() {
            SweetAlertDialog alertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Успешно")
                    .setContentText("Здравствуйте, " + name + "! Вы успешно авторизовались!")
                    .setConfirmText("Ок")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            System.exit(0);
                        }
                    });
            alertDialog.show();
        }
    };
    final Runnable notOkDialog = new Runnable() {
        @Override
        public void run() {
            final SweetAlertDialog alertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Ошибка")
                    .setContentText("Введеный вами код недействителен")
                    .setConfirmText("Ок")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            code_input.setText("");
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
            alertDialog.show();
        }
    };

    final Runnable networkError = new Runnable() {
        @Override
        public void run() {
            final SweetAlertDialog alertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Ошибка")
                    .setContentText("Отсутствует подключение к интернету")
                    .setConfirmText("Ок")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
            alertDialog.show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
        setContentView(R.layout.activity_main);
        activity = this;
        context = getApplicationContext();

        isAuthcChecker checker = new isAuthcChecker(context);
        info = findViewById(R.id.textView2);
        code_input = findViewById(R.id.editText);
        submit = findViewById(R.id.button);
        if (!checker.checkAuth()) {
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isOnline()){
                        code_number = code_input.getText().toString();
                        AsyncRequest request = new AsyncRequest(code_number);
                        try{
                            data = request.execute().get();
                            if(data != null){
                                try{
                                    if(!data.get("name").equals("")){
                                        Log.d("name", data.getString("name"));
                                        name = data.getString("lastname") + " " + data.getString("name") + " " + data.getString("secondname");
                                        SharedPreferences user_info = context.getSharedPreferences("button.danger.kz", Context.MODE_PRIVATE);
                                        Editor editor = user_info.edit();
                                        editor.putString("name", data.getString("name"));
                                        editor.putString("secondname", data.getString("secondname"));
                                        editor.putString("lastname", data.getString("lastname"));
                                        editor.putString("id", data.getString("id"));
                                        editor.commit();
                                        runOnUiThread(okDialog);

                                    }
                                    else{
                                        Toast.makeText(context, "Введенный вами код не верен", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                catch (JSONException e){
                                    Log.d("Exception", e.getMessage());
                                }
                            }
                            else {
                                runOnUiThread(notOkDialog);
                            }

                        }catch (InterruptedException e){
                            Log.d("Exception", e.getMessage());
                        }catch (ExecutionException e){
                            Log.d("Exception", e.getMessage());
                        }
                    }
                    else{
                        runOnUiThread(networkError);
                    }
                }
            });
        }
        else{
            startActivity(new Intent(activity, SOSActivity.class));
            finish();
        }


    }
    class AsyncRequest extends AsyncTask<String, Void, JSONObject>{

        String code = "";
        public AsyncRequest(String code){
            this.code = code;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            try{
                Log.d("SERVER-INPUT", code);
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("code", code));
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = httpClient.execute(post);
                if(response != null){
                    String resStr  = EntityUtils.toString(response.getEntity());
                    Log.d("resultServer", resStr);
                    try{
                        JSONObject result = new JSONObject(resStr);
                        return result;
                    }
                    catch (JSONException e){
                        return null;
                    }
                }
            }
            catch (ClientProtocolException e){

            }catch (IOException e){


            }
            return null;
        }
    }
    public void postCode(String code){

    }
}
