package no.hyper.androidutil.view.viewpager;

import android.support.v4.view.ViewPager;
import android.util.Log;

public class SelectListener implements ViewPager.OnPageChangeListener {
    private static final String TAG = SelectListener.class.getSimpleName();
    private final FragmentRegistryAdapter adapter;

    public SelectListener(FragmentRegistryAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.v(TAG, "page " + position + " selected!");

        for (int i = 0; i < adapter.getCount(); i++) {
            LoadTimingListener listener = (LoadTimingListener) adapter.getRegisteredFragment(i);

            if (listener == null) {
                continue;
            }

            if (i == position) {
                listener.onLoad(false);
            } else {
                listener.onUnload();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
