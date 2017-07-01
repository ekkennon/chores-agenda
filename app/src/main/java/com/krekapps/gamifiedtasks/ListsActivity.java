package com.krekapps.gamifiedtasks;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.krekapps.gamifiedtasks.models.Tag;
import com.krekapps.gamifiedtasks.models.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by ekk on 01-Jun-17.
 */

public class ListsActivity extends ListActivity implements EasyPermissions.PermissionCallbacks {

    private String spreadsheetId;
    private int numItems;
    GoogleAccountCredential mCredential;
    String newTaskName = "";
    String category = "";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY, SheetsScopes.SPREADSHEETS };
    private static final String PREF_ACCOUNT_NAME = "accountName";

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lists);

        Intent intent = getIntent();
        if (intent.hasExtra(AddTaskActivity.NEW_TASK_NAME)) {
            newTaskName = intent.getStringExtra(AddTaskActivity.NEW_TASK_NAME);
        } else if (intent.hasExtra(MainActivity.CATEGORY_INTENT)) {
            category = intent.getStringExtra(MainActivity.CATEGORY_INTENT);
        }

        if (category.equals("")) {
            Toast.makeText(this, "Please choose a category first", Toast.LENGTH_LONG).show();
            Intent goBack = new Intent(this, MainActivity.class);
            startActivity(goBack);
        }

        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);//TODO may need this later
        //TODO adapter.notifyDataSetChanged();  might be good for adding and completing

        String deleteTask = item.toString();
        //Toast.makeText(this, deleteTask, Toast.LENGTH_LONG).show();
        int index;
        String[] parts = deleteTask.split(":");
        if (parts[0].equals("id")) {
            index = Integer.parseInt(parts[1]);
        } else {
            index = position;
        }

        new ListsActivity.CompleteTask(mCredential, index).execute();
        getResultsFromApi();
    }

    public void loadListClick(View v) {
        getResultsFromApi();
    }

    public void chooseCategory(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void navigateAddClick(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra(MainActivity.CATEGORY_INTENT, category);
        startActivity(intent);
    }

    public void displayTasks(List<Task> tasklist) {
        String[] tasknames = new String[tasklist.size()];
        for (int i=0;i<tasklist.size();i++) {
            tasknames[i] = tasklist.get(i).toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_list, R.id.taskname, tasknames);
        setListAdapter(adapter);
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(ListsActivity.this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
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

    /**
     * An asynchronous task that handles the Google API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Map<String,List<Task>>> {
        private com.google.api.services.drive.Drive driveService = null;
        private com.google.api.services.sheets.v4.Sheets sheetService = null;
        private Exception mLastError = null;
        private String progress;

        MakeRequestTask(GoogleAccountCredential credential) {
            progress = "";
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            driveService = new com.google.api.services.drive.Drive.Builder(transport, jsonFactory, credential).setApplicationName("ChoreList using Google Sheets API Android").build();
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential).setApplicationName("ChoreList using Google Sheets API Android").build();
        }

        /**
         * Background task to call Google API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Map<String,List<Task>> doInBackground(Void... params) {
            Map<String,List<Task>> resultTasks = new HashMap<>();
            try {
                resultTasks = getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
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
        private Map<String,List<Task>> getDataFromApi() throws IOException {
            String fileName = "ChoresAgenda";

            FileList result = driveService.files().list().setQ("name='" + fileName + "'").setFields("files(id)").execute();
            List<File> files = result.getFiles();

            Map<String,List<Task>> toReturn = new HashMap<>();
            if (files != null) {
                if (files.size() > 1) {
                    //TODO this function returns a list of task, how to output an error?
                } else if (files.size() < 1) {
                    this.sheetService.spreadsheets().create(createSheet(fileName)).execute();
                    //TODO this block needs to be in main activity, then return error from this activity
                } else {
                    spreadsheetId = files.get(0).getId();
                    String range = "Tasks!A1:A";
                    ValueRange response = this.sheetService.spreadsheets().values().get(spreadsheetId, range).execute();
                    List<List<Object>> values = response.getValues();

                    if (values != null) {
                        List<Task> todays = new ArrayList<>();
                        numItems = values.size();
                        List<Task> tasks = new ArrayList<>();
                        for (List row : values) {

                            Task t = Task.fromString(row.get(0).toString());//TODO what does get(0) do? is 0 the column?

                            boolean hasOverdue = false;
                            boolean hasDueToday = false;
                            boolean hasCatTag = false;

                            Set<Tag> tags = t.getTags();
                            for (Tag tag : tags) {
                                if (tag.getName().equals("overdue")) {
                                    hasOverdue = true;
                                //} else if (tag.getName().equals(category)) {
                                    //hasCatTag = true;
                                } else if (tag.getName().equals("due today")) {
                                    hasDueToday = true;
                                }
                            }/*

                            if (!hasCatTag) {
                                //if (tags.size() > 0)
                                t.addTag(new Tag(category));
                                progress = "adding category as tag";
                            }*/
                            if (t.isDueToday()) {
                                Task taskToday = Task.fromString(t.toString());//create new task since tasks and todays need different values
                                taskToday.setRepeating(false);
                                if (!hasDueToday) {
                                    hasDueToday = true;
                                    taskToday.addTag(new Tag("due today"));
                                    t.addTag(new Tag("due today"));
                                }
                                todays.add(taskToday);
                            } else {
                                if (hasDueToday) {
                                    hasOverdue = true;
                                    t.addTag(new Tag("overdue"));
                                    t.removeTag(new Tag("due today"));
                                }
                            }
                            if (category.equals("Due Today")) {
                                if (hasDueToday) {
                                    t.setId(values.indexOf(row));
                                    tasks.add(t);
                                }
                            } else if (category.equals("Overdue")) {
                                if (hasOverdue) {
                                    t.setId(values.indexOf(row));
                                    tasks.add(t);
                                }
                            } else {
                                tasks.add(t);
                            }

                        }
                        if (tasks.size() > 0) {
                            toReturn.put("tasks",tasks);
                        }
/*
                        if (todays.size() > 0 && !category.equals("Due Today") && !category.equals("Routine")) {
                            toReturn.put("todays",todays);
                        }*/
                    }
                }
            }
            return toReturn;
        }

        private Spreadsheet createSheet(String fileName) {
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
        protected void onPostExecute(Map<String,List<Task>> output) {
            //Toast.makeText(ListsActivity.this, progress, Toast.LENGTH_LONG).show();
            if (output == null || output.size() == 0) {
                Toast.makeText(ListsActivity.this, "No results returned.", Toast.LENGTH_LONG).show();
            } else {
                if (output.containsKey("todays")) {
                    new UpdateSheet(mCredential, output.get("todays")).execute();
                }
                if (output.containsKey("tasks")) {
                    displayTasks(output.get("tasks"));
                }
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(ListsActivity.this, progress, Toast.LENGTH_LONG).show();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(ListsActivity.this, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ListsActivity.this, "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateSheet extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets sheetService = null;
        private Exception mLastError = null;
        private List<Task> tasklist;
        private String progress;

        UpdateSheet(GoogleAccountCredential credential, List<Task> tasks) {
            progress = "";
            tasklist = tasks;
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName("ChoreList using Google Sheets API Android").build();
        }

        /**
         * Background task to call Google API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
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
        private void getDataFromApi() throws IOException {
            //the new task needs to be sent as a List<List<Object>>, the following code creates that with the one task name added
            for (Task item : tasklist) {
                List<Object> listOfTaskNames = new ArrayList<>();
                listOfTaskNames.add(item.toString());
                List<List<Object>> listOfList = new ArrayList<>();
                listOfList.add(listOfTaskNames);

                Sheets.Spreadsheets.Values r1 = this.sheetService.spreadsheets().values();
                ValueRange numvalues = r1.get(spreadsheetId, "Due Today!A1:A").execute();

                int items = 1;
                if (numvalues.getValues() != null) {
                    items += numvalues.getValues().size();
                }
                ValueRange v1 = new ValueRange().setValues(listOfList);
                String v2 = "Due Today!A" + Integer.toString(items);

                Sheets.Spreadsheets.Values.Update response = r1.update(spreadsheetId, v2, v1);

                response.setValueInputOption("raw");
                response.execute();
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(ListsActivity.this, progress, Toast.LENGTH_LONG).show();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(ListsActivity.this, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ListsActivity.this, "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class CompleteTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets sheetService = null;
        private com.google.api.services.drive.Drive driveService = null;
        private Exception mLastError = null;
        private int position;

        CompleteTask(GoogleAccountCredential credential, int task) {
            position = task;
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName("ChoreList using Google Sheets API Android").build();
            driveService = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName("ChoreList using Google Sheets API Android").build();
        }

        /**
         * Background task to call Google API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
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
        private void getDataFromApi() throws IOException {
            String range = "Tasks!A" + Integer.toString(position+1);
            ValueRange readlineresponse = this.sheetService.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = readlineresponse.getValues();
            if (values != null && !values.isEmpty() && values.size() == 1) {
                List<Object> value = values.get(0);
                if (value != null && !value.isEmpty() && value.size() == 1) {
                    Task t = Task.fromString(value.get(0).toString());
                    List<Tag> tags = t.getAlTags();
                    for (int i=0;i<tags.size();i++) {
                        if (tags.get(i).getName().equals("overdue") || tags.get(i).getName().equals("due today")) {
                            tags.remove(tags.get(i));
                        }
                    }
                    if (t.isRepeating()) {
                        if (t.hasDueDate()) {
                            t.updateDueDate();
                        }

                        List<List<Object>> almain = new ArrayList<>();
                        List<Object> al = new ArrayList<>();
                        al.add(t.toString());
                        almain.add(al);
                        //TODO create new task with new due date
                        String spreadsheetId = driveService.files().list().setQ("name='ChoresAgenda'").setFields("files(id)").execute().getFiles().get(0).getId();
                        Sheets.Spreadsheets.Values.Update response = this.sheetService.spreadsheets().values().update(spreadsheetId, "Tasks!A" + Integer.toString(this.sheetService.spreadsheets().values().get(spreadsheetId, "Tasks!A1:A").execute().getValues().size() + 1), new ValueRange().setValues(almain));
                        response.setValueInputOption("raw");
                        response.execute();
                    }
                }
            }

            DimensionRange dimensionRange = new DimensionRange();
            dimensionRange.setDimension("ROWS");
            dimensionRange.setStartIndex(position);
            dimensionRange.setEndIndex(position + 1);

            int sheetId = -1;
            List<Sheet> sheets = sheetService.spreadsheets().get(spreadsheetId).execute().getSheets();
            for (Sheet s : sheets) {
                if (s.getProperties().getTitle().equals("Tasks")) {
                    sheetId = s.getProperties().getSheetId();
                }
            }

            if (sheetId > -1) {
                dimensionRange.setSheetId(sheetId);

                DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
                deleteDimensionRequest.setRange(dimensionRange);

                Request request = new Request().setDeleteDimension(deleteDimensionRequest);
                List<Request> requests = new ArrayList<>();
                requests.add(request);
                sheetService.spreadsheets().batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(requests)).execute();

            } else {
                //TODO sheet was not found
            }
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(ListsActivity.this, "GooglePlayServicesAvailabilityIOException", Toast.LENGTH_LONG).show();
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    Toast.makeText(ListsActivity.this, "UserRecoverableAuthIOException", Toast.LENGTH_LONG).show();
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(ListsActivity.this, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ListsActivity.this, "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
