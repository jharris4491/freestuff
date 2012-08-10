package com.dj.craigslistapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Location extends Activity {
	private static final String TAG = "LocationActivity";
	private FreebieApp app;
	TextView tv;
	ListView lv;
	private String state = "";
	String[] states = new String[50];

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		app = (FreebieApp) getApplication();
		app.setThreadIOException(false);
		String craigsListURL = "http://www.craigslist.org/about/sites";
		// Starts the background task and lets the UI thread continue
		new PullLocationList().execute(craigsListURL);
		// for getting a message if necessary
		String message = (String) getIntent().getSerializableExtra("message");

		// Find Views

		tv = (TextView) findViewById(R.id.locationtext);
		lv = (ListView) findViewById(R.id.statelist);

		// Set properties
		tv.setText(message);

		/*
		 * OLD STUFF Do what you would like DJ, figured it was at least
		 * reference for you to see what I did. Source source = null; String
		 * craigsListURL = "http://www.craigslist.org/about/sites";
		 * 
		 * try { source = new Source(new URL(craigsListURL)); } catch (Exception
		 * e) { // Yea this needs some work but I like the comments haha
		 * tv.append("\n" + e.getClass().getName()); // TODO Auto-generated
		 * catch block e.printStackTrace();
		 * tv.append("\nfail in getting website"); Intent i = new Intent();
		 * i.setAction(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME);
		 * Location.this.startActivity(i); finish(); }
		 * 
		 * source.fullSequentialParse(); app.setSource(source);
		 * 
		 * int i = 0; List<Element> state_list = getStateNames(source);
		 * app.setState_list(state_list);
		 * 
		 * // Code to create List of clicakble states. Once clicked, we will
		 * show // the list of cities to choosefrom.
		 * 
		 * Iterator<Element> iterator = state_list.iterator(); while
		 * (iterator.hasNext()) { if (i >= 50) break; states[i] =
		 * iterator.next().getContent().toString() + "\n"; i++; }
		 * 
		 * app.setStates(states);
		 */

		// UI Thread cannot progress at this point so we have to wait
		// Would be better to wait based on a progress bar but not
		// implemented at the moment.
		while (app.getStates() == null && !app.isThreadIOException()) {
			;
		}
		// Checking if there was a problem pulling the webpage content
		if (app.isThreadIOException()) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			Location.this.startActivity(intent);
			finish();
		} 
		else {
			states = app.getStates();
			// First paramenter - Context
			// Second parameter - Layout for the row
			// Third parameter - ID of the View to which the data is written
			// Forth - the Array of data
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					states);

			// Assign adapter to ListView
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					state = states[position];
					app.setState(state.trim());
					Log.d(TAG, "onClicked");
					Log.d(TAG, state);
					// send to location activity to retreive location.
					// Intent i = new Intent("com.dj.craigslistapp.CityList");
					Intent i = new Intent(Location.this, CityList.class);
					startActivity(i);
				}
			});
		}

	}

	private static List<Element> getStateNames(Source source) {

		Segment states = new Segment(source, 0, source.length());
		List<Element> state_list = states
				.getAllElementsByClass("state_delimiter");

		return state_list;
	}

	private class PullLocationList extends
			AsyncTask<String, Integer, ArrayList<String>> {

		@Override
		/**
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 * 
		 * This is worker portion of the AysncTask it will pull the HTML content
		 * from the page and then parse it. When finished it will store all the
		 * information in the FreebieApp Object.
		 * 
		 * @param url while the function can take more than one String the use
		 * case at the moment will only ever require one url
		 * 
		 * @return ArrayList<String> this is passed to the postProcessing
		 * function so it can update the FreebieApp Object
		 */
		protected ArrayList<String> doInBackground(String... url) {
			// set to null to act as a flag to see if the UI thread must wait or
			// not
			app.setStates(null);
			publishProgress(10);
			URL pageToPull;

			try {
				pageToPull = new URL(url[0]);
				publishProgress(30);
				try {

					Source pageContent = new Source(pageToPull);
					publishProgress(60);
					pageContent.fullSequentialParse();
					app.setSource(pageContent);
				} catch (IOException e) {
					app.setThreadIOException(true);
					e.printStackTrace();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				// should never happen with static string passed in
				// change later if string is no longer static
			}
			if (!app.isThreadIOException()) {
				publishProgress(70);

				Source pageContent = app.getSource();
				List<Element> rawStateList = getStateNames(pageContent);

				// What is the purpose of setting all these variables... are
				// they
				// ever used again?
				app.setState_list(rawStateList);
				ArrayList<String> stateList = new ArrayList();

				publishProgress(90);

				// What is the purpose of checking if i < 50?
				for (int i = 0; i < rawStateList.size() && i < 50; i++)
					stateList.add(rawStateList.get(i).toString());

				return stateList;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<String> stateList) {
			if (stateList != null) {
				publishProgress(100);
				app.setStates((String[]) stateList.toArray());
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0] * 100);
		}
	}
}
