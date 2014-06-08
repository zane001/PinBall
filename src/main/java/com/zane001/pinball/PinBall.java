package com.zane001.pinball;

/**
 * @author zane001
 * @update 2014-06-06 14:21
 */

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class PinBall extends Activity {
    private int tableWidth;
    private int tableHeight;
    private int racketY;
    private final int RACKET_HEIGHT = 20;
    private final int RACKET_WIDTH = 150;
    private final int BALL_SIZE = 12;
    private int ySpeed = 50;
    Random rand = new Random();
    private double xyRate = rand.nextDouble() - 0.5;
    private int xSpeed = (int) (ySpeed * xyRate * 2);
    private int ballX = rand.nextInt(200) + 20;
    private int ballY = rand.nextInt(10) + 20;
    private int racketX = rand.nextInt(200);
    private boolean isLose = false;
    private int score = 2048;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final GameView gameView = new GameView(this);
        setContentView(gameView);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        tableWidth = metrics.widthPixels;
        tableHeight = metrics.heightPixels;
        racketY = tableHeight - 80;

        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123 || msg.what == 0x001) {
                    gameView.invalidate();
                }
            }
        };

//      gameView.setOnKeyListener(new OnKeyListener() {
//
//		  @Override public boolean onKey(View source, int keyCode, KeyEvent
//		  event) { switch (event.getKeyCode()) { case KeyEvent.KEYCODE_A: if
//		  (racketX > 0) racketX -= 10; break; case KeyEvent.KEYCODE_D: if
//		  (racketX < tableWidth - RACKET_WIDTH) racketX += 10; break; }
//		  gameView.invalidate(); return true; } });

        gameView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                racketX = (int) event.getX() - 75;
                // racketY = (int) event.getY();
                gameView.invalidate();
                return true;
            }
        });

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (ballX <= 0 || ballX >= tableWidth - BALL_SIZE) {
                    xSpeed = -xSpeed; // 撞击左右屏幕的墙壁得分
                    score--;
                    if (score == 0) {    //如果从2048减为0，同样结束游戏
                        isLose = true;
                    }
                }
                if (ballY >= racketY - BALL_SIZE // 小球落空，没被板接到
                        && (ballX < racketX || ballX > racketX + RACKET_WIDTH)) {
                    timer.cancel();
                    isLose = true;
                } else if (ballY >= racketY - BALL_SIZE && ballX > racketX // 小球落在小板上
                        && ballX <= racketX + RACKET_WIDTH) {
                    ySpeed = -ySpeed;
                } else if (ballY < 0) {
                    ySpeed = -ySpeed; // 撞击上屏幕的墙壁得分
                    score--;
                    if (score == 0) {    //如果从2048减为0，同样结束游戏
                        isLose = true;
                    }
                }
                ballY += ySpeed;
                ballX += xSpeed;
                handler.sendEmptyMessage(0x123);
            }
        }, 0, 100);
    }

    class GameView extends View {
        Paint paint = new Paint();

//        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
//        TextView tv_score = (TextView) findViewById(R.id.score);
//        TextView tv_score = new TextView(this);
//        tv_score.setText("您的得分是：" + score);
//        this.add(ll);

        public GameView(Context context) {
            super(context);
            setFocusable(true);
        }

        public void onDraw(final Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);

            if (isLose) {
                paint.setColor(Color.RED);
                paint.setTextSize(30);
                canvas.drawText("哇哦，游戏结束 ~_~", tableWidth / 4, tableHeight / 4, paint);
                canvas.drawText("点击屏幕继续", tableWidth / 4, tableHeight / 2, paint);
                /*setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        isLose = false;
                        handler.sendEmptyMessage(0x001);
                        //invalidate();
                        return true;
                    }
                });*/
            } else {
                RectF rectF = new RectF(0, 0, tableWidth, tableHeight);
                Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
//                canvas.drawColor(Color.rgb(0,255,127));
                canvas.drawBitmap(background, null, rectF, null);  //添加背景图片
                paint.setColor(Color.rgb(240, 240, 80));
                canvas.drawCircle(ballX, ballY, BALL_SIZE, paint);  //画弹球
                paint.setColor(Color.rgb(80, 80, 200));
                canvas.drawRect(racketX, racketY, racketX + RACKET_WIDTH,   //画小板
                        racketY + RACKET_HEIGHT, paint);
                paint.setTextSize(25);
                paint.setColor(Color.rgb(255, 0, 0)); //显示得分
                canvas.drawText("您的得分是：" + score, tableWidth - 250, 20, paint);
            }
        }
    }
}