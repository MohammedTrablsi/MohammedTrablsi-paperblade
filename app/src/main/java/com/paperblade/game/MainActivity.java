package com.paperblade.game;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Activity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Paper Blade — Android wrapper.
 * Hosts the game (assets/index.html) in a fullscreen WebView and forwards
 * gamepad input (GameSir, Xbox, PlayStation, any HID controller) to the game
 * via window.androidPad(button, down) / window.androidAxis(axis, value).
 * Button indices follow the W3C "standard" gamepad layout, which is what the
 * game's remapping screen uses.
 */
public class MainActivity extends Activity {
    private WebView web;
    private float lastX = 0f, lastY = 0f, lastHatX = 0f, lastHatY = 0f;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);           // saves button mappings
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.setBackgroundColor(0xFF07070A);
        web.setWebViewClient(new WebViewClient());
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
        hideSystemUI();
    }

    private void hideSystemUI() {
        View d = getWindow().getDecorView();
        d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    @Override
    protected void onResume() { super.onResume(); web.onResume(); hideSystemUI(); }

    @Override
    protected void onPause() { super.onPause(); web.onPause(); }

    private void js(String code) {
        if (web != null) web.post(() -> web.evaluateJavascript(code, null));
    }

    /** Android KeyEvent keycode -> standard gamepad button index (-1 = not a pad button). */
    private static int padIndex(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A: return 0;
            case KeyEvent.KEYCODE_BUTTON_B: return 1;
            case KeyEvent.KEYCODE_BUTTON_X: return 2;
            case KeyEvent.KEYCODE_BUTTON_Y: return 3;
            case KeyEvent.KEYCODE_BUTTON_L1: return 4;
            case KeyEvent.KEYCODE_BUTTON_R1: return 5;
            case KeyEvent.KEYCODE_BUTTON_L2: return 6;
            case KeyEvent.KEYCODE_BUTTON_R2: return 7;
            case KeyEvent.KEYCODE_BUTTON_SELECT: return 8;
            case KeyEvent.KEYCODE_BUTTON_START: return 9;
            case KeyEvent.KEYCODE_BUTTON_THUMBL: return 10;
            case KeyEvent.KEYCODE_BUTTON_THUMBR: return 11;
            case KeyEvent.KEYCODE_DPAD_UP: return 12;
            case KeyEvent.KEYCODE_DPAD_DOWN: return 13;
            case KeyEvent.KEYCODE_DPAD_LEFT: return 14;
            case KeyEvent.KEYCODE_DPAD_RIGHT: return 15;
            case KeyEvent.KEYCODE_BUTTON_MODE: return 16;
            default: return -1;
        }
    }

    private static boolean fromGamepad(KeyEvent e) {
        int src = e.getSource();
        return (src & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                || (src & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                || (src & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int idx = padIndex(e.getKeyCode());
        boolean pad = fromGamepad(e);
        // Many controllers report their B button as KEYCODE_BACK.
        if (idx < 0 && pad && e.getKeyCode() == KeyEvent.KEYCODE_BACK) idx = 1;
        if (idx >= 0 && (pad || e.getKeyCode() >= KeyEvent.KEYCODE_BUTTON_A)) {
            if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0) js("window.androidPad&&androidPad(" + idx + ",true)");
            else if (e.getAction() == KeyEvent.ACTION_UP) js("window.androidPad&&androidPad(" + idx + ",false)");
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent e) {
        if ((e.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && e.getAction() == MotionEvent.ACTION_MOVE) {
            float x = e.getAxisValue(MotionEvent.AXIS_X), y = e.getAxisValue(MotionEvent.AXIS_Y);
            float hx = e.getAxisValue(MotionEvent.AXIS_HAT_X), hy = e.getAxisValue(MotionEvent.AXIS_HAT_Y);
            if (x != lastX || hx != lastHatX) { lastX = x; lastHatX = hx; js("window.androidAxis&&androidAxis(0," + (Math.abs(hx) > 0.5f ? hx : x) + ")"); }
            if (y != lastY || hy != lastHatY) { lastY = y; lastHatY = hy; js("window.androidAxis&&androidAxis(1," + (Math.abs(hy) > 0.5f ? hy : y) + ")"); }
            return true;
        }
        return super.dispatchGenericMotionEvent(e);
    }
}
