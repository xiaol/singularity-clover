package com.singularity.clover.util;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.singularity.clover.R;
import com.singularity.clover.Global;


public class BitmapUtil {
	
	public static final int IMAGE_PREVIEW_SIZE = 64;
	public static final int IMAGE_PREVIEW_SIZE_H = 48;
	
	private static Bitmap createOriginalBitmap(int desWidth,int desHeight){

		return Bitmap.createBitmap(desWidth, desHeight, Bitmap.Config.RGB_565);
	}
	
	public static Bitmap previewBitmp(Bitmap bitmapOrg,Context context){
        Display display = ((WindowManager) context.getSystemService(
        		Context.WINDOW_SERVICE)).getDefaultDisplay();
        int desWidth = display.getWidth(); 
        int desHeight = display.getHeight();
		
        Bitmap scaled = Bitmap.createScaledBitmap(
        		bitmapOrg,desWidth,desHeight,true);
        return scaled;
	}
	
	
	public static Bitmap createOriginalBitmap(Context context){
		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int desWidth = display.getWidth()*Global.WHITEBOARD_SIZE_FACTOR; 
        int desHeight = display.getHeight()*Global.WHITEBOARD_SIZE_FACTOR;
		
        return createOriginalBitmap(desWidth, desHeight);
	}
	
    public static Bitmap getScaledBitmap(
    		Context context,String imageUriPath){
        Uri imageUri = Uri.parse(imageUriPath);
        Bitmap scaled = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = true;
		options.inDensity = (int) (context.getResources().getDisplayMetrics().density + 0.5f);
		try{
			Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), null);
			DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
			int thresholdW = (int) (displayMetrics.density*IMAGE_PREVIEW_SIZE);
			int thresholdH = (int) (displayMetrics.density*IMAGE_PREVIEW_SIZE_H);
			if(bitmap != null){
				int w = bitmap.getWidth()/3;
				int h = bitmap.getHeight()/3;
				if( w > thresholdW){
					w = thresholdW;
				}
				if( h > thresholdH){
					h = thresholdH;
				}
				scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);
				bitmap.recycle();
				bitmap = null;    
				return decorateBitmap(scaled,displayMetrics.density); 
		}
		}catch (OutOfMemoryError e) {
			return null;
		}
		return null;
    }
    
    public static Bitmap getScaledBitmap(
    		Context context,Bitmap bitmap){
    	Bitmap scaled = null;
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int thresholdW = (int) (displayMetrics.density*IMAGE_PREVIEW_SIZE);
		int thresholdH = (int) (displayMetrics.density*IMAGE_PREVIEW_SIZE_H);
		if(bitmap != null){
			int w = bitmap.getWidth()/3;
			int h = bitmap.getHeight()/3;
			if( w > thresholdW){
				w = thresholdW;
			}
			if( h > thresholdH){
				h = thresholdH;
			}
			if(!bitmap.isRecycled()){
				scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);    
				return decorateBitmap(scaled,displayMetrics.density); 
			}
		}
		return null;
    }
    
    public static Bitmap resizePhoto(Context context,Uri imageUri){
        Bitmap scaled = null;
		Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), null);
		
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int thresholdW = displayMetrics.widthPixels;
		int thresholdH = displayMetrics.heightPixels;
		if(thresholdW > thresholdH){
			thresholdH = thresholdW;
			thresholdW = displayMetrics.heightPixels;
		}
		if(bitmap != null){
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			if(w >h){
				Matrix matrix = new Matrix();
				matrix.postScale(thresholdH/h, thresholdH/h );
				matrix.postRotate(90);
				scaled = Bitmap.createBitmap(bitmap, 0, 0, 
						bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				bitmap.recycle();
				bitmap = null;		
			}else{
				scaled = Bitmap.createScaledBitmap(
						bitmap, thresholdW, thresholdH, true);
				bitmap.recycle();
				bitmap = null;
			}		
			return scaled;
		}
		return null;
    }
    
    public static Bitmap getBitmap(Context context,String strUri,Rect drawingRect){
  		Uri uri = Uri.parse(strUri);
  		Bitmap bitmap = null;
  		BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inDither = true;
		//options.inDensity = (int) (context.getResources().getDisplayMetrics().density + 0.5f);
  		options.inSampleSize =2;
		Bitmap bitmap2 = null;
		boolean bFlag = false;
		try{
			bitmap2 = createOriginalBitmap(context);
			bitmap = BitmapFactory.decodeFile(uri.getPath(),null);
			if(bitmap != null){
				Canvas c = new Canvas(bitmap2);
				c.drawColor(context.getResources().getColor(R.color.whiteboard_bg));
				int left = (bitmap2.getWidth()-bitmap.getWidth())/2;
				int top = (bitmap2.getHeight()-bitmap.getHeight())/2;
				c.drawBitmap(bitmap, (bitmap2.getWidth()-bitmap.getWidth())/2,
						(bitmap2.getHeight()-bitmap.getHeight())/2, null);
				drawingRect.set(left, top, 
						left +bitmap.getWidth(), top + bitmap.getHeight());
				c = null;
				bitmap.recycle();
				bitmap = bitmap2;
			}else{
				bitmap = bitmap2;
				Canvas c = new Canvas(bitmap);
				c.drawColor(context.getResources().getColor(R.color.whiteboard_bg));
				c = null;
			}
			return bitmap;
		}catch (OutOfMemoryError e) {
			if(bitmap2  !=null){
				bFlag = true;
			}else{
				bFlag = false;
			}
		}
		if(!bFlag){
			return null;
		}else{
			try{
				bitmap2.recycle();
				bitmap2 = null;
				bitmap = BitmapFactory.decodeFile(uri.getPath(),null); 
				System.gc();
				return bitmap;
			}catch (OutOfMemoryError e) {
				System.gc();
				return null;
			}
		}
    }
    
    public static void deleteBitmap(Context context,String strUri){
    	Uri uri = Uri.parse(strUri);
    	context.getContentResolver().delete(uri, null, null);
    }
    
	public static void takePhoto(Activity activity,int requestCode,Uri uri) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void pickPhoto(Activity activity,int requestCode){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		activity.startActivityForResult(intent, requestCode);
	}
	
	public static Uri photoFromPicker(Intent intent){
		return intent.getData();
	}
	
	private static Bitmap decorateBitmap(Bitmap bitmap,float density){
		Canvas c = new Canvas(bitmap);
		return decorateBitmap(bitmap, c,density);
	}
	
	public static Bitmap decorateBitmap(Bitmap bitmap,Canvas c,float density){
		Paint paint = new Paint();
		
		paint.setDither(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeWidth(3*density);
        
		c.drawRect(1*density, 1*density, bitmap.getWidth() - 1*density,
				bitmap.getHeight() - 1*density, paint);
		c = null;
		return bitmap;
	}
}
