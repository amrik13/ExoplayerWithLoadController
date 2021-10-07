package com.example.demoplayerpoc;

import static com.example.demoplayerpoc.Utils.*;
import static com.example.demoplayerpoc.Utils.buildBitrateString;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QualityListAdapter extends RecyclerView.Adapter<QualityListAdapter.QualityItemViewHolder> {

    private Context mContext;
    private List<TrackGroupArray> trackGroupArrayList;
    private MappingTrackSelector.MappedTrackInfo trackInfo;
    private int trackType;
    private boolean isDefaultClicked;
    private DefaultTrackSelector.Parameters parameters;

    public QualityListAdapter(Context mContext, DefaultTrackSelector.Parameters parameters, MappingTrackSelector.MappedTrackInfo trackInfo, List<TrackGroupArray> trackGroupArrayList, int trackType){
        this.mContext = mContext;
        this.parameters = parameters;
        this.trackInfo = trackInfo;
        this.trackGroupArrayList = trackGroupArrayList;
        this.trackType = trackType;
        isDefaultClicked = false;
    }

    public void isDefaultClicked(boolean isDefaultClicked){
        this.isDefaultClicked = isDefaultClicked;
    }

    @NonNull
    @Override
    public QualityItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.quality_list_items, parent,false);
        return new QualityItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QualityItemViewHolder holder, int position) {
        if (trackGroupArrayList.get(position) != null && trackGroupArrayList.get(position).length > 0){
            for (int i =0 ; i < trackGroupArrayList.get(position).length ; i++){
                TrackGroup trackGroup = trackGroupArrayList.get(position).get(i);
                if (trackGroup != null && trackGroup.length > 0){
                    for (int formatPosition = 0; formatPosition < trackGroup.length ; formatPosition++){
                        Format format = trackGroup.getFormat(formatPosition);
                        String trackName = getTrackName(format);
                        holder.itemTitle.setText(trackName);
                    }
                }
                DefaultTrackSelector.SelectionOverride selectionOverride = parameters.getSelectionOverride(i, trackGroupArrayList.get(i));
                if (selectionOverride != null && selectionOverride.length > 0){
                    holder.itemRadioBtn.setChecked(true);
                }
            }
        }
        if (isDefaultClicked){
            holder.itemRadioBtn.setChecked(false);
        }
    }

    private String getTrackName(Format format){
        if (trackType == C.TRACK_TYPE_VIDEO) {
            return joinWithSeparator(mContext.getResources(),
                    buildRoleString(mContext.getResources(), format),
                    buildResolutionString(mContext.getResources(), format),
                    buildBitrateString(mContext.getResources(), format));
        } else if (trackType == C.TRACK_TYPE_AUDIO) {
            return joinWithSeparator(mContext.getResources(),
                    buildLanguageOrLabelString(mContext.getResources(), format),
                    buildAudioChannelString(mContext.getResources(), format),
                    buildBitrateString(mContext.getResources(), format));
        } else {
            return buildLanguageOrLabelString(mContext.getResources(), format);
        }
    }

    @Override
    public int getItemCount() {
        return trackGroupArrayList.size();
    }

    class QualityItemViewHolder extends RecyclerView.ViewHolder{
        TextView itemTitle;
        RadioButton itemRadioBtn;
        public QualityItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemRadioBtn = itemView.findViewById(R.id.item_radio);
            itemRadioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (trackClickListeners != null){
                        trackClickListeners.onTrackClicked(position);
                    }
                }
            });
        }
    }
    public void setTrackClickListeners(TrackClickListeners trackClickListeners){
        this.trackClickListeners = trackClickListeners;
    }
    private TrackClickListeners trackClickListeners;
    interface TrackClickListeners{
        void onTrackClicked(int position);
    }

    public DefaultTrackSelector.ParametersBuilder onOkButtonClicked(DefaultTrackSelector.Parameters parameters, int selectedPosition){
        DefaultTrackSelector.ParametersBuilder parametersBuilder = parameters.buildUpon();
        parametersBuilder.clearSelectionOverrides(selectedPosition);
        DefaultTrackSelector.SelectionOverride overrides = parameters.getSelectionOverride(selectedPosition, trackGroupArrayList.get(selectedPosition));
        if (overrides != null)
            parametersBuilder.setSelectionOverride(selectedPosition,  trackGroupArrayList.get(selectedPosition), overrides);
        return parametersBuilder;
    }
}
