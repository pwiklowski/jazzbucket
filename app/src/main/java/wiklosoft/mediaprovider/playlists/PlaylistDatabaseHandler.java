package wiklosoft.mediaprovider.playlists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowser.MediaItem;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pawel Wiklowski on 15.10.15.
 */
public class PlaylistDatabaseHandler extends SQLiteOpenHelper {

    public PlaylistDatabaseHandler(Context context) {
        super(context, "DbHandler", null, 1);
    }

    private final static String TABLE_NAME = "Playlists";
    public final static String FAVORITES = "Favorites";


    public final static String NAME = "NAME";
    public final static String TITLE = "TITLE";
    public final static String ITEM_ID= "ITEM_ID";
    public final static String MEDIA_ID= "MEDIA_ID";
    public final static String PLAYLIST_ID= "PLAYLIST_ID";
    public final static String DURATION = "DURATION";

    public final static String KEY_ID = "id";

    public final static String QUEUE_PLAYLIST_ID = "QUEUE_PLAYLIST_ID";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " +TABLE_NAME+"("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + PLAYLIST_ID + " STRING,"
                + TITLE+ " STRING,"
                + ITEM_ID+ " INTEGER,"
                + MEDIA_ID + " STRING,"
                + NAME + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    public void addPlaylist(Playlist playlist) {
        SQLiteDatabase db = this.getWritableDatabase();

        List<MediaItem> items = playlist.getPlaylistItems();

        for (MediaItem item: items){
            MediaDescription description = item.getDescription();
            Bundle extra = description.getExtras();

            ContentValues values = new ContentValues();
            values.put(TITLE, description.getTitle().toString());
            values.put(ITEM_ID, description.getExtras().getInt(ITEM_ID));
            values.put(MEDIA_ID, description.getMediaId());
            values.put(NAME, playlist.getName());

            // Inserting Row
            long id = db.insert(TABLE_NAME, null, values);

        }
        db.close(); // Closing database connection
    }

    public List<String> getPlaylistNames(){
        List<String> playlistsNames = new ArrayList<String>();
        String selectQuery = "SELECT DISTINCT "+ NAME +" FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(0).equals(QUEUE_PLAYLIST_ID))
                    playlistsNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return playlistsNames;
    }
    public void addItemToPlaylist(String playlistName, MediaItem item) {
        MediaDescription description = item.getDescription();
        addItemToPlaylist(playlistName, description.getTitle().toString(), description.getMediaId());
    }
    public void addItemToPlaylist(String playlistName, String title, String mediaId) {
        int id = getItemId(playlistName);
        addItemToPlaylist(playlistName, title, mediaId, id);

    }
    public void addItemToPlaylist(String playlistName, String title, String mediaId, int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(ITEM_ID, id);
        values.put(MEDIA_ID, mediaId.replace("'", "''"));
        values.put(NAME, playlistName);

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public void addItemToPlaylist(int i, String playlistName, String title, String mediaId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE " + TABLE_NAME + " SET " + ITEM_ID + "=("+ITEM_ID+" + 1) WHERE " +ITEM_ID + " >= " +i;

        db.execSQL(query);
        db.close();
        addItemToPlaylist(playlistName, title, mediaId, i);


        getPlaylist(playlistName);
    }

    public void removeItemFromPlaylist(String playlistName, String mediaId){
        String query = "DELETE FROM "+ TABLE_NAME + " WHERE "+  NAME + "='" + playlistName + "' AND " + MEDIA_ID + "='" + mediaId.replace("'", "''") + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }
    private int getItemId(String playlistName){
        int lastItem = -1;

        String selectQuery = "SELECT "+ ITEM_ID +" FROM " + TABLE_NAME + " WHERE " + NAME + "='" +playlistName +"' ORDER BY " + ITEM_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            lastItem = cursor.getInt(0);
        }

        return lastItem + 1;
    }



    public Playlist getPlaylist(String name)
    {
        Playlist playlist = new Playlist(name);

        String selectQuery = "SELECT "+TITLE+","+MEDIA_ID+"," + ITEM_ID +" FROM " + TABLE_NAME + " WHERE " + NAME + "='" + name + "' ORDER BY "+ ITEM_ID + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.d("DB", cursor.getInt(2) + " " + cursor.getString(0) + " " + cursor.getString(1));

                MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(cursor.getString(cursor.getColumnIndex(MEDIA_ID)))
                        .setTitle(cursor.getString(cursor.getColumnIndex(TITLE)))
                        .setExtras(new Bundle())
                        .build(), MediaItem.FLAG_PLAYABLE);

                playlist.addItem(item);
            } while (cursor.moveToNext());
        }

        return playlist;
    }
    public static boolean isOnFavorites(Context c, String itemId){

        PlaylistDatabaseHandler db = new PlaylistDatabaseHandler(c);
        Playlist favoritesPlaylist = db.getPlaylist(PlaylistDatabaseHandler.FAVORITES);

        for(MediaBrowser.MediaItem i: favoritesPlaylist.getPlaylistItems())
            if (i.getMediaId().equals(itemId))return true;

        return false;
    }


//    public void deleteSettings(LivingRoomSettings settings) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[] { String.valueOf(settings.getId()) });
//        db.close();
//    }
//    public void updateSetting(LivingRoomSettings setting)
//    {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(FRONT_LIGHT, setting.getFrontLight());
//        values.put(BACK_LIGHT, setting.getBackLight());
//
//        values.put(AMBIENT_RED_LIGHT , setting.getAmbientLightRed());
//        values.put(AMBIENT_GREEN_LIGHT, setting.getAmbientLightGreen());
//        values.put(AMBIENT_BLUE_LIGHT, setting.getAmbientLightBlue());
//        values.put(TABLE_LIGHT, setting.getTableLight());

//        values.put(BLINDS_LEFT, setting.getBlindsLeftPosition());
//        values.put(BLINDS_RIGHT, setting.getBlindsRightPosition());
//        values.put(SCREEN_POSITION, setting.getScreenPosition());
//        values.put(NAME, setting.getName());
//
//        db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[] { String.valueOf(setting.getId())});
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void reset()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
