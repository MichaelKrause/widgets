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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.tum.mw.lfe.dds.ExperimentActivity;
import de.tum.mw.lfe.dds.R;

public class SliderNum extends Slider{
	private static final String TAG = "ExperimentWidget.SliderNum";
	private TextView mEditText;
	//public static final int TEXT_ID = 779;
	
	public SliderNum(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout,
			String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}
	
	@Override
	public void init(){
		super.init();
		
		//add textfield
		mEditText = new TextView(mParent);
		mEditText.setClickable(false);
		mEditText.setKeyListener(null);
		mEditText.setTextSize(30);
		mEditText.setWidth(100);
		mEditText.setHeight(30);
		mEditText.setLines(1);
		mEditText.setMaxLines(1);
//		mEditText.setId(TEXT_ID);
		mEditText.setId(R.id.sliderTextbox);
		mEditText.setEnabled(false);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    params.setMargins(20, 180, 20, 20);
	    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, R.id.slider);
	    mEditText.setLayoutParams(params);
		
		mWidgetLayout.addView(mEditText);
		mWidgetLayout.requestLayout();
		
		
	}
	
	protected void progressChanged(){
		super.progressChanged();
		mEditText.setText(Integer.toString(mSeekBar.getProgress()));
	}
	
	@Override
	public void show(){
		super.show();
		progressChanged();//set initially text field
	}


}
