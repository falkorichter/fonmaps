package com.fon;

import java.util.HashMap;
import java.util.Iterator;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.PopupWindow;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class FonOverlays extends Overlay {
	private static String TAG = FonOverlays.class.getName();

	private Drawable fonspot_active = null;

	private HashMap<Integer, double[]> spots;

	public void setSpots(HashMap<Integer, double[]> spots) {
		this.spots = spots;
	}

	public FonOverlays(HashMap<Integer, double[]> spots, Resources resources, PopupWindow poup) {
		Log.i(TAG, "FonOverlays created", null);
		this.spots = spots;
		fonspot_active = resources.getDrawable(R.drawable.fonspot_active);
		fonspot_active.setBounds(0, 0, 32, 32);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		double size = -56 + 5.3 * mapView.getZoomLevel();

		// Log.i(TAG,"FonOverlays draw spot size is "+size,null);

		fonspot_active.setBounds(0, 0, (int) size, (int) size);
		if (spots != null && spots.size() > 1 && !shadow && size > 3) {
			Iterator<double[]> iterator = spots.values().iterator();
			while (iterator.hasNext()) {
				double[] next = iterator.next();
				Point spot = mapView.getProjection().toPixels(
						new GeoPoint((int) (next[0] * FonMapsActivity.MILLION),
								(int) (next[1] * FonMapsActivity.MILLION)), null);
				super.draw(canvas, mapView, shadow);
				if (spot.x > 0 && spot.y > 0)
					drawAt(canvas, fonspot_active, spot.x, spot.y, shadow);
			}
		}
	}
}
