package button.danger.kz.dangerbutton;

import android.content.Context;
import android.content.SharedPreferences;

public class isAuthcChecker {
    Context context;

    public isAuthcChecker(Context context){
        this.context = context;
    }

    public boolean checkAuth(){
        SharedPreferences user_info = context.getSharedPreferences("button.danger.kz", Context.MODE_PRIVATE);
        if(!user_info.getString("name", "").equals("")
                && !user_info.getString("lastname", "").equals("")
                && !user_info.getString("id", "").equals("")
                && !user_info.getString("secondname", "").equals("")){
            return true;
        }
        else{
            return false;
        }
    }
}
