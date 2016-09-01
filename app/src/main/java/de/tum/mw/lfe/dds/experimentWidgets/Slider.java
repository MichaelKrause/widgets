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
import android.widget.SeekBar;
import android.widget.RelativeLayout.LayoutParams;
import de.tum.mw.lfe.dds.ExperimentActivity;
import de.tum.mw.lfe.dds.R;

public class Slider extends ExperimentWidget {
	
	private static final String TAG = "ExperimentWidget.Slider";
	//public static final int SLIDER_ID = 778;
	protected SeekBar mSeekBar;	
	protected int mStepSize = 5;
	
	public Slider(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}
	

	public void setStepSize(int stepSize){
		mStepSize = stepSize;
	}
	
	public int getStepSize(){
		return mStepSize;
	}

	@Override
	public void init(String param) {
		
   	    mSeekBar = new SeekBar(mParent);
//      mSeekBar.setId(SLIDER_ID);
        mSeekBar.setId(R.id.slider);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    params.setMargins(150, 200, 150, 20);
		mSeekBar.setLayoutParams(params);
   	    mWidgetLayout.addView(mSeekBar);
   	    mWidgetLayout.requestLayout();
   	    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
	   	    	
	   	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	   	    	mUserChangedSomething = true;
	   	    	
   	    		if (fromUser == false){
   		   	        progressChanged();
   	    			return;
   	    		}
   	    		
	   	    	int mod = progress % getStepSize(); 
	   	        if (mod != 0) progress -= mod;
	   	        
	   	        seekBar.setProgress(progress);
	   	        progressChanged();
	   	        
		        mParent.traceUserInput(Integer.toString(progress));
	   	    }
	
	   	    public void onStartTrackingTouch(SeekBar seekBar) {
	
	   	    }
	
	   	    public void onStopTrackingTouch(SeekBar seekBar) {
	
	   	    }
	   	});

		
	}
	
	
	protected void progressChanged(){
		
	}
	
	
	private void dec(){
		mSeekBar.incrementProgressBy(-getStepSize());
	}
	
	private void inc(){
		mSeekBar.incrementProgressBy(getStepSize());
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
	
	@Override
	public void reset() {
		super.reset();
		if (mSeekBar != null) mSeekBar.setProgress(0);

	}

	@Override
	public String getResult() {
		return Integer.toString(mSeekBar.getProgress());
	}
	
}
