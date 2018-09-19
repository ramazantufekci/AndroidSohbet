package yazilim.dr.sohbet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegistryActivity extends AppCompatActivity {
    private TextInputLayout dIsim;
    private  TextInputLayout dMail;
    private TextInputLayout dSifre;
    private Button dBtn;
    private Toolbar dToolbar;
    private ProgressDialog dprogress;
    /**
     * Firebase Auth kullanıcının oturumunu kontrol etmek için
     */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Firebase Database veri tabanı işlemleri için
     *
     */
    private DatabaseReference ddatabase ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);
        dIsim = (TextInputLayout)findViewById(R.id.reg_display_name);
        dMail = (TextInputLayout)findViewById(R.id.reg_email);
        dSifre = (TextInputLayout)findViewById(R.id.reg_password);
        dBtn = (Button)findViewById(R.id.reg_new_btn);
        dToolbar = (Toolbar)findViewById(R.id.registry_bar);
        setSupportActionBar(dToolbar);
        getSupportActionBar().setTitle("Yeni Hesap");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dprogress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        dBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = dIsim.getEditText().getText().toString();
                String mail = dMail.getEditText().getText().toString();
                String sifre = dSifre.getEditText().getText().toString();
                if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(mail) || !TextUtils.isEmpty(sifre)){
                    dprogress.setTitle("Kullanıcı Oluşturuluyor");
                    dprogress.setMessage("Lütfen Bekleyin...");
                    dprogress.setCanceledOnTouchOutside(false);
                    dprogress.show();

                    kullaniciOlustur(display_name,mail,sifre);
                }


            }
        });
    }

    private void kullaniciOlustur(final String display_name, String mail, String sifre) {
        mAuth.createUserWithEmailAndPassword(mail,sifre).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser gecerli = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = gecerli.getUid();
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    ddatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(uid);
                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("isim",display_name);
                    userMap.put("durum","ben dunyanın");
                    userMap.put("resim","default");
                    userMap.put("kucuk_resim","default");
                    userMap.put("device_token",device_token);
                    ddatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                dprogress.dismiss();
                                Intent mainIntent = new Intent(RegistryActivity.this,MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }else {
                    dprogress.hide();
                    Toast.makeText(RegistryActivity.this, "Kullanıcı Oluşturulamadı.Formu kontrol edip tekrar deneyin.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
