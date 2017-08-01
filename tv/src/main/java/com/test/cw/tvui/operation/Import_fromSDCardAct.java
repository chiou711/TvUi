package com.test.cw.tvui.operation;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.test.cw.tvui.R;
import com.test.cw.tvui.Util;
import com.test.cw.tvui.util.ColorSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Import_fromSDCardAct extends ListActivity//ListFragment
{
    private List<String> filePathArray = null;
    List<String> fileNames = null;

    @Override
    public void onCreate(Bundle bundle) 
    {
        setContentView(R.layout.sd_file_list);
        System.out.println("Import_fromSDCardAct / _onCreateView ");
        View view = findViewById(R.id.view_back_btn_bg);
        view.setBackgroundColor(ColorSet.getBarColor(this));

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
        int selectedRow = (int)rowId;
        if(selectedRow == 0)
        {
        	//root
            getFiles(new File("/").listFiles());
        }
        else
        {
            final String filePath = filePathArray.get(selectedRow);
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
		           	Intent i = new Intent(this, Import_selectedFileAct.class);
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
                filePathArray.add(file.getPath());
                fileNames.add(file.getName());
	        }
	        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
	        														R.layout.sd_file_list_row,
	        														fileNames);
	        setListAdapter(fileList);
        }
    }
}