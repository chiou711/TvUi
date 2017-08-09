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
import android.view.View;
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

public class Import_fileViewAct extends Activity
{

    private TextView mTitleViewText;
    private TextView mBodyViewText;
    Bundle extras ;
    static File mFile;
    FileInputStream fileInputStream = null;
    View mViewFile,mViewFileProgressBar;
    public static boolean isAddingNewFolder = true;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    	System.out.println("Import_fileViewAct / onCreate");

        setContentView(R.layout.view_file);
        mViewFile = findViewById(R.id.view_file);
        mViewFileProgressBar = findViewById(R.id.view_file_progress_bar);

        mTitleViewText = (TextView) findViewById(R.id.view_title);
        mBodyViewText = (TextView) findViewById(R.id.view_body);

//	    getActionBar().setDisplayShowHomeEnabled(false);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progress);
		if(savedInstanceState == null) {
			ImportAsyncTask viewTask = new ImportAsyncTask();
			viewTask.setProgressBar(progressBar);
			viewTask.enableSaveDB(false);
			viewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			extras = getIntent().getExtras();
			mFile = new File(extras.getString("FILE_PATH"));
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
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

		// confirm button
		Button confirmButton = (Button) findViewById(R.id.view_confirm);

		// delete button
		Button deleteButton = (Button) findViewById(R.id.view_delete);
		deleteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete , 0, 0, 0);

        // do cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                finish();//TODO go back to Import_fileListAct
            }
        });

		// delete the file whose content is showing
		deleteButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view)
			{
				AlertDialog.Builder builder1 = new AlertDialog.Builder(Import_fileViewAct.this);
				builder1.setTitle("Confirmation")
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
								finish();
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
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progress);
				ImportAsyncTask confirmTask = new ImportAsyncTask();
				confirmTask.setProgressBar(progressBar);
				confirmTask.enableSaveDB(true);
				confirmTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
    }

    static ParseXmlToDB parser;
    private void insertViewContentToDB(boolean enableInsertDB)
    {
		System.out.println("Import_fileViewAct / _insertViewContentToDB / MainFragment.isNewDB = " + MainFragment.isNewDB);
		extras = getIntent().getExtras();
    	mFile = new File(extras.getString("FILE_PATH"));
    	
    	try 
    	{
    		fileInputStream = new FileInputStream(mFile);
    	} 
    	catch (FileNotFoundException e) 
    	{
    		e.printStackTrace();
    	}
		 
    	// import data by HandleXmlByFile class
    	parser = new ParseXmlToDB(this,fileInputStream);
    	parser.enableSaveDB(enableInsertDB);
    	parser.startParseThread(DB_folder.getFocusFolder_tableId());
    	while(parser.isParsing);
    }
    
    public static void createDefaultTables(Activity act,int folderTableId)
    {
		String fileName = "default" + String.valueOf(folderTableId)+".xml";
		System.out.println("Import_fileViewAct / _createDefaultTables / fileName = " + fileName);
        FileInputStream fileInputStream = null;
        File assetsFile = Util.createAssetsFile(act,fileName);
        try
        {
            fileInputStream = new FileInputStream(assetsFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // import data by ParseXmlToDB class
        parser = new ParseXmlToDB(act,fileInputStream);
        parser.enableSaveDB(true);
        parser.startParseThread(folderTableId);
        while(parser.isParsing);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
	// Show progress bar
	public class ImportAsyncTask extends AsyncTask<Void, Integer, Void> {

		ProgressBar bar;
		boolean enableSaveDB;
		public void setProgressBar(ProgressBar bar) {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
//			Util.lockOrientation(Import_fileViewAct.this);
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
				finish();
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//				Util.unlockOrientation(Import_fileViewAct.this);
				Toast.makeText(Import_fileViewAct.this,"Import finished",Toast.LENGTH_SHORT).show();
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
