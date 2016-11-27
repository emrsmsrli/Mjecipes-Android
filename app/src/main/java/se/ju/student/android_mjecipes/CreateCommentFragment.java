package se.ju.student.android_mjecipes;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    private static final String ARG_TEXT = "text";
    private static final String ARG_RATING = "rating";
    private static final String ARG_ID = "id";

    private String text;
    private int rating;
    private int id;

    private EditText textField;
    private RatingBar gradeBar;

    private OnCommentPostedListener mListener;

    public CreateCommentFragment() {

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

        textField.setText(text);
        gradeBar.setRating(rating);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        postButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Comment c = new Comment();
        c.id = id;
        c.text = textField.getText().toString();
        c.grade = (int)gradeBar.getRating();

        new AsyncTask<Comment, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Comment... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(getActivity()).getUsername(),
                        UserAgent.getInstance(getActivity()).getPassword()
                );

                return token != null && Handler.getCommentHandler().patchComment(params[0], token);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result)
                    CacheHandler.getJSONJsonCacheHandler(getActivity()).clearSingleJSONCache(Integer.toString(id), Comment.class);
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

    public interface OnCommentPostedListener {
        void onCommentPosted(boolean posted);
    }

}
