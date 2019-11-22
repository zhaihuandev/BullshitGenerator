package com.myapp.bullshitgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {

    //定义控件
    private EditText et_main_input;
    private TextView tv_main_generate,tv_main_txttitle,tv_main_content;
    private Button bt_main_copy,bt_main_output;

    private MainActivity.OnClickListener listener;

    private String article = "";//标题
    private String articleContent ="";//文章内容

    //进度条
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化视图
        initView();
    }

    //初始化视图
    private void initView() {
        et_main_input = (EditText)findViewById(R.id.et_main_input);
        tv_main_generate = (TextView)findViewById(R.id.tv_main_generate);
        tv_main_txttitle = (TextView)findViewById(R.id.tv_main_txttitle);
        tv_main_content = (TextView)findViewById(R.id.tv_main_content);
        bt_main_copy = (Button)findViewById(R.id.bt_main_copy);
        bt_main_output = (Button)findViewById(R.id.bt_main_output);

        listener = new OnClickListener();
        tv_main_generate.setOnClickListener(listener);
        bt_main_copy.setOnClickListener(listener);
        bt_main_output.setOnClickListener(listener);

        //设置等待进度条
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("请稍等...");
        pd.setIndeterminate(true);
        pd.setCancelable(false);
    }
    //点击事件的处理
    private class OnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.tv_main_generate:
                    if("".equals(et_main_input.getText().toString().trim())){
                        Toast.makeText(MainActivity.this, "输入不能为空哦！", Toast.LENGTH_LONG).show();
                    }else {
                        generate(et_main_input.getText().toString().trim());
                    }
                    break;
                case R.id.bt_main_copy:
                    if("".equals(tv_main_content.getText().toString())){
                        Toast.makeText(MainActivity.this, "还没生成文章哦！", Toast.LENGTH_LONG).show();
                    }else {
                        boolean result = copy(tv_main_content.getText().toString());
                        if(result) Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_LONG).show();
                        else Toast.makeText(MainActivity.this, "复制失败", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.bt_main_output:
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }else save();
                    break;
            }
        }
    }

    //申请权限的结果处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    save();
                }else Toast.makeText(MainActivity.this, "授权失败！", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //保存文本
    private void save() {
        if("".equals(tv_main_content.getText().toString())){
            Toast.makeText(MainActivity.this, "还没生成文章哦！", Toast.LENGTH_LONG).show();
        }else {
            pd.show();
            String filePath = getExternalCacheDir().getPath() + "/";
            System.out.println("11111111111111111111111"+filePath);
            String fileContent = tv_main_txttitle.getText().toString() + "\r\n" + tv_main_content.getText().toString();
            String fileName = tv_main_txttitle.getText().toString().trim() + ".txt";
            if(writeTxtToFile(fileContent, filePath, fileName)){
                pd.cancel();
                AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setTitle("存储成功")
                        .setMessage("文件路径：" + "Android/data/com/myapp/bullshitgenerator/" + fileName)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {//设置确定按钮
                            @Override//处理确定按钮点击事件
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();//对话框关闭。
                            }
                        }).create();
                alert.show();
            }else {
                pd.cancel();
                Toast.makeText(MainActivity.this, "储存失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    //生成文章
    private void generate(String title){
        article = title;
        String paragraph = "";
        articleContent = "";
        int size = 0;
        while (size<1500){
            int ranNum = randomNum();
            if (ranNum<50 && paragraph.length()>200){
                paragraph =addParagraph(paragraph);
                articleContent = articleContent +paragraph;
                paragraph = "";
            }else if (ranNum<20){
                String sentence = getSaying();//来点名人名言
                size = size+sentence.length();
                paragraph = paragraph+sentence;
            }else {
                String sentence = getExpound();//来点论述
                size = size+sentence.length();
                paragraph = paragraph+sentence;
            }
        }
        paragraph =addParagraph(paragraph);
        articleContent = articleContent +paragraph;
        tv_main_txttitle.setText(article);
        tv_main_content.setText(articleContent);
    }

    //随机获取一条句子 传入列表
    private String randomSentence(String[] list){
        int num = (int) Math.floor( Math.random() * list.length );
        return list[num];
    }

    //获取随机数
    private int randomNum(){
        int max =100;
        int mix = 0;
        int num = (int) (Math.random()*( max - mix ) + mix);
        return num;
    }

    //修改名人名言
    private String getSaying(){
        String saying  =randomSentence(TxtUtils.sayings);
        saying= saying.replace("曾经说过", randomSentence(TxtUtils.before) );
        saying= saying.replace("这不禁令我深思", randomSentence(TxtUtils.after) );
        return saying;
    }

    //修改论述
    private String getExpound(){
        String sentence  =randomSentence(TxtUtils.expound);
        sentence= sentence.replace("主题", article);
        return sentence;
    }

    //添加段落
    private String addParagraph(String paragraph){
        int length = paragraph.length();
        paragraph = paragraph.substring(0,length-2);
        paragraph = "        "+paragraph +"。 "+"\n";
        return paragraph;
    }

    /**
     * 复制内容到剪切板
     *
     * @param copyStr
     * @return
     */
    private boolean copy(String copyStr) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 将字符串写入到文本文件中
    public boolean writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写,一个文件写一次，这个好像没必要
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                System.out.println(strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
            return false;
        }
        return true;
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }else {
                //删除旧文件，创建新文件
                file.delete();
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }
    }
}
