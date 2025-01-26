package com.goldbookapp.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class MyWebViewClient extends WebViewClient {
  private final WeakReference<Activity> mActivityRef;

  public MyWebViewClient(Activity activity) {
    mActivityRef = new WeakReference<Activity>(activity);
  }

 /* @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url.startsWith("mailto:")) {
      final Activity activity = mActivityRef.get();
      if (activity != null) {
        MailTo mt = MailTo.parse(url);
        Intent i = newEmailIntent(activity, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
        activity.startActivity(i);
        view.reload();
        return true;
      }
    } else {
      view.loadUrl(url);
    }
    return true;
  }*/
@SuppressWarnings("deprecation")
@Override
public boolean shouldOverrideUrlLoading(WebView view, String url) {
  if (url.startsWith("mailto:")) {
    //Handle mail Urls
   /* startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));*/
    final Activity activity = mActivityRef.get();
    if (activity != null) {
      MailTo mt = MailTo.parse(url);
      Intent i = newEmailIntent(activity, mt.getTo(), getApplicationName(activity),Uri.parse(url));
      //activity.startActivity(i);
      view.reload();
      return true;
    }
  } else if (url.startsWith("tel:")) {
    //Handle telephony Urls
    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
  } else {
    view.loadUrl(url);
  }
  return true;
}

  @TargetApi(Build.VERSION_CODES.N)
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    final Uri uri = request.getUrl();
    if (uri.toString().startsWith("mailto:")) {
      //Handle mail Urls
      final Activity activity = mActivityRef.get();
      if (activity != null) {
        MailTo mt = MailTo.parse(String.valueOf(uri));
        Intent i = newEmailIntent(activity, mt.getTo(),getApplicationName(activity) ,uri);
        //activity.startActivity(i);
        view.reload();
        return true;
      }
    } else if (uri.toString().startsWith("tel:")) {
      //Handle telephony Urls
      startActivity(new Intent(Intent.ACTION_DIAL, uri));
    } else {
      //Handle Web Urls
      view.loadUrl(uri.toString());
    }
    return true;
  }
  public static String getApplicationName(Context context) {
    return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
  }

  private Intent newEmailIntent(Context context, String address, String subject, Uri uri) {
    Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
    intent.putExtra(Intent.EXTRA_EMAIL, new String[] {address});
    intent.putExtra(Intent.EXTRA_SUBJECT,subject);
    //Toast.makeText(context,subject,Toast.LENGTH_SHORT).show();
    startActivity(intent);
    return intent;



  }
}