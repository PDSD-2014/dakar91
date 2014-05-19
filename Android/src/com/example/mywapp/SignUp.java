package com.example.mywapp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SignUp extends Activity {
    private final static String TAG = "MyWhatsApp";
	private Button signUp;
	private EditText name, password, passwordCopy;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signUp = (Button)findViewById(R.id.signup);
        name = (EditText)findViewById(R.id.name);
        password = (EditText)findViewById(R.id.password);
        passwordCopy = (EditText)findViewById(R.id.password_copy);
        
		signUp.setOnClickListener(new View.OnClickListener() {
					
					@Override
		            public void onClick(View v) {
						
						if (!password.getText().toString().equals(passwordCopy.getText().toString()))
							Toast.makeText(SignUp.this, "Parola nu coincide!", Toast.LENGTH_SHORT).show();
						else {
							Properties prop = new Properties();
							
							prop.setProperty("name", name.getText().toString());
							prop.setProperty("password", password.getText().toString());
							
					        
					        try {
						        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS), "whatsapp.properties");
						        FileOutputStream fos = new FileOutputStream(file);
					        	prop.store(fos, null);
					        	fos.close();
					        	finish();
				        	} catch (Exception e) {
				        		Log.d(TAG, e.toString());	
				        	}
						}
		            }
		        });
        
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    }
 
    @Override
    public void onResume() {
    	super.onResume();
    }
 
    @Override
    public void onPause() {
    	super.onPause();
    }
 
    @Override
    public void onStop() {
    	super.onStop();
    }
 
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
 
    @Override
    public void onRestart() {
    	super.onRestart();
    }

}
