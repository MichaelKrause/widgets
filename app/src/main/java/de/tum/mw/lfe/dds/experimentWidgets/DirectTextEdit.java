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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.tum.mw.lfe.dds.ExperimentActivity;

public class DirectTextEdit extends ExperimentWidget{
	
	private static final String TAG = "ExperimentWidget.DirectTextEdit";
	private EditText mEditText;
	private DirectTextEdit mThis = this;
	
	public DirectTextEdit(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}
	@Override
	public boolean userChangedSomething(){
 
		return (!mEditText.getText().toString().equals(""));
	}

	@Override
	public void init(String param) {
		mEditText = new EditText(mParent);
		mEditText.setTextSize(40);
		mEditText.setWidth(700);
		mEditText.setHeight(50);
		mEditText.setMaxHeight(50);
		mEditText.setLines(1);
		mEditText.setMaxLines(1);
		mEditText.setFocusable(true);
		mEditText.setPadding(0, 0, 0, 500); // damit die Anweisung nicht vom Keyboard aus dem Screen 'gepushed' wird
		mWidgetLayout.addView(mEditText);
		mWidgetLayout.setOnKeyListener(new EditText.OnKeyListener() {
	        public boolean onKey(View v, int keyCode, KeyEvent event) {

	        	//never called ?!
	        	/*
		    		mUserChangedSomething = true;

	        		mParent.traceUserInput(Integer.toString(keyCode) + mParent.CSV_DELIMITER + getResult());

			    	if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			        	mParent.done(mThis);
			        	return true;		    		
			    	}	        		
	        		*/
	                return false;
	            }
	        });

		
		mEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
		    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		    	
	        	//never called ?!
	        	/*
		    	
		    	if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
		        	mParent.done(mThis);
		        	return true;		    		
		    	}
		    	
		        int result = actionId & EditorInfo.IME_MASK_ACTION;
		        switch(result) {
		        
		        case EditorInfo.IME_ACTION_DONE:
		        	mParent.done(mThis);
		        	return true;
		            //break;
		        	
		        case EditorInfo.IME_ACTION_NEXT:
		        	mParent.done(mThis);
		        	return true;
		            //break;
		        }
		        */
		        return false;
		    }
		});
		
		

	}

	@Override
	public void reset() {
		super.reset();
		mEditText.setText("");
	}

	@Override
	public void show()
	{
		super.show();
		mEditText.requestFocus();
		InputMethodManager imm = (InputMethodManager) mParent.getSystemService(mParent.INPUT_METHOD_SERVICE);
		imm.toggleSoftInputFromWindow(mLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
		
	}
	
	@Override
	public void dismiss()
	{
		super.dismiss();
		
		InputMethodManager imm = (InputMethodManager) mParent.getSystemService(mParent.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
		
	}

	
	@Override
	public String getResult() {
		if (mEditText == null) return "";
		return mEditText.getText().toString();
	}

}
