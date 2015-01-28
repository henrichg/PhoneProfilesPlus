package sk.henrichg.phoneprofilesplus;


import java.text.DateFormatSymbols;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * A {@link Preference} that displays a list of entries as
 * a dialog and allows multiple selections
 * <p>
 * This preference will store a string into the SharedPreferences. This string will be the values selected
 * from the {@link #setEntryValues(CharSequence[])} array.
 * </p>
 */
public class DaysOfWeekPreference extends ListPreference {
	//Need to make sure the SEPARATOR is unique and weird enough that it doesn't match one of the entries.
	//Not using any fancy symbols because this is interpreted as a regex for splitting strings.
	private static final String SEPARATOR = "|";
	
	public static final String allValue = "#ALL#";
	
    private boolean[] mClickedDialogEntryIndices;
    
    Context context;

    public DaysOfWeekPreference(Context context) {
        super(context);
        mClickedDialogEntryIndices = new boolean[8];
        this.context = context; 
    }

    
    public DaysOfWeekPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mClickedDialogEntryIndices = new boolean[8];
        this.context = context; 
    }
 
    @Override
    public void setEntries(CharSequence[] entries) {
    	super.setEntries(entries);
        mClickedDialogEntryIndices = new boolean[8];
    }
    
    @Override
    public void setEntryValues(CharSequence[] entries) {
    	super.setEntryValues(entries);
    }
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {

    	
    	CharSequence[] entries = getEntries();
    	CharSequence[] entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }
        
    	
        //restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, 
                new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean val) {
                    	mClickedDialogEntryIndices[which] = val;
					}
        });
    }

    public static String[] parseStoredValue(CharSequence val) {
		if ( "".equals(val) )
			return null;
		else
			return ((String)val).split("\\"+SEPARATOR);
    }
    
    private void restoreCheckedEntries(String value) {
    	CharSequence[] entryValues = getEntryValues();
    	
    	//String[] vals = parseStoredValue(getValue());
    	String[] vals = parseStoredValue(value);
    	if ( vals != null ) {
        	for ( int j=0; j<vals.length; j++ ) {
        		String val = vals[j].trim();
            	for ( int i=0; i<entryValues.length; i++ ) {
            		CharSequence entry = entryValues[i];
                	if ( entry.equals(val) ) {
            			mClickedDialogEntryIndices[i] = true;
            			break;
            		}
            	}
        	}
    	}
    }

	@Override
    protected void onDialogClosed(boolean positiveResult) {
//        super.onDialogClosed(positiveResult);
        
    	CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
        	StringBuffer value = new StringBuffer();
        	for ( int i=0; i<entryValues.length; i++ ) {
        		if ( mClickedDialogEntryIndices[i] ) {
        			value.append(entryValues[i]).append(SEPARATOR);
        		}
        	}
        	
            if (callChangeListener(value)) {
            	String val = value.toString();
            	if ( val.length() > 0 )
            		val = val.substring(0, val.length()-SEPARATOR.length());
            	setValue(val);
            }
            
            setSummary(getSummary());
            
        }
    }
	
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	
    	// change entries order by firts day of week //////////////////
    	CharSequence[] newEntries = new CharSequence[8];
    	CharSequence[] newEntryValues = new CharSequence[8];
    	
    	newEntries[0] = context.getString(R.string.array_pref_event_all);
    	newEntryValues[0] = allValue;
    	
    	String[] namesOfDay = DateFormatSymbols.getInstance().getWeekdays();
    	//for (int i = 1; i < 8; i++)
    	//	Log.e("DaysOfWeekPreference.onPrepareDialogBuilder", "namesOfDay="+namesOfDay[i]);

    	int dayOfWeek;
    	for (int i = 1; i < 8; i++)
    	{
    		dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
    		newEntries[i] = namesOfDay[dayOfWeek+1];
    		newEntryValues[i] = String.valueOf(dayOfWeek); 
    	}
    	setEntries(newEntries);
    	setEntryValues(newEntryValues);
    	/////////////////////////////////////////////////////
    	
        if (restoreValue) {
        	String sVal = getPersistedString((String)defaultValue);
        	restoreCheckedEntries(sVal);
        } else {
        	restoreCheckedEntries((String)defaultValue);
        }
    	
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
    	String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();
    	
    	String summary = "";
    	
    	if (mClickedDialogEntryIndices[0])
    	{
	    	for ( int i=0; i<namesOfDay.length; i++ )
    			summary = summary + namesOfDay[i] + " ";
    	}
    	else
    	{
    		
	        for ( int i=1; i<mClickedDialogEntryIndices.length; i++ )
	    		if (mClickedDialogEntryIndices[i])
	    		{
	    			summary = summary + namesOfDay[EventPreferencesTime.getDayOfWeekByLocale(i-1)+1] + " ";
	    		}
	    	
    	}
    	
        return summary;
    }
    
}
