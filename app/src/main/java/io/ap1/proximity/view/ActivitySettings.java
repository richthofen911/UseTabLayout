package io.ap1.proximity.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessFault;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.ap1.proximity.DefaultCallback;
import io.ap1.proximity.PermissionHandler;
import io.ap1.proximity.R;

public class ActivitySettings extends AppCompatActivity {

    private EditText nickname, aboutMe;
    private TextView tvColorValue;
    private TextView tvSave;
    private Context mContext;
    private CheckBox visible, showPicture, incognito;
    private ImageView ivProfileImage;

    private Toolbar toolbar;

    private Intent intentPicture;

    private String selectedImagePath;

    private String userObjectId;

    private static final String TAG = "ActivitySettings";
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_A_PHOTO = 2;
    public static final int USER_CHANGE_COLOR = 3;

    private static final String newPhotoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "proximity_profile_image.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mContext = this;

        nickname = (EditText)findViewById(R.id.et_settings_nickname);
        aboutMe = (EditText)findViewById(R.id.et_settings_about_me);
        visible = (CheckBox)findViewById(R.id.checkBox_settings_visible);
        showPicture = (CheckBox)findViewById(R.id.checkBox_settings_show_picture);
        incognito = (CheckBox)findViewById(R.id.checkBox_settings_incognito);
        tvColorValue = (TextView)findViewById(R.id.tv_settings_color_value);
        ivProfileImage = (ImageView)findViewById(R.id.iv_profile_image);

        toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        tvSave = (TextView) findViewById(R.id.tv_toobar_settings_save);

        setUpDrawer();

        userObjectId = getIntent().getStringExtra("userObjectId");
        Log.e(TAG, "userObjectId: " + userObjectId);
        //String objectId = user.substring(user.indexOf("objectId="), user.length() - 1);
        //Log.i("Object Id", objectId);
        //final String realObjectId = getIntent().getStringExtra("objectId");
        //Log.i("Real Object Id", realObjectId);

