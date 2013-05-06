package net.noboruhi.l2witter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LedView extends SurfaceView
    implements SurfaceHolder.Callback,Runnable {

    private SurfaceHolder holder;
    private Thread        thread;

    private Bitmap ledBitmap;
    private Canvas ledCanvas;
    private Paint  dotPaint;
    private String nextText   = "";
    private int viewSize = 0;
    private TextProducer textProducer = null;

    public void setTextProducer(TextProducer textProducer) {
        this.textProducer = textProducer;
    }

    public LedView(Context context) {
        super(context);
        initView();
    }

    public LedView(Context context,AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LedView(Context context,AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        holder = getHolder();
        holder.addCallback(this);
        holder.setFixedSize(getWidth(),getHeight());
        dotPaint = new Paint();
        dotPaint.setAntiAlias(false);
        dotPaint.setColor(Color.BLACK);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setTextSize(Const.LED_NUM);
    }

    public void setNextText(String nextText) {
        this.nextText = nextText;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);
        thread.start();
    }

    public void surfaceChanged(SurfaceHolder holder,int format,int w,int h) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }

    //スレッドの処理
    public void run() {
        Canvas canvas;
        Paint ledPaint = new Paint();
        ledPaint.setAntiAlias(true);
        ledPaint.setStyle(Paint.Style.FILL);
        ledPaint.setColor(Color.rgb(255, 128, 0));

        int offset = 0;
        String writeText  = ""; //"L2Witter started...";
        //writeLedText(writeText);
        while(thread != null) {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);
                int ledSize = viewSize / Const.LED_NUM ;

                offset++;
                if (offset > ledCanvas.getWidth() - Const.LED_NUM) {
                    offset = 0;
                    writeText  = ""; //"L2Witter started...";
                    nextText = textProducer.popString();
                    if (!"".equals(nextText)) {
                        writeText = nextText;
                    }
                    writeLedText(writeText);
                }
                
                drawChar(canvas,ledPaint,ledSize,offset);
            }
            holder.unlockCanvasAndPost(canvas);

            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 文字を描画する
     * @param canvas 描画先のcanvas
     * @param ledPaint led
     * @param ledSize ledのサイズ
     * @param offset オフセット
     */
    private void drawChar(Canvas canvas,Paint ledPaint,int ledSize,int offset) {
        // for landscape
        int screenDotNumX = getMeasuredWidth() / ledSize;
        for (int i = 0;i < screenDotNumX;i++) {
            for (int j = 0;j < Const.LED_NUM;j++) {
                if (ledBitmap.getWidth() > i + offset) {
                    if (ledBitmap.getPixel(i + offset, j) == dotPaint.getColor()) {
                        canvas.drawCircle(ledSize * i + ledSize / 2,
                                ledSize  * j + ledSize / 2,
                                ledSize /2 , ledPaint);
                        /*
                        canvas.drawRect(ledSize * i ,
                                ledSize  * j ,
                                ledSize * (i + 1) , ledSize * (j +1), ledPaint);
                        */
                    }
                }
            }
        }
        
    }
    
    /*
     * スクロールさせる文字列をLEDバッファに描いておく
     */
    private void writeLedText(String writeText) {
        int bitmapLength = (int)dotPaint.measureText(writeText);
        ledBitmap = Bitmap.createBitmap(bitmapLength + Const.LED_NUM * 2, Const.LED_NUM, Bitmap.Config.RGB_565);
        ledCanvas = new Canvas(ledBitmap);
        FontMetrics metrics = dotPaint.getFontMetrics();
        ledCanvas.drawColor(Color.WHITE);
        ledCanvas.drawText(writeText, Const.LED_NUM , - metrics.descent / 2 - metrics.ascent  , dotPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width > height) {
            viewSize = height;
        } else {
            viewSize = width;
        }
        setMeasuredDimension(width, height);
    }

}
