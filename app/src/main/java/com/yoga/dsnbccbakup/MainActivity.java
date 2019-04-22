package com.yoga.dsnbccbakup;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public String PathDB = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.solainteractive.plantation/DB";
    public String PathImages = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.solainteractive.plantation/Image";
    public DocumentFile PathSourceImage = DocumentFile.fromFile(new File(PathImages));
    public DocumentFile PathSourceImages[] = PathSourceImage.listFiles();
    public DocumentFile PathSourceDB = DocumentFile.fromFile(new File(PathDB));

    String DBFileName = "DB";
    String MimeDB = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(DBFileName));

    public InputStream fromInputStream = null;
    public OutputStream toOutputStream = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn = findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener(){
            public  void onClick(View v){
               if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                   System.out.println("Permission Not Granted");
                   ActivityCompat.requestPermissions(MainActivity.this,
                           new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                           1);
               }else{
                   saveFile(v);
               }
            }

        });
    }

    public void saveFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // intent.addCategory(Intent);
        // intent.setType("*/*");


        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 200){
                if(resultData != null){
                    DocumentFile sdCardPath = DocumentFile.fromTreeUri(this, resultData.getData());


                    if(sdCardPath.findFile("backupbcc") == null){
                        System.out.println("Folder Not Found");
                        sdCardPath.createDirectory("backupbcc");
                    }else{
                        System.out.println("Folder  Found");

                    }

                    if(sdCardPath.findFile("backupbcc").findFile("Image") == null){
                        sdCardPath.findFile("backupbcc").createDirectory("Image");
                    }else{
                        sdCardPath.findFile("backupbcc").findFile("Image").delete();
                        sdCardPath.findFile("backupbcc").createDirectory("Image");
                    }




                    if(!PathSourceDB.exists()){
                        Toast.makeText(MainActivity.this, "DB Tidak di temukan !", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    //delete exisiting db
                    if(sdCardPath.findFile("backupbcc").findFile("DB") != null){
                        sdCardPath.findFile("backupbcc").findFile("DB").delete();
                    }

                    try{
                        fromInputStream = getContentResolver().openInputStream(PathSourceDB.getUri());
                        toOutputStream = getContentResolver().openOutputStream(sdCardPath.findFile("backupbcc")
                                .createFile(MimeDB, DBFileName).getUri());

                        int DEFAULT_BUFFER_SIZE = 1024 * 4;
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int n;
                        while (-1 != (n = fromInputStream.read(buffer))) {
                            toOutputStream.write(buffer, 0, n);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error Backup File DB !", Toast.LENGTH_SHORT).show();
                        return;
                    }


                  //save image
                    for (DocumentFile doc:PathSourceImages) {
                        try{

                            String FileName = doc.getName();
                            String MimeFile = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                    MimeTypeMap.getFileExtensionFromUrl(FileName));

                            fromInputStream = getContentResolver().openInputStream(doc.getUri());
                            toOutputStream = getContentResolver().openOutputStream(sdCardPath
                                            .findFile("backupbcc")
                                            .findFile("Image")
                                            .createFile(MimeFile, FileName).getUri());


                            int DEFAULT_BUFFER_SIZE = 1024 * 4;
                            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                            int n;
                            while (-1 != (n = fromInputStream.read(buffer))) {
                                toOutputStream.write(buffer, 0, n);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error Backup  Foto !", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }



                    Toast.makeText(MainActivity.this, "Backup  Data Berhasil!!", Toast.LENGTH_SHORT).show();


                }
            }
        }

    }
}
