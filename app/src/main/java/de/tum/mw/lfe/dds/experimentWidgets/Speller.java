package de.tum.mw.lfe.dds.experimentWidgets;
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
import java.util.ArrayList;
import java.util.List;

import de.tum.mw.lfe.dds.ExperimentActivity;
import de.tum.mw.lfe.dds.R;
import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class Speller extends ExperimentWidget {

	private static final String TAG = "ExperimentWidget.Speller";
	
	public static final String DEL = "<"; 
	public static final String OK = "OK"; 
	String[] SPELL_CHARS = {"-","-","-", DEL, OK}; 
	public static final int SPELLER_EDIT_ID = 777; 
	private int mCursor;
	private ArrayList<Button> mButtons; 
	
	private EditText mEditText;
	private boolean mNeedsOkButton = true; //TODO ändern!
	
	//constructor
	public Speller(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode){
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}
	
	public boolean getNeedsOkButton() {
		return mNeedsOkButton;
	}
	
	protected String[] spell(){
		return SPELL_CHARS;
	}
	
	public void init(String param) {
		
		mCursor = 0;
		
		mEditText = new EditText(mParent);
		mEditText.setClickable(false);
		mEditText.setTextSize(40);
		mEditText.setWidth(700);
		mEditText.setHeight(50);
		mEditText.setLines(1);
		mEditText.setMaxLines(1);
		mEditText.setKeyListener(null);
//		mEditText.setId(SPELLER_EDIT_ID);
		mEditText.setId(R.id.speller);
		mWidgetLayout.addView(mEditText);

		
		mButtons = new ArrayList<Button>();
	        
        for(int i=0;i < spell().length; i++){
    		Button b = new Button(mParent);
    		mButtons.add(b);
    		b.setText(spell()[i]);
    		b.setId(0x7700 + i);
	    	b.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
	    	b.setTextColor(Color.BLACK);
	    	b.setBackgroundColor(Color.LTGRAY);
	    	b.setMinimumWidth(60);
	    	b.setMinimumHeight(60);
	    	b.setMaxWidth(60);
	    	b.setMaxHeight(60);
	    	b.setWidth(60);
	    	b.setHeight(60);
	    	b.setPadding(1, 1, 1, 1);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    params.setMargins(1, 1, 1, 1);
    		if (i==0){
    		    params.addRule(RelativeLayout.BELOW, SPELLER_EDIT_ID);
    		    b.setLayoutParams(params);

    		}else{
    		    params.addRule(RelativeLayout.RIGHT_OF, 0x7700 + i-1);
    		    params.addRule(RelativeLayout.ALIGN_BASELINE, 0x7700 + i-1);
    		    b.setLayoutParams(params);
    		}
    		mWidgetLayout.addView(b);
        }
		
        mWidgetLayout.requestLayout();
		highlight();
	
	}

	@Override
	public void reset() {
		super.reset();
		if (mEditText != null){
			mEditText.getText().clear();
		}	
		mCursor = 0;
		highlight();
	}


	public String getResult() {
		if (mEditText == null) return "";
		return mEditText.getText().toString();
	}
	
	private void decCursor(){
		if (mCursor > 0) mCursor--;
		highlight();
	    mUserChangedSomething = true;
	}
	
	private void incCursor(){
		if (mCursor < spell().length-1) mCursor++;
		highlight();
	    mUserChangedSomething = true;
	}
	
	public void turnLeft(){
		decCursor();
        mParent.traceUserInput("d");
	}
	
	public void turnRight(){
		incCursor();
        mParent.traceUserInput("i");		
	}
	
	public void highlight(){
		
  		for(Button b : mButtons){
     		 b.setBackgroundColor(Color.LTGRAY);
     	} 
  		mButtons.get(mCursor).setBackgroundColor(Color.parseColor("#1CAF9A"));
	}
	
	public void okPress(){
		Button b = mButtons.get(mCursor);
		 
		 if (b.getText().equals(OK)){//tell parent we are done. 'ok' pressed
			 mParent.done(this);
			 return;
		 }
		 
		 if (b.getText().equals(DEL)){
			 String temp = mEditText.getText().toString();
			 if (temp.length() > 0) temp = temp.substring ( 0, temp.length() - 1 );
			 mEditText.setText(temp);
		 }else{
			 mEditText.getText().append(b.getText());
		 }	 
		 mParent.traceUserInput(b.getText() + mParent.CSV_DELIMITER + getResult());

	}
		
}
