package com.example.k8455.dropboxtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.renderscript.ScriptGroup;
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
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

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
        Toast.makeText(getApplicationContext(),"Logged in Successfully",Toast.LENGTH_SHORT);

        String ACCESS_TOKEN = "";
        try {
            client = new DbxClientV2(config, Auth.getOAuth2Token());
            ACCESS_TOKEN = Auth.getOAuth2Token();

        } catch (Exception ex) {
            Log.d("Lollo", ex.getMessage());
        }
        if (ACCESS_TOKEN.isEmpty()) {

            findViewById(R.id.authButton).setVisibility(View.VISIBLE);
            //findViewById(R.id.upload).setVisibility(View.GONE);
            findViewById(R.id.uploadButton).setVisibility(View.GONE);
            //findViewById(R.id.download).setVisibility(View.GONE);
            findViewById(R.id.button).setVisibility(View.GONE);
//                findViewById(R.id.textView).setVisibility(View.GONE);
//                findViewById(R.id.textView2).setVisibility(View.GONE);
            findViewById(R.id.listView).setVisibility(View.GONE);
        } else {

            new PrintDropBoxFolders().execute();
            findViewById(R.id.authButton).setVisibility(View.GONE);
            //findViewById(R.id.upload).setVisibility(View.VISIBLE);
            findViewById(R.id.uploadButton).setVisibility(View.VISIBLE);
            //findViewById(R.id.download).setVisibility(View.VISIBLE);
            findViewById(R.id.button).setVisibility(View.VISIBLE);
//                findViewById(R.id.textView).setVisibility(View.VISIBLE);
//                findViewById(R.id.textView2).setVisibility(View.VISIBLE);
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
        //uploadtext = (EditText) findViewById(R.id.upload);
        //imageToDownload = (EditText) findViewById(R.id.download);
        // text = uploadtext.getText().toString();
        //downloadFile = imageToDownload.getText().toString();
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

            DropboxAdapter adapter = new DropboxAdapter(context, parameters);
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = parameters.get(position);


                    if (item.toLowerCase().endsWith(".txt") || item.toLowerCase().endsWith(".jpg")) {
                        downloadFile = item;
                        Log.d("Onclick item", item);
                    } else {
                        downloadFile = null;
                        folderName = item + "/";
                        Log.d("Onclick folder", folderName);
                    }

                }
            });
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

    private class DropboxTest extends AsyncTask<FileMetadata, Void, File> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected File doInBackground(FileMetadata... params) {

            //DOWNLOADER

            try {

                if(downloadFile != null) {
                    Log.d("filename", downloadFile);
                    Metadata metadata = client.files().getMetadata(downloadFile);
                    File path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS);
                    Log.d("Path", path.toString());
                    File file = new File(path, metadata.getName());
                    Log.d("Test", file.getName());

                    OutputStream outputStream = new FileOutputStream(file);
                    client.files()
                            .download(metadata.getPathLower())
                            .download(outputStream);
                    outputStream.close();

//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            intent.setData(Uri.fromFile(file));
//            context.sendBroadcast(intent);
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
           // homeButton.setEnabled(true);
            //tv.setText(files += "Downloaded: " + downloadFile);

        }

        @Override
        protected void onCancelled() {

        }
    }

//Browse phone files through intent
    Uri fullPhotoUri;
    static final int REQUEST_IMAGE_GET = 1;

    public void searchFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        } else {
            Toast.makeText(getApplicationContext(), "Not able to search files", Toast.LENGTH_SHORT).show();
        }
    }

    //UPLOAD file browser return
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            String filepath = data.getData().getPath();
            Log.d("Filepath", filepath);
            fullPhotoUri = data.getData();
            Log.d("URI:", fullPhotoUri.toString());
            new UploadDropbox().execute();

        }
    }

    public void asyncUpload(View view) {

        asynb = (Button) findViewById(R.id.uploadButton);
        searchFile(view);
        asynb.setEnabled(false);
        //uploadtext = (EditText) findViewById(R.id.upload);
        //imageToDownload = (EditText) findViewById(R.id.download);
        //text = uploadtext.getText().toString();
        //downloadFile = imageToDownload.getText().toString();
    }


    private class UploadDropbox extends AsyncTask<String, Void, FileMetadata> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected FileMetadata doInBackground(String... params) {
            //Uploader();
            //String localUri = params[0];
            String tempFold = "/";
            if(!folderName.isEmpty()){
                tempFold = folderName;
            }
            File localFile = UriHelpers.getFileForUri(getApplicationContext(), fullPhotoUri);
            if (localFile != null) {
                //String remoteFolderPath = params[1];

                String remoteFileName = localFile.getName();
                try {
                    InputStream inputStream = new FileInputStream(localFile);
                    {

                        return client.files().uploadBuilder(tempFold + remoteFileName)
                                .withAutorename(true)
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
            //tv.setText(files += "Downloaded: " + downloadFile);

        }

        @Override
        protected void onCancelled() {

        }
    }

}




//    public void Uploader()  {
//        try {
//
//
//
//
//            InputStream is = getContentResolver().openInputStream(fullPhotoUri);
//
//            InputStream in = new FileInputStream(is.toString());
//            FileMetadata metadata = client.files().uploadBuilder("/")
//                    .withAutorename(true)
//                    .uploadAndFinish(in);
//            //Bitmap bitmap = BitmapFactory.decodeStream(is);
//            is.close();
//
//
//            //String filepath = fullPhotoUri.getPath();
//
//
////            InputStream in = getContentResolver().openInputStream(fullPhotoUri);
////
////
////
////            client.files().uploadBuilder("/")
////                    .withAutorename(true)
////                    .withMute(true)
////                    .withClientModified(new Date())
////                    .uploadAndFinish(in);
////
////            in.close();
//
//
//            //java.net.URI javauri = new java.net.URI(fullPhotoUri.toString());
//
////            File file = new File(getContentResolver().SCHEME_FILE);
////            Log.d("Scheme", getContentResolver().SCHEME_FILE);
////            //file.getAbsolutePath();
////            //InputStream in = getContentResolver().openInputStream(fullPhotoUri);
////            InputStream in = getContentResolver().openInputStream(fullPhotoUri);
////
////            Log.d("Inputstream;",in.toString());
////
////
////            //try (InputStream in = new FileInputStream(file.getAbsoluteFile())) {
////            FileMetadata metadata = client.files().uploadBuilder("/KUVIA/"+file.getName())
////                    .uploadAndFinish(in);
////
////            in.close();
//
//            } catch (Exception e) {
//                Log.d("Upload Error",e.getMessage());
//            }
//        }
//
//        }
//
//
//
//            //String filename = text;
//
//
//            //Upload test
////            try (InputStream in = new FileInputStream(text)) {
////                FileMetadata metadata = client.files().uploadBuilder(text)
////                        .uploadAndFinish(in);
////
////                in.close();
////            } catch (IOException e) {
////                Log.d("Upload Error",e.getMessage());
////            }
//
//
////        } catch (Exception e){
////
////            Log.d("Upload Error",e.getMessage());
////        }



