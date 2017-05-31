package com.krekapps.choresagenda;

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
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
// import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ListsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    PlaceholderFragment fragment;
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { TasksScopes.TASKS_READONLY };

    private String whichButtonClicked = ""; //chooses whether to get tasks or task lists. set when button is clicked.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lists, menu);
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

    public void getTaskLists(View view) {
        //TODO get task button lined up next to task list button
        view.setEnabled(false);
        //TODO could add progress bar here "calling api..."

        //Toast.makeText(ListsActivity.this, "getTaskLists", Toast.LENGTH_LONG).show();

        whichButtonClicked = "tasklists";
        getResultsFromApi();

        view.setEnabled(true);
    }

    public void getTasks(View view) {
        //TODO get task button lined up next to task list button
        view.setEnabled(false);
        //TODO could add progress bar here "calling api..."

        //Toast.makeText(ListsActivity.this, "getTasks", Toast.LENGTH_LONG).show();

        whichButtonClicked = "tasks";
        getResultsFromApi();

        view.setEnabled(true);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        //Toast.makeText(ListsActivity.this, "getResultsFromApi", Toast.LENGTH_LONG).show();
        if (! isGooglePlayServicesAvailable()) {
            //Toast.makeText(ListsActivity.this, "getResultsFromApi if", Toast.LENGTH_LONG).show();
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            //Toast.makeText(ListsActivity.this, "getResultsFromApi elseif", Toast.LENGTH_LONG).show();
            chooseAccount();
        } else if (! isDeviceOnline()) {
            //Toast.makeText(ListsActivity.this, "getResultsFromApi else", Toast.LENGTH_LONG).show();
            //viewResults.setText(getResources().getString(R.string.no_network));//TODO textview
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
        //Toast.makeText(ListsActivity.this, "isGooglePlayServicesAvailable", Toast.LENGTH_LONG).show();
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        //Toast.makeText(ListsActivity.this, "acquireGooglePlayServices", Toast.LENGTH_LONG).show();
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
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
        //Toast.makeText(ListsActivity.this, "chooseAccount", Toast.LENGTH_LONG).show();
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            //Toast.makeText(ListsActivity.this, "chooseAccount if 1", Toast.LENGTH_LONG).show();
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                //Toast.makeText(ListsActivity.this, "chooseAccount if", Toast.LENGTH_LONG).show();
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                //Toast.makeText(ListsActivity.this, "chooseAccount if else", Toast.LENGTH_LONG).show();
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            //Toast.makeText(ListsActivity.this, "chooseAccount else", Toast.LENGTH_LONG).show();
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this, "This app needs to access your Google account (via Contacts).", REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        //Toast.makeText(ListsActivity.this, "isDeviceOnline", Toast.LENGTH_LONG).show();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(ListsActivity.this, "onActivityResult", Toast.LENGTH_LONG).show();
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //viewResults.setText("This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.");//TODO textview
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
        //Toast.makeText(ListsActivity.this, "onRequestPermissionsResult", Toast.LENGTH_LONG).show();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /*
        TODO The next 2 functions get errors, need to figure out why
    */

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    //@Override
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
    //@Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * An asynchronous task that handles the Google Tasks API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            //Toast.makeText(ListsActivity.this, "MakeRequestTask", Toast.LENGTH_LONG).show();
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.tasks.Tasks.Builder(transport, jsonFactory, credential).setApplicationName("Google Tasks API Android Quickstart").build();
        }

        /**
         * Background task to call Google Tasks API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            //Toast.makeText(ListsActivity.this, "MakeRequestTask doInBackground", Toast.LENGTH_LONG).show();
            try {
                if (whichButtonClicked.equals("tasklists")) {
                    //Toast.makeText(ListsActivity.this, "mrt doInBackground tasklist", Toast.LENGTH_LONG).show();
                    return getTaskListsFromApi();
                } else {
                    //Toast.makeText(ListsActivity.this, "mrt doInBackground task", Toast.LENGTH_LONG).show();
                    return getTasksFromApi();
                }
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
         * @throws IOException
         */
        private List<String> getTaskListsFromApi() throws IOException {
            // List up to 10 task lists.
            //Toast.makeText(ListsActivity.this, "MakeRequestTask getTaskListsFromApi", Toast.LENGTH_LONG).show();
            List<String> taskListInfo = new ArrayList<>();
            TaskLists result = mService.tasklists().list()
                    .setMaxResults(Long.valueOf(10))
                    .execute();
            List<TaskList> tasklists = result.getItems();
            if (tasklists != null) {
                for (TaskList tasklist : tasklists) {
                    taskListInfo.add(String.format("%s (%s)\n",
                            tasklist.getTitle(),
                            tasklist.getId()));
                }
            }
            return taskListInfo;
        }

        /**
         * Fetch a list of the first 10 task lists.
         * @return List of Strings describing task lists, or an empty list if
         *         there are no task lists found.
         * @throws IOException
         */
        private List<String> getTasksFromApi() throws IOException {
            // List up to 10 task lists.
            //Toast.makeText(ListsActivity.this, "MakeRequestTask getTasksFromApi", Toast.LENGTH_LONG).show();
            List<String> taskListInfo = new ArrayList<String>();
            TaskLists listsresult = mService.tasklists().list().setMaxResults(Long.valueOf(10)).execute();
            List<TaskList> tasklists = listsresult.getItems();
            ArrayList<com.google.api.services.tasks.model.Tasks> tasksresult = new ArrayList<>();
            ArrayList<List<Task>> alltasks = new ArrayList<>();
            if (tasklists != null) {
                for (TaskList tasklist : tasklists) {
                    String tasklistname = tasklist.getTitle();//title given by user
                    String listEtag = tasklist.getEtag();//null
                    String listid = tasklist.getId();//task list identifier
                    String listtype = tasklist.getKind();//tasks#taskList
                    String listlink = tasklist.getSelfLink();//rest link
                    String updated = tasklist.getUpdated().toString();//last update
                    taskListInfo.add(String.format("%s (%s)\n", tasklistname, updated));
                    com.google.api.services.tasks.model.Tasks trl = mService.tasks().list(listid).execute();
                    tasksresult.add(trl);
                    List<Task> tasks = trl.getItems();
                    alltasks.add(tasks);
                }
            }
            List<String> taskInfo = new ArrayList<String>();
            for (List<Task> t1 : alltasks) {
                for (Task t2 : t1) {
                    taskInfo.add(t2.getTitle());
                }
            }
            return taskInfo;//taskListInfo;
        }

        @Override
        protected void onPreExecute() {
            //Toast.makeText(ListsActivity.this, "onPreExecute", Toast.LENGTH_LONG).show();
            //viewResults.setText("");//TODO textview
            //mProgress.show(); TODO progress bar
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //Toast.makeText(ListsActivity.this, "onPostExecute", Toast.LENGTH_LONG).show();
            //mProgress.hide(); TODO progress bar
            if (output == null || output.size() == 0) {
                //viewResults.setText("No results returned.");//TODO textview
            } else {
                output.add(0, "Data retrieved using the Google Tasks API:");
                Toast.makeText(ListsActivity.this, output.get(0), Toast.LENGTH_LONG).show();
                //mSectionsPagerAdapter.displayApiResults(TextUtils.join("\n", output));
                //viewResults.setText(TextUtils.join("\n", output));
                //fragment.displayApiResults(TextUtils.join("\n", output)); //TODO this line crashes the app
            }
        }

        @Override
        protected void onCancelled() {
            //Toast.makeText(ListsActivity.this, "onCancelled", Toast.LENGTH_LONG).show();
            //mProgress.hide(); TODO progress bar
            if (mLastError != null) {
                //Toast.makeText(ListsActivity.this, "onCancelled lasterror null", Toast.LENGTH_LONG).show();
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ListsActivity.REQUEST_AUTHORIZATION);
                } else {
                    //viewResults.setText("The following error occurred:\n" + mLastError.getMessage());//TODO textview
                }
            } else {
                //Toast.makeText(ListsActivity.this, "onCancelled lasterror notnull", Toast.LENGTH_LONG).show();
                //viewResults.setText("Request cancelled.");//TODO textview
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        TextView textView;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
            //Toast.makeText(getActivity(), "fragment", Toast.LENGTH_LONG).show();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            //Toast.makeText(getActivity(), "new fragment", Toast.LENGTH_LONG).show();
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //Toast.makeText(getActivity(), "create view", Toast.LENGTH_LONG).show();
            View rootView = inflater.inflate(R.layout.fragment_lists, container, false);
            textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        public void displayApiResults(String disp) {
            //Toast.makeText(getActivity(), "displayApiResults", Toast.LENGTH_LONG).show();
            textView.setText(disp);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
