package com.example.travelmantics;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private List<TravelDeal> mDealList;
    private RecyclerViewClickListener listener;

    public DealAdapter(List<TravelDeal> mDealList, RecyclerViewClickListener listener) {
        this.mDealList = mDealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_deals, viewGroup, false);
        return new DealViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int i) {
        TravelDeal travelDeal = mDealList.get(i);
        holder.titleTextView.setText(travelDeal.getTitle());
        holder.descriptionTextView.setText(travelDeal.getDescription());
        holder.priceTextView.setText(travelDeal.getPrice());
        if (travelDeal.getImageUrl() != null) {
            Picasso.get().load(travelDeal.getImageUrl()).into(holder.dealPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mDealList.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, descriptionTextView, priceTextView;
        ImageView dealPhoto;

        public DealViewHolder(@NonNull View itemView, final RecyclerViewClickListener listener) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            priceTextView = itemView.findViewById(R.id.price_text_view);
            dealPhoto = itemView.findViewById(R.id.deal_image_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onRowClicked(getAdapterPosition());
                    }
                }
            });
        }
    }
}
