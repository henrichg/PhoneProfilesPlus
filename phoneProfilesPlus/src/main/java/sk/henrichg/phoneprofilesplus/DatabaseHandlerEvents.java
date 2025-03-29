package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class DatabaseHandlerEvents {

    // Adding new event
    static void addEvent(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.KEY_E_NAME, event._name); // Event Name
        values.put(DatabaseHandler.KEY_E_START_ORDER, event._startOrder); // start order
        values.put(DatabaseHandler.KEY_E_FK_PROFILE_START, event._fkProfileStart); // profile start
        values.put(DatabaseHandler.KEY_E_FK_PROFILE_END, event._fkProfileEnd); // profile end
        values.put(DatabaseHandler.KEY_E_STATUS, event.getStatus()); // event status
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START, event._notificationSoundStart); // notification sound
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START, event._notificationVibrateStart); // notification vibrate
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START, event._repeatNotificationStart); // repeat notification sound
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, event._repeatNotificationIntervalStart); // repeat notification sound interval
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END, event._notificationSoundEnd); // notification sound
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END, event._notificationVibrateEnd); // notification vibrate
        values.put(DatabaseHandler.KEY_E_FORCE_RUN, event._ignoreManualActivation ? 1 : 0); // force run when manual profile activation
        values.put(DatabaseHandler.KEY_E_BLOCKED, event._blocked ? 1 : 0); // temporary blocked
        values.put(DatabaseHandler.KEY_E_PRIORITY, event._priority); // priority
        values.put(DatabaseHandler.KEY_E_DELAY_START, event._delayStart); // delay for start
        values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0); // event is in delay before start
        values.put(DatabaseHandler.KEY_E_AT_END_DO, event._atEndDo); //at end of event do
        values.put(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0); // manual profile activation at start
        values.put(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
        values.put(DatabaseHandler.KEY_E_DELAY_END, event._delayEnd); // delay for end
        values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0); // event is in delay after pause
        values.put(DatabaseHandler.KEY_E_START_STATUS_TIME, event._startStatusTime); // time for status RUNNING
        values.put(DatabaseHandler.KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime); // time for change status from RUNNING to PAUSE
        values.put(DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation ? 1 : 0); // no pause event by manual profile activation
        values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, event._startWhenActivatedProfile); // start when profile is activated
        //values.put(DatabaseHandler.KEY_E_AT_END_HOW_UNDO, event._atEndHowUndo);
        values.put(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END, event._manualProfileActivationAtEnd ? 1 : 0); // manual profile activation at end
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundStartPlayAlsoInSilentMode ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundEndPlayAlsoInSilentMode ? 1 : 0);

        db.beginTransaction();

        try {
            // Inserting Row
            event._id = db.insert(DatabaseHandler.TABLE_EVENTS, null, values);
            updateEventPreferences(event, db);

            db.setTransactionSuccessful();

        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        } finally {
            db.endTransaction();
        }
    }

    static void addEvent(DatabaseHandler instance, Event event) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                event._startOrder = getMaxEventStartOrder(instance) + 1;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                addEvent(event, db);

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting single event
    static Event getEvent(DatabaseHandler instance, long event_id) {
        instance.importExportLock.lock();
        try {
            Event event = null;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{DatabaseHandler.KEY_E_ID,
                                DatabaseHandler.KEY_E_NAME,
                                DatabaseHandler.KEY_E_START_ORDER,
                                DatabaseHandler.KEY_E_FK_PROFILE_START,
                                DatabaseHandler.KEY_E_FK_PROFILE_END,
                                DatabaseHandler.KEY_E_STATUS,
                                DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START,
                                DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START,
                                DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START,
                                DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START,
                                DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END,
                                DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END,
                                DatabaseHandler.KEY_E_FORCE_RUN,
                                DatabaseHandler.KEY_E_BLOCKED,
                                DatabaseHandler.KEY_E_PRIORITY,
                                DatabaseHandler.KEY_E_DELAY_START,
                                DatabaseHandler.KEY_E_IS_IN_DELAY_START,
                                DatabaseHandler.KEY_E_AT_END_DO,
                                DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION,
                                DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE,
                                DatabaseHandler.KEY_E_DELAY_END,
                                DatabaseHandler.KEY_E_IS_IN_DELAY_END,
                                DatabaseHandler.KEY_E_START_STATUS_TIME,
                                DatabaseHandler.KEY_E_PAUSE_STATUS_TIME,
                                DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION,
                                DatabaseHandler.KEY_E_AT_END_HOW_UNDO,
                                DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END,
                                DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE,
                                DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event_id)}, null, null, null, null);

                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {

                        event = new Event(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NAME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_ORDER)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FK_PROFILE_START)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FK_PROFILE_END)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_STATUS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FORCE_RUN)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLOCKED)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PRIORITY)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DELAY_START)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_IS_IN_DELAY_START)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_AT_END_DO)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION)) == 1,
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DELAY_END)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_IS_IN_DELAY_END)) == 1,
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_STATUS_TIME)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PAUSE_STATUS_TIME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END)) == 1,
                                //cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_AT_END_HOW_UNDO))
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE)) == 1
                        );
                    }

                    cursor.close();
                //}

                if (event != null)
                    getEventPreferences(event, db);

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return event;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting All Events
    static List<Event> getAllEvents(DatabaseHandler instance, boolean calledFromImportDB) {
        if (!calledFromImportDB)
            instance.importExportLock.lock();
        try {
            List<Event> eventList = new ArrayList<>();
            try {
                if (!calledFromImportDB)
                    instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_NAME + "," +
                        DatabaseHandler.KEY_E_FK_PROFILE_START + "," +
                        DatabaseHandler.KEY_E_FK_PROFILE_END + "," +
                        DatabaseHandler.KEY_E_STATUS + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END + "," +
                        DatabaseHandler.KEY_E_FORCE_RUN + "," +
                        DatabaseHandler.KEY_E_BLOCKED + "," +
                        DatabaseHandler.KEY_E_PRIORITY + "," +
                        DatabaseHandler.KEY_E_DELAY_START + "," +
                        DatabaseHandler.KEY_E_IS_IN_DELAY_START + "," +
                        DatabaseHandler.KEY_E_AT_END_DO + "," +
                        DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION + "," +
                        DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE + "," +
                        DatabaseHandler.KEY_E_DELAY_END + "," +
                        DatabaseHandler.KEY_E_IS_IN_DELAY_END + "," +
                        DatabaseHandler.KEY_E_START_STATUS_TIME + "," +
                        DatabaseHandler.KEY_E_PAUSE_STATUS_TIME + "," +
                        DatabaseHandler.KEY_E_START_ORDER + "," +
                        DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + "," +
                        DatabaseHandler.KEY_E_AT_END_HOW_UNDO + "," +
                        DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE +
                        " FROM " + DatabaseHandler.TABLE_EVENTS +
                        " ORDER BY " + DatabaseHandler.KEY_E_ID;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Event event = new Event();
                        event._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                        event._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NAME));
                        event._fkProfileStart = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FK_PROFILE_START));
                        event._fkProfileEnd = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FK_PROFILE_END));
                        event.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_STATUS)));
                        event._notificationSoundStart = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START));
                        event._notificationVibrateStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START)) == 1;
                        event._repeatNotificationStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START)) == 1;
                        event._repeatNotificationIntervalStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START));
                        event._notificationSoundEnd = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END));
                        event._notificationVibrateEnd = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END)) == 1;
                        event._ignoreManualActivation = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FORCE_RUN)) == 1;
                        event._blocked = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLOCKED)) == 1;
                        event._priority = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PRIORITY));
                        event._delayStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DELAY_START));
                        event._isInDelayStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_IS_IN_DELAY_START)) == 1;
                        event._atEndDo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_AT_END_DO));
                        event._manualProfileActivation = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION)) == 1;
                        event._startWhenActivatedProfile = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE));
                        event._delayEnd = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DELAY_END));
                        event._isInDelayEnd = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_IS_IN_DELAY_END)) == 1;
                        event._startStatusTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_STATUS_TIME));
                        event._pauseStatusTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PAUSE_STATUS_TIME));
                        event._startOrder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_ORDER));
                        event._noPauseByManualActivation = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION)) == 1;
                        //event._atEndHowUndo = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_AT_END_HOW_UNDO));
                        event._manualProfileActivationAtEnd = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END)) == 1;
                        event._notificationSoundStartPlayAlsoInSilentMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE)) == 1;
                        event._notificationSoundEndPlayAlsoInSilentMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE)) == 1;
                        event.createEventPreferences();
                        getEventPreferences(event, db);
                        eventList.add(event);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return eventList;
        } finally {
            if (!calledFromImportDB)
                instance.stopRunningCommand();
        }
    }

    // Updating single event
    static void updateEvent(DatabaseHandler instance, Event event) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_NAME, event._name);
                values.put(DatabaseHandler.KEY_E_START_ORDER, event._startOrder);
                values.put(DatabaseHandler.KEY_E_FK_PROFILE_START, event._fkProfileStart);
                values.put(DatabaseHandler.KEY_E_FK_PROFILE_END, event._fkProfileEnd);
                values.put(DatabaseHandler.KEY_E_STATUS, event.getStatus());
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START, event._notificationSoundStart);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START, event._notificationVibrateStart ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START, event._repeatNotificationStart ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, event._repeatNotificationIntervalStart);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END, event._notificationSoundEnd);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END, event._notificationVibrateEnd ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_FORCE_RUN, event._ignoreManualActivation ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_BLOCKED, event._blocked ? 1 : 0);
                //values.put(DatabaseHandler.KEY_E_UNDONE_PROFILE, 0);
                values.put(DatabaseHandler.KEY_E_PRIORITY, event._priority);
                values.put(DatabaseHandler.KEY_E_DELAY_START, event._delayStart);
                values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_AT_END_DO, event._atEndDo);
                values.put(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, event._startWhenActivatedProfile);
                values.put(DatabaseHandler.KEY_E_DELAY_END, event._delayEnd);
                values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_START_STATUS_TIME, event._startStatusTime);
                values.put(DatabaseHandler.KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);
                values.put(DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation ? 1 : 0);
                //values.put(DatabaseHandler.KEY_E_AT_END_HOW_UNDO, event._atEndHowUndo);
                values.put(DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END, event._manualProfileActivationAtEnd ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundStartPlayAlsoInSilentMode ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundEndPlayAlsoInSilentMode ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});
                    updateEventPreferences(event, db);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEvent", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting single event
    static void deleteEvent(DatabaseHandler instance, Event event) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();
                db.delete(DatabaseHandler.TABLE_EVENTS, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[]{String.valueOf(event._id)});
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting all events
    static void deleteAllEvents(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();
                db.delete(DatabaseHandler.TABLE_EVENTS, null, null);
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    /*
    static boolean eventExists(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            boolean eventExists = false;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_ID
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    eventExists = cursor.getCount() > 0;

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return eventExists;
        } finally {
            instance.stopRunningCommand();
        }
    }
    */

    static void unlinkEventsFromProfile(DatabaseHandler instance, Profile profile) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_E_FK_PROFILE_START, 0);
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_FK_PROFILE_START + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values2 = new ContentValues();
                    values2.put(DatabaseHandler.KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    db.update(DatabaseHandler.TABLE_EVENTS, values2, DatabaseHandler.KEY_E_FK_PROFILE_END + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values3 = new ContentValues();
                    values3.put(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    db.update(DatabaseHandler.TABLE_EVENTS, values3, DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                            DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE +
                            " FROM " + DatabaseHandler.TABLE_EVENTS;
                    Cursor cursor = db.rawQuery(selectQuery, null);
                    if (cursor.moveToFirst()) {
                        do {
                            String oldFkProfiles = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE));
                            if (!oldFkProfiles.isEmpty()) {
                                String[] splits = oldFkProfiles.split(StringConstants.STR_SPLIT_REGEX);
                                StringBuilder newFkProfiles = new StringBuilder();
                                for (String split : splits) {
                                    if (!split.isEmpty()) {
                                        long fkProfile = Long.parseLong(split);
                                        if (fkProfile != profile._id) {
                                            if (newFkProfiles.length() > 0)
                                                newFkProfiles.append("|");
                                            newFkProfiles.append(split);
                                        }
                                    }
                                }
                                values = new ContentValues();
                                values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, newFkProfiles.toString());
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void unlinkAllEvents(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_FK_PROFILE_START, 0);
                values.put(DatabaseHandler.KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                values.put(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, "");

                // updating row
                db.update(DatabaseHandler.TABLE_EVENTS, values, null, null);

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting max(startOrder)
    static private int getMaxEventStartOrder(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                String countQuery = "SELECT MAX(" + DatabaseHandler.KEY_E_START_ORDER + ") FROM " + DatabaseHandler.TABLE_EVENTS;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void setEventStartOrder(DatabaseHandler instance, List<Event> list)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();
                try {

                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        Event event = list.get(i);
                        event._startOrder = i + 1;

                        values.put(DatabaseHandler.KEY_E_START_ORDER, event._startOrder);

                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                                new String[]{String.valueOf(event._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean isAnyEventEnabled(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            boolean r = false;
            try {
                instance.startRunningCommand();

                String countQuery = "SELECT count(" + DatabaseHandler.KEY_E_ID + ") FROM " + DatabaseHandler.TABLE_EVENTS +
                                        " WHERE " + DatabaseHandler.KEY_E_STATUS + " != " + Event.ESTATUS_STOP;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0) > 0;
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static private void getEventPreferencesTime(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_TIME_ENABLED,
                        DatabaseHandler.KEY_E_DAYS_OF_WEEK,
                        DatabaseHandler.KEY_E_START_TIME,
                        DatabaseHandler.KEY_E_END_TIME,
                        //DatabaseHandler.KEY_E_USE_END_TIME
                        DatabaseHandler.KEY_E_TIME_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_TIME_TYPE
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);

        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesTime eventPreferences = event._eventPreferencesTime;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_TIME_ENABLED)) == 1);

                String daysOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DAYS_OF_WEEK));

                if (daysOfWeek != null)
                {
                    String[] splits = daysOfWeek.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits[0].equals(DaysOfWeekPreference.allValue))
                    {
                        eventPreferences._sunday = true;
                        eventPreferences._monday = true;
                        eventPreferences._tuesday = true;
                        eventPreferences._wednesday = true;
                        eventPreferences._thursday = true;
                        eventPreferences._friday = true;
                        eventPreferences._saturday = true;
                    }
                    else
                    {
                        eventPreferences._sunday = false;
                        eventPreferences._monday = false;
                        eventPreferences._tuesday = false;
                        eventPreferences._wednesday = false;
                        eventPreferences._thursday = false;
                        eventPreferences._friday = false;
                        eventPreferences._saturday = false;
                        for (String value : splits)
                        {
                            eventPreferences._sunday = eventPreferences._sunday || value.equals("0");
                            eventPreferences._monday = eventPreferences._monday || value.equals("1");
                            eventPreferences._tuesday = eventPreferences._tuesday || value.equals("2");
                            eventPreferences._wednesday = eventPreferences._wednesday || value.equals("3");
                            eventPreferences._thursday = eventPreferences._thursday || value.equals("4");
                            eventPreferences._friday = eventPreferences._friday || value.equals("5");
                            eventPreferences._saturday = eventPreferences._saturday || value.equals("6");
                        }
                    }
                }
                eventPreferences._startTime = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_TIME));
                eventPreferences._endTime = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_END_TIME));
                //eventPreferences._useEndTime = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_USE_END_TIME)) == 1) ? true : false;
                eventPreferences._timeType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_TIME_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_TIME_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesBattery(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_BATTERY_ENABLED,
                        DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW,
                        DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT,
                        DatabaseHandler.KEY_E_BATTERY_CHARGING,
                        DatabaseHandler.KEY_E_BATTERY_POWER_SAVE_MODE,
                        DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_BATTERY_PLUGGED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_ENABLED)) == 1);
                eventPreferences._levelLow = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW));
                eventPreferences._levelHight = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT));
                eventPreferences._charging = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_CHARGING));
                eventPreferences._powerSaveMode = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_POWER_SAVE_MODE)) == 1);
                eventPreferences._plugged = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_PLUGGED));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesCall(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_CALL_ENABLED,
                        DatabaseHandler.KEY_E_CALL_EVENT,
                        DatabaseHandler.KEY_E_CALL_CONTACTS,
                        DatabaseHandler.KEY_E_CALL_CONTACT_LIST_TYPE,
                        DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS,
                        DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_DURATION,
                        DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_TIME,
                        DatabaseHandler.KEY_E_CALL_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT,
                        DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD,
//                        DatabaseHandler.KEY_E_CALL_STOP_RINGING,
                        DatabaseHandler.KEY_E_CALL_SEND_SMS,
                        DatabaseHandler.KEY_E_CALL_SMS_TEXT,
                        DatabaseHandler.KEY_E_CALL_ANSWER_CALL,
                        DatabaseHandler.KEY_E_CALL_ANSWER_CALL_RINGING_LENGTH,
                        DatabaseHandler.KEY_E_CALL_END_CALL,
                        DatabaseHandler.KEY_E_CALL_END_CALL_CALL_LENGTH
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCall eventPreferences = event._eventPreferencesCall;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_ENABLED)) == 1);
                eventPreferences._callEvent = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_EVENT));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTACTS));
                eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTACT_LIST_TYPE));
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS));
                eventPreferences._runAfterCallEndDuration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_DURATION));
                eventPreferences._runAfterCallEndPermanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN)) == 1);
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD));
//                eventPreferences._stopRinging = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_STOP_RINGING)) == 1);
                eventPreferences._sendSMS = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_SEND_SMS)) == 1);
                eventPreferences._smsText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_SMS_TEXT));
                eventPreferences._runAfterCallEndTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_TIME));
                eventPreferences._runAfterCallEndFromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT));
                eventPreferences._answerCall = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_ANSWER_CALL)) == 1);
                eventPreferences._answerCallRingingLength = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_ANSWER_CALL_RINGING_LENGTH));
                eventPreferences._endCall = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_END_CALL)) == 1);
                eventPreferences._endCallCallLength = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_END_CALL_CALL_LENGTH));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesAccessory(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_ACCESSORY_ENABLED,
                        DatabaseHandler.KEY_E_ACCESSORY_TYPE,
                        DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesAccessories eventPreferences = event._eventPreferencesAccessories;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACCESSORY_ENABLED)) == 1);
                eventPreferences._accessoryType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACCESSORY_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesCalendar(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_CALENDAR_ENABLED,
                        DatabaseHandler.KEY_E_CALENDAR_CALENDARS,
                        DatabaseHandler.KEY_E_CALENDAR_SEARCH_FIELD,
                        DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING,
                        DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME,
                        DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME,
                        DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND,
                        DatabaseHandler.KEY_E_CALENDAR_AVAILABILITY,
                        DatabaseHandler.KEY_E_CALENDAR_STATUS,
                        //DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS,
                        DatabaseHandler.KEY_E_CALENDAR_START_BEFORE_EVENT,
                        DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_CALENDAR_ALL_EVENTS,
                        DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS,
                        DatabaseHandler.KEY_E_CALENDAR_DAY_CONTAINS_EVENT,
                        DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_ENABLED)) == 1);
                eventPreferences._calendars = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_CALENDARS));
                eventPreferences._searchField = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_SEARCH_FIELD));
                eventPreferences._searchString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING));
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME));
                eventPreferences._endTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME));
                eventPreferences._eventFound = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND)) == 1);
                eventPreferences._availability = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_AVAILABILITY));
                eventPreferences._status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_STATUS));
                //eventPreferences._ignoreAllDayEvents = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS)) == 1);
                eventPreferences._startBeforeEvent = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_START_BEFORE_EVENT));
                eventPreferences._allEvents = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_ALL_EVENTS)) == 1);
                eventPreferences._eventTodayExists = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS)) == 1);
                eventPreferences._dayContainsEvent = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_DAY_CONTAINS_EVENT));
                eventPreferences._allDayEvents = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesWifi(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                                 new String[] { DatabaseHandler.KEY_E_WIFI_ENABLED,
                                         DatabaseHandler.KEY_E_WIFI_SSID,
                                         DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE,
                                         DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED
                                                },
                DatabaseHandler.KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_ENABLED)) == 1);
                eventPreferences._SSID = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_SSID));
                eventPreferences._connectionType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesScreen(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                                 new String[] { DatabaseHandler.KEY_E_SCREEN_ENABLED,
                                         DatabaseHandler.KEY_E_SCREEN_EVENT_TYPE,
                                         DatabaseHandler.KEY_E_SCREEN_WHEN_UNLOCKED,
                                         DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED
                                                },
                DatabaseHandler.KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SCREEN_ENABLED)) == 1);
                eventPreferences._eventType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SCREEN_EVENT_TYPE));
                eventPreferences._whenUnlocked = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SCREEN_WHEN_UNLOCKED)) == 1);
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                                 new String[] { DatabaseHandler.KEY_E_BLUETOOTH_ENABLED,
                                         DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME,
                                         DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE,
                                         DatabaseHandler.KEY_E_BLUETOOTH_DEVICES_TYPE,
                                         DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED
                                                },
                DatabaseHandler.KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_ENABLED)) == 1);
                eventPreferences._adapterName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME));
                eventPreferences._connectionType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE));
                //eventPreferences._devicesType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_DEVICES_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesSMS(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_SMS_ENABLED,
                        //DatabaseHandler.KEY_E_SMS_EVENT,
                        DatabaseHandler.KEY_E_SMS_CONTACTS,
                        DatabaseHandler.KEY_E_SMS_CONTACT_LIST_TYPE,
                        DatabaseHandler.KEY_E_SMS_START_TIME,
                        DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS,
                        DatabaseHandler.KEY_E_SMS_DURATION,
                        DatabaseHandler.KEY_E_SMS_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_SMS_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT,
                        DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_ENABLED)) == 1);
                //eventPreferences._smsEvent = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_EVENT));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_CONTACTS));
                eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_CONTACT_LIST_TYPE));
                //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_PERMANENT_RUN)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_START_TIME));
                eventPreferences._fromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT));
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesNotification(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_NOTIFICATION_ENABLED,
                        DatabaseHandler.KEY_E_NOTIFICATION_APPLICATIONS,
                        //DatabaseHandler.KEY_E_NOTIFICATION_START_TIME,
                        DatabaseHandler.KEY_E_NOTIFICATION_DURATION,
                        //DatabaseHandler.KEY_E_NOTIFICATION_END_WHEN_REMOVED,
                        //DatabaseHandler.KEY_E_NOTIFICATION_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_NOTIFICATION_IN_CALL,
                        DatabaseHandler.KEY_E_NOTIFICATION_MISSED_CALL,
                        DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_NOTIFICATION_CHECK_CONTACTS,
                        DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS,
                        DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS,
                        DatabaseHandler.KEY_E_NOTIFICATION_CHECK_TEXT,
                        DatabaseHandler.KEY_E_NOTIFICATION_TEXT,
                        DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_LIST_TYPE
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_ENABLED)) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_APPLICATIONS));
                eventPreferences._inCall = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_IN_CALL)) == 1);
                eventPreferences._missedCall = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_MISSED_CALL)) == 1);
                //eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_DURATION));
                //eventPreferences._endWhenRemoved = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_END_WHEN_REMOVED)) == 1);
                //eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_PERMANENT_RUN))) == 1);
                eventPreferences._checkContacts = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CHECK_CONTACTS)) == 1);
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS));
                eventPreferences._checkText = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CHECK_TEXT)) == 1);
                eventPreferences._text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_TEXT));
                eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_LIST_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesApplication(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_APPLICATION_ENABLED,
                        DatabaseHandler.KEY_E_APPLICATION_APPLICATIONS,
                        DatabaseHandler.KEY_E_APPLICATION_DURATION,
                        DatabaseHandler.KEY_E_APPLICATION_START_TIME,
                        DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_APPLICATION_ENABLED)) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_APPLICATION_APPLICATIONS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_APPLICATION_DURATION));
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_APPLICATION_START_TIME));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesLocation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_LOCATION_ENABLED,
                        DatabaseHandler.KEY_E_LOCATION_GEOFENCES,
                        DatabaseHandler.KEY_E_LOCATION_WHEN_OUTSIDE,
                        DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_ENABLED)) == 1);
                eventPreferences._geofences = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_GEOFENCES));
                eventPreferences._whenOutside = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_WHEN_OUTSIDE)) == 1;
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED)));

            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_ORIENTATION_ENABLED,
                        DatabaseHandler.KEY_E_ORIENTATION_SIDES,
                        DatabaseHandler.KEY_E_ORIENTATION_DISTANCE,
                        DatabaseHandler.KEY_E_ORIENTATION_DISPLAY,
                        DatabaseHandler.KEY_E_ORIENTATION_IGNORE_APPLICATIONS,
                        DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT,
                        DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN,
                        DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_ENABLED)) == 1);
                eventPreferences._sides = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_SIDES));
                eventPreferences._distance = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_DISTANCE));
                eventPreferences._display = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_DISPLAY));
                eventPreferences._checkLight = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT)) == 1;
                eventPreferences._lightMin = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN)));
                eventPreferences._lightMax = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX)));
                eventPreferences._ignoredApplications = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_IGNORE_APPLICATIONS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED,
                        DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS,
                        DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE,
                        DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED)) == 1);
                eventPreferences._cellsNames = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS));
                eventPreferences._whenOutside = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE)) == 1;
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED)));

            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesNFC(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_NFC_ENABLED,
                        DatabaseHandler.KEY_E_NFC_NFC_TAGS,
                        DatabaseHandler.KEY_E_NFC_DURATION,
                        DatabaseHandler.KEY_E_NFC_START_TIME,
                        DatabaseHandler.KEY_E_NFC_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_NFC_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNFC eventPreferences = event._eventPreferencesNFC;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_ENABLED)) == 1);
                eventPreferences._nfcTags = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_NFC_TAGS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_DURATION));
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_START_TIME));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_PERMANENT_RUN)) == 1);
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesRadioSwitch(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_GPS,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_NFC,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS,
                        DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesRadioSwitch eventPreferences = event._eventPreferencesRadioSwitch;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED)) == 1);
                eventPreferences._wifi = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI));
                eventPreferences._bluetooth = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH));
                eventPreferences._mobileData = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA));
                eventPreferences._gps = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_GPS));
                eventPreferences._nfc = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_NFC));
                eventPreferences._airplaneMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE));
                eventPreferences._defaultSIMForCalls = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS));
                eventPreferences._defaultSIMForSMS = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS));
                eventPreferences._simOnOff = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesAlarmClock(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED,
                        DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME,
                        DatabaseHandler.KEY_E_ALARM_CLOCK_DURATION,
                        DatabaseHandler.KEY_E_ALARM_CLOCK_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_ALARM_CLOCK_APPLICATIONS,
                        DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesAlarmClock eventPreferences = event._eventPreferencesAlarmClock;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_PERMANENT_RUN)) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_APPLICATIONS));
                eventPreferences._alarmPackageName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesDeviceBoot(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED,
                        DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME,
                        DatabaseHandler.KEY_E_DEVICE_BOOT_DURATION,
                        DatabaseHandler.KEY_E_DEVICE_BOOT_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesDeviceBoot eventPreferences = event._eventPreferencesDeviceBoot;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DEVICE_BOOT_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DEVICE_BOOT_PERMANENT_RUN)) == 1);
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesSoundProfile(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED,
                        DatabaseHandler.KEY_E_SOUND_PROFILE_RINGER_MODES,
                        DatabaseHandler.KEY_E_SOUND_PROFILE_ZEN_MODES,
                        DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesSoundProfile eventPreferences = event._eventPreferencesSoundProfile;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED)) == 1);
                eventPreferences._ringerModes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SOUND_PROFILE_RINGER_MODES));
                eventPreferences._zenModes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SOUND_PROFILE_ZEN_MODES));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesPeriodic(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_PERIODIC_ENABLED,
                        DatabaseHandler.KEY_E_PERIODIC_START_TIME,
                        DatabaseHandler.KEY_E_PERIODIC_COUNTER,
                        DatabaseHandler.KEY_E_PERIODIC_DURATION,
                        DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL,
                        DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesPeriodic eventPreferences = event._eventPreferencesPeriodic;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_ENABLED)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_START_TIME));
                eventPreferences._counter = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_COUNTER));
                eventPreferences._multipleInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_DURATION));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesVolumes(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_VOLUMES_ENABLED,
                        DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM,
                        DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO,
                        DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO,
                        DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO,
                        DatabaseHandler.KEY_E_VOLUMES_ALARM_TO,
                        DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO,
                        DatabaseHandler.KEY_E_VOLUMES_VOICE_TO,
                        DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO,
                        DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesVolumes eventPreferences = event._eventPreferencesVolumes;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_ENABLED)) == 1);
                eventPreferences._volumeRingtoneFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM));
                eventPreferences._volumeNotificationFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM));
                eventPreferences._volumeMediaFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM));
                eventPreferences._volumeAlarmFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM));
                eventPreferences._volumeSystemFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM));
                eventPreferences._volumeVoiceFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM));
                eventPreferences._volumeBluetoothSCOFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM));
                eventPreferences._volumeRingtoneTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO));
                eventPreferences._volumeNotificationTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO));
                eventPreferences._volumeMediaTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO));
                eventPreferences._volumeAlarmTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_ALARM_TO));
                eventPreferences._volumeSystemTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO));
                eventPreferences._volumeVoiceTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_VOICE_TO));
                eventPreferences._volumeBluetoothSCOTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesActivatedProfile(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED,
                        DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_ACTIVATED_PROFILE_START_PROFILE,
                        DatabaseHandler.KEY_E_ACTIVATED_PROFILE_END_PROFILE,
                        DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesActivatedProfile eventPreferences = event._eventPreferencesActivatedProfile;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED)) == 1);
                eventPreferences._startProfile = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_START_PROFILE));
                eventPreferences._endProfile = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_END_PROFILE));
                eventPreferences._running = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesRoaming(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_ROAMING_ENABLED,
                        DatabaseHandler.KEY_E_ROAMING_CHECK_NETWORK,
                        DatabaseHandler.KEY_E_ROAMING_CHECK_DATA,
                        DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesRoaming eventPreferences = event._eventPreferencesRoaming;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ROAMING_ENABLED)) == 1);
                eventPreferences._checkNetwork = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ROAMING_CHECK_NETWORK)) == 1);
                eventPreferences._checkData = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ROAMING_CHECK_DATA)) == 1);
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesVPN(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_VPN_ENABLED,
                        DatabaseHandler.KEY_E_VPN_CONNECTION_STATUS,
                        DatabaseHandler.KEY_E_VPN_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesVPN eventPreferences = event._eventPreferencesVPN;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VPN_ENABLED)) == 1);
                eventPreferences._connectionStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VPN_CONNECTION_STATUS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_VPN_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesBrightness(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[] { DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED,
                        DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_FROM,
                        DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM,
                        DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_TO,
                        DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_TO,
                        DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[] { String.valueOf(event._id) }, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBrightness eventPreferences = event._eventPreferencesBrightness;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED)) == 1);
                eventPreferences._operatorFrom = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_FROM));
                eventPreferences._brightnessLevelFrom = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM));
                eventPreferences._operatorTo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_TO));
                eventPreferences._brightnessLevelTo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_TO));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesMusic(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[] { DatabaseHandler.KEY_E_MUSIC_ENABLED,
                        DatabaseHandler.KEY_E_MUSIC_MUSIC_STATE,
                        DatabaseHandler.KEY_E_MUSIC_APPLICATIONS,
                        DatabaseHandler.KEY_E_MUSIC_SENSOR_PASSED
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[] { String.valueOf(event._id) }, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesMusic eventPreferences = event._eventPreferencesMusic;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MUSIC_ENABLED)) == 1);
                eventPreferences._musicState = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MUSIC_MUSIC_STATE));
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MUSIC_APPLICATIONS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MUSIC_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    static private void getEventPreferencesCallControl(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                new String[]{DatabaseHandler.KEY_E_CALL_CONTROL_ENABLED,
                        DatabaseHandler.KEY_E_CALL_CONTROL_CALL_DIRECTION,
                        DatabaseHandler.KEY_E_CALL_CONTROL_CONTACTS,
                        //DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_LIST_TYPE,
                        DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_GROUPS,
                        DatabaseHandler.KEY_E_CALL_CONTROL_NOT_IN_CONTACTS,
                        DatabaseHandler.KEY_E_CALL_CONTROL_BLOCK_CALLS,
                        DatabaseHandler.KEY_E_CALL_CONTROL_SEND_SMS,
                        DatabaseHandler.KEY_E_CALL_CONTROL_SMS_TEXT,
                        DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME,
                        DatabaseHandler.KEY_E_CALL_CONTROL_DURATION,
                        DatabaseHandler.KEY_E_CALL_CONTROL_PERMANENT_RUN,
                        DatabaseHandler.KEY_E_CALL_CONTROL_SENSOR_PASSED,
                        DatabaseHandler.KEY_E_CALL_CONTROL_CONTROL_TYPE
                },
                DatabaseHandler.KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        //if (cursor != null) {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCallControl eventPreferences = event._eventPreferencesCallControl;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_ENABLED)) == 1);
                eventPreferences._callDirection = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_CALL_DIRECTION));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_CONTACTS));
                //eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_LIST_TYPE));
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_GROUPS));
                eventPreferences._notInContacts = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_NOT_IN_CONTACTS)) == 1);
                //eventPreferences._blockCalls = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_BLOCK_CALLS)) == 1);
                eventPreferences._sendSMS = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_SEND_SMS)) == 1);
                eventPreferences._smsText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_SMS_TEXT));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_PERMANENT_RUN)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME));
                eventPreferences._controlType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_CONTROL_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_SENSOR_PASSED)));
            }
            cursor.close();
        //}
    }

    // this is called only from getEvent and getAllEvents
    // for this is not needed to calling importExportLock.lock();
    static private void getEventPreferences(Event event, SQLiteDatabase db) {
        getEventPreferencesTime(event, db);
        getEventPreferencesBattery(event, db);
        getEventPreferencesCall(event, db);
        getEventPreferencesAccessory(event, db);
        getEventPreferencesCalendar(event, db);
        getEventPreferencesWifi(event, db);
        getEventPreferencesScreen(event, db);
        getEventPreferencesBluetooth(event, db);
        getEventPreferencesSMS(event, db);
        getEventPreferencesNotification(event, db);
        getEventPreferencesApplication(event, db);
        getEventPreferencesLocation(event, db);
        getEventPreferencesOrientation(event, db);
        getEventPreferencesMobileCells(event, db);
        getEventPreferencesNFC(event, db);
        getEventPreferencesRadioSwitch(event, db);
        getEventPreferencesAlarmClock(event, db);
        getEventPreferencesDeviceBoot(event, db);
        getEventPreferencesSoundProfile(event, db);
        getEventPreferencesPeriodic(event, db);
        getEventPreferencesVolumes(event, db);
        getEventPreferencesActivatedProfile(event, db);
        getEventPreferencesRoaming(event, db);
        getEventPreferencesVPN(event, db);
        getEventPreferencesBrightness(event, db);
        getEventPreferencesMusic(event, db);
        getEventPreferencesCallControl(event, db);
    }

    static private void updateEventPreferencesTime(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesTime eventPreferences = event._eventPreferencesTime;

        String daysOfWeek = "";
        if (eventPreferences._sunday) daysOfWeek = daysOfWeek + "0|";
        if (eventPreferences._monday) daysOfWeek = daysOfWeek + "1|";
        if (eventPreferences._tuesday) daysOfWeek = daysOfWeek + "2|";
        if (eventPreferences._wednesday) daysOfWeek = daysOfWeek + "3|";
        if (eventPreferences._thursday) daysOfWeek = daysOfWeek + "4|";
        if (eventPreferences._friday) daysOfWeek = daysOfWeek + "5|";
        if (eventPreferences._saturday) daysOfWeek = daysOfWeek + "6|";

        values.put(DatabaseHandler.KEY_E_TIME_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_DAYS_OF_WEEK, daysOfWeek);
        values.put(DatabaseHandler.KEY_E_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_END_TIME, eventPreferences._endTime);
        //values.put(DatabaseHandler.KEY_E_USE_END_TIME, (eventPreferences._useEndTime) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_TIME_TYPE, eventPreferences._timeType);
        values.put(DatabaseHandler.KEY_E_TIME_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesBattery(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

        values.put(DatabaseHandler.KEY_E_BATTERY_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW, eventPreferences._levelLow);
        values.put(DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT, eventPreferences._levelHight);
        values.put(DatabaseHandler.KEY_E_BATTERY_CHARGING, eventPreferences._charging);
        values.put(DatabaseHandler.KEY_E_BATTERY_POWER_SAVE_MODE, eventPreferences._powerSaveMode ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_BATTERY_PLUGGED, eventPreferences._plugged);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesCall(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCall eventPreferences = event._eventPreferencesCall;

        values.put(DatabaseHandler.KEY_E_CALL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_EVENT, eventPreferences._callEvent);
        values.put(DatabaseHandler.KEY_E_CALL_CONTACTS, eventPreferences._contacts);
        values.put(DatabaseHandler.KEY_E_CALL_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_DURATION, eventPreferences._runAfterCallEndDuration);
        values.put(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN, (eventPreferences._runAfterCallEndPermanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD, eventPreferences._forSIMCard);
//        values.put(DatabaseHandler.KEY_E_CALL_STOP_RINGING, (eventPreferences._stopRinging) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_SEND_SMS, (eventPreferences._sendSMS) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_SMS_TEXT, eventPreferences._smsText);
        values.put(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_TIME, eventPreferences._runAfterCallEndTime);
        values.put(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT, eventPreferences._runAfterCallEndFromSIMSlot);
        values.put(DatabaseHandler.KEY_E_CALL_ANSWER_CALL, (eventPreferences._answerCall) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_ANSWER_CALL_RINGING_LENGTH, eventPreferences._answerCallRingingLength);
        values.put(DatabaseHandler.KEY_E_CALL_END_CALL, (eventPreferences._endCall) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_END_CALL_CALL_LENGTH, eventPreferences._endCallCallLength);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesAccessory(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesAccessories eventPreferences = event._eventPreferencesAccessories;

        values.put(DatabaseHandler.KEY_E_ACCESSORY_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ACCESSORY_TYPE, eventPreferences._accessoryType);
        values.put(DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesCalendar(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

        values.put(DatabaseHandler.KEY_E_CALENDAR_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALENDAR_CALENDARS, eventPreferences._calendars);
        values.put(DatabaseHandler.KEY_E_CALENDAR_SEARCH_FIELD, eventPreferences._searchField);
        values.put(DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING, eventPreferences._searchString);
        values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME, eventPreferences._endTime);
        values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND, (eventPreferences._eventFound) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALENDAR_AVAILABILITY, eventPreferences._availability);
        values.put(DatabaseHandler.KEY_E_CALENDAR_STATUS, eventPreferences._status);
        //values.put(DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, (eventPreferences._ignoreAllDayEvents) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALENDAR_START_BEFORE_EVENT, eventPreferences._startBeforeEvent);
        values.put(DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_CALENDAR_ALL_EVENTS, (eventPreferences._allEvents) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS, (eventPreferences._eventTodayExists) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALENDAR_DAY_CONTAINS_EVENT, eventPreferences._dayContainsEvent);
        values.put(DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS, eventPreferences._allDayEvents);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesWifi(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

        values.put(DatabaseHandler.KEY_E_WIFI_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_WIFI_SSID, eventPreferences._SSID);
        values.put(DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE, eventPreferences._connectionType);
        values.put(DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesScreen(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

        values.put(DatabaseHandler.KEY_E_SCREEN_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_SCREEN_EVENT_TYPE, eventPreferences._eventType);
        values.put(DatabaseHandler.KEY_E_SCREEN_WHEN_UNLOCKED, (eventPreferences._whenUnlocked) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

        values.put(DatabaseHandler.KEY_E_BLUETOOTH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME, eventPreferences._adapterName);
        values.put(DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE, eventPreferences._connectionType);
        //values.put(DatabaseHandler.KEY_E_BLUETOOTH_DEVICES_TYPE, eventPreferences._devicesType);
        values.put(DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesSMS(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

        values.put(DatabaseHandler.KEY_E_SMS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        //values.put(DatabaseHandler.KEY_E_SMS_EVENT, eventPreferences._smsEvent);
        values.put(DatabaseHandler.KEY_E_SMS_CONTACTS, eventPreferences._contacts);
        values.put(DatabaseHandler.KEY_E_SMS_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(DatabaseHandler.KEY_E_SMS_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_SMS_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_SMS_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT, eventPreferences._fromSIMSlot);
        values.put(DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD, eventPreferences._forSIMCard);

        values.put(DatabaseHandler.KEY_E_SMS_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesNotification(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

        values.put(DatabaseHandler.KEY_E_NOTIFICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_APPLICATIONS, eventPreferences._applications);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_IN_CALL, (eventPreferences._inCall) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_MISSED_CALL, (eventPreferences._missedCall) ? 1 : 0);
        //values.put(DatabaseHandler.KEY_E_NOTIFICATION_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_DURATION, eventPreferences._duration);
        //values.put(DatabaseHandler.KEY_E_NOTIFICATION_END_WHEN_REMOVED, (eventPreferences._endWhenRemoved) ? 1 : 0);
        //values.put(DatabaseHandler.KEY_E_NOTIFICATION_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_CHECK_CONTACTS, (eventPreferences._checkContacts) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS, eventPreferences._contacts);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_CHECK_TEXT, (eventPreferences._checkText) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_TEXT, eventPreferences._text);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesApplication(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

        values.put(DatabaseHandler.KEY_E_APPLICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_APPLICATION_APPLICATIONS, eventPreferences._applications);
        values.put(DatabaseHandler.KEY_E_APPLICATION_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_APPLICATION_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesLocation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

        values.put(DatabaseHandler.KEY_E_LOCATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_LOCATION_GEOFENCES, eventPreferences._geofences);
        values.put(DatabaseHandler.KEY_E_LOCATION_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

        values.put(DatabaseHandler.KEY_E_ORIENTATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_SIDES, eventPreferences._sides);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_DISTANCE, eventPreferences._distance);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_DISPLAY, eventPreferences._display);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT, (eventPreferences._checkLight) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN, eventPreferences._lightMin);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX, eventPreferences._lightMax);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_IGNORE_APPLICATIONS, eventPreferences._ignoredApplications);
        values.put(DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

        values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cellsNames);
        values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD, eventPreferences._forSIMCard);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesNFC(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNFC eventPreferences = event._eventPreferencesNFC;

        values.put(DatabaseHandler.KEY_E_NFC_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NFC_NFC_TAGS, eventPreferences._nfcTags);
        values.put(DatabaseHandler.KEY_E_NFC_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_NFC_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_NFC_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_NFC_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesRadioSwitch(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesRadioSwitch eventPreferences = event._eventPreferencesRadioSwitch;

        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI, eventPreferences._wifi);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH, eventPreferences._bluetooth);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA, eventPreferences._mobileData);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_GPS, eventPreferences._gps);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_NFC, eventPreferences._nfc);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE, eventPreferences._airplaneMode);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, eventPreferences._defaultSIMForCalls);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, eventPreferences._defaultSIMForSMS);
        values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF, eventPreferences._simOnOff);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesAlarmClock(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesAlarmClock eventPreferences = event._eventPreferencesAlarmClock;

        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_APPLICATIONS, eventPreferences._applications);
        values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME, eventPreferences._alarmPackageName);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesDeviceBoot(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesDeviceBoot eventPreferences = event._eventPreferencesDeviceBoot;

        values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesSoundProfile(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesSoundProfile eventPreferences = event._eventPreferencesSoundProfile;

        values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_RINGER_MODES, eventPreferences._ringerModes);
        values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_ZEN_MODES, eventPreferences._zenModes);
        values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesPeriodic(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesPeriodic eventPreferences = event._eventPreferencesPeriodic;

        values.put(DatabaseHandler.KEY_E_PERIODIC_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_PERIODIC_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_PERIODIC_COUNTER, eventPreferences._counter);
        values.put(DatabaseHandler.KEY_E_PERIODIC_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL, eventPreferences._multipleInterval);
        values.put(DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesVolumes(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesVolumes eventPreferences = event._eventPreferencesVolumes;

        values.put(DatabaseHandler.KEY_E_VOLUMES_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM, eventPreferences._volumeRingtoneFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM, eventPreferences._volumeNotificationFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM, eventPreferences._volumeMediaFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM, eventPreferences._volumeAlarmFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM, eventPreferences._volumeSystemFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM, eventPreferences._volumeVoiceFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM, eventPreferences._volumeBluetoothSCOFrom);
        values.put(DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO, eventPreferences._volumeRingtoneTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO, eventPreferences._volumeNotificationTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO, eventPreferences._volumeMediaTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_ALARM_TO, eventPreferences._volumeAlarmTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO, eventPreferences._volumeSystemTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_VOICE_TO, eventPreferences._volumeVoiceTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO, eventPreferences._volumeBluetoothSCOTo);
        values.put(DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesActivatedProfile(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesActivatedProfile eventPreferences = event._eventPreferencesActivatedProfile;

        values.put(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_START_PROFILE, eventPreferences._startProfile);
        values.put(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_END_PROFILE, eventPreferences._endProfile);
        values.put(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING, eventPreferences._running);
        values.put(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesRoaming(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesRoaming eventPreferences = event._eventPreferencesRoaming;

        values.put(DatabaseHandler.KEY_E_ROAMING_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ROAMING_CHECK_NETWORK, (eventPreferences._checkNetwork) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ROAMING_CHECK_DATA, (eventPreferences._checkData) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD, eventPreferences._forSIMCard);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesVPN(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesVPN eventPreferences = event._eventPreferencesVPN;

        values.put(DatabaseHandler.KEY_E_VPN_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_VPN_CONNECTION_STATUS, eventPreferences._connectionStatus);
        values.put(DatabaseHandler.KEY_E_VPN_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesBrightness(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBrightness eventPreferences = event._eventPreferencesBrightness;

        values.put(DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_FROM, eventPreferences._operatorFrom);
        values.put(DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM, eventPreferences._brightnessLevelFrom);
        values.put(DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_TO, eventPreferences._operatorTo);
        values.put(DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_TO, eventPreferences._brightnessLevelTo);
        values.put(DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesMusic(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesMusic eventPreferences = event._eventPreferencesMusic;

        values.put(DatabaseHandler.KEY_E_MUSIC_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_MUSIC_MUSIC_STATE, eventPreferences._musicState);
        values.put(DatabaseHandler.KEY_E_MUSIC_APPLICATIONS, eventPreferences._applications);
        values.put(DatabaseHandler.KEY_E_MUSIC_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    static private void updateEventPreferencesCallControl(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCallControl eventPreferences = event._eventPreferencesCallControl;

        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_CALL_DIRECTION, eventPreferences._callDirection);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_CONTACTS, eventPreferences._contacts);
        //values.put(DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_NOT_IN_CONTACTS, (eventPreferences._notInContacts) ? 1 : 0);
        //values.put(DatabaseHandler.KEY_E_CALL_CONTROL_BLOCK_CALLS, (eventPreferences._blockCalls) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_SEND_SMS, (eventPreferences._sendSMS) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_SMS_TEXT, eventPreferences._smsText);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_DURATION, eventPreferences._duration);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME, eventPreferences._startTime);
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(DatabaseHandler.KEY_E_CALL_CONTROL_CONTROL_TYPE, eventPreferences._controlType);

        // updating row
        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    // this is called only from addEvent and updateEvent.
    // for this is not needed to calling importExportLock.lock();
    static private void updateEventPreferences(Event event, SQLiteDatabase db) {
        updateEventPreferencesTime(event, db);
        updateEventPreferencesBattery(event, db);
        updateEventPreferencesCall(event, db);
        updateEventPreferencesAccessory(event, db);
        updateEventPreferencesCalendar(event, db);
        updateEventPreferencesWifi(event, db);
        updateEventPreferencesScreen(event, db);
        updateEventPreferencesBluetooth(event, db);
        updateEventPreferencesSMS(event, db);
        updateEventPreferencesNotification(event, db);
        updateEventPreferencesApplication(event, db);
        updateEventPreferencesLocation(event, db);
        updateEventPreferencesOrientation(event, db);
        updateEventPreferencesMobileCells(event, db);
        updateEventPreferencesNFC(event, db);
        updateEventPreferencesRadioSwitch(event, db);
        updateEventPreferencesAlarmClock(event, db);
        updateEventPreferencesDeviceBoot(event, db);
        updateEventPreferencesSoundProfile(event, db);
        updateEventPreferencesPeriodic(event, db);
        updateEventPreferencesVolumes(event, db);
        updateEventPreferencesActivatedProfile(event, db);
        updateEventPreferencesRoaming(event, db);
        updateEventPreferencesVPN(event, db);
        updateEventPreferencesBrightness(event, db);
        updateEventPreferencesMusic(event, db);
        updateEventPreferencesCallControl(event, db);
    }


    static int getEventStatus(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            int eventStatus = 0;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_STATUS
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_STATUS));
                    }

                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return eventStatus;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventStatus(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                int status = event.getStatus();
                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_STATUS, status);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventStatus", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventBlocked(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_BLOCKED, event._blocked ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventBlocked", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void unblockAllEvents(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_BLOCKED, 0);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(DatabaseHandler.TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.unblockAllEvents", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateAllEventsStatus(DatabaseHandler instance, int fromStatus, int toStatus)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_STATUS, toStatus);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_STATUS + " = ?",
                            new String[]{String.valueOf(fromStatus)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateAllEventsStatus", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static long getEventIdByName(DatabaseHandler instance, String name)
    {
        instance.importExportLock.lock();
        try {
            long id = 0;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{DatabaseHandler.KEY_E_ID},
                        "trim(" + DatabaseHandler.KEY_E_NAME + ")=?",
                        new String[]{name}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                    }

                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return id;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String getEventName(DatabaseHandler instance, long event_id)
    {
        instance.importExportLock.lock();
        try {
            String name = "";
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{DatabaseHandler.KEY_E_NAME},
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{Long.toString(event_id)}, null, null, null, null);

                //if (cursor != null) {
                if (cursor.moveToFirst())
                    name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NAME));
                cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return name;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getEventSensorPassed(DatabaseHandler instance, EventPreferences eventPreferences, int eventType)
    {
        if (eventPreferences._event != null) {
            instance.importExportLock.lock();
            try {
                int sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                try {
                    instance.startRunningCommand();

                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = instance.getMyWritableDatabase();

                    String sensorPassedField = "";
                    switch (eventType) {
                        case DatabaseHandler.ETYPE_BLUETOOTH:
                            sensorPassedField = DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_LOCATION:
                            sensorPassedField = DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_MOBILE_CELLS:
                            sensorPassedField = DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_ORIENTATION:
                            sensorPassedField = DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_WIFI:
                            sensorPassedField = DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_TIME:
                            sensorPassedField = DatabaseHandler.KEY_E_TIME_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_BATTERY:
                        case DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL:
                            sensorPassedField = DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_CALL:
                            sensorPassedField = DatabaseHandler.KEY_E_CALL_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_ACCESSORY:
                            sensorPassedField = DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_CALENDAR:
                            sensorPassedField = DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_SCREEN:
                            sensorPassedField = DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_BRIGHTNESS:
                            sensorPassedField = DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_SMS:
                            sensorPassedField = DatabaseHandler.KEY_E_SMS_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_NOTIFICATION:
                            sensorPassedField = DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_APPLICATION:
                            sensorPassedField = DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_NFC:
                            sensorPassedField = DatabaseHandler.KEY_E_NFC_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH:
                            sensorPassedField = DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_ALARM_CLOCK:
                            sensorPassedField = DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_DEVICE_BOOT:
                            sensorPassedField = DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_SOUND_PROFILE:
                            sensorPassedField = DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_PERIODIC:
                            sensorPassedField = DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_VOLUMES:
                            sensorPassedField = DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_ACTIVATED_PROFILE:
                            sensorPassedField = DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_ROAMING:
                            sensorPassedField = DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_VPN:
                            sensorPassedField = DatabaseHandler.KEY_E_VPN_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_MUSIC:
                            sensorPassedField = DatabaseHandler.KEY_E_MUSIC_SENSOR_PASSED;
                            break;
                        case DatabaseHandler.ETYPE_CALL_CONTROL:
                            sensorPassedField = DatabaseHandler.KEY_E_CALL_CONTROL_SENSOR_PASSED;
                            break;
                    }

                    Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                            new String[]{
                                    sensorPassedField
                            },
                            DatabaseHandler.KEY_E_ID + "=?",
                            new String[]{String.valueOf(eventPreferences._event._id)}, null, null, null, null);
                    //if (cursor != null) {
                        cursor.moveToFirst();

                        if (cursor.getCount() > 0) {
                            sensorPassed = cursor.getInt(cursor.getColumnIndexOrThrow(sensorPassedField));
                        }

                        cursor.close();
                    //}

                    //db.close();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                return sensorPassed;
            } finally {
                instance.stopRunningCommand();
            }
        }
        else
            return EventPreferences.SENSOR_PASSED_NOT_PASSED;
    }

    static void updateEventSensorPassed(DatabaseHandler instance, Event event, int eventType)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                int sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                String sensorPassedField = "";
                switch (eventType) {
                    case DatabaseHandler.ETYPE_BLUETOOTH:
                        sensorPassed = event._eventPreferencesBluetooth.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_LOCATION:
                        sensorPassed = event._eventPreferencesLocation.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_MOBILE_CELLS:
                        sensorPassed = event._eventPreferencesMobileCells.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_ORIENTATION:
                        sensorPassed = event._eventPreferencesOrientation.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_WIFI:
                        sensorPassed = event._eventPreferencesWifi.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_TIME:
                        sensorPassed = event._eventPreferencesTime.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_TIME_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_BATTERY:
                    case DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL:
                        sensorPassed = event._eventPreferencesBattery.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_CALL:
                        sensorPassed = event._eventPreferencesCall.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_CALL_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_ACCESSORY:
                        sensorPassed = event._eventPreferencesAccessories.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_CALENDAR:
                        sensorPassed = event._eventPreferencesCalendar.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_SCREEN:
                        sensorPassed = event._eventPreferencesScreen.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_BRIGHTNESS:
                        sensorPassed = event._eventPreferencesBrightness.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_SMS:
                        sensorPassed = event._eventPreferencesSMS.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_SMS_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_NOTIFICATION:
                        sensorPassed = event._eventPreferencesNotification.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_APPLICATION:
                        sensorPassed = event._eventPreferencesApplication.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_NFC:
                        sensorPassed = event._eventPreferencesNFC.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_NFC_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_RADIO_SWITCH:
                        sensorPassed = event._eventPreferencesRadioSwitch.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_ALARM_CLOCK:
                        sensorPassed = event._eventPreferencesAlarmClock.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_DEVICE_BOOT:
                        sensorPassed = event._eventPreferencesDeviceBoot.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_SOUND_PROFILE:
                        sensorPassed = event._eventPreferencesSoundProfile.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_PERIODIC:
                        sensorPassed = event._eventPreferencesPeriodic.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_VOLUMES:
                        sensorPassed = event._eventPreferencesVolumes.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_ACTIVATED_PROFILE:
                        sensorPassed = event._eventPreferencesActivatedProfile.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_ROAMING:
                        sensorPassed = event._eventPreferencesRoaming.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_VPN:
                        sensorPassed = event._eventPreferencesVPN.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_VPN_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_MUSIC:
                        sensorPassed = event._eventPreferencesMusic.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_MUSIC_SENSOR_PASSED;
                        break;
                    case DatabaseHandler.ETYPE_CALL_CONTROL:
                        sensorPassed = event._eventPreferencesCallControl.getSensorPassed();
                        sensorPassedField = DatabaseHandler.KEY_E_CALL_CONTROL_SENSOR_PASSED;
                        break;
                }
                ContentValues values = new ContentValues();
                values.put(sensorPassedField, sensorPassed);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventSensorPassed", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateAllEventSensorsPassedForEvent(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED, event._eventPreferencesBluetooth.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED, event._eventPreferencesLocation.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED, event._eventPreferencesMobileCells.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED, event._eventPreferencesOrientation.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED, event._eventPreferencesWifi.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED, event._eventPreferencesApplication.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED, event._eventPreferencesBattery.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED, event._eventPreferencesCalendar.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_CALL_SENSOR_PASSED, event._eventPreferencesCall.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_NFC_SENSOR_PASSED, event._eventPreferencesNFC.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED, event._eventPreferencesNotification.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED, event._eventPreferencesAccessories.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED, event._eventPreferencesRadioSwitch.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED, event._eventPreferencesScreen.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED, event._eventPreferencesBrightness.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_SMS_SENSOR_PASSED, event._eventPreferencesSMS.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_TIME_SENSOR_PASSED, event._eventPreferencesTime.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED, event._eventPreferencesAlarmClock.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED, event._eventPreferencesDeviceBoot.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED, event._eventPreferencesSoundProfile.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED, event._eventPreferencesPeriodic.getSensorPassed());
                values.put(DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED, event._eventPreferencesVolumes.getSensorPassed());

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateAllEventSensorsPassedForEvent", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateAllEventsSensorsPassed(DatabaseHandler instance, int sensorPassed)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_CALL_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_NFC_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_SMS_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_TIME_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED, sensorPassed);
                values.put(DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED, sensorPassed);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(DatabaseHandler.TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateAllEventsSensorsPassed", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getTypeEventsCount(DatabaseHandler instance, int eventType/*, boolean onlyRunning*/)
    {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                final String countQuery;
                String eventTypeChecked;
                //if (onlyRunning)
                //    eventTypeChecked = DatabaseHandler.KEY_E_STATUS + "=2";  //  only running events
                //else
                    eventTypeChecked = DatabaseHandler.KEY_E_STATUS + "!=0";  //  only not stopped events
                if (eventType != DatabaseHandler.ETYPE_ALL) {
                    eventTypeChecked = eventTypeChecked  + " AND ";
                    if (eventType == DatabaseHandler.ETYPE_TIME)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_TIME_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_BATTERY)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_BATTERY_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_BATTERY_ENABLED + "=1" + " AND " +
                                "((" + DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW + " > 0) OR (" + DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT + " < 100))";
                    else if (eventType == DatabaseHandler.ETYPE_CALL)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_CALL_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_ACCESSORY)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_ACCESSORY_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_CALENDAR)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_CALENDAR_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_WIFI_CONNECTED)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_WIFI_ENABLED + "=1" + " AND " +
                                "(" + DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE + "=0 OR " + DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE + "=2)";
                    else if (eventType == DatabaseHandler.ETYPE_WIFI_NEARBY)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_WIFI_ENABLED + "=1" + " AND " +
                                "(" + DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE + "=1 OR " + DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE + "=3)";
                    else if (eventType == DatabaseHandler.ETYPE_SCREEN)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_SCREEN_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                                "(" + DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0 OR " + DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE + "=2)";
                    else if (eventType == DatabaseHandler.ETYPE_BLUETOOTH_NEARBY)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                                "(" + DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE + "=1 OR " + DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE + "=3)";
                    else if (eventType == DatabaseHandler.ETYPE_SMS)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_SMS_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_NOTIFICATION)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_NOTIFICATION_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_APPLICATION)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_APPLICATION_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_LOCATION)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_LOCATION_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_ORIENTATION)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_ORIENTATION_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_MOBILE_CELLS)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_NFC)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_NFC_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_WIFI)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_GPS)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_GPS + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_NFC)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_NFC + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_RADIO_SWITCH_SIM_ON_OFF)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_ALARM_CLOCK)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_TIME_TWILIGHT)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_TIME_ENABLED + "=1" + " AND " +
                                DatabaseHandler.KEY_E_TIME_TYPE + "!=0";
                    else if (eventType == DatabaseHandler.ETYPE_DEVICE_BOOT)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_SOUND_PROFILE)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_PERIODIC)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_PERIODIC_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_VOLUMES)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_VOLUMES_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_ACTIVATED_PROFILE)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_ROAMING)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_ROAMING_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_VPN)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_VPN_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_BRIGHTNESS)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_MUSIC)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_MUSIC_ENABLED + "=1";
                    else if (eventType == DatabaseHandler.ETYPE_CALL_CONTROL)
                        eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_CALL_CONTROL_ENABLED + "=1";
                }

                countQuery = "SELECT  count(*) FROM " + DatabaseHandler.TABLE_EVENTS +
                        " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getNotStoppedEventsCount(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                final String countQuery;
                countQuery = "SELECT  count(*) FROM " + DatabaseHandler.TABLE_EVENTS +
                                " WHERE " + DatabaseHandler.KEY_E_STATUS + "!=0";

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventCalendarTimes(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME, event._eventPreferencesCalendar._startTime);
                values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME, event._eventPreferencesCalendar._endTime);
                values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND, event._eventPreferencesCalendar._eventFound ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventCalendarTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void setEventCalendarTimes(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME,
                                DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME,
                                DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCalendar._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME));
                        event._eventPreferencesCalendar._endTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME));
                        event._eventPreferencesCalendar._eventFound = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND)) == 1);
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventCalendarTodayExists(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS, event._eventPreferencesCalendar._eventTodayExists ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventCalendarTodayExists", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean getEventInDelayStart(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            int eventInDelay = 0;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_IS_IN_DELAY_START
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventInDelay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_IS_IN_DELAY_START));
                    }

                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return (eventInDelay == 1);
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventInDelayStart(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_START_STATUS_TIME, event._startStatusTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventInDelayStart", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void resetAllEventsInDelayStart(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_START, 0);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(DatabaseHandler.TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.resetAllEventsInDelayStart", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean getEventInDelayEnd(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            int eventInDelay = 0;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_IS_IN_DELAY_END
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventInDelay = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_IS_IN_DELAY_END));
                    }

                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return (eventInDelay == 1);
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventInDelayEnd(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
                values.put(DatabaseHandler.KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateEventInDelayEnd", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateSMSStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_SMS_START_TIME, event._eventPreferencesSMS._startTime);
                values.put(DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT, event._eventPreferencesSMS._fromSIMSlot);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateSMSStartTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getSMSStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_SMS_START_TIME,
                                DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesSMS._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_START_TIME));
                        event._eventPreferencesSMS._fromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT));
                        //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateNFCStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_NFC_START_TIME, event._eventPreferencesNFC._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateNFCStartTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getNFCStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_NFC_START_TIME
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesNFC._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NFC_START_TIME));
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateCallRunAfterCallEndTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_TIME, event._eventPreferencesCall._runAfterCallEndTime);
                values.put(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT, event._eventPreferencesCall._runAfterCallEndFromSIMSlot);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateCallStartTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getCallRunAfterCallEndTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_TIME,
                                DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCall._runAfterCallEndTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_TIME));
                        event._eventPreferencesCall._runAfterCallEndFromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT));
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    /*
    static void updateCallRingingTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_CALL_RINGING_TIME, event._eventPreferencesCall._ringingTime);
                values.put(DatabaseHandler.KEY_E_CALL_RINGING_FROM_SIM_SLOT, event._eventPreferencesCall._ringingFromSIMSlot);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateCallStartTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getCallRingingTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_CALL_RINGING_TIME,
                                DatabaseHandler.KEY_E_CALL_RINGING_FROM_SIM_SLOT
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCall._ringingTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RINGING_TIME));
                        event._eventPreferencesCall._ringingFromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_RINGING_FROM_SIM_SLOT));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }
    */

    static void updateAlarmClockStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME, event._eventPreferencesAlarmClock._startTime);
                values.put(DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME, event._eventPreferencesAlarmClock._alarmPackageName);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateAlarmClockStartTime", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getAlarmClockStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME,
                                DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesAlarmClock._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME));
                        event._eventPreferencesAlarmClock._alarmPackageName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME));
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateDeviceBootStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME, event._eventPreferencesDeviceBoot._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateDeviceBootStartTime", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getDeviceBootStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesDeviceBoot._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME));
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updatePeriodicCounter(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_PERIODIC_COUNTER, event._eventPreferencesPeriodic._counter);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updatePeriodicCounter", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updatePeriodicStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_PERIODIC_START_TIME, event._eventPreferencesPeriodic._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updatePeriodicStartTime", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getPeriodicStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_PERIODIC_COUNTER,
                                DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL,
                                DatabaseHandler.KEY_E_PERIODIC_START_TIME
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        int multiplyInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL));

                        event._eventPreferencesPeriodic._counter = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_COUNTER));
                        if (event._eventPreferencesPeriodic._counter >=
                                ApplicationPreferences.applicationEventPeriodicScanningScanInterval * multiplyInterval)
                            event._eventPreferencesPeriodic._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_START_TIME));
                        else
                            event._eventPreferencesPeriodic._startTime = 0;

                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateApplicationStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_APPLICATION_START_TIME, event._eventPreferencesApplication._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateNFCStartTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getApplicationStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_APPLICATION_START_TIME
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesApplication._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_APPLICATION_START_TIME));
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateCallControlStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME, event._eventPreferencesCallControl._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateCallControlStartTimes", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void getCallControlStartTime(DatabaseHandler instance, Event event)
    {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{
                                DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME
                        },
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCallControl._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME));
                    }

                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateEventForceRun(DatabaseHandler instance, Event event) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_E_FORCE_RUN, event._ignoreManualActivation);
                    if (event._ignoreManualActivation) {
                        values.put(DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation);
                    }

                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getOrientationWithLightSensorEventsCount(DatabaseHandler instance)
    {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                final String countQuery;
                String eventTypeChecked;
                eventTypeChecked = DatabaseHandler.KEY_E_STATUS + "!=0 AND ";  //  only not stopped events
                eventTypeChecked = eventTypeChecked + DatabaseHandler.KEY_E_ORIENTATION_ENABLED + "=1 AND " +
                        DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT + "=1";

                countQuery = "SELECT  count(*) FROM " + DatabaseHandler.TABLE_EVENTS +
                        " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateActivatedProfileSensorRunningParameter(DatabaseHandler instance, Event event) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING, event._eventPreferencesActivatedProfile._running);

                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

// EVENT TIMELINE ------------------------------------------------------------------

    // Adding time line
    static void addEventTimeline(DatabaseHandler instance, EventTimeline eventTimeline) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_ET_FK_EVENT, eventTimeline._fkEvent); // Event id
                //values.put(DatabaseHandler.KEY_ET_FK_PROFILE_RETURN, eventTimeline._fkProfileEndActivated); // Profile id returned on pause/stop event
                values.put(DatabaseHandler.KEY_ET_EORDER, getMaxEOrderET(instance) + 1); // event running order

                db.beginTransaction();

                try {
                    // Inserting Row
                    eventTimeline._id = db.insert(DatabaseHandler.TABLE_EVENT_TIMELINE, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting max(eorder)
    static private int getMaxEOrderET(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                String countQuery = "SELECT MAX(" + DatabaseHandler.KEY_ET_EORDER + ") FROM " + DatabaseHandler.TABLE_EVENT_TIMELINE;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting all event timeline
    static List<EventTimeline> getAllEventTimelines(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            List<EventTimeline> eventTimelineList = new ArrayList<>();
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ET_ID + "," +
                        DatabaseHandler.KEY_ET_FK_EVENT + "," +
                        DatabaseHandler.KEY_ET_FK_PROFILE_RETURN + "," +
                        DatabaseHandler.KEY_ET_EORDER +
                        " FROM " + DatabaseHandler.TABLE_EVENT_TIMELINE +
                        " ORDER BY " + DatabaseHandler.KEY_ET_EORDER;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        EventTimeline eventTimeline = new EventTimeline();

                        eventTimeline._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ET_ID));
                        eventTimeline._fkEvent = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ET_FK_EVENT));
                        //eventTimeline._fkProfileEndActivated = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ET_FK_PROFILE_RETURN));
                        eventTimeline._eorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ET_EORDER));

                        // Adding event timeline to list
                        eventTimelineList.add(eventTimeline);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return eventTimelineList;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting event timeline
    static void deleteEventTimeline(DatabaseHandler instance, EventTimeline eventTimeline) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();
                db.delete(DatabaseHandler.TABLE_EVENT_TIMELINE, DatabaseHandler.KEY_ET_ID + " = ?",
                        new String[]{String.valueOf(eventTimeline._id)});
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting all events from timeline
    static void deleteAllEventTimelines(DatabaseHandler instance/*boolean updateEventStatus*/) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_E_STATUS, Event.ESTATUS_PAUSE);

                db.beginTransaction();

                try {

                    db.delete(DatabaseHandler.TABLE_EVENT_TIMELINE, null, null);

                    //if (updateEventStatus) {
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_STATUS + " = ?",
                            new String[]{String.valueOf(Event.ESTATUS_RUNNING)});
                    //}

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.deleteAllEventTimelines", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String getLastStartedEventName(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            String eventName = "?";
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                String query =
                        "SELECT "+DatabaseHandler.KEY_ET_FK_EVENT+" FROM "+DatabaseHandler.TABLE_EVENT_TIMELINE+" ORDER BY "+DatabaseHandler.KEY_ET_EORDER+" DESC LIMIT 1";
                Cursor cursor1 = db.rawQuery(query, null);

                long lastEvent = 0;

                if (cursor1.getCount() > 0) {
                    if (cursor1.moveToFirst()) {
                        lastEvent = cursor1.getLong(0);
                    }

                    if (lastEvent > 0) {
                        query = "SELECT "+DatabaseHandler.KEY_E_NAME+","+DatabaseHandler.KEY_E_FORCE_RUN+
                                " FROM "+DatabaseHandler.TABLE_EVENTS+
                                " WHERE "+DatabaseHandler.KEY_E_ID+"="+lastEvent;
                        Cursor cursor2 = db.rawQuery(query, null);

                        if (cursor2.getCount() > 0) {
                            if (cursor2.moveToFirst()) {
                                String _eventName = cursor2.getString(0);
                                boolean _forceRun = cursor2.getInt(1) == 1;
                                //if ((!ApplicationPreferences.prefEventsBlocked) || _forceRun)
                                //    eventName = _eventName;
                                if ((!EventStatic.getEventsBlocked(instance.context)) || _forceRun)
                                    eventName = _eventName;
                            }
                        }
                        cursor2.close();
                    }
                }
                cursor1.close();
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return eventName;
        } finally {
            instance.stopRunningCommand();
        }
    }

