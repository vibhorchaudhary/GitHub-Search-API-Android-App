package com.mapprr.githubsearch.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.mapprr.githubsearch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolBarTitle;

    @BindView(R.id.webView)
    WebView webView;

    private ProgressDialog pDialog;

    private String url, name;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);
        ButterKnife.bind(this);
        url = getIntent().getStringExtra("url");
        name = getIntent().getStringExtra("name");

        setProgressBar();

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    hideProgressBar();
                }
            }
        });

        webView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);
        toolBarTitle.setText(name);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void setProgressBar() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(this);
        }
        if (!pDialog.isShowing() && !this.isFinishing()) {
            pDialog.setMessage("Loading Information! Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    private void hideProgressBar() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.cancel();
        }
    }
}
