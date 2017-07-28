package com.test.cw.tvui.folder;

import android.app.Activity;

import com.test.cw.tvui.MainActivity;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;


/**
 * Created by CW on 2016/8/23.
 */
public class Folder
{
//    public DragSortListView listView;
//    SimpleDragSortCursorAdapter adapter;
//    DragSortController controller;
    Activity act;

    public Folder(Activity act)
    {
        this.act = act;
    }

    // List all com.test.cw.tvui.folder tables
    public static void listAllPageTables(Activity act)
    {
        // list all com.test.cw.tvui.folder tables
//        int foldersCount = MainActivity.mDb_drawer.getFoldersCount();
//        for(int folderPos=0; folderPos<foldersCount; folderPos++)
        {
//            String folderTitle = MainActivity.mDb_drawer.getFolderTitle(folderPos);
//            MainActivity.mFocus_folderPos = folderPos;

            // list all com.test.cw.tvui.folder tables
//            int folderTableId = MainActivity.mDb_drawer.getFolderTableId(folderPos);
//            System.out.println("--- com.test.cw.tvui.folder table Id = " + folderTableId +
//                               ", com.test.cw.tvui.folder title = " + folderTitle);

            DB_folder db_folder = new DB_folder(act,1);//folderTableId);

            int pagesCount = db_folder.getPagesCount(true);

            for(int pagePos=0; pagePos<pagesCount; pagePos++)
            {
//                TabsHost.mNow_pageId = pagePos;
                int pageId = db_folder.getPageId(pagePos, true);
                int pageTableId = db_folder.getPageTableId(pagePos, true);
                String pageTitle = db_folder.getPageTitle(pagePos, true);
                System.out.println("   --- page Id = " + pageId);
                System.out.println("   --- page table Id = " + pageTableId);
                System.out.println("   --- page title = " + pageTitle);

//                MainActivity.mLastOkTabId = pageId;

                try {
                    DB_page db_page = new DB_page(act,pageTableId);
                    db_page.open();
                    int note_count = db_page.getNotesCount(false);
                    for(int i=0;i<note_count;i++)
                    {
                        String link = db_page.getNoteLinkUri(i,false);
                        System.out.println("   ------ note link = " + link);
                    }

                    db_page.close();
                } catch (Exception e) {
                }
            }
        }
    }

}