package com.example.appspecificstorage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText userNameEditText;
    CheckBox externalStorageChkBox;
    CheckBox cacheChkBox;
    Button saveBtn;
    Context context;
    String userName = "";
    File externalFileRef = null;
    File cacheFileRef = null;
    private static final String FILE_NAME = "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameEditText = findViewById(R.id.userNameEditText);
        externalStorageChkBox = findViewById(R.id.externalStorageId);
        cacheChkBox = findViewById(R.id.cacheChkBoxId);
        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);
        context = getApplicationContext();
        if(isUserNameFileExistInInternalStorage()) {
            try {
                FileInputStream fis = context.openFileInput(FILE_NAME);
                userName = readUserNameFromInputStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if(isUserNameFileExistInExternalStorage()) {
            try {
                FileInputStream fis = new FileInputStream(externalFileRef);
                userName = readUserNameFromInputStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if(isUserNameFileExistInCache()) {
            try {
                FileInputStream fis = new FileInputStream(cacheFileRef);
                userName = readUserNameFromInputStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(!userName.isEmpty()) {
            userNameEditText.setText(userName);
        }
    }

    @Override
    public void onClick(View v) {
        userName = userNameEditText.getText().toString();
        System.out.println("User Name -> "+userName);
        System.out.println("App specific directory -> "+context.getFilesDir().getAbsolutePath());
        writeUserName();
    }

    public boolean isUserNameFileExistInInternalStorage() {
        for (String fileName : context.fileList()) {
            if(fileName.equals(FILE_NAME)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserNameFileExistInCache() {
        File tempFolder = context.getCacheDir();
        if(tempFolder.isDirectory()) {
            File[] tempFiles = tempFolder.listFiles();
            for(File tempFile: tempFiles) {
                if(tempFile.getName().contains(FILE_NAME)) {
                    cacheFileRef = tempFile;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUserNameFileExistInExternalStorage() {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);
        for (File externalFile : externalStorageVolumes) {
            if (externalFile.isDirectory()) {
                File[] files = externalFile.listFiles();
                for (File file : files) {
                    if(file.getName().equalsIgnoreCase(FILE_NAME)) {
                        externalFileRef = file;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void writeUserName() {
        if (externalStorageChkBox.isChecked() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                writeIntoExternalStorage();
            }
        } else if (cacheChkBox.isChecked()) {
            if(isUserNameFileExistInCache()) {
                writeIntoOutputStream(externalFileRef);
            } else {
                writeIntoCache();
            }
        } else {
            writeIntoInternalStorage();
        }
    }

    private void writeIntoCache() {
        try {
            File file = File.createTempFile(FILE_NAME, ".txt", context.getCacheDir());
            writeIntoOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIntoOutputStream(File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(userName.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void writeIntoExternalStorage() {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);
        for (File file : externalStorageVolumes) {
            System.out.println("file name -> "+file.getName());
            System.out.println("file absolute path -> "+file.getAbsolutePath());

            // Because of this line we have to added @RequiresApi annotation in this method
            System.out.println("is removeable -> "+Environment.isExternalStorageRemovable(file));
        }

        if (externalStorageVolumes != null && externalStorageVolumes.length > 0) {
            File directory = externalStorageVolumes[0];
            File file = new File(directory.getPath() + File.separator + FILE_NAME);
            writeIntoOutputStream(file);
        }
    }

    public void writeIntoInternalStorage() {
        try(FileOutputStream fos = context.openFileOutput(FILE_NAME, MODE_PRIVATE)) {
            fos.write(userName.getBytes());
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readUserNameFromInputStream(FileInputStream fis) {
        StringBuilder sb = new StringBuilder();
        String userName = "";
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line = br.readLine();
            while(line != null) {
                sb.append(line);
                line = br.readLine();
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            userName = sb.toString();
        }
        return userName.replace("\n", "").replace("\r", "");
    }
}