        Backendless.Persistence.of( BackendlessUser.class ).findById(userObjectId, new DefaultCallback<BackendlessUser>(mContext) {
            @Override
            public void handleResponse(BackendlessUser response) {
                super.handleResponse(response);
                Log.e("user obj", response.getProperty("nickname") + " "
                                + response.getProperty("bio") + " "
                                + response.getProperty("visible") + " "
                                + response.getProperty("showPicture") + " "
                                + response.getProperty("incognito") + " "
                                + response.getProperty("color") + " "
                );
                Log.e(TAG, "user persis resp: " + response.toString());
                nickname.setText((String) response.getProperty("nickname"));
                aboutMe.setText((String) response.getProperty("bio"));
                visible.setChecked(Boolean.getBoolean((String) response.getProperty("visible")));
                showPicture.setChecked(Boolean.getBoolean((String) response.getProperty("showPicture")));
                incognito.setChecked(Boolean.getBoolean((String) response.getProperty("incognito")));
                tvColorValue.setBackgroundColor(Color.parseColor(((String) response.getProperty("color"))));
                tvColorValue.setText((String) response.getProperty("color"));
                tvColorValue.setTextColor(Color.parseColor(((String) response.getProperty("color"))));
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                Log.e("Handle fault", fault.toString());
            }
        });
    }

    protected void setUpDrawer(){
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_settings) {
                    // swipe back the drawer
                    drawer.closeDrawer(GravityCompat.START);
                } else if (id == R.id.nav_logout) {

                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    // override onBackPressed() here to handle swiping out the drawer
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onCameraIconClicked(View v){
        showMenu(v);
    }

    public void onSaveClicked(View v){
        Backendless.Persistence.of(BackendlessUser.class).findById(userObjectId, new DefaultCallback<BackendlessUser>(ActivitySettings.this) {
            @Override
            public void handleResponse(BackendlessUser respFindUser) {
                super.handleResponse(respFindUser);
                Log.e(TAG, "user found");
                respFindUser.setProperty("nickname", nickname.getText().toString());
                respFindUser.setProperty("bio", aboutMe.getText().toString());
                respFindUser.setProperty("visible", String.valueOf(visible.isChecked()));
                respFindUser.setProperty("showPicture", String.valueOf(showPicture.isChecked()));
                respFindUser.setProperty("incognito", String.valueOf(incognito.isChecked()));
                respFindUser.setProperty("color", tvColorValue.getText().toString());
                Backendless.Persistence.save(respFindUser, new DefaultCallback<BackendlessUser>(mContext) {
                    @Override
                    public void handleResponse(BackendlessUser updatedResponse) {
                        super.handleResponse(updatedResponse);
                        Log.e(TAG, "update user persis :" + updatedResponse.toString());
                       // ActivitySettings.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                        Toast.makeText(ActivitySettings.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault updatedFault) {
                        super.handleFault(updatedFault);
                        Log.e(TAG, "update user persis err: " + updatedFault.toString());
                    }
                });
            }
            @Override
            public void handleFault(BackendlessFault respFindUser) {
                super.handleFault(respFindUser);
                Log.e(TAG, "find user err: " + respFindUser.toString());
            }
        });
    }

    public void onColorClicked(View v){
        Intent changeColor = new Intent(ActivitySettings.this, ActivityColorPicker.class);
        changeColor.putExtra("caller", "userSettings");
        changeColor.putExtra("defaultColor", tvColorValue.getText().toString());
        startActivityForResult(changeColor, USER_CHANGE_COLOR);
    }

    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.image_take_photo:
                        if(!PermissionHandler.checkPermission(ActivitySettings.this, Manifest.permission.CAMERA)){
                            PermissionHandler.requestPermission(ActivitySettings.this, Manifest.permission.CAMERA);
                        }else {
                            intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intentPicture.addCategory(Intent.CATEGORY_DEFAULT);
                            File file = new File(newPhotoPath);
                            Uri imageUri = Uri.fromFile(file );
                            intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intentPicture, TAKE_A_PHOTO);
                        }
                        return true;
                    case R.id.image_choose_picture:
                        if(!PermissionHandler.checkPermission(ActivitySettings.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            PermissionHandler.requestPermission(ActivitySettings.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }else {
                            intentPicture = new Intent();
                            intentPicture.setType("image/*");
                            intentPicture.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intentPicture, "Select a picture"), SELECT_PICTURE);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.pick_or_capture);
        popup.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SELECT_PICTURE:
                if(resultCode == RESULT_OK){
                    Uri selectedImageUri = data.getData();
                    if (Build.VERSION.SDK_INT < 19) {
                        selectedImagePath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                        ivProfileImage.setImageBitmap(bitmap);

                    } else {
                        ParcelFileDescriptor parcelFileDescriptor;
                        try {
                            parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                            parcelFileDescriptor.close();
                            ivProfileImage.setImageBitmap(image);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case TAKE_A_PHOTO:
                if(resultCode == RESULT_OK){
                    selectedImagePath = getPath(Uri.parse(newPhotoPath));
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    ivProfileImage.setImageBitmap(bitmap);
                }
                break;
            case USER_CHANGE_COLOR:
                if(resultCode == RESULT_OK){
                    int newColor = data.getIntExtra("newColor", 0);
                    String hexColor = String.format("#%06X", (0xFFFFFF & newColor));
                    Log.e(TAG, "color: " + hexColor);
                    tvColorValue.setBackgroundColor(newColor);
                    tvColorValue.setText(hexColor);
                    tvColorValue.setTextColor(newColor);
                }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case TAKE_A_PHOTO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intentPicture.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivityForResult(intentPicture, TAKE_A_PHOTO);
                } else {
                    Toast.makeText(this, "no permission to access the camera", Toast.LENGTH_SHORT).show();
                }
                break;
            case SELECT_PICTURE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intentPicture = new Intent();
                    intentPicture.setType("image/*");
                    intentPicture.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intentPicture, "Select a picture"), SELECT_PICTURE);
                } else {
                    Toast.makeText(this, "no permission to access the local pictures", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}