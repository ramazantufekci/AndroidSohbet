package yazilim.dr.sohbet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String dChatUser;
    private Toolbar chatToolbar;

    private DatabaseReference dRootRef;

    private TextView dTitle;
    private TextView dLastseen;
    private CircleImageView circleImageView;
    private ImageButton dchataddBtn,dchatsendBtn;
    private EditText dchatText;
    private FirebaseAuth dauth;
    private String dgecerliKul;
    private RecyclerView drecyclerView;
    private SwipeRefreshLayout drefreshLayout;

    private final List<Mesajlar> mesajlarList = new ArrayList<>();
    private LinearLayoutManager dlayoutManager;
    private MesajAdapter mesajAdapter;

    private DatabaseReference userMesajDb;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int gecerliSayfa = 1;


    //Firebase Storage
    private StorageReference dstorage;

    private int itemPos = 0;
    private String dLastKey = "";
    private String dPrevKey = "";
    private static final int GALERY_PICK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatToolbar = (Toolbar)findViewById(R.id.chat_app_bar);

        dauth = FirebaseAuth.getInstance();
        dgecerliKul = dauth.getCurrentUser().getUid();

        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dRootRef = FirebaseDatabase.getInstance().getReference();

        dChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        //getSupportActionBar().setTitle(userName);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);


        //---custom action bar items ----
        dTitle = (TextView)findViewById(R.id.custom_bar_title);
        dLastseen = (TextView)findViewById(R.id.custom_bar_seen);
        circleImageView = (CircleImageView)findViewById(R.id.chat_user_resim);

        dchataddBtn = (ImageButton)findViewById(R.id.chat_add_btn);
        dchatsendBtn = (ImageButton)findViewById(R.id.chat_send_btn);
        dchatText = (EditText)findViewById(R.id.chat_mesaj_view);
        drefreshLayout = (SwipeRefreshLayout)findViewById(R.id.mesaj_swipe_layout);

        dstorage = FirebaseStorage.getInstance().getReference();

        mesajAdapter = new MesajAdapter(mesajlarList);

        drecyclerView = (RecyclerView)findViewById(R.id.mesaj_list);
        dlayoutManager = new LinearLayoutManager(this);
        drecyclerView.setHasFixedSize(true);
        drecyclerView.setLayoutManager(dlayoutManager);
        drecyclerView.setAdapter(mesajAdapter);
        loadMesajlar();

        dTitle.setText(userName);

        dRootRef.child("Kullanicilar").child(dChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("resim").getValue().toString();


                if (online.equals("true")){
                    dLastseen.setText("Online");
                }else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    dLastseen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        dRootRef.child("Chat").child(dgecerliKul).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(dChatUser)){
                    Map chatMap = new HashMap();
                    chatMap.put("seen",false);
                    chatMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+dgecerliKul+"/"+dChatUser,chatMap);
                    chatUserMap.put("Chat/"+dChatUser+"/"+dgecerliKul,chatMap);


                    dRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Log.d("Chat log",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dchatsendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMesaj();
            }
        });


        //image send button
        dchataddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galeryIntent = new Intent();
                galeryIntent.setType("image/*");
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galeryIntent,"Resim Seç :"),GALERY_PICK);
            }
        });


        drefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                gecerliSayfa++;

                itemPos = 0;
                loadMoreMesajlar();
            }
        });


    }

    //resim ekleme butonuna basıldıktan sonra resim secilme muhabbeti


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            final String gecerli_kul_ref = "mesajlar/"+dgecerliKul+"/"+dChatUser;
            final String chat_user_ref = "mesajlar/"+dChatUser+"/"+dgecerliKul;

            DatabaseReference user_mesaj_push = dRootRef.child("mesajlar").child(dgecerliKul).child(dChatUser).push();

            final String push_id = user_mesaj_push.getKey();

            StorageReference filepath = dstorage.child("mesaj_resim").child(push_id+".jpg");
            StorageTask<UploadTask.TaskSnapshot> taskSnapshotStorageTask = filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map mesajMap = new HashMap();
                        mesajMap.put("mesaj", download_url);
                        mesajMap.put("seen", false);
                        mesajMap.put("type", "image");
                        mesajMap.put("time", ServerValue.TIMESTAMP);
                        mesajMap.put("from", dgecerliKul);

                        Map mesajUserMap = new HashMap();
                        mesajUserMap.put(gecerli_kul_ref + "/" + push_id, mesajMap);
                        mesajUserMap.put(chat_user_ref + "/" + push_id, mesajMap);

                        dchatText.setText("");

                        dRootRef.updateChildren(mesajUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            }
                        });
                    }
                }
            });

        }
    }

    private void loadMoreMesajlar() {
        DatabaseReference mesajRef = dRootRef.child("mesajlar").child(dgecerliKul).child(dChatUser);
        Query mesajQuery = mesajRef.orderByKey().endAt(dLastKey).limitToLast(10);
        mesajQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mesajlar mesajlar = dataSnapshot.getValue(Mesajlar.class);
                String mesajKey = dataSnapshot.getKey();

                if (!dPrevKey.equals(mesajKey)){
                    mesajlarList.add(itemPos++,mesajlar);
                }else {
                    dPrevKey = mesajKey;
                }


                if (itemPos == 1){

                    dLastKey = mesajKey;
                }
                //Log.d("TotalKeys","dLastKet "+dLastKey+" | dprevKey "+dPrevKey+" | mesajKey "+mesajKey);

                mesajAdapter.notifyDataSetChanged();

                //  drecyclerView.scrollToPosition(mesajlarList.size()-1);
                drefreshLayout.setRefreshing(false);
                //dlayoutManager.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMesajlar() {
        DatabaseReference mesajRef = dRootRef.child("mesajlar").child(dgecerliKul).child(dChatUser);
        Query mesajQuery = mesajRef.limitToLast(gecerliSayfa*TOTAL_ITEMS_TO_LOAD);
        mesajQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mesajlar mesajlar = dataSnapshot.getValue(Mesajlar.class);

                itemPos++;
                if (itemPos == 1){
                    String mesajKey = dataSnapshot.getKey();
                    dLastKey = mesajKey;
                    dPrevKey = mesajKey;
                }
                mesajlarList.add(mesajlar);
                mesajAdapter.notifyDataSetChanged();

                drecyclerView.scrollToPosition(mesajlarList.size()-1);
                drefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMesaj() {
        String mesaj = dchatText.getText().toString();

        if (!TextUtils.isEmpty(mesaj)){

            String gecerli_kul_ref = "mesajlar/"+dgecerliKul+"/"+dChatUser;
            String chat_user_ref = "mesajlar/"+dChatUser+"/"+dgecerliKul;

            DatabaseReference user_mesaj_push = dRootRef.child("mesajlar").child(dgecerliKul).child(dChatUser).push();

            String push_id = user_mesaj_push.getKey();

            Map mesajMap = new HashMap();
            mesajMap.put("mesaj",mesaj);
            mesajMap.put("seen",false);
            mesajMap.put("type","text");
            mesajMap.put("time",ServerValue.TIMESTAMP);
            mesajMap.put("from",dgecerliKul);

            Map mesajUserMap = new HashMap();
            mesajUserMap.put(gecerli_kul_ref+"/"+push_id,mesajMap);
            mesajUserMap.put(chat_user_ref+"/"+push_id,mesajMap);

            dchatText.setText("");

            dRootRef.updateChildren(mesajUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d("Chat log",databaseError.getMessage().toString());
                    }

                }
            });

        }
    }
}
