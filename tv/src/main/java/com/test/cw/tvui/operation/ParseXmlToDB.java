package com.test.cw.tvui.operation;

import android.app.Activity;
import android.content.Context;

import com.test.cw.tvui.main.MainFragment;
import com.test.cw.tvui.util.Util;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.preference.Define;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.InputStream;

public class ParseXmlToDB {

    private String pageName,title,body,picture,audio,link;
    private FileInputStream fileInputStream = null;
    public volatile boolean isParsing = true;
    public String fileBody = "";
    private String strSplitter;
    private boolean mEnableInsertDB = true;
    private DB_folder mDb_folder;
    private DB_page mDb_page;
    private Activity act;

    // constructor
    public ParseXmlToDB(Context context, FileInputStream fileInputStream)
    {
	    this.fileInputStream = fileInputStream;
        act = (Activity) context;
	    int folderTableId = DB_folder.getFocusFolder_tableId();//Util.getPref_lastTimeView_folder_tableId(mContext);
	    mDb_folder = new DB_folder(context, folderTableId);

	    int pageTableId = 1;//Util.getPref_lastTimeView_page_tableId(mContext);
	    mDb_page = new DB_page(context,pageTableId);
    }

    public void enableSaveDB(boolean en)
    {
        mEnableInsertDB = en;
    }

    public String getTitle()
   {
      return title;
   }

    // thread to start parsing
    public void startParseThread(final int folderTableId)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    InputStream stream = fileInputStream;
                    final int folder_table_id = folderTableId;
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(stream, null);
                    parseXML(parser,folder_table_id);
                    stream.close();
                }
                catch (Exception e)
                { }
            }
        });
        thread.start();
    }

    // parse XML
    public void parseXML(XmlPullParser parser, int folderTableId)
    {
        System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / folderTableId = " + folderTableId);
        int event;
        String text=null;

        DB_folder.setFocusFolder_tableId(folderTableId);

        // last page table Id
        int lastPageTableId = 0;
        if(MainFragment.isNewDB && !Import_fileView.isAddingNewFolder ) {
            lastPageTableId = 0;
        }
        else if(!MainFragment.isNewDB || Import_fileView.isAddingNewFolder )
        {
            mDb_folder.open();
            lastPageTableId = mDb_folder.getPagesCount(false);
            mDb_folder.close();
        }


        try
        {
            event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT)
            {
        	    String name = parser.getName(); //name: null, link, item, title, description
        	    switch (event)
	            {
	                case XmlPullParser.START_TAG:
	                if(name.equals("note"))
                    {
	            	    strSplitter = "--- note ---";
                    }
		            break;

                    case XmlPullParser.TEXT:
                       text = parser.getText();
                    break;

                    case XmlPullParser.END_TAG:
                        if(name.equals("page_name"))
                        {
                            pageName = text.trim();

                            //Add page
                            if(mEnableInsertDB)
                            {
                                lastPageTableId++;
                                DB_page.setFocusPage_tableId(lastPageTableId);

                                // style is not set in XML file, so insert default style instead
                                int style = 0;
                                long insertedPageId = mDb_folder.insertPage(DB_folder.getFocusFolder_tableName(),
                                                                    pageName,
                                                                    lastPageTableId,
                                                                    style );
                                System.out.println("insertedPageId = " + insertedPageId);
                                System.out.println("lastPageTableId = " + lastPageTableId);

                                // insert new page table
                                mDb_folder.insertPageTable(mDb_folder,folderTableId, lastPageTableId, false );
                            }

                            fileBody = fileBody.concat(Util.NEW_LINE + "=== " + "Page:" + " " + pageName + " ===");
                        }
                        else if(name.equals("title"))
                        {
                            text = text.replace("[n]"," ");
                            text = text.replace("[s]"," ");
                            title = text.trim();
                        }
                        else if(name.equals("body"))
                        {
                            body = text.trim();
                        }
                        else if(name.equals("picture"))
                        {
                            picture = text.trim();
                        }
                        else if(name.equals("audio"))
                        {
                            audio = text.trim();
                        }
                        else if(name.equals("link"))
                        {
                            link = text.trim();
                            if(mEnableInsertDB)
                            {
                                DB_page.setFocusPage_tableId(lastPageTableId);
                                //Add links
                                if(title.length() !=0 || body.length() != 0 || picture.length() !=0 || audio.length() !=0 ||link.length() !=0)
                                {
                                    // apply title for saving time
                                    if(Util.isEmptyString(title))
                                        title = Util.getYouTubeTitle(link);

                                    long returnedNoteId = mDb_page.insertNote(title, picture, audio, "", link, body,0, (long) 0);
                                    System.out.println("just insert note: returnedNoteId = " + returnedNoteId +
                                                        ", title = " + title +
                                                        ", link = " + link        );
                                }
                            }
                            fileBody = fileBody.concat(Util.NEW_LINE + strSplitter);
                            fileBody = fileBody.concat(Util.NEW_LINE + "title:" + " " + title);
                            fileBody = fileBody.concat(Util.NEW_LINE + "body:" + " " + body);
                            fileBody = fileBody.concat(Util.NEW_LINE + "picture:" + " " + picture);
                            fileBody = fileBody.concat(Util.NEW_LINE + "audio:" + " " + audio);
                            fileBody = fileBody.concat(Util.NEW_LINE + "link:" + " " + link);
                            fileBody = fileBody.concat(Util.NEW_LINE);
                        }
                        break;
	            }
        	    event = parser.next();
            }
//            System.out.println("ParseXmlToDB / _parseXMLAndInsertDB / fileBody = " + fileBody);

            // parse finished, set flags
            isParsing = false;
            if(MainFragment.isNewDB &&
               DB_folder.getFocusFolder_tableId() == Define.DEFAULT_XML_FILES_COUNT)
            {
                MainFragment.isNewDB = false;
                // set preference
                Util.setPref_has_default_import(act,true,0);
            }
            else
                MainFragment.isNewDB = true;

            if(Import_fileView.isAddingNewFolder)
                Import_fileView.isAddingNewFolder = false;

            if(MainFragment.alertDlg != null)
                MainFragment.alertDlg.dismiss();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}