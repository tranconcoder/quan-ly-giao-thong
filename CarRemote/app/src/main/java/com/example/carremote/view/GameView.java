package com.example.carremote.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

class GameView extends View {

    private int mXPos, mYPos;
    protected int cCordX = 720, cCordY = 1400, cSize = 50, cDefSize = 50;
    //    protected int cCenterX = 700, cCenterY = 2500;
    protected int cCenterX , cCenterY ;
    private final int ocDiameter, icDiameter;
    private final Handler handler = new Handler();
    private final long delayMillis = 16; // Khoảng 60 FPS

    @SuppressLint("ClickableViewAccessibility")
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ocDiameter = 200; // Giá trị mặc định
        this.icDiameter = 80; // Giá trị mặc định

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        this.mXPos = this.cCenterX = displayMetrics.widthPixels / 2;
        this.mYPos = this.cCenterY = this.ocDiameter + this.icDiameter;

        init();
    }

    // Constructor tùy chỉnh như trước đây
    public GameView(Context context, int ocDiameter, int icDiameter) {
        super(context);
        this.ocDiameter = ocDiameter;
        this.icDiameter = icDiameter;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        this.cCenterX = displayMetrics.widthPixels / 2;
        this.cCenterY = this.ocDiameter + this.icDiameter;

        init();
    }

    // Phương thức khởi tạo chung
    private void init() {
        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    mXPos = (int) event.getX();
                    mYPos = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    mXPos = cCenterX;
                    mYPos = cCenterY;
                    break;
            }
            return true;
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(this, delayMillis);
            }
        }, delayMillis);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        Log.d("GameView", "onDraw called");

        // Vẽ vòng tròn lớn (joystick)
        paint.setColor(Color.BLUE);
        canvas.drawCircle(cCenterX, cCenterY, ocDiameter, paint);

        // Tính toán vị trí ngón tay và đối tượng joystick
        int jXPos = mXPos - cCenterX;
        int jYPos = mYPos - cCenterY;

        double dToMouse = Math.sqrt(Math.pow(cCenterX - mXPos, 2) + Math.pow(cCenterY - mYPos, 2));
        double ratio = ocDiameter / dToMouse;

        // Điều chỉnh vị trí của joystick sao cho không vượt quá vòng tròn ngoài
        if (dToMouse > ocDiameter) {
            jXPos = (int) ((mXPos - cCenterX) * ratio);
            jYPos = (int) ((mYPos - cCenterY) * ratio);
        }

        // Vẽ vòng tròn nhỏ (biểu thị vị trí ngón tay)
        paint.setColor(Color.MAGENTA);

        if (dToMouse > ocDiameter) {
            canvas.drawCircle((int) (cCenterX + (mXPos - cCenterX) * ratio),
                    (int) (cCenterY + (mYPos - cCenterY) * ratio),
                    icDiameter, paint);
        } else {
            canvas.drawCircle(mXPos, mYPos, icDiameter, paint);
        }

        // Tính toán hướng di chuyển
        String direction = getDirection(jXPos, jYPos);

        // Hiển thị hướng điều khiển
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Hướng: " + direction, 0, 50, paint);
    }

    // Phương thức để xác định hướng điều khiển
    private String getDirection(int jXPos, int jYPos) {
        String directionX = "";
        String directionY = "";

        // Xác định hướng ngang (trái/phải)
        if (Math.abs(jXPos) > 50) {
            if (jXPos > 0) {
                directionX = "Phải";
            } else {
                directionX = "Trái";
            }
        }

        // Xác định hướng dọc (lên/xuống)
        if (Math.abs(jYPos) > 50) {
            if (jYPos > 0) {
                directionY = "Dưới";
            } else {
                directionY = "Trên";
            }
        }

        // Kết hợp các hướng
        if (!directionX.isEmpty() && !directionY.isEmpty()) {
            return directionY + " - " + directionX;
        } else if (!directionX.isEmpty()) {
            return directionX;
        } else if (!directionY.isEmpty()) {
            return directionY;
        } else {
            return "Không di chuyển";
        }
    }
}
