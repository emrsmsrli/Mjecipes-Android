package se.ju.student.android_mjecipes;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class CreateCommentFragment extends Fragment implements View.OnClickListener {

    public interface OnCommentPostedListener {
        void onCommentPosted(boolean posted, Errors errors);
    }

    private static final int IMAGE_REQUEST_CODE = 1;
    private static final String ARG_TEXT = "text";
    private static final String ARG_RATING = "rating";
    private static final String ARG_ID = "id";

    private Uri outputFileUri;
    private String text;
    private int rating;
    private int id;
    private boolean edit;

    private EditText textField;
    private Button uploadImage;
    private RatingBar gradeBar;
    private FloatingActionButton fab;
    private Uri imageURI = null;

    private OnCommentPostedListener mListener;

    public CreateCommentFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TEXT, "");
            rating = getArguments().getInt(ARG_RATING, 3);
            id = getArguments().getInt(ARG_ID, 0);
        } else {
            text = "";
            rating = 3;
            id = 0;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textField = (EditText) view.findViewById(R.id.create_comment_text);
        gradeBar = (RatingBar) view.findViewById(R.id.create_comment_grade);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.create_comment_fab);
        uploadImage = (Button)  view.findViewById(R.id.create_comment_upload_button);
        Button postButton = (Button) view.findViewById(R.id.create_comment_post_button);
        Button close = (Button) view.findViewById(R.id.create_comment_close);

        textField.setText(text);
        gradeBar.setRating(rating);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fixFAB();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if(TextUtils.isEmpty(textField.getText())) {
            uploadImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(imageURI == null)
                        openImageIntent();
                    else {
                        imageURI = null;
                        uploadImage.setText(getString(R.string.create_comment_upload_image_button));
                    }
                }

                private void openImageIntent() {
                    requestReadPermission();
                    final File root = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "Mjecipes");
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
                    final PackageManager packageManager = getActivity().getPackageManager();
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

            });
            close.setVisibility(View.INVISIBLE);
            edit = false;
        } else {
            uploadImage.setVisibility(View.INVISIBLE);
            edit = true;
        }

        postButton.setOnClickListener(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case IMAGE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final boolean isCamera;
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null && action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }

                    imageURI = isCamera ? outputFileUri : data.getData();
                    uploadImage.setText(getString(R.string.create_comment_remove_image_button));
                }
                break;
        }
    }

    private void fixFAB() {
        if(!edit) {
            if(fab != null)
                fab.setImageResource(R.drawable.ic_format_quote_white_24dp);
        } else {
            if(fab != null && fab.getVisibility() == View.INVISIBLE)
                fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        Comment c = new Comment();
        c.text = textField.getText().toString();
        c.grade = (int)gradeBar.getRating();

        new AsyncTask<Comment, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Comment... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(getActivity()).getUsername(),
                        UserAgent.getInstance(getActivity()).getPassword()
                );

                if (token != null) {
                    if(edit) {
                        params[0].id = id;
                        return Handler.getCommentHandler().patchComment(params[0], token);
                    } else {
                        params[0].commenterId = UserAgent.getInstance(getActivity()).getUserID();

                        if(Handler.getRecipeHandler().postComment(id, params[0], token)) {
                            int commentid = Integer.parseInt(Handler.getRecipeHandler().getErrors().error);

                            if (imageURI != null) {
                                InputStream is;
                                try {
                                    is = getActivity().getContentResolver().openInputStream(imageURI);
                                    return is != null && Handler.getCommentHandler().postImage(commentid, is, token);
                                } catch(FileNotFoundException e) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    CacheHandler.getJSONJsonCacheHandler(getActivity()).clearSingleJSONCache(Integer.toString(id), Comment.class);
                    if (imageURI != null)
                        CacheHandler.getImageCacheHandler(getActivity()).clearSingleImageCache("Comment-" + id);
                }

                if(mListener != null)
                    mListener.onCommentPosted(result, result ? null : (edit ? Handler.getCommentHandler().getErrors() : Handler.getRecipeHandler().getErrors()));

                fixFAB();
                getActivity().getSupportFragmentManager().popBackStack();
            }

        }.execute(c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_comment, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCommentPostedListener) {
            mListener = (OnCommentPostedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void requestReadPermission() {
        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getActivity().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static CreateCommentFragment newInstance(String text, int rating, int commentID) {
        CreateCommentFragment fragment = new CreateCommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putInt(ARG_RATING, rating);
        args.putInt(ARG_ID, commentID);
        fragment.setArguments(args);
        return fragment;
    }

}
