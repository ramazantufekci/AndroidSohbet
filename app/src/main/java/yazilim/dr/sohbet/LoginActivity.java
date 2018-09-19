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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar dToolbar;
    private TextInputLayout dLoginmail;
    private TextInputLayout dLoginpass;
    private Button dLogin_btn;
    private ProgressDialog dLoginProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dToolbar = (Toolbar)findViewById(R.id.login_bar);
        setSupportActionBar(dToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Giriş");
        mAuth = FirebaseAuth.getInstance();
        dLoginProgress = new ProgressDialog(this);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        dLoginmail = (TextInputLayout)findViewById(R.id.dLogin_mail);
        dLoginpass = (TextInputLayout)findViewById(R.id.dLogin_pass);
        dLogin_btn = (Button)findViewById(R.id.grsBtn);

        dLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = dLoginmail.getEditText().getText().toString();
                String sifre = dLoginpass.getEditText().getText().toString();
                if (!TextUtils.isEmpty(mail) || !TextUtils.isEmpty(sifre)){
                    dLoginProgress.setTitle("Giriş Yapılıyor");
                    dLoginProgress.setMessage("Lütfen Bekleyin...");
                    dLoginProgress.setCanceledOnTouchOutside(false);
                    dLoginProgress.show();
                    loginUser(mail,sifre);
                }
            }
        });
    }

    private void loginUser(String mail, String sifre) {
        mAuth.signInWithEmailAndPassword(mail,sifre).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    dLoginProgress.dismiss();

                    String gecerli_kul_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    databaseReference.child(gecerli_kul_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent loginIntent = new Intent(LoginActivity.this,MainActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginIntent);
                            finish();
                        }
                    });


                }else {
                    dLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, "Giriş Yapılamadı.Formu kontrol edip Tekrar deneyin.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
