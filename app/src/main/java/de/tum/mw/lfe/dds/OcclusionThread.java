package de.tum.mw.lfe.dds;
/*
MIT License

        Copyright (c) 2015-2016 Michael Krause

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class OcclusionThread extends Thread {
	//dont forget: android manifest permission INTERNET 

	private static final String TAG = "DDS.OcclusionThread";		
	
    public static final int RETRY_AFTER_MS = 2000;//retry to reconnect after xxx ms
    public Handler mHandler;
    public Handler mCallbackHandler;//if dikablis return data we will forward it to this handler.
    public int mCallbackHandlerWhatValue;//msg value for callback.
    public String mIp = "192.168.1.18";
    public int mPort = 7009;	    
	public static final int CONNECT = 1;	    
	public static final int COMMAND = 2;	    
	public static final int CLOSE   = 3;
	
	
	public static final int OCCLUSION_START = 0; 
	public static final int OCCLUSION_STOP = 1; 
	public static final int OCCLUSION_TOGGLE = 2; 	
	
	private long mLastTry;//last time we tried to log in
    private boolean mEndThread = false;
    private boolean mRunClient = true;
	private Socket mClient = null;
	private PrintWriter mOut = null;
	private BufferedReader mIn = null;
	private int mTxCounter = 0;
	private int mRxCounter = 0;
    private boolean mError = false;
	private List<String> mCommands = new ArrayList<String>();
	private Handler mGuiHandler;
	
	public OcclusionThread(Handler guiHandler){
		mGuiHandler = guiHandler;
	}
	
	public boolean isConnected(){
		
		if (mClient == null) return false;
		
		if ((mClient != null) || (mClient.isConnected()) || (!mError)){
			return true;
		}else{
			return false;
		}
		
	}
	
    public void sendCommand(String command){
    	Log.d(TAG,"queued command:+ "+command);
    	synchronized(mCommands){
    		mCommands.add(command);
    	}
    }
    
    public void resetCommandQueue(){
    	synchronized(mCommands){
    		mCommands.clear();
    	}	
    }
    
    
    public void close(){
    	//synchronized(mClient){
    		try {      
			  if (mClient != null) {
				  //mClient.getOutputStream().close();
				  //mClient.getInputStream().close();
				  mOut.close();
				  mIn.close();
				  mClient.close();

			  }    
			} catch (IOException e) {
 				Log.e(TAG, "close failed: " +e.getMessage());					
			} 
    	//}
    }
    
    public void end(){
		  mRunClient = false;
		  mEndThread = true;
		  close();	
    }
    
    public void connect(String ip, int port){
    		try{
			   Log.d(TAG, "Try to connect to IP:"+ ip + " Port:" + Integer.toString(port) );			
 			   mClient = new Socket(ip, port);
 			   mClient.setTcpNoDelay(true);
 			   mOut = new PrintWriter(mClient.getOutputStream(),true);
 			   mIn = new BufferedReader(new InputStreamReader(mClient.getInputStream()));   
 			} catch(UnknownHostException e) {
 			   Log.e(TAG, "Unknown host: "+e.getMessage());
 			} catch(IOException e) {
 				Log.e(TAG, "No I/O: "+e.getMessage());
 			}	
    }
    
    @Override
    public void run() {
    	
    	Log.d(TAG, "Hello from OcclusionThread");
    	
    	resetCommandQueue();
    	
    	//-------prepare handler & looper--------------
        Looper.prepare();
        
        /*
        mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // Act on the message
                	// connect to IP/port
                	if (msg.what == CONNECT){
                		//TODO implement
                	}                    	
                	// add dikablis command to command queue----
                	if (msg.what == COMMAND){
                		String command = (String)msg.obj;
                		mCommands.add(command);
                	}
                	// close
                	if (msg.what == CLOSE){
                		//TODO implement
                	
                	}
                }
        };
        Looper.loop();
        */
        
        while(!mEndThread){
        	while(mRunClient){
        		  //connect-------------------------------
	    			if ((mClient == null) || (!mClient.isConnected()) || mError){
		    			try{	
		    				close();
		    				long now = System.currentTimeMillis();
		    				if (now - mLastTry > RETRY_AFTER_MS){//wait before try again
		    					connect(mIp, mPort);
		    					mLastTry = now;
		    				}
		    				
			    			if (mClient.isConnected()){
			    				Log.d(TAG, "Try to login");
				                //mOut.println("Luser,pass");
				                //mOut.flush();
				                mRxCounter++;
				                //TODO evaluate server response
				                mError = false;
			    			}else{
			    				Log.d(TAG, "connect: Nope");
			    				Thread.sleep(RETRY_AFTER_MS, 0);//if connection fails wait before try again			    				
			    			}        	
		    			}catch(Exception e){
			 				Log.e(TAG, "error while connecting : "+e.getMessage());
			 				try{
		    				Thread.sleep(RETRY_AFTER_MS, 0);//if connection fails wait before try again
			 				}catch(Exception ex){}
		    			}	
	    			}
	    			
	    			//send--------------
	    			try{
	    				synchronized(mCommands){
		    				if ((mClient.isConnected()) && (mCommands.size() > 0)){
			    				String firstCommandInQueue = mCommands.get(0);
				                mOut.println(firstCommandInQueue);
				                //TODO_MAN null termination for triggers
				                //mOut.print(firstCommandInQueue);
				                //mOut.print(0);
				                mOut.flush();
	    						Log.d(TAG,Integer.toString(mTxCounter)+" TX: "+firstCommandInQueue);
				                mTxCounter++;
				                mCommands.remove(0);//remove first in queue
		    				} 
	    				}
	    			}catch(Exception e){
		 				Log.e(TAG, "error in runClient send: " +e.getMessage());
		 				mError = true;
	    			}	
	    			//receive--------------
	    			try{
	    				int rxTemp=-1;
	    				if (mIn != null){
	    					rxTemp = mIn.read(); 
	    				}
	    				if (rxTemp != -1){
	    	    		    Message msg = mGuiHandler.obtainMessage();
	    	    		    msg.what = rxTemp;
	    	    		    mGuiHandler.sendMessage(msg);
	    				}
	    			}catch(Exception e){
		 				Log.e(TAG, "error in runClient receive: "+e.getMessage());
		 				mError = true;
	    			}	
	            }
        }//while (mRunClient)
        
        close();
        
    }//while(!mEndThread)
    
    
    public String getIp(){
    	return mIp;
    }
    
    public void setIp(String ip){
    	mIp = ip;
    }
 
    
    private void sendCommand(int eventType){
 		
 		String triggerString = "";
 		//-----------------------------
 	    switch(eventType){
 	    case OCCLUSION_START:
 	    	sendCommand('#');
 	    	break;
 	    case  OCCLUSION_STOP:
 	    	sendCommand('$');
 	    	break;
 	    case  OCCLUSION_TOGGLE:
 	    	sendCommand('t');
 	    	break;
 	    default:
 	    } 	 	}
    
    
}









