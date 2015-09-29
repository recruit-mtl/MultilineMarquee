package jp.co.recruit.mtl.osharetenki.multilinemarqueelibrary;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * display text view.
 * - automatically start marquee if this view is on screen.
 * - display text of two lines if view's height is enough.
 */
public class MultiLineMarqueeTextView extends View {

	private static final int DEFAULT_TEXT_SIZE_DIPS = 14;
	private static final int INTERVAL_SECOND_TEXT_DIPS = 80;
	private static final long DURATION_TRANSLATE = 3000;
	private static final long DURATION_SUSPEND = 2000;

	private static final String RETURN_CODE = "\n";

	private Paint paint = new Paint();
	private Rect clipping = new Rect();
	private ValueAnimator translateAnimator;
	private ValueAnimator suspendAnimator;
	private String originalText;
	private String[] splitText;

	private int displayWidth;
	private int displayHeight;
	private float textWidth;
	private float textHeight;
	private float adjustTextBaseline;
	private int intervalSecondText;
	private int positionSecondText;

	private boolean needMarquee = false;
	private boolean stoppedMarquee = false;
	private ObserverThread observer;

	private boolean validTapReset = false;

	private Handler handler = new Handler();

	public MultiLineMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// get display size and density.
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		displayWidth = point.x;
		displayHeight = point.y;
		final float density = getResources().getDisplayMetrics().density;

		// load XML settings
		parseXmlSettings(context, attrs, density);

		paint.setAntiAlias(true);

		adjustTextBaseline = paint.descent() + paint.ascent();
		textHeight = Math.abs(paint.getFontMetrics().top) + paint.getFontMetrics().bottom;
		intervalSecondText = (int)(INTERVAL_SECOND_TEXT_DIPS * density);

		LinearInterpolator linearInterpolator = new LinearInterpolator();
		translateAnimator = ValueAnimator.ofFloat(0.0f, -1.0f);
		translateAnimator.setInterpolator(linearInterpolator);
		translateAnimator.removeAllUpdateListeners();
		translateAnimator.addUpdateListener(animatorUpdateListener);
		translateAnimator.removeAllListeners();
		translateAnimator.addListener(animatorListener);
		translateAnimator.setDuration(DURATION_TRANSLATE);

		suspendAnimator = ValueAnimator.ofFloat(0.0f, 0.0f);
		suspendAnimator.setInterpolator(linearInterpolator);
		suspendAnimator.removeAllUpdateListeners();
		suspendAnimator.addUpdateListener(animatorUpdateListener);
		suspendAnimator.removeAllListeners();
		suspendAnimator.addListener(animatorListener);
		suspendAnimator.setDuration(DURATION_SUSPEND);

