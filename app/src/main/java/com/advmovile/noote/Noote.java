package com.advmovile.noote;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Noote extends ListActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private NooteDbAdapter mDbHelper;

    EditText searchEdit;
    private String titleSort = "none";
    private String dateSort = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noote);
        mDbHelper = new NooteDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView()); // floating context menu - onCreateContextMenu

        // setting up the filter adaptor
        searchEdit = (EditText) findViewById(R.id.searchText);
        searchEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                // Abstract Method of TextWatcher Interface.
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Abstract Method of TextWatcher Interface.

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ListView av = (ListView) findViewById(android.R.id.list);
                SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter) av.getAdapter();
                filterAdapter.getFilter().filter(s.toString());


            }
        });
    }

    private void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list
        String[] fromT = new String[]{NooteDbAdapter.KEY_TITLE, NooteDbAdapter.KEY_CATEGORY, NooteDbAdapter.KEY_DATE};
//        String[] fromD = new String[]{NotesDbAdapter.KEY_DATE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, fromT, to);
        setListAdapter(notes);

        notes.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return mDbHelper.filter(constraint, titleSort, dateSort);
            }
        });

        stopManagingCursor(notesCursor);
    }


    public void insertNote(View v){
        createNote();
    }




    // context menu and action configuration - long click on list item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, NooteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, NooteEdit.class);
        i.putExtra(NooteDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    public void sortByTitle(View view) {
        switch(titleSort) {
            case "asc" :
                titleSort = "des";
                break;
            case "des" :
                titleSort = "none";
                break;
            case "none" :
                titleSort = "asc";
                break;
        }

        System.out.println("sort by title");

        // disable sort by Date
        dateSort = "none";

        ListView av = (ListView) findViewById(android.R.id.list);
        EditText searchEdit = (EditText) findViewById(R.id.searchText);
        SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter) av.getAdapter();
        filterAdapter.getFilter().filter(searchEdit.getText().toString());
    }


    public void sortByDateMing(View view) {
        switch(dateSort) {
            case "asc" :
                dateSort = "des";
                break;
            case "des" :
                dateSort = "none";
                break;
            case "none" :
                dateSort = "asc";
                break;
        }
        System.out.println("sort by date");

        // disable sort by title
        titleSort = "none";

        ListView av = (ListView) findViewById(android.R.id.list);
        EditText searchEdit = (EditText) findViewById(R.id.searchText);
        SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter) av.getAdapter();
        filterAdapter.getFilter().filter(searchEdit.getText().toString());
    }
}
