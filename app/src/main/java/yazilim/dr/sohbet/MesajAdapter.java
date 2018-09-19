package yazilim.dr.sohbet;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ramazan on 9/14/2018.
 */

public class MesajAdapter extends RecyclerView.Adapter<MesajAdapter.MesajViewHolder>{

    private List<Mesajlar> dmesajList;
    private FirebaseAuth dAuth;
    private DatabaseReference databaseReference;

    public MesajAdapter(List<Mesajlar> dmesajList) {
        this.dmesajList = dmesajList;
    }

    @Override
    public MesajViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mesaj_single_layout,parent,false);
        return new MesajViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MesajAdapter.MesajViewHolder holder, int position) {

        String gecerli_kul_id = dAuth.getCurrentUser().getUid();

        Mesajlar c = dmesajList.get(position);

        String from_user = c.getFrom();
        String mesajType = c.getType();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(from_user);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String isim = dataSnapshot.child("isim").getValue().toString();
                String resim = dataSnapshot.child("kucuk_resim").getValue().toString();

                holder.isimView.setText(isim);
                Picasso.with(holder.profilImage.getContext()).load(resim).placeholder(R.drawable.default_avatar).into(holder.profilImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mesajType.equals("text")){
            holder.mesajText.setText(c.getMesaj());
            holder.mesajResim.setVisibility(View.INVISIBLE);
        }else {
            holder.mesajText.setVisibility(View.INVISIBLE);
            holder.mesajResim.setVisibility(View.VISIBLE);
            Picasso.with(holder.profilImage.getContext()).load(c.getMesaj()).placeholder(R.drawable.default_avatar).into(holder.mesajResim);
        }



        if (from_user.equals(gecerli_kul_id)){
            holder.mesajText.setBackgroundColor(Color.WHITE);
            holder.mesajText.setTextColor(Color.BLACK);
        }else {
            holder.mesajText.setBackgroundResource(R.drawable.mesaj_text_backround);
            holder.mesajText.setTextColor(Color.WHITE);
        }
        holder.mesajText.setText(c.getMesaj());

    }

    @Override
    public int getItemCount() {
        return dmesajList.size();
    }

    public class MesajViewHolder extends RecyclerView.ViewHolder{

        public TextView mesajText;
        public CircleImageView profilImage;
        public TextView isimView;
        public ImageView mesajResim;

        public MesajViewHolder(View view){
            super(view);

            mesajText = (TextView)view.findViewById(R.id.mesaj_text_layout);
            profilImage = (CircleImageView)view.findViewById(R.id.mesaj_profile_layout);
            isimView = (TextView)view.findViewById(R.id.name_text_layout);
            mesajResim = (ImageView)view.findViewById(R.id.mesaj_resim_layout);
            dAuth = FirebaseAuth.getInstance();
        }
    }
}
