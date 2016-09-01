package de.tum.mw.lfe.dds;
/*
MIT License

        Copyright (c) 2015-2016 Michael Krause, Lorenz Prasch

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
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.tum.mw.lfe.dds.experimentWidgets.DirectTextEdit;
import de.tum.mw.lfe.dds.experimentWidgets.ExperimentWidget;
import de.tum.mw.lfe.dds.experimentWidgets.ListSelect;
import de.tum.mw.lfe.dds.experimentWidgets.ListSelectNoKinetic;
import de.tum.mw.lfe.dds.experimentWidgets.NumPad;
import de.tum.mw.lfe.dds.experimentWidgets.NumPicker;
import de.tum.mw.lfe.dds.experimentWidgets.NumPickerTap;
import de.tum.mw.lfe.dds.experimentWidgets.SliderNum;
import de.tum.mw.lfe.dds.experimentWidgets.SliderVis;
import de.tum.mw.lfe.dds.experimentWidgets.SpellerAbc;
import de.tum.mw.lfe.dds.experimentWidgets.SpellerNum;

public class ExperimentActivity extends Activity{
	private static final String TAG = "DDS.Activity";	

    private IntentFilter mFilter;
   
	private PowerManager.WakeLock mWakeLock = null;	
	private ExperimentActivity mContext = this;
	private Handler mHandler = new Handler();
    private ProgressDialog mProgress;
    private boolean mDelayInProgress = false;
	 	
	
	//experimentWidgets
	//public ArrayList<ExperimentWidget> mExpWidgets = new ArrayList<ExperimentWidget>();
	public ExperimentWidget[] mTasks;// = new ExperimentWidget[13];
	
	//LOGGING related	
	private File mLogFile=null;//logging file
	public static final String CSV_DELIMITER = ";"; //delimiter within csv
	public static final String CSV_LINE_END = "\r\n"; //line end in csv
	public static final String FOLDER = "DDS"; //folder
	public static final String FOLDER_DATE_STR = "yyyy-MM-dd";//logging folder format
	public static final String FILE_EXT = ".txt";
	public static final String HEADER ="timestamp;reason;data";    
    
	public static final byte TRIGGER_INSTRUCTION = 1;    
	public static final byte TRIGGER_DELAY = 2;    
	public static final byte TRIGGER_TRIAL1 = 3;    
	public static final byte TRIGGER_TRIAL2 = 4;    
	public static final byte TRIGGER_TRIAL3 = 5;  

  	private int mOldDikablisRetValue;
  	private byte mOldDikablisTriggerCondition;
  	private byte mOldDikablisTriggerTask;
  	private byte mOldDikablisTriggerSubtask;


	private SilabServerRunnable mSilabServerRunnable = null;
	private Thread mSilabServerThread = null;	

    private static DikablisThread mDikablisThread = null;
    private static OcclusionThread mOcclusionThread = null;
    
    // Silab Package to send
    private SilabPacket mSilabPacket = new SilabPacket();
    
    private static BtThread mBtThread = null;  
    
    // globale Schleifenvariable
    private static int mTaskNumber = 0; 
    
    // OK-Button (ruft mit onClickListener done() auf)
    private Button mOkButton;
    
    // Dismiss-Button (ruft mit onClickListener dismissOverlay() auf)
    private Button mDismissButton;
    
    // Overlay Content
    private ImageView mInstructionImage;
    private TextView mInstructionTitle;
    
    private TextView mTaskType;
    private TextView mTaskSubtask;
    private TextView mInTaskSubtask;
    
    // Configuration
    private Button mOkConfig;
    private boolean mIsTouch; //this is a touchscreen experiment
    
    // Experiment Arrays:
    public RelativeLayout mExperimentWidgetLayout;
    
    // Zahleneingabe Touchscreen - 2 Ziffern vs 5 Ziffern vs 10 Ziffern    
    public NumPad mNumPad42;
    public NumPad mNumPad80805;
    public NumPad mNumPad0171829683;
    
    // Listenauswahl - Position 5 vs Position 48 vs Position 97
	public ListSelect mListSelectBielefeld;
	public ListSelect mListSelectKarlsruhe;
	public ListSelect mListSelectWolfsburg;
	
	// Nummernspinner - 78
	public NumPicker mNumPicker;
	
	// Texteingabe - MÃ¼nchen
	public DirectTextEdit mDirectTextEdit;

	// Numerischer Slider - 10 vs 45 vs 90
	public SliderNum mSliderNum10;
	public SliderNum mSliderNum45;
	public SliderNum mSliderNum90;
	
	// Visueller Slider - 20% vs 60% vs 85%
	public SliderVis mSliderVis20;
	public SliderVis mSliderVis60;
	public SliderVis mSliderVis85;
	
	// Texteingabe per DDS - MÃ¼nchen
	public SpellerAbc mSpellerAbc;
	
    // Nummerneingabe per DDS - 2 Ziffern vs 5 Ziffern vs 10 Ziffern
	public SpellerNum mSpellerNum42;
    public SpellerNum mSpellerNum80805;
    public SpellerNum mSpellerNum0171829683;
	
    
    
    
    
    private void kickOffDikablisThread(){
		if (mDikablisThread == null){
			Log.d(TAG, "start dikablis thread");
			mDikablisThread = new DikablisThread(this, mDikablisHandler);
			mDikablisThread.start();
	    }
	} 
  
    private void kickOffOcclusionThread(){
		if (mOcclusionThread == null){
			Log.d(TAG, "start occlusion thread");
			mOcclusionThread = new OcclusionThread(mOcclusionGuiHandler);
			mOcclusionThread.start();
	    }
    }	
	    
    
  //-------------------------------------------------------------------
  	final Handler mSilabGuiHandler = new Handler(){
		  @Override
		  public void handleMessage(Message msg) {
			super.handleMessage(msg);
			    
			//RadioButton connectedRB = (RadioButton)findViewById(R.id.connectedRB);

		    if(msg.what==SilabServerRunnable.NOT_CONNECTED){
				//connectedRB.setChecked(false);
		    	Log.i(TAG,">>>> not connected");
		    }
		    if(msg.what==SilabServerRunnable.CONNECTED){
				//connectedRB.setChecked(true);
		    	Log.i(TAG,">>>>connected");
			}		    
		    if(msg.what==SilabServerRunnable.UPDATE_MARKER_TEXT){
		    	//String temp = new String(Byte.toString(mButton));//convert char to string
		    	//connectedRB.setText(temp);
			    
			}
			//TextView ip = (TextView)findViewById(R.id.ipTv);
			//ip.setText(mServerRunnable.ipStatus());

		  }
		} ;
		
	    private Runnable brightnessFull = new Runnable() {
			public void run() {
		        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 255); 
			} 			    	
	    };
	    private Runnable brightnessDim = new Runnable() {
			public void run() {
		        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 0);
		        mHandler.postDelayed(brightnessFull, 500);
			} 			    	
	    };	    
	    
	  	final Handler mOcclusionGuiHandler = new Handler(){
	  		  @Override
	  		  public void handleMessage(Message msg) {
	  			super.handleMessage(msg);
	  			    
	  			//RadioButton connectedRB = (RadioButton)findViewById(R.id.connectedRB);
	  			
	  		    if(msg.what== 'R'){//ready beacon
	  		    	Log.i(TAG,">>>>>ready");
	  		    }
	  		    if(msg.what=='#'){//start
	  		    	Log.i(TAG,">>>>>occ exp started");
	  		    	if (mLogFile != null) writeToLoggingFile("occ", "start");
	  		    }
	  		    if(msg.what=='$'){//stop
	  		    	Log.i(TAG,">>>>>occ exp stoped");
	  		    	if (mLogFile != null) writeToLoggingFile("occ", "stop");
	  		    }
	  		    if(msg.what=='o'){//open
	  		    	Log.i(TAG,">>>>>open");
	  		    	if (mLogFile != null) writeToLoggingFile("occ", "open");
	  		    }
	  		    if(msg.what=='c'){//close
	  		    	Log.i(TAG,">>>>>close");
	  		    	if (mLogFile != null) writeToLoggingFile("occ", "close");
	  		    	mHandler.post(brightnessDim);
	  		    }
	  		  }
	  		} ;    
	
	  	    private Handler mBtHandler = new Handler() {
	  	        public void handleMessage(Message msg) {
	  	            // Act on the message
	  	        	if (msg.what == BtThread.BT_RX_CALLBACK){

	  	        		String tx = (String)msg.obj;
	  	        		
	  	        		Log.i(TAG,">>>>>"+tx);
	  	        		
	  	    			if ((findViewById(R.id.InstructionLayout).getVisibility() == View.VISIBLE) && tx.equals("#PP")) {
	  	    				dismissInstructionOverlay();
	  	    			}else{
	  	    				if ((mTaskNumber < mTasks.length)  && //within array
	  	    					 (!mDelayInProgress)){ //delay not in progress
	  	    					
	  	    					mTasks[mTaskNumber].dispatcher(tx);//forward tx-message to current mTask	  	    				
	  	    				}
	  	    			}
	  	    			
	  	        	}                    	

	  	        }
	  	    }; 
	  	  private Handler mDikablisHandler = new Handler() {
	  	        public void handleMessage(Message msg) {
	  	            // Act on the message
	  	        	// connect to IP/port
	  	        	int dikablisFrame;
	  	        	if (msg.what == DikablisThread.DIKABLIS_FRAMENUMBER){
	  	        		String temp = (String)msg.obj;
	  	        		try{
	  	        		   dikablisFrame = Integer.parseInt(temp);
    		        	}
			        	catch(Exception ex){
			        		dikablisFrame = -1;
			        		Log.e(TAG, "Failed to convert Dikablis frameCount: "+ex.getMessage());
			        	}	
	  	        			mSilabPacket.dikablisFrame = dikablisFrame;
		  	        	if (mSilabServerRunnable != null){
	  	        			mSilabServerRunnable.send2Silab(mSilabPacket);
		  	        	}
	  	        	}                    	

	  	        }
	  	    };      

    private void stopBtThread(){
	    if (mBtThread != null){
	    	mBtThread.end();
	    	mBtThread = null;
	    } 		 
    }
    
    
    private void kickOffBtThread(){
		if (mBtThread == null){
			Log.d(TAG, "start mBtThread");
			mBtThread = new BtThread(this, mBtHandler);
			mBtThread.start();
	    }
	} 
	   
 
  //The BroadcastReceiver that listens for bluetooth broadcasts
   private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
               //Device found
        		if(BtThread.checkIfMyDevice(device)){
        			stopBtThread();
        			kickOffBtThread();
        		}
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
               //Device is now connected
        		if(BtThread.checkIfMyDevice(device)){
        			stopBtThread();
        			kickOffBtThread();
        		}           	            	
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
               //Device is about to disconnect
        		if(BtThread.checkIfMyDevice(device)){
        			stopBtThread();
        			kickOffBtThread();
        		}
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
               //Device has disconnected
        		if(BtThread.checkIfMyDevice(device)){
        			stopBtThread();
        			kickOffBtThread();
        		}
            }           
        }
    };	
	
    
    // randomize the Array of ExpWidgets
    static void shuffleArray(ExperimentWidget[] ar)
	  {
	    Random rnd = new Random();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      ExperimentWidget a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
    
//    // Show the tasks out of the Taskarray
//    static void showTask(ExperimentWidget tsk) {  	
//    	tsk.show();
//    	int len = new java.util.Scanner(System.in).nextInt();
//    	taskNumber++;
//    	tsk.hide();
//    }

    
    public void  refreshInstructionOverlay(){
    	
 		mTaskType.setText(mTasks[mTaskNumber].getName());
 		int trial = mTasks[mTaskNumber].getDesiredResultsCursor();
 		String desiredResult = mTasks[mTaskNumber].getDesiredResults()[trial];
 		mTaskSubtask.setText( desiredResult );
 		mInTaskSubtask.setText("Gewünschte Eingabe: "+ desiredResult );
 		
    }

    private void registerBtEventReceiver(){
        mFilter = new IntentFilter();
        mFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mFilter.addAction(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, mFilter);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
        //no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //full light
        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 255); 
		
        
        mTaskNumber = 0;
        
		
	    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.main);
				
	    getWakeLock();
		
        kickOffBtThread();
        
        //get Layout for Widgets
        mExperimentWidgetLayout = (RelativeLayout)findViewById(R.id.RelativeLayoutExperimentWidget);
        
        // get dismiss-Button
        mDismissButton = (Button) findViewById(R.id.OkButtonOverlay);
                
        // OnClickListener fÃ¼r dismiss-Button
        mDismissButton.setOnClickListener (new View.OnClickListener() {
        	public void onClick (View v) {
        		
        		dismissInstructionOverlay();
        		 		
        	}
        });


        
		mTasks = new ExperimentWidget[] {//foo dummy task
				new NumPad((byte) 1, this, mExperimentWidgetLayout, "foo", new String[]{"bar"}, 0, ExperimentWidget.DELAY_NOINDICATION)
		};
		
        // get OK-Button
        mOkButton = (Button) findViewById(R.id.OkButton);
                
        // OnClickListener fÃ¼r OK-Button
        mOkButton.setOnClickListener (new View.OnClickListener() {
        	public void onClick (View v) {
        		done(mTasks[mTaskNumber]);
        	}
        });

	        
	    
	    //instruction screen
 		//mInstructionImage = (ImageView) findViewById(R.id.InstructionImage);
 		mInstructionTitle = (TextView) findViewById(R.id.InstructionTitle);
 		mTaskType = (TextView) findViewById(R.id.TaskType);
 		mTaskSubtask = (TextView) findViewById(R.id.TaskSubtask);
 		mInTaskSubtask = (TextView) findViewById(R.id.inTaskSubtask);

	    
	    // Configuration-Screen
        mOkConfig = (Button) findViewById(R.id.OkButtonConfiguration);
        mOkConfig.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				mTasks[0].dismiss();//TODO strange
				
				RadioGroup radioButtonGroup = (RadioGroup) findViewById(R.id.ConditionPicker);
				int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
				View radioButton = radioButtonGroup.findViewById(radioButtonID);
				int idx = radioButtonGroup.indexOfChild(radioButton);
				if (idx == 0) {
					//mInstructionImage.setImageResource(R.drawable.touch);
			    	mInstructionTitle.setText(R.string.Touch);
			    	mIsTouch = true;
				} else {
				    //mInstructionImage.setImageResource(R.drawable.dds);
			    	mInstructionTitle.setText(R.string.DDS);
			    	mIsTouch = false;
				}

				
				//subject
				EditText vpNumberEditText = (EditText) findViewById(R.id.VPNumber);
				String temp = vpNumberEditText.getText().toString();
				int vp;
        		try{
        			vp = Integer.parseInt(temp);
	        	}
		        	catch(Exception ex){
		        		vp = -1;
		        		Log.e(TAG, "Failed to convert VPNumber: "+ex.getMessage());
		        	}

				mSilabPacket.subject = (byte)vp;
				
				
				//condition
				Spinner stageSpinner = (Spinner) findViewById(R.id.StageSpinner);
				mSilabPacket.stage = (byte)(stageSpinner.getSelectedItemPosition() +1); //+1 !!! important so dikablis triggers start with 1
				
				
				findViewById(R.id.ConfigurationScreen).setVisibility(View.GONE);
				
				initArray();
				
				refreshInstructionOverlay();
				
				prepareLogging();
				
				sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_INSTRUCTION, true, false);//first trigger
			}
		});
	    
        /*
		if (mTasks[mTaskNumber].getNeedsOkButton()) {
			mOkButton.setVisibility(View.VISIBLE);
	    	
	    	mInstructionImage.setImageResource(R.drawable.touch);
	    	mInstructionTitle.setText(R.string.Touch);
	    } else {
	    	mOkButton.setVisibility(View.GONE);
	    	
	    	mInstructionImage.setImageResource(R.drawable.dds);
	    	mInstructionTitle.setText(R.string.DDS);
	    }
 		
	    if (mTaskNumber > (mTasks.length)) {
    		finish();
		} else {
			mTasks[mTaskNumber].show();
			
		}
		*/
	    
	}
	
	public void initArray(){
		
		if (mIsTouch) {//touch 
			
			if (mSilabPacket.stage == 1){//eingewöhnung
				mTasks = new ExperimentWidget[] {


						//delay determined 2,4,8 seconds
						new NumPad((byte) 1, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"23"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 2, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"80333"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 3, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"63"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPad((byte) 4, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"85386"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 5, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"73"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 6, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"82"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPad((byte) 7, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"80852"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 8, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"70"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 9, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"42"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//number input 3, 5, 10
						new NumPad((byte)10, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"429","618","286"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)11, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"50383","71579"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)12, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"049 319 8023"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)13, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Bielefeld","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)14, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Koblenz","Minden"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)15, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Witten","Ulm"}, 0, ExperimentWidget.DELAY_NONE),
						//list select middle, end
						new ListSelectNoKinetic((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Krefeld","Minden"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelectNoKinetic((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Velbert","Witten"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker TAP ~2, ~4, ~8
						new NumPickerTap((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"48 (TAP)","52 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"46 (TAP)","55 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"42 (TAP)","58 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ROLL ~2, ~4, ~8
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"47 (ROLL)","51 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)22, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"45 (ROLL)","54 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)23, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"43 (ROLL)","59 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)24, this, mExperimentWidgetLayout, "Slider mit Nummernangabe", new String[]{"50","35","70"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)25, this, mExperimentWidgetLayout, "Slider graphisch", new String[]{"70%","50%","35%"}, 0, ExperimentWidget.DELAY_NONE),
						//text input 2,4,8 chars
						new DirectTextEdit((byte)26, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"ab","xy","wa"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)27, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"dorf","post"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)28, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"kontrast"}, 0, ExperimentWidget.DELAY_NONE)

				};
			}
			if (mSilabPacket.stage == 2){//occlusion
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPad((byte) 1, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"24"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 2, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"50764"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 3, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"63"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPad((byte) 4, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"08346"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 5, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"87"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 6, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"56"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPad((byte) 7, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"97301"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 8, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"69"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 9, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"54"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//number input 3, 5, 10
						new NumPad((byte)10, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"739","618","927"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)11, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"80415","17249"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)12, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"0170 361 602"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)13, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Bochum","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)14, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Moers","Mönchengladbach"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)15, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Ulm","Würzburg"}, 0, ExperimentWidget.DELAY_NONE),
						//list select middle, end
						new ListSelectNoKinetic((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Potsdam","Recklinghausen"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelectNoKinetic((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Stuttgart","Tübingen"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker TAP ~2, ~4, ~8
						new NumPickerTap((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"48 (TAP)","52 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"54 (TAP)","46 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"42 (TAP)","58 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ROLL ~2, ~4, ~8
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"52 (ROLL)","48 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)22, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"46 (ROLL)","54 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)23, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"58 (ROLL)","42 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)24, this, mExperimentWidgetLayout, "Slider mit Nummernangabe", new String[]{"55","20","85"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)25, this, mExperimentWidgetLayout, "Slider graphisch", new String[]{"55%","20%","85%"}, 0, ExperimentWidget.DELAY_NONE),
						//text input 2,4,8 chars
						new DirectTextEdit((byte)26, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"ce","pa","ki"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)27, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"kurz","text"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)28, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"bankomat"}, 0, ExperimentWidget.DELAY_NONE)
				};
			}
			if (mSilabPacket.stage == 3){//baseline
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPad((byte) 1, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"37"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 2, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"60974"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 3, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"51"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPad((byte) 4, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"97824"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 5, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"39"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 6, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"47"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPad((byte) 7, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"67834"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 8, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"72"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 9, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"17"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//number input 3, 5, 10
						new NumPad((byte)10, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"143","645","806"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)11, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"73928","48294"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)12, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"049 310 8026"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)13, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Berlin","Bochum"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)14, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Lübeck","Magdeburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)15, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Witten","Wiesbaden"}, 0, ExperimentWidget.DELAY_NONE),
						//list select middle, end
						new ListSelectNoKinetic((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Leipzig","Krefeld"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelectNoKinetic((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Wuppertal","Ulm"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker TAP ~2, ~4, ~8
						new NumPickerTap((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"48 (TAP)","52 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"54 (TAP)","46 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"42 (TAP)","58 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ROLL ~2, ~4, ~8
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"52 (ROLL)","48 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)22, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"46 (ROLL)","54 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)23, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"58 (ROLL)","42 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)24, this, mExperimentWidgetLayout, "Slider mit Nummernangabe", new String[]{"90","25","50"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)25, this, mExperimentWidgetLayout, "Slider graphisch", new String[]{"90%","25%","50%"}, 0, ExperimentWidget.DELAY_NONE),
						//text input 2,4,8 chars
						new DirectTextEdit((byte)26, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"ui","mo","he"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)27, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"juni","haus"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)28, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"zylinder"}, 0, ExperimentWidget.DELAY_NONE)
				};
			}
			if (mSilabPacket.stage == 4){//DRT
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPad((byte) 1, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"12"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 2, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"70604"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 3, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"89"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPad((byte) 4, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"38025"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 5, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"56"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 6, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"20"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPad((byte) 7, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"34902"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 8, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"43"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 9, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"11"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//number input 3, 5, 10
						new NumPad((byte)10, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"934","101","429"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)11, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"75098","39206"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)12, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"0171 872 097"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)13, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Bochum","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)14, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Magdeburg","Koblenz"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)15, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Velbert","Wolfsburg"}, 0, ExperimentWidget.DELAY_NONE),
						//list select middle, end
						new ListSelectNoKinetic((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Moers","Minden"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelectNoKinetic((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Würzburg","Solingen"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker TAP ~2, ~4, ~8
						new NumPickerTap((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"48 (TAP)","52 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"54 (TAP)","46 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"42 (TAP)","58 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ROLL ~2, ~4, ~8
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"52 (ROLL)","48 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)22, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"46 (ROLL)","54 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)23, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"58 (ROLL)","42 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)24, this, mExperimentWidgetLayout, "Slider mit Nummernangabe", new String[]{"20","45","85"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)25, this, mExperimentWidgetLayout, "Slider graphisch", new String[]{"20%","45%","85%"}, 0, ExperimentWidget.DELAY_NONE),
						//text input 2,4,8 chars
						new DirectTextEdit((byte)26, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"ku","et","of"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)27, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"ende","park"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)28, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"fabrikat"}, 0, ExperimentWidget.DELAY_NONE)
				};
			}
			if (mSilabPacket.stage == 5){//AAM driving+dikablis
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPad((byte) 1, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"66"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 2, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"90872"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPad((byte) 3, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"24"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPad((byte) 4, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"57303"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 5, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"90"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPad((byte) 6, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"14"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPad((byte) 7, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"65278"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 8, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"28"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPad((byte) 9, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"96"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//number input 3, 5, 10
						new NumPad((byte)10, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"721","904","792"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)11, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"18365","40192"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPad((byte)12, this, mExperimentWidgetLayout, "Nummerneingabe per Touchscreen", new String[]{"189 101 7350"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)13, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Bielefeld","Berlin"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)14, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Mülheim an der Ruhr","Neuss"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)15, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Trier","Wolfsburg"}, 0, ExperimentWidget.DELAY_NONE),
						//list select middle, end
						new ListSelectNoKinetic((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Krefeld","Minden"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelectNoKinetic((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Würzburg","Wiesbaden"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker TAP ~2, ~4, ~8
						new NumPickerTap((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"48 (TAP)","52 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"54 (TAP)","46 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPickerTap((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"42 (TAP)","58 (TAP)"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ROLL ~2, ~4, ~8
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"52 (ROLL)","48 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)22, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"46 (ROLL)","54 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)23, this, mExperimentWidgetLayout, "Auswahl einer Zahl", new String[]{"58 (ROLL)","42 (ROLL)"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)24, this, mExperimentWidgetLayout, "Slider mit Nummernangabe", new String[]{"85","50","35"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)25, this, mExperimentWidgetLayout, "Slider graphisch", new String[]{"85%","50%","35%"}, 0, ExperimentWidget.DELAY_NONE),
						//text input 2,4,8 chars
						new DirectTextEdit((byte)26, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"te","di","wc"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)27, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"info","oder"}, 0, ExperimentWidget.DELAY_NONE),
						new DirectTextEdit((byte)28, this, mExperimentWidgetLayout, "Eingabe eines Wortes", new String[]{"tagebuch"}, 0, ExperimentWidget.DELAY_NONE)
				};
			}
			
		} else { // Dreh Drück Steller / Rotary
			
			
			if (mSilabPacket.stage == 1){//eingewöhnung
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPicker((byte) 1, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"44"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 2, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"52"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 3, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"25"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPicker((byte) 4, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"65"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 5, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"39"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 6, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"47"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPicker((byte) 7, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"59"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 8, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"28"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 9, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"68"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//num speller 3,5,10
						new SpellerNum((byte)10, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"235","489","205"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)11, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"87596","59635"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)12, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"021 369 7512"}, 0, ExperimentWidget.DELAY_NONE),
						//char speller 2, 4, 8
						new SpellerAbc((byte)13, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"LP","BH","ES"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)14, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"WAND","HAUS"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)15, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"COMPUTER"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Bielefeld","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Minden","Mülheim an der Ruhr"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)18, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Ulm","Würzburg"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ~2, ~4, ~8
						new NumPicker((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"48","52"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"54","46"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"42","58"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)22, this, mExperimentWidgetLayout, "Slider mit Nummernangabe (Dreh-Drücksteller)", new String[]{"50","90","35"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)23, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"50%","90%","35%"}, 0, ExperimentWidget.DELAY_NONE),
				};
			}
			if (mSilabPacket.stage == 2){//occlusion
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPicker((byte) 1, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"55"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 2, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"69"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 3, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"24"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPicker((byte) 4, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"39"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 5, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"58"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 6, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"47"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPicker((byte) 7, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"69"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 8, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"37"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 9, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"59"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//num speller 3,5,10
						new SpellerNum((byte)10, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"153","761","964"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)11, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"35693","75986"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)12, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"031 745 7548"}, 0, ExperimentWidget.DELAY_NONE),
						//char speller 2, 4, 8
						new SpellerAbc((byte)13, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"PE","AS","RI"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)14, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"HANS","MAUS"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)15, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"FAHRZEUG"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Bielefeld","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Magdeburg","Moers"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)18, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Wolfsburg","Ulm"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ~2, ~4, ~8
						new NumPicker((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"52","48"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"46","54"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"58","42"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)22, this, mExperimentWidgetLayout, "Slider mit Nummernangabe (Dreh-Drücksteller)", new String[]{"90","45","25"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)23, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"90%","45%","25%"}, 0, ExperimentWidget.DELAY_NONE),
				};
			}
			if (mSilabPacket.stage == 3){//baseline
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPicker((byte) 1, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"47"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 2, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"55"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 3, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"26"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPicker((byte) 4, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"59"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 5, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"19"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 6, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"51"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPicker((byte) 7, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"37"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 8, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"46"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 9, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"68"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//num speller 3,5,10
						new SpellerNum((byte)10, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"145","439","759"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)11, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"34975","12458"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)12, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"0156 7951 43"}, 0, ExperimentWidget.DELAY_NONE),
						//char speller 2, 4, 8
						new SpellerAbc((byte)13, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"BO","ZU","AU"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)14, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"DORF","POST"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)15, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"TANZKURS"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Augsburg","Bochum"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Koblenz","Krefeld"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)18, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Velbert","Ulm"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ~2, ~4, ~8
						new NumPicker((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"52","48"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"46","54"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"58","42"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)22, this, mExperimentWidgetLayout, "Slider mit Nummernangabe (Dreh-Drücksteller)", new String[]{"50","90","25"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)23, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"50%","90%","25%"}, 0, ExperimentWidget.DELAY_NONE),
				};
			}
			if (mSilabPacket.stage == 4){//DRT
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPicker((byte) 1, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"23"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 2, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"33"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 3, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"63"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPicker((byte) 4, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"47"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 5, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"40"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 6, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"62"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPicker((byte) 7, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"59"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 8, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"35"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 9, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"42"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//num speller 3,5,10
						new SpellerNum((byte)10, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"420","639","528"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)11, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"84747","71579"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)12, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"089 319 8023"}, 0, ExperimentWidget.DELAY_NONE),
						//char speller 2, 4, 8
						new SpellerAbc((byte)13, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"MA","CE","TI"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)14, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"HAND","EURO"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)15, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"HOCHHAUS"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Bochum","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Mannheim","Lünen"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)18, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Velbert","Trier"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ~2, ~4, ~8
						new NumPicker((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"52","48"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"46","54"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"58","42"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)22, this, mExperimentWidgetLayout, "Slider mit Nummernangabe (Dreh-Drücksteller)", new String[]{"90","55","20"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)23, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"90%","55%","20%"}, 0, ExperimentWidget.DELAY_NONE),
				};
			}
			if (mSilabPacket.stage == 5){//AAM driving+dikablis
				mTasks = new ExperimentWidget[] {
						//delay determined 2,4,8 seconds
						new NumPicker((byte) 1, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"71"}, 2000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 2, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"56"}, 4000, ExperimentWidget.DELAY_DETERMINED),
						new NumPicker((byte) 3, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"24"}, 8000, ExperimentWidget.DELAY_DETERMINED),
						//delay indetermined 2,4,8 seconds
						new NumPicker((byte) 4, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"49"}, 2000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 5, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"62"}, 4000, ExperimentWidget.DELAY_INDETERMINED),
						new NumPicker((byte) 6, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"23"}, 8000, ExperimentWidget.DELAY_INDETERMINED),
						//delay no indication 2,4,8 seconds
						new NumPicker((byte) 7, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"65"}, 2000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 8, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"55"}, 4000, ExperimentWidget.DELAY_NOINDICATION),
						new NumPicker((byte) 9, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"62"}, 8000, ExperimentWidget.DELAY_NOINDICATION),
						//num speller 3,5,10
						new SpellerNum((byte)10, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"425","175","345"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)11, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"95648","47514"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerNum((byte)12, this, mExperimentWidgetLayout, "Eingabe einer Zahl per Dreh-Drücksteller", new String[]{"0195 275 457"}, 0, ExperimentWidget.DELAY_NONE),
						//char speller 2, 4, 8
						new SpellerAbc((byte)13, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"AM","HA","DE"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)14, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"BANK","RAUM"}, 0, ExperimentWidget.DELAY_NONE),
						new SpellerAbc((byte)15, this, mExperimentWidgetLayout, "Eingabe von Buchstaben per Dreh-Drücksteller", new String[]{"TESAFILM"}, 0, ExperimentWidget.DELAY_NONE),
						//list select first page, middle, end
						new ListSelect((byte)16, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Bergisch Gladbach","Bielefeld"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)17, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Neuss","Oberhausen"}, 0, ExperimentWidget.DELAY_NONE),
						new ListSelect((byte)18, this, mExperimentWidgetLayout, "Auswahl aus einer Liste per Dreh-Drücksteller", new String[]{"Solingen","Wiesbaden"}, 0, ExperimentWidget.DELAY_NONE),
						//numpicker ~2, ~4, ~8
						new NumPicker((byte)19, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"52","48"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)20, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"46","54"}, 0, ExperimentWidget.DELAY_NONE),
						new NumPicker((byte)21, this, mExperimentWidgetLayout, "Auswahl einer Zahl per Dreh-Drücksteller", new String[]{"58","42"}, 0, ExperimentWidget.DELAY_NONE),
						//slider num
						new SliderNum((byte)22, this, mExperimentWidgetLayout, "Slider mit Nummernangabe (Dreh-Drücksteller)", new String[]{"50","20","85"}, 0, ExperimentWidget.DELAY_NONE),
						//slider vis
						new SliderVis((byte)23, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"50%","20%","85%"}, 0, ExperimentWidget.DELAY_NONE),
				};
			}
			
			
		}
			
