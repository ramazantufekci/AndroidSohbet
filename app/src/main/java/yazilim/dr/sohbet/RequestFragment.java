package yazilim.dr.sohbet;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView drecyclerView;
    private FirebaseAuth dAuth;

    private DatabaseReference duserDatabase;
    private DatabaseReference dUserRequest;

    private String dgecerliKulId;

    private View dView;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dView = inflater.inflate(R.layout.fragment_request,container,false);
        drecyclerView = (RecyclerView) dView.findViewById(R.id.request_list);

        dAuth = FirebaseAuth.getInstance();
        dgecerliKulId = dAuth.getCurrentUser().getUid();
        dUserRequest = FirebaseDatabase.getInstance().getReference().child("arkadas_sev");
        duserDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        drecyclerView.setHasFixedSize(true);
        drecyclerView.setLayoutManager(linearLayoutManager);
        return dView;

    }


    @Override
    public void onStart() {
        super.onStart();

        Query quer = dUserRequest.child(dgecerliKulId).orderByValue();
        Log.d("Sorgu",""+quer);
        FirebaseRecyclerAdapter<Users,RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, RequestViewHolder>(
                Users.class,
                R.layout.users_single,
                RequestViewHolder.class,
                quer
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Users model, int position) {
                final String list_user_id = getRef(position).getKey();
                Query sorggg = dUserRequest.child(dgecerliKulId).child(list_user_id);
                sorggg.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("istek_turu")) {
                            String sonuc = dataSnapshot.child("istek_turu").getValue().toString();
                            if (sonuc.equals("sevilen")) {
                                drecyclerView.setVisibility(View.VISIBLE);
                                duserDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        viewHolder.setName(dataSnapshot.child("isim").getValue().toString());
                                        viewHolder.setDurum(dataSnapshot.child("durum").getValue().toString());
                                        viewHolder.setUserImage(dataSnapshot.child("kucuk_resim").getValue().toString(), getContext());
                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent profilIntent = new Intent(getContext(), ProfilActivity.class);
                                                profilIntent.putExtra("user_id", list_user_id);
                                                startActivity(profilIntent);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                drecyclerView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        drecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String deneme){
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(deneme);
        }

        public void setDurum(String durum){
            TextView userDurumView = (TextView)mView.findViewById(R.id.user_single_durum);
            userDurumView.setText(durum);
        }

        public void setUserImage(String image, Context ctx){
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.user_single_resim);
            Picasso.with(ctx).load(image).placeholder(R.drawable.default_avatar).into(userImageView);
        }
    }
}
