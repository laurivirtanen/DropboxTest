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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

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
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file_task);
        context = this;

        //really bad permission check, fix if time
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            //do the things} else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        Button authb = (Button) findViewById(R.id.authButton);

        try {
            authb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Auth.startOAuth2Authentication(DownloadFileTask.this, getString(R.string.app_key));

                }
            });
        } catch (Exception ex) {
            Log.d("auth error: ", ex.getMessage());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        String ACCESS_TOKEN = "";
        try {
            client = new DbxClientV2(config, Auth.getOAuth2Token());
            ACCESS_TOKEN = Auth.getOAuth2Token();

        } catch (Exception ex) {
            Log.d("Access token failed", ex.getMessage());
        }


        ///Shows or hides UI depending on login success
        if (ACCESS_TOKEN.isEmpty()) {
            findViewById(R.id.authButton).setVisibility(View.VISIBLE);
            findViewById(R.id.uploadButton).setVisibility(View.GONE);
            findViewById(R.id.button).setVisibility(View.GONE);
            findViewById(R.id.listView).setVisibility(View.GONE);
        } else {
            new PrintDropBoxFolders().execute();
            findViewById(R.id.authButton).setVisibility(View.GONE);
            findViewById(R.id.uploadButton).setVisibility(View.VISIBLE);
            findViewById(R.id.button).setVisibility(View.VISIBLE);
            findViewById(R.id.listView).setVisibility(View.VISIBLE);
        }
    }




    private Button asynb;
    //private EditText uploadtext;
    //private String text;
    private String downloadFile;
    private ArrayList<String> files;
    private String folderName = "";
    //ArrayAdapter<String> adapter;
//    private Button homeButton;
////
//    public void homeButton(View view){
//        homeButton = (Button) findViewById(R.id.homeButton);
//        homeButton.setEnabled(false);
//        folderName = "";
//        new PrintDropBoxFolders().execute();
//    }



    public void asyncTaskClicked(View view) {
        asynb = (Button) findViewById(R.id.button);
        asynb.setEnabled(false);
        new DropboxTest().execute();
        new PrintDropBoxFolders().execute();
    }


    private class PrintDropBoxFolders extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {


        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            return PrintStuff();
        }

        @Override
        protected void onPostExecute(final ArrayList<String> parameters) {

            try {
                DropboxAdapter adapter = new DropboxAdapter(context, parameters);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String item = parameters.get(position);

                        //Bad check for file, fix if time
                        if (item.contains(".")) {
                            downloadFile = item;
                            Log.d("Onclick item", item);
                        } else {
                            downloadFile = null;
                            folderName = item + "/";
                            Log.d("Onclick folder", folderName);
                        }

                    }
                });
            }catch (Exception e){
                Log.d("Error","**** ERROR ****");
            }
        }

        public ArrayList<String> PrintStuff() {
            try {

                ListFolderResult result = client.files().listFolder(folderName);
                files = new ArrayList<String>();
                while (true) {

                    for (Metadata metadata : result.getEntries()) {
                        files.add(metadata.getPathDisplay());
                    }

                    if (!result.getHasMore()) {
                        break;
                    }
                }
                return files;
            } catch (DbxException e) {
                Log.d("Print Error", e.getMessage());
            }
            return files;
        }
    }


    //**********
    // DOWNLOADER
    // DOWNLOADER
    // DOWNLOADER
    //**********
    private class DropboxTest extends AsyncTask<String, Void, File> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected File doInBackground(String... params) {

            //**********
            // DOWNLOADER
            // DOWNLOADER
            // DOWNLOADER
            //**********

            try {

                if(downloadFile != null) {
                    Log.d("filename", downloadFile);
                    Metadata metadata = client.files().getMetadata(downloadFile);
                    File path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS);
                    File file = new File(path, metadata.getName());
                    //Log.d("Test", file.getName());

                    OutputStream outputStream = new FileOutputStream(file);
                    client.files()
                            .download(metadata.getPathLower())
                            .download(outputStream);
                    outputStream.close();

                    Log.d("Downloaded file", file.getAbsolutePath());
                    return file;
                }



            } catch (DbxException | IOException e) {
                Log.e("Error", e.getMessage());
                mException = e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... parameters) {
        }

        @Override
        protected void onPostExecute(File parameters) {
            asynb.setEnabled(true);
            if(parameters != null){
                Toast.makeText(context,"--- Downloaded ---\n"+parameters.getName(),Toast.LENGTH_LONG).show();
            }
           // homeButton.setEnabled(true);

        }

        @Override
        protected void onCancelled() {

        }
    }


    //Browse phone files through intent
    //Browse phone files through intent
    //Browse phone files through intent
    static final int REQUEST_FILE_GET = 1;
    public void searchFile(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_FILE_GET);
        } else {
            Toast.makeText(getApplicationContext(), "Not able to search files", Toast.LENGTH_SHORT).show();
        }

    }


    //UPLOAD file browser return
    //UPLOAD file browser return
    //UPLOAD file browser return
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE_GET && resultCode == RESULT_OK) {
//            Bitmap thumbnail = data.getParcelableExtra("data");
//            String filepath = data.getData().getPath();
            //Log.d("Filepath", filepath);
            //Log.d("URI:", filepathUri.toString());
            //Uri filepathUri = data.getData();

            //*** SEND FILE URI TO UPLOAD ***//
            new UploadDropbox().execute(data.getData());

        }
    }

    public void asyncUpload(View view) {

        asynb = (Button) findViewById(R.id.uploadButton);
        searchFile(view);
        asynb.setEnabled(false);
    }


    //Uploader
    //Uploader
    //Uploader
    //Uploader
    //Uploader
    private class UploadDropbox extends AsyncTask<Uri, Void, FileMetadata> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected FileMetadata doInBackground(Uri... params) {


            String tempFolder = "/";
            if(!folderName.isEmpty()){
                tempFolder = folderName;
            }
            File localFile = UriHelpers.getFileForUri(getApplicationContext(), params[0]);
            if (localFile != null) {
                //String remoteFolderPath = params[1];

                String remoteFileName = localFile.getName();
                try {
                    InputStream inputStream = new FileInputStream(localFile);
                    {
                        return client.files().uploadBuilder(tempFolder + remoteFileName)
                                .withMode(WriteMode.OVERWRITE)
                                .uploadAndFinish(inputStream);
                    }

                } catch (DbxException | IOException ex) {
                    Log.d("Uploader ERROR", ex.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... parameters) {
        }

        @Override
        protected void onPostExecute(FileMetadata parameters) {
            asynb.setEnabled(true);
            if(parameters != null){
            Toast.makeText(context,"--- Uploaded ---\n"+parameters.getName(),Toast.LENGTH_LONG).show();
            }


        }

        @Override
        protected void onCancelled() {

        }
    }

}




