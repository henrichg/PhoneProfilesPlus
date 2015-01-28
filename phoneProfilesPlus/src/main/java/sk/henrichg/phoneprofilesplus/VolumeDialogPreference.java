/**
 * Copyright CMW Mobile.com, 2010.
 */
package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;

import android.content.res.TypedArray;

import android.media.AudioManager;
import android.preference.DialogPreference;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton;

/**
 * The SeekBarDialogPreference class is a DialogPreference based and provides a
 * seekbar preference.
 * @author Casper Wakkers
 */
public class VolumeDialogPreference extends
		DialogPreference implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
	// Layout widgets.

	Context _context;
	private SeekBar seekBar = null;
	private TextView valueText = null;
	private CheckBox noChangeChBox = null; 
	private CheckBox defaultProfileChBox = null; 
	
	private AudioManager audioManager = null;

	// Custom xml attributes.
	private String volumeType = null;
	private int noChange = 0;
	private int defaultProfile = 0;
	private int disableDefaultProfile = 0;
	
	private int maximumValue = 7;
	private int minimumValue = 0;
	private int defaultValue = 0;
	private int defaultRingerMode = 0;
	private int stepSize = 1;

	private String sValue = "0|1";
	private int value = 0;

	/**
	 * The SeekBarDialogPreference constructor.
	 * @param context of this preference.
	 * @param attrs custom xml attributes.
	 */
	public VolumeDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		_context = context;
		
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
			R.styleable.VolumeDialogPreference);

		volumeType = typedArray.getString(
			R.styleable.VolumeDialogPreference_volumeType);
		noChange = typedArray.getInteger(
			R.styleable.VolumeDialogPreference_vNoChange, 1);
		defaultProfile = typedArray.getInteger(
				R.styleable.VolumeDialogPreference_vDefaultProfile, 0);
		disableDefaultProfile = typedArray.getInteger(
				R.styleable.VolumeDialogPreference_vDisableDefaultProfile, 0);

		audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		// zistima maximalnu hodnotu z audio managera
		if (volumeType.equalsIgnoreCase("RINGTONE")) 
			maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		else
		if (volumeType.equalsIgnoreCase("NOTIFICATION")) 
			maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		else
		if (volumeType.equalsIgnoreCase("MEDIA")) 
			maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		else
		if (volumeType.equalsIgnoreCase("ALARM")) 
			maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		else
		if (volumeType.equalsIgnoreCase("SYSTEM")) 
			maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		else
		if (volumeType.equalsIgnoreCase("VOICE")) 
			maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		// zistime default hodnotu z audio managera
		if (volumeType.equalsIgnoreCase("RINGTONE")) 
			defaultValue = audioManager.getStreamVolume(AudioManager.STREAM_RING);
		else
		if (volumeType.equalsIgnoreCase("NOTIFICATION")) 
			defaultValue = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		else
		if (volumeType.equalsIgnoreCase("MEDIA")) 
			defaultValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		else
		if (volumeType.equalsIgnoreCase("ALARM")) 
			defaultValue = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
		else
		if (volumeType.equalsIgnoreCase("SYSTEM")) 
			defaultValue = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		else
		if (volumeType.equalsIgnoreCase("VOICE")) 
			defaultValue = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		defaultRingerMode = audioManager.getRingerMode();
		
		typedArray.recycle();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressLint("InflateParams")
	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		View view = layoutInflater.inflate(
			R.layout.activity_volume_pref_dialog, null);

		seekBar = (SeekBar)view.findViewById(R.id.volumePrefDialogSeekbar);
		valueText = (TextView)view.findViewById(R.id.volumePrefDialogValueText);
		noChangeChBox = (CheckBox)view.findViewById(R.id.volumePrefDialogNoChange);
		defaultProfileChBox = (CheckBox)view.findViewById(R.id.volumePrefDialogDefaultProfile);

		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setKeyProgressIncrement(stepSize);
		seekBar.setMax(maximumValue - minimumValue);
		
		getValueVDP();

		seekBar.setProgress(value);

		noChangeChBox.setOnCheckedChangeListener(this);
		noChangeChBox.setChecked((noChange == 1));

		defaultProfileChBox.setOnCheckedChangeListener(this);
		defaultProfileChBox.setChecked((defaultProfile == 1));
		defaultProfileChBox.setEnabled(disableDefaultProfile == 0);

		if (noChange == 1)
			defaultProfileChBox.setChecked(false);
		if (defaultProfile == 1)
			noChangeChBox.setChecked(false);
		
		valueText.setEnabled((noChange == 0) && (defaultProfile == 0));
		seekBar.setEnabled((noChange == 0) && (defaultProfile == 0));
		
		return view;
	}
	/**
	 * {@inheritDoc}
	 */
	public void onProgressChanged(SeekBar seek, int newValue,
			boolean fromTouch) {
		// Round the value to the closest integer value.
		if (stepSize >= 1) {
			value = Math.round(newValue/stepSize)*stepSize;
		}
		else {
			value = newValue;
		}

		// Set the valueText text.
		valueText.setText(String.valueOf(value + minimumValue));
		
		callChangeListener(value);
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		//Log.d("VolumeDialogPreference.onCheckedChanged", Boolean.toString(isChecked));
		
		if (buttonView.getId() == R.id.volumePrefDialogNoChange)
		{
			noChange = (isChecked)? 1 : 0;

			valueText.setEnabled((noChange == 0) && (defaultProfile == 0));
			seekBar.setEnabled((noChange == 0) && (defaultProfile == 0));
			if (isChecked)
				defaultProfileChBox.setChecked(false);
		}

		if (buttonView.getId() == R.id.volumePrefDialogDefaultProfile)
		{
			defaultProfile = (isChecked)? 1 : 0;

			valueText.setEnabled((noChange == 0) && (defaultProfile == 0));
			seekBar.setEnabled((noChange == 0) && (defaultProfile == 0));
			if (isChecked)
				noChangeChBox.setChecked(false);
		}
		
		callChangeListener(noChange);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onStartTrackingTouch(SeekBar seek) {
	}
	/**
	 * {@inheritDoc}
	 */
	public void onStopTrackingTouch(SeekBar seek) {
		if (volumeType.equalsIgnoreCase("RINGTONE")) 
			audioManager.setStreamVolume(AudioManager.STREAM_RING, value, AudioManager.FLAG_PLAY_SOUND);
		else
		if (volumeType.equalsIgnoreCase("NOTIFICATION")) 
			audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, value, AudioManager.FLAG_PLAY_SOUND);
		else
		if (volumeType.equalsIgnoreCase("MEDIA")) 
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_PLAY_SOUND);
		else
		if (volumeType.equalsIgnoreCase("ALARM")) 
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, value, AudioManager.FLAG_PLAY_SOUND);
		else
		if (volumeType.equalsIgnoreCase("SYSTEM")) 
			audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, value, AudioManager.FLAG_PLAY_SOUND);
		else
		if (volumeType.equalsIgnoreCase("VOICE")) 
			audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, value, AudioManager.FLAG_PLAY_SOUND);
	}
	/**
	 * {@inheritDoc}
	 */
	public void onClick(DialogInterface dialog, int which) {
		// if the positive button is clicked, we persist the value.
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (shouldPersist()) {
				//Log.d("VolumeDialogPreference.onClick","xxxx");
				persistString(Integer.toString(value + minimumValue)
						+ "|" + Integer.toString(noChange)
						+ "|" + Integer.toString(defaultProfile));
				setSummaryVDP();
			}
		}

		super.onClick(dialog, which);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		if (restoreValue) {
			// restore state
			getValueVDP();
		}
		else {
			// set state
			value = 0;
			noChange = 1;
			defaultProfile = 0;
			persistString(Integer.toString(value + minimumValue)
					+ "|" + Integer.toString(noChange)
					+ "|" + Integer.toString(defaultProfile));			
		}
		setSummaryVDP();
	}
	
	private void getValueVDP()
	{
		// Get the persistent value and correct it for the minimum value.
		sValue = getPersistedString(sValue);
		
		String[] splits = sValue.split("\\|");
		try {
			value = Integer.parseInt(splits[0]);
			if (value == -1)
			{
				value = defaultValue;
			}
		} catch (Exception e) {
			value = 0;
		}
		value = value - minimumValue;
		try {
			noChange = Integer.parseInt(splits[1]);
		} catch (Exception e) {
			noChange = 1;
		}
		try {
			defaultProfile = Integer.parseInt(splits[2]);
		} catch (Exception e) {
			defaultProfile = 0;
		}
		
		// You're never know...
		if (value < 0) {
			value = 0;
		}
	}
	
	private void setSummaryVDP()
	{
		String prefVolumeDataSummary;
		if (noChange == 1)
			prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_no_change);
		else
		if (defaultProfile == 1)
			prefVolumeDataSummary = _context.getResources().getString(R.string.preference_profile_default_profile);
		else
			prefVolumeDataSummary = String.valueOf(value) + " / " + String.valueOf(maximumValue);
		setSummary(prefVolumeDataSummary);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog)
	{
		audioManager.setRingerMode(defaultRingerMode);
		if (volumeType.equalsIgnoreCase("RINGTONE")) 
			audioManager.setStreamVolume(AudioManager.STREAM_RING, defaultValue, 0);
		else
		if (volumeType.equalsIgnoreCase("NOTIFICATION")) 
			audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, defaultValue, 0);
		else
		if (volumeType.equalsIgnoreCase("MEDIA")) 
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultValue, 0);
		else
		if (volumeType.equalsIgnoreCase("ALARM")) 
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, defaultValue, 0);
		else
		if (volumeType.equalsIgnoreCase("SYSTEM")) 
			audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, defaultValue, 0);
		else
		if (volumeType.equalsIgnoreCase("VOICE")) 
			audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, defaultValue, 0);
		audioManager.setRingerMode(defaultRingerMode);
	}
	
}
