package com.example.peter.coffeekeeper.Controllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.peter.coffeekeeper.Adapters.BrewAdapter;
import com.example.peter.coffeekeeper.Database.DBOperator;
import com.example.peter.coffeekeeper.Models.BrewRecipe;
import com.example.peter.coffeekeeper.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class BrewFragment extends Fragment implements AdapterView.OnItemClickListener {


    private static final String LIST_STATE_KEY = "key";
    private static final String BREW_RECIPE_LIST_KEY = "key2" ;
    private static final String BREW_RECIPE_PREFERENCE_KEY = "brewListKey";
    private OnFragmentInteractionListener mListener;
    private FloatingActionButton addBrewButton;
    private RecyclerView rv;
    private boolean isViewShown;
    private DBOperator mDBOperator;
    private BrewAdapter ba;
    private ArrayList<BrewRecipe> brewRecipeArrayList;
    private String sortByCurrent;
    private ItemTouchHelper itemTouchHelper;
    private ImageButton imageButton, addButton;
    LinearLayoutManager llm;

    public BrewFragment() {
        // Required empty public constructor
    }


    public static BrewFragment newInstance(String param1, String param2) {
        BrewFragment fragment = new BrewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    //TODO add date added field to brew, sort by date, add newest to top of last saved instance



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDBOperator = new DBOperator(this.getContext());
        brewRecipeArrayList = new ArrayList<BrewRecipe>();
        ba = new BrewAdapter(getContext(), brewRecipeArrayList);
        new LoadBrews().execute("initialize");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(isVisibleToUser && getView()!=null) {
//            Log.i("SetVisible", "Set user visible called on Brew Fragment");
//            new LoadBrews().execute("checkDBChanged");
//        }
//    }

    //TODO HANDLE ARRAYLIST in onCreate instead of onResume

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO ROAST LISTS RECYCLERVIEW + SORT



        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_brew, container, false);
        rv = rootView.findViewById(R.id.brew_recycler_view);
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        rv.setAdapter(ba);
        if (savedInstanceState != null) {
            Log.i("Brew Order", "Setting to last saved order");
            try {
                Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(LIST_STATE_KEY);
                rv.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                brewRecipeArrayList = savedInstanceState.getParcelableArrayList(BREW_RECIPE_LIST_KEY);
                ba.setBrewList(brewRecipeArrayList);
            }
            catch (Exception e){
                e.printStackTrace();
                new LoadBrews().execute("initialize");
            }

        }


        //handle drog drop
        ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
            //and in your imlpementaion of
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(brewRecipeArrayList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                // and notify the adapter that its dataset has changed
                ba.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //TODO
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP );
            }
        };
        ItemTouchHelper ith = new ItemTouchHelper(ithCallback);
        ith.attachToRecyclerView(rv);
        imageButton = rootView.findViewById(R.id.sort_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu();
            }
        });
