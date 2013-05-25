package com.android.herenow;

/** Global imports **/
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import android.content.Context;
import java.util.HashMap;

/** Localisation imports **/
import java.util.Locale;

/** WebView imports **/
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.content.res.Configuration;
import android.view.ViewGroup.LayoutParams;
import android.view.KeyEvent;

/** URLbuilder imports **/
import android.net.Uri;
import android.net.Uri.Builder;

/** Telephony Manager imports **/
import android.telephony.TelephonyManager;

/** Location imports **/
import android.telephony.gsm.GsmCellLocation;
import android.location.Location;
import android.location.LocationManager;

/** Exception imports **/
import java.lang.NullPointerException;
import java.lang.NumberFormatException;

public class HereNow extends Activity
{
	protected FrameLayout webViewPlaceholder;
	protected WebView webView;
	
	private static final String LOGCATAG = "HereNowAndroid";
	
	public static String getLangageCode()
	{
		String result;
		
		HashMap isomatch = new HashMap();
		isomatch.put("ENG", "0");
		isomatch.put("USA", "0");
		isomatch.put("FRA", "2");
		isomatch.put("DEU", "3");
		isomatch.put("ESP", "4");
		isomatch.put("ITA", "5");
		isomatch.put("SWE", "6");
		isomatch.put("DNK", "7");
		isomatch.put("NOR", "8");
		isomatch.put("FIN", "9");
		isomatch.put("PRT", "13");
		isomatch.put("TUR", "14");
		isomatch.put("ISL", "15");
		isomatch.put("RUS", "16");
		isomatch.put("HUN", "17");
		isomatch.put("NLD", "18");
		isomatch.put("CZE", "25");
		isomatch.put("SVK", "26");
		isomatch.put("POL", "27");
		isomatch.put("SVN", "28");
		isomatch.put("JPN", "29");
		isomatch.put("CHN", "30");
		
		String default_locale = Locale.getDefault().getISO3Country();
		
		result = (String) isomatch.get(default_locale);
		if(result == null)
		{
			result = "0";
		}
		
		return result;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		initUI();
	}
	
	protected void initUI()
	{
		// 1st - build the URL
		Uri.Builder url = Uri.parse("http://ics.svc.ovi.com/ics/app").buildUpon();
		url.appendQueryParameter("page", "livestream/herenow");
		url.appendQueryParameter("service", "page");
		url.appendQueryParameter("language", getLangageCode());
		
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
		try {
			int cid = location.getCid();
			int lac = location.getLac();
			if (Integer.toString(cid) != null ) {
				url.appendQueryParameter("cid", Integer.toString(cid));
			}
			if (Integer.toString(lac) != null) {
				url.appendQueryParameter("lac", Integer.toString(lac));
			}
		}
		catch (NullPointerException e) {
			Log.e(LOGCATAG, "CID or LAC access failed");
		}
		
		String networkOperator = telephonyManager.getNetworkOperator();
		if ((networkOperator != null) && (networkOperator.length() > 0)) {
			try {
				int mcc = Integer.parseInt(networkOperator.substring(0, 3));
				int mnc = Integer.parseInt(networkOperator.substring(3));
				
				url.appendQueryParameter("cmcc", Integer.toString(mcc));
				url.appendQueryParameter("cmnc", Integer.toString(mnc));
			} 
			catch (NumberFormatException e) {
				Log.e(LOGCATAG, "MNC or MNC access failed");

			}
		}
			
		String url_string = url.build().toString();
		// Logcating the url, for debug purposes
		//Log.v(LOGCATAG, url_string);
		
		// 2nd - Open it in a WebView
		webViewPlaceholder = ((FrameLayout)findViewById(R.id.webViewPlaceholder));
		
		if (webView == null)
		{
			webView = new WebView(this);
			webView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			webView.setScrollbarFadingEnabled(true);
			webView.getSettings().setJavaScriptEnabled(true);
			
			// Load the URLs inside the WebView, not in the external web browser
			webView.setWebViewClient(new WebViewClient());
			
			webView.loadUrl(url_string);
			
		}
		// Attach the WebView to its placeholder
		webViewPlaceholder.addView(webView);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		if (webView != null)
		{
		  // Remove the WebView from the old placeholder
		  webViewPlaceholder.removeView(webView);
		}

		super.onConfigurationChanged(newConfig);
		 
		// Load the layout resource for the new configuration
		setContentView(R.layout.main);

		// Reinitialize the UI
		initUI();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		// Save the state of the WebView
		webView.saveState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		// Restore the state of the WebView
		webView.restoreState(savedInstanceState);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// Check if the key event was the Back button and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		// If it wasn't the Back key or there's no web page history, bubble up to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}
}
