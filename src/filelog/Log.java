/**
 * Game create for competition on http://annimon.com
 *
 * Copyright (C) 2012, 2013 NaikSoftware
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package filelog;

import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Log {

    private static final String token = " : ";
    private static final long MAX_LEN = 51200;//50 Kb

    public static void d(String tag, String message) {
        try {
            boolean noClear;
            File file = new File(Environment.getExternalStorageDirectory(), "chars-log.txt");
            if (file.length() > MAX_LEN) {
                noClear = false;
            } else {
                noClear = true;
            }
            FileWriter fw = new FileWriter(file, noClear);
            String msg = "\n" + new Date().toLocaleString() + token + tag + token + message;
            fw.write(msg);
            fw.flush();
            fw.close();
            //Log.d("L", msg);
        } catch (IOException e) {
            //Log.e("L", "err in logging", e);
        }
    }

}
