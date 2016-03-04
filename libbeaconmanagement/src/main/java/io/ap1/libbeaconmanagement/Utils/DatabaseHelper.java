package io.ap1.libbeaconmanagement.Utils;

import android.content.Context;
//import android.database.SQLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import io.ap1.libbeaconmanagement.Beacon;
import io.ap1.libbeaconmanagement.BeaconOperation;

/**
 * Created by admin on 06/10/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "beacons.db";
    private static final int DATABASE_VERSION = 1;

    private static Dao<Beacon, Integer> beaconDao = null;
    private RuntimeExceptionDao<Beacon, Integer> beaconRuntimeExceptionDao = null;
    private static DatabaseHelper instance;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public static synchronized DatabaseHelper getHelper(Context context){
        if(instance == null){
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource){
        try{
            TableUtils.createTable(connectionSource, Beacon.class);
            beaconDao = getBeaconDao();
            beaconRuntimeExceptionDao = getBeaconRuntimeExceptionDao();
        } catch(SQLException e){
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Beacon.class, true);
        }
        catch (SQLException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public Dao<Beacon, Integer> getBeaconDao() throws SQLException{
        if (beaconDao == null){
            beaconDao = getDao(Beacon.class);
            Log.e("beaconDao", "null but created");
        }
        return beaconDao;
    }

    public RuntimeExceptionDao<Beacon, Integer> getBeaconRuntimeExceptionDao(){
        if (beaconRuntimeExceptionDao == null){
            beaconRuntimeExceptionDao = getRuntimeExceptionDao(Beacon.class);
            Log.e("beaconRuntimeExDao", "null but created");
        }
        return beaconRuntimeExceptionDao;
    }

    public void saveABeacon(Beacon newBeacon){
        if(beaconDao == null){
            try{
                beaconDao = getBeaconDao();
                Log.e("beaconDao saveBeacon", "null but created");
            } catch(SQLException e){
                Log.e(TAG, e.toString());
            }
        }
        try {
            beaconDao.createIfNotExists(newBeacon);
        }catch (SQLException e){
            Log.e("save beacon error", e.toString());
        }
    }

    public void deleteAllBeacons(Context context){
        try {
            beaconDao.delete(queryForAllBeacons());
        }catch (SQLException e){
            Log.e("del all beacons error", e.toString());
        }

    }

    public List<Beacon> queryForAllBeacons(){
        List<Beacon> beaconsInLocalDB;
        try {
            beaconsInLocalDB = beaconDao.queryForAll();
            return beaconsInLocalDB;
        }catch (SQLException e){
            Log.e("query all error", e.toString());
            return null;
        }
    }

    /**
     * check if a detected beacon is in local db or not. if so, it should have more properties
     * to use
     * @param beaconFromDetectedList
     * @return
     */
    public Beacon queryForOneBeacon(Beacon beaconFromDetectedList){
        try {
            List<Beacon> beaconWanted = beaconDao.queryBuilder().where().
                    eq("major", beaconFromDetectedList.getMajor()).and().
                    eq("minor", beaconFromDetectedList.getMinor()).query();
            if(beaconWanted.size() > 0){
                Log.e("queryOneBeacon", "in");
                return beaconWanted.get(0);
            }else {
                Log.e("queryOneBeacon", "not in");
                return null;
            }
        }catch (SQLException e){
            Log.e("queryOneBeacon", "error");
            return null;
        }
    }

    public List<Beacon> queryForBeaconsByCompany(String companyName){
        try {
            List<Beacon> beaconByTheCompany = beaconDao.queryBuilder().where().
                    eq("companyname", companyName).query();
            return beaconByTheCompany;
        }catch (SQLException e){
            Log.e("queryByCompany", e.toString());
            return null;
        }
    }

    public boolean isBeaconInLocalDB(Beacon beaconDetected){
        List<Beacon> beaconsInLocalDB = queryForAllBeacons();
        if(beaconsInLocalDB != null){
            for (Beacon localBeacon: beaconsInLocalDB){
                if(BeaconOperation.equals(beaconDetected, localBeacon)){
                    return true;
                }
            }
            return false;
        }else{
            Log.e("queryLocalBeacon", "error");
            return false;
        }
    }

    public String[] queryDistinct(String columnName){
        try{
            List<Beacon> distinctSamples = beaconDao.queryBuilder().distinct().selectColumns(columnName).query();
            int resultsAmount = distinctSamples.size();
            String[] results = new String[resultsAmount];
            for(int i = 0; i < resultsAmount; i++){
                results[i] = ((Beacon)((ArrayList)distinctSamples).get(i)).getCompanyname().replace("\'", "\'\'");
            }
            return results;
        }catch (SQLException e) {
            Log.e("queryDistinct", e.toString());
            return null;
        }
    }

    @Override
    public void close(){
        super.close();
        beaconRuntimeExceptionDao = null;
    }
}
