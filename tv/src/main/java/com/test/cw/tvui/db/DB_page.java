package com.test.cw.tvui.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 *  Data Base Class for Page
 *
 */
public class DB_page
{

    private Context context = null;
    private DatabaseHelper dB_helper;
    static SQLiteDatabase mSqlDb;

	// Table name format: Page1_2
	private static String DB_PAGE_TABLE_PREFIX = "Page";
    private static String DB_PAGE_TABLE_NAME; // Note: name = prefix + id

	// Note rows
    public static final String KEY_NOTE_ID = "_id"; //do not rename _id for using CursorAdapter (BaseColumns._ID)
    public static final String KEY_NOTE_TITLE = "note_title";
    public static final String KEY_NOTE_BODY = "note_body";
    public static final String KEY_NOTE_MARKING = "note_marking";
    public static final String KEY_NOTE_PICTURE_URI = "note_picture_uri";
    public static final String KEY_NOTE_AUDIO_URI = "note_audio_uri";
    static final String KEY_NOTE_DRAWING_URI = "note_drawing_uri";
    public static final String KEY_NOTE_LINK_URI = "note_link_uri";
    public static final String KEY_NOTE_CREATED = "note_created";

	// DB
    public DB_page mDb_page;

	// Cursor
	public static Cursor mCursor_note;

	// Table Id
    private static int mTableId_page;

    /** Constructor */
	public DB_page(Context context, int pageTableId)
	{
		this.context = context;
		setFocusPage_tableId(pageTableId);
	}

    /**
     * DB functions
     * 
     */
	public DB_page open() throws SQLException
	{
		dB_helper = new DatabaseHelper(context);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = dB_helper.getWritableDatabase();

		//try to get note cursor
		//??? unknown reason, last view page table id could be changed and then cause
		// an exception when getting this cursor
		// workaround: to apply an existing page table that is found firstly
		try
		{
			mCursor_note = this.getNoteCursor_byPageTableId(getFocusPage_tableId());
//			System.out.println("DB_page / _open / open page table OK / table name = " + DB_PAGE_TABLE_NAME);
		}
		catch(Exception e)
		{
			System.out.println("DB_page / _open / open page table NG! / table name = " + DB_PAGE_TABLE_NAME);

//			// since the page table does not exist, delete the tab in com.test.cw.tvui.folder table
//			System.out.println("   getFocusFolder_tableName() = " + getFocusFolder_tableName());
//			System.out.println("   TabsHost.mCurrentTabIndex = " + TabsHost.mNow_pageId);
//			deletePageRow( TabsHost.mNow_pageId, MainActivity.act);
//
//			DB_drawer db_drawer = new DB_drawer(MainActivity.act);
//			db_drawer.open();
//			// find a new one last view page table, if pager table dose not exist
//			int foldersCount = db_drawer.mCursor_folder.getCount();// db_drawer.getFoldersCount();
//
//			for(int folderPos=0; folderPos< foldersCount; folderPos++)
//			{
//				if(db_drawer.mCursor_folder.moveToPosition(folderPos) )
//				{
//					int folder_tableId = db_drawer.getFolderTableId(folderPos);
//					System.out.println("DB_page / folder_tableId = " + folder_tableId);
//					DB_folder.setFocusFolder_tableId(folder_tableId);
//					Util.setPref_lastTimeView_folder_tableId(MainActivity.act, folder_tableId);
//
//					DB_folder db_folder = new DB_folder(MainActivity.act,Util.getPref_lastTimeView_folder_tableId(MainActivity.act));
//					db_folder.open();
//					int pagesCount = db_folder.mCursor_page.getCount();//db_folder.getPagesCount(true);
//					for(int pagePos=0; pagePos < pagesCount; pagePos++)
//					{
//						if(db_folder.mCursor_page.moveToPosition(pagePos))
//						{
//							int page_tableId = db_folder.getPageTableId(pagePos,true);
//							System.out.println("DB_page / find pageTableId = " + page_tableId);
//							setFocusPage_tableId(page_tableId);
//							Util.setPref_lastTimeView_page_tableId(MainActivity.act, page_tableId);
//							db_folder.close();
//							db_drawer.close();
//
//							MainActivity.act.recreate();
//							return DB_page.this;
//						}
//					}//for
//				}
//				db_drawer.close();
//			}//for
		}//catch

		return DB_page.this;
	}

	public void close()
	{
		if((mCursor_note != null)&& (!mCursor_note.isClosed()))
			mCursor_note.close();

		dB_helper.close();
	}