		changeTapReset();
	}

	public MultiLineMarqueeTextView setText(CharSequence s) {
		if (null != s && 0 < s.length()) {
			originalText = s.toString();
			createDisplayText();
			if (0 < getMeasuredWidth() && 0 < getMeasuredWidth())
				invalidate();
		}
		return this;
	}

	public MultiLineMarqueeTextView setText(int resId) {
		if (0 < resId) {
			originalText = getContext().getString(resId);
			createDisplayText();
			if (0 < getMeasuredWidth() && 0 < getMeasuredWidth())
				invalidate();
		}
		return this;
	}

	public MultiLineMarqueeTextView setTextSize(float textSize) {
		paint.setTextSize(textSize);
		adjustTextBaseline = paint.descent() + paint.ascent();
		textHeight = Math.abs(paint.getFontMetrics().top) + paint.getFontMetrics().bottom;
		return this;
	}

	public MultiLineMarqueeTextView setTextColor(int textColor) {
		paint.setColor(textColor);
		return this;
	}

	public void setTapReset(boolean isValid) {
		validTapReset = isValid;
		changeTapReset();
	}

	public void startMarquee() {
		stoppedMarquee = false;
		if (!translateAnimator.isRunning() && !suspendAnimator.isRunning()) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					suspendAnimator.start();
				}
			});
		}
	}

	public void stopMarquee() {
		if (!stoppedMarquee)
			resetMarquee();
		if (null != observer) {
			observer.finish();
			observer = null;
		}
	}

	public void autoMarquee() {
		if (null == observer) {
			observer = new ObserverThread();
			observer.start();
		} else {
			observer.interrupt();
		}
	}

	private void resetMarquee() {
		stoppedMarquee = true;
		handler.post(new Runnable() {
			@Override
			public void run() {
				suspendAnimator.cancel();
				suspendAnimator.end();
				translateAnimator.cancel();
				translateAnimator.end();
				invalidate();
			}
		});
	}

	private void checkMarquee() {
		// start marquee if needed and this view is on screen. otherwise, stop marquee.
		if (needMarquee) {
			// start if a part of this view is on screen.
			if (hasWindowFocus() && isOnScreen()) {
				startMarquee();
			} else if (!stoppedMarquee) {
				resetMarquee();
			}
		}
	}

	private void changeTapReset() {
		if (validTapReset) {
			setOnClickListener(onClickListener);
		} else {
			setOnClickListener(null);
		}
	}


	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			clipping = new Rect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
			createDisplayText();
		}
		autoMarquee();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus && isOnScreen() && isEnabled())
			autoMarquee();
		else
			stopMarquee();
	}

	@Override
	public void onDetachedFromWindow() {
		stopMarquee();
		setText("");
		super.onDetachedFromWindow();
	}

	@Override
	public void onDraw(Canvas canvas) {
		ValueAnimator animator = translateAnimator.isRunning() ? translateAnimator : suspendAnimator;
		int xPos = (int) (positionSecondText * (Float) animator.getAnimatedValue()) + getPaddingLeft();
		int yPos = (int) ((canvas.getHeight() / 2) - (adjustTextBaseline / 2));

		canvas.save();
		canvas.clipRect(clipping);
		if (null != splitText && 0 < splitText.length) {
			Integer[] offsets = createVerticalOffsetsFromBaseline(splitText.length);
			for (int i = 0; i < splitText.length; ++i) {
				canvas.drawText(splitText[i], xPos, yPos + offsets[i], paint);
				canvas.drawText(splitText[i], xPos + positionSecondText, yPos + offsets[i], paint);
			}
		}
		canvas.restore();
	}




	private void parseXmlSettings(Context context, AttributeSet attrs, float density) {
		// parse "android:text"
		String textValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
		if (null == textValue) {
			originalText = "";
		} else {
			if (textValue.startsWith("@")) {
				originalText = context.getString(Integer.valueOf(textValue.replace("@", "")));
			} else {
				originalText = textValue;
			}
		}

		// parse "android:textSize"
		String textSizeValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textSize");
		float defaultTextSize = DEFAULT_TEXT_SIZE_DIPS * density;
		if (null == textSizeValue) {
			setTextSize(defaultTextSize);
		} else {
			if (textSizeValue.endsWith("dip")) {
				setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Float.valueOf(textSizeValue.replace("dip", "")), getResources().getDisplayMetrics()));
			} else if (textSizeValue.endsWith("sp")) {
				setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Float.valueOf(textSizeValue.replace("sp", "")), getResources().getDisplayMetrics()));
			}
		}

		// parse "android:textColor"
		String textColorValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textColor");
		if (null != textColorValue) {
			if (textColorValue.startsWith("@")) {
				setTextColor(context.getResources().getColor(Integer.valueOf(textColorValue.replace("@", ""))));
			} else if (textColorValue.startsWith("#")) {
				setTextColor(Color.parseColor(textColorValue));
			}
		}
	}

	private void createDisplayText() {
		if (translateAnimator.isRunning() || suspendAnimator.isRunning()) {
			resetMarquee();
		}
		// if not measured yet, skip without creating text.
		if (0 == getMeasuredWidth() || 0 == getMeasuredHeight()) {
			return;
		}
		int allowedLineCount = (int)((getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / textHeight);
		if (1 < allowedLineCount) {
			// display text of two lines if view's height is enough.
			String[] temp = originalText.split(RETURN_CODE);
			int lineCount = temp.length;
			if (lineCount > allowedLineCount) {
				splitText = adjustLineText(temp, allowedLineCount);
			} else {
				splitText = temp;
			}
		} else {
			// display text on one line.
			splitText = originalText.replaceAll(RETURN_CODE, "").split(RETURN_CODE);
		}
		textWidth = 0.0f;
		for (String message : splitText) {
			float width = paint.measureText(message);
			if (width > textWidth)
				textWidth = width;
		}
		needMarquee = textWidth > (getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
		if ((textWidth + intervalSecondText) > getMeasuredWidth()) {
			positionSecondText = (int) (textWidth + intervalSecondText);
			// change translate animation duration along the length of text.
			translateAnimator.setDuration(DURATION_TRANSLATE * positionSecondText / getMeasuredWidth());
		} else {
			positionSecondText = getMeasuredWidth();
			// change translate animation duration into default.
			translateAnimator.setDuration(DURATION_TRANSLATE);
		}
	}

	private String[] adjustLineText(String[] text, int adjustLineCount) {
		if (text.length <= adjustLineCount)
			return text;
		float minLength = 0.0f;
		int minIndex = 0;
		// check shortest width which lines when combined any two lines.
		for (int i = 0; i < text.length - 1; ++i) {
			float length = paint.measureText(text[i]) + paint.measureText(" ") + paint.measureText(text[i + 1]);
			if (0.0f == minLength || length < minLength) {
				minLength = length;
				minIndex = i;
			}
		}
		ArrayList<String> combinedText = new ArrayList<String>();
		for (int i = 0; i < text.length; ++i) {
			if (i == minIndex) {
				// shortest width, combine next line.
				combinedText.add(text[i]+" "+text[i + 1]);
			} else if (i == minIndex + 1) {
				// combined line, do nothing.
				continue;
			} else {
				// ordinary
				combinedText.add(text[i]);
			}
		}
		return adjustLineText(combinedText.toArray(new String[0]), adjustLineCount);
	}

	private Integer[] createVerticalOffsetsFromBaseline(int lineCount) {
		ArrayList<Integer> offsets = new ArrayList<Integer>();

		int coefficient = lineCount - 1;
		for (int i = 0; i < lineCount; ++i, coefficient-=2) {
			offsets.add(new Integer((int) (adjustTextBaseline * coefficient)));
		}

		return offsets.toArray(new Integer[0]);
	}

	private boolean isOnScreen() {
		int[] location = new int[2];
		getLocationOnScreen(location);

		if (-getMeasuredWidth() <= location[0] && location[0] < displayWidth && -getMeasuredHeight() <= location[1] && location[1] < displayHeight) {
			return true;
		} else {
			return false;
		}
	}



	private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (stoppedMarquee || !isEnabled())
				return;
			// run alternately.
			if (animation.equals(translateAnimator)) {
				// start suspend when finished translation.
				suspendAnimator.start();
			}
			if (animation.equals(suspendAnimator)) {
				// start translate animation when finished suspension.
				translateAnimator.start();
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}
	};

	private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			invalidate();
		}
	};

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetMarquee();
		}
	};

	private class ObserverThread extends Thread {
		private boolean requestedFinish = false;

		@Override
		public void run() {
			while (true) {
				if (requestedFinish)
					break;
				checkMarquee();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}

		public void finish() {
			requestedFinish = true;
		}
	}
}
