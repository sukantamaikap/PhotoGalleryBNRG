package com.bignerdranch.android.photogallerybnrg;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by smaikap on 29/8/16.
 */
public class PhotoPageFragment extends VisibleFragment {

    private static final String ARG_URI = "photo_page_url";
    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(final Uri uri) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        final PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUri = this.getArguments().getParcelable(ARG_URI);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_photo_page, container, false);

        this.mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_photo_page_progress_bar);
        this.mProgressBar.setMax(100);

        this.mWebView = (WebView) view.findViewById(R.id.fragment_photo_page_web_view);
        this.mWebView.getSettings().setJavaScriptEnabled(Boolean.TRUE);
        this.mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(final WebView view, final int newProgress) {
                if (newProgress == 100) {
                    PhotoPageFragment.this.mProgressBar.setVisibility(View.GONE);
                } else {
                    PhotoPageFragment.this.mProgressBar.setVisibility(View.VISIBLE);
                    PhotoPageFragment.this.mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(final WebView view, final String title) {
                final AppCompatActivity activity = (AppCompatActivity) PhotoPageFragment.this.getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        });
        this.mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {
                return false;
            }
        });
        this.mWebView.loadUrl(this.mUri.toString());
        return view;
    }
}
