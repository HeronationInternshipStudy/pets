package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.PeriodicSync;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class PetProvider extends ContentProvider {
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDbHelper mDbHelper;

    private static final int PETS=100;
    private static final int PET_ID=101;
    private static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper=new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database=mDbHelper.getReadableDatabase();
        Cursor cursor=null;
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                break;
            case PET_ID:
                selection= PetContract.PetEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor=database.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI "+uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri,contentValues);
                default:
                    throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri,contentValues,s,strings);
            case PET_ID:
                s= PetContract.PetEntry._ID+"=?";
                strings=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,contentValues,s,strings);
                default:
                    throw new IllegalArgumentException("Update is not supported for "+uri);
        }
    }

    private Uri insertPet(Uri uri,ContentValues values){
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if(name ==null){
            throw new IllegalArgumentException("Pet requires a name");
        }
        Integer gender=values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if(gender==null||!PetContract.PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        Integer weight =values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if(weight!=null&&weight<0){
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        SQLiteDatabase database=mDbHelper.getWritableDatabase();
        long id=database.insert(PetContract.PetEntry.TABLE_NAME,null,values);
        if(id==-1){
            Log.e(LOG_TAG,"Failed to insert row for "+uri);
            return null;
        }
        return ContentUris.withAppendedId(uri,id);
    }

    private int updatePet(Uri uri, ContentValues values,String selection, String[] selectionArgs){
        if(values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name=values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if(name==null){
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        Integer gender=values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if(gender==null||!PetContract.PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        Integer weight =values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if(weight!=null&&weight<0){
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return database.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }


}


