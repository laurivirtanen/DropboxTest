package com.example.k8455.dropboxtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.fileproperties.DbxUserFilePropertiesRequests;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DownloadFileTask extends Activity {

    DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
    DbxClientV2 client;
    Exception mException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file_task);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                PackageManager.PERMISSION_GRANTED) {
            //do the things} else {
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    1);}

        Button authb = (Button) findViewById(R.id.authButton);

        try{
            authb.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Auth.startOAuth2Authentication(DownloadFileTask.this,getString(R.string.app_key));

                }
            });
        }catch (Exception ex){
            Log.d("auth error: ", ex.getMessage());
        }

    }


    @Override
    protected void onResume(){
        super.onResume();
        String ACCESS_TOKEN = "";
        try{
            client = new DbxClientV2(config, Auth.getOAuth2Token());
            ACCESS_TOKEN = Auth.getOAuth2Token();

        }catch (Exception ex){
            Log.d("Lollo", ex.getMessage());
        }
            if (ACCESS_TOKEN.isEmpty()) {

                findViewById(R.id.authButton).setVisibility(View.VISIBLE);
                findViewById(R.id.upload).setVisibility(View.GONE);
                findViewById(R.id.download).setVisibility(View.GONE);
                findViewById(R.id.button).setVisibility(View.GONE);
                findViewById(R.id.textView).setVisibility(View.GONE);
                findViewById(R.id.textView2).setVisibility(View.GONE);
                findViewById(R.id.scrollView).setVisibility(View.GONE);
            } else {

                new PrintDropBoxFolders().execute();
                findViewById(R.id.authButton).setVisibility(View.GONE);
                findViewById(R.id.upload).setVisibility(View.VISIBLE);
                findViewById(R.id.download).setVisibility(View.VISIBLE);
                findViewById(R.id.button).setVisibility(View.VISIBLE);
                findViewById(R.id.textView).setVisibility(View.VISIBLE);
                findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
            }
    }



    private Button asynb;
    private EditText uploadtext;
    private EditText imageToDownload;
    private TextView tv;
    private String text;
    private String downloadFile;
    private String files;




    public void asyncTaskClicked(View view){
        asynb = (Button) findViewById(R.id.button);
        asynb.setEnabled(false);
        uploadtext = (EditText) findViewById(R.id.upload);
        imageToDownload = (EditText) findViewById(R.id.download);
        text = uploadtext.getText().toString();
        downloadFile = imageToDownload.getText().toString();
        new DropboxTest().execute();
    }


    private class PrintDropBoxFolders extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {


            return PrintStuff();
        }
        @Override
        protected void onPostExecute(String parameters) {
            tv = (TextView) findViewById(R.id.textview);
            tv.setText(files + "\n");
        }

        public String PrintStuff(){
            try{

                ListFolderResult result = client.files().listFolder("/KUVIA/");
                files = "";
                while (true) {

                    for (Metadata metadata : result.getEntries()) {
                        files += "\n"+metadata.getPathDisplay();
                    }

                    if (!result.getHasMore()) {
                        break;
                    }
                }
                return files;
            }catch (DbxException e){
                System.out.println(e);
            }
            return "error";
        }
    }



    private class DropboxTest extends AsyncTask<FileMetadata, Void , File> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected File doInBackground(FileMetadata... params) {
            //Uploader();
            Downloader();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... parameters) {
        }

        @Override
        protected void onPostExecute(File parameters) {
            asynb.setEnabled(true);
            tv = (TextView) findViewById(R.id.textview);
            tv.setText(files += "Downloaded: " + downloadFile);

        }

        @Override
        protected void onCancelled() {

        }
    }

    public void Downloader(){
        try{
            Metadata metadata = client.files().getMetadata("/KUVIA/"+downloadFile);
            System.out.println("Testing" + metadata);

            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File file = new File(path,metadata.getName());

            OutputStream outputStream = new FileOutputStream(file);

            client.files()
                    .download(metadata.getPathLower())
                    .download(outputStream);
            outputStream.close();



        }catch(DbxException | IOException e){
            Log.e("Error",e.getMessage());
            mException = e;
        }finally {
            Log.d("END","Testausta");
        }
    }



    public void Uploader()  {
        try {


            String filename = text+".txt";
            String string = "Hello world!";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            File file = new File(this.getFilesDir(), filename);

            //Upload test
            try (InputStream in = new FileInputStream(file)) {
                FileMetadata metadata = client.files().uploadBuilder(filename)
                        .uploadAndFinish(in);

                in.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }


        } catch (DbxException e){
            mException = e;
            System.out.println(e.toString());
        }

    }



}