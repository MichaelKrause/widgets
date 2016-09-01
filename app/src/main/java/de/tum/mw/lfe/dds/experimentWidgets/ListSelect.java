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
import java.util.Arrays;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import de.tum.mw.lfe.dds.ExperimentActivity;
import de.tum.mw.lfe.dds.R;

public class ListSelect extends ExperimentWidget {


	private static final String TAG = "ExperimentWidget.ListSelect";	
	protected ListView  mListView ;
	protected int mCursor;
	protected ArrayAdapter<String> mListAdapter ; 
	public static final String[] cities = new String[] { 
		"	 Aachen 	"	,
		"	 Augsburg 	"	,
		"	 Bergisch Gladbach 	"	,
		"	 Berlin 	"	,
		"	 Bielefeld 	"	,
		"	 Bochum 	"	,
		"	 Bonn 	"	,
		"	 Bottrop 	"	,
		"	 Braunschweig 	"	,
		"	 Bremen 	"	,
		"	 Bremerhaven 	"	,
		"	 Chemnitz	"	,
		"	 Cottbus 	"	,
		"	 Darmstadt 	"	,
		"	 Dessau-Roßlau	"	,
		"	 Dortmund 	"	,
		"	 Dresden 	"	,
		"	 Duisburg 	"	,
		"	 Düren 	"	,
		"	 Düsseldorf 	"	,
		"	 Erfurt 	"	,
		"	 Erlangen 	"	,
		"	 Essen 	"	,
		"	 Esslingen am Neckar 	"	,
		"	 Flensburg 	"	,
		"	 Frankfurt am Main 	"	,
		"	 Freiburg im Breisgau 	"	,
		"	 Fürth 	"	,
		"	 Gelsenkirchen 	"	,
		"	 Gera 	"	,
		"	 Göttingen 	"	,
		"	 Gütersloh 	"	,
		"	 Hagen 	"	,
		"	 Halle (Saale) 	"	,
		"	 Hamburg 	"	,
		"	 Hamm 	"	,
		"	 Hanau 	"	,
		"	 Hannover 	"	,
		"	 Heidelberg 	"	,
		"	 Heilbronn 	"	,
		"	 Herne 	"	,
		"	 Hildesheim 	"	,
		"	 Ingolstadt 	"	,
		"	 Iserlohn 	"	,
		"	 Jena 	"	,
		"	 Kaiserslautern 	"	,
		"	 Karlsruhe 	"	,
		"	 Kassel 	"	,
		"	 Kiel 	"	,
		"	 Koblenz 	"	,
		"	 Köln 	"	,
		"	 Konstanz 	"	,
		"	 Krefeld 	"	,
		"	 Leipzig 	"	,
		"	 Leverkusen 	"	,
		"	 Lübeck 	"	,
		"	 Ludwigsburg 	"	,
		"	 Ludwigshafen am Rhein 	"	,
		"	 Lünen 	"	,
		"	 Magdeburg 	"	,
		"	 Mainz 	"	,
		"	 Mannheim 	"	,
		"	 Marl 	"	,
		"	 Minden 	"	,
		"	 Moers 	"	,
		"	 Mönchengladbach 	"	,
		"	 Mülheim an der Ruhr 	"	,
		"	 München 	"	,
		"	 Münster (Westfalen) 	"	,
		"	 Neuss 	"	,
		"	 Nürnberg 	"	,
		"	 Oberhausen 	"	,
		"	 Offenbach am Main 	"	,
		"	 Oldenburg	"	,
		"	 Osnabrück 	"	,
		"	 Paderborn 	"	,
		"	 Pforzheim 	"	,
		"	 Potsdam 	"	,
		"	 Ratingen 	"	,
		"	 Recklinghausen 	"	,
		"	 Regensburg 	"	,
		"	 Remscheid 	"	,
		"	 Reutlingen 	"	,
		"	 Rostock 	"	,
		"	 Saarbrücken 	"	,
		"	 Salzgitter 	"	,
		"	 Schwerin 	"	,
		"	 Siegen 	"	,
		"	 Solingen 	"	,
		"	 Stuttgart 	"	,
		"	 Trier 	"	,
		"	 Tübingen 	"	,
		"	 Ulm 	"	,
		"	 Velbert 	"	,
		"	 Wiesbaden 	"	,
		"	 Witten 	"	,
		"	 Wolfsburg 	"	,
		"	 Wuppertal 	"	,
		"	 Würzburg 	"	,
		"	 Zwickau 	"		
	};
	

