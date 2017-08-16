package com.test.cw.tvui.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 *  Data Base Class for Folder
 *
 */
public class DB_folder
{

    private Context context = null;
    private DatabaseHelper dB_helper;
    static SQLiteDatabase mSqlDb;

	// Table name format: Folder1
	static String DB_FOLDER_TABLE_PREFIX = "Folder";

	// Table name format: Page1_2
	private static String DB_PAGE_TABLE_PREFIX = "Page";
    private static String DB_PAGE_TABLE_NAME; // Note: name = prefix + id

	// Page rows
    static final String KEY_PAGE_ID = "page_id"; //can rename _id for using BaseAdapter
    static final String KEY_PAGE_TITLE = "page_title";
    static final String KEY_PAGE_TABLE_ID = "page_table_id";
    static final String KEY_PAGE_STYLE = "page_style";
    static final String KEY_PAGE_CREATED = "page_created";

	// Cursor
	static Cursor mCursor_page;

	// Table Id
	private static int mTableId_folder;

    /** Constructor */
	public DB_folder(Context context, int folderTableId)
	{
		this.context = context;
		setFocusFolder_tableId(folderTableId);
	}

    /**
     * DB functions
     * 
     */
	public DB_folder open() throws SQLException
	{
		dB_helper = new DatabaseHelper(context);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = dB_helper.getWritableDatabase();

        try
        {
            mCursor_page = this.getPageCursor_byFolderTableId(getFocusFolder_tableId());

            // since no page is created in com.test.cw.tvui.folder table, delete the com.test.cw.tvui.folder Id
            // but this is not for importing preference condition
//            if( mCursor_page.getCount() == 0)
//            {
//                if(Define.HAS_PREFERENCE )//&&
////                   !Util.getPref_has_default_import(MainActivity.act,MainAct.mFocus_folderPos) )
//                {
//                    // importing preference
//                }
//                else {
//                    DB_drawer db_drawer = new DB_drawer(context);
//                    int folderId = (int) db_drawer.getFolderId(0);//MainActivity.mFocus_folderPos);
//                    // since the com.test.cw.tvui.folder table does not exist, delete the com.test.cw.tvui.folder Id in drawer table
//                    db_drawer.deleteFolderId(folderId);
//                }
//            }
        }
        catch (Exception e)
        {
            System.out.println("DB_folder / open com.test.cw.tvui.folder table NG! / table id = " + getFocusFolder_tableId());
//            DB_drawer db_drawer = new DB_drawer(context);
//            int folderId =  (int) db_drawer.getFolderId(0);//MainAct.mFocus_folderPos);
//            // since the com.test.cw.tvui.folder table does not exist, delete the com.test.cw.tvui.folder Id in drawer table
//            db_drawer.deleteFolderId(folderId);
        }

        return DB_folder.this;
	}

	public void close()
	{
        if((mCursor_page != null) && (!mCursor_page.isClosed()))
            mCursor_page.close();
		dB_helper.close();
	}

    //insert new page table by
    // 1 SQLiteDatabase
    // 2 assigned drawer Id
    // 3 page table Id
    public void insertPageTable(DB_folder db, int folderId, int pageId, boolean is_SQLiteOpenHelper_onCreate)
    {   
    	if(!is_SQLiteOpenHelper_onCreate)
    		db.open();

        //format "Page1_2"
    	DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(String.valueOf(folderId)+
    														"_"+
    														String.valueOf(pageId));
        String dB_insert_table = "CREATE TABLE IF NOT EXISTS " + DB_PAGE_TABLE_NAME + "(" +
        							DB_page.KEY_NOTE_ID + " INTEGER PRIMARY KEY," +
									DB_page.KEY_NOTE_TITLE + " TEXT," +
									DB_page.KEY_NOTE_PICTURE_URI + " TEXT," +
									DB_page.KEY_NOTE_AUDIO_URI + " TEXT," +
									DB_page.KEY_NOTE_DRAWING_URI + " TEXT," +
									DB_page.KEY_NOTE_LINK_URI + " TEXT," +
									DB_page.KEY_NOTE_BODY + " TEXT," +
									DB_page.KEY_NOTE_MARKING + " INTEGER," +
									DB_page.KEY_NOTE_CREATED + " INTEGER);";
        mSqlDb.execSQL(dB_insert_table);

        if(!is_SQLiteOpenHelper_onCreate)
        	db.close();
    }

    //delete page table
    public void dropPageTable(int id)
    {   
    	this.open();

        //format "Page1_2"
    	DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(String.valueOf(getFocusFolder_tableId())+"_"+String.valueOf(id));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_PAGE_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);         

        this.close();
    }   
    
    //delete page table by drawer com.test.cw.tvui.folder table Id
    public void dropPageTable(int drawerFolderTableId, int id)
    {   
    	this.open();

        //format "Page1_2"
    	DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(String.valueOf(drawerFolderTableId)+"_"+String.valueOf(id));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_PAGE_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);         

        this.close();
    } 
    
    /*
     * Folder table columns for page row
     * 
     */
    String[] strPageColumns = new String[] {
			KEY_PAGE_ID,
			KEY_PAGE_TITLE,
			KEY_PAGE_TABLE_ID,
			KEY_PAGE_STYLE,
			KEY_PAGE_CREATED
        };   

    // get page cursor
    public Cursor getPageCursor_byFolderTableId(int i) {
        return mSqlDb.query(DB_FOLDER_TABLE_PREFIX + String.valueOf(i),
							strPageColumns,
							null,
							null,
							null,
							null,
							null
							);
    }

