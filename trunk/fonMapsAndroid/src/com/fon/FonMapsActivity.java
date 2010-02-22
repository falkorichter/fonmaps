package com.fon;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * A minimal Map application.
 */
public class FonMapsActivity extends MapActivity implements OnClickListener {
	private static String TAG = FonMapsActivity.class.getName();

	private MapView mMapView;

	private List<Overlay> overlays;

	private HashMap<Integer, double[]> spots;

	public static final int MILLION = 1000000;

	private FonOverlays fonoverlay;

	private Exception exception = null;

	private MapController controller;

	private fonPopupWindow messageWindow;

	private PopupThread thread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		this.getResources().getDrawable(R.drawable.fonspot_active);

		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		Button goButton = (Button) findViewById(R.id.go);
		goButton.setOnClickListener(this);

		// Add the map view to the frame
		mMapView = new MapView(this, "04TCziHkkyQ_S-Z--AIbdEdH3n_zsu-S2uPkLMw");
		frame.addView(mMapView, new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		mMapView.setClickable(true);
		mMapView.getController().setZoom(15);
		mMapView.setEnabled(true);

		messageWindow = new fonPopupWindow(this);
		messageWindow.setHeight(100);
		messageWindow.setWidth(200);

		thread = new PopupThread(messageWindow);

		addZoomControls();

		controller = mMapView.getController();
		controller.setCenter(new GeoPoint((int) (MILLION * 40.41718823764398), (int) (MILLION * -3.6993026733398438)));

		fonoverlay = new FonOverlays(spots, this.getResources(), messageWindow);
		overlays = mMapView.getOverlays();
		overlays.add(fonoverlay);

		// findFonSpots(mMapView);
		// displayFonSpots(mMapView);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_I:
				int level = mMapView.getZoomLevel();
				mMapView.getController().setZoom(level + 1);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_O:
				int level2 = mMapView.getZoomLevel();
				mMapView.getController().setZoom(level2 - 1);
				return true;
			case KeyEvent.KEYCODE_S:
				mMapView.setSatellite(true);
				return true;
			case KeyEvent.KEYCODE_T:
				mMapView.setSatellite(false);
				return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 2, R.string.myLocation);
		// menu 1
		SubMenu sub1 = menu.addSubMenu(3, 2, 3, R.string.possibleViews);
		sub1.add(3, 3, 1, R.string.satelite);
		sub1.add(3, 4, 2, R.string.traffic);
		// menu 2
		SubMenu sub = menu.addSubMenu(2, 5, 1, R.string.zoom);
		sub.add(2, 6, 1, R.string.zoomIn);
		sub.add(2, 7, 2, R.string.zoomOut);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		switch (item.getItemId()) {
			case 1:
				return true;
			case 3:
				mMapView.setSatellite(true);
				return true;
			case 4:
				mMapView.setSatellite(false);
				return true;
			case 6:
				controller.zoomIn();
				return true;
			case 7:
				controller.zoomOut();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void findFonSpots(MapView mMapView) {

		double latitudeSpan = mMapView.getLatitudeSpan();
		double longitudeSpan = mMapView.getLongitudeSpan();

		GeoPoint center = mMapView.getMapCenter();

		double swLat = (center.getLatitudeE6() - (latitudeSpan / 2)) / MILLION;
		double swLng = (center.getLongitudeE6() - (longitudeSpan / 2)) / MILLION;

		double neLat = (center.getLatitudeE6() + (latitudeSpan / 2)) / MILLION;
		double neLng = (center.getLongitudeE6() + (longitudeSpan / 2)) / MILLION;

		// Log.i(TAG,"center: "+center.toString(),null);
		// Log.i(TAG,"latspan: "+center.toString(),null);

		try {
			URL nodesURL = new URL("http://maps.fon.com/ajax/getNodes?" + "swLat=" + swLat + "&swLng=" + swLng
					+ "&neLat=" + neLat + "&neLng=" + neLng + "&zoom=" + mMapView.getZoomLevel() + "&forceRefresh=0"
					+ "&filters=2047" + "&status=1");
			// nodesURL = new
			// URL("http://maps.fon.com/ajax/getNodes?swLat=40.41718823764398&swLng=-3.6993026733398438&neLat=40.42452300107238&neLng=-3.680269718170166&zoom=16&forceRefresh=0&filters=2047&status=1");

			HttpURLConnection con = (HttpURLConnection) nodesURL.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			String jsonString = con.getHeaderField("X-JSON");

			// remove the wrong braces at the end and beginning
			jsonString = jsonString.substring(1, jsonString.length() - 1);

			JSONArray nodesArray = new JSONArray(jsonString);

			// should be "add" nodesArray.getJSONArray(1).getString(0)
			if ((!(nodesArray.getJSONArray(1).getString(0)).equals("add"))) {
				Log
						.i(TAG, "wrong format of the response it is:'" + nodesArray.getJSONArray(1).getString(0) + "'",
								null);

				spots = null;
				return;
			}

			JSONArray add = nodesArray.getJSONArray(1).getJSONArray(1);
			HashMap<Integer, double[]> nodes = new HashMap<Integer, double[]>();

			for (int i = 0; i < add.length(); i++) {
				// add.getJSONArray(i).getJSONArray(5)

				JSONArray arrayOfNodes = null;
				try {
					if (add.getJSONArray(i).optInt(2) == 1 || add.getJSONArray(i).optInt(2) == 0) {
						arrayOfNodes = add.getJSONArray(i).optJSONArray(5);
					} else {
						arrayOfNodes = add.getJSONArray(i).optJSONArray(3);
						if (arrayOfNodes == null) {
							arrayOfNodes = add.getJSONArray(i).optJSONArray(2);
						}
					}
				} catch (JSONException e) {
					exception = e;
				}
				if (arrayOfNodes == null) {
					Log.i(TAG, "problem with this Array", exception);
					spots = null;
					return;
				}

				for (int j = 0; j < arrayOfNodes.length(); j++) {
					JSONArray node = arrayOfNodes.getJSONArray(j);
					try {
						double coord[] = { node.getDouble(1), node.getDouble(2) };
						nodes.put(node.getInt(0), coord);
					} catch (Exception e) {
						Log.w(TAG, "Problem with node" + node.getInt(0), e);
					}
				}
			}
			spots = nodes;
			Log.i(TAG, nodes.size() + " Spots fetched", null);
			return;

		} catch (Exception e) {
			spots = null;
			Log.w(TAG, "An Excpetion occured while fetching the FonSpot locations", e);
		}

		Log.i(TAG, "no Spots", null);
		spots = null;
		return;
	}

	private void addZoomControls() {
		mMapView.setBuiltInZoomControls(true);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onClick(View v) {
		Log.i(TAG, "button klicked", null);
		messageWindow.setText("test123456");
		if (!messageWindow.isShowing()) {
			messageWindow.setAnimationStyle(2);
			messageWindow.showAtLocation(mMapView, 1, 0, 0);
			Log.i(TAG, "calling to diappear", null);
			thread.startFor(1000, messageWindow);
			Log.i(TAG, "after the call", null);
		} else {
			messageWindow.dismiss();
		}

		findFonSpots(mMapView);
		// displayFonSpots(mMapView);
		// mMapView.refreshDrawableState();
	}

	public void dismissPopup(PopupThread popupThread) {
		popupThread.stop();
	}
}
