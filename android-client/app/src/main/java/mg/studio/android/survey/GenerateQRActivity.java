package mg.studio.android.survey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class GenerateQRActivity extends AppCompatActivity {

    //问题
    ArrayList<String> questions;
    //类型
    ArrayList<String> types;
    //选项
    Map<String,ArrayList<String>> allchoices;
    //控件
    ImageView im_QR;
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    //保存或者分享
    private int choice=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_q_r);

        initView();
        //上锁
        ApplicationUtil.getInstance().addActivity(GenerateQRActivity.this);


        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("Bundle");
        //问题
        assert bundle != null;
        questions=bundle.getStringArrayList("questions");
        //类型
        types=bundle.getStringArrayList("types");
        //所有选项
        allchoices = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra("allchoices");
        //构造目标Json对象
        JSONObject mJson=buildJsonObject();
        System.out.println("---------------------");
        System.out.println(mJson.toString());


        //将Json对象转换为String字符串，并用来生成二维码
        final Bitmap bitmap=createQRCodeBitmap(mJson.toString(), 800, 800,"UTF-8","H", "1", Color.BLACK, Color.WHITE);
        //设置二维码
        im_QR.setImageBitmap(bitmap);

        //动态申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }


        //长按保存
        im_QR.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean result=saveImageToGallery(GenerateQRActivity.this,bitmap,"QR.jpg");
                if(result==true){
                    Toast.makeText(GenerateQRActivity.this,getText(R.string.saveSuccessed).toString(),Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(GenerateQRActivity.this,getText(R.string.saveFailed).toString(),Toast.LENGTH_SHORT).show();
                }
                return result;
            }
        });


    }
    //初始化页面
    void initView(){
        im_QR=findViewById(R.id.im_QR);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, getText(R.string.permissionFailed).toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }





    //生成目标son对象
    JSONObject buildJsonObject(){
        //id,时间戳
        String id= String.valueOf(System.currentTimeMillis());
        //len,长度
        int len=questions.size();
        //questions
        JSONObject mJson=new JSONObject();//最外层
        JSONObject survey=new JSONObject();
        JSONArray mQuestions=new JSONArray();
        try {
            survey.put("id", id);
            survey.put("len",len);

            for (int i=0;i<len;i++){
                JSONObject question=new JSONObject();
                question.put("type",types.get(i));
                question.put("question",questions.get(i));;


                if(types.get(i).equals("single")||types.get(i).equals("multiple")){
                    JSONArray jOptions=new JSONArray();
                    ArrayList<String> options=new ArrayList<>();
                    options=allchoices.get(questions.get(i));

                    for(int j=0;j<options.size();j++){
                        JSONObject jsonObject2=new JSONObject();
                        jsonObject2.put(String.valueOf(j+1),options.get(j));

                        jOptions.put(jsonObject2);
                    }

                    question.put("options",jOptions);

                }

                mQuestions.put(question);
            }

            survey.put("questions",mQuestions);
            mJson.put("survey",survey);

        }catch (Exception e){
            e.printStackTrace();
        }
        assert mJson!=null;
        return mJson;

    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width,int height,
                                            String character_set,String error_correction_level,
                                            String margin,int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 保存图片到指定路径
     *
     * @param context
     * @param bitmap   要保存的图片
     * @param fileName 自定义图片名称
     * @return
     */
    public static boolean saveImageToGallery(Context context, Bitmap bitmap, String fileName) {
        // 保存图片至指定路径
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(storePath, fileName);
        int i=0;
        while (file.exists()) {
            i++;
            file=new File(storePath,"QR"+i+".jpg");
        }


        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片(80代表压缩20%)
            boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            //发送广播通知系统图库刷新数据
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void myExit(View view) {
        LockUtil.getInstance().unLock(this,GenerateQRActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // unlock success
            if (resultCode == RESULT_OK) {
                //exit
                Toast.makeText( this, getText(R.string.report_exit).toString(), Toast.LENGTH_SHORT).show();
                ApplicationUtil.getInstance().exit();
            } else {
                // unlock failed
                Toast.makeText(this, getText(R.string.report_canceled).toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
