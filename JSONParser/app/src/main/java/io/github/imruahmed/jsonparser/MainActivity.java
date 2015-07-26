package io.github.imruahmed.jsonparser;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

    // The JSON REST Service I will pull from
    static String yahooStockInfo = "http://query.yahooapis.com/v1/public/yql?" + "q=select%20*%20from%20yahoo.finance.quote%20where%20symbol" + "%20in%20(%22YHOO%22)&format=json&env=store%3A%2F%2" + "Fdatatables.org%2Falltableswithkeys&callback=cbfunc";

    // Will hold the values I pull from the JSON
    static String stockSymbol = "";
    static String stockDaysLow = "";
    static String stockDaysHigh = "";
    static String stockChange = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get any saved data
        super.onCreate(savedInstanceState);

        // Point to the name for the layout xml file used
        setContentView(R.layout.activity_main);

        // Call for doInBackground() in MyAsyncTask to be executed
        new MyAsyncTask().execute();

    }
    // Use AsyncTask if you need to perform background tasks, but also need
    // to change components on the GUI. Put the background operations in
    // doInBackground. Put the GUI manipulation code in onPostExecute

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... arg0) {

            // HTTP Client that supports streaming uploads and downloads
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            // Define that I want to use the POST method to grab data from
            // the provided URL
            HttpPost httppost = new HttpPost(yahooStockInfo);

            // Web service used is defined
            httppost.setHeader("Content-type", "application/json");

            // Used to read data from the URL
            InputStream inputStream = null;

            // Will hold the whole all the data gathered from the URL
            String result = null;

            try {

                // Get a response if any from the web service
                HttpResponse response = httpclient.execute(httppost);

                // The content from the requested URL along with headers, etc.
                HttpEntity entity = response.getEntity();

                // Get the main content from the URL
                inputStream = entity.getContent();

                // JSON is UTF-8 by default
                // BufferedReader reads data from the InputStream until the Buffer is full
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                // Will store the data
                StringBuilder stringBuilder = new StringBuilder();

                String line;

                // Read in the data from the Buffer untilnothing is left
                while ((line = reader.readLine()) != null)
                {

                    // Add data from the buffer to the StringBuilder
                    stringBuilder.append(line + "\n");
                }

                // Store the complete data in result
                result = stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {

                // Close the InputStream when you're done with it
                try{if(inputStream != null)inputStream.close();}
                catch(Exception e){}
            }

            // Holds Key Value pairs from a JSON source
            JSONObject jsonObject;
            try {

                // Delete cbfunc( and ); from the results
                result = result.substring(11);
                result = result.substring(0, result.length()-2);

                // Print out all the data read in
                // Log.v("JSONParser RESULT ", result);

                // Get the root JSONObject
                jsonObject = new JSONObject(result);


                // Get the JSON object named query
                JSONObject queryJSONObject = jsonObject.getJSONObject("query");

                // Get the JSON object named results inside of the query object
                JSONObject resultsJSONObject = queryJSONObject.getJSONObject("results");

                // Get the JSON object named quote inside of the results object
                JSONObject quoteJSONObject = resultsJSONObject.getJSONObject("quote");

                // Get the JSON Strings in the quote object
                stockSymbol = quoteJSONObject.getString("symbol");
                stockDaysLow = quoteJSONObject.getString("DaysLow");
                stockDaysHigh = quoteJSONObject.getString("DaysHigh");
                stockChange = quoteJSONObject.getString("Change");

                // EXTRA STUFF THAT HAS NOTHING TO DO WITH THE PROGRAM

                Log.v("SYMBOL ", stockSymbol);
                Log.v("Days Low ", stockDaysLow);
                Log.v("Days High ", stockDaysHigh);
                Log.v("Change ", stockChange);

                // GET ARRAY DATA
                JSONArray queryArray = quoteJSONObject.names();

                List<String> list = new ArrayList<String>();
                for (int i=0; i<queryArray.length(); i++) {
                    list.add( queryArray.getString(i) );
                }

                for(String item : list){

                    Log.v("JSON ARRAY ITEMS ", item);

                }
                // END OF GET ARRAY DATA

                // Gets the first item in the JSONObject
                JSONArray objectArray = resultsJSONObject.names();

                // Prints out that first item in the JSONObject
                Log.v("JSON NEXT NODE ", objectArray.getString(0));


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return result;

        }

        protected void onPostExecute(String result){

            // Gain access so I can change the TextViews
            TextView line1 = (TextView)findViewById(R.id.line1);
            TextView line2 = (TextView)findViewById(R.id.line2);
            TextView line3 = (TextView)findViewById(R.id.line3);

            // Change the values for all the TextViews
            line1.setText("Stock: " + stockSymbol + " : " + stockChange);

            line2.setText("Days Low: " + stockDaysLow);

            line3.setText("Days High: " + stockDaysHigh);

        }

    }

}