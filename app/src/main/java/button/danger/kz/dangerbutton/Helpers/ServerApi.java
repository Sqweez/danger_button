package button.danger.kz.dangerbutton.Helpers;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

public class ServerApi extends AsyncTask<Void, Void, String> {

    double lat;
    double lon;
    int id;
    int code;
    public static String url = "http://test10.intermake.ru/mp/index.php";

    public ServerApi(double lat, double lon, int id, int code){
        this.lat = lat;
        this.lon = lon;
        this.id = id;
        this.code = code;
    }
    public List<NameValuePair> setPairs(){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.clear();
        pairs.add(new BasicNameValuePair("id", String.valueOf(id)));
        if(code == 0){
            pairs.add(new BasicNameValuePair("lat", String.valueOf(lat)));
            pairs.add(new BasicNameValuePair("lon", String.valueOf(lon)));
        }
        else{
            pairs.add(new BasicNameValuePair("cancel", String.valueOf(code)));

        }
        return pairs;
    }
    @Override
    protected String doInBackground(Void... voids) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        try{
            List<NameValuePair> pairs = setPairs();
            post.setEntity(new UrlEncodedFormEntity(pairs));
            HttpResponse response = httpClient.execute(post);
            if(response != null){
                String res = EntityUtils.toString(response.getEntity());
                Log.d("serverRes", res);
                return res;
            }
        }catch (ClientProtocolException e){
            return "";
        }catch (IOException e){
            return "";
        }
        return "";
    }

}
