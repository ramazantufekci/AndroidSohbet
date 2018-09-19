package yazilim.dr.sohbet;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfilActivity extends AppCompatActivity {

    private ImageView dimageView;
    private TextView dprofil_name;
    private TextView dprofil_durum;
    private TextView dprofil_arkadas;
    private Button dprofil_arkadas_btn,dprofil_sevme_btn;
    private DatabaseReference databaseReference;
    private DatabaseReference arkadasistekDatabase;
    private DatabaseReference arkadasDatabase;
    private DatabaseReference bildirimDatabase;
    private DatabaseReference drootRef;
    private FirebaseUser dgecerli_kul;
    private ProgressDialog progressDialog;

    private String gecerli_durum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        final String user_id = getIntent().getStringExtra("user_id");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id);
        arkadasistekDatabase = FirebaseDatabase.getInstance().getReference().child("arkadas_sev");
        arkadasDatabase = FirebaseDatabase.getInstance().getReference().child("arkadaslar");
        bildirimDatabase = FirebaseDatabase.getInstance().getReference().child("bildirim");
        drootRef = FirebaseDatabase.getInstance().getReference();
        dgecerli_kul = FirebaseAuth.getInstance().getCurrentUser();

        gecerli_durum = "degil";


        dimageView = (ImageView) findViewById(R.id.profil_avatar);
        dprofil_name = (TextView) findViewById(R.id.profil_isim);
        dprofil_durum = (TextView) findViewById(R.id.profil_durum);
        dprofil_arkadas = (TextView) findViewById(R.id.profil_arkadas);
        dprofil_arkadas_btn = (Button) findViewById(R.id.profil_istek_btn);
        dprofil_sevme_btn = (Button) findViewById(R.id.profil_sevme_btn);

        dprofil_sevme_btn.setVisibility(View.INVISIBLE);
        dprofil_sevme_btn.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Kullanıcı profili yükleniyor.");
        progressDialog.setMessage("Lütfen bekleyin...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profil_isim = dataSnapshot.child("isim").getValue().toString();
                String profil_durum = dataSnapshot.child("durum").getValue().toString();
                String profil_resim = dataSnapshot.child("resim").getValue().toString();

                dprofil_name.setText(profil_isim);
                dprofil_durum.setText(profil_durum);

                Picasso.with(ProfilActivity.this).load(profil_resim).placeholder(R.drawable.default_avatar).into(dimageView);

                // arkadas listesi ve istekler
                arkadasistekDatabase.child(dgecerli_kul.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String istek_turu = dataSnapshot.child(user_id).child("istek_turu").getValue().toString();

                            if (istek_turu.equals("sevilen")){
                                gecerli_durum = "sevmek_istiyor";
                                dprofil_arkadas_btn.setText("Sevmesine izin ver");
                                dprofil_sevme_btn.setVisibility(View.VISIBLE);
                                dprofil_sevme_btn.setEnabled(true);
                            }else if(istek_turu.equals("seven")){
                                gecerli_durum = "sev";
                                dprofil_arkadas_btn.setText("Sevmekten vazgeç");

                                dprofil_sevme_btn.setVisibility(View.INVISIBLE);
                                dprofil_sevme_btn.setEnabled(false);
                            }
                            progressDialog.dismiss();
                        }else{
                            arkadasDatabase.child(dgecerli_kul.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        gecerli_durum = "arkadas";
                                        dprofil_arkadas_btn.setText("Artık sevme");

                                        dprofil_sevme_btn.setVisibility(View.INVISIBLE);
                                        dprofil_sevme_btn.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();

                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dprofil_arkadas_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dprofil_arkadas_btn.setEnabled(false);
                // istek gönder
                if (gecerli_durum.equals("degil")){

                    DatabaseReference newNotification = drootRef.child("bildirim").child(user_id).push();
                    String newNotificationId = newNotification.getKey();

                    HashMap<String,String>bildirimData = new HashMap<>();
                    bildirimData.put("gonderen",dgecerli_kul.getUid());
                    bildirimData.put("tur","request");

                    Map requestMap = new HashMap();
                    requestMap.put("arkadas_sev/"+dgecerli_kul.getUid()+"/"+user_id+"/istek_turu","seven");
                    requestMap.put("arkadas_sev/"+user_id+"/"+dgecerli_kul.getUid()+"/istek_turu","sevilen");
                    requestMap.put("bildirim/"+user_id+"/"+newNotificationId,bildirimData);
                    drootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Toast.makeText(ProfilActivity.this, "Böyle istek olmaz Seven kişi", Toast.LENGTH_SHORT).show();
                            }
                            dprofil_arkadas_btn.setEnabled(true);
                            gecerli_durum = "sev";
                            dprofil_arkadas_btn.setText("Sevmekten Vazgeç");



                        }
                    });
                }
                //istek vazgec
                if (gecerli_durum.equals("sev")){
                    arkadasistekDatabase.child(dgecerli_kul.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            arkadasistekDatabase.child(user_id).child(dgecerli_kul.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dprofil_arkadas_btn.setEnabled(true);
                                    gecerli_durum = "degil";
                                    dprofil_arkadas_btn.setText("Sev");

                                    dprofil_sevme_btn.setVisibility(View.INVISIBLE);
                                    dprofil_sevme_btn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                //istek gönderildi
                if (gecerli_durum.equals("sevmek_istiyor")){
                    final String gecerliTarih = DateFormat.getDateTimeInstance().format(new Date());

                    Map arkadasMap = new HashMap();
                    arkadasMap.put("arkadaslar/"+dgecerli_kul.getUid()+"/"+user_id+"/date",gecerliTarih);
                    arkadasMap.put("arkadaslar/"+user_id+"/"+dgecerli_kul.getUid()+"/date",gecerliTarih);

                    arkadasMap.put("arkadas_sev/"+dgecerli_kul.getUid()+"/"+user_id,null);
                    arkadasMap.put("arkadas_sev/"+user_id+"/"+dgecerli_kul.getUid(),null);

                    drootRef.updateChildren(arkadasMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                dprofil_arkadas_btn.setEnabled(true);
                                gecerli_durum = "arkadas";
                                dprofil_arkadas_btn.setText("Artık sevme");

                                dprofil_sevme_btn.setVisibility(View.INVISIBLE);
                                dprofil_sevme_btn.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfilActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }

                //artık sevme
                if (gecerli_durum.equals("arkadas")){

                    Map artikSevme = new HashMap();
                    artikSevme.put("arkadaslar/"+dgecerli_kul.getUid()+"/"+user_id,null);
                    artikSevme.put("arkadaslar/"+user_id+"/"+dgecerli_kul.getUid(),null);

                    drootRef.updateChildren(artikSevme, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {

                                gecerli_durum = "degil";
                                dprofil_arkadas_btn.setText("Sev");

                                dprofil_sevme_btn.setVisibility(View.INVISIBLE);
                                dprofil_sevme_btn.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfilActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                            }
                            dprofil_arkadas_btn.setEnabled(true);
                        }
                    });

                }

            }
        });
    }
}
