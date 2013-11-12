/*
 * Game create for competition on http://annimon.com
 */
package ua.naiksoftware.chars.games;

import android.app.*;
import android.content.*;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.widget.*;
import java.util.ArrayList;
import java.util.Random;
import ua.naiksoftware.chars.*;

/**
 *
 * @author Naik
 */
public class SnakeSingleActivity extends EngineActivity {

    private final static String tag = "SnakeSingleActivity";
    /* голова для первой змеи */
    public static final char HEAD_LEFT = 'a';
    public static final char HEAD_UP = 'b';
    public static final char HEAD_RIGHT = 'c';
    public static final char HEAD_DOWN = 'd';
    /* голова для второй змеи */
    public static final char HEAD_LEFT_2 = 'e';
    public static final char HEAD_UP_2 = 'f';
    public static final char HEAD_RIGHT_2 = 'g';
    public static final char HEAD_DOWN_2 = 'h';
    /* элементы змеи 1 и 2 и бонусов и блоков */
    public static final char SNAKE_1 = 'm';
    public static final char SNAKE_2 = 'n';
    public static final char SLOW = 'u';    // замедление
    public static final char SURPRISE = 'w';// удвоение очков
    public static final char EAT = 'x';
    public static final char BLOCK = 'z';
    private static final byte HIDE_BONUS = 1;// время прошло, скрыть бонус
    private static final byte NOTIFY = 2; // послать уведомление 
    private static Random rnd = new Random();
    private Context context;
    private int inRow;
    private int inColumn;
    private char[][] gameArea;
    private ArrayList<Elem> listSnake = new ArrayList<Elem>();
    private Elem prevFirstElem;// предидущий элемент, нужен для расчета
    private Elem surprise, slow;
    /* начальные координаты змеи */
    private int beginX = 2;
    private int beginY = 1;
    private int endX = 1;
    private int endY = 1;
    /* направление */
    private int directX = 1;
    private int directY = 0;
    private int doubleClick = 0;// было ли два клика подряд
    private int posEndSnakeElem;// последний елемент змеи
    private int score, record;
    private int sleep = 500;    // начальная пауза (скорость)
    private static final byte DIFF = 5;// ускорение при сьедании еды
    private boolean twoClick;
    private boolean isMultiplayer = false;
    private boolean blockButtons = true;// блокировка обработки нажатий
    private boolean showBonus = false;
    private String message = "";

    /**
     * Вызывается лишь один раз при запуске игры
     *
     * @param inRow число блоков в ряде
     * @param inColumn число блоков в столбце
     * @param gameArea игровое поле
     */
    @Override
    protected void gameStart(int inRow, int inColumn, char[][] gameArea) {
        //Log.d(tag, "startGame started with: inRow=" + inRow + ", inColumn=" + inColumn);
        context = getApplicationContext();
        this.gameArea = gameArea;
        this.inRow = inRow;
        this.inColumn = inColumn;
        prevFirstElem = new Elem(HEAD_RIGHT, beginX, beginY);
        listSnake.add(0, prevFirstElem);
        listSnake.add(1, new Elem(SNAKE_1, endX, endY));
        drawRect();// "нарисовать" на игровом поле рамку из блоков
        putSnakeToArr();
        genElem(EAT);
        posEndSnakeElem = 1;
        twoClick = false;
        SharedPreferences prefs = getSharedPreferences(tag, Activity.MODE_PRIVATE);
        record = prefs.getInt("record", 0);
        score = 0;
    }

