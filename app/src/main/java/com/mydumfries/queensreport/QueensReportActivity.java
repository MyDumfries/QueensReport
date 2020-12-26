package com.mydumfries.queensreport;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import com.mydumfries.queensreport.EventDataSQLHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class QueensReportActivity extends Activity {
    /** Called when the activity is first created. */
	public static final String SETTINGS="Settings";
    public static final String EDIT_ID="starttime";
    public static final String PERIOD="period";
	private EventDataSQLHelper eventsData;
	private Button timerbutton; 
	private Button QOSgoal;
	private Button OPSgoal;
	private Button extratimebutton;
	private Handler mHandler = new Handler();
	private long startTime;
	private long elapsedTime;
	private final int REFRESH_RATE = 1000;
	private String minutes,seconds,opponents;
	private long secs,mins=-1;
	private boolean secondhalf=false;
	private boolean extratimesecondhalf=false;
	private boolean extratime=false;
	private boolean liveupdate=false;
	private int squadavailable=0;
	List<String> Squad = new ArrayList<String>();
	static final int NOTE_DIALOG_ID=1;
	static final int PLAYER_PICKER_ID=2;
	static final int QUEENS_GOAL=3;
	static final int OPPS_GOAL=4;
	static final int CROWD=5;
	static final int SUBSTITUTION=6;
	private String note;
	private String note2;
	private String note3;
	private String temptext;
	private String temptext2;
	private int QOSgoals=0;
	private int OPSgoals=0;
	private int voicenotes=0;
	private int subs=0;
	private int yellows=0;
	private int reds=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getsquad();
        timerbutton = (Button) findViewById(R.id.timerbutton);
        extratimebutton=(Button) findViewById(R.id.ExtraTimeButton);
        extratimebutton.setVisibility(View.INVISIBLE);
        QOSgoal = (Button) findViewById(R.id.Queensgoal);
        OPSgoal = (Button) findViewById(R.id.OPSgoals);
        eventsData = new EventDataSQLHelper(this);
        final SQLiteDatabase db = eventsData.getReadableDatabase();
        String sql = "create table if not exists " + EventDataSQLHelper.TABLE + "( " + BaseColumns._ID
        		 + " integer primary key, " + EventDataSQLHelper.TIME + " text, "
 				+ EventDataSQLHelper.NOTES + " text);";
		Log.d("EventsData", "onCreate: " + sql);
		db.execSQL(sql);
		final Cursor cursor = getEvents(null,null);
		if (cursor.getCount()>0)
		{
			new AlertDialog.Builder(this)
	        .setTitle("Data Found In Database") 
	        .setMessage("Do you want to view or delete the data?") 
	        .setCancelable(false) 
	        .setPositiveButton("View", new DialogInterface.OnClickListener() 
	        { 
	             public void onClick(DialogInterface dialog, int id) 
	             {	 
	                DisplayData();
	             }
	          })	                	   
	          .setNegativeButton("Delete", new DialogInterface.OnClickListener() { 
	                   public void onClick(DialogInterface dialog, int id) { 
	                	   String sql2 = "drop table if exists " + EventDataSQLHelper.TABLE + " ;";
	               		   db.execSQL(sql2);
	               		mHandler.removeCallbacks(startTimer);
	    				timerbutton.setText("Kick Off");
	    				mins=-1;
	    				QOSgoals=0;
	    				OPSgoals=0;
	    				voicenotes=0;
	    				subs=0;
	    				yellows=0;
	    				reds=0;
	    				secondhalf=false;
	    				extratime=false;
	    				extratimesecondhalf=false;
	    				final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
	                   	Editor editor=prefs.edit();
	                   	editor.putString("CROWD","0");
	                   	editor.putString("OPSGOALS","0");
	                   	editor.putString("QOSGOALS","0");
	                   	editor.putInt(PERIOD,0);
	                   	editor.putInt("SUBS",subs);
	                   	editor.putInt("YELLOWS",yellows );
	                   	editor.putInt("REDS",reds);
	                   	editor.commit();
	                   	note=(String) "";
                		try {
        					note = URLEncoder.encode(note, "utf-8");
        				} catch (UnsupportedEncodingException e1) {
        					// TODO Auto-generated catch block
        					e1.printStackTrace();
        				}
                		new UpdateSite()
    					.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=&memo="+note);
	    				String sdcard= "/sdcard/QueensReport/";
	    				File fileList = new File( sdcard );

	    				if (fileList != null){ //check if dir is not null
	    				 File[] filenames = fileList.listFiles();
	    				 
	    				 for (File tmpf : filenames){ 
	    				  tmpf.delete();
	    				 }
	    				}
	               		String sql = "create table if not exists " + EventDataSQLHelper.TABLE + "( " + BaseColumns._ID
	                   		 + " integer primary key, " + EventDataSQLHelper.TIME + " text, "
	            				+ EventDataSQLHelper.NOTES + " text);";
	           		Log.d("EventsData", "onCreate: " + sql);
	           		db.execSQL(sql);
	                   } 
	               })  
	               .show(); 
		}
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
	    	android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
			Context Context=getApplicationContext();
			new AlertDialog.Builder(this)
		        .setTitle("Do You Want To Do A Live Update?") 
		               .setMessage("You will need Internet Connectivity for the whole session.") 
		               .setCancelable(false) 
		               .setPositiveButton("Yes", new DialogInterface.OnClickListener() { 
		                   public void onClick(DialogInterface dialog, int id) { 
		                        liveupdate=true;
//		                        int temp=getsquad();
		                        final Cursor cursor = getEvents(null,null);
		                		if (cursor.getCount()==0)
		                		{
		                			note=(String) "CLEAR";
		                			try {
		                				note = URLEncoder.encode(note, "utf-8");
		                			} catch (UnsupportedEncodingException e1) {
		                				// TODO Auto-generated catch block
		                				e1.printStackTrace();
		                			}
		                			new UpdateSite()
		                			.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=&event=&memo="+note);
		                		} 
		                   }
		               }) 
		               .setNegativeButton("No", null) 
		               .show();
			ArrayAdapter<?> adapter;
		while (squadavailable==0)
		{
			//wait until squad available updated
		}
        if (squadavailable==1)
        	{
        	adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Squad);
        	}
        	else
        	{
        		adapter=ArrayAdapter.createFromResource(this, R.array.squad, android.R.layout.simple_spinner_item);
        	}
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner player1= (Spinner) findViewById(R.id.player1);
        player1.setAdapter(adapter);
        player1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
            		note=(String) player1.getItemAtPosition(player1.getSelectedItemPosition());
            		try {
    					note = URLEncoder.encode(note, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
                    new UpdateSite()
					.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player1&memo="+note);
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player2= (Spinner) findViewById(R.id.player2);
        player2.setAdapter(adapter);
        player2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player2.getItemAtPosition(player2.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player2&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player3= (Spinner) findViewById(R.id.player3);
        player3.setAdapter(adapter);
        player3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player3.getItemAtPosition(player3.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player3&memo="+note);
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player4= (Spinner) findViewById(R.id.player4);
        player4.setAdapter(adapter);
        player4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player4.getItemAtPosition(player4.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player4&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player5= (Spinner) findViewById(R.id.player5);
        player5.setAdapter(adapter);
        player5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player5.getItemAtPosition(player5.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player5&memo="+note);
        	}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player6= (Spinner) findViewById(R.id.player6);
        player6.setAdapter(adapter);
        player6.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player6.getItemAtPosition(player6.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player6&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player7= (Spinner) findViewById(R.id.player7);
        player7.setAdapter(adapter);
        player7.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player7.getItemAtPosition(player7.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player7&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player8= (Spinner) findViewById(R.id.player8);
        player8.setAdapter(adapter);
        player8.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player8.getItemAtPosition(player8.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player8&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player9= (Spinner) findViewById(R.id.player9);
        player9.setAdapter(adapter);
        player9.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player9.getItemAtPosition(player9.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player9&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player10= (Spinner) findViewById(R.id.player10);
        player10.setAdapter(adapter);
        player10.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player10.getItemAtPosition(player10.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player10&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner player11= (Spinner) findViewById(R.id.player11);
        player11.setAdapter(adapter);
        player11.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) player11.getItemAtPosition(player11.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player11&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        //final Spinner player12= (Spinner) findViewById(R.id.player12);
        //player12.setAdapter(adapter);
        //final Spinner player13= (Spinner) findViewById(R.id.player13);
        //player13.setAdapter(adapter);
        final Spinner sub1= (Spinner) findViewById(R.id.sub1);
        sub1.setAdapter(adapter);
        sub1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) sub1.getItemAtPosition(sub1.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player12&memo="+note);
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner sub2= (Spinner) findViewById(R.id.sub2);
        sub2.setAdapter(adapter);
        sub2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) sub2.getItemAtPosition(sub2.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player13&memo="+note);
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner sub3= (Spinner) findViewById(R.id.sub3);
        sub3.setAdapter(adapter);
        sub3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) sub3.getItemAtPosition(sub3.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player14&memo="+note);
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner sub4= (Spinner) findViewById(R.id.sub4);
        sub4.setAdapter(adapter);
        sub4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View itemSelected,
        			int selectedItemPosition, long selectedId){
        		if (liveupdate)
        		{
        		note=(String) sub4.getItemAtPosition(sub4.getSelectedItemPosition());
        		try {
					note = URLEncoder.encode(note, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player15&memo="+note); 
        		}
        }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        final Spinner sub5= (Spinner) findViewById(R.id.sub5);
          sub5.setAdapter(adapter);
          sub5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
          	@Override
          	public void onItemSelected(AdapterView<?> parent, View itemSelected,
          			int selectedItemPosition, long selectedId){
          		if (liveupdate)
        		{
          		note=(String) sub5.getItemAtPosition(sub5.getSelectedItemPosition());
          		try {
  					note = URLEncoder.encode(note, "utf-8");
  				} catch (UnsupportedEncodingException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  				}
          		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player16&memo="+note); 
        		}
          }

  			@Override
  			public void onNothingSelected(AdapterView<?> arg0) {
  				// TODO Auto-generated method stub
  				
  			}
          });
          final Spinner sub6= (Spinner) findViewById(R.id.sub6);
          sub6.setAdapter(adapter);
          sub6.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
          	@Override
          	public void onItemSelected(AdapterView<?> parent, View itemSelected,
          			int selectedItemPosition, long selectedId){
          		if (liveupdate)
        		{
          		note=(String) sub6.getItemAtPosition(sub6.getSelectedItemPosition());
          		try {
  					note = URLEncoder.encode(note, "utf-8");
  				} catch (UnsupportedEncodingException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  				}
          		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player17&memo="+note); 
        		}
          }

  			@Override
  			public void onNothingSelected(AdapterView<?> arg0) {
  				// TODO Auto-generated method stub
  				
  			}
          });
          final Spinner sub7= (Spinner) findViewById(R.id.sub7);
          sub7.setAdapter(adapter);
          sub7.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
          	@Override
          	public void onItemSelected(AdapterView<?> parent, View itemSelected,
          			int selectedItemPosition, long selectedId){
          		if (liveupdate)
        		{
          		note=(String) sub7.getItemAtPosition(sub7.getSelectedItemPosition());
          		try {
  					note = URLEncoder.encode(note, "utf-8");
  				} catch (UnsupportedEncodingException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  				}
          		new UpdateSite()
				.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team+report&event=player18&memo="+note); 
        		}
          }

  			@Override
  			public void onNothingSelected(AdapterView<?> arg0) {
  				// TODO Auto-generated method stub
  				
  			}
          });
    }
    
    @Override 
    public void onBackPressed() { 
        new AlertDialog.Builder(this)
        .setTitle("Are You Sure You Want to Quit?") 
               .setMessage("All Data Will Be Lost!!!!!") 
               .setCancelable(false) 
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() { 
                   public void onClick(DialogInterface dialog, int id) { 
                	   final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
                   	Editor editor=prefs.edit();
                   	editor.putString("CROWD","0");
                   	editor.putString("OPSGOALS","0");
                   	editor.putString("QOSGOALS","0");
                   	editor.putInt("SUBS", 0);
                   	editor.putInt("YELLOWS", 0);
                   	editor.putInt("REDS", 0);
                   	editor.putInt(PERIOD,0);
                   	editor.commit();
                        QueensReportActivity.this.finish(); 
                   } 
               }) 
               .setNegativeButton("No", null) 
               .show(); 
    } 

    
public void startClick (View view){
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
	startTime = System.currentTimeMillis();
	final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
	Editor editor=prefs.edit();
	editor.putLong(EDIT_ID,startTime);
	editor.putInt(PERIOD,0);
	editor.commit();
	mHandler.removeCallbacks(startTimer);	
    mHandler.postDelayed(startTimer, 0);
    timerbutton.setText("Half Time");
    final TableRow newRow = new TableRow(this);
      addTextToRowWithValues(newRow, " ");
      addTextToRowWithValues(newRow, "00:00");
      addTextToRowWithValues(newRow, " ");
      addTextToRowWithValues(newRow, "Match has Kicked Off");
      eventsTable.addView(newRow,0);
		if (liveupdate)
		{ 
		    new UpdateSite()
			.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='00:00'&memo='The+Match+has+Kicked+Off'"); 
		}
    timerbutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            HalfTime(v);
        }
    });
    values.put(EventDataSQLHelper.TIME, "00:00");
    values.put(EventDataSQLHelper.NOTES, "The Match has Kicked Off");
    db.insert(EventDataSQLHelper.TABLE, null, values);
}

