package yazilim.dr.sohbet;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar dtoolbar;
    private TextInputLayout dtext;
    private Button dSave;

    /**
     *
     * Firebase kullanıcı ve database tanımları
     */
    private DatabaseReference databaseReference;
    private FirebaseUser dgecerliKul;

    //progress
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //Firebase database ve kullanıcı işlemleri;
        dgecerliKul = FirebaseAuth.getInstance().getCurrentUser();
        String uid = dgecerliKul.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(uid);
        //progress
        progressDialog = new ProgressDialog(this);
        dtoolbar = (Toolbar)findViewById(R.id.status_bar);
        setSupportActionBar(dtoolbar);
        getSupportActionBar().setTitle("Durum");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //intent put
        String status_value = getIntent().getStringExtra("status_value");
        dtext = (TextInputLayout)findViewById(R.id.status_input);
        dSave = (Button)findViewById(R.id.status_save);
        dtext.getEditText().setText(status_value);
        dSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //progress
                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Durumunuz güncelleniyor.");
                progressDialog.setMessage("Lütfen bekleyin...");
                progressDialog.show();
                String status = dtext.getEditText().getText().toString();
                databaseReference.child("durum").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                        }else {
                            Toast.makeText(getApplicationContext(), "Durum güncellenmedi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}
