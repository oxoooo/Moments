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

package ooo.oxo.moments.net;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import java.io.InputStream;

import ooo.oxo.moments.model.Media;

public class ImageCandidatesLoader extends BaseGlideUrlLoader<Media.ImageCandidates> {

    private static final String TAG = "ImageCandidatesLoader";

    public ImageCandidatesLoader(ModelLoader<GlideUrl, InputStream> concreteLoader) {
        super(concreteLoader);
    }

    @Override
    protected String getUrl(Media.ImageCandidates model, int width, int height) {
        Media.Resource best = null;

        for (Media.Resource candidate : model.candidates) {
            if (best == null) {
                Log.d(TAG, String.format("pick %dx%d for %dx%d first",
                        candidate.width, candidate.height, width, height));
                best = candidate;
                continue;
            }

            // TODO: Not perfect while has original ratio candidates.
            // Should find a pair of whose one side is closest to while the other side equals.
            if (candidate.width < width || candidate.height < height) {
                if (candidate.width > best.width && candidate.height > best.width) {
                    Log.d(TAG, String.format("prefer larger %dx%d than %dx%d for %dx%d",
                            candidate.width, candidate.height, best.width, best.height,
                            width, height));
                    best = candidate;
                } else {
                    Log.d(TAG, String.format("%dx%d is not larger than %dx%d for %dx%d",
                            candidate.width, candidate.height, best.width, best.height,
                            width, height));
                }
            } else {
                if (candidate.width < best.width && candidate.height < best.height) {
                    Log.d(TAG, String.format("prefer smaller %dx%d than %dx%d for %dx%d",
                            candidate.width, candidate.height, best.width, best.height,
                            width, height));
                    best = candidate;
                } else {
                    Log.d(TAG, String.format("%dx%d is not smaller than %dx%d for %dx%d",
                            candidate.width, candidate.height, best.width, best.height,
                            width, height));
                }
            }
        }

        return best == null ? null : best.url;
    }

    public static class Factory implements ModelLoaderFactory<Media.ImageCandidates, InputStream> {

        @Override
        public ModelLoader<Media.ImageCandidates, InputStream> build(
                Context context, GenericLoaderFactory factories) {
            return new ImageCandidatesLoader(
                    factories.buildModelLoader(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {
        }

    }

}
