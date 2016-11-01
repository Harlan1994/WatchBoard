package com.harlan.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.harlan.exception.NoDetermineSizeException;
import com.harlan.watchboard.R;

import java.util.Calendar;

import static com.harlan.utils.ScreenUtil.dp2px;
import static com.harlan.utils.ScreenUtil.sp2px;

/**
 * Created by Harlan1994 on 2016/11/1.
 */

public class WatchBoard extends View {

    private float mPadding; //表盘边距
    private float mRadius; //外圆半径
    private float mTextSize; //字体大小
    private float mHourPointerWidth; // 时针宽度
    private float mMinutePointerWidth; //分针宽度
    private float mSecondPointerWitdh;//秒针宽度
    private float mPointerEndLength;//指针末尾的长度

    private int mPointerRadius; //指针圆角
    private int mLongColor; //长线颜色
    private int mShortColor; //断线颜色
    private int mHourPointerColor; //时针颜色
    private int mMinutePointerColor; //分针颜色
    private int mSecondPointerColor;//秒针颜色

    private Paint mPaint; //画笔

    public WatchBoard(Context context) {
        this(context, null);
    }

    public WatchBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainAttrs(context, attrs);
        initPaint();
    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.WatchBoard);
            mPadding = typedArray.getDimension(R.styleable.WatchBoard_wb_padding, dp2px(context, 10));
            mTextSize = typedArray.getDimension(R.styleable.WatchBoard_wb_text_size, sp2px(context, 16));
            mHourPointerWidth = typedArray.getDimension(R.styleable.WatchBoard_wb_hour_pointer_width, dp2px(context, 5));
            mMinutePointerWidth = typedArray.getDimension(R.styleable.WatchBoard_wb_minute_pointer_width, dp2px(context, 3));
            mSecondPointerWitdh = typedArray.getDimension(R.styleable.WatchBoard_wb_second_pointer_width, dp2px(context, 2));
            mPointerEndLength = typedArray.getDimension(R.styleable.WatchBoard_wb_pointer_end_length, dp2px(context, 10));
            mPointerRadius = (int) typedArray.getDimension(R.styleable.WatchBoard_wb_pointer_corner_radius, dp2px(context, 10));
            mLongColor = typedArray.getColor(R.styleable.WatchBoard_wb_scale_long_color, Color.argb(255, 0, 0, 0));
            mShortColor = typedArray.getColor(R.styleable.WatchBoard_wb_scale_short_color, Color.argb(125, 0, 0, 0));
            mHourPointerColor = typedArray.getColor(R.styleable.WatchBoard_wb_hour_pointer_color, Color.BLACK);
            mMinutePointerColor = typedArray.getColor(R.styleable.WatchBoard_wb_minute_pointer_color, Color.BLACK);
            mSecondPointerColor = typedArray.getColor(R.styleable.WatchBoard_wb_minute_pointer_color, Color.RED);
        } catch (Exception e) {
            mPadding = dp2px(context, 10);
            mTextSize = sp2px(context, 16);
            mHourPointerWidth = dp2px(context, 5);
            mMinutePointerWidth = dp2px(context, 3);
            mSecondPointerWitdh = dp2px(context, 2);
            mPointerEndLength = dp2px(context, 10);
            mHourPointerColor = Color.BLACK;
            mMinutePointerColor = Color.BLACK;
            mSecondPointerColor = Color.RED;
            mPointerRadius = dp2px(context, 10);
            mLongColor = Color.argb(255, 0, 0, 0);
            mShortColor = Color.argb(125, 0, 0, 0);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 1000;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        /**
         * 至少有一个为确定值,要获取其中的最小值
         */
        if (widthMode == MeasureSpec.AT_MOST
                || widthMode == MeasureSpec.UNSPECIFIED
                || heightMode == MeasureSpec.AT_MOST
                || heightMode == MeasureSpec.UNSPECIFIED) {
            try {
                throw new NoDetermineSizeException("At lease one of width and height should be defined exactly.");
            } catch (NoDetermineSizeException e) {
                e.printStackTrace();
            }
        } else {
            if (widthMode == MeasureSpec.EXACTLY) {
                width = Math.min(widthSize, width);
            }
            if (heightMode == MeasureSpec.EXACTLY) {
                width = Math.min(heightSize, width);
            }
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //表盘半径，需要减去整个View的padding值
        mRadius = (Math.min(w, h) - getPaddingLeft() - getPaddingRight()) / 2;

        //指针尾部的长度
        mPointerEndLength = mRadius / 6;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //将canvas坐标移至中间
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);

        /**
         * 绘制内容
         */
        drawBoard(canvas); //绘制表盘底盘
        drawScale(canvas); //绘制刻度
        drawPointer(canvas); //绘制指针
        canvas.restore();

        postInvalidateDelayed(1000);
    }

    /**
     * 绘制表盘底盘
     *
     * @param canvas
     */
    private void drawBoard(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);

        //已移至中间，故cx，cy都为0
        canvas.drawCircle(0, 0, mRadius, mPaint);
    }

    /**
     * 绘制指针
     *
     * @param canvas
     */
    private void drawPointer(Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        int mHourAngle = (hour % 12) * 360 / 12;
        int mMinuteAngle = minute * 360 / 60;
        int mSecondAngle = second * 360 / 60;

        //绘制时针
        canvas.save();
        canvas.rotate(mHourAngle);
        RectF mHourRectF = new RectF(-mHourPointerWidth / 2, -mRadius * 3 / 5, mHourPointerWidth / 2, mPointerEndLength);
        mPaint.setColor(mHourPointerColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mHourPointerWidth);
        canvas.drawRoundRect(mHourRectF, mPointerRadius, mPointerRadius, mPaint);
        canvas.restore();

        //绘制分针
        canvas.save();
        canvas.rotate(mMinuteAngle);
        RectF mMinuteRectF = new RectF(-mMinutePointerWidth / 2, -mRadius * 3.5f / 5, mMinutePointerWidth / 2, mPointerEndLength);
        mPaint.setColor(mMinutePointerColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mMinutePointerWidth);
        canvas.drawRoundRect(mMinuteRectF, mPointerRadius, mPointerRadius, mPaint);
        canvas.restore();

        //绘制秒针
        canvas.save();
        canvas.rotate(mSecondAngle);
        RectF mSecondRectF = new RectF(-mSecondPointerWitdh / 2, -mRadius + 15, mSecondPointerWitdh / 2, mPointerEndLength);
        mPaint.setColor(mSecondPointerColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mSecondPointerWitdh);
        canvas.drawRoundRect(mSecondRectF, mPointerRadius, mPointerRadius, mPaint);
        canvas.restore();

        //绘制中心小圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mSecondPointerColor);
        canvas.drawCircle(0, 0, mSecondPointerWitdh * 4, mPaint);
    }

    /**
     * 绘制刻度，包括刻度线和刻度数字
     *
     * @param canvas
     */
    private void drawScale(Canvas canvas) {
        //总共60个刻度，其中12个整点刻度，需要加粗加长
        //360 / 60 = 6', 所以每绘制一个刻度旋转6'
        int mLineWidth = 0;
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) { //整点刻度
                mPaint.setStrokeWidth(dp2px(getContext(), 1.5f));
                mPaint.setColor(mLongColor);
                mLineWidth = 40;

                //整点需要绘制刻度值
                mPaint.setTextSize(mTextSize);
                mPaint.setColor(Color.BLACK);
                mPaint.setStyle(Paint.Style.FILL);
                String text = ((i / 5) == 0 ? 12 : (i / 5)) + "";
                Rect textBounds = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBounds);
                int textHeight = textBounds.bottom - textBounds.top;
                canvas.save();
                canvas.translate(0, -mRadius + mLineWidth + dp2px(getContext(), 5) + mPadding + textHeight / 2);
                canvas.rotate(-6 * i);
                canvas.drawText(text, -(textBounds.right + textBounds.left) / 2, -(textBounds.bottom + textBounds.top) / 2, mPaint);
                canvas.restore();
            } else {
                mPaint.setStrokeWidth(dp2px(getContext(), 1f));
                mPaint.setColor(mShortColor);
                mLineWidth = 30;
            }

            //canvas中心点为整个view中心，所以上部应该为负数
            canvas.drawLine(0, -mRadius + dp2px(getContext(), 10), 0, -mRadius + mLineWidth + dp2px(getContext(), 10), mPaint);
            canvas.rotate(6);
        }
    }
}
