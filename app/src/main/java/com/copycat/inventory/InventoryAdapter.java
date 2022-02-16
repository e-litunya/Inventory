package com.copycat.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private ArrayList<InventoryData> inventoryData;
    private  Context context;

    public InventoryAdapter(ArrayList<InventoryData> inventoryData, Context context) {
        this.inventoryData = inventoryData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.inventory_report_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        InventoryData inventory=inventoryData.get(position);
        holder.datacenter.setText(inventory.dataCenter);
        holder.rack.setText(inventory.rackName);
        holder.systemType.setText(inventory.deviceType);
        holder.formFactor.setText(inventory.deviceFormFactor);
        holder.vendor.setText(inventory.deviceManufacturer);
        holder.chassisSerial.setText(inventory.chassisSerial);
        holder.chassisModel.setText(inventory.chassisModel);
        holder.deviceSlot.setText(String.valueOf(inventory.serverSlot));
        holder.deviceSerial.setText(inventory.deviceSerial);
        holder.deviceModel.setText(inventory.deviceModel);
        holder.deviceModelID.setText(inventory.deviceModelNumber);
        holder.rackPosition.setText(inventory.rackPosition);


    }

    @Override
    public int getItemCount() {
        return inventoryData.size()==0 ? 0: inventoryData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView datacenter;
        private  TextView rack;
        private  TextView systemType;
        private  TextView formFactor;
        private  TextView vendor;
        private  TextView chassisSerial;
        private  TextView chassisModel;
        private  TextView deviceSlot;
        private  TextView deviceSerial;
        private  TextView deviceModel;
        private  TextView deviceModelID;
        private TextView rackPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            datacenter = itemView.findViewById(R.id.report_datacenter);
            rack = itemView.findViewById(R.id.report_rack);
            systemType = itemView.findViewById(R.id.report_system_type);
            formFactor = itemView.findViewById(R.id.report_formFactor);
            vendor = itemView.findViewById(R.id.report_vendor);
            chassisSerial = itemView.findViewById(R.id.report_encSerial);
            chassisModel = itemView.findViewById(R.id.report_encModel);
            deviceSlot = itemView.findViewById(R.id.report_slot);
            deviceSerial = itemView.findViewById(R.id.report_id);
            deviceModel = itemView.findViewById(R.id.report_model);
            deviceModelID = itemView.findViewById(R.id.report_modelNumber);
            rackPosition=itemView.findViewById(R.id.report_rackPosition);

        }
    }


}
