package com.betalogika.savekaya;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;

public class HomeActivity extends AppCompatActivity {

    private static final CharSequence[] PICTURE_ITEMS   = {"Gallery", "Camera", "Cancel"};
    private static final CharSequence[] CATEGORY_ITEMS  = {"Old", "Middle Age", "Young"};
    private static final int REQUEST_IMAGE_GALLERY      = 0;
    private static final int REQUEST_IMAGE_CAPTURE      = 1;

    String mCurrentPhotoPath;

    private AutoCompleteTextView mCategoryView;
    private Bitmap mImageData;
    private ImageView mImageview;
    private View mProgressView;
    private View mAddPlaceFormView;
    private int mSelectedCategory           = 0;
    private boolean isGetImageFromGallery  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupComponent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                Uri targetUri = data.getData();
                Picasso.with(this)
                        .load(targetUri.toString())
                        .fit()
                        .centerCrop()
                        .into(mImageview);
                try {
                    mImageData = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Uri targetUri = data.getData();
                mCurrentPhotoPath = targetUri.toString();
                mImageData = (Bitmap) extras.get("data");
                mImageview.setImageBitmap(mImageData);
            }
        }
    }

    private void setupComponent() {
        mCategoryView   = (AutoCompleteTextView) findViewById(R.id.category);
        mImageview      = (ImageView) findViewById(R.id.image);

        mCategoryView.setFocusable(false);
        mCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategoryDialogBox();
            }
        });

        mImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPictureDialogBox();
            }
        });

        Button mPostButton = (Button) findViewById(R.id.post);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddPlace();
            }
        });

        mAddPlaceFormView = findViewById(R.id.home_form);
        mProgressView = findViewById(R.id.home_progress);
    }

    private void dispatchSelectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void showPictureDialogBox() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Add Picture");
        builder.setItems(PICTURE_ITEMS, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        isGetImageFromGallery = true;
                        dispatchSelectFromGallery();
                        break;
                    case 1:
                        isGetImageFromGallery = false;
                        dispatchTakePictureIntent();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void showCategoryDialogBox() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Select Category");
        builder.setSingleChoiceItems(CATEGORY_ITEMS,
                mSelectedCategory, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedCategory = item;
                    }});
        builder.setPositiveButton("set category", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mCategoryView.setText(CATEGORY_ITEMS[mSelectedCategory]);
            }
        });
        builder.show();
    }

    private void attemptAddPlace() {
        AutoCompleteTextView mTitleView = (AutoCompleteTextView) findViewById(R.id.title);
        AutoCompleteTextView mLocationView = (AutoCompleteTextView) findViewById(R.id.location);
        AutoCompleteTextView mDescriptionView = (AutoCompleteTextView) findViewById(R.id.description);

        mTitleView.setError(null);
        mLocationView.setError(null);
        mCategoryView.setError(null);
        mDescriptionView.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (mImageData == null || mImageData.sameAs(Bitmap.createBitmap(mImageData.getWidth(), mImageData.getHeight(), mImageData.getConfig()))) {
            Toast.makeText(this, "Please put Image", Toast.LENGTH_LONG).show();
            focusView = mImageview;
            cancel = true;
        } else if (TextUtils.isEmpty(mTitleView.getText().toString())) {
            mTitleView.setError(getString(R.string.error_field_required));
            focusView = mTitleView;
            cancel = true;
        } else if (TextUtils.isEmpty(mLocationView.getText().toString())) {
            mLocationView.setError(getString(R.string.error_field_required));
            focusView = mLocationView;
            cancel = true;
        } else if (TextUtils.isEmpty(mCategoryView.getText().toString())) {
            mCategoryView.setError(getString(R.string.error_field_required));
            focusView = mCategoryView;
            cancel = true;
        } else if (TextUtils.isEmpty(mDescriptionView.getText().toString())) {
            mDescriptionView.setError(getString(R.string.error_field_required));
            focusView = mDescriptionView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            if(isGetImageFromGallery){
                //send to server not implemented
                showHomePage();
            } else {
                galleryAddPic();
                //send to server not implemented
                showHomePage();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mAddPlaceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddPlaceFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddPlaceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAddPlaceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        showProgress(false);
    }

    private void showHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
