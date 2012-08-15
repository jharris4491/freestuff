package com.dj.craigslistapp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FreebiehunterActivity extends Activity {

	private static final String TAG = "FreebieHunterActivity";
	Button locationBtn;
	Button findStuffButton;
	TextView textView;
	private FreebieApp app;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Find Views
		app = (FreebieApp) getApplication();
		locationBtn = (Button) findViewById(R.id.buttonLocation);
		Log.i(TAG, "location button value: " + locationBtn.toString()); 
		findStuffButton = (Button) findViewById(R.id.buttonfindStuff);
		textView = (TextView) findViewById(R.id.locationDisplay);

		// Eventually deprecate this for on start to deserialize the App
		// variable and pull this in
		// Persistence for the location previously selected.
		String FILENAME = "location";
		byte[] locationBuffer = new byte[99];
		FileInputStream locationInputStream = null;
		if (app.getLocationURL().equals("")) {
			try {
				locationInputStream = openFileInput(FILENAME);
				locationInputStream.read(locationBuffer);
				locationInputStream.close();
				app.setLocationURL(new String(locationBuffer));
			} catch (FileNotFoundException e) {
				Log.e(TAG,
						"While reading from location file: FileNotFoundException Occured");
				Log.e(TAG, "Buffer Size = " + locationBuffer.length
						+ "Filename = " + FILENAME);
			} catch (IOException e) {
				Log.e(TAG,
						"While reading from location file: IOException Occured");
				Log.e(TAG, "Buffer Size = " + locationBuffer.length
						+ "Filename = " + FILENAME);
			}
		}
		
		Log.i(TAG, "Location Selection done, Setting OnClickListener for Location Button");
		// Set Attributes
		locationBtn.setOnClickListener(new OnClickListener() {
			// called when button is clicked
			public void onClick(View v) {
				Log.i(TAG, "onClick implementation");
				Log.d(TAG, "onClicked");
				// send to location activity to retrieve location.
				Bundle b = new Bundle();
				Log.i(TAG, "Made Bundle");
				b.putString("message", "Location Activity Started");
				Log.i(TAG, "Bundle message created");
				Intent locationSelectionIntent = new Intent(
						FreebiehunterActivity.this, Location.class);
				Log.i(TAG, "Intent created");
				locationSelectionIntent.putExtras(b);
				Log.i(TAG, "Attached bundle to intents");
				startActivity(locationSelectionIntent);
			}
		});
		
		Log.i(TAG, "Setting OnClickListener for Find Button");
		findStuffButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "onClicked");
				// send to location activity to retrieve location.
				Bundle bundle = new Bundle();
				bundle.putString("message", "FindStuff Activity Started");
				Intent findListingsIntent = new Intent(
						FreebiehunterActivity.this, FindStuff.class);
				findListingsIntent.putExtras(bundle);
				startActivity(findListingsIntent);
			}
		});

		if (app.getCity().length() > 0)
			textView.setText("Location: " + app.getCity());
		else
			textView.setText("Location: No Location Selected");
	}

}