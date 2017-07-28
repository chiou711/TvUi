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

import com.test.cw.tvui.R;
import com.test.cw.tvui.ColorSet;
import com.test.cw.tvui.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Import_selectedFileAct extends Activity 
{

    private TextView mTitleViewText;
    private TextView mBodyViewText;
    Bundle extras ;
    static File mFile;
    FileInputStream fileInputStream = null;
    View mViewFile,mViewFileProgressBar;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    	System.out.println("Import_selectedFileAct / onCreate");

        setContentView(R.layout.view_file);
        mViewFile = findViewById(R.id.view_file);
        mViewFileProgressBar = findViewById(R.id.view_file_progress_bar);

        mTitleViewText = (TextView) findViewById(R.id.view_title);
        mBodyViewText = (TextView) findViewById(R.id.view_body);

	    getActionBar().setDisplayShowHomeEnabled(false);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progress);
		if(savedInstanceState == null) {
			ImportAsyncTask task = new ImportAsyncTask();
			task.setProgressBar(progressBar);
			task.enableSaveDB(false);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//mFile is created inside ImportAsyncTask / _insertSelectedFileContentToDB
		}
		else
		{
			extras = getIntent().getExtras();
			mFile = new File(extras.getString("FILE_PATH"));
			mTitleViewText.setText(mFile.getName());
			mBodyViewText.setText(importObject.fileBody);
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
                finish();
            }
        });

		// delete the file whose content is showing
		deleteButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view)
			{
				Util util = new Util(Import_selectedFileAct.this);
//				util.vibrate();

				AlertDialog.Builder builder1 = new AlertDialog.Builder(Import_selectedFileAct.this);
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
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progress);
				ImportAsyncTask task = new ImportAsyncTask();
				task.setProgressBar(progressBar);
				task.enableSaveDB(true);
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
    }

    static ParseStreamToDB importObject;
    private void insertSelectedFileContentToDB(boolean enableInsertDB) 
    {
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
    	importObject = new ParseStreamToDB(this,fileInputStream);
    	importObject.enableInsertDB(enableInsertDB);
    	importObject.handleXML();
    	while(importObject.isParsing);
    }
    
    public static void createDefaultTables(Activity act,String fileName)
    {
		System.out.println("Import_selectedFileAct / _createDefaultTables / fileName = " + fileName);

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

        // import data by HandleXmlByFile class
        importObject = new ParseStreamToDB(act,fileInputStream);
        importObject.enableInsertDB(true);
        importObject.handleXML();
        while(importObject.isParsing);
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
//			Util.lockOrientation(Import_selectedFileAct.this);
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
			insertSelectedFileContentToDB(enableSaveDB);
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
//				Util.unlockOrientation(Import_selectedFileAct.this);
				Toast.makeText(Import_selectedFileAct.this,"Import finished",Toast.LENGTH_SHORT).show();
			}
			else
			{
			    // show Import content
		    	mTitleViewText.setText(mFile.getName());
		    	mBodyViewText.setText(importObject.fileBody);
			}
		}
	}    
}
