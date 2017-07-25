package com.test.cw.tvui;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.InputStream;

class ParseStream {

   private String pageName,title,body,picture,audio,link;
   private Context mContext;
   
   private FileInputStream fileInputStream = null;
   volatile boolean parsingComplete = true;
   String fileBody = "";
   private String strSplitter;
//   private boolean mEnableInsertDB = true;

   //

   ParseStream(Context context,FileInputStream fileInputStream)
   {
	   mContext = context;
	   this.fileInputStream = fileInputStream;
   }
   
   public String getTitle()
   {
      return title;
   }
   
   public String getPage()
   {
      return pageName;
   }
   
   public void parseXMLAndInsertDB(XmlPullParser myParser)
   {
	  
      int event;
      String text=null;
      int linkIndex = -1;
      int pageIndex = -1;

      try 
      {
         event = myParser.getEventType();
         while (event != XmlPullParser.END_DOCUMENT) 
         {
        	 String name = myParser.getName(); //name: null, link, item, title, description
//        	 System.out.println("ParseStream / _parseXMLAndInsertDB / name = " + name);
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
                      pageIndex++;
                      MainFragment.pagesArr[pageIndex] = pageName;
                      linkIndex = -1;
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
//					  picture = Util.getDefaultExternalStoragePath(picture);
	               }		           
	               else if(name.equals("audio"))
	               { 	
	            	  audio = text.trim();
//					  audio = Util.getDefaultExternalStoragePath(audio);
				   }
	               else if(name.equals("link"))
	               { 	
	            	  link = text.trim();
//	            	  if(mEnableInsertDB)
	            	  {
//		            	  DB_page.setFocusPage_tableId(TabsHost.getLastExist_TabId());
						  //TODO add links
		            	  if(title.length() !=0 || body.length() != 0 || picture.length() !=0 || audio.length() !=0 ||link.length() !=0)
		            	  {
                              linkIndex++;
							  MainFragment.linksArr[pageIndex][linkIndex] = link;
		            	  }
	            	  }
		              fileBody = fileBody.concat(Util.NEW_LINE + strSplitter);
		              fileBody = fileBody.concat(Util.NEW_LINE + "title:" + " " + title);
		        	  fileBody = fileBody.concat(Util.NEW_LINE + "body:" + " " + body);
		        	  fileBody = fileBody.concat(Util.NEW_LINE + "picture:" + " " + picture);
		        	  fileBody = fileBody.concat(Util.NEW_LINE + "audio:" + " " + audio);
		        	  fileBody = fileBody.concat(Util.NEW_LINE + "link:" + " " + MainFragment.linksArr);
	            	  fileBody = fileBody.concat(Util.NEW_LINE);
	               }	               
	               break;
	         }		 
        	 event = myParser.next();
         }
//         System.out.println("import_handleXmlFile / fileBody = " + fileBody);
         parsingComplete = false;
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

   void handleXML()
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
   
//   void enableInsertDB(boolean en)
//   {
//	   mEnableInsertDB = en;
//   }
}