package org.libsdl.app;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

import android.widget.ArrayAdapter;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ListViewActivity extends Activity {
	private ListView mainListView ;
	private ArrayAdapter<String> listAdapter ;

	static final public String sRoot = "/sdcard"; 
	
	File[] files = null; /*files in pwd*/
	String sParent = null; /*path of ".."*/

private int refreshFileList(String path)
{
	File[] fa = null;
	try{
		fa = new File(path).listFiles();
	}catch(Exception e){
		fa = null;
	}
	if(null == fa){
		Toast.makeText(getApplicationContext(), "Can't Access " + path, Toast.LENGTH_SHORT).show();
		return -1;
	}
	files = fa;
	listAdapter.clear();
	listAdapter.add("..");
	for(File f : files){
		listAdapter.add(f.getName());
	}

	return 0;
}

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    mainListView = (ListView) findViewById( R.id.mainListView );

    listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);

	sParent = sRoot;
    refreshFileList(sRoot);
    
    mainListView.setAdapter( listAdapter );

    mainListView.setOnItemClickListener(new OnItemClickListener() {
  	public void onItemClick(AdapterView<?> parent, View view,
      int position, long id) {
	  if(0 == position){
	  	refreshFileList(sParent);
		if(!sParent.equals(sRoot)){
			sParent = new File(sParent).getParent();
		}
		return;
	  }
	
      File f = files[position-1];
	  if(f.isDirectory()){
	     refreshFileList(f.getPath());
		 sParent = f.getParent();
	  }else if(f.isFile()){
	      Intent intent = new Intent(getApplicationContext(), SDLActivity.class);
	      intent.putExtra("filename", f.getPath() );
	      startActivity(intent);
	  }
    }});
	
}
}
