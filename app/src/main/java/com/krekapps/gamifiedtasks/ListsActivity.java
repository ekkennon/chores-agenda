package com.krekapps.gamifiedtasks;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.text.InputType.TYPE_CLASS_TEXT;

/**
 * Created by ekk on 01-Jun-17.
 */

public class ListsActivity extends ListActivity implements EasyPermissions.PermissionCallbacks {
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY, SheetsScopes.SPREADSHEETS };
    private String spreadsheetId;
    GoogleAccountCredential mCredential;
    //private TextView mOutputText;
    //ProgressDialog mProgress;
    //private EditText mNewTaskName;
    private int numItems;
    String[] tasknames;

    private com.google.api.services.drive.Drive driveService = null;
    private com.google.api.services.sheets.v4.Sheets sheetService = null;
    private Exception mLastError = null;
    private String progress = "progress";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    //private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY, SheetsScopes.SPREADSHEETS };
    private static final String PREF_ACCOUNT_NAME = "accountName";
    String newTaskName;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lists);



        //Intent intent = getIntent();
        //View header = getLayoutInflater().inflate(R.layout.activity_lists, null);
        //ListView listView = (ListView) findViewById(R.id.listview);
/*

        Button view = (Button) findViewById(R.id.loadlist);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getResultsFromApi();*/
                //Toast.makeText(ListsActivity.this, "getresults", Toast.LENGTH_LONG).show();
                //ArrayAdapter<Object> adapter = new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames);//TODO this should be ArrayAdapter<String>
                //Toast.makeText(ListsActivity.this, "arrayadapter", Toast.LENGTH_LONG).show();
                //setListAdapter(adapter);
            /*}
        });

        //listView.addHeaderView(header);
//new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames);
        //listView.setAdapter(new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames));
    }

        //TODO get values to populate list as String[]
        //String[] values = intent.getStringArrayExtra("tasks");
        //spreadsheetId = values[0];


        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // Initialize credentials and service object.*/
        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {*/

        progress = "created";
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

    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        newTaskName = intent.getStringExtra(MainActivity.NEW_TASK_NAME);

        /*
        Toast.makeText(ListsActivity.this, "resume", Toast.LENGTH_LONG).show();
                getResultsFromApi();
        Toast.makeText(ListsActivity.this, "getresults", Toast.LENGTH_LONG).show();
                ArrayAdapter<Object> adapter = new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames);//TODO this should be ArrayAdapter<String>
        Toast.makeText(ListsActivity.this, "arrayadapter", Toast.LENGTH_LONG).show();
        setListAdapter(adapter);
        Toast.makeText(ListsActivity.this, "setlistadapter", Toast.LENGTH_LONG).show();*/
            }/*
        });*/


        /*
        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.fabadd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewTask();
            }
        });*/


        /*
        mOutputText = (TextView) findViewById(R.id.viewer);

        mNewTaskName = (EditText) findViewById(R.id.newtask);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google API ...");
        */
    //}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        //position++;
        /*if (position == numItems) {
            //TODO adding task
            Toast.makeText(ListsActivity.this, "adding task", Toast.LENGTH_LONG).show();
            EditText editor = (EditText) getListAdapter().getItem(position);
            //TextView editor = (TextView) getListAdapter().getItem(position);//findViewById(R.id.taskname);
            //editor.setEnabled(true);
            editor.setFocusable(true);
            //editor.setClickable(true);
            editor.setFocusableInTouchMode(true);
            editor.setCursorVisible(true);
            //editor.setInputType(TYPE_CLASS_TEXT);
        } else {*/
            //TODO adapter.notifyDataSetChanged();  might be good for adding and completing

            //GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
            //mCredential.setSelectedAccountName(getPreferences(Context.MODE_PRIVATE).getString("accountName", null));
            //Toast.makeText(ListsActivity.this, mCredential.toString(), Toast.LENGTH_LONG).show();

            //} else if (mCredential.getSelectedAccountName() == null) {
            //Toast.makeText(ListsActivity.this, "else if", Toast.LENGTH_LONG).show();
            //startActivityForResult(mCredential.newChooseAccountIntent(), 1000);
            //chooseAccount();


            //Toast.makeText(MainActivity.this, mCredential.toString(), Toast.LENGTH_LONG).show();
            new ListsActivity.CompleteTask(mCredential, position).execute();//TODO
            getResultsFromApi();
        //}

        //new ListsActivity.CompleteTask(mCredential, position).execute();
    }

    public void loadListClick(View v) {
        Toast.makeText(ListsActivity.this, newTaskName, Toast.LENGTH_LONG).show();
        progress = "loading list";
        //Toast.makeText(ListsActivity.this, "clicked", Toast.LENGTH_LONG).show();

        //Toast.makeText(ListsActivity.this, tasknames.toString(), Toast.LENGTH_LONG).show();
        //Toast.makeText(ListsActivity.this, "tasknames", Toast.LENGTH_LONG).show();

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        //mService = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential).setApplicationName("Google Tasks API Android Quickstart").build();
        driveService = new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, mCredential).setApplicationName("Drive API Android Quickstart").build();
        sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, mCredential).setApplicationName("Google Sheets API Android Quickstart").build();

        getResultsFromApi();

        //tasknames = new String[]{""};
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames);
        //Toast.makeText(ListsActivity.this, "arrayadapter", Toast.LENGTH_LONG).show();
        //setListAdapter(adapter);

        if (!newTaskName.isEmpty()) {
            new UpdateSheet(mCredential, newTaskName).execute();
            //getResultsFromApi();
        }

    }

    public void displayTasks() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames);
        //Toast.makeText(ListsActivity.this, "arrayadapter", Toast.LENGTH_LONG).show();
        setListAdapter(adapter);
    }

    private void getResultsFromApi() {
        progress = "getting results";
        //Toast.makeText(ListsActivity.this, "getting api results", Toast.LENGTH_LONG).show();
        //Toast.makeText(ListsActivity.this, mCredential.getSelectedAccountName(), Toast.LENGTH_LONG).show();

        if (! isGooglePlayServicesAvailable()) {
            //Toast.makeText(ListsActivity.this, "acquireGooglePlayServices", Toast.LENGTH_LONG).show();
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            //Toast.makeText(ListsActivity.this, "chooseaccount", Toast.LENGTH_LONG).show();
            chooseAccount();
        } else if (! isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
            Toast.makeText(ListsActivity.this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(ListsActivity.this, mCredential.toString(), Toast.LENGTH_LONG).show();
            new MakeRequestTask(mCredential).execute();
            /*try {
                getDataFromApi();
            } catch (Exception e) {
                Toast.makeText(ListsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }*/

            //Toast.makeText(ListsActivity.this, "executed", Toast.LENGTH_LONG).show();
        }
    }
/*
    private void createNewTask() {
        new UpdateSheet(mCredential, mNewTaskName.getText().toString()).execute();
        mNewTaskName.setText("");
        getResultsFromApi();
    }*/

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                //Toast.makeText(MainActivity.this, mCredential.toString(), Toast.LENGTH_LONG).show();
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this, "This app needs to access your Google account (via Contacts).", REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(ListsActivity.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(ListsActivity.this, "This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private void getDataFromApi() throws IOException {
        progress = "mrt getting data";
        Toast.makeText(ListsActivity.this, progress, Toast.LENGTH_LONG).show();
        // Get a list of up to 10 files.
        String fileName = "ChoresAgenda";
        //List<String> fileInfo = new ArrayList<>();
        FileList result = driveService.files().list().setQ("name='" + fileName + "'").setFields("files(id)").execute();
        List<File> files = result.getFiles();
        if (files != null) {
            //progress = Integer.toString(files.size());

            if (files.size() > 1) {
                //fileInfo.add("Multiple files returned with name ");
                //fileInfo.add(fileName);
                //fileInfo.add(". Please Check the files in your account.");
            } else if (files.size() < 1) {
                this.sheetService.spreadsheets().create(createSheet(fileName)).execute();
                //fileInfo.add("sheet created");
            } else {
                spreadsheetId = files.get(0).getId();
                progress = spreadsheetId;
                String range = "Tasks!A1:A";
                progress = range;
                ValueRange response = this.sheetService.spreadsheets().values().get(spreadsheetId, range).execute();
                progress = "response";
                List<List<Object>> values = response.getValues();
                progress = "values";
                //numItems = values.size();
                progress = Integer.toString(numItems);
                if (values != null) {
                    numItems = values.size();
                    tasknames = new String[numItems];
                    progress = "";
                    int counter = 0;
                    for (List row : values) {
                        String s = row.get(0).toString();
                        //fileInfo.add(s);
                        tasknames[counter] = s;
                        progress += "fileinfo " + s;
                        counter++;
                    }
                }
            }
        }
        //return fileInfo;
    }

    private Spreadsheet createSheet(String fileName) {
        progress = "creating sheet";
        //TODO add settings for user to pick categories (sheet tabs)
        Spreadsheet spreadsheet = new Spreadsheet();
        SpreadsheetProperties properties = new SpreadsheetProperties();
        properties.setTitle(fileName);
        spreadsheet.setProperties(properties);

        Sheet taskSheet = new Sheet();
        //Sheet routineSheet = new Sheet();
        //Sheet projectSheet = new Sheet();

        SheetProperties sheetProps =  new SheetProperties();
        sheetProps.setTitle("Tasks");
        taskSheet.setProperties(sheetProps);
        List<Sheet> sheetList = new ArrayList<>();
        sheetList.add(taskSheet);
        spreadsheet.setSheets(sheetList);
        return spreadsheet;
    }

    /**
     * An asynchronous task that handles the Google API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        //private com.google.api.services.tasks.Tasks mService = null;
        private com.google.api.services.drive.Drive driveService = null;
        private com.google.api.services.sheets.v4.Sheets sheetService = null;
        private Exception mLastError = null;
        private String progress = "progress";

        MakeRequestTask(GoogleAccountCredential credential) {
            progress = "make request task";
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            //mService = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential).setApplicationName("Google Tasks API Android Quickstart").build();
            driveService = new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential).setApplicationName("Drive API Android Quickstart").build();
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential).setApplicationName("Google Sheets API Android Quickstart").build();
        }

        /**
         * Background task to call Google API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            progress = "do in background";
            List<String> resultTasks = new ArrayList<>();
            try {
                resultTasks = getDataFromApi();
                //progress = "error in gdfa";
                //return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                //return null;
            } finally {
                if (resultTasks.size() < 1) {
                    return null;
                } else {
                    return resultTasks;
                }
            }
        }

        /**
         * Fetch a list of the first 10 task lists.
         * @return List of Strings describing task lists, or an empty list if
         *         there are no task lists found.
         *---------------------------------------------------------
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            progress = "mrt getting data";
            // Get a list of up to 10 files.
            String fileName = "ChoresAgenda";
            List<String> fileInfo = new ArrayList<>();
            FileList result = driveService.files().list().setQ("name='" + fileName + "'").setFields("files(id)").execute();
            List<File> files = result.getFiles();
            if (files != null) {
                //progress = Integer.toString(files.size());

                if (files.size() > 1) {
                    fileInfo.add("Multiple files returned with name ");
                    fileInfo.add(fileName);
                    fileInfo.add(". Please Check the files in your account.");
                } else if (files.size() < 1) {
                    this.sheetService.spreadsheets().create(createSheet(fileName)).execute();
                    fileInfo.add("sheet created");
                } else {
                    spreadsheetId = files.get(0).getId();
                    progress = spreadsheetId;
                    String range = "Tasks!A1:A";
                    progress = range;
                    ValueRange response = this.sheetService.spreadsheets().values().get(spreadsheetId, range).execute();
                    progress = "response";
                    List<List<Object>> values = response.getValues();
                    progress = "values";
                    numItems = values.size();
                    progress = Integer.toString(numItems);
                    if (values != null) {//TODO this should be before previous line
                        progress = "";
                        for (List row : values) {
                            String s = row.get(0).toString();
                            fileInfo.add(s);
                            progress += "fileinfo " + s;
                        }
                    }
                }
            }
            return fileInfo;
        }

        private Spreadsheet createSheet(String fileName) {
            progress = "creating sheet";
            //TODO add settings for user to pick categories (sheet tabs)
            Spreadsheet spreadsheet = new Spreadsheet();
            SpreadsheetProperties properties = new SpreadsheetProperties();
            properties.setTitle(fileName);
            spreadsheet.setProperties(properties);

            Sheet taskSheet = new Sheet();
            //Sheet routineSheet = new Sheet();
            //Sheet projectSheet = new Sheet();

            SheetProperties sheetProps =  new SheetProperties();
            sheetProps.setTitle("Tasks");
            taskSheet.setProperties(sheetProps);
            List<Sheet> sheetList = new ArrayList<>();
            sheetList.add(taskSheet);
            spreadsheet.setSheets(sheetList);
            return spreadsheet;
        }


        @Override
        protected void onPreExecute() {
            progress = "pre execute";
            //mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            progress = "post execute";
            //Toast.makeText(ListsActivity.this, progress, Toast.LENGTH_LONG).show();
            //mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(ListsActivity.this, "No results returned.", Toast.LENGTH_LONG).show();
                //mOutputText.setText("No results returned.");
            } else {
                //TODO list should show in list view
                output.add("");
                //Toast.makeText(ListsActivity.this, output.size(), Toast.LENGTH_LONG).show();
                tasknames = output.toArray(new String[output.size()]);
                //Toast.makeText(ListsActivity.this, tasknames.length, Toast.LENGTH_LONG).show();
                displayTasks();
                //output.add(0, "Data retrieved using the Google API:");
                //mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            //Toast.makeText(ListsActivity.this, "canceled", Toast.LENGTH_LONG).show();
            progress = "cancelled";
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    progress = "cancelled else";
                    //Toast.makeText(ListsActivity.this, "error: " + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                    //mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                progress = "request cancelled";
                //Toast.makeText(ListsActivity.this, "Request cancelled.", Toast.LENGTH_LONG).show();
                //mOutputText.setText("");
            }
        }
    }
    private class UpdateSheet extends AsyncTask<Void, Void, List<String>> {
        //private com.google.api.services.drive.Drive driveService = null;
        private com.google.api.services.sheets.v4.Sheets sheetService = null;
        private Exception mLastError = null;
        private String taskName;

        UpdateSheet(GoogleAccountCredential credential, String task) {
            taskName = task;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            //driveService = new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential).setApplicationName("Drive API Android Quickstart").build();
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential).setApplicationName("Google Sheets API Android Quickstart").build();
        }

        /**
         * Background task to call Google API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                //Toast.makeText(MainActivity.this, taskName, Toast.LENGTH_LONG).show();
                return getDataFromApi(taskName);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the first 10 task lists.
         * @return List of Strings describing task lists, or an empty list if
         *         there are no task lists found.
         *---------------------------------------------------------
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi(String task) throws IOException {
            // Get a list of up to 10 files.
            //String fileName = "ChoresAgenda";
            List<String> fileInfo = new ArrayList<>();
            //FileList result = driveService.files().list().setQ("name='" + fileName + "'").setFields("files(id)").execute();
            //List<File> files = result.getFiles();
            //if (files != null) {
            //String spreadsheetId = files.get(0).getId();
            String range = "Tasks!A" + Integer.toString(numItems+1);
            //Toast.makeText(MainActivity.this, range, Toast.LENGTH_LONG).show();
            List<List<Object>> v = new ArrayList<>();
            ArrayList<Object> s = new ArrayList<>();
            s.add(task);
            v.add(s);
            ValueRange values = new ValueRange();
            values.setValues(v);
            Sheets.Spreadsheets.Values.Update response = this.sheetService.spreadsheets().values().update(spreadsheetId, range, values);
            response.setValueInputOption("raw");
            UpdateValuesResponse request = response.execute();

                /*
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (List row : values) {
                        fileInfo.add(row.get(0).toString());
                    }
                }*/
            //}
            fileInfo.add("task added");
            return fileInfo;
        }

        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            if (output == null || output.size() == 0) {
                //mOutputText.setText("No results returned.");
                Toast.makeText(ListsActivity.this, "No results returned.", Toast.LENGTH_LONG).show();
            } else {
               //tasknames = output.toArray(new String[output.size()]);
                //output.add(0, "Data retrieved using the Google API:");
                getResultsFromApi();
                //mOutputText.setText(TextUtils.join("\n", output));
                /*
                Intent intent = new Intent(getApplicationContext(), ListsActivity.class);
                intent.putExtra("tasks", output.toArray());
                startActivity(intent);*/
            }
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                    Toast.makeText(ListsActivity.this, "error: " + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                    //mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                Toast.makeText(ListsActivity.this, "Request cancelled.", Toast.LENGTH_LONG).show();


                //mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class CompleteTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive driveService = null;
        private com.google.api.services.sheets.v4.Sheets sheetService = null;
        private Exception mLastError = null;
        private int position;
        private String progress = "progress";

        CompleteTask(GoogleAccountCredential credential, int task) {
            position = task;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            driveService = new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential).setApplicationName("Drive API Android Quickstart").build();
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential).setApplicationName("Google Sheets API Android Quickstart").build();
        }

        /**
         * Background task to call Google API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                //Toast.makeText(MainActivity.this, taskName, Toast.LENGTH_LONG).show();
                progress = "doinbackground";
                return getDataFromApi(position);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the first 10 task lists.
         * @return List of Strings describing task lists, or an empty list if
         *         there are no task lists found.
         *---------------------------------------------------------
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi(int position) throws IOException {
            progress = "getData";
            /*String fileName = "ChoresAgenda";
            progress = fileName;
            FileList result = driveService.files().list().setQ("name='" + fileName + "'").setFields("files(id)").execute();
            progress = "result";
            List<File> files = result.getFiles();
            progress = "files";
            String spreadsheetId = files.get(0).getId();
            progress = spreadsheetId;*/
            List<String> fileInfo = new ArrayList<>();
            progress = "fileinfo";
            //FileList result = driveService.files().list().setQ("name='" + fileName + "'").setFields("files(id)").execute();
            //List<File> files = result.getFiles();
            //if (files != null) {
            //String spreadsheetId = files.get(0).getId();
            //String range = "Tasks!A" + Integer.toString(position+1);
            //Toast.makeText(MainActivity.this, range, Toast.LENGTH_LONG).show();
            /*List<List<Object>> v = new ArrayList<>();
            ArrayList<Object> s = new ArrayList<>();
            s.add(task);
            v.add(s);
            ValueRange values = new ValueRange();
            values.setValues(v);*/
            //Sheets.Spreadsheets.Values.Update response = this.sheetService.spreadsheets().values().update(spreadsheetId, range, values);

            //public void deleteRow(Integer StartIndex, Integer EndIndex) {
                /*Spreadsheet spreadsheet = null;
                try {
                    spreadsheet = sheetService.spreadsheets().get(GoogleExcelFileUtil.SPREASHEET_ID).execute();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } */
            //Spreadsheet spreadsheet = new Spreadsheet();
            //SpreadsheetProperties properties = new SpreadsheetProperties();
                //Sheet s = new Sheet();

            Spreadsheet spreadsheet = sheetService.spreadsheets().get(spreadsheetId).execute();
            progress = "spreadsheet";
            List<Sheet> sheets = spreadsheet.getSheets();
            int sheetId = 0;
            for (Sheet s : sheets) {/*
                if (s.getProperties().getTitle().equals("Tasks")) {
                    sheetId = s.getProperties().getSheetId();
                }*/
                fileInfo.add(s.getProperties().getTitle());
            }
            progress = Integer.toString(fileInfo.size());
            sheetId = sheets.get(0).getProperties().getSheetId();
            //int i = sheets.get(0).getProperties().getSheetId();
                BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
                Request request = new Request();
                DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
                DimensionRange dimensionRange = new DimensionRange();
                dimensionRange.setDimension("ROWS");
                dimensionRange.setStartIndex(position);
                dimensionRange.setEndIndex(position+1);
            dimensionRange.setSheetId(sheetId);

                //dimensionRange.setSheetId(Integer.parseInt(spreadsheetId));//spreadsheet.getSheets().get(0).getProperties().getSheetId());
                deleteDimensionRequest.setRange(dimensionRange);

                request.setDeleteDimension(deleteDimensionRequest);

                List<Request> requests = new ArrayList<>();
                requests.add(request);
                content.setRequests(requests);

                //try {
                    sheetService.spreadsheets().batchUpdate(spreadsheetId, content).execute();
                    /*
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    dimensionRange = null;
                    deleteDimensionRequest = null;
                    request = null;
                    requests = null;
                    content = null;
                }
            }*/
            /*
            DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
            Request r = new Request();
            r.setDeleteDimension()
            Sheets.Spreadsheets.BatchUpdate update;
            update = this.sheetService.spreadsheets().batchUpdate(spreadsheetId, deleteDimensionRequest);
            update
            BatchUpdateSpreadsheetResponse batchUpdateSpreadsheetResponse = update.execute();
*/

            /*
            ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
            clearValuesRequest.
            Sheets.Spreadsheets.Values.Clear clr = this.sheetService.spreadsheets().values().clear(spreadsheetId, range, clearValuesRequest);
            //response.setValueInputOption("raw");
            ClearValuesResponse request = clr.execute();
*/
                /*
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (List row : values) {
                        fileInfo.add(row.get(0).toString());
                    }
                }*/
            //}

            return fileInfo;
        }

        @Override
        protected void onPreExecute() {/*
            mOutputText.setText("");
            mProgress.show();*/
            Toast.makeText(ListsActivity.this, Integer.toString(position+1) + " selected", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            Toast.makeText(ListsActivity.this, "postexecute", Toast.LENGTH_LONG).show();
            //mProgress.hide();
            if (output == null || output.size() == 0) {
                //mOutputText.setText("No results returned.");
                Toast.makeText(ListsActivity.this, "no results", Toast.LENGTH_LONG).show();
            } else {
                //output.add(0, "Data retrieved using the Google API:");
                //mOutputText.setText(TextUtils.join("\n", output));/*
                Toast.makeText(ListsActivity.this, output.get(0), Toast.LENGTH_LONG).show();
                /*
                Intent intent = new Intent(getApplicationContext(), ListsActivity.class);
                intent.putExtra("tasks", output.toArray());
                startActivity(intent);*/
            }
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(ListsActivity.this, "GooglePlayServicesAvailabilityIOException", Toast.LENGTH_LONG).show();
                    //showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    Toast.makeText(ListsActivity.this, "UserRecoverableAuthIOException", Toast.LENGTH_LONG).show();
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                    Toast.makeText(ListsActivity.this, progress, Toast.LENGTH_LONG).show();
                    Toast.makeText(ListsActivity.this, mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                //mOutputText.setText("Request cancelled.");
                Toast.makeText(ListsActivity.this, "canceled", Toast.LENGTH_LONG).show();
            }
        }
    }
}
