/*
 * Moments - To the best Instagram client
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program;  if not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.moments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import ooo.oxo.moments.util.PostponedTransitionTrigger;
import ooo.oxo.moments.util.SimpleTransitionListener;
import ooo.oxo.moments.widget.ImmersiveUtil;
import ooo.oxo.moments.widget.PullBackLayout;

public class ViewerActivity extends AppCompatActivity implements PullBackLayout.Callback {

    @Bind(R.id.puller)
    PullBackLayout puller;

    @Bind(R.id.image)
    ImageViewTouch image;

    @Bind(R.id.header)
    View header;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.footer)
    View footer;

    @Bind(R.id.caption)
    TextView caption;

    private PostponedTransitionTrigger transitionTrigger;

    private ColorDrawable background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewer_activity);

        ButterKnife.bind(this);

        setTitle(null);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        puller.setCallback(this);

        if (getIntent().hasExtra("caption")) {
            caption.setText(getIntent().getStringExtra("caption"));
            caption.setVisibility(View.VISIBLE);
            footer.setVisibility(View.VISIBLE);
        } else {
            caption.setVisibility(View.GONE);
            footer.setVisibility(View.GONE);
        }

        supportPostponeEnterTransition();

        String url = getIntent().getDataString();

        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        image.setTransitionName(url);
        image.setSingleTapListener(this::toggleFade);
        image.setDoubleTapListener(this::fadeOut);

        transitionTrigger = new PostponedTransitionTrigger(this);

        DrawableRequestBuilder<String> request = Glide.with(this).load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(transitionTrigger);

        if (getIntent().hasExtra("thumbnail")) {
            request.thumbnail(Glide.with(this)
                    .load(getIntent().getStringExtra("thumbnail"))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE));
        }

        request.into(new GlideDrawableImageViewTarget(image) {
            @Override
            public void getSize(SizeReadyCallback cb) {
                cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL);
            }
        });

        getWindow().getEnterTransition().addListener(new SimpleTransitionListener() {
            @Override
            public void onTransitionEnd(Transition transition) {
                getWindow().getEnterTransition().removeListener(this);
                fadeIn();
            }
        });

        background = new ColorDrawable(Color.BLACK);
        getWindow().getDecorView().setBackground(background);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transitionTrigger.cancel();
    }

    private void fadeIn() {
        header.animate().alpha(1).start();
        footer.animate().alpha(1).start();
        toolbar.animate().alpha(1).start();
        caption.animate().alpha(1).start();
        showSystemUi();
    }

    private void fadeOut() {
        header.animate().alpha(0).start();
        footer.animate().alpha(0).start();
        toolbar.animate().alpha(0).start();
        caption.animate().alpha(0).start();
        hideSystemUi();
    }

    private void showSystemUi() {
        ImmersiveUtil.exit(image);
    }

    private void hideSystemUi() {
        ImmersiveUtil.enter(image);
    }

    private void toggleFade() {
        if (header.getAlpha() == 0) {
            fadeIn();
        } else {
            fadeOut();
        }
    }

    @Override
    public void onPullStart() {
        fadeOut();
        showSystemUi();
    }

    @Override
    public void onPull(float progress) {
        background.setAlpha((int) (0xff * (1f - progress * 0.75f)));
    }

    @Override
    public void onPullCancel() {
        fadeIn();
    }

    @Override
    public void onPullComplete() {
        supportFinishAfterTransition();
    }

    @Override
    public boolean canPullDown() {
        return !image.canScroll();
    }

}
