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
        SUBSCRIPT,
        SUPERSCRIPT,
        STRIKETHROUGH,
        UNDERLINE,
        H1,
        H2,
        H3,
        H4,
        H5,
        H6,
        ORDEREDLIST,
        UNORDEREDLIST,
        JUSTIFYCENTER,
        JUSTIFYFULL,
        JUSTUFYLEFT,
        JUSTIFYRIGHT
    }

    public interface OnTextChangeListener {
        void onTextChange(String text);
        void onStyleCheck(String text, List<Type> types);

    }

    public interface OnDecorationStateListener {
        void onStateChangeListener(String text, List<Type> types);
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SCHEME = "rte-callback://";
    private static final String STATE_SCHEME = "rte-state://";
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

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }

    public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
        mDecorationStateListener = listener;
    }

    public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
        mLoadListener = listener;
    }

    private void callback(String text) {
        mContents = text.replaceFirst(CALLBACK_SCHEME, "");
        String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
        List<Type> types = new ArrayList<>();
        for (Type type : Type.values()) {
            if (TextUtils.indexOf(state, type.name()) != -1) {
                types.add(type);
            }
        }
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mContents);
            mTextChangeListener.onStyleCheck(state, types);
        }
    }

    private void stateCheck(String text) {
        String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
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
                exec("javascript:RTE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:RTE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:RTE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:RTE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:RTE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:RTE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER:
                exec("javascript:RTE.setVerticalAlign(\"middle\")");
                exec("javascript:RTE.setTextAlign(\"center\")");
                break;
        }

        ta.recycle();
    }

    public void setHtml(String contents) {
        if (contents == null) {
            contents = "";
        }
        try {
            exec("javascript:RTE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
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
        exec("javascript:RTE.setBaseTextColor('" + hex + "');");
    }

    public void setEditorFontSize(int px) {
        exec("javascript:RTE.setBaseFontSize('" + px + "px');");
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RTE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
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

        exec("javascript:RTE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    @Override
    public void setBackground(Drawable background) {
        Bitmap bitmap = Utils.toBitmap(background);
        String base64 = Utils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RTE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    public void setBackground(String url) {
        exec("javascript:RTE.setBackgroundImage('url(" + url + ")');");
    }

    public void setEditorWidth(int px) {
        exec("javascript:RTE.setWidth('" + px + "px');");
    }

    public void setEditorHeight(int px) {
        exec("javascript:RTE.setHeight('" + px + "px');");
    }

    public void setPlaceholder(String placeholder) {
        exec("javascript:RTE.setPlaceholder('" + placeholder + "');");
    }

    public void setInputEnabled(Boolean inputEnabled) {
        exec("javascript:RTE.setInputEnabled(" + inputEnabled + ")");
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
        exec("javascript:RTE.undo();");
    }

    public void redo() {
        exec("javascript:RTE.redo();");
    }

    public void setBold() {
        exec("javascript:RTE.setBold();");
    }

    public void setItalic() {
        exec("javascript:RTE.setItalic();");
    }

    public void setUnderline() {
        exec("javascript:RTE.setUnderline();");
    }

    public void setTextColor(int color) {
        exec("javascript:RTE.prepareInsert();");

        String hex = convertHexColorString(color);
        exec("javascript:RTE.setTextColor('" + hex + "');");
    }

    public void setTextBackgroundColor(int color) {
        exec("javascript:RTE.prepareInsert();");

        String hex = convertHexColorString(color);
        exec("javascript:RTE.setTextBackgroundColor('" + hex + "');");
    }

    public void setFontSize(int fontSize) {
        if (fontSize > 7 || fontSize < 1) {
            Log.e("UpiRichEditor", "Font size should have a value between 1-7");
        }
        exec("javascript:RTE.setFontSize('" + fontSize + "');");
    }

    public void removeFormat() {
        exec("javascript:RTE.removeFormat();");
    }

    public void setHeading(int heading) {
        exec("javascript:RTE.setHeading('" + heading + "');");
    }

    public void setIndent() {
        exec("javascript:RTE.setIndent();");
    }

    public void setOutdent() {
        exec("javascript:RTE.setOutdent();");
    }

    public void setAlignLeft() {
        exec("javascript:RTE.setJustifyLeft();");
    }

    public void setAlignCenter() {
        exec("javascript:RTE.setJustifyCenter();");
    }

    public void setAlignRight() {
        exec("javascript:RTE.setJustifyRight();");
    }

    public void setBlockquote() {
        exec("javascript:RTE.setBlockquote();");
    }

    public void setBullets() {
        exec("javascript:RTE.setBullets();");
    }

    public void setNumbers() {
        exec("javascript:RTE.setNumbers();");
    }

    public void insertImage(String url, String alt) {
        exec("javascript:RTE.prepareInsert();");
        exec("javascript:RTE.insertImage('" + url + "', '" + alt + "');");
    }

    public void insertLink(String href, String title) {
        exec("javascript:RTE.prepareInsert();");
        exec("javascript:RTE.insertLink('" + href + "', '" + title + "');");
    }

    public void focusEditor() {
        requestFocus();
        exec("javascript:RTE.focus();");
    }

    public void clearFocusEditor() {
        exec("javascript:RTE.blurFocus();");
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

            if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
                callback(decode);
                return true;
            } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
                stateCheck(decode);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}

