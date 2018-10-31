package devtolife.upinbiburte;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copyright (C) 2017 VMarch
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class UpiRichEditor extends WebView {

    public enum Type {
        BOLD,
        ITALIC,
        UNDERLINE,
        ORDEREDLIST,
        UNORDEREDLIST
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    public interface OnDecorationStateListener {
        void onStateChangeListener(String text, List<Type> types);
    }

    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String STYLE_PART = "style-part://";
    private static final String TEXT_PART = "text-part://";

    private boolean isReady = false;
    private String mContents;
    private OnTextChangeListener mTextChangeListener;
    private OnDecorationStateListener mDecorationStateListener;
    private AfterInitialLoadListener mLoadListener;

    public UpiRichEditor(Context context) {
        this(context, null);
    }

    public UpiRichEditor(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public UpiRichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        getSettings().setJavaScriptEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(createWebviewClient());
        loadUrl(SETUP_HTML);

        applyAttributes(context, attrs);
    }

    protected EditorWebViewClient createWebviewClient() {
        return new EditorWebViewClient();
    }

    public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
        mLoadListener = listener;
    }

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }

    public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
        mDecorationStateListener = listener;
    }

    private void stateAndContentCheck(String data) {
        getContentPart(data);
        getStylesPart(data);
    }

    private void getContentPart(String primaryData) {
        mContents = primaryData.replaceFirst("(.*)" + TEXT_PART, "");

        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mContents);
        }

    }

    private void getStylesPart(String primaryData) {

        String preState = primaryData.replaceFirst(TEXT_PART + "(.*)", "");
        String state = preState.replaceFirst(STYLE_PART, "").toUpperCase(Locale.ENGLISH);
        List<Type> types = new ArrayList<>();
        for (Type type : Type.values()) {
            if (TextUtils.indexOf(state, type.name()) != -1) {
                types.add(type);
            }
        }

        if (mDecorationStateListener != null) {
            mDecorationStateListener.onStateChangeListener(state, types);
        }
    }


    private void applyAttributes(Context context, AttributeSet attrs) {
        final int[] attrsArray = new int[]{
                android.R.attr.gravity
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

        int gravity = ta.getInt(0, NO_ID);
        switch (gravity) {
            case Gravity.LEFT:
                exec("javascript:URE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:URE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:URE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:URE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:URE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:URE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER:
                exec("javascript:URE.setVerticalAlign(\"middle\")");
                exec("javascript:URE.setTextAlign(\"center\")");
                break;
        }

        ta.recycle();
    }

    public void setHtml(String contents) {
        if (contents == null) {
            contents = "";
        }
        try {
            exec("javascript:URE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
        } catch (UnsupportedEncodingException e) {
            // No handling
        }
        mContents = contents;
    }

    public String getHtml() {
        return mContents;
    }

    public void setEditorFontColor(int color) {
        String hex = convertHexColorString(color);
        exec("javascript:URE.setBaseTextColor('" + hex + "');");
    }

    public void setEditorFontSize(int px) {
        exec("javascript:URE.setBaseFontSize('" + px + "px');");
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:URE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                + "px');");
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // still not support RTL.
        setPadding(start, top, end, bottom);
    }

    public void setEditorBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resid) {
        Bitmap bitmap = Utils.decodeResource(getContext(), resid);
        String base64 = Utils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:URE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    @Override
    public void setBackground(Drawable background) {
        Bitmap bitmap = Utils.toBitmap(background);
        String base64 = Utils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:URE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    public void setBackground(String url) {
        exec("javascript:URE.setBackgroundImage('url(" + url + ")');");
    }

    public void setEditorWidth(int px) {
        exec("javascript:URE.setWidth('" + px + "px');");
    }

    public void setEditorHeight(int px) {
        exec("javascript:URE.setHeight('" + px + "px');");
    }

    public void setPlaceholder(String placeholder) {
        exec("javascript:URE.setPlaceholder('" + placeholder + "');");
    }

    public void setInputEnabled(Boolean inputEnabled) {
        exec("javascript:URE.setInputEnabled(" + inputEnabled + ")");
    }

    public void loadCSS(String cssFile) {
        String jsCSSImport = "(function() {" +
                "    var head  = document.getElementsByTagName(\"head\")[0];" +
                "    var link  = document.createElement(\"link\");" +
                "    link.rel  = \"stylesheet\";" +
                "    link.type = \"text/css\";" +
                "    link.href = \"" + cssFile + "\";" +
                "    link.media = \"all\";" +
                "    head.appendChild(link);" +
                "}) ();";
        exec("javascript:" + jsCSSImport + "");
    }

    public void undo() {
        exec("javascript:URE.undo();");
    }

    public void setBold() {
        exec("javascript:URE.setBold();");
    }

    public void setItalic() {
        exec("javascript:URE.setItalic();");
    }

    public void setUnderline() {
        exec("javascript:URE.setUnderline();");
    }

    public void setBullets() {
        exec("javascript:URE.setBullets();");
    }

    public void setNumbers() {
        exec("javascript:URE.setNumbers();");
    }

    public void insertImage(String url, String alt) {
        exec("javascript:URE.prepareInsert();");
        exec("javascript:URE.insertImage('" + url + "', '" + alt + "');");
    }

    public void insertStyledImage(String url, String styles, String alt) {
        exec("javascript:URE.prepareInsert();");
        exec("javascript:URE.insertStyledImage('" + url + "', '" + styles + "', '" + alt + "');");
    }

    public void insertLink(String href, String title) {
        exec("javascript:URE.prepareInsert();");
        exec("javascript:URE.insertLink('" + href + "', '" + title + "');");
    }

    public void setTextColor(int color) {
        exec("javascript:URE.prepareInsert();");

        String hex = convertHexColorString(color);
        exec("javascript:URE.setTextColor('" + hex + "');");
    }

    public void setFontSize(int fontSize) {
        if (fontSize > 7 || fontSize < 1) {
            Log.e("UpiRichEditor", "Font size should have a value between 1-7");
        }
        exec("javascript:URE.setFontSize('" + fontSize + "');");
    }

    public void focusEditor() {
        requestFocus();
        exec("javascript:URE.focus();");
    }

    public void clearFocusEditor() {
        exec("javascript:URE.blurFocus();");
    }

    private String convertHexColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    protected void exec(final String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec(trigger);
                }
            }, 100);
        }
    }

    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    protected class EditorWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(SETUP_HTML);
            if (mLoadListener != null) {
                mLoadListener.onAfterInitialLoad(isReady);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String decode;
            try {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // No handling
                return false;
            }

            if (TextUtils.indexOf(url, STYLE_PART) == 0) {
                stateAndContentCheck(decode);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}

