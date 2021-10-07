package com.example.demoplayerpoc;

import android.content.res.Resources;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;

public class Utils {

    public static String joinWithSeparator(Resources resources, String... items) {
        String itemList = "";
        for (String item : items) {
            if (item.length() > 0) {
                if (TextUtils.isEmpty(itemList)) {
                    itemList = item;
                } else {
                    itemList = resources.getString(com.google.android.exoplayer2.ui.R.string.exo_item_list, itemList, item);
                }
            }
        }
        return itemList;
    }

    public static String buildRoleString(Resources resources, Format format) {
        String roles = "";
        if ((format.roleFlags & C.ROLE_FLAG_ALTERNATE) != 0) {
            roles = resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_role_alternate);
        }
        if ((format.roleFlags & C.ROLE_FLAG_SUPPLEMENTARY) != 0) {
            roles = joinWithSeparator(resources, resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_role_supplementary));
        }
        if ((format.roleFlags & C.ROLE_FLAG_COMMENTARY) != 0) {
            roles = joinWithSeparator(resources, resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_role_commentary));
        }
        if ((format.roleFlags & (C.ROLE_FLAG_CAPTION | C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND)) != 0) {
            roles = joinWithSeparator(resources, resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_role_closed_captions));
        }
        return roles;
    }

    public static String buildResolutionString(Resources resources, Format format) {
        int width = format.width;
        int height = format.height;
        return width == Format.NO_VALUE || height == Format.NO_VALUE
                ? ""
                : resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_resolution, width, height);
    }

    public static String buildBitrateString(Resources resources, Format format) {
        int bitrate = format.bitrate;
        return bitrate == Format.NO_VALUE
                ? ""
                : resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_bitrate, bitrate / 1000000f);
    }

    public static String buildAudioChannelString(Resources resources, Format format) {
        int channelCount = format.channelCount;
        if (channelCount == Format.NO_VALUE || channelCount < 1) {
            return "";
        }
        switch (channelCount) {
            case 1:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_mono);
            case 2:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_stereo);
            case 6:
            case 7:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_surround_5_point_1);
            case 8:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_surround_7_point_1);
            default:
                return resources.getString(com.google.android.exoplayer2.ui.R.string.exo_track_surround);
        }
    }

    public static String buildLanguageOrLabelString(Resources resources, Format format) {
        String languageAndRole =
                joinWithSeparator(resources, buildLanguageString(format), buildRoleString(resources, format));
        return TextUtils.isEmpty(languageAndRole) ? buildLabelString(format) : languageAndRole;
    }

    private static String buildLabelString(Format format) {
        return TextUtils.isEmpty(format.label) ? "" : format.label;
    }
    private static String buildLanguageString(Format format) {
        String language = format.language;
        if (TextUtils.isEmpty(language) || C.LANGUAGE_UNDETERMINED.equals(language)) {
            return "";
        }
        Locale locale = Util.SDK_INT >= 21 ? Locale.forLanguageTag(language) : new Locale(language);
        return locale.getDisplayName();
    }

}
