package com.test.cw.tvui.folder;

import android.app.Activity;

import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.preference.Define;


/**
 * Created by CW on 2017/8/4.
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

            for(int i=1;i<= Define.ORIGIN_FOLDERS_COUNT;i++)
            {
                System.out.println("--- folder table Id = " + i );
                DB_folder db_folder = new DB_folder(act, i);//folderTableId);
//                DB_folder.setFocusFolder_tableId(i);
                db_folder.open();
                int pagesCount = db_folder.getPagesCount(false);
                db_folder.close();

                System.out.println("--- pagesCount = " + pagesCount );

                for (int pagePos = 0; pagePos < pagesCount; pagePos++)
                {
                    int pageId = db_folder.getPageId(pagePos, true);
                    int pageTableId = db_folder.getPageTableId(pagePos, true);
                    String pageTitle = db_folder.getPageTitle(pagePos, true);
                    System.out.println("   --- page Id = " + pageId);
                    System.out.println("   --- page table Id = " + pageTableId);
                    System.out.println("   --- page title = " + pageTitle);


                    try {
                        DB_page db_page = new DB_page(act, pageTableId);
                        db_page.open();
                        int note_count = db_page.getNotesCount(false);
                        for (int cnt = 0; cnt < note_count; cnt++) {
                            String link = db_page.getNoteLinkUri(cnt, false);
                            System.out.println("   ------ note link = " + link);
                        }

                        db_page.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

}