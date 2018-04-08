package com.sourcey.materiallogindemo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.Menu;
//import android.view.MenuItem;
//
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}

import android.Manifest;
import android.content.Context;
import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.database.Cursor;
        import android.graphics.Bitmap;
        import android.net.Uri;
        import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.Toast;

        import net.gotev.uploadservice.MultipartUploadRequest;
        import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Declaring views
    private Button buttonChoose;
    private Button buttonUpload;
    private ImageView imageView;
    private EditText editText;

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;

    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Bitmap to get image from gallery
    private Bitmap bitmap;

    //Uri to store the image uri
    private Uri filePath;

    private static final String UPLOAD_URL="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Requesting storage permission
        requestStoragePermission();

        //Initializing views
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editTextName);

        //Setting clicklistener
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }


    /*
    * This is the method responsible for image upload
    * We need the full image path and the name for the image in this method
    * */
    public void uploadMultipart() {
        //getting name for the image
        String name = editText.getText().toString().trim();

        //getting the actual path of the image
        String path = getPath(filePath);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("name", name) //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v == buttonChoose) {
            showFileChooser();
        }
        if (v == buttonUpload) {
            uploadMultipart();
        }
    }


}

//public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    //Declaring views
//    private Button buttonChoose;
//    private Button buttonUpload;
//    private ImageView imageView;
//    private EditText editText;
//    private EditText txt;
//    private  Button take_photo;
//    //Image request code
//    private int PICK_IMAGE_REQUEST = 1;
//    private Bitmap b;
//    int flag = 0;
//
//
//    //storage permission code
//    private static final int STORAGE_PERMISSION_CODE = 123;
//
//    //Bitmap to get image from gallery
//    private Bitmap bitmap;
//
//    //Uri to store the image uri
//    private Uri filePath;
//
//    private static final String UPLOAD_URL="";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        //Requesting storage permission
//        requestStoragePermission();
//
//        //Initializing views
//        //txt = (EditText) findViewById(R.id.txt);
//        buttonChoose = (Button) findViewById(R.id.buttonChoose);
//       // buttonUpload = (Button) findViewById(R.id.buttonUpload);
//        imageView = (ImageView) findViewById(R.id.imageView);
//        editText = (EditText) findViewById(R.id.editTextName);
//
//        //Setting clicklistener
//        buttonChoose.setOnClickListener(this);
//    //    buttonUpload.setOnClickListener(this);
//        imageView = (ImageView) findViewById(R.id.imageView);
//        take_photo = (Button) findViewById(R.id.button3);
//
//        take_photo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if(flag == 0) {
//
//                    // -- code for taking photo --
//
//                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(i, 99);
//
//                } else if(flag == 1) {
//
//                    //--- code for saving photo ---
//
//                    savePhotoToMySdCard(b);
//
//                    Toast.makeText(getApplicationContext(), "Photo saved to sd card!", Toast.LENGTH_SHORT).show();
//
//                    flag = 0;
//                    take_photo.setText("Take Photo");
//
//                }
//
//            }
//        });
//    }
//
//
//    /*
//    * This is the method responsible for image upload
//    * We need the full image path and the name for the image in this method
//    * */
////    public void uploadMultipart() {
////        //getting name for the image
////        String name = editText.getText().toString().trim();
////
////        //getting the actual path of the image
////        String path = getPath(filePath);
////
////        //Uploading code
////        try {
////            String uploadId = UUID.randomUUID().toString();
////
////            //Creating a multi part request
////            new MultipartUploadRequest(this,uploadId,UPLOAD_URL)
////                    .addFileToUpload(path, "image") //Adding file
////                    .addParameter("name", name) //Adding text parameter to the request
////                    .setNotificationConfig(new UploadNotificationConfig())
////                    .setMaxRetries(2)
////                    .startUpload(); //Starting the upload
////
////        } catch (Exception exc) {
////            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
////        }
////    }
//
//
//
//    //method to show file chooser
//    private void showFileChooser() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//    }
//
//    //handling the image chooser activity result
//  //  @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            filePath = data.getData();
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                imageView.setImageBitmap(bitmap);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//           }
//        }
//
//        if(requestCode == 99 && resultCode == RESULT_OK && data != null){
//
//            b = (Bitmap) data.getExtras().get("data");
//
//            imageView.setImageBitmap(b);
//
//            flag = 1;
//            take_photo.setText("Save Photo");
//
//        }
//
//    }
//
//    //method to get the file path from uri
//    public String getPath(Uri uri) {
//        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//        cursor.moveToFirst();
//        String document_id = cursor.getString(0);
//        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
//        cursor.close();
//
//        cursor = getContentResolver().query(
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
//        cursor.moveToFirst();
//        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//        cursor.close();
//
//        return path;
//    }
//
//
//    //Requesting permission
//    private void requestStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
//            return;
//
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            //If the user has denied the permission previously your code will come to this block
//            //Here you can explain why you need this permission
//            //Explain here why you need this permission
//        }
//        //And finally ask for the permission
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//    }
//
//
//    //This method will be called when the user will tap on allow or deny
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//
//        //Checking the request code of our request
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//
//            //If permission is granted
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                //Displaying a toast
//                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
//            } else {
//                //Displaying another toast if permission is not granted
//                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        if (v == buttonChoose) {
//            showFileChooser();
//        }
////        if (v == buttonUpload) {
////           // uploadMultipart();
////        }
//    }
//    public void sendMessage(View v)
//    {
//        String message;
//        message = editText.getText().toString();
//        Intent contentIntent = new Intent();
//        contentIntent.setAction(Intent.ACTION_SEND);
//        contentIntent.putExtra(Intent.EXTRA_TEXT,message);
//        contentIntent.setType("text/plain");
//        startActivity(contentIntent);
//    }
//    public void sendPicture(View view1)
//    {
//        ArrayList<Uri> imageUris = new ArrayList<Uri>();
//        imageUris.add(Uri.parse("android.resource://com.example.pradyothrao.data_sharing/"+R.drawable.logo));
//      //  imageUris.add(Uri.parse("android.resource://com.example.pradyothrao.data_sharing/"+R.drawable.logo));
//        Intent intent = new Intent();
//        intent.setAction(intent.ACTION_SEND);
//        intent.putExtra(intent.EXTRA_STREAM,imageUris);
//        intent.setType("image/*");
//        startActivity(Intent.createChooser(intent,"Send Message"));
//    }
//    //@Override
////    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////
////        if(requestCode == 99 && resultCode == RESULT_OK && data != null){
////
////            b = (Bitmap) data.getExtras().get("data");
////
////            imageView.setImageBitmap(b);
////
////            flag = 1;
////            take_photo.setText("Save Photo");
////
////        }
////
////    }
//
//
//    private void savePhotoToMySdCard(Bitmap bit){
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
//        String pname = sdf.format(new Date());
//
//
//        String root = Environment.getExternalStorageDirectory().toString();
//        File folder = new File(root+"/SCC_Photos");
//        folder.mkdirs();
//
//        File my_file = new File(folder, pname+".png");
//
//        try {
//
//            FileOutputStream stream = new FileOutputStream(my_file);
//            bit.compress(Bitmap.CompressFormat.PNG, 80, stream);
//            stream.flush();
//            stream.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//
//}
