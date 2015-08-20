package org.libsdl.app;
import android.util.Log;
import java.io.*;
import android.content.Context;
import android.os.Environment;

public class History
{
	public static int read(Context ctx, String fileName, String[] data, int cnt)
	{
		int i = 0;

		try{
			BufferedReader fp = null;

			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/ffplay/history.txt");
			if(!dir.exists()){
				dir.getParentFile().mkdirs();
				dir.createNewFile();
			}

			if(dir.exists()){
				fp = new BufferedReader(new InputStreamReader(new FileInputStream(dir)));
			}else{
				fp = new BufferedReader(new InputStreamReader(ctx.openFileInput(fileName)));
			}

			for(i = 0; fp != null && i < cnt; ++i){
				data[i] = fp.readLine();
				if(null == data[i]){
					break;
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return i;
	}

	public static int write(Context ctx, String fileName, String[] data, int cnt)
	{
		FileOutputStream fp = null;
		int i;
	
		try{
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/ffplay/history.txt");
			if(!dir.exists()){
				dir.getParentFile().mkdirs();
				dir.createNewFile();	
			}

			if(dir.exists()){
				fp = new FileOutputStream(dir);
			}else{
				fp = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
			}

			for(i = 0; i < cnt; ++i){
				fp.write(data[i].getBytes());
				fp.write('\n');
			}
			fp.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
		return 0;
	}
	
	String fileName = "history.txt";
	String[] lines = new String[16];
	int cnt = 0;
	Context ctx = null; //

	public History(Context inCtx)
	{
		int i;
		ctx = inCtx;
		cnt = read(ctx, fileName, lines, lines.length);
		for(i = 0; i < cnt; ++i){
			Log.v("History", "line " + i + " is " + lines[i]);	
		}
		Log.v("History", "total lines " + cnt);
	}
	public String[] getLines()
	{
		return lines;
	}
	public int getCount()
	{
		return cnt;
	}

	public int put(String str)
	{
		int i;
		for(i = 0; i < cnt; ++i){
			if(str.equals(lines[i])){
				return -1;
			}
		}

		if(cnt <= lines.length - 1){
			lines[cnt+0] = str;
			cnt++;
		}else{
			for(i = 0; i < cnt-1; ++i){
				lines[i] = lines[i+1];
			}
			lines[i] = str;
			cnt = i+1;
		}	
		write(ctx, fileName, lines, cnt);
		return cnt;
	}

	public int remove(String str)
	{
		int i, j;
		for(i = 0; i < cnt; ++i){
			if(str.equals(lines[i])){
				break;
			}
		}
		if(i >= cnt){
			return -1;
		}
		for(j = i; j < cnt-1; ++j){
			lines[j] = lines[j+1];	
		}
		cnt--;
		write(ctx, fileName, lines, cnt);
		return 0;
	}
}
