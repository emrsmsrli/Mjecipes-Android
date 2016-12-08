package se.ju.student.android_mjecipes;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Direction;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class CreateRecipeActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE = 1;

    private FrameLayout activityLayout;
    private EditText name;
    private EditText description;
    private LinearLayout directionsLayout;
    private Button post;
    private boolean editMode = false;
    private Uri outputFileUri = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_recipe_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(editMode)
            menu.findItem(R.id.upload_image).setVisible(false);
        else {
            if (outputFileUri != null)
                menu.findItem(R.id.upload_image).setIcon(R.drawable.ic_remove_circle_outline_white_24dp);
            else
                menu.findItem(R.id.upload_image).setIcon(R.drawable.ic_image_white_24dp);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.upload_image:
                if(outputFileUri == null)
                    openImageIntent();
                else
                    outputFileUri = null;
                invalidateOptionsMenu();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        activityLayout = (FrameLayout) findViewById(R.id.activity_create_recipe);
        name = (EditText) findViewById(R.id.name_edit_text);
        description = (EditText) findViewById(R.id.desc_edit_text);
        directionsLayout = (LinearLayout) findViewById(R.id.directions_ll);
        post = (Button) findViewById(R.id.post_button);
        Button addDirection = (Button) findViewById(R.id.direction_add_button);

        if(directionsLayout != null)
            directionsLayout
                    .findViewById(R.id.direction_entry)
                    .findViewById(R.id.remove_direction_button)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    directionsLayout.removeView(directionsLayout.findViewById(R.id.direction_entry));
                }
            });

        if(addDirection != null)
            addDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View direction = inflateDirectionEntry("");
                    direction.findViewById(R.id.remove_direction_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            directionsLayout.removeView(direction);
                        }
                    });
                }
            });

        if(getIntent().hasExtra("recipeID")) {
            editMode = true;
            invalidateOptionsMenu();
            editMode();
            return;
        }

        if(post != null)
            post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setClickable(false);
                    Recipe r = initRecipe();
                    postRecipe(r);
                }
            });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    private View inflateDirectionEntry(String description) {
        getLayoutInflater().inflate(R.layout.direction_entry, directionsLayout, true);
        final View v = directionsLayout.getChildAt(directionsLayout.getChildCount() - 1);
        ((EditText)v.findViewById(R.id.direction_edit_text)).setText(description);
        v.findViewById(R.id.remove_direction_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                directionsLayout.removeView(v);
            }
        });
        return v;
    }

    private void editMode() {
        setTitle("Edit recipe");
        View v = findViewById(R.id.loading_screen);
        if(v != null)
            v.setVisibility(View.VISIBLE);

        final Intent intent = getIntent();

        directionsLayout.removeView(
                directionsLayout
                        .findViewById(R.id.direction_entry));

        name.setText(intent.getStringExtra("recipeName"));
        description.setText(intent.getStringExtra("recipeDesc"));
        String[] directions = intent.getStringArrayExtra("recipeDirecs");
        for(String desc: directions)
            inflateDirectionEntry(desc);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                Recipe r = initRecipe();
                r.id = Integer.parseInt(intent.getStringExtra("recipeID"));
                editRecipe(r);
            }
        });

        if(v != null)
            v.setVisibility(View.GONE);
    }

    private Recipe initRecipe() {
        Recipe r = new Recipe();
        r.name = name.getText().toString();
        r.description = description.getText().toString();
        r.creatorId=UserAgent.getInstance(getBaseContext()).getUserID();

        int childCount = directionsLayout.getChildCount();
        Direction[] directions = new Direction[childCount];
        for(int i = 0; i < childCount; ++i) {
            directions[i] = new Direction();
            directions[i].order = i;
            directions[i].description = ((EditText)directionsLayout.getChildAt(i).findViewById(R.id.direction_edit_text)).getText().toString();
        }

        r.directions = directions.length == 0 ? null : directions;

        return r;
    }

    private void postRecipe(Recipe recipe) {
        new AsyncTask<Recipe, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Recipe... params) {
                JWToken token = getToken();

                InputStream is = null;
                if(outputFileUri != null) {
                    try {
                        is = getContentResolver().openInputStream(outputFileUri);
                    } catch (FileNotFoundException e) {
                        is = null;
                    }
                }

                if(token != null && Handler.getRecipeHandler().postRecipe(params[0], token)) {
                    if(is != null)
                        uploadImage(Integer.parseInt(Handler.getRecipeHandler().getErrors().error), is);
                    return true;
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean posted) {
                if(posted) {
                    setResult(RESULT_OK);
                    finish();
                } else
                    showError();
            }

        }.execute(recipe);
    }

    private void editRecipe(Recipe recipe) {
        new AsyncTask<Recipe, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Recipe... params) {
                JWToken token = getToken();

                return token != null && Handler.getRecipeHandler().patchRecipe(params[0], token);
            }

            @Override
            protected void onPostExecute(Boolean edited) {
                if(edited) {
                    setResult(RESULT_OK);
                    finish();
                } else
                    showError();
            }
        }.execute(recipe);
    }

    private void showError() {
        post.setClickable(true);

        Errors errors = Handler.getRecipeHandler().getErrors();
        View focus = null;
        if(errors.hasError(Errors.RECIPE_NAME_MISSING)) {
            name.setError(getString(R.string.error_recipe_name_missing));
            focus = name;
        } else if(errors.hasError(Errors.RECIPE_NAME_WRONG_LENGTH)) {
            name.setError(getString(R.string.error_recipe_name_wrong_length));
            focus = name;
        } else if(errors.hasError(Errors.RECIPE_DESCRIPTION_MISSING)) {
            description.setError(getString(R.string.error_recipe_desc_missing));
            focus = description;
        } else if(errors.hasError(Errors.RECIPE_DESCRIPTION_WRONG_LENGTH)) {
            description.setError(getString(R.string.error_recipe_desc_wrong_length));
            focus = description;
        } else if(errors.hasError(Errors.RECIPE_DIRECTIONS_TOO_FEW))
            Snackbar.make(activityLayout, getString(R.string.error_recipe_direc_missing), Snackbar.LENGTH_SHORT).show();
        else if(errors.hasError(Errors.RECIPE_DIRECTION_DESCRIPTION_MISSING))
            Snackbar.make(activityLayout, getString(R.string.error_recipe_direc_desc_missing), Snackbar.LENGTH_SHORT).show();
        else if(errors.hasError(Errors.RECIPE_DIRECTION_DESCRIPTION_WRONG_LENGTH))
            Snackbar.make(activityLayout, getString(R.string.error_recipe_direc_desc_wrong_length), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(activityLayout, getString(R.string.something_went_wrong), Snackbar.LENGTH_SHORT).show();

        if(focus != null)
            focus.requestFocus();
    }

    private JWToken getToken() {
        return Handler.getTokenHandler().getToken(
                UserAgent.getInstance(this).getUsername(),
                UserAgent.getInstance(this).getPassword()
        );
    }

    public void requestReadPermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case IMAGE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    final boolean isCamera;
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null && action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }

                    if(!isCamera)
                        outputFileUri = data.getData();
                } else {
                    outputFileUri = null;
                    invalidateOptionsMenu();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                Snackbar.make(activityLayout, getString(R.string.error_upload_image_permission_needed), Snackbar.LENGTH_SHORT);
            }
        }
    }

    private void uploadImage(final int recipeID, InputStream stream) {
        new AsyncTask<InputStream, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(InputStream... params) {
                JWToken token = getToken();

                return token != null && Handler.getRecipeHandler().postImage(recipeID, params[0], token);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if(!result)
                    Snackbar.make(activityLayout, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();
            }
        }.execute(stream);
    }

    private void openImageIntent() {
        requestReadPermission();
        final File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "Mjecipes");
        if(root.mkdirs())
            return;
        final String fname = "img-" + UUID.randomUUID().toString();

        File sdImageMainDirectory;
        try {
            sdImageMainDirectory = File.createTempFile(fname, ".jpg", root);
        } catch(IOException e) {
            return;
        }

        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE);
    }

}
