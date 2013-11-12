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
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.*;
import android.util.*;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;
//import filelog.Log;

/**
 *
 * @author Naik
 */
public abstract class EngineActivity extends Activity implements SizeListener, View.OnClickListener, Runnable {

    public static Context CONTEXT;
    public static final char FON = 'y';
    public static final int ACTION_LEFT_1 = 1;
    public static final int ACTION_RIGHT_1 = 2;
    public static final int ACTION_LEFT_2 = 3;
    public static final int ACTION_RIGHT_2 = 4;
    public static final int TEXT_SIZE = 12;
    public static final int STATE_NOT_INIT = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_RUNNING = 3;
    private static final byte SEND_RECORD = 1;
    private static final byte GAME_OVER = 2;
    private static final byte CHECK_LOGIN_AND_PASS = 3;
    private static final byte CHECK_LOGIN_OR_PASS_ERR = 4;
    private static final byte CHECK_LOGIN_AND_PASS_OK = 5;
    private static final String tag = "EngineActivity";
    private static final int MSG_REPAINT_GAME = 1;
    private static final int MSG_REPAINT_SCORE = 2;
    /* для ввода логина и пароля */
    private View inputLoginAndPass;
    private EditText loginEditText;
    private EditText passEditText;
    private String newLogin, newPass;
    private String errLoginOrPass;
    private AlertDialog dialogWaitReg, dialogFormReg, dialogQestionReg;

    private GameView gameField;
    private Panel panel;
    private Button btnLeft;
    private Button btnRight;
    private Button btnTwoLeft; // show in multiplayer
    private Button btnTwoRight;// show in multiplayer
    private Button btnPause;
    private byte state;// game state
    private int inRow;
    private int inColumn;
    private long timeSave, time;
    private int game_time;
    private int score;
    private char[][] gameArea;
    private Thread game;
    private Thread timeThread;
    private boolean hasPhysicalKeyboard, isLandscape;
    private int lastKey; // Для проверки нажатия одной и той же клавиши

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        state = STATE_NOT_INIT;
        CONTEXT = this;
        inputLoginAndPass = LayoutInflater.from(CONTEXT).inflate(R.layout.reg_form, null);
        loginEditText = (EditText) inputLoginAndPass.findViewById(R.id.login);
        passEditText = (EditText) inputLoginAndPass.findViewById(R.id.pass);
        setContentView(R.layout.game);

        gameField = (GameView) findViewById(R.id.gameField);
        gameField.setZOrderOnTop(true);
        gameField.getHolder().setFormat(PixelFormat.TRANSPARENT);
        gameField.setOnSizeListener(this);
        panel = (Panel) findViewById(R.id.panel);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
        btnTwoLeft = (Button) findViewById(R.id.btnTwoLeft);
        btnTwoRight = (Button) findViewById(R.id.btnTwoRight);
        btnPause = (Button) findViewById(R.id.btnPause);

        if (isGameMultiplayer()) {
            findViewById(R.id.layoutMultiplayer).setVisibility(View.VISIBLE);
            panel.setVisibility(View.GONE);
        }

        Configuration config = getResources().getConfiguration();
        hasPhysicalKeyboard = (config.keyboard != Configuration.KEYBOARD_NOKEYS);
        isLandscape = (config.orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (hasPhysicalKeyboard && isLandscape) {
            btnLeft.setVisibility(View.GONE);
            btnRight.setVisibility(View.GONE);
            lastKey = 0;
        }

        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnTwoLeft.setOnClickListener(this);
        btnTwoRight.setOnClickListener(this);
    }

