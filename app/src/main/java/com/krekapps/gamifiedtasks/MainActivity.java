package com.krekapps.gamifiedtasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by ekk on 18-Apr-17.
 * from starter code at https://developers.google.com/google-apps/tasks/quickstart/android
 */

public class MainActivity extends AppCompatActivity {
    public static String NEW_TASK_NAME = "newtaskname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton view = (FloatingActionButton) findViewById(R.id.fabview);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ListsActivity.class);
                intent.putExtra(NEW_TASK_NAME, "");
                startActivity(intent);
            }
        });

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fabadd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editor = (EditText) findViewById(R.id.newtask);
                String newTaskName = editor.getText().toString();
                editor.setText("");
                Intent intent = new Intent(getApplicationContext(), ListsActivity.class);
                intent.putExtra(NEW_TASK_NAME, newTaskName);
                startActivity(intent);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