    /**
     *  Page table columns for note row
     * 
     */
    private String[] strNoteColumns = new String[] {
          KEY_NOTE_ID,
          KEY_NOTE_TITLE,
          KEY_NOTE_PICTURE_URI,
          KEY_NOTE_AUDIO_URI,
          KEY_NOTE_DRAWING_URI,
          KEY_NOTE_LINK_URI,
          KEY_NOTE_BODY,
          KEY_NOTE_MARKING,
          KEY_NOTE_CREATED
      };

    // select all notes
    private Cursor getNoteCursor_byPageTableId(int pageTableId) {

        // table number initialization: name = prefix + id
        DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(
                                                    String.valueOf(DB_folder.getFocusFolder_tableId())+
                                                    "_"+
                                                    String.valueOf(pageTableId) );

        return mSqlDb.query(DB_PAGE_TABLE_NAME,
             strNoteColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    //set page table id
    public static void setFocusPage_tableId(int id)
    {
    	mTableId_page = id;
    }
    
    //get page table id
    public static int getFocusPage_tableId()
    {
    	return mTableId_page;
    }
    
    // Insert note
    // createTime: 0 will update time
    public long insertNote(String title,String pictureUri, String audioUri, String drawingUri, String linkUri, String body, int marking, Long createTime)
    { 
    	this.open();

        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_NOTE_TITLE, title);   
        args.put(KEY_NOTE_PICTURE_URI, pictureUri);
        args.put(KEY_NOTE_AUDIO_URI, audioUri);
        args.put(KEY_NOTE_DRAWING_URI, drawingUri);
        args.put(KEY_NOTE_LINK_URI, linkUri);
        args.put(KEY_NOTE_BODY, body);
        if(createTime == 0)
        	args.put(KEY_NOTE_CREATED, now.getTime());
        else
        	args.put(KEY_NOTE_CREATED, createTime);
        	
        args.put(KEY_NOTE_MARKING,marking);
        long rowId = mSqlDb.insert(DB_PAGE_TABLE_NAME, null, args);

        this.close();

        return rowId;  
    }  
    
    public boolean deleteNote(long rowId,boolean enDbOpenClose) 
    {
    	if(enDbOpenClose)
    		this.open();

    	int rowsEffected = mSqlDb.delete(DB_PAGE_TABLE_NAME, KEY_NOTE_ID + "=" + rowId, null);

        if(enDbOpenClose)
        	this.close();

        return (rowsEffected > 0);
    }    
    
    //query note
    public Cursor queryNote(long rowId) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
									DB_PAGE_TABLE_NAME,
					                new String[] {KEY_NOTE_ID,
				  								  KEY_NOTE_TITLE,
				  								  KEY_NOTE_PICTURE_URI,
				  								  KEY_NOTE_AUDIO_URI,
				  								  KEY_NOTE_DRAWING_URI,
				  								  KEY_NOTE_LINK_URI,
        										  KEY_NOTE_BODY,
        										  KEY_NOTE_MARKING,
        										  KEY_NOTE_CREATED},
					                KEY_NOTE_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    // update note
    // 		createTime:  0 for Don't update time
    public boolean updateNote(long rowId, String title, String pictureUri, String audioUri, String drawingUri, 
    						  String linkUri, String body, long marking, long createTime,boolean enDbOpenClose) 
    {
    	if(enDbOpenClose)
    		this.open();

        ContentValues args = new ContentValues();
        args.put(KEY_NOTE_TITLE, title);
        args.put(KEY_NOTE_PICTURE_URI, pictureUri);
        args.put(KEY_NOTE_AUDIO_URI, audioUri);
        args.put(KEY_NOTE_DRAWING_URI, drawingUri);
        args.put(KEY_NOTE_LINK_URI, linkUri);
        args.put(KEY_NOTE_BODY, body);
        args.put(KEY_NOTE_MARKING, marking);
        
        Cursor cursor = queryNote(rowId);
        if(createTime == 0)
        	args.put(KEY_NOTE_CREATED, cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED)));
        else
        	args.put(KEY_NOTE_CREATED, createTime);

        int cUpdateItems = mSqlDb.update(DB_PAGE_TABLE_NAME, args, KEY_NOTE_ID + "=" + rowId, null);

		if(enDbOpenClose)
        	this.close();

