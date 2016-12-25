package org.lance.app;

import org.lance.emptyproject.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

/**  
 * PopupUtils
 * @author ganchengkai
 *
 */
@SuppressLint("NewApi")
public class PopupUtils {
	private static final String TAG = "PopupUtils";

	public static final int UP = 0;
	public static final int BOTTOM = UP + 1;
	public static final int LEFT = UP + 2;
	public static final int RIGHT = UP + 3;

	static final int extraHeight = 30;
	private static int maxWidth = 200;// replace
	private static int padding = 50;// replace

	/** 居中显示 */
	public static void showInCenter(Context context, final View parent,
			int mesg, final int offsetX, final int offsetY, final int location) {
		if (context == null || ((Activity) context).isFinishing()) {
			return;
		}
		showInCenter(context, parent, mesg, offsetX, offsetY, location, null);
	}

	/** 控制居中显示的气泡提示框---up,bottom validate */
	public static void showInCenter(Context context, final View parent,
			int mesg, final int offsetX, final int offsetY, final int location,
			OnDismissListener dismissListener) {
		if (parent == null) {
			return;
		}
		if (parent.getVisibility() != View.VISIBLE) {
			return;
		}
		if (parent.getWidth() == 0 || parent.getHeight() == 0) {
			return;
		}

		DisplayMetrics dm = parent.getResources().getDisplayMetrics();
		final float density = dm.density;
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.popup_tips, null);
		final TextView tips = (TextView) view
				.findViewById(R.id.tv_popup);

		if (location == BOTTOM) {
			view.setBackgroundResource(R.drawable.pop_bottom_center);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.CENTER;
			tips.setGravity(Gravity.CENTER);
			tips.setLayoutParams(params);
		}
		Spanned spannedMsg = getSpannedMsg(context, mesg);
		tips.setText(spannedMsg);
		final int lines = calTextLines(spannedMsg.toString(), tips,maxWidth);

