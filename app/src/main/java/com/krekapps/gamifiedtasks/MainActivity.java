package com.krekapps.gamifiedtasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ekk on 18-Apr-17.
 * from starter code at https://developers.google.com/google-apps/tasks/quickstart/android
 */

public class MainActivity extends AppCompatActivity {
    Spinner taskCategories;
    static final String CATEGORY_INTENT = "category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        taskCategories = (Spinner) findViewById(R.id.categories);
        displayCategories();

        FloatingActionButton view = (FloatingActionButton) findViewById(R.id.fabview);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String catName = taskCategories.getSelectedItem().toString();
                if (catName.length() > 0) {
                    Intent intent = new Intent(getApplicationContext(), ListsActivity.class);
                    intent.putExtra(CATEGORY_INTENT, catName);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Please choose a category first", Toast.LENGTH_LONG).show();
                }
            }
        });
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
        if (id == R.id.action_add_tags) {
            Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayCategories() {
        List<String> catNames = new ArrayList<>();
        catNames.add("Overdue");
        catNames.add("Due Today");
        catNames.add("All Tasks");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, catNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskCategories.setAdapter(adapter);
    }
}
