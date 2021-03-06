package com.example.peter.coffeekeeper.Controllers;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.billingclient.api.Purchase;
import com.example.peter.coffeekeeper.Database.DBOperator;
import com.example.peter.coffeekeeper.Models.BrewRecipe;
import com.example.peter.coffeekeeper.R;
import com.example.peter.coffeekeeper.SectionsPageAdapter;
import com.example.peter.coffeekeeper.util.BillingManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

public class BrewRecipeActivity extends AppCompatActivity implements BrewRecipeFragment.SendBrew, BillingManager.BillingUpdatesListener {

    private BrewRecipe br;
    DBOperator db;
    private int waterUnits = 16;
    TextView waterWeightTv, coffeeWeightTv, grindTv, strengthTv, textTimer, stepTv, notesTv, titleTv;
    CountDownTimer countDownTimer;
    ProgressBar barTimer;
    Button showNotesButton;
    int bloomMinutes, bloomSeconds, brewSeconds;
    boolean brewFinished, isNotesShowing;
    android.support.v7.widget.Toolbar myBar;
    String name;
    LinearLayout bottomSheet;
    BottomSheetBehavior bottomSheetBehavior;
    ImageView imageView;
    BillingManager billingManager;
    AdView adView;

    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;



    //Testing
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_recipe);
        name = getIntent().getExtras().getString("Brew Name");
        DBOperator dbOperator = new DBOperator(this);
        br = dbOperator.getBrewRecipe(name);
//        ActionBar ab = getSupportActionBar();
//        ab.setTitle(name);
//        ab.setElevation(0);
//        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        if(savedInstanceState == null) {
            BrewRecipeFragment brewRecipeFragment = new BrewRecipeFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frameLayout, brewRecipeFragment, "recipeFragment")
                    .disallowAddToBackStack()
                    .commit();
            isNotesShowing = false;
        }

        billingManager = new BillingManager(this, this);


        imageView = findViewById(R.id.notes_drop_icon);
        bottomSheet = findViewById(R.id.bottom_sheet);
        // init the bottom sheet behavior
         bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);



// set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    float deg = (imageView.getRotation() == 0F) ? 180F : 0F;
                    imageView.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        myBar = findViewById(R.id.my_bar);
        setSupportActionBar(myBar);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(name);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        showNotesButton = findViewById(R.id.show_notes_button);
        final BrewNotesFragment notesFragment = new BrewNotesFragment();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                float deg = (v.getRotation() == 0F) ? 180F : 0F;
//                v.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        showNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                float deg = (imageView.getRotation() == 0F) ? 180F : 0F;
//                imageView.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });



        notesTv = findViewById(R.id.notes_text_view);
        grindTv = findViewById(R.id.brew_grind_notes_text_view);
        titleTv = findViewById(R.id.brew_title_notes_text_view);
        notesTv.setText(br.getNotes());
        titleTv.setText(name);
        grindTv.setText(br.getGrind());
        checkPremium();

//
    }

    @Override
    protected void onDestroy() {
        billingManager.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration config) {
        super.onConfigurationChanged(config);


    }


    public void update(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void sendBrewRecipe(BrewRecipe brew) {
        String notes = brew.getNotes();
        String grind = brew.getGrind();
//        BrewNotesFragment bn = (BrewNotesFragment) getSupportFragmentManager().findFragmentByTag("notesFragment");
//        bn.setTitle(brew.getName());
//        bn.setNotes(notes);
//        bn.setGrind(grind);
    }

    public void checkPremium() {
        adView = findViewById(R.id.adView);

        billingManager.queryPurchases();
    }

    @Override
    public void onBillingClientSetupFinished() {

    }

    @Override
    public void onConsumeFinished(String token, int result) {

    }

    @Override
    public void onPurchasesUpdated(List<Purchase> purchases) {

        boolean isPremium = false;
        for(com.android.billingclient.api.Purchase p : purchases) {
            if(p.getSku().equals(MainActivity.SKU)) {
                Log.d("Billing", "Purchased...");
                isPremium = true;
            }
        }
        if(isPremium && adView.getVisibility() == View.VISIBLE) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        if (!isPremium) {
            Log.d("Billing", "Not premium...");
            adView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

    }
}
