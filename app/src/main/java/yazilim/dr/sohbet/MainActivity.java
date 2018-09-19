package yazilim.dr.sohbet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mtoolBar;
    private ViewPager dviewPager;
    private DatabaseReference dUserRef;

    private SectionsPagerAdapter dsectionsPagerAdapter;
    private TabLayout dtabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtoolBar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle("DR Sohbet");
        dviewPager = (ViewPager)findViewById(R.id.tabPager);
        dsectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        dviewPager.setAdapter(dsectionsPagerAdapter);
        //

        dtabLayout = (TabLayout)findViewById(R.id.main_tabs);
        dtabLayout.setupWithViewPager(dviewPager);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            dUserRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(mAuth.getCurrentUser().getUid());
        }


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    // User is signed in
                    sendToStart();
                } else {
                    // User is signed out

                    Log.d("ÇIK", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseUser gecerliKul = mAuth.getCurrentUser();
        if (gecerliKul == null){
            sendToStart();
        }else {
            dUserRef.child("online").setValue("true");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    /**
     *
     * ayar menusu ile ilgili
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_cikis){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if (item.getItemId() == R.id.main_account_setting){
            Intent ayarIntent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(ayarIntent);
        }
        if (item.getItemId() == R.id.main_alluser){
            Intent ayarIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(ayarIntent);
        }
        return true;
    }
}
