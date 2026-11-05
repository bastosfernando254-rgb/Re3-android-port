package com.re3.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int REQUEST_CUE = 41;
    private static final String PREFS = "re3_android";
    private static final String KEY_CUE_URI = "cue_uri";

    static { System.loadLibrary("re3_android"); }

    private native String nativeStatus();
    private native void nativeSetButtons(int mask);

    private TextView statusView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF0C0E12);

        statusView = new TextView(this);
        statusView.setTextColor(0xFFFFFFFF);
        statusView.setTextSize(15);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(24, 24, 24, 24);
        statusView.setOnClickListener(v -> showDiscOptions());
        TouchPadView pad = new TouchPadView(this, mask -> nativeSetButtons(mask));
        root.addView(pad, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Add the status panel last so it remains tappable above the touch overlay.
        root.addView(statusView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ));

        setContentView(root);
        refreshStatus();
    }

    private void showDiscOptions() {
        new AlertDialog.Builder(this)
            .setTitle("Disco do jogo")
            .setMessage("Selecione o arquivo .cue da sua cópia do jogo. O shell ainda não consegue iniciar o jogo; a integração do leitor de disco com o motor está pendente.")
            .setPositiveButton("Selecionar CUE", (d, w) -> chooseCue())
            .setNegativeButton("Fechar", null)
            .show();
    }

    private void chooseCue() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
            "application/octet-stream", "text/plain", "application/x-cue"
        });
        startActivityForResult(intent, REQUEST_CUE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CUE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                final int flags = data.getFlags() &
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {
                    // Alguns provedores não oferecem permissão persistente.
                }
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(KEY_CUE_URI, uri.toString()).apply();
                refreshStatus();
            }
        }
    }

    private void refreshStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(KEY_CUE_URI, null);
        String disc = saved == null ? "Nenhum CUE selecionado" : "CUE selecionado: " + Uri.parse(saved).getLastPathSegment();
        statusView.setText("RESIDENT EVIL 3 — ANDROID SHELL\n" + nativeStatus() +
            "\n" + disc + "\nToque aqui para escolher/trocar o CUE.");
    }

    @Override protected void onPause() {
        nativeSetButtons(0);
        super.onPause();
    }
}
