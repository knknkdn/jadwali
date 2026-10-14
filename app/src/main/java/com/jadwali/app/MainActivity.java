package com.jadwali.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class MainActivity extends Activity {
    WebView w;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Scheduler.channel(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }
        w = new WebView(this);
        setContentView(w);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        w.setWebViewClient(new WebViewClient());
        w.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView v, String u, String m, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this).setMessage(m)
                    .setPositiveButton(android.R.string.ok, (d, i) -> r.confirm())
                    .setNegativeButton(android.R.string.cancel, (d, i) -> r.cancel())
                    .setOnCancelListener(d -> r.cancel()).show();
                return true;
            }
        });
        w.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void schedule(String json) {
                Scheduler.schedule(MainActivity.this, json);
            }

            @JavascriptInterface
            public void testNotif(String t, String b) {
                Scheduler.test(MainActivity.this, t, b);
            }

            @JavascriptInterface
            public void pickImage() {
                runOnUiThread(() -> {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(i, 7);
                });
            }
        }, "Android");
        w.loadUrl("file:///android_asset/index.html");
    }

    void js(String code) {
        runOnUiThread(() -> w.evaluateJavascript(code, null));
    }

    static void flush(List<Text.Line> row, JSONArray out) {
        if (row.isEmpty()) return;
        row.sort((a, b) -> a.getBoundingBox().left - b.getBoundingBox().left);
        StringBuilder sb = new StringBuilder();
        for (Text.Line l : row) sb.append(l.getText()).append(" | ");
        out.put(sb.toString());
    }

    @Override
    protected void onActivityResult(int rq, int rs, Intent d) {
        super.onActivityResult(rq, rs, d);
        if (rq != 7 || rs != RESULT_OK || d == null || d.getData() == null) return;
        try {
            InputImage im = InputImage.fromFilePath(this, d.getData());
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(im)
                .addOnSuccessListener(t -> {
                    List<Text.Line> ls = new ArrayList<>();
                    for (Text.TextBlock b : t.getTextBlocks()) ls.addAll(b.getLines());
                    ls.removeIf(l -> l.getBoundingBox() == null);
                    ls.sort((a, b) -> a.getBoundingBox().centerY() - b.getBoundingBox().centerY());
                    JSONArray out = new JSONArray();
                    List<Text.Line> row = new ArrayList<>();
                    int ref = 0;
                    for (Text.Line l : ls) {
                        int cy = l.getBoundingBox().centerY();
                        int th = Math.max(10, l.getBoundingBox().height() * 3 / 5);
                        if (!row.isEmpty() && Math.abs(cy - ref) > th) {
                            flush(row, out);
                            row = new ArrayList<>();
                        }
                        if (row.isEmpty()) ref = cy;
                        row.add(l);
                    }
                    flush(row, out);
                    js("onOcr(" + out.toString() + ")");
                })
                .addOnFailureListener(e -> js("onOcr([])"));
        } catch (Exception e) {
            js("onOcr([])");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
