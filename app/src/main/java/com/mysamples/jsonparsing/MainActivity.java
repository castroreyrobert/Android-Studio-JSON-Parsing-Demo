package com.mysamples.jsonparsing;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mysamples.jsonparsing.models.MovieModels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private ListView listViewMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        listViewMovie = (ListView)findViewById(R.id.listViewMovies);
    }

    public class JSONTask extends AsyncTask<String,String,List<MovieModels>>{

        @Override
        protected List<MovieModels> doInBackground(String... urls) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;


            try {
                URL url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) !=null){
                    stringBuffer.append(line);
                }

                String finalJson = stringBuffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);

                JSONArray parentArray = parentObject.getJSONArray("movies");


                //GETTING THE OBJECTS FROM THE URL
                List<MovieModels> movieModelList = new ArrayList<>();
                for (int x = 0; x <parentArray.length() ; x++) {
                    MovieModels models = new MovieModels();
                    JSONObject finalObject = parentArray.getJSONObject(x);

                    models.setMovie(finalObject.getString("movie"));
                    models.setYear(finalObject.getInt("year"));
                    models.setRating((float) finalObject.getDouble("rating"));
                    models.setDirector(finalObject.getString("director"));
                    models.setTagline(finalObject.getString("tagline"));
                    models.setDuration(finalObject.getString("duration"));

                    models.setImage(finalObject.getString("image"));
                    models.setStory(finalObject.getString("story"));


                    //getting the Array cast  From the Movie Object
                    List<MovieModels.Cast> castList = new ArrayList<>();
                    for (int i = 0; i <finalObject.getJSONArray("cast").length() ; i++) {
                        MovieModels.Cast cast = new MovieModels.Cast();
                        cast.setName(finalObject.getJSONArray("cast").getJSONObject(i).getString("name"));
                        castList.add(cast);
                    }
                    models.setCastList(castList);

                    movieModelList.add(models);
                }

                return movieModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
                try {
                    if (reader !=null) {
                        reader.close();
                    }
                } catch (IOException e) {


                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MovieModels> s) {
            super.onPostExecute(s);

            //CALLING THE MOVIEADAPTER CLASS INSIDE THIS CLASS
            MovieAdapter movieAdapter = new MovieAdapter(getApplicationContext(),R.layout.custom_row,
                    s);
            listViewMovie.setAdapter(movieAdapter);
        }
    }

    public class MovieAdapter extends ArrayAdapter{

        private List<MovieModels> movieModel;
        private int resource;
        private  LayoutInflater inflater;


        public MovieAdapter(Context context, int resource, List<MovieModels> objects) {
            super(context, resource, objects);
            movieModel = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null){
                convertView = inflater.inflate(resource, null);
            }


            //Casting all the attributes from the custom_row adapter
            ImageView imageViewList;
            TextView textViewMovie, textViewTagLine, textViewYear, textViewDuration,
                    textViewDirector, textViewCast, textViewStory;
            RatingBar ratingBar;

            imageViewList = (ImageView)convertView.findViewById(R.id.imageViewList);
            textViewMovie = (TextView)convertView.findViewById(R.id.textViewMovie);
            textViewTagLine = (TextView)convertView.findViewById(R.id.textViewTagLine);
            textViewYear = (TextView)convertView.findViewById(R.id.textViewYear);
            textViewDuration = (TextView)convertView.findViewById(R.id.textViewDuration);
            textViewDirector = (TextView)convertView.findViewById(R.id.textViewDirector);
            ratingBar = (RatingBar)convertView.findViewById(R.id.ratingBarMovie);
            textViewCast = (TextView)convertView.findViewById(R.id.textViewCast);
            textViewStory = (TextView)convertView.findViewById(R.id.textViewStory);



            //PUTTING THE VALUES FROM THE MOVIEMODEL TO THE TEXTVIEWS
            textViewMovie.setText(movieModel.get(position).getMovie());
            textViewTagLine.setText(movieModel.get(position).getTagline());
            textViewYear.setText("Year: " + movieModel.get(position).getYear());
            textViewDuration.setText(movieModel.get(position).getDuration());
            textViewDirector.setText(movieModel.get(position).getDirector());
            ratingBar.setRating(movieModel.get(position).getRating()/2);

            //PUTTING THE ARRAY CASTLIST TO THE TEXTVIEWS
            StringBuffer castBuffer = new StringBuffer();
            for (MovieModels.Cast cast: movieModel.get(position).getCastList()) {
                castBuffer.append(cast.getName() + ", ");
            }
            textViewCast.setText(castBuffer);
            textViewStory.setText(movieModel.get(position).getStory());
            return convertView;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new JSONTask().execute("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
