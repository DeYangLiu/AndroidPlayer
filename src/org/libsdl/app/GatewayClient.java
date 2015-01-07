package org.libsdl.app;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.*; /*Collections, Enumeration*/
import java.lang.reflect.Field;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.wifi.WifiManager;
import android.content.*; /*Context, Intent*/

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.*;
import android.util.*; /*Log*/

public class GatewayClient extends Activity 
{
	ListView mainListView;
	ArrayAdapter<String> listAdapter = null;
	int listCount = 0;
	String[] listName = new String[100];
	String[] listUrl = new String[100];

	long startTime = 0;
	History history;

	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {   
			String str = (String)msg.obj;
			int i;
			switch (msg.what) {   
				case 1:   
				listAdapter.clear();
				listAdapter.add(str);
				for(i = 0; i < listCount; ++i){
					listAdapter.add(listName[i]);
				}
				break;   
			}   
		}   
	};

	Runnable mRunnable = new Runnable() 
	{
		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			setTitle("elapsed " + String.format("%d:%02d", minutes, seconds));
			mHandler.postDelayed(this, 1000);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gw);

		mainListView = (ListView) findViewById( R.id.gw_list);
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);

		mainListView.setAdapter(listAdapter);
		mainListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
				if(position >= 1){
					String fullPath = listUrl[position-1];
					Intent intent = new Intent(getApplicationContext(), SDLActivity.class);
					intent.putExtra("filename", fullPath);
					startActivity(intent);
					history.put(fullPath);

					mHandler.removeCallbacks(mRunnable);
				}
		}});

		startTime = System.currentTimeMillis();
		mHandler.postDelayed(mRunnable, 0);
		Log.v("gw", "==start get");
		startGet();

		history = new History(getApplicationContext());
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mRunnable);
	}

	String findWord(String str, String prefix, String suffix)
	{/*return null if not found.*/
		String res = "";
		String str2 = str.toLowerCase();
		String prefix2 = prefix.toLowerCase();
		String suffix2 = suffix.toLowerCase();

		int j, i = str2.indexOf(prefix2, 0);
		if(i >= 0){
			i += prefix2.length();
			j = str2.indexOf(suffix2, i);
			if(j >= 0){
				res = str.substring(i, j).trim();
			}
		}
		return res;	
	}

	public static InetAddress getAddress() throws Exception{
		Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface iface : Collections.list(interfaceEnumeration)){
			if(!iface.isUp() || iface.isLoopback()){
				continue;
			}
			if(!iface.supportsMulticast()){
				continue;
			}

			for (InetAddress addr : Collections.list(iface.getInetAddresses()) ){
				if(!(addr instanceof Inet4Address) || addr.isLoopbackAddress()){
					continue;
				}
				return addr;
			}
		}
		return null;
	}
	
	String getLocation() throws Exception{

		String data = 
			"M-SEARCH * HTTP/1.1\r\n"+
			"HOST: 239.255.255.250:1900\r\n"+
			"MAN: \"ssdp:discover\"\r\n"+
			"MX: 3\r\n"+
			"ST: ssdp:all\r\n"+
			"\r\n";
		String res = "";
		int cnt = 0;
		
		InetAddress localInAddress = getAddress(); /*InetAddress.getLocalHost() is "127.0.0.1"*/
		if(null == localInAddress){
			Log.v("gw", "cant get addr");
			return "";
		}
		String ifaceStr = "";
		
		SocketAddress mSSDPMulticastGroup;
		MulticastSocket mSSDPSocket;
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		mSSDPSocket = new MulticastSocket(new InetSocketAddress(localInAddress, 0));
		/*mSSDPSocket.setReuseAddress(true);
		mSSDPSocket.setReceiveBufferSize(32768); 
		mSSDPSocket.setTimeToLive(4);*/
		NetworkInterface netIf = NetworkInterface.getByInetAddress(localInAddress);
		mSSDPMulticastGroup = new InetSocketAddress("239.255.255.250", 1900);
		mSSDPSocket.joinGroup(mSSDPMulticastGroup, netIf);

		ifaceStr = netIf.getDisplayName() + ":" + localInAddress.getHostAddress().toString();
		ifaceStr += " local=" + localInAddress;
		Log.v("gw", ifaceStr);

		WifiManager.MulticastLock lock = wm.createMulticastLock("lock");
		if(!lock.isHeld()){
			lock.acquire();
		}else{
			Log.v("gw", "==lock is held");
		}
		Log.v("gw", "==after lock==");

		DatagramPacket dp = new DatagramPacket(data.getBytes(), data.length(), mSSDPMulticastGroup); 
		mSSDPSocket.send(dp); 
		
		Log.v("gw", "==send "+data.substring(0,8));	
		do{
		byte[] buf = new byte[640+1024]; 
		DatagramPacket rp = new DatagramPacket(buf, buf.length); 
		mSSDPSocket.receive(rp); 
		String str = new String(buf, "ISO-8859-1");
		int endPos = str.indexOf("\r\n\r\n");
		Log.v("gw", "udp recv:\n" + str.substring(0, endPos));
		res = findWord(str, "Location:", "\n");
		++cnt;
		}while(res.equals("") && cnt < 16);

		mSSDPSocket.close(); 
		lock.release();	
		return res;
	}

	String getFile(String urlString) throws Exception{
		if(urlString.equals("") || urlString.charAt(0)=='/')return "";

		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.connect();
		InputStream stream = conn.getInputStream();
		//bitmap = BitmapFactory.decodeStream(is); 
		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(stream));

		String resultData = "", inputLine  = "";  
		while((inputLine = bufferReader.readLine()) != null){  
			resultData += inputLine + "\n";  
		} 
		stream.close();	
		conn.disconnect();

		return resultData;
	}

	void startGet()
	{
		Thread thread = new Thread(new Runnable(){
		@Override
		public void run() {
			try {
				String url, xml, location, name;
				updateList(1, "get location");
				location = getLocation();
				xml = getFile(location);
				Log.v("gw", "get xml: "+xml);
				url = findWord(xml, "<presentationURL>", "</presentationURL>");
				name = findWord(xml, "<friendlyName", "</friendlyName>");
				Log.v("gw", "url='" + url+"'");
				xml = getFile(url + "/getList"); //todo
				getList(xml);
				updateList(1, name);
			} catch (Exception e) {
				e.printStackTrace();
		}}});

		thread.start(); 

		/*try{
			thread.join();
		}catch(Exception e) {
			e.printStackTrace();
		}*/

	}
	boolean getList(String str)
	{
		listName[0] = "test1";
		listUrl[0] = "http://127.0.0.1/1.flv";
		listCount  = 1;
		return true;
	}
	
	void updateList(int type, String str)
	{
		mHandler.sendMessage(Message.obtain(mHandler, type, 0, 0, (Object)str));
	}

	
	int readDescr()
	{
		int ret = 0;
		int event;
		String text = null, name = null;
		try{
			XmlPullParserFactory xpf = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = xpf.newPullParser();
			xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);


			InputStream in = getApplicationContext().getAssets().open("temp.xml");	
			xpp.setInput(in, null);

			event = xpp.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String country, humidity; 
				name = xpp.getName();
				switch (event){
					case XmlPullParser.START_TAG:
						break;
					case XmlPullParser.TEXT:
						text = xpp.getText();
						break;

					case XmlPullParser.END_TAG:
						if(name.equals("country")){//<country>GB</country>
							country = text;
						}
						else if(name.equals("humidity")){//<humidity value="77" unit="%"/> 	
							humidity = xpp.getAttributeValue(null,"value");
						}
						break;
				}		 
				event = xpp.next(); 

			}

			in.close();
		}catch (Exception e){
			e.printStackTrace();
			ret = -1;
		}finally{
			return ret;	
		}
	}
}

