package hci.com.tentativecapstoneui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        }
        else {
            Thread logoTimer = new Thread() {
                public void run() {
                    try {
                        int logoTimer = 0;
                        while (logoTimer < 1000) {
                            sleep(100);
                            logoTimer = logoTimer + 100;
                        }
                        startActivity(new Intent(SplashScreen.this, Login.class));
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        finish();
                    }
                }
            };

            logoTimer.start();
        }
    }
}