public void HalfTime (View view){
	if (mins<45)
	{
		final TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
		final TableRow newRow = new TableRow(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Half Time?");
			alertDialogBuilder
				.setMessage("Are You Sure? We haven't played 45 minutes yet.")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mHandler.removeCallbacks(startTimer);
						timerbutton.setText("2nd Half");
						secondhalf=true;
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, minutes + ":" + seconds);
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, "The Half Time whistle has blown.");
					    eventsTable.addView(newRow,0);
					    String time=minutes + ":" + seconds;
					    SQLiteDatabase db = eventsData.getWritableDatabase();
					    ContentValues values = new ContentValues();
					    values.put(EventDataSQLHelper.TIME, time);
					    values.put(EventDataSQLHelper.NOTES, "The Half Time whistle has blown.");
					    db.insert(EventDataSQLHelper.TABLE, null, values);
						if (liveupdate)
						{ 
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='The+Half+Time+whistle+has+blown.'");  
						}
						timerbutton.setOnClickListener(new View.OnClickListener() {
					        public void onClick(View v) {
					            SecondHalf(v);
					        }
					    });	
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
	}
	else
	{
	mHandler.removeCallbacks(startTimer);
	timerbutton.setText("2nd Half");
	secondhalf=true;
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, minutes + ":" + seconds);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "The Half Time whistle has blown.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, time);
    values.put(EventDataSQLHelper.NOTES, "The Half Time whistle has blown.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='The+Half+Time+whistle+has+blown.'"); 
	}
	timerbutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            SecondHalf(v);
        }
    });
	}
}

