package com.test.cw.tvui.operation;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.test.cw.tvui.util.ColorSet;
import com.test.cw.tvui.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

            // simple alphabetic sort
//            Arrays.sort(files);

            // sort by modification
//            Arrays.sort(files, new Comparator<File>() {
//                public int compare(File f1, File f2) {
////                    return Long.compare(f1.lastModified(), f2.lastModified());//old first
//                    return Long.compare(f2.lastModified(), f1.lastModified());//new first
//                }
//            });

            // sort by alphabetic
            Arrays.sort(files, new FileNameComparator());

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

    // Directory group and file group, both directory and file are sorted alphabetically
    // cf. https://stackoverflow.com/questions/24404055/sort-filelist-folders-then-files-both-alphabetically-in-android
    private class FileNameComparator implements Comparator<File>{
        public int compare(File lhsS, File rhsS){
            File lhs = new File(lhsS.toString().toLowerCase(Locale.US));
            File rhs= new File(rhsS.toString().toLowerCase(Locale.US));
            if (lhs.isDirectory() && !rhs.isDirectory()){
                // Directory before File
                return -1;
            } else if (!lhs.isDirectory() && rhs.isDirectory()){
                // File after directory
                return 1;
            } else {
                // Otherwise in Alphabetic order...
                return lhs.getName().compareTo(rhs.getName());
            }
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

            String appName = getString(R.string.app_name);
            convertView.setFocusable(true);
            convertView.setClickable(true);
            TextView tv = (TextView)convertView.findViewById(R.id.text1);
            tv.setText(fileNames.get(position));
            if(fileNames.get(position).contains("sdcard")   ||
               fileNames.get(position).contains(appName)    ||
               fileNames.get(position).contains("LiteNote") ||
               fileNames.get(position).contains("Download")         )
                tv.setTypeface(null, Typeface.BOLD);

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