//        if (!isViewShown) {
//            new LoadBrews().execute("checkDBChanged");
//        }
        addButton = rootView.findViewById(R.id.add_brew_top_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddBrew.class);
                startActivityForResult(intent, 1);
            }
        });

        return rootView;


    }

    public void showSortMenu() {
        //creating a popup menu
        PopupMenu popup = new PopupMenu(getContext(), imageButton);
        //inflating menu from xml resource
        popup.inflate(R.menu.sort_menu);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sort_by_name:
                        new LoadBrews().execute("sortName");
                        return true;
                    case R.id.sort_by_method:
                        new LoadBrews().execute("sortMethod");
                        return true;
                    case R.id.sort_by_date:
                        new LoadBrews().execute("sortDate");
                        return true;
                    default:
                        return false;
                }
            }
        });
        //displaying the popup
        popup.show();
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(ba.getBrewList());
        Log.i("JSON", json);
        prefsEditor.putString(BREW_RECIPE_PREFERENCE_KEY, json);
        prefsEditor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Parcelable mListState = llm.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);
        outState.putParcelableArrayList(BREW_RECIPE_LIST_KEY, ba.getBrewList());
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tv = view.findViewById(R.id.brew_title_text_view);
        String brewName = tv.getText().toString();
        Intent myIntent = new Intent(view.getContext(), BrewRecipeActivity.class);
        myIntent.putExtra("Brew Name", brewName);
        startActivity(myIntent);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == AddBrew.CHANGED_BREW_DB_DATA) {
            new LoadBrews().execute("refreshDB");
        }
        Log.i("Result", "Brew frag result received");

    }

    private class LoadBrews extends AsyncTask<String, Void, ArrayList<BrewRecipe>> {
        private ArrayList<BrewRecipe> list;
        private boolean resort = false;

        @Override
        protected ArrayList<BrewRecipe> doInBackground(String... params) {
            list = brewRecipeArrayList;
            switch (params[0]) {
                case "initialize":
                    list = initialize();
                    break;
                case "sortName":
                    list = sortName();
                    break;
                case "sortDate":
                    list = sortDate();
                    break;
                case "sortMethod":
                    list = sortMethod();
                    break;
                case "refreshDB":
                    list = mDBOperator.getBrewRecipes();
                    resort = true;
                    break;

            }
            return list;
        }


        @Override
        protected void onPostExecute(ArrayList<BrewRecipe> brewRecipes) {
            super.onPostExecute(brewRecipes);
            brewRecipeArrayList = brewRecipes;
            ba.setBrewList(brewRecipes);
            if (resort) {
                doInBackground("sortDate");
            }
        }


        ArrayList<BrewRecipe> initialize(){
            try {
                ArrayList<BrewRecipe> dbBrewList = mDBOperator.getBrewRecipes();
                ArrayList<BrewRecipe> sortedJsonList = mDBOperator.getBrewRecipes();

                Collections.sort(dbBrewList, new Comparator<BrewRecipe>() {
                    @Override
                    public int compare(BrewRecipe brewRecipe, BrewRecipe t1) {
                        return brewRecipe.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                Gson gson = new Gson();
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getActivity().getApplicationContext());
                String json = appSharedPrefs.getString(BREW_RECIPE_PREFERENCE_KEY, "");
                if (json.isEmpty()) {
                    Log.i("Shared Pref", "No shared preferences for brew list order");
                } else {
                    Log.i("Shared Pref", "Loading shared preferences for brew list order");
                    Type type = new TypeToken<List<BrewRecipe>>() {
                    }.getType();
                    brewRecipeArrayList = gson.fromJson(json, type);
                    sortedJsonList = new ArrayList<>(brewRecipeArrayList);
                    Collections.sort(sortedJsonList, new Comparator<BrewRecipe>() {
                        @Override
                        public int compare(BrewRecipe brewRecipe, BrewRecipe t1) {
                            return brewRecipe.getName().compareToIgnoreCase(t1.getName());
                        }
                    });

                }
                if (brewRecipeArrayList != null) {
                    if (!dbBrewList.equals(sortedJsonList)) {
                        for (BrewRecipe br : sortedJsonList) {
                            Log.i("JSON", br.getName());
                        }
                        Log.i("Shared Pref", "Json list and db list dont match");
                        return dbBrewList;
                    } else {
                        Log.i("Shared Pref", "Lists match, setting to saved");
                        return brewRecipeArrayList;
                    }
                } else {
                    brewRecipeArrayList = dbBrewList;
                    return brewRecipeArrayList;
                }
            }
            catch(Exception e) {
                brewRecipeArrayList = mDBOperator.getBrewRecipes();
                return brewRecipeArrayList;
                }
        }

        ArrayList<BrewRecipe> sortName() {
            try {
                ArrayList<BrewRecipe> currentList = ba.getBrewList();
                Collections.sort(currentList, new Comparator<BrewRecipe>() {
                    @Override
                    public int compare(BrewRecipe o1, BrewRecipe o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
                return currentList;
            }
            catch(Exception e) {
                brewRecipeArrayList = mDBOperator.getBrewRecipes();
                return brewRecipeArrayList;
            }
        }

        ArrayList<BrewRecipe> sortDate() {
            try {
                ArrayList<BrewRecipe> currentList = ba.getBrewList();
                Collections.sort(currentList, new Comparator<BrewRecipe>() {
                    @Override
                    public int compare(BrewRecipe o1, BrewRecipe o2) {
                        int compareResult = 0;
                        try {
                            Date first = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(o1.getDateAdded());
                            Date end = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(o2.getDateAdded());
                            compareResult = end.compareTo(first);
                        } catch (ParseException e) {
                            Log.e("Parse Error", e.toString());
                        }
                        return compareResult;
                    }
                });
                return currentList;
            }
            catch(Exception e) {
                    brewRecipeArrayList = mDBOperator.getBrewRecipes();
                    return brewRecipeArrayList;
                }
        }

        ArrayList<BrewRecipe> sortMethod() {
            ArrayList<BrewRecipe> currentList = ba.getBrewList();
            Collections.sort(currentList, new Comparator<BrewRecipe>() {
                @Override
                public int compare(BrewRecipe o1, BrewRecipe o2) {
                    return o1.getBrewMethod().compareToIgnoreCase(o2.getBrewMethod());
                }
            });
            return currentList;

        }
    }
}