		final int textWidth = calTextWidth(spannedMsg.toString(), tips,maxWidth);
		final float charHeight = calCharHeight(spannedMsg.toString(), tips);
		final int textHeight = calTextHeight(lines, charHeight, 0);
		final int popupWidth = textWidth + (int) (16 * density / 1.5f)
				+ padding * 2;
		final int popupHeight = textHeight + (int) (48 * density / 1.5f)
				+ padding * 2;
		final PopupWindow mPWindow = createPopup(view, popupWidth, popupHeight,
				dismissListener);
		int[] results = calOffestXY(parent, location, popupWidth, popupHeight,
				offsetX, offsetY);
		mPWindow.showAtLocation(parent, Gravity.NO_GRAVITY, results[0],
				results[1]);
		parent.postDelayed(new Runnable() {

			@Override
			public void run() {
				int tmpLines = tips.getLineCount();
				if (lines != tmpLines) {
					mPWindow.dismiss();
					int textHeight = calTextHeight(tmpLines, charHeight, 0);
					final int popWidth = textWidth
							+ (int) (16 * density / 1.5f) + padding * 2;
					final int popHeight = textHeight
							+ (int) (48 * density / 1.5f) + padding * 2;
					int[] results = calOffestXY(parent, location, popWidth,
							popHeight, offsetX, offsetY);
					PopupWindow mPWindow = createPopup(view, popWidth,
							popHeight, null);
					mPWindow.showAtLocation(parent, Gravity.NO_GRAVITY,
							results[0], results[1]);
				}
			}
		}, 10);

	}

	public static void showInUp(Context context, final View parent,
			int mesg, final int offsetX, final int offsetY, final int location) {
		if (context == null || ((Activity) context).isFinishing()) {
			return;
		}
		showInUp(context, parent, mesg, offsetX, offsetY, location, null);
	}

	/** 向上侧边显示气泡提示-- left right validate */
	public static void showInUp(Context context, final View parent,
			int mesg, final int offsetX, final int offsetY, final int location,
			OnDismissListener dismissListener) {
		if (parent == null) {
			return;
		}
		if (parent.getVisibility() != View.VISIBLE) {
			return;
		}
		if (parent.getWidth() == 0 || parent.getHeight() == 0) {
			return;
		}

		DisplayMetrics dm = parent.getResources().getDisplayMetrics();
		final float density = dm.density;
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.popup_tips, null);
		view.setBackgroundResource(R.drawable.pop_up_right);
		final TextView tips = (TextView) view
				.findViewById(R.id.tv_popup);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		tips.setGravity(Gravity.CENTER);
		tips.setLayoutParams(params);

		if (location == LEFT) {
			view.setBackgroundResource(R.drawable.pop_up_left);
		} else if (location == RIGHT) {
			view.setBackgroundResource(R.drawable.pop_up_right);
		}
		Spanned spannedMsg = getSpannedMsg(context, mesg);
		if (spannedMsg == null) {
			return;
		}

		tips.setText(spannedMsg);
		final int lines = calTextLines(spannedMsg.toString(), tips,maxWidth);

		final int textWidth = calTextWidth(spannedMsg.toString(), tips,maxWidth);
		final float charHeight = calCharHeight(spannedMsg.toString(), tips);
		final int textHeight = calTextHeight(lines, charHeight, 0);
		final int popupWidth = textWidth + (int) (0 * density / 1.5f) + padding
				* 2;
		final int popupHeight = textHeight
				+ (int) (extraHeight * density / 1.5f) + padding * 2;
		final PopupWindow mPWindow = createPopup(view, popupWidth, popupHeight,
				dismissListener);

		int offsetx = (int) (popupWidth - 21 * density / 1.5f - parent
				.getWidth() / 2);
		if (location == LEFT) {
			offsetx = (int) (21 * density / 1.5f - parent.getWidth() / 2);
		}
		mPWindow.showAsDropDown(parent, -offsetx + offsetX, offsetY);

		parent.postDelayed(new Runnable() {

			@Override
			public void run() {
				int tmpLines = tips.getLineCount();
				if (lines != tmpLines) {
					mPWindow.dismiss();
					int textHeight = calTextHeight(tmpLines, charHeight, 0);
					final int popWidth = textWidth + (int) (0 * density / 1.5f)
							+ padding * 2;
					final int popHeight = textHeight
							+ (int) (extraHeight * density / 1.5f) + padding
							* 2;
					PopupWindow mPWindow = createPopup(view, popWidth,
							popHeight, null);
					int offsetx = (int) (popupWidth - 21 * density / 1.5f - parent
							.getWidth() / 2);
					mPWindow.showAsDropDown(parent, -offsetx + offsetX, offsetY);
				}
			}
		}, 10);

	}

	/** 向下显示气泡提示--- up,bottom validate */
	public static void showInBottom(Context context, final View parent,
			int mesg, final int offsetX, final int offsetY, final int location,
			OnDismissListener dismissListener) {
		if (parent == null) {
			return;
		}
		if (parent.getVisibility() != View.VISIBLE) {
			return;
		}
		if (parent.getWidth() == 0 || parent.getHeight() == 0) {
			return;
		}

		DisplayMetrics dm = parent.getResources().getDisplayMetrics();
		final float density = dm.density;
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.popup_tips, null);
		view.setBackgroundResource(R.drawable.pop_bottom_left);
		final TextView tips = (TextView) view
				.findViewById(R.id.tv_popup);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		tips.setGravity(Gravity.CENTER);
		tips.setLayoutParams(params);

		if (location == LEFT) {
			view.setBackgroundResource(R.drawable.pop_bottom_left);
		} else if (location == RIGHT) {
			view.setBackgroundResource(R.drawable.pop_bottom_right);
		}
		Spanned spannedMsg = getSpannedMsg(context, mesg);
		if (spannedMsg == null) {
			return;
		}

		tips.setText(spannedMsg);
		final int lines = calTextLines(spannedMsg.toString(), tips, maxWidth);

		final int textWidth = calTextWidth(spannedMsg.toString(), tips,
				maxWidth);
		final float charHeight = calCharHeight(spannedMsg.toString(), tips);
		final int textHeight = calTextHeight(lines, charHeight, 0);
		final int popupWidth = textWidth + (int) (0 * density / 1.5f) + padding
				* 2;
		final int popupHeight = textHeight
				+ (int) (extraHeight * density / 1.5f) + padding * 2;
		final PopupWindow mPWindow = createPopup(view, popupWidth, popupHeight,
				dismissListener);
		int offsetx = (int) (popupWidth - 21 * density / 1.5f - parent
				.getWidth() / 2);
		if (location == LEFT) {
			offsetx = (int) (21 * density / 1.5f);
		}
		mPWindow.showAsDropDown(parent, -offsetx + offsetX, -parent.getHeight()
				- popupHeight + offsetY);

		parent.postDelayed(new Runnable() {

			@Override
			public void run() {
				int tmpLines = tips.getLineCount();
				if (lines != tmpLines) {
					mPWindow.dismiss();
					int textHeight = calTextHeight(tmpLines, charHeight, 0);
					final int popWidth = textWidth + (int) (0 * density / 1.5f)
							+ padding * 2;
					final int popHeight = textHeight
							+ (int) (extraHeight * density / 1.5f) + padding
							* 2;
					PopupWindow mPWindow = createPopup(view, popWidth,
							popHeight, null);
					mPWindow.showAsDropDown(parent, offsetX,
							-parent.getHeight() - popupHeight + offsetY);
				}
			}
		}, 10);

	}

	/** init PopupWindow */
	private static PopupWindow createPopup(View view, int width, int height,
			OnDismissListener dismissListener) {
		PopupWindow popup = new PopupWindow(view, width, height);
		popup.setAnimationStyle(R.style.popup_animation);
		popup.setFocusable(true);
		popup.setTouchable(false);
		popup.setOutsideTouchable(true);
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.update();
		popup.setOnDismissListener(dismissListener);
		return popup;
	}

	/** 计算锚点的偏移 */
	private static int[] calOffestXY(View parent, int location, int popupWidth,
			int popupHeight, int offestX, int offestY) {
		int[] results = new int[2];
		int height = parent.getHeight();
		int width = parent.getWidth();
		int[] locs = new int[2];
		parent.getLocationOnScreen(locs);
		int lx = locs[0];
		int ly = locs[1];
		int x = 0;
		int y = 0;
		switch (location) {
		case UP:
			x = lx - (popupWidth / 2 - width / 2) + offestX;
			y = ly - popupHeight + offestY;
			break;
		case BOTTOM:
			x = lx - (popupWidth / 2 - width / 2) + offestX;
			y = ly + height + offestY;
			break;
		case LEFT:
			x = (lx - 2 * width - offestX) < 0 ? 0 : (lx - 2 * width - offestX);
			y = ly - offestY;
			break;
		case RIGHT:
			x = lx + width + offestX;
			y = ly - offestY;
			break;
		}
		results[0] = x;
		results[1] = y;
		return results;
	}

	/** 获取string.xml里面含有html标签的文本 */
	private static Spanned getSpannedMsg(Context context, int source) {
		Spanned html = null;
		try {
			String strxml = context.getResources().getString(source);
			html = Html.fromHtml(strxml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return html;
	}

	/** 计算文本的所应占用的行数---在控件显示之前计算 */
	private static int calTextLines(String source, TextView textView,
			int maxWidth) {
		TextPaint textPaint = textView.getPaint();
		float measureTextWidth = (int) textPaint.measureText(source);
		int lines = (int) Math.ceil(measureTextWidth / maxWidth);
		return lines;
	}

	/** 计算文本占用的宽度 */
	private static int calTextWidth(String source, TextView textView,
			int maxWidth) {
		TextPaint textPaint = textView.getPaint();
		float measureTextWidth = (int) textPaint.measureText(source);
		int ceilWidth = (int) Math.ceil(measureTextWidth);
		int resultWidth = ceilWidth < maxWidth ? ceilWidth : maxWidth;
		return resultWidth;
	}

	/** 计算文本占用的高度 */
	private static int calTextHeight(int lines, float charHeight,
			float lineSpace) {
		return (int) Math.ceil(lines * charHeight + (lines - 1) * lineSpace);
	}

	/** 计算文本字符的高度 */
	private static float calCharHeight(String source, TextView textView) {
		TextPaint textPaint = textView.getPaint();
		textPaint.setTextSize(textView.getTextSize());
		FontMetrics fm = textPaint.getFontMetrics();
		float charHeight = (float) Math.ceil(fm.descent - fm.ascent);
		return charHeight;
	}

}
