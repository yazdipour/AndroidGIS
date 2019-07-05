package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers;

import android.content.Context;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.util.ArrayList;

public class MarkerHandler {

    private ItemizedLayer<MarkerItem> markerLayer;
    private ArrayList<MarkerItem> markersList = new ArrayList<>();

    private MarkerSymbol getSymbol(Bitmap bitmapMarker, boolean BILLBOARDS) {
        return BILLBOARDS ?
                new MarkerSymbol(bitmapMarker, MarkerSymbol.HotspotPlace.BOTTOM_CENTER)
                : new MarkerSymbol(bitmapMarker, MarkerSymbol.HotspotPlace.CENTER, false);
    }

    public MarkerSymbol getSymbol(int drawable, Context context, boolean BILLBOARDS) {
        Bitmap bitmapMarker = org.oscim.android.canvas.AndroidGraphics.
                drawableToBitmap(context.getResources().getDrawable(drawable));
        return getSymbol(bitmapMarker, BILLBOARDS);
    }

    public MarkerItem getMarkerItem(GeoPoint location, MarkerSymbol symbol, String title,
                                    String description) {
        MarkerItem markerItem = new MarkerItem(title, description, location);
        markerItem.setMarker(symbol);
        return markerItem;
    }

    public MarkerItem getMarkerItem(GeoPoint location, String title,
                                    String description) {
        return new MarkerItem(title, description, location);
    }

    ItemizedLayer<MarkerItem> getMarkerLayer(Map map, MarkerSymbol symbol,
                                             ItemizedLayer.OnItemGestureListener<MarkerItem> listener) {
        if (markerLayer == null)
            markerLayer = new ItemizedLayer<>(map, markersList, symbol, listener);
        return markerLayer;
    }

    public ItemizedLayer<MarkerItem> getMarkerLayer() {
        return markerLayer;
    }

    public int addItem(MarkerItem item) {
        int index = markersList.size();
        markersList.add(index, item);
        markerLayer.update();
        markerLayer.populate();
        return index;
    }

    public void removeItem(MarkerItem item) {
        markersList.remove(item);
        markerLayer.removeItem(item);
    }
}