	public ListSelect(byte dikablisNumber, ExperimentActivity parent, RelativeLayout layout, String name, String[] desiredResults, long delay, byte delayMode) {
		super(dikablisNumber, parent, layout, name, desiredResults, delay,  delayMode);
	}
	
	
	@Override
	public void init(String param) {

		mListView = new ListView(mParent);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mListView.setLayoutParams(params);
		//mListView.setClickable(false);
		//mListView.setHorizontalScrollBarEnabled(true);
		//mListView.setSmoothScrollbarEnabled(false);
		//mListView.setCacheColorHint(Color.TRANSPARENT);
		mWidgetLayout.addView(mListView);

	    ArrayList<String> citytList = new ArrayList<String>();  
	    citytList.addAll( Arrays.asList(cities) ); 
	    mListAdapter = new ArrayAdapter<String>(mParent, R.layout.listitem, citytList){
	    	 
	    		 @Override
	    		 public View getView(int position, View convertView, ViewGroup parent)
	    		 {

	    			 View row = super.getView(position, convertView, parent);
	    			 
	    			 row.setDrawingCacheEnabled(false);
	    			 
	    			 TextView tv = (TextView)row.findViewById(R.id.listSelectTextView);
	    			 tv.setTextSize(28);
	    			 if (tv!=null){
    					if (tv.getText().equals(cities[mCursor])){
    						row.setBackgroundColor(Color.parseColor("#7777ff"));
    					}else{
    						row.setBackgroundColor(Color.TRANSPARENT);
    					}	
	    			 }
	    			 
	    			 return row;
	    		 }
	    		 
	    };
	    mListView.setAdapter( mListAdapter ); 
	    
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		mListView.setOnItemClickListener( new ListView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		    	mUserChangedSomething = true;
		    	mCursor = pos;	  
				highlight();
		    }
		  });
				
		//mListView.setDrawSelectorOnTop(true);

		
		mCursor = 0;
		highlight();

	}
	

	@Override
	public void reset() {
		super.reset();
		setTo(0);
	}

	public void setTo(int position){
		if ((position >= 0) && (position < mListAdapter.getCount())){
			mCursor = position;
		}
		mListView.setSelection(position);
		highlight();
	}
	
	@Override
	public String getResult() {
	  return cities[mCursor];
	}
	
	
	private void decCursor(){
		if (mCursor > 0) mCursor--;
		mListView.smoothScrollToPosition(mCursor-1);
		mListView.setSelection(mCursor-1);
		highlight();
		mUserChangedSomething = true;
	}
	
	private void incCursor(){
		if (mCursor < mListAdapter.getCount()-1) mCursor++;
		mListView.setSelection(mCursor-1);
		highlight();
		mUserChangedSomething = true;
	}
	
	public void turnLeft(){
		decCursor();
	}
	
	public void turnRight(){
		incCursor();
	}
	
	
	public View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition() -  listView.getHeaderViewsCount();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition ) {
		    return listView.getAdapter().getView(pos, null, listView);
		} else {
		    final int childIndex = pos - firstListItemPosition;
		    return listView.getChildAt(childIndex);
		}
		}
	
	public void highlight(){

		mParent.traceUserInput(Integer.toString(mCursor) + mParent.CSV_DELIMITER + getResult());

		mListView.invalidate();
		mListView.requestLayout();
		
		for(int i=0;i <mListView.getChildCount();i++){
			View v = mListView.getChildAt(i);
			v.setBackgroundColor(Color.TRANSPARENT);
			TextView tv = (TextView)v.findViewById(R.id.listSelectTextView);
			
			if (tv!=null){
				if (tv.getText().equals(cities[mCursor])){
					v.setBackgroundColor(Color.parseColor("#7777ff"));
				}	
			}
			v.requestLayout();
		}


	}
	
	public void okPress(){
		mParent.done(this);
	}
	
	
}
