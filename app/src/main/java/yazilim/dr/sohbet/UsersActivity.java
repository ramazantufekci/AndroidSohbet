package yazilim.dr.sohbet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar dtoolbar;
    private RecyclerView dusersList;

    private DatabaseReference databaseReference;
    private String gecerli_kul_id;
    private FirebaseUser dgecerli_kul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        dtoolbar = (Toolbar)findViewById(R.id.users_bar);
        setSupportActionBar(dtoolbar);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        dgecerli_kul = FirebaseAuth.getInstance().getCurrentUser();
        getSupportActionBar().setTitle("Tüm Kullanıcılar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gecerli_kul_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        dusersList = (RecyclerView)findViewById(R.id.users_list);
        dusersList.setHasFixedSize(true);
        dusersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single,
                UsersViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                    viewHolder.setName(model.getIsim());
                    viewHolder.setUserDurum(model.getDurum());
                    viewHolder.setUserResim(model.getKucuk_resim(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.dView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profilIntent = new Intent(UsersActivity.this,ProfilActivity.class);
                        profilIntent.putExtra("user_id",user_id);
                        startActivity(profilIntent);

                    }
                });
            }
        };

        dusersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View dView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            dView = itemView;
        }

        public void setName(String name){
            TextView usernameView = (TextView) dView.findViewById(R.id.user_single_name);
            usernameView.setText(name);
        }

        public void setUserDurum(String durum){
            TextView durumView = (TextView) dView.findViewById(R.id.user_single_durum);
            durumView.setText(durum);
        }

        public void setUserResim(String resim, Context ctx){
            CircleImageView userResimView = (CircleImageView) dView.findViewById(R.id.user_single_resim);
            Picasso.with(ctx).load(resim).placeholder(R.drawable.default_avatar).into(userResimView);

        }
    }
}
