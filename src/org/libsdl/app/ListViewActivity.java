package org.libsdl.app;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

import android.widget.ArrayAdapter;

import android.view.View;
import android.view.View.*; /*OnClickListener*/
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;  
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ListViewActivity extends Activity {
	private ListView mainListView ;
	private ArrayAdapter<String> listAdapter ;

	static final public String sRoot = "/sdcard"; 
	
	File[] files = null; /*files in pwd*/
	String sParent = null; /*path of ".."*/
	int inHistory = 0;
	History history = null;

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
	listAdapter.add("History");
	listAdapter.add("..");
	for(File f : files){
		listAdapter.add(f.getName());
	}

	return 0;
}

int refreshHistory(int onlyHistory)
{
	int i, n;
	String[] str = null;

	listAdapter.clear();
	if(1 == onlyHistory){
		listAdapter.add("..");
		str = history.getLines();
		n = history.getCount();

		for(i = 0; i < n; ++i){
			listAdapter.add(str[i]);	
		}
	}else if(0 == onlyHistory){
		listAdapter.add("History");
		listAdapter.add("..");
		for(File f : files){
			listAdapter.add(f.getName());
		}
	}
	return 0;
}

void startPlay(String fullPath)
{
	Intent intent = new Intent(getApplicationContext(), SDLActivity.class);
	intent.putExtra("filename", fullPath);
	startActivity(intent);
	history.put(fullPath);
}


@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	setTitle("choose files...");
	inHistory = 0;
	history = new History(getApplicationContext());

	Button btn = (Button)findViewById(R.id.btn_gw);
	btn.setOnClickListener(new OnClickListener(){
		@Override
		public void onClick(View v){
			Intent intent = new Intent(getApplicationContext(), GatewayClient.class);
			startActivity(intent);	
		}
	});

	final EditText editText=(EditText)findViewById(R.id.edit_text);  
	editText.setOnEditorActionListener(new OnEditorActionListener() {  
		@Override  
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
			startPlay(editText.getText().toString());
			return true;  
		}  
	});  

	mainListView = (ListView) findViewById( R.id.mainListView );
	listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);

	sParent = sRoot;
	refreshFileList(sRoot);

	mainListView.setAdapter( listAdapter );

	mainListView.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {
			if(1 == inHistory){
				if(0 == position){
					inHistory = 0;
					refreshHistory(inHistory);
				}else{
					startPlay(history.getLines()[position-1]);
				}
				return;
			}

			if( 0 == position){
				inHistory = 1;
				refreshHistory(inHistory);
			}else if(1 == position){
				refreshFileList(sParent);
				if(!sParent.equals(sRoot)){
					sParent = new File(sParent).getParent();
				}
			}else{
				File f = files[position-2];
				if(f.isDirectory()){
					refreshFileList(f.getPath());
					sParent = f.getParent();
				}else if(f.isFile()){
					startPlay(f.getPath());
				}
			}
		}});

	mainListView.setOnItemLongClickListener(new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> arg0, View v,
			int index, long arg3) {
			if(0 == inHistory || index < 1){
				return false;
			}
			String path = history.getLines()[index-1];
			history.remove(path);
			refreshHistory(inHistory);
			return true;
		}
	}); 

}
}