public void SecondHalf (View view){
	startTime = System.currentTimeMillis();
	final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
	Editor editor=prefs.edit();
	editor.putLong(EDIT_ID,startTime);
	editor.putInt(PERIOD,45);
	editor.commit();
	mHandler.removeCallbacks(startTimer);
    mHandler.postDelayed(startTimer, 0);
	timerbutton.setText("Full Time");
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "45:00");
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "The 2nd Half has kicked off.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, "45:00");
    values.put(EventDataSQLHelper.NOTES, "The 2nd Half has kicked off.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='45:00'&memo='The+2nd+Half+has+kicked+off.'"); 
	}
	timerbutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            FullTime(v);
        }
    });
}

public void FullTime (View view){
	if (mins<90)
	{
		final TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
		final TableRow newRow = new TableRow(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Full Time?");
			alertDialogBuilder
				.setMessage("Are You Sure? We haven't played 90 minutes yet.")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mHandler.removeCallbacks(startTimer);
						timerbutton.setText("Clear Data");
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, minutes + ":" + seconds);
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, "The Full Time whistle has blown.");
					    eventsTable.addView(newRow,0);
					    String time=minutes + ":" + seconds;
					    SQLiteDatabase db = eventsData.getWritableDatabase();
					    ContentValues values = new ContentValues();
					    values.put(EventDataSQLHelper.TIME, time);
					    values.put(EventDataSQLHelper.NOTES, "The Full Time whistle has blown.");
					    db.insert(EventDataSQLHelper.TABLE, null, values);
						if (liveupdate)
						{
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='The+Full+Time+whistle+has+blown.'");  
						}
					    Button extratimebutton=(Button) findViewById(R.id.ExtraTimeButton);
					    extratimebutton.setVisibility(View.VISIBLE);
					    extratimebutton.setOnClickListener(new View.OnClickListener() {
					        public void onClick(View v) {
					            ExtraTimeKickOff(v);
					        }
					    });
						timerbutton.setOnClickListener(new View.OnClickListener() {
					        public void onClick(View v) {
					            ClearData(v);
					        }
					    });	
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
	}
	else
	{
	mHandler.removeCallbacks(startTimer);
	timerbutton.setText("Clear Data");
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, minutes + ":" + seconds);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "The Full Time whistle has blown.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, time);
    values.put(EventDataSQLHelper.NOTES, "The Full Time whistle has blown.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='The+Full+Time+whistle+has+blown.'");  
	}
    Button extratimebutton=(Button) findViewById(R.id.ExtraTimeButton);
    extratimebutton.setVisibility(View.VISIBLE);
    extratimebutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ExtraTimeKickOff(v);
        }
    });
	timerbutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ClearData(v);
        }
    });
	}
}
public void ExtraTimeKickOff(View view)
{
	startTime = System.currentTimeMillis();
	final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
	Editor editor=prefs.edit();
	editor.putLong(EDIT_ID,startTime);
	editor.putInt(PERIOD,90);
	editor.commit();
	mHandler.removeCallbacks(startTimer);
    mHandler.postDelayed(startTimer, 0);
	extratimebutton.setText("Half Time");
	extratime=true;
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "90:00");
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "Extra Time has kicked off.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, time);
    values.put(EventDataSQLHelper.NOTES, "Extra Time has kicked off.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='90:00'&memo='Extra+Time+has+Kicked+Off.'");  
	}
	extratimebutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ExtraTimeHalfTime(v);
        }
    });
}
public void ExtraTimeHalfTime(View view )
{
	if (mins<105)
	{
		final TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
		final TableRow newRow = new TableRow(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Half Time?");
			alertDialogBuilder
				.setMessage("Are You Sure? We haven't played 15 minutes yet.")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mHandler.removeCallbacks(startTimer);
						extratimebutton.setText("2nd Half");
						extratimesecondhalf=true;
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, minutes + ":" + seconds);
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, "Half Time in Extra Time.");
					    eventsTable.addView(newRow,0);
					    String time=minutes + ":" + seconds;
					    SQLiteDatabase db = eventsData.getWritableDatabase();
					    ContentValues values = new ContentValues();
					    values.put(EventDataSQLHelper.TIME, time);
					    values.put(EventDataSQLHelper.NOTES, "The Half Time in Extra Time.");
					    db.insert(EventDataSQLHelper.TABLE, null, values);
						if (liveupdate)
						{ 
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='Half+Time+in+extra+time.'"); 
						}
						extratimebutton.setOnClickListener(new View.OnClickListener() {
					        public void onClick(View v) {
					            ExtraTimeSecondHalf(v);
					        }
					    });	
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
	}
	else
	{
	mHandler.removeCallbacks(startTimer);
	extratimebutton.setText("2nd Half");
	extratimesecondhalf=true;
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, minutes + ":" + seconds);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "Half Time in Extra Time.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, time);
    values.put(EventDataSQLHelper.NOTES, "The Half Time in Extra Time.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='Half+Time+in+extra+time.'"); 
	}
	extratimebutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ExtraTimeSecondHalf(v);
        }
    });
	}
}

