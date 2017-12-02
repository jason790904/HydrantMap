package com.renai.firedepartment.keelong.hydrantmap;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ManualActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    final static int SEARCH_PAGE_COUNT =10;
    final static int MAIN_PAGE_COUNT = 7;
    final static int INVESITIGATION_PAGE_COUNT = 9;
    final static int HISTORY_PAGE_COUNT =1;
    final static int ADD_PAGE_COUNT = 3 ;

    public final static int MANUAL_TYPE_MAIN = 500;
    public final static int MANUAL_TYPE_INVESITIGATION =501;
    public final static int MANUAL_TYPE_SEARCH =502;
    public final static int MANUAL_TYPE_HISTORY =503;
    public final static int MANUAL_TYPE_ADD =504;

    public final static String ARG_MANUAL_TYPE = "manual_type";

    private int type_code;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);


        type_code = getIntent().getIntExtra(ARG_MANUAL_TYPE,-1);
        if(type_code!=-1) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),type_code);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        }else{
            this.finish();
        }

        mViewPager.addOnPageChangeListener(pageChangeListener);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            /*if(position-1 >= 0)
                ((PlaceholderFragment)mSectionsPagerAdapter.getItem(position-1)).destroyImage();
            if(position+1<mSectionsPagerAdapter.getCount())
                ((PlaceholderFragment)mSectionsPagerAdapter.getItem(position+1)).destroyImage();
*/
            //((PlaceholderFragment)mSectionsPagerAdapter.getItem(position)).setImage(getResources());


        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_INDEX = "section_number";
        private static final String ARG_RES_ID = "resId";
        private static final String ARG_TOTAL="total";

        TextView currentPage;
        TextView totalPage ;
        ImageView content;
        int ResourceId;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int index,int resourceId,int totalPages) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_INDEX, index);
            args.putInt(ARG_RES_ID, resourceId);
            args.putInt(ARG_TOTAL,totalPages);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_manual, container, false);
             currentPage=rootView.findViewById(R.id.manual_currentPage);
             totalPage = rootView.findViewById(R.id.manual_totalPage);
             content = rootView.findViewById(R.id.manual_imageView);

            currentPage.setText(String.valueOf(getArguments().getInt(ARG_INDEX)));
            totalPage.setText(String.valueOf(getArguments().getInt(ARG_TOTAL)));

            ResourceId = getArguments().getInt(ARG_RES_ID);
            content.setImageResource(ResourceId);


            return rootView;
        }


        @Override
        public void onDestroy(){
            super.onDestroy();
            content.setImageDrawable(null);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        int typeCode;
        int totalPage;
        ArrayList<Integer> manualPages;

        public SectionsPagerAdapter(FragmentManager fm , int typecode) {
            super(fm);
            typeCode = typecode;
            manualPages = new ArrayList<>();
            switch (typeCode){
                case MANUAL_TYPE_ADD:
                    totalPage = ADD_PAGE_COUNT;
                    manualPages.add(R.drawable.manual_add_page_1);
                    manualPages.add(R.drawable.manual_add_page_2);
                    manualPages.add(R.drawable.manual_add_page_3);

                    break;
                case MANUAL_TYPE_MAIN:
                    totalPage = MAIN_PAGE_COUNT;
                    manualPages.add(R.drawable.manual_main_page1);
                    manualPages.add(R.drawable.manual_main_page2);
                    manualPages.add(R.drawable.manual_main_page3);
                    manualPages.add(R.drawable.manual_main_page4);
                    manualPages.add(R.drawable.manual_main_page5);
                    manualPages.add(R.drawable.manual_main_page6);
                    manualPages.add(R.drawable.manual_main_page7);

                    break;
                case MANUAL_TYPE_INVESITIGATION:
                    totalPage = INVESITIGATION_PAGE_COUNT;
                    manualPages.add(R.drawable.manual_investigation_page_1);
                    manualPages.add(R.drawable.manual_investigation_page_2);
                    manualPages.add(R.drawable.manual_investigation_page_3);
                    manualPages.add(R.drawable.manual_investigation_page_4);
                    manualPages.add(R.drawable.manual_investigation_page_5);
                    manualPages.add(R.drawable.manual_investigation_page_6);
                    manualPages.add(R.drawable.manual_investigation_page_7);
                    manualPages.add(R.drawable.manual_investigation_page_8);
                    manualPages.add(R.drawable.manual_investigation_page_9);



                    break;
                case MANUAL_TYPE_HISTORY:
                    totalPage = HISTORY_PAGE_COUNT;
                    manualPages.add(R.drawable.manul_history_page1);

                    break;
                case MANUAL_TYPE_SEARCH:
                    totalPage = SEARCH_PAGE_COUNT;
                    manualPages.add(R.drawable.manul_search_page_1);
                    manualPages.add(R.drawable.manul_search_page_2);
                    manualPages.add(R.drawable.manul_search_page_3);
                    manualPages.add(R.drawable.manul_search_page_4);
                    manualPages.add(R.drawable.manul_search_page_5);
                    manualPages.add(R.drawable.manul_search_page_6);
                    manualPages.add(R.drawable.manul_search_page_7);
                    manualPages.add(R.drawable.manul_search_page_8);
                    manualPages.add(R.drawable.manul_search_page_9);
                    manualPages.add(R.drawable.manul_search_page_10);

                    break;
            }
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1,manualPages.get(position),totalPage);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return totalPage;
        }
    }
}
