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
package ua.naiksoftware.chars;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import ua.naiksoftware.chars.games.*;

/**
 *
 * @author Naik
 */
public class MainActivity extends Activity {

    private static final String tag = "MainActivity";
    public static String login, pass;
    public static SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(tag, "########## MAIN ON CREATE ##########");
        prefs = getSharedPreferences(tag, Activity.MODE_PRIVATE);
        login = prefs.getString("login", "no");
        pass = prefs.getString(pass, "no");
    }

    public void startSnakeSingle(View view) {
        Log.d(tag, "startSnakeSingle started");
        startActivityForResult(new Intent(this, SnakeSingleActivity.class), 1);
    }

    // TODO: Multiplayer
    public void startSnakeMulti(View view) {
    }
    
    // TODO: Racing
    public void startRacing(View view) {
    }

    public void showRecord(View view) {
        Log.d(tag, "showRecord");
        Intent i = new Intent(this, RecordActivity.class);
        startActivityForResult(i, 1);
    }

    public void showInfo(View view) {
        Log.d(tag, "showInfo");
        TextView info = new TextView(this);
        info.setText(R.string.info);
        info.setTextSize(15);
        info.setPadding(20, 20, 20, 20);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setCancelable(true);
        builder.setView(info);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface p1, int p2) {
                // auto close
            }
        });
        builder.show();
    }
}
