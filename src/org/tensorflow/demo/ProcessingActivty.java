package org.tensorflow.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessingActivty extends Activity {
    private static final String TAG = "ProcessingActivty";
    private StorageReference mStorageRef;
    private int uploadedImages;

    String MOBILE_MEDIA_FOLDER = "/ARSDKMedias/";
    private ProgressDialog mUploadProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processing_activty);
        mStorageRef = FirebaseStorage.getInstance().getReference();



        //save();




    }

    @Override
    protected void onResume() {
        super.onResume();

        Handler handler = new Handler(Looper.getMainLooper());

        final String[] images = new String[16]; //,"sadas", "dsad", "sadas"
        createProgressDialog(images.length);
        connect();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                for(int i=0;i<16;i++){
                    images[i] = "pepe.jpg";
                }
                mUploadProgressDialog.setMessage("Sending Information");
                uploadImages(images);
            }
        },4000);


    }

    private void connect(){


        String networkSSID = "14963193";
        String networkPass = "DBFF5D78CEA82";

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";

        conf.wepKeys[0] = "\"" + networkPass + "\"";
        //conf.wepKeys[0] =  networkPass ;
        conf.wepTxKeyIndex = 0;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);

        wifiManager.reconnect();




    }

    private void sendToMtc(ApiService.FlightData flightData,Callback<ApiService.ResponseMessage> callback){
        Call<ApiService.ResponseMessage> call= RetrofitClient.getInstance().getApiService().saveFlightData(flightData);
        call.enqueue(callback);
    }
    private void save(){


        String externalDirectory = Environment.getExternalStorageDirectory().toString().concat(MOBILE_MEDIA_FOLDER);

        File file = new File(externalDirectory + "/" + "Pepe.jpg");;
  //      Bitmap bitmap = BitmapFactory.decodeFile(externalDirectory + "/" + "Pepe.jpg");


        /*
        ApiService.Pedido pedido = new ApiService.Pedido("adsad","asda",3);
        Call<ApiService.ResponseMessage> call= RetrofitClient.getInstance().getApiService().saveIntereses(pedido);
        call.enqueue(new Callback<ApiService.ResponseMessage>() {
            @Override
            public void onResponse(Call<ApiService.ResponseMessage> call, Response<ApiService.ResponseMessage> response) {
                ApiService.ResponseMessage res=response.body();
                Log.e("d",":v");

            }

            @Override
            public void onFailure(Call<ApiService.ResponseMessage> call, Throwable t) {

                Log.e("d",":v");
            }
        });

        */


        //    String img = convertImageToStringForServer(bitmap);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "exa,ple");
        Call<okhttp3.ResponseBody> req = RetrofitClient.getInstance().getApiService().postImage( body, name);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Do Something
                Log.e("H","_V-");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("H","_V-s");
            }
        });

    }

    public static String convertImageToStringForServer(Bitmap imageBitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(imageBitmap != null) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }else{
            return null;
        }
    }

    private void createProgressDialog(int max){
        mUploadProgressDialog = new ProgressDialog(this);
        mUploadProgressDialog.setIndeterminate(false);
        mUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUploadProgressDialog.setMessage("Changing Network ...");
        mUploadProgressDialog.setMax(max);
        mUploadProgressDialog.setSecondaryProgress(0);
        mUploadProgressDialog.setCancelable(false);
        mUploadProgressDialog.show();
    }

    private void upadateProgressDialog(int progress){
        mUploadProgressDialog.setSecondaryProgress(progress);
        mUploadProgressDialog.setProgress(progress);

    }

    private void closeProgressDialog(){
        mUploadProgressDialog.dismiss();
        mUploadProgressDialog = null;
    }

    private void uploadImages(final String[] images){
        uploadedImages = 0;
        final String[] imgsUrls = new String[images.length];
        List<Classifier.Recognition>[] results = new List[images.length];


        String externalDirectory = Environment.getExternalStorageDirectory().toString().concat(MOBILE_MEDIA_FOLDER);
        String storageFolderName = (new Date())+"";
        final String flightId = UUID.randomUUID().toString();
        for(int i=0;i<images.length;i++){
            final int _i=i;

            final Bitmap bm = getResizedBitmap(externalDirectory + "/" + images[_i]);



            uploadBitmap(bm, storageFolderName, UUID.randomUUID() +".jpg")
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                final List<Classifier.Recognition> result = classifyImage(bm);
                                bm.recycle();
                                imgsUrls[_i] = downloadUrl.toString();
                                String[] tags = new String[result.size()];
                                String[] scores = new String[result.size()];
                                Classifier.Recognition r = null;
                                for(int i=0;i<result.size();i++){
                                    r = result.get(i);
                                    tags[i] = r.getTitle();
                                    scores[i] = r.getConfidence()+"";
                                }

                                ApiService.FlightData flightData = new ApiService.FlightData(
                                        flightId,
                                        downloadUrl.toString(),
                                        tags,
                                        scores

                                );
                                sendToMtc(flightData, new Callback<ApiService.ResponseMessage>() {
                                    @Override
                                    public void onResponse(Call<ApiService.ResponseMessage> call, Response<ApiService.ResponseMessage> response) {
                                        synchronized (this){
                                            uploadedImages++;
                                            ProcessingActivty.this.upadateProgressDialog(uploadedImages);
                                            if(uploadedImages == images.length){ //all images uploaded
                                                closeProgressDialog();
                                                Log.e(TAG,"Finish uploading");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiService.ResponseMessage> call, Throwable t) {

                                    }
                                });



                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("Upload failed", ":V");
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });


        };



    }



    private UploadTask uploadFile(String path){
        Uri file = Uri.fromFile(new File(path));
        StorageReference riversRef = mStorageRef.child("fligh2"+"/"+ UUID.randomUUID() +".jpg");
        return riversRef.putFile(file);
    }

    private UploadTask uploadBitmap(Bitmap bitmap,String folder,String name){

        StorageReference riversRef = mStorageRef.child(folder + "/" + name);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return riversRef.putBytes(stream.toByteArray());
    }


    public Bitmap getResizedBitmap(String path) {

        BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
        mBitmapOptions.inJustDecodeBounds = true;

        Bitmap mCurrentBitmap = BitmapFactory.decodeFile(path, mBitmapOptions);
        int srcWidth = mBitmapOptions.outWidth;
        int srcHeight = mBitmapOptions.outHeight;
        int targetWidth = 1000;
        mBitmapOptions = new BitmapFactory.Options();
        mBitmapOptions.inScaled = true;
        mBitmapOptions.inSampleSize = prevPowOf2( ((float)srcWidth )/ targetWidth) ;
        mBitmapOptions.inDensity = srcWidth;
        mBitmapOptions.inTargetDensity =  targetWidth * mBitmapOptions.inSampleSize;

        mCurrentBitmap = BitmapFactory.decodeFile(path, mBitmapOptions);


        return mCurrentBitmap;
    }
    /**
     *  if num > 1 returns biggest power of 2  <= num,
     *  else (num<=1) returns 1
     */

    static int prevPowOf2(float num){
        //Math.floor(Math.Log2(num));
        if(num <= 1 )
            return 1;
        int pow = 1;
        while(pow<=num){
            pow = pow << 1;
        }
        //here are  nextPowOf2, so we must divide by 2
        return pow >> 1;

    }

    static Bitmap resizeBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        //bm.recycle();
        return resizedBitmap;
    }

    private List<Classifier.Recognition>  classifyImage(Bitmap bitmap){

        Bitmap b2 = resizeBitmap(bitmap, 224, 224);
        ClassifierActivity ca = new ClassifierActivity();
        List<Classifier.Recognition> results = ca.classifyImg(b2, getAssets());

        Log.e(TAG, "----Results ----");
        for(Object o:results){
            Log.e(TAG, o.toString());

        }

        return results;
    }
}
