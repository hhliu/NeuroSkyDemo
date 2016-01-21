package cycu.nclab.demo.neuroskydemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DB_neurosky {

	final String TAG = this.getClass().getSimpleName();

	/**************** 主資料庫 資料庫結構設定 ****************/
	static final int VERSION = 1;
	boolean shouldBeUpate = false;

	public static final String DATABASE = "neuroskyRecord.db";
	public static final String ATT_TABLE = "attention";
	public static final String MED_TABLE = "meditation";
	public static final String RAW_TABLE = "rawEEG";
	public static final String BLANK_TABLE = "blank";

	public static final String KEY_ID = "_id"; // 資料流水號，_id由SimpleCursorAdapter綁定，不可更改
	public static final String KEY_TIMESTAMP = "ttimestampp";  // 時間戳記，只能做到紀錄存入資料庫的時間，沒有資料產生的時間

	public static final String KEY_ATTENTION = "attention";
	public static final String KEY_MEDITATION = "meditation";
	public static final String KEY_BLANK = "blank";
	public static final String KEY_RAW = "raw";


	public static final String KEY_Ch1 = "delta";
	public static final String KEY_Ch2 = "theta";
	public static final String KEY_Ch3 = "lowAlpha";
	public static final String KEY_Ch4 = "hightAlpha";
	public static final String KEY_Ch5 = "lowBeta";
	public static final String KEY_Ch6 = "highBeta";
	public static final String KEY_Ch7 = "lowGamma";
	public static final String KEY_Ch8 = "highGamma";

	// 建立資料庫欄位格式
	public static final String createAttTable = "CREATE TABLE IF NOT EXISTS "
			+ ATT_TABLE + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ KEY_TIMESTAMP + " INTEGER, "
			+ KEY_ATTENTION + " INTEGER);";

	public static final String createMedTable = "CREATE TABLE IF NOT EXISTS "
			+ MED_TABLE + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ KEY_TIMESTAMP + " INTEGER, "
			+ KEY_MEDITATION + " INTEGER);";

	public static final String createBlankTable = "CREATE TABLE IF NOT EXISTS "
			+ BLANK_TABLE + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ KEY_TIMESTAMP + " INTEGER, "
			+ KEY_BLANK + " INTEGER);";

	public static final String createRawTable = "CREATE TABLE IF NOT EXISTS "
			+ RAW_TABLE + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ KEY_TIMESTAMP + " INTEGER, "
			+ KEY_RAW + " INTEGER);";

	// NeuroSky MindWave沒有輸出這一項
//	public static final String createRawTable = "CREATE TABLE IF NOT EXISTS "
//			+ RAW_TABLE + " ("
//			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
//			+ KEY_TIMESTAMP + " INTEGER, "
//			+ KEY_Ch1 + " DOUBLE, "
//			+ KEY_Ch2 + " DOUBLE, "
//			+ KEY_Ch3 + " DOUBLE, "
//			+ KEY_Ch4 + " DOUBLE, "
//			+ KEY_Ch5 + " DOUBLE, "
//			+ KEY_Ch6 + " DOUBLE, "
//			+ KEY_Ch7 + " DOUBLE, "
//			+ KEY_Ch8 + " DOUBLE);";



	/********************** 主資料庫 成員函式集 *********************************/
	// instance of dbHelper
	private DbHelper dbHelper; // 不希望被改變
	SQLiteDatabase db;
	private Context context;

	public DB_neurosky(Context context) {
		dbHelper = new DbHelper(context);
		this.context = context;
		LOCK = new Object();
		Log.d(TAG, "Initialized database in Constructor of DB");
	}

	/** 開啟寫入模式（可寫可讀），回傳db */
	public DB_neurosky openToWrite() throws SQLException {
		synchronized (LOCK) {
			db = dbHelper.getWritableDatabase(); // 若資料庫存在則開啟；若不存在則建立一個新的
			return this;
		}
	}

	/** 開啟讀取模式，回傳db */
	public DB_neurosky openToRead() throws SQLException {
		synchronized (LOCK) {
			db = dbHelper.getReadableDatabase(); // 若資料庫存在則開啟；若不存在則建立一個新的
			return this;
		}
	}

	/** 開啟寫入模式（可寫可讀)，回傳boolean */
	public boolean openDBWriteable() throws SQLException {
		synchronized (LOCK) {
			db = dbHelper.getWritableDatabase(); // 若資料庫存在則開啟；若不存在則建立一個新的
			if (db != null)
				return true;
			else
				return false;
		}
	}

	/** 開啟讀取模式，，回傳boolean */
	public boolean openDBForRead() throws SQLException {
		synchronized (LOCK) {
			db = dbHelper.getReadableDatabase(); // 若資料庫存在則開啟；若不存在則建立一個新的
			if (db != null && db.isReadOnly())
				return true;
			else
				return false;
		}
	}

	/** 關閉資料庫 */
	public void close() {
		synchronized (LOCK) {
			dbHelper.close();
		}
	}

	/*******************************************************************************/
	/*******************************************************************************/
	private static Object LOCK;

	// DbHelper implementations, 宣告成為靜態
	// DbHelper類別為sqliteDB類別的內隱類別(inner class)
	// 加上static關鍵字，DbHelper就變成sqliteDB類別的巢狀類別(nested class)
	private class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		// 只會被呼叫一次，在第一次資料庫建立的時候
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Creating database: " + DATABASE);
			db.execSQL(createAttTable);
			db.execSQL(createMedTable);
			db.execSQL(createBlankTable);
			db.execSQL(createRawTable);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 無用的程式碼
			db.execSQL("DROP TABLE IF EXISTS " + ATT_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + MED_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + BLANK_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + RAW_TABLE);
			db.execSQL(createAttTable);
			db.execSQL(createMedTable);
			db.execSQL(createBlankTable);
			db.execSQL(createRawTable);
		}

	}


	/** 添加資料 */
	public void insert(String TABLE, ContentValues values) throws SQLException {
		// db.insertWithOnConflict(TABLE, null, values,
		// SQLiteDatabase.CONFLICT_IGNORE); //這個指令需要And 2.2以上版本
		synchronized (LOCK) {
			db.insertOrThrow(TABLE, null, values); // 如果插入錯誤會丟一個例外出來，暫不處理。
			//Log.d(TAG, "insert data into db");
		}
	}



	/** 刪除資料 */
	public int delete(String TABLE) {
		int number = 0;
		if (TABLE != null) {
			openDBWriteable();
			synchronized (LOCK) {
				db.execSQL("delete from " + TABLE);
				Toast.makeText(context, "資料庫已清除", Toast.LENGTH_SHORT);
			}
		} else {
			openDBWriteable();
			synchronized (LOCK) {
				db.execSQL("delete from " + RAW_TABLE);
				db.execSQL("delete from " + ATT_TABLE);
				db.execSQL("delete from " + MED_TABLE);
				db.execSQL("delete from " + BLANK_TABLE);
				Toast.makeText(context, "資料庫已清除", Toast.LENGTH_SHORT);
			}
			close();
		}
		return number;
	}



	



}
