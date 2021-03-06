package se.ju.student.android_mjecipes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class RecipePageFragment extends Fragment implements View.OnClickListener {

    public interface OnFragmentInteractionListener {
        void loaded();
        void onRecipeLongClick(View v, int recipeID, String creatorID);
    }

    private static final String ARG_RECIPES = "recipes";
    private static final String ARG_USERNAME = "username";

    private Recipe[] recipes;
    private String username;

    private OnFragmentInteractionListener mListener;
    private LinearLayout pageLayout;
    public static ActionMode actionMode;

    public RecipePageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            recipes = (Recipe[]) getArguments().getSerializable(ARG_RECIPES);
            username = getArguments().getString(ARG_USERNAME);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageLayout = (LinearLayout) view.findViewById(R.id.recipe_page_ll);
        inflateRecipes(recipes, username);
        if(mListener != null)
            mListener.loaded();
    }

    private void inflateRecipes(final Recipe[] recipes, String username) {
        LayoutInflater inf = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < recipes.length; ++i) {
            if(recipes[i] == null)
                break;

            inf.inflate(R.layout.recipe_entry, pageLayout);
            final View vv = pageLayout.getChildAt(i);
            ((TextView) vv.findViewById(R.id.main_recipe_id)).setText(String.format(Locale.ENGLISH, "%d", recipes[i].id));
            ((TextView) vv.findViewById(R.id.main_recipe_name)).setText(recipes[i].name);
            ((TextView) vv.findViewById(R.id.main_recipe_date)).setText(sdf.format(new Date(recipes[i].created * 1000)));
            ((TextView) vv.findViewById(R.id.main_recipe_description)).setText(recipes[i].description);
            ((TextView) vv.findViewById(R.id.main_recipe_creatorname)).setText(username != null ? username : (recipes[i].creator == null ? "" : recipes[i].creator.userName));
            if (recipes[i].image != null) {
                final ImageView iv = (ImageView) vv.findViewById(R.id.main_recipe_image);
                CacheHandler.getImageCacheHandler(getActivity()).downloadImage(new ImageRequest(recipes[i].image, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        if(iv.getWidth() < response.getWidth())
                            return;
                        //TODO handle bitmaps smarter
                        //TODO handle caching first
                        iv.setImageBitmap(response);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }, iv.getWidth(), iv.getHeight(), null, null, null));
            }

            vv.setOnClickListener(this);

            final String cID = recipes[i].creator != null ? recipes[i].creator.id : recipes[i].creatorId;
            final int rID = recipes[i].id;
            vv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(mListener != null) {
                        mListener.onRecipeLongClick(v, rID, cID);
                        actionMode = ((MainActivity) getActivity()).getActionMode();
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if(actionMode != null) {
            actionMode.finish();
            actionMode = null;
        } else {
            Intent i = new Intent(getActivity(), ShowRecipeActivity.class);
            i.putExtra("recipeId", ((TextView) v.findViewById(R.id.main_recipe_id)).getText());
            startActivity(i);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public static RecipePageFragment newInstance(Recipe[] recipes, String username) {
        RecipePageFragment fragment = new RecipePageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECIPES, recipes);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

}