		return cUpdateItems > 0;
    }    
    
    
	public int getNotesCount(boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		int count = mCursor_note.getCount();

		if(enDbOpenClose)
			this.close();

		return count;
	}	
	
	public int getCheckedNotesCount()
	{
		this.open();

		int countCheck =0;
		int notesCount = getNotesCount(false);
		for(int i=0;i< notesCount ;i++)
		{
			if(getNoteMarking(i,false) == 1)//??? why exception
				countCheck++;
		}

		this.close();

		return countCheck;
	}		
	
	
	// get note by Id
	public String getNoteLink_byId(Long mRowId)
	{
		this.open();

		String link = queryNote(mRowId).getString(queryNote(mRowId)
									   .getColumnIndexOrThrow(DB_page.KEY_NOTE_LINK_URI));
		this.close();

		return link;
	}	
	
	public String getNoteTitle_byId(Long mRowId)
	{
		this.open();

		String title = queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB_page.KEY_NOTE_TITLE));

		this.close();

		return title;
	}
	
	public String getNoteBody_byId(Long mRowId)
	{
		this.open();

		String id = queryNote(mRowId).getString(queryNote(mRowId)
												.getColumnIndexOrThrow(DB_page.KEY_NOTE_BODY));
		this.close();

		return id;
	}

	public String getNotePictureUri_byId(Long mRowId)
	{
		this.open();

        String pictureUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB_page.KEY_NOTE_PICTURE_URI));

		this.close();

		return pictureUri;
	}
	
	public String getNotePictureUri_byId(Long mRowId, boolean enOpen, boolean enClose)
	{
		if(enOpen)
			this.open();

        String pictureUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB_page.KEY_NOTE_PICTURE_URI));
		if(enClose)
			this.close();

		return pictureUri;
	}	
	
	public String getNoteAudioUri_byId(Long mRowId)
	{
		this.open();

		String audioUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB_page.KEY_NOTE_AUDIO_URI));

		this.close();

		return audioUri;
	}	
	
	public String getNoteDrawingUri_byId(Long mRowId)
	{
		this.open();
		String drawingUri = queryNote(mRowId).getString(queryNote(mRowId)
											 			.getColumnIndexOrThrow(DB_page.KEY_NOTE_DRAWING_URI));
		this.close();

		return drawingUri;
	}	
	
	public String getNoteLinkUri_byId(Long mRowId)
	{
		this.open();
		String linkUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB_page.KEY_NOTE_LINK_URI));
		this.close();

		return linkUri;
	}		
	
	public Long getNoteMarking_byId(Long mRowId)
	{
		this.open();
		Long marking = queryNote(mRowId).getLong(queryNote(mRowId)
											.getColumnIndexOrThrow(DB_page.KEY_NOTE_MARKING));
		this.close();

		return marking;
		
	}

	public Long getNoteCreatedTime_byId(Long mRowId)
	{
		this.open();

		Long time = queryNote(mRowId).getLong(queryNote(mRowId)
											.getColumnIndexOrThrow(DB_page.KEY_NOTE_CREATED));

		this.close();

		return time;
	}

	// get note by position
	public Long getNoteId(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);
	    Long id = mCursor_note.getLong(mCursor_note.getColumnIndex(KEY_NOTE_ID));

		if(enDbOpenClose)
	    	this.close();

		return id;
	}	
	
	public String getNoteTitle(int position,boolean enDbOpenClose)
	{
		String title = null;

		if(enDbOpenClose)
			this.open();

		if(mCursor_note.moveToPosition(position))
			title = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_TITLE));

		if(enDbOpenClose)
        	this.close();

		return title;
	}	
	
	public String getNoteBody(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);

		String body = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_BODY));

		if(enDbOpenClose)
        	this.close();

		return body;
	}
	
	public String getNotePictureUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);

		String pictureUri = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_PICTURE_URI));

		if(enDbOpenClose)
        	this.close();

		return pictureUri;
	}
	
	public String getNoteAudioUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);

		String audioUri = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_AUDIO_URI));

		if(enDbOpenClose)
        	this.close();

		return audioUri;
	}	
	
	public String getNoteDrawingUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose) 
			this.open();

		mCursor_note.moveToPosition(position);
        String drawingUri = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_DRAWING_URI));

		if(enDbOpenClose)
        	this.close();

		return drawingUri;
	}	
	
	public String getNoteLinkUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose) 
			this.open();

		mCursor_note.moveToPosition(position);
        String linkUri = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_LINK_URI));

		if(enDbOpenClose)
        	this.close();

		return linkUri;
	}	
	
	public int getNoteMarking(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);

		int marking = mCursor_note.getInt(mCursor_note.getColumnIndex(KEY_NOTE_MARKING));

		if(enDbOpenClose)
			this.close();

		return marking;
	}
	
	public Long getNoteCreatedTime(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);
		Long time = mCursor_note.getLong(mCursor_note.getColumnIndex(KEY_NOTE_CREATED));

		if(enDbOpenClose)
			this.close();

		return time;
	}
}