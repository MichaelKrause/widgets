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
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.RelativeLayout.LayoutParams;

import de.tum.mw.lfe.dds.ExperimentActivity;
import de.tum.mw.lfe.dds.R;

public class SliderVis extends Slider{
	private static final String TAG = "ExperimentWidget.SliderVis";
	private boolean mNeedsOkButton = true;
	protected SeekBar mVisSeekBar; //is in Slider parent class
	//public static final int SLIDER_ID2 = 863;
	
	public SliderVis(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout,
			String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}

	
	@Override
	public void init(String param) {
		
		super.init(param);
		

		mVisSeekBar = new SeekBar(mParent);

//		mVisSeekBar.setId(SLIDER_ID2);
		mVisSeekBar.setId(R.id.targetSlider);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    params.setMargins(150, 20, 150, 20);
 //original	    params.addRule(RelativeLayout.ABOVE, SLIDER_ID);
	    //params.addRule(RelativeLayout.ABOVE, R.id.slider);
        mVisSeekBar.setLayoutParams(params);
   	    mWidgetLayout.addView(mVisSeekBar);
   	    mVisSeekBar.setEnabled(false);
   	    mVisSeekBar.requestLayout();


        //originally in the experiments the targetSlider was aligned above the slider for inteactions
        //this seems not to work anymore?! now in the workaround the interaction slider is aligned below the target slider

        //workaround:
        RelativeLayout.LayoutParams tempparams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        tempparams.setMargins(150, 200, 150, 20);
	    tempparams.addRule(RelativeLayout.BELOW, R.id.targetSlider);
        mSeekBar.setLayoutParams(tempparams);
        mSeekBar.requestLayout();

		updateVisualTarget();



	}
	@Override
	public void reset(){
		super.reset();
		updateVisualTarget();
	}	
	
	public void updateVisualTarget(){
		String temp = mDesiredResults[mDesiredResultsCursor];
		String progress = temp.replace("%", "");
		int p = Integer.parseInt(progress);
		if (mVisSeekBar != null){
			mVisSeekBar.setSecondaryProgress(p);
			mVisSeekBar.setProgress(p);
		}
	}




}