public void ExtraTimeSecondHalf (View view){
	startTime = System.currentTimeMillis();
	final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
	Editor editor=prefs.edit();
	editor.putLong(EDIT_ID,startTime);
	editor.putInt(PERIOD,105);
	editor.commit();
	mHandler.removeCallbacks(startTimer);
    mHandler.postDelayed(startTimer, 0);
	extratimebutton.setText("Full Time");
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "105:00");
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "The 2nd Half of Extra Time has kicked off.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, time);
    values.put(EventDataSQLHelper.NOTES, "The 2nd Half of Extra Time has kicked off.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='105:00'&memo='The+2nd+Half+of+Extra+Time+has+Kicked+Off.'"); 
	}
	extratimebutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ExtraTimeFullTime(v);
        }
    });
}

public void ExtraTimeFullTime (View view){
	if (mins<120)
	{
		final TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
		final TableRow newRow = new TableRow(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Full Time?");
			alertDialogBuilder
				.setMessage("Are You Sure? We haven't played 120 minutes yet.")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mHandler.removeCallbacks(startTimer);
						extratimebutton.setText("Clear Data");
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, minutes + ":" + seconds);
					    addTextToRowWithValues(newRow, " ");
					    addTextToRowWithValues(newRow, "The Full Time whistle has blown.");
					    eventsTable.addView(newRow,0);
					    String time=minutes + ":" + seconds;
					    SQLiteDatabase db = eventsData.getWritableDatabase();
					    ContentValues values = new ContentValues();
					    values.put(EventDataSQLHelper.TIME, time);
					    values.put(EventDataSQLHelper.NOTES, "The Full Time whistle has blown.");
					    db.insert(EventDataSQLHelper.TABLE, null, values);
						if (liveupdate)
						{
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='The+Full+Time+whistle+has+blown.'");  
						}
					    extratimebutton.setVisibility(View.INVISIBLE);
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
	}
	else
	{
	mHandler.removeCallbacks(startTimer);
	extratimebutton.setText("Clear Data");
	TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
	final TableRow newRow = new TableRow(this);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, minutes + ":" + seconds);
    addTextToRowWithValues(newRow, " ");
    addTextToRowWithValues(newRow, "The Full Time whistle has blown.");
    eventsTable.addView(newRow,0);
    String time=minutes + ":" + seconds;
    SQLiteDatabase db = eventsData.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(EventDataSQLHelper.TIME, time);
    values.put(EventDataSQLHelper.NOTES, "The Full Time whistle has blown.");
    db.insert(EventDataSQLHelper.TABLE, null, values);
	if (liveupdate)
	{
	    new UpdateSite()
		.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='The+Full+Time+whistle+has+blown.'"); 
	}
    extratimebutton.setVisibility(View.INVISIBLE);
	timerbutton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ClearData(v);
        }
    });
	}
}

public void ClearData(View view){
	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	alertDialogBuilder.setTitle("Clear Data?");
	alertDialogBuilder
		.setMessage("Are You Sure? All voice recordings will be deleted.")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
		        final SQLiteDatabase db = eventsData.getReadableDatabase();
				String sql2 = "drop table if exists " + EventDataSQLHelper.TABLE + " ;";
        		   db.execSQL(sql2);
				mHandler.removeCallbacks(startTimer);
				timerbutton.setText("Kick Off");
				mins=-1;
				QOSgoals=0;
				OPSgoals=0;
				voicenotes=0;
				subs=0;
				yellows=0;
				reds=0;
				secondhalf=false;
				extratime=false;
				extratimesecondhalf=false;
				
				TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
				eventsTable.removeAllViews();
				String sdcard= "/storage/extSdCard/QueensReport/";
				File fileList = new File( sdcard );

				if (fileList != null){ //check if dir is not null
				 File[] filenames = fileList.listFiles();
				 
				 for (File tmpf : filenames){ 
				  tmpf.delete();
				 }
				}
			    
				timerbutton.setOnClickListener(new View.OnClickListener() {
			        public void onClick(View v) {
			            startClick(v);
			        }
			    });	
			}
		  })
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

}
public void YellowCard(View view){
	temptext=minutes + ":" + seconds;
	temptext2=" YELLOW CARD ";
	showDialog(PLAYER_PICKER_ID);
}
public void RedCard(View view){
	temptext=minutes + ":" + seconds;
	temptext2=" RED CARD ";
	showDialog(PLAYER_PICKER_ID);
}
public void Substitution(View view){
	temptext=minutes + ":" + seconds;
	temptext2=" SUBSTITUTION ";
	showDialog(SUBSTITUTION);
}
public void TakeNote(View view){
	temptext=minutes + ":" + seconds;
	showDialog(NOTE_DIALOG_ID);
}