    @Override
    protected void gameLoop() {
        //Log.d(tag, "gameLoop started");
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException localInterruptedException1) {
            //Log.d(tag, "InterruptedException in gameLoop");
            return;
        }
        if (doubleClick == 0) {  // если двойного нажатия небыло
            blockButtons = false;// то начинаем обрабатывать нажатия
        } else {                 // иначе
            twoClick = true;     // после обработки первого нажатия в конце метода обработаем второе
        }
        /* select a head */
        char head = SNAKE_1;
        if (directX == 1) {
            head = HEAD_RIGHT;
        } else if (directX == -1) {
            head = HEAD_LEFT;
        } else if (directY == 1) {
            head = HEAD_DOWN;
        } else if (directY == -1) {
            head = HEAD_UP;
        }
        beginX += directX;
        beginY += directY;
        char next = gameArea[beginY][beginX];
        if (next == BLOCK || next == SNAKE_1) {

            /* Вьехал сам в себя или за границы поля */
            if (score < 1 || score < record) {
                gameOver();
            } else {
                SharedPreferences prefs = getSharedPreferences(tag, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("record", score);
                editor.commit();
                sendRecord();
            }
            return;
        } else if (next == EAT) {

            /* Сьел еду */
            int newPos = posEndSnakeElem; // новая позиция головы в ArrayList
            if (posEndSnakeElem < (listSnake.size() - 1)) {
                newPos++;
            } else {
                newPos = 0;
                posEndSnakeElem++;
            }
            sleep = sleep - DIFF;
            sleep = sleep < 0 ? 10 : sleep;
            score++;
            setScore(score);
            listSnake.get(listSnake.indexOf(prevFirstElem)).setChar(SNAKE_1);
            prevFirstElem = new Elem(head, beginX, beginY);
            listSnake.add(newPos, prevFirstElem);
            genElem(EAT);
            message = "+1";
            handler.sendEmptyMessage(NOTIFY);
        } else {
            if (next == SURPRISE) {

                /* Сьел удвоение очков */
                surprise = null;
                score *= 1.1;// +10%
                setScore(score);
                handler.removeMessages(HIDE_BONUS);// уже не нужно убирать бонус, на его месте голова змеи
                showBonus = false;
                message = "+10%";
                handler.sendEmptyMessage(NOTIFY);
            } else if (next == SLOW) {

                /* Сьел замедление на 20 мс */
                slow = null;
                sleep += 20;
                handler.removeMessages(HIDE_BONUS);// уже не нужно убирать бонус, на его месте голова змеи
                showBonus = false;
                message = "Speed -20";
                handler.sendEmptyMessage(NOTIFY);
            }
            /* Продвижение змеи  */
            Elem getEnd = listSnake.get(posEndSnakeElem); // достаем последний элемент змеи
            gameArea[getEnd.getY()][getEnd.getX()] = FON; // на его месте ставим фон 
            getEnd.y = beginY;                            // задаем концу координаты головы
            getEnd.x = beginX;
            getEnd.setChar(head);                         // устанавливаем голову
            listSnake.get(listSnake.indexOf(prevFirstElem)).setChar(SNAKE_1);// предыдущее место головы затираем элементом змеи
            prevFirstElem = getEnd;// теперь конец стал началом и уже устарел, т.е. в след. итерации будет сразу за головой
            /* Изменяем индекс последнего элемента в ArrayList вследствие движения */
            if (posEndSnakeElem > 0) {
                posEndSnakeElem--;
            } else {
                posEndSnakeElem = listSnake.size() - 1;
            }

            /* Генерация бонусов */
            if (!showBonus) {
                switch (rnd.nextInt(10)) {
                    case 3:
                        surprise = genElem(SURPRISE);
                        handler.sendEmptyMessageDelayed(HIDE_BONUS, rnd.nextInt(6000) + 1000);
                        showBonus = true;
                        break;
                    case 6:
                        slow = genElem(SLOW);
                        handler.sendEmptyMessageDelayed(HIDE_BONUS, rnd.nextInt(6000) + 1000);
                        showBonus = true;
                        break;
                }
            }
        }
        putSnakeToArr();
        if (twoClick) {             // обработка второго (пропущеного при отрисовке нажатия
            twoClick = false;
            blockButtons = false;
            gameAction(doubleClick);// эмулируем упущеное нажатие
        }
    }

    private Elem genElem(char elem) {
		int eatPosX, eatPosY;
		do {
			eatPosX = rnd.nextInt(inRow);
			eatPosY = rnd.nextInt(inColumn);
        } while (gameArea[eatPosY][eatPosX] != FON);
        gameArea[eatPosY][eatPosX] = elem;
        return new Elem(elem, eatPosX, eatPosY);
    }

    public void putSnakeToArr() {
        for (int i = 0; i < listSnake.size(); i++) {
            Elem localSnakeElem = listSnake.get(i);
            gameArea[localSnakeElem.getY()][localSnakeElem.getX()] = localSnakeElem.getChar();
        }
    }

    @Override
    protected void gameAction(int action) {
        //Log.d(tag, "gameAction in game started");
        if (blockButtons) {
            doubleClick = action;
            return;
        } else {
            blockButtons = true;
            doubleClick = 0;
        }
        switch (action) {
            case EngineActivity.ACTION_LEFT_1:
                if (directY < 0) {
                    directY = 0;
                    directX = -1;
                } else if (directX < 0) {
                    directX = 0;
                    directY = -1;
                } else if (directY > 0) {
                    directY = 0;
                    directX = -1;
                } else if (directX > 0) {
                    directX = 0;
                    directY = -1;
                }
                break;
            case EngineActivity.ACTION_RIGHT_1:
                if (directY < 0) {
                    directY = 0;
                    directX = 1;
                } else if (directX > 0) {
                    directX = 0;
                    directY = 1;
                } else if (directY > 0) {
                    directY = 0;
                    directX = 1;
                } else if (directX < 0) {
                    directX = 0;
                    directY = 1;
                }
                break;
        }
    }

    private void drawRect() {
        for (int i = 0; i < inColumn; i++) {
            gameArea[i][0] = BLOCK;
            gameArea[i][inRow - 1] = BLOCK;
        }
        for (int i = 1; i < (inRow - 1); i++) {
            gameArea[0][i] = BLOCK;
            gameArea[inColumn - 1][i] = BLOCK;
        }
    }

    @Override
    protected void gamePause() {
        //Log.d(tag, "gamePause");
        if (showBonus) {
            handler.removeMessages(HIDE_BONUS);
        }
    }

    @Override
    protected void gameResume(int state) {
        //Log.d(tag, "gameResume with state=" + state);
        if (state != STATE_NOT_INIT && showBonus) {
            handler.sendEmptyMessageDelayed(HIDE_BONUS, rnd.nextInt(1000) + 1000);
        }
    }

    @Override
    protected boolean isGameMultiplayer() {
        return isMultiplayer;
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_BONUS:
                    if (surprise != null) {
                        gameArea[surprise.getY()][surprise.getX()] = FON;
                        surprise = null;
                    }
                    if (slow != null) {
                        gameArea[slow.getY()][slow.getX()] = FON;
                        slow = null;
                    }
                    showBonus = false;
                    break;
                case NOTIFY:
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
            }
        }
    };
}
