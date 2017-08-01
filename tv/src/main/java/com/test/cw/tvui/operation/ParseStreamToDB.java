package com.test.cw.tvui.operation;

import android.content.Context;

import com.test.cw.tvui.MainActivity;
import com.test.cw.tvui.MainFragment;
import com.test.cw.tvui.Util;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.InputStream;

public class ParseStreamToDB {

    private String pageName,title,body,picture,audio,link;
    private FileInputStream fileInputStream = null;
    public volatile boolean isParsing = true;
    public String fileBody = "";
    private String strSplitter;
    private boolean mEnableInsertDB = true;
    private DB_folder mDb_folder;
    private DB_page mDb_page;

    public ParseStreamToDB(Context context, FileInputStream fileInputStream)
    {
	    this.fileInputStream = fileInputStream;

	    int folderTableId = 1;//Util.getPref_lastTimeView_folder_tableId(mContext);
	    mDb_folder = new DB_folder(MainActivity.mAct, folderTableId);

	    int pageTableId = 1;//Util.getPref_lastTimeView_page_tableId(mContext);
	    mDb_page = new DB_page(MainActivity.mAct,pageTableId);
    }

    public String getTitle()
   {
      return title;
   }


    public void parseXMLAndInsertDB(XmlPullParser myParser)
    {

        int event;
        String text=null;

        // last page table Id
        int lastPageTableId;
        if(MainFragment.isNew)
            lastPageTableId = 0;
        else {
            mDb_folder.open();
            lastPageTableId = mDb_folder.getPagesCount(false);
            mDb_folder.close();
        }

        try
        {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT)
            {
        	    String name = myParser.getName(); //name: null, link, item, title, description
        	    switch (event)
	            {
	                case XmlPullParser.START_TAG:
	                if(name.equals("note"))
                    {
	            	    strSplitter = "--- note ---";
                    }
		            break;

	            case XmlPullParser.TEXT:
			       text = myParser.getText();
	            break;

	            case XmlPullParser.END_TAG:
		            if(name.equals("page_name"))
		            {
	                    pageName = text.trim();

					    //TODO add page
                        if(mEnableInsertDB)
                        {
                            int style = 0;//Util.getNewPageStyle(mContext);
                            lastPageTableId++;

                            DB_page.setFocusPage_tableId(lastPageTableId);
                            // style is not set in XML file, so insert default style instead
                            long retId = mDb_folder.insertPage(DB_folder.getFocusFolder_tableName(),
                                    pageName,
                                    lastPageTableId,
                                    style );
                            System.out.println("retId = " + retId);

                            // insert table for new tab
                            mDb_folder.insertPageTable(mDb_folder,1, lastPageTableId, false );//TODO drawerId = 1
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
		            	    DB_page.setFocusPage_tableId(lastPageTableId);//TabsHost.getLastExist_TabId());
						    //TODO add links
		            	    if(title.length() !=0 || body.length() != 0 || picture.length() !=0 || audio.length() !=0 ||link.length() !=0)
		            	    {
                                title = Util.getYouTubeTitle(link);
                                if((!Util.isEmptyString(picture)) || (!Util.isEmptyString(audio)))
                                    mDb_page.insertNote(title, picture, audio, "", link, body,1, (long) 0); // add mark for media
                                else
                                    mDb_page.insertNote(title, picture, audio, "", link, body,0, (long) 0);
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
        	    event = myParser.next();
            }
            System.out.println("parseXMLAndInsertDB / fileBody = " + fileBody);

            // parse finished
            isParsing = false;
            MainFragment.isNew = false;
            if(MainFragment.alertDlg != null)
                MainFragment.alertDlg.dismiss();
            // set preference
            Util.setPref_has_default_import(MainActivity.mAct,true,0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void handleXML()
    {
	    Thread thread = new Thread(new Runnable()
	    {
		    @Override
		    public void run()
		    {
                try
                {
                    InputStream stream = fileInputStream;
                    XmlPullParser myParser = XmlPullParserFactory.newInstance().newPullParser();
                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);
                    parseXMLAndInsertDB(myParser);
                    stream.close();
                }
                catch (Exception e)
                { }
            }
        });
	    thread.start();
    }
   
    public void enableInsertDB(boolean en)
   {
	   mEnableInsertDB = en;
   }
}