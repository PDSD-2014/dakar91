package com.example.mywapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
 
public class FirstScreen extends Activity{

    private final static String TAG = "MyWhatsApp";
	private final String SERVER_IP = "10.22.3.117";
	private final int PORT = 9000;	
	private final int SIGNED_UP = 1;
	private String DEVICE_ID, NAME, PASSWD;
	private Connection con;
	private Request whatToSend;
	private ArrayList<String> userNames;
	private ArrayList<User> users;
	private ArrayAdapter<String> arrayAdapter;
	private ListView userList;
	private Button btn;
	private boolean isDone = false, authenticated = false, prompted = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
		userNames = new ArrayList<String>();
        Log.d(TAG, "onCreate first screen.");
		DEVICE_ID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		users = new ArrayList<User>();

		userList = (ListView)findViewById(R.id.users);		
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNames);
        userList.setAdapter(arrayAdapter);
        
        userList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                	try {
                		if (!users.get(position).getImei().equals(DEVICE_ID)) {
		                    Intent nextScreen = new Intent(getApplicationContext(), MyWhatsapp.class);
		                    nextScreen.putExtra("from_imei", DEVICE_ID);
		                    nextScreen.putExtra("to_imei", users.get(position).getImei());
		                    nextScreen.putExtra("to_name", users.get(position).getName());
		                    startActivity(nextScreen); 
                		}
                	} catch (Exception e) {
                        Log.d(TAG, e.toString()); 
                	}
                 }
        	});
        
		
        
    }
    
    @Override
    public void onStart() {
        Log.d(TAG, "onStart first screen."); 
    	super.onStart();
    }
 
    @Override
    public void onResume() {

    	Log.d(TAG, "on resume inceput first screen");
    	if (authenticated) {
    		updateUsers();
    	} else {
    		if (!createConnection()) {
    			if (prompted) { 
    				finish();
    			} else {
	    			Intent signUp = new Intent(getApplicationContext(), SignUp.class);
	    			prompted = true;
	    			startActivityForResult(signUp, SIGNED_UP);
    			}
    		} else {
    			updateUsers();
    		}
    	}
        Log.d(TAG, "onResume first screen."); 
    	super.onResume();
    }
    
    private boolean createConnection () {

    	Log.d(TAG, "on create connection first screen");
    	try {
			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS), "whatsapp.properties");
			FileInputStream fis = new FileInputStream(file);
			Properties prop = new Properties();
			prop.load(fis);	
			this.PASSWD = prop.getProperty("password");
			this.NAME = prop.getProperty("name");
			fis.close();
			Connection.getInstance().startConnection(SERVER_IP, DEVICE_ID, PORT, NAME, PASSWD);
			authenticated = true;
		} catch (Exception e) {
			Log.d(TAG, e.toString());
			return false;
		}
    	
    	return true;
    }
        
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "on activityresult first screen");
        switch (requestCode) {
	        case SIGNED_UP:
	        	switch(resultCode) {
		        	case RESULT_OK:
		            	Log.d(TAG, "on activityresult OK");
		        		createConnection();
		        		break;
	        	}
        }
    }
    
    private void updateUsers () {
    	new Thread(new Runnable() {
        	
        	@Override
        	public void run () {
        		isDone = false;
        		
        		while(!isDone) {
        			try {
	        			if (!isDone) {
	        				UsersResponse resp = (UsersResponse)Connection.getInstance().sendThis(new GetUsers(DEVICE_ID));
	        				users = resp.users;
							//Log.d(TAG, DEVICE_ID + " sunt dupa cerere imediat ");
	        				
	        				
	        				userList.post(new Runnable() {
    							@Override
    							public void run() {
    								try {
	    								userNames.clear();

    									//Log.d(TAG, DEVICE_ID + " momentan am " + users.size());
	    		        				for (User u : users) {
	    		        					if (!u.getImei().equals(DEVICE_ID)) {
	    		        						userNames.add(u.getName());
	    		        					} else {
	    		        						userNames.add("MyPhone");
	    		        					}
        									//Log.d(TAG, DEVICE_ID + " " + u.getImei());
	    		        				}
	    		        				arrayAdapter.notifyDataSetChanged();

    								} catch (Exception e) {
    									Log.d(TAG, e.toString());
    								}
    							}	
    						});
	        			}
						Thread.sleep(5000);
					} catch (Exception e) {
						Log.d(TAG, e.toString());
					}
        		}
        	}
        }).start();
    }
 
    @Override
    public void onPause() {
    	isDone = true;
        Log.d(TAG, "onPause first screen."); 
    	super.onPause();
    }
 
    @Override
    public void onStop() {
        Log.d(TAG, "onStop first screen."); 
    	super.onStop();
    }
 
    @Override
    public void onDestroy() {
    	if (authenticated)
    		Connection.getInstance().stopConnection();
        Log.d(TAG, "onDestroy first screen."); 
    	super.onDestroy();
    }
 
    @Override
    public void onRestart() {
    	super.onRestart();
    }
 
    @Override
    public void onSaveInstanceState(Bundle state) {
        // apelarea metodei din activitatea parinte este recomandata, dar nu obligatorie
    	super.onSaveInstanceState(state);
    }
 
    @Override
    public void onRestoreInstanceState(Bundle state) {
        // apelarea metodei din activitatea parinte este recomandata, dar nu obligatorie
    	super.onRestoreInstanceState(state);
    }

}
