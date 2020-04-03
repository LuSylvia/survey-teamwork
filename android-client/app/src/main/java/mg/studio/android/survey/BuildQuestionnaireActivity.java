package mg.studio.android.survey;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

//用于构造问卷
public class BuildQuestionnaireActivity extends AppCompatActivity {
    private LinearLayout linear1;
    Button btn_add,btn_finish,btn_delete,btn_next;
    private RadioButton rb_single,rb_multichoice,rb_edit;
    EditText ed_question;
    //表示该问卷总共有多少问题
    static int totalQuestions=0;
    //存储editText
    ArrayList<EditText> editTextList=new ArrayList<>();
    //存储问题
    ArrayList<String> questions=new ArrayList<>();
    //存储类型
    ArrayList<String> types=new ArrayList<>();
    //存储所有问题的所有选项
    HashMap<String,ArrayList<String>> allChoices=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_questionnaire);
        initView();
        ApplicationUtil.getInstance().addActivity(BuildQuestionnaireActivity.this);
    }

    private void initView(){
        ed_question=findViewById(R.id.ed_question);

        linear1=findViewById(R.id.linear1);


        btn_add=findViewById(R.id.btn_add);
        btn_delete=findViewById(R.id.btn_delete);
        btn_next=findViewById(R.id.btn_next);
        btn_finish=findViewById(R.id.btn_finish);

        rb_single=findViewById(R.id.rb_single);
        rb_multichoice=findViewById(R.id.rb_multichoice);
        rb_edit=findViewById(R.id.rb_edit);

    }



    public void myClick(View view) {
        switch (view.getId()){
            case R.id.btn_add:
                if (rb_edit.isChecked()){
                    Toast.makeText(this,getText(R.string.invalid_operation).toString(),Toast.LENGTH_SHORT).show();
                    break;
                }
                EditText ed=new EditText(this);
                linear1.addView(ed);
                ed.setTextSize(18);
                ed.setHint(getText(R.string.choiceHint).toString());
                editTextList.add(ed);

                break;

            case R.id.btn_delete:
                if(rb_edit.isChecked()){
                    Toast.makeText(this,getText(R.string.invalid_operation).toString(),Toast.LENGTH_SHORT).show();
                    break;
                }

                linear1.removeView(editTextList.get(editTextList.size()-1));
                editTextList.remove(editTextList.size()-1);

                break;

            case R.id.btn_next:

                if(!addData()){
                    //非法操作，直接退出
                    break;
                }


                linear1.removeAllViewsInLayout();
                //删除editText
                editTextList.clear();
                setContentView(R.layout.activity_build_questionnaire);
                initView();


                break;

            case R.id.btn_finish:
                //最终处理，将所有选项和问题存储进json字符串

                if(!addData()){
                    //非法操作，直接退出
                    break;
                }

                Intent intent=new Intent(BuildQuestionnaireActivity.this,GenerateQRActivity.class);
                Bundle bundle=new Bundle();
                bundle.putStringArrayList("questions",  questions);
                bundle.putStringArrayList("types",types);
                intent.putExtra("allchoices",(Serializable)allChoices);
                intent.putExtra("Bundle",bundle);
                startActivity(intent);


                break;



            default:
                break;
        }



    }


    private boolean addData(){
        if(ed_question.getText().toString().equals("")){
            //题目为空，直接返回
            Toast.makeText(this,getText(R.string.invalid_operation_noTitle).toString(),Toast.LENGTH_SHORT).show();
            return false;
        }
        //类型为空，也直接返回
        if(!(rb_single.isChecked() ||rb_multichoice.isChecked()||rb_edit.isChecked() ) ){
            Toast.makeText(this,getText(R.string.invalid_operation_noType).toString(),Toast.LENGTH_SHORT).show();
            return false;
        }
        //如果类型为单选，但没有添加任何选项，直接返回
        if (rb_single.isChecked()&&editTextList.size()<1){
            Toast.makeText(this,getText(R.string.invalid_operation_singleChoice).toString(),Toast.LENGTH_SHORT).show();
            return false;
        }
        //如果类型为多选，但没有添加至少2个选项，直接返回
        if(rb_multichoice.isChecked()&&editTextList.size()<2){
            Toast.makeText(this,getText(R.string.invalid_operation_multipleChoices).toString(),Toast.LENGTH_SHORT).show();
            return false;
        }

        //题目
        questions.add(ed_question.getText().toString());
        //类型
        if(rb_single.isChecked()){
            types.add("single");
        }else if(rb_multichoice.isChecked()){
            types.add("multiple");
        }else if(rb_edit.isChecked()){
            types.add("edittext");
        }
        //填空题
        if(rb_edit.isChecked()){
            return true;

        }else {
            //选择题
            //存储单个问题的所有选项
            ArrayList<String> choices=new ArrayList<>();


            for(int i=0;i<editTextList.size();i++){
                String text=editTextList.get(i).getText().toString();
                choices.add(text);
                System.out.println((i+1)+"——"+text);
            }

            allChoices.put(ed_question.getText().toString(),choices);
            return true;
        }

    }





}