//    // insert page with SqlDb parameter
//    public static long insertPage(SQLiteDatabase sqlDb, String intoTable, String title, long ntId, int style)
//    {
//        Date now = new Date();
//        ContentValues args = new ContentValues();
//        args.put(KEY_PAGE_TITLE, title);
//        args.put(KEY_PAGE_TABLE_ID, ntId);
//        args.put(KEY_PAGE_STYLE, style);
//        args.put(KEY_PAGE_CREATED, now.getTime());
//
//        return sqlDb.insert(intoTable, null, args);
//    }
    
    // insert page
    public long insertPage(String intoTable, String title, long ntId, int style)
    {
    	this.open();

        Date now = new Date();
        ContentValues args = new ContentValues();
        args.put(KEY_PAGE_TITLE, title);
        args.put(KEY_PAGE_TABLE_ID, ntId);
        args.put(KEY_PAGE_STYLE, style);
        args.put(KEY_PAGE_CREATED, now.getTime());
        long rowId = mSqlDb.insert(intoTable, null, args);

        this.close();

        return rowId;

    }
    
    // delete page
    public long deletePage(String table, int tabId)
    {
    	System.out.println("DB / deletePage / table = " + table + ", tab Id = " + tabId);

        this.open();
        long rowsNumber = mSqlDb.delete(table, KEY_PAGE_ID + "='" + tabId +"'", null);
        this.close();

        if(rowsNumber > 0)
        	System.out.println("DB / deletePage / rowsNumber =" + rowsNumber);
        else
        	System.out.println("DB / deletePage / failed to delete");

        return rowsNumber;
    }

    //update page
    public boolean updatePage(long id, String title, long ntId, int style, boolean enDbOpenClose)
    {
        if(enDbOpenClose)
    	    this.open();

        ContentValues args = new ContentValues();
        Date now = new Date();
        args.put(KEY_PAGE_TITLE, title);
        args.put(KEY_PAGE_TABLE_ID, ntId);
        args.put(KEY_PAGE_STYLE, style);
        args.put(KEY_PAGE_CREATED, now.getTime());
        int rowsNumber = mSqlDb.update(DB_FOLDER_TABLE_PREFIX +String.valueOf(getFocusFolder_tableId()), args, KEY_PAGE_ID + "=" + id, null);

        if(enDbOpenClose)
            this.close();

        return  (rowsNumber>0)?true:false;
    }

    public Cursor getPageCursor()
    {
		return mCursor_page;
    }
    
	public int getPagesCount(boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

        int count = mCursor_page.getCount();

        if(enDbOpenClose)
			this.close();

		return count;
	}

	public int getPageId(int position, boolean enDbOpenClose)
	{
        if(enDbOpenClose)
            this.open();

        if(mCursor_page.moveToPosition(position))
        {
            int pageId = mCursor_page.getInt(mCursor_page.getColumnIndex(KEY_PAGE_ID));
//			System.out.println("DB_folder / _getPageId / pageId = " + pageId);

            if(enDbOpenClose)
                this.close();

            return pageId;
        }
        else
        {
            if(enDbOpenClose)
                this.close();

            return -1;
        }
	}

    //get current page title
    public String getCurrentPageTitle()
    {
    	String title = null;

    	this.open();
    	int pagesCount = getPagesCount(false);
    	for(int i=0;i< pagesCount; i++ )
    	{
    		if( Integer.valueOf(DB_page.getFocusPage_tableId()) == getPageTableId(i,false))
    		{
    			title = getPageTitle(i,false);
    		}
    	}
    	this.close();

        return title;
    }

	public int getPageTableId(int position, boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

        mCursor_page.moveToPosition(position);
        int id = mCursor_page.getInt(mCursor_page.getColumnIndex(KEY_PAGE_TABLE_ID));

        if(enDbOpenClose)
        	this.close();

        return id;
	}

	public String getPageTitle(int position, boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

        mCursor_page.moveToPosition(position);
        String title = mCursor_page.getString(mCursor_page.getColumnIndex(KEY_PAGE_TITLE));

        if(enDbOpenClose)
        	this.close();

        return title;
	}

	public int getPageStyle(int position, boolean enDbOpenClose)
	{
		int style = 0;

        if(enDbOpenClose)
		    this.open();

        if(mCursor_page.moveToPosition(position))
			style = mCursor_page.getInt(mCursor_page.getColumnIndex(KEY_PAGE_STYLE));

        if(enDbOpenClose)
            this.close();

        return style;
	}

    public static void setFocusFolder_tableId(int i)
    {
    	mTableId_folder = i;
        System.out.println("DB_folder / _setFocusFolder_tableId / mTableId_folder = " + mTableId_folder);
    }

    public static int getFocusFolder_tableId()
    {
    	return mTableId_folder;
    }
    
	// get current com.test.cw.tvui.folder table name
	public static String getFocusFolder_tableName()
	{
		return DB_folder.DB_FOLDER_TABLE_PREFIX + DB_folder.getFocusFolder_tableId();
	}
}