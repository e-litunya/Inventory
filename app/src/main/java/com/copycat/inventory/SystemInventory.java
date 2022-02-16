package com.copycat.inventory;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import org.jetbrains.annotations.Nullable;

@IgnoreExtraProperties
public class SystemInventory {
    @PropertyName("Customer Name")
    public String customerName;
    @PropertyName("Data Center")
    public String dataCenter;
    @PropertyName("Device Rack")
    public String rackName;
    @PropertyName("Device Type")
    public String deviceType;
    @PropertyName("Form Factor")
    public String deviceFormFactor;
    @PropertyName("Manufacturer")
    public String deviceManufacturer;
    @PropertyName("Chassis Serial Number")
    public String chassisSerial;
    @PropertyName("Chassis Model")
    public String chassisModel;
    @PropertyName("Device Slot")
    public long serverSlot;
    @PropertyName("Device Serial Number")
    public String deviceSerial;
    @PropertyName("Rack Unit")
    public String rackPosition;
    @PropertyName("Device Model")
    public String deviceModel;
    @PropertyName("Device Model Number")
    public String deviceModelNumber;
    @PropertyName("Engineer")
    public String userID;

    public SystemInventory(String customerName, String dataCenter, String rackName, String deviceType, String deviceManufacturer, String deviceSerial, String rackPosition, String deviceModel, @Nullable String modelNumber, String user) {
        //non compute
        this.customerName = customerName;
        this.dataCenter = dataCenter;
        this.rackName = rackName;
        this.deviceType = deviceType;
        this.deviceManufacturer = deviceManufacturer;
        this.deviceSerial = deviceSerial;
        this.rackPosition = rackPosition;
        this.deviceModel = deviceModel;
        this.deviceModelNumber = modelNumber;
        this.userID = user;
    }

    public SystemInventory(String customerName, String dataCenter, String rackName, String deviceType, String deviceFormFactor, String deviceManufacturer, String chassisSerial, String chassisModel, String serverSlot, String deviceSerial, String deviceModel, @Nullable String deviceModelNumber, String user) {

        //unified infrastructure
        this.customerName = customerName;
        this.dataCenter = dataCenter;
        this.rackName = rackName;
        this.deviceType = deviceType;
        this.deviceFormFactor = deviceFormFactor;
        this.deviceManufacturer = deviceManufacturer;
        this.chassisSerial = chassisSerial;
        this.userID = user;
        this.chassisModel = chassisModel;
        this.serverSlot = Long.parseLong(serverSlot);
        this.deviceSerial = deviceSerial;
        this.deviceModel = deviceModel;
        this.deviceModelNumber = deviceModelNumber;
    }

    public SystemInventory(String customerName, String dataCenter, String rackName, String deviceType, String deviceFormFactor, String deviceManufacturer, String deviceSerial, String rackPosition, String deviceModel, @Nullable String deviceModelNumber, String userID) {
        //standalone
        this.customerName = customerName;
        this.dataCenter = dataCenter;
        this.rackName = rackName;
        this.deviceType = deviceType;
        this.deviceFormFactor = deviceFormFactor;
        this.deviceManufacturer = deviceManufacturer;
        this.deviceSerial = deviceSerial;
        this.rackPosition = rackPosition;
        this.deviceModel = deviceModel;
        this.deviceModelNumber = deviceModelNumber;
        this.userID = userID;
    }

    public SystemInventory() {
    }


    @Exclude
    public String getCustomerName() {
        return customerName;
    }

    @Exclude
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Exclude
    public String getDataCenter() {
        return dataCenter;
    }

    @Exclude
    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    @Exclude
    public String getRackName() {
        return rackName;
    }

    @Exclude
    public void setRackName(String rackName) {
        this.rackName = rackName;
    }

    @Exclude
    public String getDeviceType() {
        return deviceType;
    }

    @Exclude
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Exclude
    public String getDeviceFormFactor() {
        return deviceFormFactor;
    }

    @Exclude
    public void setDeviceFormFactor(String deviceFormFactor) {
        this.deviceFormFactor = deviceFormFactor;
    }

    @Exclude
    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    @Exclude
    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    @Exclude
    public String getChassisSerial() {
        return chassisSerial;
    }

    @Exclude
    public void setChassisSerial(String chassisSerial) {
        this.chassisSerial = chassisSerial;
    }

    @Exclude
    public String getChassisModel() {
        return chassisModel;
    }

    @Exclude
    public void setChassisModel(String chassisModel) {
        this.chassisModel = chassisModel;
    }

    @Exclude
    public long getServerSlot() {
        return serverSlot;
    }

    @Exclude
    public void setServerSlot(long serverSlot) {
        this.serverSlot = serverSlot;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    @Exclude
    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    @Exclude
    public String getRackPosition() {
        return rackPosition;
    }

    @Exclude
    public void setRackPosition(String rackPosition) {
        this.rackPosition = rackPosition;
    }

    @Exclude
    public String getDeviceModel() {
        return deviceModel;
    }

    @Exclude
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    @Exclude
    public String getDeviceModelNumber() {
        return deviceModelNumber;
    }

    @Exclude
    public void setDeviceModelNumber(String deviceModelNumber) {
        this.deviceModelNumber = deviceModelNumber;
    }

    @Exclude
    public String getUserID() {
        return userID;
    }

    @Exclude
    public void setUserID(String user) {
        this.userID = user;
    }
}
