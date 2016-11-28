package se.ju.student.android_mjecipes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class CreateCommentFragment extends Fragment implements View.OnClickListener {

    public interface OnCommentPostedListener {
        void onCommentPosted(boolean posted);
    }

    private static final int IMAGE_REQUEST_CODE = 1;
    private static final String ARG_TEXT = "text";
    private static final String ARG_RATING = "rating";
    private static final String ARG_ID = "id";

    private String text;
    private int rating;
    private int id;
    private boolean edit;

    private EditText textField;
    private RatingBar gradeBar;
    private String imageDir = null;

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
        Button postButton = (Button) view.findViewById(R.id.create_comment_post_button);
        Button close = (Button) view.findViewById(R.id.create_comment_close);
        Button uploadImage = (Button)  view.findViewById(R.id.create_comment_upload_button);

        textField.setText(text);
        gradeBar.setRating(rating);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if(TextUtils.isEmpty(textField.getText())) {
            uploadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // FIXME: 28/11/2016 add camera request
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i, IMAGE_REQUEST_CODE);
                }
            });
            edit = false;
        } else {
            uploadImage.setClickable(false);
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
                if(resultCode == MainActivity.RESULT_OK) {
                    imageDir = getRealPathFromURI(data.getData());
                }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;

        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
                        boolean toReturn = Handler.getRecipeHandler().postComment(id, params[0], token);
                        if (imageDir != null)
                            toReturn &= Handler.getCommentHandler().postImage(id, imageDir, token);
                        return toReturn;
                    }
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    CacheHandler.getJSONJsonCacheHandler(getActivity()).clearSingleJSONCache(Integer.toString(id), Comment.class);
                    if (imageDir != null)
                        CacheHandler.getImageCacheHandler(getActivity()).clearSingleImageCache(imageDir);
                }

                if(mListener != null)
                    mListener.onCommentPosted(result);
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
