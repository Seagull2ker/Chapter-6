package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper dbHelper= new TodoDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }
            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());//更新数据
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                TodoContract.FeedEntry.COLUMN_NAME_TITLE,
                TodoContract.FeedEntry.COLUMN_NAME_SUBTITLE,
                TodoContract.FeedEntry.COLUMN_NAME_STATE
        };
        // Filter results WHERE "title" = 'My Title'
        String selection = TodoContract.FeedEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = {"my_title"};
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TodoContract.FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";
        Cursor cursor = db.query(
                TodoContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        //Log.i("ch", "perfrom query data:");
        List<Note> notes =new ArrayList<>();
        while (cursor.moveToNext()) {//循环查询的结果
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.FeedEntry._ID));
            String title = cursor.getString(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_TITLE));
            String subTitle = cursor.getString(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_SUBTITLE));
            int state = cursor.getInt(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_STATE));
            //Log.i("ch", "itemId:" + itemId + ", title:" + title + ", subTitle:" + subTitle);

            Note note = new Note(itemId);
            note.setContent(title);
            note.setDate(subTitle);
            note.setState(state);
            notes.add(note);
        }
        cursor.close();

        return notes;//返回JavaBeans形式的数据
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = TodoContract.FeedEntry.COLUMN_NAME_SUBTITLE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {note.getDate()};
        // Issue SQL statement.
        int deletedRows = db.delete(TodoContract.FeedEntry.TABLE_NAME, selection, selectionArgs);

        notesAdapter.refresh(loadNotesFromDatabase());//更新数据
    }

    private void updateNode(Note note) {
        Log.d("chenhui","进入updateNode");

        // TODO 更新数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TodoContract.FeedEntry.COLUMN_NAME_STATE, note.getState());
        // Which row to update, based on the title
        String selection = TodoContract.FeedEntry.COLUMN_NAME_SUBTITLE + " LIKE ?";
        String[] selectionArgs = {note.getDate()};
        int count = db.update(
                TodoContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        notesAdapter.refresh(loadNotesFromDatabase());//更新数据
    }

}