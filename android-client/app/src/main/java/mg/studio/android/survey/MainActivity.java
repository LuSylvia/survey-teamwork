package mg.studio.android.survey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Intent MtoC;
    CheckBox ac;
    Button btn_start;
    Button btn_changeLanguage;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        ac = (CheckBox) findViewById(R.id.accept);
        btn_start=(Button)findViewById(R.id.btn_start);
        btn_changeLanguage=findViewById(R.id.btn_changeLanguage);

        MtoC = new Intent(MainActivity.this, ChooseActivity.class);
        ApplicationUtil.getInstance().addActivity(MainActivity.this);
        ac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    btn_start.setVisibility(View.VISIBLE);
                }else{
                    btn_start.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    //Users can enter the questionnaire survey after confirming the terms
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void start(View view) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (ac.isChecked()) {
            startActivity(MtoC);
        } else {
            AlertDialog accept = new AlertDialog.Builder(this)
                    .setMessage(getText(R.string.main_tips).toString())
                    .setPositiveButton(getText(R.string.OK).toString(), null)
                    .create();
            accept.show();
        }
    }

    public void myClick(View view) {
        if(view.getId()==R.id.btn_changeLanguage){
            //默认切英文
            Locale myLocale = Locale.ENGLISH;
            if(btn_changeLanguage.getText().toString().equals("Chinese->English")){
                //中文环境，切英文
                myLocale=Locale.ENGLISH;
            }else if(btn_changeLanguage.getText().toString().equals("英->中")){
                //英文环境，切中文
                myLocale=Locale.SIMPLIFIED_CHINESE;
            }

            set(myLocale);
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    //设置语言
    private void set(Locale myLocale) {
        // 本地语言设置
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}