public void RecordNote(View view){
	temptext=minutes + ":" + seconds;
	    final String LOG_TAG = "AudioRecordTest";
	    String mFileName = null;
	    //final String mFileName2=null;
	    //final RecordButton mRecordButton = null;
	    //MediaRecorder mRecorder = null;
	    //final PlayButton   mPlayButton = null;
	    final MediaPlayer   mPlayer = null;
	    	final MediaRecorder mRecorder = new MediaRecorder();
	        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	        mFileName= "/sdcard";
	        File sdcarddir = new File(mFileName + "/QueensReport");
	        if (!sdcarddir.exists())
	        	   sdcarddir.mkdir();
	        if (mins<0)
	        {
	        	voicenotes=voicenotes+1;
	        	mFileName += "/QueensReport/"+voicenotes+".3gp";
	        }
	        else
	        {
	        	mFileName += "/QueensReport/"+minutes + seconds+".3gp";
	        }
	        final String mFileName2=mFileName;
	        mRecorder.setOutputFile(mFileName);
	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	        try {
	            mRecorder.prepare();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }
	        mRecorder.start();
	        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Void Recorder");
			alertDialogBuilder
				.setMessage("Click stop to save.")
				.setCancelable(false)
				.setPositiveButton("Stop",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mRecorder.stop();
						mRecorder.release();
					}
				  });
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				final TableRow newRow = new TableRow(this);
				TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);					
			    addTextToRowWithValues(newRow, " ");
			    addTextToRowWithValues(newRow, temptext);
			    addTextToRowWithValues(newRow, " ");
			    addTextToRowWithValues(newRow, "Voice Recording (Click to Listen)");
			    SQLiteDatabase db = eventsData.getWritableDatabase();
			    ContentValues values = new ContentValues();
			    values.put(EventDataSQLHelper.TIME, temptext);
			    values.put(EventDataSQLHelper.NOTES, "Voice Recording (Click to Listen) "+ mFileName2);
			    db.insert(EventDataSQLHelper.TABLE, null, values);
			    newRow.setOnClickListener(new OnClickListener()
	            {
	            	public void onClick(View v)
	            	{
	          			MediaPlayer mPlayer2=new MediaPlayer();
	          			try{
	          				mPlayer2.setDataSource(mFileName2);
	          				mPlayer2.prepare();
	          				mPlayer2.start();
	          			}catch(IOException e){
	          				Log.e(LOG_TAG,"prepare() failed");
	          			}
	            	}
	            });
			    eventsTable.addView(newRow,0);
	}
	        
