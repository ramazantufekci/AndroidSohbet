package yazilim.dr.sohbet;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView darkadas_liste;

    private DatabaseReference databaseReference;
    private DatabaseReference kullaniciBilgi;
    private FirebaseAuth dAuth;

    private String gecerliKul_id;
    private View dmainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dmainView = inflater.inflate(R.layout.fragment_friends,container,false);
        darkadas_liste = (RecyclerView)dmainView.findViewById(R.id.arkadas_liste);
        dAuth = FirebaseAuth.getInstance();
        gecerliKul_id = dAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("arkadaslar").child(gecerliKul_id);
        databaseReference.keepSynced(true);
        kullaniciBilgi = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        kullaniciBilgi.keepSynced(true);
        darkadas_liste.setHasFixedSize(true);
        darkadas_liste.setLayoutManager(new LinearLayoutManager(getContext()));

        return dmainView;


    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single,
                FriendsViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String list_user_id = getRef(position).getKey();

                kullaniciBilgi.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("isim").getValue().toString();
                        String userThumb = dataSnapshot.child("kucuk_resim").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.setName(userName);
                        viewHolder.setUserResim(userThumb,getContext());

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Profili Aç","Mesaj Gönder"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Seç");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (i == 0){
                                            Intent profileIntent = new Intent(getContext(),ProfilActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if (i == 1){
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        darkadas_liste.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View view;


        public FriendsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDate(String date){
            TextView userNameView = (TextView) view.findViewById(R.id.user_single_durum);
            userNameView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = (TextView)view.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setUserResim(String resim, Context ctx){
            CircleImageView userResimView = (CircleImageView) view.findViewById(R.id.user_single_resim);
            Picasso.with(ctx).load(resim).placeholder(R.drawable.default_avatar).into(userResimView);

        }

        public void setUserOnline(String online_icon){
            ImageView userOnlineView = (ImageView)view.findViewById(R.id.user_single_online);

            if (online_icon.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
