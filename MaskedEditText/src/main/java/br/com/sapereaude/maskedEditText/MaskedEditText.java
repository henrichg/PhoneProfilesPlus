package br.com.sapereaude.maskedEditText;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MaskedEditText extends EditText implements TextWatcher {

	private String mask;
	private char maskFill;
	private char charRepresentation;
	private int[] rawToMask;
	private RawText rawText;
	private boolean editingBefore;
	private boolean editingOnChanged;
	private boolean editingAfter;
	private int[] maskToRaw;
	private char[] charsInMask;
	private int selection;
	private boolean initialized;
	private boolean ignore;
	protected int maxRawLength;
	private int lastValidMaskPosition;
	private boolean selectionChanged;
	private OnFocusChangeListener focusChangeListener;
	
	public MaskedEditText(Context context) {
		super(context);
		init();
	}
	
	public MaskedEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.MaskedEditText);
		mask = attributes.getString(R.styleable.MaskedEditText_mask);
		
		String maskFill = attributes.getString(R.styleable.MaskedEditText_mask_fill);
		this.maskFill = (maskFill != null && maskFill.length() > 0) ? maskFill.charAt(0) : ' ';

		String representation = attributes.getString(R.styleable.MaskedEditText_char_representation);
		
		if(representation == null) {
			charRepresentation = '#';
		}
		else {
			charRepresentation = representation.charAt(0);
		}
		
		cleanUp();
		
		// Ignoring enter key presses
		setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				switch (actionId) {
				case EditorInfo.IME_ACTION_NEXT:
					// fixing actionNext
					return false;
				default:
					return true;
				}
			}
		});
	}

	/** @param listener - its onFocusChange() method will be called before performing MaskedEditText operations, 
	 * related to this event. */
	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener listener) {
		focusChangeListener = listener;
	}
	
	private void cleanUp() {
        if (mask == null) {
            return;
        }
		initialized = false;
		
		generatePositionArrays();
		
		rawText = new RawText();
		selection = rawToMask[0];

		editingBefore = true;
		editingOnChanged = true;
		editingAfter = true;
		if(hasHint()) {
			this.setText(null);
		}
		else {
			this.setText(mask.replace(charRepresentation, maskFill));
		}
		editingBefore = false;
		editingOnChanged = false;
		editingAfter = false;
		
		maxRawLength = maskToRaw[previousValidPosition(mask.length() - 1)] + 1;
		lastValidMaskPosition = findLastValidMaskPosition();
		initialized = true;
		
		super.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (focusChangeListener != null) {
					focusChangeListener.onFocusChange(v, hasFocus);
				}
					
				if(hasFocus() && (rawText.length() > 0 || !hasHint())) {
					selectionChanged = false;
					MaskedEditText.this.setSelection(lastValidPosition());
				}
			}
		});
	}

	private int findLastValidMaskPosition() {
		for(int i = maskToRaw.length - 1; i >= 0; i--) {
			if(maskToRaw[i] != -1) return i;
		}
		throw new RuntimeException("Mask contains only the representation char");
	}

	private boolean hasHint() {
		return getHint() != null;
	}
	
	public MaskedEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setMask(String mask) {
		this.mask = mask;
		cleanUp();
	}
	
	public String getMask() {
		return this.mask;
	}
	
	public void setCharRepresentation(char charRepresentation) {
		this.charRepresentation = charRepresentation;
		cleanUp();
	}
	
	public char getCharRepresentation() {
		return this.charRepresentation;
	}
	
	private void generatePositionArrays() {
		int[] aux = new int[mask.length()];
		maskToRaw = new int[mask.length()];
		String charsInMaskAux = "";
		
		int charIndex = 0;
		for(int i = 0; i < mask.length(); i++) {
			char currentChar = mask.charAt(i);
			if(currentChar == charRepresentation) {
				aux[charIndex] = i;
				maskToRaw[i] = charIndex++;
			}
			else {
				String charAsString = Character.toString(currentChar);
				if(!charsInMaskAux.contains(charAsString) && !Character.isLetter(currentChar) && !Character.isDigit(currentChar)) {
					charsInMaskAux = charsInMaskAux.concat(charAsString);
				}
				maskToRaw[i] = -1;
			}
		}
		if(charsInMaskAux.indexOf(' ') < 0) {
			charsInMaskAux = charsInMaskAux + " ";
		}
		charsInMask = charsInMaskAux.toCharArray();
		
		rawToMask = new int[charIndex];
		for (int i = 0; i < charIndex; i++) {
			rawToMask[i] = aux[i];
		}
	}
	
	private void init() {
		addTextChangedListener(this);
        this.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
        if (mask == null) {
            return;
        }
		if(!editingBefore) {
			editingBefore = true;
			if(start > lastValidMaskPosition) {
				ignore = true;
			}
			int rangeStart = start;
			if(after == 0) {
				rangeStart = erasingStart(start);
			}
			Range range = calculateRange(rangeStart, start + count);
			if(range.getStart() != -1) {
				rawText.subtractFromString(range);
			}
			if(count > 0) {
				selection = previousValidPosition(start);
			}
		}
	}

	private int erasingStart(int start) {
		while(start > 0 && maskToRaw[start] == -1) {
			start--;
		}
		return start;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mask == null) {
            return;
        }
		if(!editingOnChanged && editingBefore) {
			editingOnChanged = true;
			if(ignore) {
				return;
			}
			if(count > 0) {
				int startingPosition = maskToRaw[nextValidPosition(start)];
				String addedString = s.subSequence(start, start + count).toString();
				count = rawText.addToString(clear(addedString), startingPosition, maxRawLength);
				if(initialized) {
					int currentPosition;
					if(startingPosition + count < rawToMask.length) 
						currentPosition = rawToMask[startingPosition + count];
					else
						currentPosition = lastValidMaskPosition + 1;
					selection = nextValidPosition(currentPosition);
				}
			}
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
        if (mask == null) {
            return;
        }
		if(!editingAfter && editingBefore && editingOnChanged) {
			editingAfter = true;
			if(rawText.length() == 0 && hasHint()) {
				selection = 0;
				setText(null);
			}
			else {
				setText(makeMaskedText());
			}
			
			selectionChanged = false;
			setSelection(selection);
			
			editingBefore = false;
			editingOnChanged = false;
			editingAfter = false;
			ignore = false;
		}
	}
	
	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		// On Android 4+ this method is being called more than 1 time if there is a hint in the EditText, what moves the cursor to left
		// Using the boolean var selectionChanged to limit to one execution
        if (mask == null) {
            super.onSelectionChanged(selStart, selEnd);
            return;
        }
		if(initialized ){
			if(!selectionChanged) {
		
				if(rawText.length() == 0 && hasHint()) {
					selStart = 0;
					selEnd = 0;
				}
				else {
					selStart = fixSelection(selStart);
					selEnd = fixSelection(selEnd);
				}
				setSelection(selStart, selEnd);
				selectionChanged = true;
			}
			else{//check to see if the current selection is outside the already entered text
				if(!(hasHint() && rawText.length() == 0) && selStart > rawText.length() - 1){
					setSelection(fixSelection(selStart),fixSelection(selEnd));
				}
			}
		}
		super.onSelectionChanged(selStart, selEnd);
	}
	
	private int fixSelection(int selection) {
		if(selection > lastValidPosition()) {
			return lastValidPosition();
		} 
		else {
			return nextValidPosition(selection);
		}
	}

	private int nextValidPosition(int currentPosition) {
		while(currentPosition < lastValidMaskPosition && maskToRaw[currentPosition] == -1) {
			currentPosition++;
		}
		if(currentPosition > lastValidMaskPosition) return lastValidMaskPosition + 1;
		return currentPosition;
	}
	
	private int previousValidPosition(int currentPosition) {
		while(currentPosition >= 0 && maskToRaw[currentPosition] == -1) {
			currentPosition--;
			if(currentPosition < 0) {
				return nextValidPosition(0);
			}
		}
		return currentPosition;
	}
	
	private int lastValidPosition() {
		if(rawText.length() == maxRawLength) {
			return rawToMask[rawText.length() - 1] + 1;
		}
		return nextValidPosition(rawToMask[rawText.length()]);
	}
	
	private String makeMaskedText() {
		char[] maskedText = mask.replace(charRepresentation, ' ').toCharArray();
		for(int i = 0; i < rawToMask.length; i++) {
			if(i < rawText.length()) {
				maskedText[rawToMask[i]] = rawText.charAt(i);
			}
			else {
				maskedText[rawToMask[i]] = maskFill;
			}
		}
		return new String(maskedText);
	}

	private Range calculateRange(int start, int end) {
		Range range = new Range();
		for(int i = start; i <= end && i < mask.length(); i++) {
			if(maskToRaw[i] != -1) {
				if(range.getStart() == -1) {
					range.setStart(maskToRaw[i]);
				}
				range.setEnd(maskToRaw[i]);
			}
		}
		if(end == mask.length()) {
			range.setEnd(rawText.length());
		}
		if(range.getStart() == range.getEnd() && start < end) {
			int newStart = previousValidPosition(range.getStart() - 1);
			if(newStart < range.getStart()) {
				range.setStart(newStart);
			}
		}
		return range;
	}
	
	private String clear(String string) {
		for(char c : charsInMask) {
			string = string.replace(Character.toString(c), "");
		}
		return string;
	}
}