/*
		mTasks = new ExperimentWidget[] {
				//delay determined 2,4,8 seconds
				//new ListSelectNoKinetic((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl (TAP)", new String[]{"München", "Aachen", "Bremen"}, 0, ExperimentWidget.DELAY_NONE),
				//new SliderVis((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl (TAP)", new String[]{"50%"}, 0, ExperimentWidget.DELAY_NONE),
				//new NumPicker((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl (TAP)", new String[]{"1","2"}, 8000, ExperimentWidget.DELAY_DETERMINED),
				//new NumPicker((byte)18, this, mExperimentWidgetLayout, "Auswahl einer Zahl (TAP)", new String[]{"48 (TAP)","52","47"}, 0, ExperimentWidget.DELAY_NONE),
				new SliderVis((byte)23, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"70%","50%","35%"}, 0, ExperimentWidget.DELAY_NONE)
                //new SliderNum((byte)24, this, mExperimentWidgetLayout, "Slider graphisch (Dreh-Drücksteller)", new String[]{"70","50","35"}, 0, ExperimentWidget.DELAY_NONE)
				//new ListSelect((byte)12, this, mExperimentWidgetLayout, "Auswahl aus einer Liste", new String[]{"Bielefeld","Augsburg"}, 0, ExperimentWidget.DELAY_NONE),
		};
*/
		
		shuffleArray(mTasks);
	
	}
	
	//logic: save last trigger in mOld
    // when new start trigger is issued send stop trigger with mOld
	public void sendTrigger(byte event, byte subtask, boolean isTheExperimentFirstTrigger, boolean isTheExperimentFinalTrigger){
		int trig = -1;
		
		if((event == DikablisThread.DIKABLIS_EVENT_START) && (!isTheExperimentFirstTrigger)){//send stop trigger for old intervall;
			if (mDikablisThread != null) {
				trig = mDikablisThread.sendDikablisTrigger(DikablisThread.DIKABLIS_EVENT_STOP, mOldDikablisTriggerCondition, mOldDikablisTriggerTask, mOldDikablisTriggerSubtask, (byte)0);
			}
		}
				

        if(isTheExperimentFinalTrigger){
            mSilabPacket.dikablisTrigger = 0;

            if (mSilabServerRunnable != null){
                mSilabServerRunnable.send2Silab(mSilabPacket);
            }
            return;//send no start trigger if experiment ended
        }

		byte condition;		
		if (mIsTouch){
			condition = 1;
		}else{
			condition = 2;			
		}
		
		byte task = 0;
		if (!isTheExperimentFinalTrigger){
			task = mTasks[mTaskNumber].getDikablisNumber();			
		}else{
			task = 0;
		}
		
		if (mDikablisThread != null) {
			trig = mDikablisThread.sendDikablisTrigger(event, condition, task, subtask, (byte)0);
		}
		
		mSilabPacket.dikablisTrigger = trig;
		
      	if (mSilabServerRunnable != null){
  			mSilabServerRunnable.send2Silab(mSilabPacket);
      	}
      	
      	mOldDikablisTriggerCondition = condition;
      	mOldDikablisTriggerTask = task;
      	mOldDikablisTriggerSubtask = subtask;
      	
      	mOldDikablisRetValue = trig;//save old ret value for data logging

      	
      	//write some data to log file
      	if(event == DikablisThread.DIKABLIS_EVENT_START){
    		//data logging
    		String data = Integer.toString(trig);
    	    writeToLoggingFile("startTrigger", data);
            //toasting(Integer.toString(trig),3000);//TODO just DEMO
      	}
      	
      	
	}
	
	  @Override
	    protected void onStop() {
	        super.onStop();
	        

	    }	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//this.unregisterReceiver(mReceiver);
		
		stopBtThread();
		
        if(mWakeLock != null){
         	mWakeLock.release();
        }
		
	}
	
	@Override
	public void onPause() {
        super.onPause();
	    //unregisterReceiver(mReceiver);
        
        if (mLogFile != null) writeToLoggingFile("app", "pause");
        try {
            this.unregisterReceiver(mReceiver);//new
        }catch(Exception e){Log.d(TAG, "failed unreceiver receiver");}
        mReceiver=null;

        stopBtThread();//new

	    if (mDikablisThread != null){
	    	mDikablisThread.end();
	    	mDikablisThread = null;
	    }   
        
	    if (mOcclusionThread != null){
	    	mOcclusionThread.end();
	    	mOcclusionThread = null;
	    }

        stopSilabServer();//new

        finish();//new
        
	}
	
	
	@Override
	public void onResume() {
        super.onResume();

        if (mLogFile != null) writeToLoggingFile("app", "resume");

        kickOffBtThread();//new
        registerBtEventReceiver();//new

        kickOffDikablisThread();
        kickOffOcclusionThread();
        
        startSilabServer();
        
	}
	
   protected void getWakeLock(){
	    try{
			PowerManager powerManger = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        mWakeLock = powerManger.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.FULL_WAKE_LOCK, "de.tum.ergonomie.buttons");
	        mWakeLock.acquire();
		}catch(Exception e){
       	Log.e(TAG,"get wakelock failed:"+ e.getMessage());
		}	
   }
   
   //----SILAB--------
	public void startSilabServer(){
		if (mSilabServerRunnable == null){
			mSilabServerRunnable = new SilabServerRunnable(mSilabGuiHandler);
		}
		if (mSilabServerThread == null){
			mSilabServerThread = new Thread(mSilabServerRunnable);
			mSilabServerThread.start();
		}
		
		//TextView ip = (TextView)findViewById(R.id.ipTv);
	    //ip.setText(mSilabServerRunnable.ipStatus());
	}
	
	public void stopSilabServer(){
        try {
        	if (mSilabServerThread != null) mSilabServerThread.interrupt();
        	if (mSilabServerRunnable != null) mSilabServerRunnable.closeSockets();
        } catch (Exception e) {
			Log.e(TAG, "mServerThread.interrupt() failed: " + e.getMessage());
        }
        mSilabServerRunnable = null;//new
        mSilabServerThread = null;//new
	}
   
   

	//---LOGGING--------------------	
	public void prepareLogging(){

		File folder = null;
		SimpleDateFormat  dateFormat = new SimpleDateFormat(FOLDER_DATE_STR);
		String folderTimeStr =  dateFormat.format(new Date());
		String timestamp = Long.toString(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
		String subject = Byte.toString(mSilabPacket.subject);
		String stage = Byte.toString(mSilabPacket.stage);
		String mode;
		
		if (mIsTouch){
			mode = "t";
		}else{
			mode = "r";
		}
		
	   try{
		   //try to prepare external logging
		   String folderStr = Environment.getExternalStorageDirectory () + File.separator + FOLDER + File.separator + folderTimeStr;
		   mLogFile = new File(folderStr, timestamp+"_VP"+subject+"_VB"+stage+"_"+mode + FILE_EXT);
		   folder = new File(folderStr);
		   folder.mkdirs();//create missing dirs
		   mLogFile.createNewFile();
		   if (!mLogFile.canWrite()) throw new Exception();
	   }catch(Exception e){
		   try{
	    	   error("maybe no SD card inserted");//toast
			   finish();//we quit. we will not continue without file logging

			   //we do not log to internal memory, its not so easy to get the files back, external is easier via usb mass storage
			   /*
			   //try to prepare internal logging
				File intfolder = getApplicationContext().getDir("data", Context.MODE_WORLD_WRITEABLE);
				String folderStr = intfolder.getAbsolutePath() + File.separator + folderTimeStr;
				toasting("logging internal to: " +folderStr, Toast.LENGTH_LONG);
				file = new File(folderStr, timestamp + FILE_EXT);
			    folder = new File(folderStr);
			    folder.mkdirs();//create missing dirs
				file.createNewFile();
				if (!file.canWrite()) throw new Exception();
				*/
		   }catch(Exception e2){
			   mLogFile= null;
	    	   error("exception during prepareLogging(): " + e2.getMessage());//toast
			   finish();//we quit. we will not continue without file logging
		   }//catch(Exception e2)
	   }//catch(Exception e)	   
		   
		
	   try{
		String header = HEADER + CSV_DELIMITER + getVersionString() + "\r\n";
	    byte[] headerBytes = header.getBytes("US-ASCII");
		writeToFile(headerBytes,mLogFile);

	   }catch(Exception e3){
		   error("error writing header: "+e3.getMessage());//toast
		   finish();//we quit. we will not continue without file logging
	   }		   
		   
	}	

	
	public void writeToLoggingFile(String reason, String data){
		 StringBuilder log = new StringBuilder(2048);
		 log.append(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
		 log.append(CSV_DELIMITER);
		 log.append(reason);
		 log.append(CSV_DELIMITER);
		 log.append(data);
		 log.append(CSV_LINE_END);
		 		 
		 
		   try{
			   String tempStr = log.toString();
			    byte[] bytes = tempStr.getBytes("US-ASCII");
				writeToFile(bytes,mLogFile);
			   }catch(Exception e){
				   error("error writing log data: "+e.getMessage());//toast
				   finish();//we quit. we will not continue without file logging
			   }		
	}
	
	public void writeToFile(byte[] data, File file){
		   		       		
   		if (data == null){//error
       		error("writeFile() data==null?!");
       		finish();//we quit. we will not continue without proper file logging
   		}
   		
		FileOutputStream dest = null; 
							
		try {
			dest = new FileOutputStream(file, true);
			dest.write(data);
		}catch(Exception e){
			error("writeFile() failed. msg: " + e.getMessage());
       		finish();//we quit. we will not continue without file logging
			
		}finally {
			try{
				dest.flush();
				dest.close();
			}catch(Exception e){}
		}
		
		return;
   }	
	
	private void error(final String msg){//toast and log some errors
		toasting(msg, Toast.LENGTH_LONG);
		Log.e(TAG,msg);
	}
	
	private void toasting(final String msg, final int duration){
		Context context = getApplicationContext();
		CharSequence text = msg;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();		
	}
	
	private String getVersionString(){
		String retString = "";
		String appVersionName = "";
		int appVersionCode = 0;
		try{
			appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
			appVersionCode= getPackageManager().getPackageInfo(getPackageName(), 0 ).versionCode;
		}catch (Exception e) {
			Log.e(TAG, "getVersionString failed: "+e.getMessage());
		 }
		
		retString = "V"+appVersionName+"."+appVersionCode;
		
		return retString;
	}

	
    private Runnable delayedShow = new Runnable() {
		public void run() {
			mDelayInProgress = false;
		    mProgress.dismiss();
		    mOkButton.setVisibility(View.VISIBLE);
		    mTasks[mTaskNumber].show();
			sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_TRIAL1, false, false);
		} 			    	
    };	
    
	public static final byte UPDATE_PROGRESS_MS = 50;
	private long mLastDelayedUpdateTime = 50;
	
    private Runnable updateProgess = new Runnable() {
 		public void run() {
 			long now = System.currentTimeMillis();
 			mProgress.incrementProgressBy((int)(now - mLastDelayedUpdateTime));//+10 sligthly adjust for time drift
 			mLastDelayedUpdateTime = now;
 			if (mProgress.isShowing()){
 				mHandler.postDelayed(updateProgess,UPDATE_PROGRESS_MS);
 			}
 			
 		} 			    	
     };   
	
	public void dismissInstructionOverlay(){
		findViewById(R.id.InstructionLayout).setVisibility(View.GONE);
		
		if(mTasks[mTaskNumber].getDelay() > 0){
			mDelayInProgress = true;
			mOkButton.setVisibility(View.GONE);
			sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_DELAY, false, false);
			mHandler.postDelayed(delayedShow,mTasks[mTaskNumber].getDelay());//start with delay the show() command 

	        //mProgress = new ProgressDialog(this,R.style.myProgressBar);
	        mProgress = new ProgressDialog(this,ProgressDialog.THEME_TRADITIONAL);
	        mProgress.setCancelable(false);
	        mProgress.setCanceledOnTouchOutside(false);
	        
		    switch (mTasks[mTaskNumber].getDelayMode() ) {
	    	case ExperimentWidget.DELAY_DETERMINED:
			    mProgress.setProgress(0);
			    mProgress.setProgressNumberFormat(null);
			    mProgress.setIndeterminate(false);
			    mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			    mProgress.setMax((int)mTasks[mTaskNumber].getDelay());//delay max ~30 sec!!!
			    mProgress.show();
			    mLastDelayedUpdateTime = System.currentTimeMillis();
			    mHandler.postDelayed(updateProgess,UPDATE_PROGRESS_MS);
	    		break;
	    	case ExperimentWidget.DELAY_INDETERMINED:
			    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			    mProgress.setIndeterminate(true);
			    mProgress.show();
	    		break;	    		
	    	case ExperimentWidget.DELAY_NOINDICATION:
	    		break;

	        default:
		    }

		     

		}else{
			mDelayInProgress = false;
			mTasks[mTaskNumber].show();	//start dircetly
			sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_TRIAL1, false, false);
		}
	}
		
	
	public void traceUserInput(String data){
        if (mLogFile != null) writeToLoggingFile("trace", data);
	}
	
	// Method that gets called as soon as the User finishes a task with ok-button OR
	// the experiment widgets calls done functionality (e.g., by rotary knob 'ok/done'-push)
	public void done(ExperimentWidget expWidget){
		
		
		if (!mTasks[mTaskNumber].userChangedSomething()) return;// if user changed nothing, do nothing just return

		
		//data logging
		String data = Integer.toString(mOldDikablisRetValue);
		data += CSV_DELIMITER;
		data += expWidget.getDesiredResults()[expWidget.getDesiredResultsCursor()];		
		data += CSV_DELIMITER;
		data += expWidget.getResult();		
	    writeToLoggingFile("result", data);
	    
						
		if ( mTasks[mTaskNumber].getDesiredResultsCursor() < (mTasks[mTaskNumber].getDesiredResults().length -1)){
			mTasks[mTaskNumber].incDesiredResultsCursor();//increment trial cursor
			mTasks[mTaskNumber].reset();//e.g. delete input text
			
			refreshInstructionOverlay();
			
		    switch ( mTasks[mTaskNumber].getDesiredResultsCursor() ) {
	    	case 1:
				sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_TRIAL2, false, false);
	    		break;
	    		
	    	case 2:
				sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_TRIAL3, false, false);
	    		break;

	        default:
		    }//end switch
		}else{//this was the last trial of this task; therefore => next task
			mTasks[mTaskNumber].dismiss();//hide old one and do hard-reset (reset ResultsCursor)
			mTaskNumber++;//next task
			if (mTaskNumber >= mTasks.length){//check if end of experiment
                sendTrigger(DikablisThread.DIKABLIS_EVENT_START, mOldDikablisTriggerSubtask, false, true);//last trigger
				endDialog();
			}else{
				findViewById(R.id.InstructionLayout).setVisibility(View.VISIBLE);//show instruction screen
                sendTrigger(DikablisThread.DIKABLIS_EVENT_START, TRIGGER_INSTRUCTION, false, false);//first trigger
                refreshInstructionOverlay();//instruction for new task
			}	
		}
		

		
	}
	
	void endDialog(){

		AlertDialog alert = new AlertDialog.Builder(this)
          .setMessage( "" )
	      .setTitle("Vielen Dank!")
	      .setPositiveButton("Ok",
	         new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton){
		        	 finish();
		         }
	         })
	      .setOnCancelListener(new DialogInterface.OnCancelListener() {         
	    	public void onCancel(DialogInterface dialog) {
	    			finish();
	    		}
	      	})
	      .create();
		alert.setCancelable(false);
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}
	
 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	String str ="";
    	
		if (event.getRepeatCount() > 0){
		  str = "#PP";
		}else{
	
		    switch (keyCode) {//control radio task e.g. by bluetooth keyboard
		    	case KeyEvent.KEYCODE_VOLUME_DOWN:
		    		str = "#tl";
		    		break;
		    		
		    	case KeyEvent.KEYCODE_VOLUME_UP:
		    		str = "#tr";
		    		break;
	
		        default:
		        	return false;
		    }
		    
		}
	    
		Message msg = mBtHandler.obtainMessage();
		msg.what = BtThread.BT_RX_CALLBACK;
		msg.obj = str;
		mBtHandler.sendMessage(msg);
        return true;
	    
	}	
	
	

	
		
}