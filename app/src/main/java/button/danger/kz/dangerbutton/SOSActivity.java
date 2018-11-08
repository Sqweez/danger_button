package button.danger.kz.dangerbutton;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import button.danger.kz.dangerbutton.Helpers.GPSLocator;
import button.danger.kz.dangerbutton.Helpers.ServerApi;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SOSActivity extends AppCompatActivity {

    Activity activity;
    Context context;
    String name;
    String data;
    String resCancel;
    double lat;
    double lon;
    Geocoder geocoder;
    Locale locale;
    int user_id;
    TextView textView;

    final Runnable OkDialog = new Runnable() {
        @Override
        public void run() {
            final SweetAlertDialog alertDialog = new SweetAlertDialog(SOSActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Успешно")
                    .setContentText("Вызов успешно отменен")
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
    final Runnable notOkDialog = new Runnable() {
        @Override
        public void run() {
            final SweetAlertDialog alertDialog = new SweetAlertDialog(SOSActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Внимание")
                    .setContentText("Вызов не был отменен")
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        SharedPreferences user_info = context.getSharedPreferences("button.danger.kz", Context.MODE_PRIVATE);
        user_id = Integer.parseInt(user_info.getString("id", ""));
        locale = new Locale("ru");
        geocoder = new Geocoder(context, locale);
        super.onResume();
        showGPSCoordinates();
        if(isOnline()){
            ServerApi serverApi = new ServerApi(lat, lon, user_id, 0);
            try{
                String response = serverApi.execute().get();
                if(response.equals("")){
                    textView.setText(R.string.error);
                }
                else{
                    textView.setText(R.string.sos);
                }
            }catch (ExecutionException e){}
            catch (InterruptedException e){}
        }
        else{
            textView.setText(R.string.network_error);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        menu.getItem(0).setIcon(R.drawable.ic_cancel);
        menu.getItem(1).setIcon(R.drawable.ic_lpgout);
        menu.getItem(2).setIcon(R.drawable.ic_exit);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.action_logout:
                SharedPreferences user_info = context.getSharedPreferences("button.danger.kz", Context.MODE_PRIVATE);
                SharedPreferences.Editor  editor = user_info.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(activity, MainActivity.class));
                finish();
                break;
            case R.id.action_exit:
                System.exit(0);
                break;
            case R.id.action_cancel:
                final EditText editText = new EditText(this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
                SweetAlertDialog alert =new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Введите код отмены")
                        .setConfirmText("Ok")
                        .setCustomView(editText)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                int code = Integer.parseInt(editText.getText().toString());
                                ServerApi serverApi = new ServerApi(0, 0, user_id, code);
                                try{
                                    resCancel = serverApi.execute().get();
                                }catch (ExecutionException e) {resCancel = "";}
                                catch (InterruptedException e) {resCancel = "";}
                                sweetAlertDialog.dismissWithAnimation();
                                if(resCancel.equals("cancel-Ok")){
                                    runOnUiThread(OkDialog);
                                }
                                else{
                                    runOnUiThread(notOkDialog);
                                }
                            }
                        });
                        alert.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void showGPSCoordinates(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GPSLocator gpsLocator = new GPSLocator(this, locationManager);
        gpsLocator.getLocation();
        Location loc = gpsLocator.getLastBestLocation();
        try{
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        }catch (NullPointerException e){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(SOSActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(SOSActivity.this);
            }
            builder.setTitle("Внимание");
            builder.setMessage(name + ", включите в настройках своего телефона определение местоположения по всем источникам");
            builder.setCancelable(false);
            builder.setPositiveButton("Включить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(viewIntent);
                }
            });
            builder.create().show();

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        activity = this;
        context = activity.getApplicationContext();
        textView = findViewById(R.id.textView);
        SharedPreferences user_info = context.getSharedPreferences("button.danger.kz", Context.MODE_PRIVATE);
        name = user_info.getString("name", "") + " " + user_info.getString("surname","");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

    }

}
