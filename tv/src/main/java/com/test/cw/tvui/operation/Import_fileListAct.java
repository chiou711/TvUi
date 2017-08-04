package com.test.cw.tvui.operation;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.cw.tvui.R;
import com.test.cw.tvui.Util;
import com.test.cw.tvui.util.ColorSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Import_fileListAct extends ListActivity
{
    private List<String> filePathArray = null;
    List<String> fileNames = null;
    ListView listView;

    @Override
    public void onCreate(Bundle bundle) 
    {
        setContentView(R.layout.sd_file_list);
        System.out.println("Import_fileListAct / _onCreate");
        View view = findViewById(R.id.view_back_btn_bg);
        view.setBackgroundColor(ColorSet.getBarColor(this));

        listView = (ListView)findViewById(android.R.id.list);
        listView.setItemsCanFocus(true);

        // back button
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

        // do cancel
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        super.onCreate(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        String dirString = Environment.getExternalStorageDirectory().toString() +
                "/" +
                Util.getStorageDirName(this);
        getFiles(new File(dirString).listFiles());
    }

    // on list item click
    @Override
    public void onListItemClick(ListView l, View v, int position, long rowId)
    {
        System.out.println("Import_fileListAct / _onListViewItemClick / position = " + position);
        int selectedRow = position;
        if(selectedRow == 0)
        {
        	//root
            getFiles(new File("/").listFiles());
        }
        else
        {
            final String filePath = filePathArray.get(selectedRow);
            System.out.println("Import_fileListAct / _onListViewItemClick / filePath = " + filePath);
            final File file = new File(filePath);
            if(file.isDirectory())
            {
            	//directory
                getFiles(file.listFiles());
            }
            else
            {
            	// view the selected file's content
            	if( file.isFile() &&
                   (file.getName().contains("XML") ||
                    file.getName().contains("xml")     ))
            	{
		           	Intent i = new Intent(this, Import_fileViewAct.class);
		           	i.putExtra("FILE_PATH", filePath);
		           	startActivity(i);
            	}
            	else
            	{
            		Toast.makeText(this,"file_not_found",Toast.LENGTH_SHORT).show();
            		String dirString = Environment.getExternalStorageDirectory().toString() +
					          "/" +
					          Util.getStorageDirName(this);
                    getFiles(new File(dirString).listFiles());
            	}
            }
        }
    }

    private void getFiles(File[] files)
    {
        if(files == null)
        {
        	Toast.makeText(this,"toast_import_SDCard_no_file",Toast.LENGTH_SHORT).show();
        	this.finish();
        }
        else
        {
//        	System.out.println("files length = " + files.length);
            filePathArray = new ArrayList<String>();
            fileNames = new ArrayList<String>();
            filePathArray.add("");
            fileNames.add("ROOT");
            
	        for(File file : files)
	        {
                System.out.println("Import_fileListAct / _getFiles / file.getPath() = " + file.getPath());
                System.out.println("Import_fileListAct / _getFiles / file.getName() = " + file.getName());
                filePathArray.add(file.getPath());
                fileNames.add(file.getName());

            }
            FilenameAdapter fileList = new FilenameAdapter(this,
                                                           R.layout.sd_file_list_row,
                                                           fileNames);

	        setListAdapter(fileList);
        }

    }

    // File name array for setting focus and file name
    class FilenameAdapter extends ArrayAdapter
    {
        public FilenameAdapter(Context context,int resource,List objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView,ViewGroup parent) {
            if(convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.sd_file_list_row, parent, false);
            }

            convertView.setFocusable(true);
            convertView.setClickable(true);
            TextView tv = (TextView)convertView.findViewById(R.id.text1);
            tv.setText(fileNames.get(position));

            final int item = position;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListItemClick(listView,v,item,item);
                }
            });
            return convertView;
        }
    }

}