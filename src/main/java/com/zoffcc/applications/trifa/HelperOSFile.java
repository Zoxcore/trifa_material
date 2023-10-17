package com.zoffcc.applications.trifa;

import java.awt.*;
import java.io.File;

public class HelperOSFile {

    private static final String TAG = "trifa.Hlp.HelperOSFile";

    public static void show_containing_dir_in_explorer(final String filename_with_path)
    {
        try
        {
            String containing_dir = new File(filename_with_path).getParent();
            if (containing_dir != null)
            {
                if (!containing_dir.equals(""))
                {
                    File c_dir = new File(containing_dir);
                    if (c_dir.isDirectory())
                    {
                        Desktop.getDesktop().open(c_dir);
                    }
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }
}