public void QueensGoal(View view){
	temptext=minutes + ":" + seconds;
	temptext2=" GOAL QUEENS!!! Scorer - ";
	showDialog(QUEENS_GOAL);
}
public void OpsGoal(View view){
	final EditText op2 = (EditText) findViewById(R.id.Opponents);
	opponents=op2.getText().toString();
	temptext=minutes + ":" + seconds;
	temptext2=" GOAL "+opponents+". Scorer - ";
	showDialog(OPPS_GOAL);
}
public void SetCrowd(View view){
	showDialog(CROWD);
}
private void updateTimer (float time){
	secs = (long)(time/1000);
	mins = (long)((time/1000)/60);
	SharedPreferences Settings = getSharedPreferences(SETTINGS,
			Context.MODE_PRIVATE);
	int KOtime = Settings.getInt(PERIOD, 0);

	secs = secs % 60;
	seconds=String.valueOf(secs);
	if(secs == 0){
		seconds = "00";
	}
	if(secs <10 && secs > 0){
		seconds = "0"+seconds;
	}

	mins = mins % 60;
	mins=mins+KOtime;
//	if (secondhalf)
//	{
//		mins=mins+45;
//	}
//	if (extratime)
//	{
//		mins=mins+45;
//	}
//	if (extratimesecondhalf)
//	{
//		mins=mins+15;
//	}
	minutes=String.valueOf(mins);
	if(mins == 0){
		minutes = "00";
	}
	if(mins <10 && mins > 0){
		minutes = "0"+minutes;
	}
	((TextView)findViewById(R.id.timer)).setText(minutes + ":" + seconds);
}
private Runnable startTimer = new Runnable() {
	   public void run() {
		   elapsedTime = System.currentTimeMillis() - startTime;
		   updateTimer(elapsedTime);
		   mHandler.postDelayed(this,REFRESH_RATE);
		}
	};
	private void addTextToRowWithValues(final TableRow tableRow, String text) 
    {
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setHeight(50);
        textView.setText(text);
        tableRow.addView(textView);
		tableRow.setMinimumHeight(50);
    }
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
		case NOTE_DIALOG_ID:
			final TableRow newRow = new TableRow(this);
			LayoutInflater inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout=inflater.inflate(R.layout.notedialog,(ViewGroup) findViewById(R.id.root));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			builder.setTitle("Add a Note");
			builder.setMessage("Enter Your Note.");
			builder.setPositiveButton(android.R.string.ok, 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					final EditText TitleText = (EditText) layout.findViewById(R.id.noteText);
					note=TitleText.getText().toString();
					TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);					
				    addTextToRowWithValues(newRow, " ");
				    addTextToRowWithValues(newRow, temptext);
				    addTextToRowWithValues(newRow, " ");
				    addTextToRowWithValues(newRow, note);
				    eventsTable.addView(newRow,0);
				    String time=minutes + ":" + seconds;
				    SQLiteDatabase db = eventsData.getWritableDatabase();
				    ContentValues values = new ContentValues();
				    values.put(EventDataSQLHelper.TIME, time);
				    values.put(EventDataSQLHelper.NOTES, note);
				    db.insert(EventDataSQLHelper.TABLE, null, values);
				    note=note.replace(" ", "+"); 
				    try {
    					note = URLEncoder.encode(note, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
					if (liveupdate)
					{ 
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='"+note+"'"); 
					}
					QueensReportActivity.this.removeDialog(NOTE_DIALOG_ID);
				}
				});
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					QueensReportActivity.this.removeDialog(NOTE_DIALOG_ID);
				}					
		});
			AlertDialog noteDialog=builder.create();
			return noteDialog;
		case CROWD:
			LayoutInflater inflater5=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout5=inflater5.inflate(R.layout.notedialog,(ViewGroup) findViewById(R.id.root));
			AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
			builder5.setView(layout5);
			final EditText CrowdText = (EditText) layout5.findViewById(R.id.noteText);
			CrowdText.setInputType(InputType.TYPE_CLASS_NUMBER);
			builder5.setTitle("Enter The Crowd");
			builder5.setMessage("What Is The Crowd?");
			builder5.setPositiveButton(android.R.string.ok, 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					final EditText CrowdText = (EditText) layout5.findViewById(R.id.noteText);
					CrowdText.setInputType(InputType.TYPE_CLASS_NUMBER);
					note=CrowdText.getText().toString();
					TextView crowd=(TextView) findViewById(R.id.Crowdtext);
					crowd.setText(note);
					String wnote=note;
					final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
					Editor editor=prefs.edit();
					editor.putString("CROWD",note);
					editor.commit();
					String time=minutes + ":" + seconds;
				    note="The+crowd+today+is+"+note; 
				    SQLiteDatabase db = eventsData.getWritableDatabase();
				    ContentValues values = new ContentValues();
				    values.put(EventDataSQLHelper.TIME, time);
				    values.put(EventDataSQLHelper.NOTES, note);
				    db.insert(EventDataSQLHelper.TABLE, null, values);
					if (liveupdate)
					{
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='"+note+"'");  
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=crowd&memo="+wnote);
					}
					QueensReportActivity.this.removeDialog(CROWD);
				}
				});
			builder5.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					QueensReportActivity.this.removeDialog(CROWD);
				}					
		});
			AlertDialog CrowdDialog=builder5.create();
			return CrowdDialog;
		case OPPS_GOAL:
			final TableRow newRow4 = new TableRow(this);
			LayoutInflater inflater4=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout4=inflater4.inflate(R.layout.notedialog,(ViewGroup) findViewById(R.id.root));
			AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
			builder4.setView(layout4);
			builder4.setTitle("Opposition Scorer");
			builder4.setMessage("Who scored?");
			builder4.setPositiveButton(android.R.string.ok, 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					final EditText TitleText = (EditText) layout4.findViewById(R.id.noteText);
					note=TitleText.getText().toString();
					String player=note;
					try {
    					player = URLEncoder.encode(player, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
					Button OPSgoal=(Button) findViewById(R.id.OPSgoals);
					OPSgoals=OPSgoals+1;
					OPSgoal.setText(String.valueOf(OPSgoals));
					final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
					Editor editor=prefs.edit();
					editor.putString("OPSGOALS",String.valueOf(OPSgoals));
					editor.commit();
					//OPSgoal.setText(OPSgoals);
					TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);					
				    addTextToRowWithValues(newRow4, " ");
				    addTextToRowWithValues(newRow4, temptext);
				    addTextToRowWithValues(newRow4, " ");
				    note=temptext2+note+" "+QOSgoals+"-"+OPSgoals;
				    addTextToRowWithValues(newRow4, note);
				    eventsTable.addView(newRow4,0);
				    String time=minutes + ":" + seconds;
				    SQLiteDatabase db = eventsData.getWritableDatabase();
				    ContentValues values = new ContentValues();
				    values.put(EventDataSQLHelper.TIME, time);
				    values.put(EventDataSQLHelper.NOTES, note);
				    db.insert(EventDataSQLHelper.TABLE, null, values);
				    note=note.replace(" ", "+"); 
					if (liveupdate)
					{ 
						String subsString = String.valueOf(OPSgoals);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=oppsscorertime"+subsString+"&memo="+minutes); 
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=oppsscorer"+subsString+"&memo="+player);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='"+note+"'"); 
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=oppsgoals&memo="+OPSgoals);
					}
					QueensReportActivity.this.removeDialog(OPPS_GOAL);
				}
				});
			builder4.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					QueensReportActivity.this.removeDialog(OPPS_GOAL);
				}					
		});
			AlertDialog OppGoalDialog=builder4.create();
			return OppGoalDialog;
		case PLAYER_PICKER_ID:
			LayoutInflater inflater2=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout2=inflater2.inflate(R.layout.yellowcard,(ViewGroup) findViewById(R.id.root2));
			final TableRow newRow2 = new TableRow(this);
			ArrayAdapter<?> adapter;
			if (squadavailable==1)
			{
				adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Squad);
			}
			else
			{
				adapter=ArrayAdapter.createFromResource(this, R.array.squad, android.R.layout.simple_spinner_item);
			}
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			final ArrayAdapter<CharSequence> adapter2=ArrayAdapter.createFromResource(QueensReportActivity.this, R.array.squad, android.R.layout.simple_spinner_item);
//	        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        final Spinner player= (Spinner) layout2.findViewById(R.id.player_spinner);
	        player.setAdapter(adapter);
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setView(layout2);
			builder2.setTitle("Pick Player");
			builder2.setPositiveButton(android.R.string.ok, 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					note=(String) player.getItemAtPosition(player.getSelectedItemPosition());
					//note="Stephen McKenna";
					TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
					String temptext3=temptext2;
					String player=note;
					try {
    					player = URLEncoder.encode(player, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
					temptext2+=note;
				    addTextToRowWithValues(newRow2, " ");
				    addTextToRowWithValues(newRow2, temptext);
				    addTextToRowWithValues(newRow2, " ");
				    addTextToRowWithValues(newRow2, temptext2);
				    eventsTable.addView(newRow2,0);
				    String time=minutes + ":" + seconds;
				    SQLiteDatabase db = eventsData.getWritableDatabase();
				    ContentValues values = new ContentValues();
				    values.put(EventDataSQLHelper.TIME, time);
				    values.put(EventDataSQLHelper.NOTES, temptext2);
				    db.insert(EventDataSQLHelper.TABLE, null, values);
				    note=temptext2.replace(" ", "+"); 
					if (liveupdate)
					{
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='"+note+"'"); 
					    if (temptext3.equals(" YELLOW CARD "))
					    {
					    	yellows=yellows+1;
					    	String subsString = String.valueOf(yellows);
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=yellow"+subsString+"&memo="+player);
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=yellows&memo="+yellows);
					    }
					    if (temptext3.equals(" RED CARD "))
					    {
					    	reds=reds+1;
					    	String subsString = String.valueOf(reds);
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=red"+subsString+"&memo="+player);
						    new UpdateSite()
							.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=reds&memo="+reds);
					    }
					}
					QueensReportActivity.this.removeDialog(PLAYER_PICKER_ID);
				}
				});
			builder2.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					QueensReportActivity.this.removeDialog(PLAYER_PICKER_ID);
				}					
		});
			AlertDialog noteDialog2=builder2.create();
			return noteDialog2;
		case SUBSTITUTION:
			LayoutInflater inflater6=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout6=inflater6.inflate(R.layout.substitution,(ViewGroup) findViewById(R.id.root3));
			final TableRow newRow6 = new TableRow(this);
			if (squadavailable==1)
			{
				adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Squad);
			}
			else
			{
				adapter=ArrayAdapter.createFromResource(this, R.array.squad, android.R.layout.simple_spinner_item);
			}
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			final ArrayAdapter<CharSequence> adapter6=ArrayAdapter.createFromResource(QueensReportActivity.this, R.array.squad, android.R.layout.simple_spinner_item);
//	        adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        final Spinner playerOn= (Spinner) layout6.findViewById(R.id.player1_spinner);
	        playerOn.setAdapter(adapter);
	        final Spinner playerOff= (Spinner) layout6.findViewById(R.id.player2_spinner);
	        playerOff.setAdapter(adapter);
			AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
			builder6.setView(layout6);
			builder6.setTitle("Substitution");
			builder6.setPositiveButton(android.R.string.ok, 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					String subtime=temptext.substring(0,2);
					note=(String) playerOn.getItemAtPosition(playerOn.getSelectedItemPosition());
					String PlayerOn=note;
					try {
    					PlayerOn = URLEncoder.encode(PlayerOn, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
					TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
					temptext2+=note;
					note=(String) playerOff.getItemAtPosition(playerOff.getSelectedItemPosition());
					String PlayerOff=note;
					try {
    					PlayerOff = URLEncoder.encode(PlayerOff, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
					temptext2+=" on for "+note;
				    addTextToRowWithValues(newRow6, " ");
				    addTextToRowWithValues(newRow6, temptext);
				    addTextToRowWithValues(newRow6, " ");
				    addTextToRowWithValues(newRow6, temptext2);
				    eventsTable.addView(newRow6,0);
				    String time=minutes + ":" + seconds;
				    SQLiteDatabase db = eventsData.getWritableDatabase();
				    ContentValues values = new ContentValues();
				    values.put(EventDataSQLHelper.TIME, time);
				    values.put(EventDataSQLHelper.NOTES, temptext2);
				    db.insert(EventDataSQLHelper.TABLE, null, values);
				    note=temptext2.replace(" ", "+");
				    subs=subs+1;
					if (liveupdate)
					{
						String subsString = String.valueOf(subs);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='"+note+"'"); 
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=sub"+subsString+"&memo="+PlayerOn);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=subfor"+subsString+"&memo="+PlayerOff);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=subtime"+subsString+"&memo="+subtime);
					}
					final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
					Editor editor=prefs.edit();
					editor.putInt("SUBS",subs);
					editor.commit();
					QueensReportActivity.this.removeDialog(SUBSTITUTION);
				}
				});
			builder6.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					QueensReportActivity.this.removeDialog(SUBSTITUTION);
				}					
		});
			AlertDialog noteDialog6=builder6.create();
			return noteDialog6;
		case QUEENS_GOAL:
			LayoutInflater inflater3=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout3=inflater3.inflate(R.layout.yellowcard,(ViewGroup) findViewById(R.id.root2));
			final TableRow newRow3 = new TableRow(this);
			if (squadavailable==1)
			{
				adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Squad);
			}
			else
			{
				adapter=ArrayAdapter.createFromResource(this, R.array.squad, android.R.layout.simple_spinner_item);
			}
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			final ArrayAdapter<CharSequence> adapter3=ArrayAdapter.createFromResource(QueensReportActivity.this, R.array.squad, android.R.layout.simple_spinner_item);
//	        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        final Spinner player3= (Spinner) layout3.findViewById(R.id.player_spinner);
	        player3.setAdapter(adapter);
			AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
			builder3.setView(layout3);
			builder3.setTitle("Who Scored?");
			builder3.setPositiveButton(android.R.string.ok, 
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					Button QOSgoal=(Button) findViewById(R.id.Queensgoal);
					QOSgoals=QOSgoals+1;
					QOSgoal.setText(String.valueOf(QOSgoals));
					final SharedPreferences prefs = getSharedPreferences(SETTINGS,Context.MODE_PRIVATE);
					Editor editor=prefs.edit();
					editor.putString("QOSGOALS",String.valueOf(QOSgoals));
					editor.commit();
					note=(String) player3.getItemAtPosition(player3.getSelectedItemPosition());
					String player=note;
					try {
    					player = URLEncoder.encode(player, "utf-8");
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
					//note="Stephen McKenna";
					TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
					temptext2+=note+ " "+QOSgoals+"-"+OPSgoals;
				    addTextToRowWithValues(newRow3, " ");
				    addTextToRowWithValues(newRow3, temptext);
				    addTextToRowWithValues(newRow3, " ");
				    addTextToRowWithValues(newRow3, temptext2);
				    eventsTable.addView(newRow3,0);
				    String time=minutes + ":" + seconds;
				    SQLiteDatabase db = eventsData.getWritableDatabase();
				    ContentValues values = new ContentValues();
				    values.put(EventDataSQLHelper.TIME, time);
				    values.put(EventDataSQLHelper.NOTES, temptext2);
				    db.insert(EventDataSQLHelper.TABLE, null, values);
				    note=temptext2.replace(" ", "+"); 
					if (liveupdate)
					{
						String subsString = String.valueOf(QOSgoals);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=qosscorertime"+subsString+"&memo="+minutes); 
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=qosscorer"+subsString+"&memo="+player);
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=livedata&event='"+time+"'&memo='"+note+"'");
					    new UpdateSite()
						.execute("http://www.qosfan.co.uk/RemoteUpdate.php?tables=team&event=qosgoals&memo="+QOSgoals);
					}
					QueensReportActivity.this.removeDialog(QUEENS_GOAL);
				}
				});
			builder3.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					QueensReportActivity.this.removeDialog(QUEENS_GOAL);
				}					
		});
			AlertDialog noteDialog3=builder3.create();
			return noteDialog3;
		}
		return null;
	}
	private Cursor getEvents(String search,String sortorder) {
        SQLiteDatabase db = eventsData.getReadableDatabase();
        Cursor cursor = db.query(EventDataSQLHelper.TABLE, null, search, null, null, null, sortorder);
        startManagingCursor(cursor);
        return cursor;
      }
	public void DisplayData()
	{
		Cursor cursor = getEvents(null,null);
		TableLayout eventsTable = (TableLayout) findViewById(R.id.tableLayout1);
		while (cursor.moveToNext()) 
        {
			 final TableRow newRow = new TableRow(this);
			 String mFileName2;
			 mFileName2="";
			 final String mFileName= "/storage/extSdCard";
        	 String time = cursor.getString(1);
        	 final String event = cursor.getString(2);
        	 if (event.contains("Voice Recording (Click to Listen) "))
	        	 {
	        			  mFileName2=event.substring(34);
	        	 }
        	 final String mFileName3=mFileName2;
        	 addTextToRowWithValues(newRow, " ");
        	 addTextToRowWithValues(newRow, time);
        	 addTextToRowWithValues(newRow, " ");
        	 addTextToRowWithValues(newRow, event);
        	 newRow.setOnClickListener(new OnClickListener()
	            {
	            	public void onClick(View v)
	            	{
	            		final String LOG_TAG = "AudioRecordTest";
	          			MediaPlayer mPlayer2=new MediaPlayer();
	          			try{
	          				mPlayer2.setDataSource(mFileName3);
	          				mPlayer2.prepare();
	          				mPlayer2.start();
	          			}catch(IOException e){
	          				Log.e(LOG_TAG,"prepare() failed");
	          			}
	            	}
	            });
        	 eventsTable.addView(newRow,0);
        }
        timerbutton.setText("Clear Data");
 	    timerbutton.setOnClickListener(new View.OnClickListener() {
 	        public void onClick(View v) {
 	            ClearData(v);
 	        }
 	    });
 	   Button extratimebutton=(Button) findViewById(R.id.ExtraTimeButton);
	    extratimebutton.setVisibility(View.VISIBLE);
	    extratimebutton.setText("Continue");
	    extratimebutton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	SharedPreferences Settings = getSharedPreferences(SETTINGS,
	    				Context.MODE_PRIVATE);
	    		startTime = Settings.getLong(EDIT_ID, 0);
	    		int period = Settings.getInt(PERIOD,0);
	    		String scrowd=Settings.getString("CROWD","0");
	    		String sQOSgoals=Settings.getString("QOSGOALS", "0");
	    		String sOPSgoals=Settings.getString("OPSGOALS", "0");
	    		subs=Settings.getInt("SUBS", 0);
	    		yellows=Settings.getInt("YELLOWS", 0);
	    		reds=Settings.getInt("REDS", 0);
	    		TextView crowd=(TextView) findViewById(R.id.Crowdtext);
				crowd.setText(scrowd);
				OPSgoal.setText(sOPSgoals);
				QOSgoal.setText(sQOSgoals);
				try {
				    QOSgoals = Integer.parseInt(sQOSgoals);
				} catch(NumberFormatException nfe) {
				   System.out.println("Could not parse " + nfe);
				} 
				try {
				    OPSgoals = Integer.parseInt(sOPSgoals);
				} catch(NumberFormatException nfe) {
				   System.out.println("Could not parse " + nfe);
				} 
				
	    		long debug1=startTime;
	    		mHandler.removeCallbacks(startTimer);	
	    	    mHandler.postDelayed(startTimer, 0);
	    	    if (period == 0)
	    	    {
	    	    timerbutton.setText("Half Time");
	    	    timerbutton.setOnClickListener(new View.OnClickListener() {
	    	        public void onClick(View v) {
	    	            HalfTime(v);
	    	        }
	    	    });
	    	    }
	    	    if (period == 45)
	    	    {
	    	    timerbutton.setText("Full Time");
	    	    timerbutton.setOnClickListener(new View.OnClickListener() {
	    	        public void onClick(View v) {
	    	           FullTime(v);
	    	        }
	    	    });
	    	    }
	            Button extratimebutton=(Button) findViewById(R.id.ExtraTimeButton);
	            extratimebutton.setVisibility(View.INVISIBLE);
	        }
	    });
	}
	private class UpdateSite extends AsyncTask<String, Void, Boolean> {
		protected Boolean doInBackground(String... strings) {
			String string = strings[0];
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(string);
			HttpResponse response;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
	}
	void getsquad()
	{
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        //do network action in this function
		    	XmlPullParserFactory factory = null;
				try {
					factory = XmlPullParserFactory.newInstance();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				factory.setNamespaceAware(true);
				XmlPullParser xpp = null;
				try {
					xpp = factory.newPullParser();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				URL xmlUrl = null;
				try {
					xmlUrl = new URL(
							"http://www.qosfan.co.uk/Squadxml.php");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// set the input for the parser using an InputStreamReader
				try {
					xpp.setInput(xmlUrl.openStream(), null);
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					squadavailable=2;
					e.printStackTrace();
					return;
				}
//					booksData = new EventDataSQLHelper(this);
					try {
						processBooks(xpp);
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				squadavailable=1;
		    }
		});
		thread.start();
	}
	private void processBooks(XmlPullParser books)
			throws XmlPullParserException, IOException {
		Squad.add("Please Select");
		int eventType = books.getEventType();
		boolean bFoundScores = false;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String strName = books.getName();
				if (strName.equals("player")) {
					bFoundScores = true;
					String name = books.getAttributeValue(null, "title");
					String number = books.getAttributeValue(null, "number");
					Squad.add(name);
			}
			}
			eventType = books.next();
		}
		// Handle no scores available
		if (bFoundScores == false) {
			squadavailable=2;
		}
		}
}