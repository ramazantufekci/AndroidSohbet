package yazilim.dr.sohbet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button regBtn;
    private Button dGiris;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        dGiris = (Button)findViewById(R.id.btn_giris);
        regBtn = (Button)findViewById(R.id.reg_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_intent = new Intent(StartActivity.this,RegistryActivity.class);
                startActivity(reg_intent);
                finish();
            }
        });
        dGiris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }
}
