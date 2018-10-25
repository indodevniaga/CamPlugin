package com.indodevniaga.cordova.plugin;

// The native Toast API
import android.widget.Toast;
import android.net.Uri;
import android.Manifest;
import android.os.Build;
import android.content.pm.PackageManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import org.apache.cordova.LOG;

public class CamPlugin extends CordovaPlugin {
  private static final String DURATION_LONG = "long";

  public CallbackContext context;
  FileUtil fileUtil;
  private static final int REQUEST_CAMERA = 100;

  private static final int REQUEST_SCAN = 4;
  private Uri fileUri = null;
  public static final int MEDIA_TYPE_IMAGE = 1;
  Uri mCameraUri;
  String modifiedImagePath;
  String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA };
  String pathPicture;

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
    // Verify that the user sent a 'show' action
    this.context = callbackContext;
    if (!action.equals("show")) {
      callbackContext.error("\"" + action + "\" is not a recognized action.");
      return false;
    }
    String message;
    String duration;
    try {
      JSONObject options = args.getJSONObject(0);
      message = options.getString("message");
      duration = options.getString("duration");
    } catch (JSONException e) {
      callbackContext.error("Error encountered: " + e.getMessage());
      return false;
    }
    // Create the toast
    // Toast toast = Toast.makeText(cordova.getActivity(), message,
    //     DURATION_LONG.equals(duration) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
    // // Display toast
    // toast.show();
    if(hasPermisssionStorage()==true){
      try {
        dispatchTakePictureIntent();

      } catch (IOException io) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR);
        context.sendPluginResult(result);

        //Toast.makeText(cordova.getActivity(), "error camera", Toast.LENGTH_SHORT).show();
      }
    }else{
    requestPermissionsStorage(0);
    }
    
    return true;
  }

  void resizePicture(String url) {
    File imgFileOrig = new File(url);
    Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());
    int origWidth = b.getWidth();
    int origHeight = b.getHeight();

    final int destWidth = 600;// or the width you need

    if (origWidth > destWidth) {
      int destHeight = origHeight / (origWidth / destWidth);
      Bitmap b2 = Bitmap.createScaledBitmap(b, 300, 300, false);
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();

      b2.compress(Bitmap.CompressFormat.JPEG, 90, outStream);

      File f = new File(url);

      try {
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(outStream.toByteArray());
        fo.close();
        final JSONObject json = new JSONObject();
        // if(location != null){
        try {

          json.put("photoPath", Uri.fromFile(f));
          PluginResult result = new PluginResult(PluginResult.Status.OK, json);
          context.sendPluginResult(result);
        } catch (JSONException exc) {
          PluginResult  result = new PluginResult(PluginResult.Status.ERROR);
          context.sendPluginResult(result);
        }
     
      } catch (IOException e) {
      
        PluginResult result = new PluginResult(PluginResult.Status.ERROR);
        context.sendPluginResult(result);
      }

    }

  }

  public boolean hasPermisssionStorage() {
    boolean statusPermission = false;
    for (String p : permissions) {
      if (!PermissionHelper.hasPermission(this, p)) {
    return false;
      }
    }

    return true;
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        this.context.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        return;
      }
    }
    switch (requestCode) {
    case 0:
     // Toast.makeText(cordova.getActivity(), "Have Permission", Toast.LENGTH_SHORT).show();
      try {
        dispatchTakePictureIntent();

      } catch (IOException io) {
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        context.sendPluginResult(result);

        //Toast.makeText(cordova.getActivity(), "error camera", Toast.LENGTH_SHORT).show();
      }
      break;

    }
  }

  public void requestPermissionsStorage(int requestCode) {
    PermissionHelper.requestPermissions(this, requestCode, permissions);
    
  }


  private void dispatchTakePictureIntent() throws IOException {
    final boolean isLolipop = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    PluginResult pluginResult;
    if (isLolipop) {
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      fileUri = getOutputMediaFileUri(1);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
      cordova.setActivityResultCallback(this);
      cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CAMERA);
      Log.e("SDK:", "" + Build.VERSION.SDK_INT);
    } else {
      Log.e("SDK:", "" + Build.VERSION.SDK_INT);
      mCameraUri = cordova.getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          new ContentValues());
      Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (cameraIntent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
        cordova.setActivityResultCallback(this);
        cordova.startActivityForResult((CordovaPlugin) this, cameraIntent, REQUEST_CAMERA);
      }

    }

  }

  private boolean isDeviceSupportCamera() {
    if (cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
  
      return true;
    } else {
   
      return false;
    }
  }

  public Uri getOutputMediaFileUri(int type) {
    return Uri.fromFile(getOutputMediaFile(type));
  }

  public void onSaveInstanceState(Bundle outState) {
    onSaveInstanceState(outState);
    outState.putParcelable("file_uri", fileUri);
  }

  public void onRestoreInstanceState(Bundle savedInstanceState) {
    onRestoreInstanceState(savedInstanceState);

    // get the file url
    fileUri = savedInstanceState.getParcelable("file_uri");
  }

  
  private static File getOutputMediaFile(int type) {
   
    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "Attendance");
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("derectory", "Oops! Failed create Momo directory");
        return null;
      }
    }

   

    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE) {
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
      mediaFile = new File(mediaStorageDir.getPath() + File.separator + "_" + timeStamp + ".jpg");
    } else {
      return null;
    }

    return mediaFile;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    PluginResult result;
  
    if (resultCode == -1) {
      if (requestCode == REQUEST_CAMERA) {
        final boolean isLolipop = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isLolipop) {
          Log.e("Uri Camera", "" + fileUri.getPath());
          resizePicture(fileUri.getPath());

          // pluginResult = new PluginResult(PluginResult.Status.OK);
          // context.sendPluginResult(pluginResult);
        } else {
          Log.e("Uri Camera", "" + modifiedImagePath);

          modifiedImagePath = FileUtil.getPathFromUri(cordova.getActivity(), mCameraUri);
          resizePicture(modifiedImagePath);
          // pluginResult = new PluginResult(PluginResult.Status.OK);
          // context.sendPluginResult(pluginResult);

        }
      }

      else if (resultCode == 0) {
         result = new PluginResult(PluginResult.Status.ERROR);
        context.sendPluginResult(result);
        //  Toast.makeText(cordova.getActivity(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
      } else {
         result = new PluginResult(PluginResult.Status.ERROR);
        context.sendPluginResult(result);

        //Toast.makeText(cordova.getActivity(), "Sorry, You Cant't Capture Image", Toast.LENGTH_SHORT).show();
      }
      if (!isDeviceSupportCamera()) {
         result = new PluginResult(PluginResult.Status.ERROR);
        context.sendPluginResult(result);
        //  Toast.makeText(cordova.getActivity(), "Sorry! Your device doesn't support camera", Toast.LENGTH_LONG).show();
      }
    } else {
      // finish();
    }
  }

}