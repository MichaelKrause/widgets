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
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.tum.mw.lfe.dds.ExperimentActivity;
import de.tum.mw.lfe.dds.R;

public class NumPickerTap extends ExperimentWidget{

	private static final String TAG = "ExperimentWidget.SpinBox";
    private NumberPicker mNumberPicker;
    private TextView mTextView;

    private Button mDecButton;
    private Button mIncButton;


	public NumPickerTap(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);


    }


	public void setTo(int value){
		mNumberPicker.setValue(value);
        refreshTextView();
	}

    public void refreshTextView(){
        mTextView.setText(Integer.toString(mNumberPicker.getValue()));
    }

	@Override
	public void init(String param) {
		mNumberPicker = new NumberPicker(mParent);
		mNumberPicker.setMinValue(1);
		mNumberPicker.setMaxValue(100);
        //mNumberPicker.setId(R.id.numTap);
        //mNumberPicker.setVisibility(View.GONE);

        mTextView = new TextView(mParent);
        mTextView.setId(R.id.numTap);
        mTextView.setTextSize(20);



        mDecButton = new Button(mParent);
        mIncButton = new Button(mParent);

        mDecButton.setText("-");
        mDecButton.setTextSize(20);
        mDecButton.setId(R.id.decTap);
        mDecButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dec();


            }
        });

        mIncButton.setText("+");
        mIncButton.setTextSize(20);
        mIncButton.setId(R.id.incTap);
        mIncButton.setOnClickListener (new View.OnClickListener() {
            public void onClick (View v) {

                inc();


            }
        });



		mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {

                mUserChangedSomething = true;
                mParent.traceUserInput(Integer.toString(i) + mParent.CSV_DELIMITER + Integer.toString(i2));
            }
        });



        //mWidgetLayout.addView(mNumberPicker);
        mWidgetLayout.addView(mTextView);
        mWidgetLayout.addView(mDecButton);
        mWidgetLayout.addView(mIncButton);

        RelativeLayout.LayoutParams tempParam = (RelativeLayout.LayoutParams) mDecButton.getLayoutParams();
        tempParam = (RelativeLayout.LayoutParams) mDecButton.getLayoutParams();
        tempParam.addRule(RelativeLayout.BELOW, R.id.numTap);
        //tempParam.addRule(RelativeLayout.LEFT_OF, R.id.incTap);
        tempParam.height = 220;
        tempParam.width = 220;
        //tempParam.setMargins(20, 20, 20, 20);
        mDecButton.setLayoutParams(tempParam);



        tempParam = (RelativeLayout.LayoutParams) mIncButton.getLayoutParams();
        tempParam.addRule(RelativeLayout.RIGHT_OF, R.id.decTap);
        tempParam.addRule(RelativeLayout.BELOW, R.id.numTap);
        tempParam.height = 220;
        tempParam.width = 220;
        //tempParam.setMargins(20, 20, 20, 20);
        mIncButton.setLayoutParams(tempParam);



        tempParam = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
        tempParam.addRule(RelativeLayout.CENTER_HORIZONTAL, R.id.decTap);
        tempParam.setMargins(20, 20, 20, 50);
        mTextView.setLayoutParams(tempParam);
        refreshTextView();


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
		mUserChangedSomething = true;
        mParent.traceUserInput(Integer.toString(mNumberPicker.getValue()));
        refreshTextView();


    }
	
	private void inc(){
		mNumberPicker.setValue(mNumberPicker.getValue()+1);
		mUserChangedSomething = true;
        mParent.traceUserInput(Integer.toString(mNumberPicker.getValue()));
        refreshTextView();

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
