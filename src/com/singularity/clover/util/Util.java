package com.singularity.clover.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;


public class Util {
	public static byte[] serializeIdArray(Object ids) 
									throws IOException{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(ids);
		out.close();
		
		byte[] asBytes = bout.toByteArray();
		return asBytes;
	}

	public  static Object deserializeIdArray(Cursor cur,int columnIndex) 
			throws IOException, SQLException, ClassNotFoundException {
		byte[] buf = cur.getBlob(columnIndex);
		if(buf == null)
			return null;
		ByteArrayInputStream bin = new ByteArrayInputStream(buf);
		ObjectInputStream oin = new ObjectInputStream(bin);
		oin.close();
		return oin.readObject();
	}
	
	/**
     * Draw the view into a bitmap.
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        int color = v.getDrawingCacheBackgroundColor();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
           //Log.e("Util", "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
    
    public static Bitmap getViewBitmapByCanvas(View v){
    	Bitmap b = Bitmap.createBitmap( 
    			v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
    	
	    Canvas c = new Canvas(b);
	    c.drawARGB(0, 255, 255, 255);
	    //v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
	    v.draw(c);
	    return b;
    }
	
}
