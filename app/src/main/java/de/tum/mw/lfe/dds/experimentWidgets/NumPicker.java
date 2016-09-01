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
import android.widget.RelativeLayout;
import android.widget.NumberPicker;
import de.tum.mw.lfe.dds.ExperimentActivity;

public class NumPicker extends ExperimentWidget{
	
	private static final String TAG = "ExperimentWidget.SpinBox";
	private NumberPicker mNumberPicker;
	
	public NumPicker(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}


	public void setTo(int value){
		mNumberPicker.setValue(value);
	}
	
	@Override
	public void init(String param) {
		mNumberPicker = new NumberPicker(mParent);
		mNumberPicker.setMinValue(1);
		mNumberPicker.setMaxValue(100);
		
		mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
	    public void onValueChange(NumberPicker numberPicker, int i, int i2) {
			mUserChangedSomething = true;
	        mParent.traceUserInput(Integer.toString(i) + mParent.CSV_DELIMITER + Integer.toString(i2));
	    }
	});
		
		
		mWidgetLayout.addView(mNumberPicker);
	}
	


	@Override
	public void reset() {
		super.reset();
		mNumberPicker.setValue(mNumberPicker.getMinValue());
		setTo(50);
	}

	@Override
	public String getResult() {
		return Integer.toString(mNumberPicker.getValue());
	}	
	
	
	private void dec(){
		mNumberPicker.setValue(mNumberPicker.getValue()-1);
        mParent.traceUserInput(Integer.toString(mNumberPicker.getValue()));
		mUserChangedSomething = true;	
	}
	
	private void inc(){
		mNumberPicker.setValue(mNumberPicker.getValue()+1);
        mParent.traceUserInput(Integer.toString(mNumberPicker.getValue()));
		mUserChangedSomething = true;
	}
	
	public void turnLeft(){
		dec();
	}
	
	public void turnRight(){
		inc();
	}	
	
	public void okPress(){
		mParent.done(this);
	}	
	
}
