package yazilim.dr.sohbet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {
    /**
     * firebase database ve kullanıcı ile ilgili işlemler için
     */
    private DatabaseReference databaseReference;
    private FirebaseUser dgecerliKul;

    /**
     *
     * Arayuz için
     */
    private CircleImageView mImage;
    private TextView disim;
    private TextView ddurum;
    private Button ddurumBtn;
    private Button dresimBtn;
    private static final int GALERY_PICK = 1;

    //progress
    private ProgressDialog progress;

    //Firebase storage
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mImage = (CircleImageView)findViewById(R.id.setting_image);
        disim = (TextView)findViewById(R.id.setting_display_name);
        ddurum = (TextView)findViewById(R.id.setting_durum);
        ddurumBtn = (Button)findViewById(R.id.setting_degis_durum);
        dresimBtn = (Button)findViewById(R.id.setting_degis_resim);
        //firebase stroge
        storageReference = FirebaseStorage.getInstance().getReference();
        dgecerliKul = FirebaseAuth.getInstance().getCurrentUser();
        String uid = dgecerliKul.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(uid);

        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String isim = dataSnapshot.child("isim").getValue().toString();
                String durum = dataSnapshot.child("durum").getValue().toString();
                final String resim = dataSnapshot.child("resim").getValue().toString();

                disim.setText(isim);
                ddurum.setText(durum);

                Picasso.with(SettingActivity.this).load(resim).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(mImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(SettingActivity.this).load(resim)
                                .placeholder(R.drawable.default_avatar).into(mImage);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Durum butonu event
        ddurumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_text = ddurum.getText().toString();
                Intent durumIntent = new Intent(SettingActivity.this,StatusActivity.class);
                durumIntent.putExtra("status_value",status_text);
                startActivity(durumIntent);

            }
        });

        //Resim butonu event
        dresimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galeryIntent = new Intent();
                galeryIntent.setType("image/*");
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galeryIntent,"Resim Seç"),GALERY_PICK);
                //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(SettingActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1).setMinCropWindowSize(500,500).start(this);
            //Toast.makeText(this, ""+imageUri, Toast.LENGTH_SHORT).show();
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                progress = new ProgressDialog(SettingActivity.this);
                progress.setTitle("Profil Resmi Yükleniyor.");
                progress.setMessage("Lütfen Bekleyiniz...");
                progress.setCanceledOnTouchOutside(false);
                progress.show();
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                String dgecerliUid = dgecerliKul.getUid();

                Bitmap thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = storageReference.child("profil_resim").child(dgecerliUid+".jpg");
                final StorageReference thumb_filepath = storageReference.child("profil_resim").child("thumbs").child(dgecerliUid+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                          final  String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()){
                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("resim",download_url);
                                        update_hashMap.put("kucuk_resim",thumb_downloadUrl);
                                        databaseReference.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    progress.dismiss();
                                                    Toast.makeText(SettingActivity.this, "Profil resmi yüklendi.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else {
                                        Toast.makeText(SettingActivity.this, "Küçük resmi yüklenemedi!!!", Toast.LENGTH_SHORT).show();
                                        progress.dismiss();
                                    }

                                }
                            });

                        }else {
                            Toast.makeText(SettingActivity.this, "Profil resmi yüklenemedi!!!", Toast.LENGTH_SHORT).show();
                            progress.dismiss();
                        }
                    }
                });
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }
    }

    public static String random(){
        Random generator =new Random();
        StringBuilder stringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tmpChar;
        for (int i = 0;i<randomLength;i++){
            tmpChar = (char) (generator.nextInt(5)+71);
            stringBuilder.append(tmpChar);
        }
        return stringBuilder.toString();
    }
}