    /**
     * Игровое поле отображается и готово к запуску игры
     *
     * @param inRow в ряду элементов
     * @param inColumn в столбце элементов
     */
    @Override
    public void onSizeChanged(int inRow, int inColumn) {
        this.inRow = inRow;
        this.inColumn = inColumn;
        gameArea = new char[inColumn][inRow];
        initArea();                                 // рассчитали размеры поля теперь инициализируем игровое поле
        sendAreaToView();                           // отсылаем поле в GameView
        gameStart(inRow, inColumn, gameArea);       // инициализация игры
        btnPause.setText(">>");
        state = STATE_PAUSED;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (state == STATE_RUNNING) {
            gamePause(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sendAreaToView();
    }

    /**
     * Задано в XML
     *
     * @param v - кнопка паузы|продолжения
     */
    public void gamePause(View v) {
        if (state == STATE_PAUSED || state == STATE_NOT_INIT) {
            gameResume(state);
            state = STATE_RUNNING;// теперь можем запускать цикл
            btnPause.setText("||");
            sendAreaToView();
            timeThread = new Thread(repaintTimeAndScores);
            timeThread.start();
            game = new Thread(this);
            game.start();
            time = System.currentTimeMillis() - timeSave;
        } else if (state == STATE_RUNNING) {
            gamePause();
            state = STATE_PAUSED;
            btnPause.setText(">>");
            timeThread.interrupt();
            game.interrupt();
            timeSave = System.currentTimeMillis() - time;
        }
    }

    protected abstract void gameStart(int inRow, int inColumn, char[][] gameArea);

    protected abstract void gameLoop();

    protected abstract void gamePause();

    protected abstract void gameResume(int state);

    protected abstract void gameAction(int action);

    protected abstract boolean isGameMultiplayer();

    protected void sendAreaToView() {
        repaint.sendEmptyMessage(MSG_REPAINT_GAME);// paint in UI thread
    }
    Handler repaint = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPAINT_GAME:
                    gameField.setArea(gameArea);
                    break;
                case MSG_REPAINT_SCORE:
                    panel.setText(score, round((System.currentTimeMillis() - time) / 1000f));
                    panel.invalidate();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int action = 0;
        switch (v.getId()) {
            case R.id.btnLeft:
                action = ACTION_LEFT_1;
                break;
            case R.id.btnRight:
                action = ACTION_RIGHT_1;
                break;
            case R.id.btnTwoLeft:
                action = ACTION_LEFT_2;
                break;
            case R.id.btnTwoRight:
                action = ACTION_RIGHT_2;
                break;
        }
        gameAction(action);
    }

    /*
    * method by aNNiMON
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if (!hasPhysicalKeyboard) {
            return super.onKeyDown(keyCode, msg);
        }

        int action = 0;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                action = ACTION_LEFT_1;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                action = ACTION_RIGHT_1;
                break;
        }
        if (lastKey != keyCode) {
            lastKey = keyCode;
            gameAction(action);
        }

        return super.onKeyDown(keyCode, msg);
    }

    private void initArea() {
        for (int i = 0; i < inColumn; i++) {
            for (int j = 0; j < inRow; j++) {
                gameArea[i][j] = FON;
            }
        }
    }

    @Override
    public void run() {
        while (state == STATE_RUNNING) {
            gameLoop();
        }
    }

    public int getState() {
        return state;
    }

    public void setScore(int _score) {
        score = _score;
    }
    Runnable repaintTimeAndScores = new Runnable() {
        public void run() {
            while (state == STATE_RUNNING) {
                try {
                    Thread.sleep(100);
                    repaint.sendEmptyMessage(MSG_REPAINT_SCORE);
                } catch (InterruptedException e) {
                }
            }
        }
    };

    protected static float round(float sourceNum) {
        int temp = (int) (sourceNum / 0.1f);
        return temp / 10f;
    }

    /**
     * Рекорд не установлен, просто выводим уведомление и закрываем
     */
    protected void gameOver() {
        send.sendEmptyMessage(GAME_OVER);// go to UI thread
    }

    /**
     * Отсылаем рекорды если игрок не против
     */
    protected void sendRecord() {
        game_time = (int) round((System.currentTimeMillis() - time) / 100f);
        send.sendEmptyMessage(SEND_RECORD);
    }
    Handler send = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (state == STATE_RUNNING) {
                gamePause(null);// Остановим игру
            }
            if (msg.what == SEND_RECORD) {
                if (dialogQestionReg == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT);
                    builder.setTitle(R.string.send_record);
                    builder.setIcon(android.R.drawable.ic_menu_upload);
                    builder.setMessage(getString(R.string.text_send_record) + " " + score + "?");
                    builder.setCancelable(false);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface p1, int p2) {
                            if (MainActivity.login.equals("no") || MainActivity.pass.equals("no")) {
                                send.sendEmptyMessage(CHECK_LOGIN_AND_PASS);
                            } else {
                                new Sender(EngineActivity.this).execute(score, game_time);
                            }
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface p1, int p2) {
                            finish();
                        }
                    });
                    dialogQestionReg = builder.create();
                }
                dialogQestionReg.show();
            } else if (msg.what == GAME_OVER) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT);
                builder.setTitle(R.string.game_over);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setMessage(getString(R.string.text_game_over) + " " + score + ".");
                builder.setCancelable(false);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface p1, int p2) {
                        finish();
                    }
                });
                builder.show();
            } else if (msg.what == CHECK_LOGIN_AND_PASS) {
                if (dialogFormReg == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT);
                    builder.setCancelable(false);
                    builder.setView(inputLoginAndPass);
                    builder.setPositiveButton(android.R.string.ok, inputNameDialogListener);
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    });
                    dialogFormReg = builder.create();
                }
                dialogFormReg.show();
            } else if (msg.what == CHECK_LOGIN_OR_PASS_ERR) {
                Toast toast = Toast.makeText(CONTEXT, errLoginOrPass, Toast.LENGTH_LONG);
                toast.show();
                send.sendEmptyMessage(CHECK_LOGIN_AND_PASS);
            } else if (msg.what == CHECK_LOGIN_AND_PASS_OK) {
                new Sender(EngineActivity.this).execute(score, game_time);
            }
        }
    };
    DialogInterface.OnClickListener inputNameDialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface p1, int p2) {
            newLogin = loginEditText.getText().toString();
            newPass = passEditText.getText().toString();
            Pattern p = Pattern.compile("[\\w]{3,10}");
            Matcher mL = p.matcher(newLogin);
            Matcher mP = p.matcher(newPass);
            if (!mL.matches() || !mP.matches()) {
                errLoginOrPass = getString(R.string.invalid_login_or_pass);
                send.sendEmptyMessage(CHECK_LOGIN_OR_PASS_ERR);
            } else {
                /*  Диалог о том что идет проверка данных на сервере */
                if (dialogWaitReg == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT);
                    builder.setCustomTitle(new ProgressBar(CONTEXT));;
                    builder.setMessage(getString(R.string.validation_data));
                    builder.setCancelable(false);
                    dialogWaitReg = builder.create();
                }
                dialogWaitReg.show();
                new Thread(check).start();
            }
        }
    };
    Runnable check = new Runnable() {
        @Override
        public void run() {
            try {
                String url = Sender.URL.replace("game", "chars") + "?l=" + newLogin + "&p=" + newPass + "&valid=" + Sender.md5(newLogin + newPass + newLogin.length());
                int code = new URL(url).openConnection().getContentLength();
                if (code == 3) {        // OK
                    SharedPreferences.Editor editor = MainActivity.prefs.edit();
                    editor.putString("login", newLogin);
                    editor.putString("pass", newPass);
                    editor.commit();
                    MainActivity.login = newLogin;
                    MainActivity.pass = newPass;
                    send.sendEmptyMessage(CHECK_LOGIN_AND_PASS_OK);
                } else if (code == -1) {// NOT CONNECT
                    errLoginOrPass = "Can not connect to Internet";
                    send.sendEmptyMessage(CHECK_LOGIN_OR_PASS_ERR);
                } else {                // NAME EXISTS
                    errLoginOrPass = getString(R.string.name_exists);
                    send.sendEmptyMessage(CHECK_LOGIN_OR_PASS_ERR);
                }
            } catch (IOException e) {
                Log.e(tag, "err in check name", e);
                errLoginOrPass = e.getMessage().replace("eof-cms.h2m.ru", "annimon.com");// hide site url :)
                send.sendEmptyMessage(CHECK_LOGIN_OR_PASS_ERR);
            } catch (Exception e) {
                Log.e(tag, "err in check name", e);
                errLoginOrPass = e.getMessage();
                send.sendEmptyMessage(CHECK_LOGIN_OR_PASS_ERR);
            }
        }
    };
}