// GEOFENCES ----------------------------------------------------------------------

    // Adding new geofence
    static void addGeofence(DatabaseHandler instance, Geofence geofence) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_G_NAME, geofence._name); // geofence Name
                values.put(DatabaseHandler.KEY_G_LATITUDE, geofence._latitude);
                values.put(DatabaseHandler.KEY_G_LONGITUDE, geofence._longitude);
                values.put(DatabaseHandler.KEY_G_RADIUS, geofence._radius);
                values.put(DatabaseHandler.KEY_G_CHECKED, 0);
                values.put(DatabaseHandler.KEY_G_TRANSITION, 0);

                db.beginTransaction();

                try {
                    // Inserting Row
                    geofence._id = db.insert(DatabaseHandler.TABLE_GEOFENCES, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting single geofence
    static Geofence getGeofence(DatabaseHandler instance, long geofenceId) {
        instance.importExportLock.lock();
        try {
            Geofence geofence = null;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_GEOFENCES,
                        new String[]{DatabaseHandler.KEY_G_ID,
                                DatabaseHandler.KEY_G_NAME,
                                DatabaseHandler.KEY_G_LATITUDE,
                                DatabaseHandler.KEY_G_LONGITUDE,
                                DatabaseHandler.KEY_G_RADIUS
                        },
                        DatabaseHandler.KEY_G_ID + "=?",
                        new String[]{String.valueOf(geofenceId)}, null, null, null, null);

                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        geofence = new Geofence();
                        geofence._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID));
                        geofence._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_NAME));
                        geofence._latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LATITUDE));
                        geofence._longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LONGITUDE));
                        geofence._radius = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_RADIUS));
                    }

                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return geofence;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting All geofences
    static List<Geofence> getAllGeofences(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            List<Geofence> geofenceList = new ArrayList<>();
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_G_ID + "," +
                        DatabaseHandler.KEY_G_NAME + "," +
                        DatabaseHandler.KEY_G_LATITUDE + "," +
                        DatabaseHandler.KEY_G_LONGITUDE + "," +
                        DatabaseHandler.KEY_G_RADIUS + "," +
                        DatabaseHandler.KEY_G_TRANSITION +
                        " FROM " + DatabaseHandler.TABLE_GEOFENCES +
                        " ORDER BY " + DatabaseHandler.KEY_G_ID;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Geofence geofence = new Geofence();
                        geofence._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID));
                        geofence._name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_NAME));
                        geofence._latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LATITUDE));
                        geofence._longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_LONGITUDE));
                        geofence._radius = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_RADIUS));
                        geofence._transition = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_TRANSITION));
                        geofenceList.add(geofence);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return geofenceList;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Updating single geofence
    static void updateGeofence(DatabaseHandler instance, Geofence geofence) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_G_NAME, geofence._name);
                values.put(DatabaseHandler.KEY_G_LATITUDE, geofence._latitude);
                values.put(DatabaseHandler.KEY_G_LONGITUDE, geofence._longitude);
                values.put(DatabaseHandler.KEY_G_RADIUS, geofence._radius);
                values.put(DatabaseHandler.KEY_G_CHECKED, 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_GEOFENCES, values, DatabaseHandler.KEY_G_ID + " = ?",
                            new String[]{String.valueOf(geofence._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateGeofence", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void updateGeofenceTransition(DatabaseHandler instance, long geofenceId, int geofenceTransition) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_G_TRANSITION, geofenceTransition);
                    db.update(DatabaseHandler.TABLE_GEOFENCES, values, DatabaseHandler.KEY_G_ID + " = ?", new String[]{String.valueOf(geofenceId)});

                    //db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateGeofenceTransition", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void clearAllGeofenceTransitions(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_G_TRANSITION, 0);
                    db.update(DatabaseHandler.TABLE_GEOFENCES, values, null, null);

                    //db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.clearAllGeofenceTransitions", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting single geofence
    static void deleteGeofence(DatabaseHandler instance, long geofenceId) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_LOCATION_GEOFENCES +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                try (Cursor cursor = db.rawQuery(selectQuery, null)) {

                    // delete geofence
                    db.delete(DatabaseHandler.TABLE_GEOFENCES, DatabaseHandler.KEY_G_ID + " = ?",
                            new String[]{String.valueOf(geofenceId)});

                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            String geofences = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_GEOFENCES));
                            String[] splits = geofences.split(StringConstants.STR_SPLIT_REGEX);
                            boolean found = false;
                            //geofences = "";
                            StringBuilder value = new StringBuilder();
                            for (String geofence : splits) {
                                if (!geofence.isEmpty()) {
                                    if (!geofence.equals(Long.toString(geofenceId))) {
                                        //if (!geofences.isEmpty())
                                        //    geofences = geofences + "|";
                                        //geofences = geofences + geofence;
                                        if (value.length() > 0)
                                            value.append("|");
                                        value.append(geofence);
                                    } else
                                        found = true;
                                }
                            }
                            geofences = value.toString();
                            if (found) {
                                // unlink geofence from events
                                ContentValues values = new ContentValues();
                                values.put(DatabaseHandler.KEY_E_LOCATION_GEOFENCES, geofences);
                                db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID)))});
                            }
                        } while (cursor.moveToNext());
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.deleteGeofence", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void checkGeofence(DatabaseHandler instance, String geofences, int check, boolean ucheckAll) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();

                try {
                    if (ucheckAll) {
                        // uncheck geofences
                        values.clear();
                        values.put(DatabaseHandler.KEY_G_CHECKED, 0);
                        db.update(DatabaseHandler.TABLE_GEOFENCES, values, null, null);
                    }
                    if (!geofences.isEmpty()) {
                        // check geofences
                        String[] splits = geofences.split(StringConstants.STR_SPLIT_REGEX);
                        for (String geofence : splits) {
                            if (!geofence.isEmpty()) {
                                int _check = check;
                                if (check == 2) {
                                    // check == 2 - change checked state in db
                                    final String selectQuery = "SELECT " + DatabaseHandler.KEY_G_CHECKED +
                                            " FROM " + DatabaseHandler.TABLE_GEOFENCES +
                                            " WHERE " + DatabaseHandler.KEY_G_ID + "=" + geofence;
                                    Cursor cursor = db.rawQuery(selectQuery, null);
                                    //if (cursor != null) {
                                        if (cursor.moveToFirst())
                                            // switch caeked state in db: 1,2 -> 0, 0 -> 1
                                            _check = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_CHECKED)) == 0) ? 1 : 0;
                                        cursor.close();
                                    //}
                                }
                                if (_check != 2) {
                                    // save into db only check = 0, 1 = true check
                                    values.clear();
                                    values.put(DatabaseHandler.KEY_G_CHECKED, _check);
                                    db.update(DatabaseHandler.TABLE_GEOFENCES, values, DatabaseHandler.KEY_G_ID + " = ?", new String[]{geofence});
                                }
                            }
                        }
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.checkGeofence", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static Cursor getGeofencesCursor(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            Cursor cursor = null;
            try {
                instance.startRunningCommand();

                final String selectQuery = "SELECT " + DatabaseHandler.KEY_G_ID + "," +
                        DatabaseHandler.KEY_G_LATITUDE + "," +
                        DatabaseHandler.KEY_G_LONGITUDE + "," +
                        DatabaseHandler.KEY_G_RADIUS + "," +
                        DatabaseHandler.KEY_G_NAME + "," +
                        DatabaseHandler.KEY_G_CHECKED +
                        " FROM " + DatabaseHandler.TABLE_GEOFENCES +
                        " ORDER BY " + /*KEY_G_CHECKED + " DESC," +*/ DatabaseHandler.KEY_G_NAME + " ASC";

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                cursor = db.rawQuery(selectQuery, null);
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return cursor;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String getGeofenceName(DatabaseHandler instance, long geofenceId) {
        instance.importExportLock.lock();
        try {
            String r = "";
            try {
                instance.startRunningCommand();

                final String countQuery = "SELECT " + DatabaseHandler.KEY_G_NAME +
                        " FROM " + DatabaseHandler.TABLE_GEOFENCES +
                        " WHERE " + DatabaseHandler.KEY_G_ID + "=" + geofenceId;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    if (cursor.moveToFirst())
                        r = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_NAME));
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String getCheckedGeofences(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            //String value = "";
            StringBuilder value = new StringBuilder();
            try {
                instance.startRunningCommand();

                final String countQuery = "SELECT " + DatabaseHandler.KEY_G_ID + ","
                        + DatabaseHandler.KEY_G_CHECKED +
                        " FROM " + DatabaseHandler.TABLE_GEOFENCES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_CHECKED)) == 1) {
                                //if (!value.isEmpty())
                                //    value = value + "|";
                                //value = value + cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID));
                                if (value.length() > 0)
                                    value.append("|");
                                value.append(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_ID)));
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            //return value;
            return value.toString();
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getGeofenceCount(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                String countQuery = "SELECT  count(*) FROM " + DatabaseHandler.TABLE_GEOFENCES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean isGeofenceUsed(DatabaseHandler instance, long geofenceId/*, boolean onlyEnabledEvents*/) {
        instance.importExportLock.lock();
        try {
            boolean found = false;
            try {
                instance.startRunningCommand();

                String selectQuery = "SELECT " + DatabaseHandler.KEY_E_LOCATION_GEOFENCES +
                        " FROM " + DatabaseHandler.TABLE_EVENTS +
                        " WHERE " + DatabaseHandler.KEY_E_LOCATION_ENABLED + "=1";

                /*
                if (onlyEnabledEvents)
                    selectQuery = selectQuery + " AND " + KEY_E_STATUS + " IN (" +
                            String.valueOf(Event.ESTATUS_PAUSE) + "," +
                            String.valueOf(Event.ESTATUS_RUNNING) + ")";
                */

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String geofences = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_GEOFENCES));
                        String[] splits = geofences.split(StringConstants.STR_SPLIT_REGEX);
                        for (String geofence : splits) {
                            if (!geofence.isEmpty()) {
                                if (geofence.equals(Long.toString(geofenceId))) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found)
                            break;
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return found;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getGeofenceTransition(DatabaseHandler instance, long geofenceId) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                final String countQuery = "SELECT " + DatabaseHandler.KEY_G_TRANSITION +
                        " FROM " + DatabaseHandler.TABLE_GEOFENCES +
                        " WHERE " + DatabaseHandler.KEY_G_ID + "=" + geofenceId;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    if (cursor.moveToFirst())
                        r = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_G_TRANSITION));
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

// MOBILE_CELLS ----------------------------------------------------------------------

    // Adding new mobile cell
    static private void addMobileCell(DatabaseHandler instance, MobileCell mobileCell) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_MC_CELL_ID, mobileCell._cellId);
                values.put(DatabaseHandler.KEY_MC_CELL_ID_LONG, mobileCell._cellIdLong);
                values.put(DatabaseHandler.KEY_MC_NAME, mobileCell._name);
                values.put(DatabaseHandler.KEY_MC_NEW, mobileCell._new ? 1 : 0);
                values.put(DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME, mobileCell._lastConnectedTime);
                //values.put(DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS, mobileCell._lastRunningEvents);
                //values.put(DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS, mobileCell._lastPausedEvents);
                //values.put(DatabaseHandler.KEY_MC_DO_NOT_DETECT, mobileCell._doNotDetect ? 1 : 0);

                db.beginTransaction();

                try {
                    // Inserting Row
                    mobileCell._id = db.insert(DatabaseHandler.TABLE_MOBILE_CELLS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Updating single mobile cell
    static private void updateMobileCell(DatabaseHandler instance, MobileCell mobileCell) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_MC_CELL_ID, mobileCell._cellId);
                values.put(DatabaseHandler.KEY_MC_CELL_ID_LONG, mobileCell._cellIdLong);
                values.put(DatabaseHandler.KEY_MC_NAME, mobileCell._name);
                values.put(DatabaseHandler.KEY_MC_NEW, mobileCell._new ? 1 : 0);
                values.put(DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME, mobileCell._lastConnectedTime);
                //values.put(DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS, mobileCell._lastRunningEvents);
                //values.put(DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS, mobileCell._lastPausedEvents);
                //values.put(DatabaseHandler.KEY_MC_DO_NOT_DETECT, mobileCell._doNotDetect ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_MOBILE_CELLS, values, DatabaseHandler.KEY_MC_ID + " = ?",
                            new String[]{String.valueOf(mobileCell._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateMobileCell", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // add mobile cells to list
    static void addMobileCellsToList(DatabaseHandler instance, List<MobileCellsData> cellsList,
                                     int onlyCellId, long onlyCellIdlong,
                                     boolean calledFromImportDB) {
        if (!calledFromImportDB)
            instance.importExportLock.lock();
        try {
            try {
                if (!calledFromImportDB)
                    instance.startRunningCommand();

                // Select All Query
                String selectQuery = "SELECT " +
                        DatabaseHandler.KEY_MC_CELL_ID + "," +
                        DatabaseHandler.KEY_MC_CELL_ID_LONG + "," +
                        DatabaseHandler.KEY_MC_NAME + "," +
                        DatabaseHandler.KEY_MC_NEW + "," +
                        DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME + //"," +
                        //DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS + "," +
                        //DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS + "," +
                        //DatabaseHandler.KEY_MC_DO_NOT_DETECT +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS;

                boolean whereAdded = true; // all cells from db
                if ((onlyCellId != 0) && (onlyCellIdlong != 0)) {
                    whereAdded = false; // only specified cell from db
                    if (onlyCellId != Integer.MAX_VALUE) {
                        selectQuery = selectQuery +
                                " WHERE " + DatabaseHandler.KEY_MC_CELL_ID + "=" + onlyCellId;
                        whereAdded = true;
                    }
                    else if (onlyCellIdlong != Long.MAX_VALUE) {
                        selectQuery = selectQuery +
                                " WHERE " + DatabaseHandler.KEY_MC_CELL_ID_LONG + "=" + onlyCellIdlong;
                        whereAdded = true;
                    }
                }

                if (whereAdded) {
                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = instance.getMyWritableDatabase();

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            int cellIdDB = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID));
                            long cellIdDBLong = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID_LONG));
                            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
                            boolean _new = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NEW)) == 1;
                            long lastConnectedTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME));
                            //String lastRunningEvents = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS));
                            //String lastPausedEvents = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS));
                            //boolean doNotDetect = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_DO_NOT_DETECT)) == 1;
                            //Log.e("DatabaseHandlerEvents.addMobileCellsToList", "cellId="+cellId + " new="+_new);
                            boolean found = false;
                            for (MobileCellsData cellData : cellsList) {
                                if (cellIdDB != Integer.MAX_VALUE) {
                                    if (cellData.cellId == cellIdDB) {
                                        found = true;
                                        break;
                                    }
                                } else if (cellIdDBLong != Long.MAX_VALUE) {
                                    if (cellData.cellIdLong == cellIdDBLong) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                MobileCellsData cell = new MobileCellsData(
                                        cellIdDB, cellIdDBLong, name, false, _new, lastConnectedTime/*,
                                    lastRunningEvents, lastPausedEvents, doNotDetect*/);
                                cellsList.add(cell);
                            }
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                    //db.close();
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            if (!calledFromImportDB)
                instance.stopRunningCommand();
        }
    }

    static void saveMobileCellsList(DatabaseHandler instance, List<MobileCellsData> cellsList, boolean _new, boolean renameExistingCell) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_MC_ID + "," +
                        DatabaseHandler.KEY_MC_CELL_ID + "," +
                        DatabaseHandler.KEY_MC_CELL_ID_LONG + "," +
                        DatabaseHandler.KEY_MC_NAME + "," +
                        DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME + //"," +
                        //DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS + "," +
                        //DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS + "," +
                        //DatabaseHandler.KEY_MC_DO_NOT_DETECT +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                for (MobileCellsData cell : cellsList) {
                    boolean found = false;
                    long foundedDbId = 0;
                    String foundedCellName = "";
                    long foundedLastConnectedTime = 0;
                    //String foundedLastRunningEvents = "";
                    //String foundedLastPausedEvents = "";
                    //boolean doNotDetect = false;
                    if (cursor.moveToFirst()) {
                        do {
                            int dbCellId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID));
                            long dbCellIdLong = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID_LONG));
                            if (dbCellId != Integer.MAX_VALUE) {
                                if (dbCellId == cell.cellId) {
                                    foundedDbId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_ID));
                                    foundedCellName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
                                    foundedLastConnectedTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME));
                                    //foundedLastRunningEvents = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS));
                                    //foundedLastPausedEvents = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS));
                                    //doNotDetect = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_DO_NOT_DETECT)) == 1;
                                    found = true;
                                    break;
                                }
                            } else if (dbCellIdLong != Long.MAX_VALUE) {
                                if (dbCellIdLong == cell.cellIdLong) {
                                    foundedDbId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_ID));
                                    foundedCellName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
                                    foundedLastConnectedTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME));
                                    //foundedLastRunningEvents = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS));
                                    //foundedLastPausedEvents = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS));
                                    //doNotDetect = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_DO_NOT_DETECT)) == 1;
                                    found = true;
                                    break;
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                    MobileCell mobileCell = new MobileCell();
                    if (!found) {
                        //Log.d("DatabaseHandler.saveMobileCellsList", "!found");
                        mobileCell._cellId = cell.cellId;
                        mobileCell._cellIdLong = cell.cellIdLong;
                        mobileCell._name = cell.name;
                        mobileCell._new = true;
                        mobileCell._lastConnectedTime = cell.lastConnectedTime;
                        //mobileCell._lastRunningEvents = cell.lastRunningEvents;
                        //mobileCell._lastPausedEvents = cell.lastPausedEvents;
                        //mobileCell._doNotDetect = cell.doNotDetect;
                        if (!cell.name.isEmpty()) {
                            addMobileCell(instance, mobileCell);
                        }
                    } else {
                        //Log.d("DatabaseHandler.saveMobileCellsList", "found="+foundedDbId+" cell.new="+cell._new+" new="+_new);
                        mobileCell._id = foundedDbId;
                        mobileCell._cellId = cell.cellId;
                        mobileCell._cellIdLong = cell.cellIdLong;
                        mobileCell._name = cell.name;
                        if (!renameExistingCell && !foundedCellName.isEmpty())
                            mobileCell._name = foundedCellName;
                        mobileCell._new = _new && cell._new;
                        if (cell.connected)
                            mobileCell._lastConnectedTime = cell.lastConnectedTime;
                        else
                            mobileCell._lastConnectedTime = foundedLastConnectedTime;
                        //mobileCell._lastRunningEvents = cell.lastRunningEvents;
                        //mobileCell._lastPausedEvents = cell.lastPausedEvents;
                        //mobileCell._doNotDetect = cell.doNotDetect;
                        if (!cell.name.isEmpty()) {
                            updateMobileCell(instance, mobileCell);
                        }
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String renameMobileCellsList(DatabaseHandler instance, List<MobileCellsData> cellsList, String toCellName, boolean _new, String selectedIds) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_MC_ID + "," +
                        DatabaseHandler.KEY_MC_CELL_ID + "," +
                        DatabaseHandler.KEY_MC_CELL_ID_LONG + "," +
                        DatabaseHandler.KEY_MC_NAME +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                StringBuilder renamedCells = new StringBuilder();

                for (MobileCellsData cell : cellsList) {
                    boolean found = false;
                    long foundedDbId = 0;
                    if (cursor.moveToFirst()) {
                        do {
                            int dbCellId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID));
                            long dbCellIdLong = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_CELL_ID_LONG));
                            if ((dbCellId != Integer.MAX_VALUE) && (cell.cellId != Integer.MAX_VALUE)) {
                                if (dbCellId == cell.cellId) {
                                    foundedDbId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_ID));
                                    found = true;
                                    break;
                                }
                            } else
                            if ((dbCellIdLong != Long.MAX_VALUE) && (cell.cellIdLong != Long.MAX_VALUE)) {
                                if (dbCellIdLong == cell.cellIdLong) {
                                    foundedDbId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_ID));
                                    found = true;
                                    break;
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                    if (found) {
                        if (_new) {
                            // change news
                            if (cell._new) {
                                String oldCellName = cell.name;

                                cell.name = toCellName;
                                MobileCell mobileCell = new MobileCell();
                                mobileCell._id = foundedDbId;
                                mobileCell._cellId = cell.cellId;
                                mobileCell._cellIdLong = cell.cellIdLong;
                                mobileCell._name = cell.name;
                                mobileCell._new = true;
                                mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                //mobileCell._lastRunningEvents = cell.lastRunningEvents;
                                //mobileCell._lastPausedEvents = cell.lastPausedEvents;
                                //mobileCell._doNotDetect = cell.doNotDetect;
                                updateMobileCell(instance, mobileCell);

                                if ((oldCellName != null) && (!oldCellName.isEmpty())) {
                                    if (renamedCells.length() > 0)
                                        renamedCells.append("|");
                                    renamedCells.append(oldCellName);
                                }
                            }
                        } else {
                            if (selectedIds != null) {
                                // change selected
                                String[] splits = selectedIds.split(StringConstants.STR_SPLIT_REGEX);
                                for (String valueCell : splits) {
                                    if (!valueCell.isEmpty()) {
                                        boolean updateCell = false;
                                        long _valueCell = Long.parseLong(valueCell);
                                        if (cell.cellId != Integer.MAX_VALUE) {
                                            if (_valueCell == cell.cellId)
                                                updateCell = true;
                                        } else if (cell.cellIdLong != Long.MAX_VALUE) {
                                            if (_valueCell == cell.cellIdLong)
                                                updateCell = true;
                                        }
                                        if (updateCell) {
                                            String oldCellName = cell.name;

                                            cell.name = toCellName;
                                            MobileCell mobileCell = new MobileCell();
                                            mobileCell._id = foundedDbId;
                                            mobileCell._cellId = cell.cellId;
                                            mobileCell._cellIdLong = cell.cellIdLong;
                                            mobileCell._name = cell.name;
                                            mobileCell._new = cell._new;
                                            mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                            //mobileCell._lastRunningEvents = cell.lastRunningEvents;
                                            //mobileCell._lastPausedEvents = cell.lastPausedEvents;
                                            //mobileCell._doNotDetect = cell.doNotDetect;
                                            updateMobileCell(instance, mobileCell);

                                            if ((oldCellName != null) && (!oldCellName.isEmpty())) {
                                                if (renamedCells.length() > 0)
                                                    renamedCells.append("|");
                                                renamedCells.append(oldCellName);
                                            }

                                        }
                                    }
                                }
                            }
                            else {
                                // change all
                                String oldCellName = cell.name;

                                cell.name = toCellName;
                                MobileCell mobileCell = new MobileCell();
                                mobileCell._id = foundedDbId;
                                mobileCell._cellId = cell.cellId;
                                mobileCell._cellIdLong = cell.cellIdLong;
                                mobileCell._name = cell.name;
                                mobileCell._new = cell._new;
                                mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                //mobileCell._lastRunningEvents = cell.lastRunningEvents;
                                //mobileCell._lastPausedEvents = cell.lastPausedEvents;
                                //mobileCell._doNotDetect = cell.doNotDetect;
                                updateMobileCell(instance, mobileCell);

                                if ((oldCellName != null) && (!oldCellName.isEmpty())) {
                                    if (renamedCells.length() > 0)
                                        renamedCells.append("|");
                                    renamedCells.append(oldCellName);
                                }
                            }
                        }
                    }
                }

                cursor.close();
                //db.close();

                return renamedCells.toString();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
        return "";
    }

    static void deleteMobileCell(DatabaseHandler inctance, int mobileCell, long mobileCellLong, boolean calledFromImportDB) {
        if (!calledFromImportDB)
            inctance.importExportLock.lock();
        try {
            try {
                if (!calledFromImportDB)
                    inctance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = inctance.getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete mobile cell
                    if (mobileCell != Integer.MAX_VALUE)
                        db.delete(DatabaseHandler.TABLE_MOBILE_CELLS, DatabaseHandler.KEY_MC_CELL_ID + " = ?",
                            new String[]{String.valueOf(mobileCell)});
                    else
                    if (mobileCellLong != Long.MAX_VALUE)
                        db.delete(DatabaseHandler.TABLE_MOBILE_CELLS, DatabaseHandler.KEY_MC_CELL_ID_LONG + " = ?",
                                new String[]{String.valueOf(mobileCellLong)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.deleteMobileCell", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            if (!calledFromImportDB)
                inctance.stopRunningCommand();
        }
    }

    static void updateMobileCellLastConnectedTime(DatabaseHandler instance, int mobileCell, long mobileCellLong, long lastConnectedTime) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME, lastConnectedTime);

                db.beginTransaction();

                try {
                    // updating row
                    if (mobileCell != Integer.MAX_VALUE)
                        db.update(DatabaseHandler.TABLE_MOBILE_CELLS, values, DatabaseHandler.KEY_MC_CELL_ID + " = ?",
                            new String[]{String.valueOf(mobileCell)});
                    else
                    if (mobileCellLong != Long.MAX_VALUE)
                        db.update(DatabaseHandler.TABLE_MOBILE_CELLS, values, DatabaseHandler.KEY_MC_CELL_ID_LONG + " = ?",
                                new String[]{String.valueOf(mobileCellLong)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateMobileCellLastConnectedTime", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void setAllMobileCellsAsOld(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_MC_NEW, false);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_MOBILE_CELLS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateMobileCellLastConnectedTime", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void addMobileCellNamesToList(DatabaseHandler instance, List<String> cellNamesList) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_MC_NAME +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS +
                        " WHERE " + DatabaseHandler.KEY_MC_NAME + " IS NOT NULL" +
                        " AND " + DatabaseHandler.KEY_MC_NAME + " <> ''" +
                        " GROUP BY " + DatabaseHandler.KEY_MC_NAME +
                        " ORDER BY " + DatabaseHandler.KEY_MC_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
                        cellNamesList.add(name);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getNewMobileCellsCount(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS +
                        " WHERE " + DatabaseHandler.KEY_MC_NEW + "=1";

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getMobileCellNameCount(DatabaseHandler instance, String cellName) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS +
                        " WHERE " + DatabaseHandler.KEY_MC_NAME + "='" + cellName + "'";

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Updating single event
    static void updateMobileCellsCells(DatabaseHandler instance, long eventId, String cells) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();

                //EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;
                //values.put(KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cells);
                values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS, cells);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?",
                            new String[] { String.valueOf(eventId) });

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateMobileCellsCells", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean isMobileCellSaved(DatabaseHandler instance, int mobileCell, long mobileCellLong) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                // Select All Query
                String selectQuery = null;
                if (mobileCell != Integer.MAX_VALUE)
                    selectQuery = "SELECT COUNT(*) " +
                            " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS +
                            " WHERE " + DatabaseHandler.KEY_MC_CELL_ID + "=" + mobileCell;
                else
                if (mobileCellLong != Long.MAX_VALUE)
                    selectQuery = "SELECT COUNT(*) " +
                            " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS +
                            " WHERE " + DatabaseHandler.KEY_MC_CELL_ID_LONG + "=" + mobileCellLong;

                if (selectQuery != null) {
                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = instance.getMyWritableDatabase();

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    //if (cursor != null) {
                        cursor.moveToFirst();
                        r = cursor.getInt(0);
                        cursor.close();
                    //}
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r > 0;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void loadMobileCellsSensorEvents(DatabaseHandler instance,
                                                  List<MobileCellsSensorEvent> eventList) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                eventList.clear();

                final String countQuery;
                String eventTypeChecked;
                //eventTypeChecked = DatabaseHandler.KEY_E_STATUS + "=" + Event.ESTATUS_PAUSE + " AND ";  //  only paused events
                eventTypeChecked = /*eventTypeChecked +*/ DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + "=1";

                countQuery = "SELECT " +
                        DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS + /*"," +
                        DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE +*/
                        " FROM " + DatabaseHandler.TABLE_EVENTS + " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                //if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            MobileCellsSensorEvent mobileCellsSensorEvent = new MobileCellsSensorEvent();
                            mobileCellsSensorEvent.eventId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                            mobileCellsSensorEvent.cellNames = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS));
                            //mobileCellsSensorEvent.whenOutside = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE)) == 1;
                            eventList.add(mobileCellsSensorEvent);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static String getEventMobileCellsCells(DatabaseHandler instance, long eventId) {
        instance.importExportLock.lock();
        try {
            String cells = "";
            try {
                instance.startRunningCommand();

                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS},
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{String.valueOf(eventId)}, null, null, null, null);
                //if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0)
                    {
                        cells = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS));
                    }
                    cursor.close();
                //}

                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

            return cells;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static void deleteNonNamedNotUsedCells(DatabaseHandler instance, boolean calledFromImportDB) {
        try {
            // load cells from db
            List<MobileCellsData> cellsList = new ArrayList<>();
            addMobileCellsToList(instance, cellsList, 0, 0, calledFromImportDB);
            // load events from db
            List<Event> eventList;
            eventList = getAllEvents(instance, calledFromImportDB);
            for (Iterator<MobileCellsData> it = cellsList.iterator(); it.hasNext(); ) {
                MobileCellsData cell = it.next();
                if (cell.name.isEmpty()) {
                    boolean found = false;
                    for (Event event : eventList) {
                        if (event._eventPreferencesMobileCells != null) {
                            if ((event._eventPreferencesMobileCells._enabled)) {
                                boolean cellIdAllowed = false;
                                long cellId = cell.cellId;
                                if (cellId == Integer.MAX_VALUE) {
                                    cellId = cell.cellIdLong;
                                    if (cellId != Long.MAX_VALUE)
                                        cellIdAllowed = true;
                                } else
                                    cellIdAllowed = true;
                                if (cellIdAllowed) {
                                    if (event._eventPreferencesMobileCells._cellsNames.contains("|" + cellId + "|")) {
                                        // cell is between others
                                        found = true;
                                        break;
                                    }
                                    if (event._eventPreferencesMobileCells._cellsNames.startsWith(cellId + "|")) {
                                        // cell is at start of others
                                        found = true;
                                        break;
                                    }
                                    if (event._eventPreferencesMobileCells._cellsNames.endsWith("|" + cellId)) {
                                        // cell is at end of others
                                        found = true;
                                        break;
                                    }
                                    if (event._eventPreferencesMobileCells._cellsNames.equals(String.valueOf(cellId))) {
                                        // only this cell is configured
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!found) {
                        deleteMobileCell(instance, cell.cellId, cell.cellIdLong, calledFromImportDB);
                        //cellsList.remove(cell);
                        it.remove();
                    }
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }


// NFC_TAGS ----------------------------------------------------------------------

    // Adding new nfc tag
    static void addNFCTag(DatabaseHandler instance, NFCTag tag) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + DatabaseHandler.TABLE_NFC_TAGS +
                        " WHERE " + DatabaseHandler.KEY_NT_NAME + "='" + tag._name + "'";

                Cursor cursor = db.rawQuery(selectQuery, null);

                int r;// = 0;
                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                if (r == 0) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.KEY_NT_UID, tag._uid);
                    values.put(DatabaseHandler.KEY_NT_NAME, tag._name);

                    db.beginTransaction();

                    try {
                        // Inserting Row
                        db.insert(DatabaseHandler.TABLE_NFC_TAGS, null, values);

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    } finally {
                        db.endTransaction();
                    }
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Getting All nfc tags
    static List<NFCTag> getAllNFCTags(DatabaseHandler instance) {
        instance.importExportLock.lock();
        try {
            List<NFCTag> nfcTagList = new ArrayList<>();
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_NT_ID + "," +
                        DatabaseHandler.KEY_NT_UID + ", " +
                        DatabaseHandler.KEY_NT_NAME +
                        " FROM " + DatabaseHandler.TABLE_NFC_TAGS +
                        " ORDER BY " + DatabaseHandler.KEY_NT_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        NFCTag nfcTag = new NFCTag(
                            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NT_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NT_UID)));
                        nfcTagList.add(nfcTag);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return nfcTagList;
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Updating single nfc tag
    static void updateNFCTag(DatabaseHandler instance, NFCTag tag) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_NT_UID, tag._uid);
                values.put(DatabaseHandler.KEY_NT_NAME, tag._name);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(DatabaseHandler.TABLE_NFC_TAGS, values, DatabaseHandler.KEY_NT_ID + " = ?",
                            new String[]{String.valueOf(tag._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.updateNFCTag", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    // Deleting single nfc tag
    static void deleteNFCTag(DatabaseHandler instance, NFCTag tag) {
        instance.importExportLock.lock();
        try {
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(DatabaseHandler.TABLE_NFC_TAGS, DatabaseHandler.KEY_NT_ID + " = ?",
                            new String[]{String.valueOf(tag._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandlerEvents.deleteNFCTag", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        } finally {
            instance.stopRunningCommand();
        }
    }

    static boolean eventExists(DatabaseHandler instance, long event_id) {
        instance.importExportLock.lock();
        try {
            int r = 0;
            try {
                instance.startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + DatabaseHandler.TABLE_EVENTS +
                        " WHERE " + DatabaseHandler.KEY_E_ID + "=" + event_id;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                //if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return r > 0;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getEventPriority(DatabaseHandler instance, long event_id)
    {
        instance.importExportLock.lock();
        try {
            int priority = -1;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{DatabaseHandler.KEY_E_PRIORITY},
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{Long.toString(event_id)}, null, null, null, null);

                //if (cursor != null) {
                    if (cursor.moveToFirst())
                        priority = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PRIORITY));
                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return priority;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static int getEventIgnoreManualActivation(DatabaseHandler instance, long event_id)
    {
        instance.importExportLock.lock();
        try {
            int ignoreManualActivation = -1;
            try {
                instance.startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = instance.getMyWritableDatabase();

                Cursor cursor = db.query(DatabaseHandler.TABLE_EVENTS,
                        new String[]{DatabaseHandler.KEY_E_FORCE_RUN},
                        DatabaseHandler.KEY_E_ID + "=?",
                        new String[]{Long.toString(event_id)}, null, null, null, null);

                //if (cursor != null) {
                    if (cursor.moveToFirst())
                        ignoreManualActivation = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FORCE_RUN));
                    cursor.close();
                //}

                //db.close();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return ignoreManualActivation;
        } finally {
            instance.stopRunningCommand();
        }
    }

    static List<Event> getCallControlEvents(DatabaseHandler instance) {
        List<Event> eventList = new ArrayList<>();
        try {
            // Select All Query
            final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                    DatabaseHandler.KEY_E_STATUS + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_ENABLED + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_CALL_DIRECTION + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_CONTACTS + "," +
                    //DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_LIST_TYPE + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_CONTACT_GROUPS + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_NOT_IN_CONTACTS + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_BLOCK_CALLS + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_SEND_SMS + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_SMS_TEXT + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_START_TIME + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_DURATION + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_PERMANENT_RUN + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_SENSOR_PASSED + "," +
                    DatabaseHandler.KEY_E_CALL_CONTROL_CONTROL_TYPE +
                    " FROM " + DatabaseHandler.TABLE_EVENTS +
                    " WHERE " + DatabaseHandler.KEY_E_CALL_CONTROL_ENABLED + "=1" +
                    " ORDER BY " + DatabaseHandler.KEY_E_ID;

            //SQLiteDatabase db = this.getReadableDatabase();
            SQLiteDatabase db = instance.getMyWritableDatabase();

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    event._id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                    event.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_STATUS)));
                    event.createEventPreferencesCallControl();
                    getEventPreferencesCallControl(event, db);
                    eventList.add(event);
                } while (cursor.moveToNext());
            }

            cursor.close();
            //db.close();

        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        return eventList;
    }

}
