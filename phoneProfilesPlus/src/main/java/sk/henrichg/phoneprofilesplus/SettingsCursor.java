package sk.henrichg.phoneprofilesplus;

import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public class SettingsCursor implements Cursor {
    @Nullable
    private final Cursor cursor;

    private SortHelper sortHelper;
    private Comparator<String> comparator;
    private String[] data;
    private Integer[] integerData;
    private int position = -1;

    private void sortValues() {
        if (cursor != null && sortHelper != null && comparator != null) {
            int count = cursor.getCount();
            if (data == null || data.length != count) {
                data = new String[count];
            }
            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                data[i] = sortHelper.getString(cursor);
            }
            if (integerData == null || integerData.length != count) {
                integerData = new Integer[count];
            }
            for (int i2 = 0; i2 < count; i2++) integerData[i2] = i2;
            Arrays.sort(integerData, (i1, i2) -> comparator.compare(data[i1], data[i2]));
            moveToPosition(-1);
        }
    }

    public SettingsCursor(@Nullable Cursor cursor) {
        this.cursor = cursor;
        this.sortHelper = SortHelper.getInstance(/* Setting key */ 1);
        this.comparator = String.CASE_INSENSITIVE_ORDER;
        sortValues();
    }

    @SuppressWarnings("unused")
    public void setSortCriteria(SortHelper sortHelper, Comparator<String> comparator) {
        this.sortHelper = sortHelper;
        this.comparator = comparator;
        sortValues();
    }

    @Override
    public void close() {
        if (cursor != null) cursor.close();
    }

    @Override
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        if (cursor == null) charArrayBuffer.sizeCopied = 0;
        else cursor.copyStringToBuffer(i, charArrayBuffer);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deactivate() {
        if (cursor != null) cursor.deactivate();
    }

    @Override
    public byte[] getBlob(int i) {
        return cursor == null ? null : cursor.getBlob(i);
    }

    @Override
    public int getColumnCount() {
        return cursor == null ? 0 : cursor.getColumnCount();
    }

    @Override
    public int getColumnIndex(String str) {
        return  cursor == null ? -1 : cursor.getColumnIndex(str);
    }

    @Override
    public int getColumnIndexOrThrow(String str) {
        if (cursor != null) return cursor.getColumnIndexOrThrow(str);
        throw new IllegalArgumentException();
    }

    @Override
    public String getColumnName(int i) {
        return cursor == null ? "" : cursor.getColumnName(i);
    }

    @Override
    public String[] getColumnNames() {
        return cursor == null ? new String[0] : cursor.getColumnNames();
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public double getDouble(int i) {
        return cursor == null ? Double.NaN : cursor.getDouble(i);
    }

    @Override
    public Bundle getExtras() {
        return cursor == null ? Bundle.EMPTY : cursor.getExtras();
    }

    @Override
    public float getFloat(int i) {
        return cursor == null ? Float.NaN : cursor.getFloat(i);
    }

    @Override
    public int getInt(int i) {
        return cursor == null ? Integer.MIN_VALUE : cursor.getInt(i);
    }

    @Override
    public long getLong(int i) {
        return cursor == null ? Long.MIN_VALUE : cursor.getLong(i);
    }

    @Override
    public Uri getNotificationUri() {
        return cursor == null ? null : cursor.getNotificationUri();
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public short getShort(int i) {
        return cursor == null ? Short.MIN_VALUE : cursor.getShort(i);
    }

    @Override
    public String getString(int i) {
        return cursor == null ? null : cursor.getString(i);
    }

    @SuppressLint("WrongConstant")
    @Override
    public int getType(int i) {
        return cursor == null ? 0 : cursor.getType(i);
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override
    public boolean isAfterLast() {
        return position >= integerData.length;
    }

    @Override
    public boolean isBeforeFirst() {
        return position < 0;
    }

    @Override
    public boolean isClosed() {
        return cursor != null && cursor.isClosed();
    }

    @Override
    public boolean isFirst() {
        return position == 0;
    }

    @Override
    public boolean isLast() {
        return position + 1 == integerData.length;
    }

    @Override
    public boolean isNull(int i) {
        return cursor == null || cursor.isNull(i);
    }

    @Override
    public boolean move(int i) {
        return moveToPosition(position + i);
    }

    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return moveToPosition(integerData.length - 1);
    }

    @Override
    public boolean moveToNext() {
        return moveToPosition(position + 1);
    }

    @Override
    public boolean moveToPosition(int i) {
        position = i;
        if (i >= 0 && i < integerData.length) i = integerData[i];
        return cursor != null && cursor.moveToPosition(i);
    }

    @Override
    public boolean moveToPrevious() {
        return moveToPosition(position - 1);
    }

    @Override
    public void registerContentObserver(ContentObserver contentObserver) {
        if (cursor != null) cursor.registerContentObserver(contentObserver);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        if (cursor != null) cursor.registerDataSetObserver(dataSetObserver);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean requery() {
        return cursor == null || cursor.requery();
    }

    @Override
    public Bundle respond(Bundle bundle) {
        return cursor == null ? Bundle.EMPTY : cursor.respond(bundle);
    }

    @Override
    public void setExtras(Bundle bundle) {
        if (cursor != null) cursor.setExtras(bundle);
    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        if (cursor != null) cursor.setNotificationUri(contentResolver, uri);
    }

    @Override
    public void unregisterContentObserver(ContentObserver contentObserver) {
        if (cursor != null) cursor.unregisterContentObserver(contentObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        if (cursor != null) cursor.unregisterDataSetObserver(dataSetObserver);
    }


    private static class SortHelper {
        @NonNull
        public static SortHelper getInstance(int column) {
            return new SortHelper(column);
        }

        private final int column;

        public SortHelper(int column) {
            this.column = column;
        }

        public String getString(@NonNull Cursor cursor) {
            return cursor.getString(column);
        }
    }
}
