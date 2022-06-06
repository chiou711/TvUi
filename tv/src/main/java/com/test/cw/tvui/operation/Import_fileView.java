/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.cw.tvui.operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.cw.tvui.main.MainFragment;
import com.test.cw.tvui.R;
import com.test.cw.tvui.util.ColorSet;
import com.test.cw.tvui.util.Util;
import com.test.cw.tvui.db.DB_folder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import androidx.fragment.app.Fragment;

public class Import_fileView extends Fragment
{

    private TextView mTitleViewText;
    private TextView mBodyViewText;
//    Bundle extras ;
    String filePath;
    static File mFile;
    FileInputStream fileInputStream = null;
    View mViewFile,mViewFileProgressBar;
    public static boolean isAddingNewFolder = true;
    View rootView;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("Import_fileView / _onCreateView");
        rootView = inflater.inflate(R.layout.sd_file_view,container, false);

		mViewFile = rootView.findViewById(R.id.view_file);
		mViewFileProgressBar = rootView.findViewById(R.id.view_file_progress_bar);

		mTitleViewText = (TextView) rootView.findViewById(R.id.view_title);
		mBodyViewText = (TextView) rootView.findViewById(R.id.view_body);


		ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.import_progress);
		if(savedInstanceState == null) {
			ImportAsyncTask viewTask = new ImportAsyncTask();
			viewTask.setProgressBar(progressBar);
			viewTask.enableSaveDB(false);
			viewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			mFile = new File(filePath);
			mTitleViewText.setText(mFile.getName());
			mBodyViewText.setText(parser.fileBody);
		}

		int style = 2;
		//set title color
		mTitleViewText.setTextColor(ColorSet.mText_ColorArray[style]);
		mTitleViewText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
		//set body color
		mBodyViewText.setTextColor(ColorSet.mText_ColorArray[style]);
		mBodyViewText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		// back button
		Button backButton = (Button) rootView.findViewById(R.id.view_back);
		backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

		// confirm button
		Button confirmButton = (Button) rootView.findViewById(R.id.view_confirm);

		// delete button
		Button deleteButton = (Button) rootView.findViewById(R.id.view_delete);
		deleteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete , 0, 0, 0);

		// do cancel
		backButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                Import_fileListAct.isBack_fileView = true;
			}
		});

		// delete the file whose content is showing
		deleteButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view)
			{
				AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
				builder1.setTitle("Confirmation")//TODO locale
						.setMessage("Do you want to delete this file?" +
								" (" + mFile.getName() +")" )
						.setNegativeButton("No", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog1, int which1) {/*nothing to do*/}
						})
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog1, int which1)
							{
								mFile.delete();
                                getActivity().getSupportFragmentManager().popBackStack();
                                Import_fileListAct.isBack_fileView = true;
							}
						})
						.show();
			}
		});

		// confirm to import view to DB
		confirmButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View view)
			{
				isAddingNewFolder = true;
				ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.import_progress);
				ImportAsyncTask confirmTask = new ImportAsyncTask();
				confirmTask.setProgressBar(progressBar);
				confirmTask.enableSaveDB(true);
				confirmTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		// set focus on
        confirmButton.requestFocus();

        return rootView;

	}

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    	System.out.println("Import_fileView / onCreate");
        Bundle arguments = getArguments();
        filePath = arguments.getString("KEY_FILE_PATH");
    }

    static ParseXmlToDB parser;
    private void insertViewContentToDB(boolean enableInsertDB)
    {
		System.out.println("Import_fileView / _insertViewContentToDB / MainFragment.isNewDB = " + MainFragment.isNewDB);
        System.out.println("Import_fileView / _insertViewContentToDB / filePath = " +filePath);
    	mFile = new File(filePath);
    	
    	try 
    	{
    		fileInputStream = new FileInputStream(mFile);
    	} 
    	catch (FileNotFoundException e) 
    	{
    		e.printStackTrace();
    	}
		 
    	// import data by HandleXmlByFile class
    	parser = new ParseXmlToDB(getActivity(),fileInputStream);
    	parser.enableSaveDB(enableInsertDB);
    	parser.startParseThread(DB_folder.getFocusFolder_tableId());
    	while(parser.isParsing);
    }
    
    public static void createDefaultTables(Activity act,int folderTableId)
    {
		String fileName = "default" + String.valueOf(folderTableId)+".xml";
		System.out.println("Import_fileView / _createDefaultTables / fileName = " + fileName);
        File assetsFile = Util.createAssetsFile(act,fileName);

        if(assetsFile == null)
            return;

        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(assetsFile);

            // import data by ParseXmlToDB class
            parser = new ParseXmlToDB(act,fileInputStream);
            parser.enableSaveDB(true);
            parser.startParseThread(folderTableId);
            while(parser.isParsing);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
	// Show progress bar
	public class ImportAsyncTask extends AsyncTask<Void, Integer, Void> {

		ProgressBar bar;
		boolean enableSaveDB;
		public void setProgressBar(ProgressBar bar) {
			this.bar = bar;
		    mViewFile.setVisibility(View.GONE);
		    mViewFileProgressBar.setVisibility(View.VISIBLE);
		    bar.setVisibility(View.VISIBLE);
		}
		
		public void enableSaveDB(boolean enable)
		{
			enableSaveDB = enable;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		    super.onProgressUpdate(values);
		    if (this.bar != null) {
		        bar.setProgress(values[0]);
		    }
		}
		
		@Override
		protected Void doInBackground(Void... params) 
		{
			insertViewContentToDB(enableSaveDB);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			bar.setVisibility(View.GONE);
			mViewFile.setVisibility(View.VISIBLE);
			
			if(enableSaveDB)
			{
                getActivity().getSupportFragmentManager().popBackStack();
                Import_fileListAct.isBack_fileView = true;
				Toast.makeText(getActivity(),"Import finished",Toast.LENGTH_SHORT).show();//TODO locale
			}
			else
			{
			    // show Import content
		    	mTitleViewText.setText(mFile.getName());
		    	mBodyViewText.setText(parser.fileBody);
			}
		}
	}    
}
