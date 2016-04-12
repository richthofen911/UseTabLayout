package io.ap1.proximity.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.ap1.proximity.Constants;
import io.ap1.proximity.DefaultBackendlessCallback;
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
    private String profileImageName;
    private String myUserObjectId;
    private Bitmap profileImage;

    private static final String TAG = "ActivitySettings";
    public static final int USER_CHANGE_COLOR = 3;
    private static final int INTENT_CODE_TAKE_PHOTO = 201;
    private static final int INTENT_CODE_SELECT_PICTURE = 202;

    private float density;

    private static String newPhotoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsoluteFile()
            + File.separator + "proximity_profile_image.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mContext = this;

        density = getResources().getDisplayMetrics().density;

        nickname = (EditText)findViewById(R.id.et_settings_nickname);
        aboutMe = (EditText)findViewById(R.id.et_settings_about_me);
        visible = (CheckBox)findViewById(R.id.checkBox_settings_visible);
        showPicture = (CheckBox)findViewById(R.id.checkBox_settings_show_picture);
        incognito = (CheckBox)findViewById(R.id.checkBox_settings_incognito);
        tvColorValue = (TextView)findViewById(R.id.tv_settings_color_value);
        ivProfileImage = (ImageView)findViewById(R.id.iv_profile_image);

        toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        tvSave = (TextView) findViewById(R.id.tv_company_details_toobar_save);

        setUpDrawer();

        myUserObjectId = getIntent().getStringExtra("userObjectId");
        Log.e(TAG, "userObjectId: " + myUserObjectId);

        Backendless.Persistence.of(BackendlessUser.class).findById(myUserObjectId, new DefaultBackendlessCallback<BackendlessUser>(mContext, "Getting user data...") {
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
                tvColorValue.setBackgroundColor(Color.parseColor(("#" + response.getProperty("color"))));
                tvColorValue.setText((String) response.getProperty("color"));
                tvColorValue.setTextColor(Color.parseColor(("#" + response.getProperty("color"))));
                profileImageName = (String) response.getProperty("profileImage");
                Log.e(TAG, "profileImageName: " + profileImageName);
                Picasso.with(ActivitySettings.this).load(Constants.PROFILE_IMAGE_PATH_ROOT + profileImageName).into(ivProfileImage);
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
        // if either taking a photo or choose a picture will change default profileImageName 'placeholder.png'to 'userObjectId.png'
        if (!profileImageName.equals("placeholder.png") && profileImage != null) {
            Toast.makeText(this, "update Image", Toast.LENGTH_SHORT).show();
            // update profile image
            // 100 is quality, true means overwrite the old one with if same name, "profileImage" is remote file directory path
            Backendless.Files.Android.upload(profileImage, Bitmap.CompressFormat.PNG, 100, myUserObjectId + ".png", "profileImage", true, new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(BackendlessFile backendlessFile) {
                    Toast.makeText(ActivitySettings.this, "Successfully update your profile image", Toast.LENGTH_SHORT).show();
                    updateUserDataExceptImage();
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });
        }else
            updateUserDataExceptImage();
    }

    private void updateUserDataExceptImage(){
        Backendless.Persistence.of(BackendlessUser.class).findById(myUserObjectId, new DefaultBackendlessCallback<BackendlessUser>(this, "Updating user data") {
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
                respFindUser.setProperty("profileImage", profileImageName);
                Backendless.Persistence.save(respFindUser, new DefaultBackendlessCallback<BackendlessUser>(mContext, TAG) {
                    @Override
                    public void handleResponse(BackendlessUser updatedResponse) {
                        super.handleResponse(updatedResponse);
                        Log.e(TAG, "update user persis :" + updatedResponse.toString());
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
                        // write_external_storage is also necessary to save the photo on the device to access
                        String[] notGrantedPermissionCamera = PermissionHandler.checkPermissions(ActivitySettings.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                        if(notGrantedPermissionCamera != null)
                            PermissionHandler.requestPermissions(ActivitySettings.this, notGrantedPermissionCamera, Constants.PERMISSION_REQUEST_CODE_CAMERA);
                        else {
                            dispatchTakingPhotoIntent();
                        }
                        return true;
                    case R.id.image_choose_picture:
                        String[] notGrantedPermissionWExStorage = PermissionHandler.checkPermissions(ActivitySettings.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                        if(notGrantedPermissionWExStorage != null)
                            PermissionHandler.requestPermissions(ActivitySettings.this, notGrantedPermissionWExStorage, Constants.PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                        else {
                            dispatchSelectPictureIntent();
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

    private Bitmap resizeImage(Bitmap bitmapOriginal){
        // desired image size is height(<=200px) * width(<=200px)
        int bmpWidth = bitmapOriginal.getWidth();
        int bmpHeight = bitmapOriginal.getHeight();
        Log.e("original width, height", bmpWidth + ", " + bmpHeight);
        // choose the larger value between width and height as the resizing arg
        float wantedScale = 200 / (bmpWidth > bmpHeight ? bmpWidth : bmpHeight / density);
        Log.e("wanted scale", wantedScale + "");
        Matrix matrix = new Matrix();
        matrix.postScale(wantedScale, wantedScale);
        return Bitmap.createBitmap(bitmapOriginal, 0, 0, bmpWidth, bmpHeight, matrix, true);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File imageFile = new File(newPhotoPath);
        if(imageFile.isFile())
            imageFile.createNewFile();
        Log.e(TAG, "createImageFile: AbsolutePath " + newPhotoPath);
        return imageFile;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(newPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakingPhotoIntent(){
        intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intentPicture.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e){
                Log.e(TAG, "onMenuItemClick: create photo file error: " + e.toString());
            }
            if(photoFile != null){
                intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(intentPicture, INTENT_CODE_TAKE_PHOTO);
            }
        }
    }

    private void dispatchSelectPictureIntent(){
        intentPicture = new Intent();
        intentPicture.setType("image/*");
        intentPicture.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentPicture, "Select a picture"), INTENT_CODE_SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case INTENT_CODE_SELECT_PICTURE:
                if(resultCode == RESULT_OK){
                    Uri selectedImageUri = data.getData();
                    if (Build.VERSION.SDK_INT < 19) {
                        selectedImagePath = getPath(selectedImageUri);
                        profileImage = resizeImage(BitmapFactory.decodeFile(selectedImagePath));
                        ivProfileImage.setImageBitmap(profileImage);
                        profileImageName = myUserObjectId + ".png";
                    } else {
                        ParcelFileDescriptor parcelFileDescriptor;
                        try {
                            parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            profileImage = resizeImage(BitmapFactory.decodeFileDescriptor(fileDescriptor));
                            parcelFileDescriptor.close();
                            ivProfileImage.setImageBitmap(profileImage);
                            profileImageName = myUserObjectId + ".png";
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case INTENT_CODE_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    selectedImagePath = getPath(Uri.parse(newPhotoPath));
                    profileImage = resizeImage(BitmapFactory.decodeFile(selectedImagePath));
                    ivProfileImage.setImageBitmap(profileImage);
                    profileImageName = myUserObjectId + ".png";
                    galleryAddPic();
                }
                break;
            case USER_CHANGE_COLOR:
                if(resultCode == RESULT_OK){
                    int newColor = data.getIntExtra("newColor", 0);
                    String hexColor = Integer.toHexString(newColor).substring(2);
                    Log.e(TAG, "color: " + hexColor);
                    tvColorValue.setBackgroundColor(newColor);
                    tvColorValue.setText(hexColor);
                    tvColorValue.setTextColor(newColor);
                }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAMERA:
                if(grantResults.length > 0)
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        dispatchTakingPhotoIntent();
                    } else {
                        Snackbar.make(toolbar, "CAMERA and WRITE_EXTERNAL_STORAGE permissions are required to take a photo for your profile image and access by this app",
                            Snackbar.LENGTH_SHORT)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PermissionHandler.requestPermissions(ActivitySettings.this,
                                            new String[]{Manifest.permission.CAMERA},
                                            Constants.PERMISSION_REQUEST_CODE_CAMERA);
                                }
                            })
                            .show();
                    }
                break;
            case Constants.PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchSelectPictureIntent();
                } else {
                    Snackbar.make(toolbar, "Write External Storage permission is required to choose a picture from your local device as the profile image",
                            Snackbar.LENGTH_SHORT)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PermissionHandler.requestPermissions(ActivitySettings.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            Constants.PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                                }
                            })
                            .show();
                }
                break;
        }
    }
}
