package com.copycat.inventory.ui.entry;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.copycat.inventory.MainActivity;
import com.copycat.inventory.R;
import com.copycat.inventory.SystemInventory;
import com.copycat.inventory.databinding.FragmentEntryBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Locale;

public class EntryFragment extends Fragment implements View.OnClickListener {

    private FragmentEntryBinding binding;
    private AutoCompleteTextView vendors, deviceType, formFactor;
    private volatile boolean unified, standalone, typeNumber, chassis, validationStatus;
    private String deviceVendor;
    private String deviceForm;
    private String device;
    private EditText customerName, datacenterName, rack, enclosureSn, enclosureModel, deviceSlot, serialNumber, rackStart, rackEnd, deviceModel, modelNumber;
    private FloatingActionButton captureButton;
    private ImageButton saveButton;
    private ProgressDialog progressDialog;
    private boolean computeIO, nonCompute;
    private TextInputLayout machineType;


    public EntryFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EntryViewModel entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);

        binding = FragmentEntryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        entryViewModel.getText().observe(getViewLifecycleOwner(), s -> {

        });

        customerName = root.findViewById(R.id.customer_name);
        datacenterName = root.findViewById(R.id.datacenter_name);
        rack = root.findViewById(R.id.rack_number);
        enclosureSn = root.findViewById(R.id.chassis_sn);
        enclosureModel = root.findViewById(R.id.enclosure_model);
        deviceSlot = root.findViewById(R.id.chassis_slot);
        serialNumber = root.findViewById(R.id.system_serial);
        rackStart = root.findViewById(R.id.rack_start);
        rackEnd = root.findViewById(R.id.rack_end);
        deviceModel = root.findViewById(R.id.system_model);
        modelNumber = root.findViewById(R.id.system_product);
        vendors = root.findViewById(R.id.vendor);
        deviceType = root.findViewById(R.id.system_type);
        machineType = root.findViewById(R.id.product_number);
        formFactor = root.findViewById(R.id.form_type);
        saveButton = root.findViewById(R.id.saveButton);
        captureButton = root.findViewById(R.id.ocrButton);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.AppName);

        String[] devices = getResources().getStringArray(R.array.device_type);
        String[] forms = getResources().getStringArray(R.array.device_form);
        String[] vendor = getResources().getStringArray(R.array.vendors);
        ArrayAdapter<String> devicesAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_layout, devices);
        ArrayAdapter<String> formsAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_layout, forms);
        ArrayAdapter<String> vendorAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_layout, vendor);
        vendors.setAdapter(vendorAdapter);
        deviceType.setAdapter(devicesAdapter);
        formFactor.setAdapter(formsAdapter);
        formFactor.setOnItemClickListener((parent, view, position, id) -> {
            deviceForm = parent.getItemAtPosition(position).toString();

            if (position == 1 || position == 3) {
                binding.enclosureSn.setEnabled(true);
                binding.enclosureType.setEnabled(true);
                binding.slot.setEnabled(true);
                unified = true;
                standalone = false;


            } else {
                binding.enclosureSn.setEnabled(false);
                binding.enclosureType.setEnabled(false);
                binding.slot.setEnabled(false);
                rackStart.setEnabled(true);
                rackEnd.setEnabled(true);
                unified = false;
                standalone = true;
            }
            if (position == 3) {
                chassis = true;
                deviceSlot.setEnabled(false);
            }
        });
        deviceType.setOnItemClickListener((parent, view, position, id) -> {
            device = parent.getItemAtPosition(position).toString();

            if (position == 0) {
                formFactor.setEnabled(true);

                nonCompute = false;
            } else {

                nonCompute = true;
            }
            if (position == 3) {
                binding.enclosureSn.setEnabled(true);
                binding.enclosureType.setEnabled(true);
                binding.slot.setEnabled(true);
                computeIO = true;
            } else {
                binding.enclosureSn.setEnabled(false);
                binding.enclosureType.setEnabled(false);
                binding.slot.setEnabled(false);
                computeIO = false;
            }
        });
        saveButton.setOnClickListener(this);
        captureButton.setOnClickListener(this);
        vendors.setOnItemClickListener((parent, view, position, id) -> {

            deviceVendor = parent.getItemAtPosition(position).toString();
            if (position == 0) {
                typeNumber = true;
                machineType.setHint(R.string.system_product);
            } else if (position == 2) {
                typeNumber = true;
                machineType.setHint(R.string.system_machine);
            } else {
                typeNumber = false;
            }
        });


        return root;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getUserID(String user) {
        String userName = "";
        if (!TextUtils.isEmpty(user)) {
            String[] arrays = user.split("@");
            userName = arrays[0];
        }


        return userName;
    }

    private String getRackPosition(String start, String end) {
        return start + " -- " + end;
    }

    private void saveData() {
        fieldsValidation();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String userID = getUserID(MainActivity.userID);
        String rackLocation = getResources().getString(R.string.defRack);
        if (standalone && validationStatus) {
            setProgressDialogMessage(getResources().getString(R.string.saveStart));
            rackLocation = getRackPosition(rackStart.getText().toString(), rackEnd.getText().toString());
            SystemInventory systemInventory = new SystemInventory(customerName.getText().toString(),
                    datacenterName.getText().toString(), rack.getText().toString(), device, deviceForm, deviceVendor,
                    serialNumber.getText().toString().trim().toUpperCase(Locale.ROOT), rackLocation, deviceModel.getText().toString(),
                    modelNumber.getText().toString(), userID);
            databaseReference.push().setValue(systemInventory, (error, ref) -> {

                if (error == null) {
                    setProgressDialogMessage(getResources().getString(R.string.saveComplete));
                } else {
                    String errorString = "Error: " + error.getMessage();
                    setProgressDialogMessage(errorString);
                }
            });
        } else if (chassis && validationStatus) {
            setProgressDialogMessage(getResources().getString(R.string.saveStart));
            rackLocation = getRackPosition(rackStart.getText().toString(), rackEnd.getText().toString());
            SystemInventory systemInventory = new SystemInventory();
            systemInventory.setCustomerName(customerName.getText().toString());
            systemInventory.setDataCenter(datacenterName.getText().toString());
            systemInventory.setRackName(rack.getText().toString());
            systemInventory.setDeviceType(device);
            systemInventory.setDeviceFormFactor(deviceForm);
            systemInventory.setDeviceManufacturer(deviceVendor);
            systemInventory.setDeviceSerial(enclosureSn.getText().toString().trim().toUpperCase(Locale.ROOT));
            systemInventory.setChassisSerial(enclosureSn.getText().toString());
            systemInventory.setChassisModel(enclosureModel.getText().toString());
            systemInventory.setRackPosition(rackLocation);
            if (typeNumber)
                systemInventory.setDeviceModelNumber(modelNumber.getText().toString());
            systemInventory.setUserID(userID);

            databaseReference.push().setValue(systemInventory, (error, ref) -> {

                if (error == null) {
                    setProgressDialogMessage(getResources().getString(R.string.saveComplete));
                } else {
                    String errorString = "Error: " + error.getMessage();
                    setProgressDialogMessage(errorString);
                }
            });

        } else if (unified && validationStatus) {
            setProgressDialogMessage(getResources().getString(R.string.saveStart));
            SystemInventory systemInventory = new SystemInventory();
            systemInventory.setCustomerName(customerName.getText().toString());
            systemInventory.setDataCenter(datacenterName.getText().toString());
            systemInventory.setRackName(rack.getText().toString());
            systemInventory.setDeviceType(device);
            systemInventory.setDeviceFormFactor(deviceForm);
            systemInventory.setDeviceManufacturer(deviceVendor);
            systemInventory.setChassisSerial(enclosureSn.getText().toString());
            systemInventory.setChassisModel(enclosureModel.getText().toString());
            systemInventory.setServerSlot(Long.parseLong(deviceSlot.getText().toString()));
            systemInventory.setDeviceSerial(serialNumber.getText().toString().toUpperCase(Locale.ROOT).trim());
            systemInventory.setDeviceModel(deviceModel.getText().toString());
            if (typeNumber)
                systemInventory.setDeviceModelNumber(modelNumber.getText().toString());
            systemInventory.setUserID(userID);

            databaseReference.push().setValue(systemInventory, (error, ref) -> {

                if (error == null) {
                    setProgressDialogMessage(getResources().getString(R.string.saveComplete));
                } else {
                    String errorString = "Error: " + error.getMessage();
                    setProgressDialogMessage(errorString);
                }
            });
        } else if (computeIO && validationStatus) {
            setProgressDialogMessage(getResources().getString(R.string.saveStart));
            SystemInventory systemInventory = new SystemInventory();
            systemInventory.setCustomerName(customerName.getText().toString());
            systemInventory.setDataCenter(datacenterName.getText().toString());
            systemInventory.setRackName(rack.getText().toString());
            systemInventory.setDeviceType(device);
            systemInventory.setDeviceManufacturer(deviceVendor);
            systemInventory.setChassisSerial(enclosureSn.getText().toString());
            systemInventory.setChassisModel(enclosureModel.getText().toString());
            systemInventory.setServerSlot(Long.parseLong(deviceSlot.getText().toString()));
            systemInventory.setDeviceSerial(serialNumber.getText().toString().toUpperCase(Locale.ROOT).trim());
            systemInventory.setDeviceModel(deviceModel.getText().toString());
            if (typeNumber)
                systemInventory.setDeviceModelNumber(modelNumber.getText().toString());
            systemInventory.setUserID(userID);

            databaseReference.push().setValue(systemInventory, (error, ref) -> {

                if (error == null) {
                    setProgressDialogMessage(getResources().getString(R.string.saveComplete));
                } else {
                    String errorString = "Error: " + error.getMessage();
                    setProgressDialogMessage(errorString);
                }
            });
        } else if (nonCompute && validationStatus) {
            setProgressDialogMessage(getResources().getString(R.string.saveStart));
            rackLocation = getRackPosition(rackStart.getText().toString(), rackEnd.getText().toString());
            SystemInventory systemInventory = new SystemInventory();
            systemInventory.setCustomerName(customerName.getText().toString());
            systemInventory.setDataCenter(datacenterName.getText().toString());
            systemInventory.setRackName(rack.getText().toString());
            systemInventory.setDeviceType(device);
            systemInventory.setDeviceManufacturer(deviceVendor);
            systemInventory.setDeviceSerial(serialNumber.getText().toString().trim().toUpperCase(Locale.ROOT));
            systemInventory.setDeviceModel(deviceModel.getText().toString());
            systemInventory.setRackPosition(rackLocation);
            if (typeNumber)
                systemInventory.setDeviceModelNumber(modelNumber.getText().toString());
            systemInventory.setUserID(userID);

            databaseReference.push().setValue(systemInventory, (error, ref) -> {

                if (error == null) {
                    setProgressDialogMessage(getResources().getString(R.string.saveComplete));
                } else {
                    String errorString = "Error: " + error.getMessage();
                    setProgressDialogMessage(errorString);
                }
            });


        } else {
            String message = getResources().getString(R.string.validationError);

            setProgressDialogMessage(message);
        }


    }


    @Override
    public void onClick(View v) {
        if (v == saveButton) {

            saveData();
        }

        if (v == captureButton) {

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(getActivity());
        }

    }

    private void setProgressDialogMessage(String message) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.show();
        try {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 3000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ArrayList<String> checkEmptyFields(ArrayList<EditText> editTexts, ArrayList<AutoCompleteTextView> textViews) {
        ArrayList<String> resultArrayList = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();
        for (int index = 0; index < editTexts.size(); index++) {
            if (TextUtils.isEmpty(editTexts.get(index).getText().toString())) {
                try {
                    TextInputLayout inputLayout = (TextInputLayout)
                            getActivity().findViewById(editTexts.get(index).getId())
                                    .getParent()
                                    .getParent();
                    String messages = inputLayout.getHint().toString();
                    resultArrayList.add(messages);
                } catch (NullPointerException s) {
                    s.printStackTrace();
                }


            }

        }

        for (int index = 0; index < textViews.size(); index++) {
            if (TextUtils.isEmpty(textViews.get(index).getText().toString())) {
                try {
                    TextInputLayout inputLayout = (TextInputLayout)
                            getActivity().findViewById(textViews.get(index).getId())
                                    .getParent()
                                    .getParent();
                    String messages = inputLayout.getHint().toString();
                    strings.add(messages);
                } catch (NullPointerException s) {
                    s.printStackTrace();
                }
            }
        }

        validationStatus = resultArrayList.size() == 0 && strings.size() == 0;

        resultArrayList.addAll(strings);

        return resultArrayList;

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void fieldsValidation() {
        ArrayList<EditText> requiredFields;
        ArrayList<AutoCompleteTextView> requiredSelections;
        String toastMessage, message;
        int scenario = 0;
        if (standalone) {
            scenario = 1;
        } else if (chassis) {
            scenario = 2;
        } else if (unified) {
            scenario = 3;
        } else if (nonCompute) {
            scenario = 4;
        } else if (computeIO) {
            scenario = 5;
        }

        switch (scenario) {

            case 1:
                requiredFields = new ArrayList<>();
                requiredSelections = new ArrayList<>();
                requiredSelections.add(deviceType);
                requiredSelections.add(vendors);
                requiredSelections.add(formFactor);
                requiredFields.add(customerName);
                requiredFields.add(datacenterName);
                requiredFields.add(rack);
                requiredFields.add(serialNumber);
                requiredFields.add(deviceModel);
                //check rack position
                if (typeNumber)
                    requiredFields.add(modelNumber);
                message = prepareMessages(checkEmptyFields(requiredFields, requiredSelections), true);
                if (!TextUtils.isEmpty(message)) {
                    toastMessage = message.concat(" ").concat(getResources().getString(R.string.emptyLabel));
                    showToast(toastMessage);
                }
                break;
            case 2:
                requiredFields = new ArrayList<>();
                requiredSelections = new ArrayList<>();
                requiredSelections.add(deviceType);
                requiredSelections.add(vendors);
                requiredFields.add(customerName);
                requiredFields.add(datacenterName);
                requiredFields.add(rack);
                requiredFields.add(enclosureModel);
                requiredFields.add(enclosureSn);
                if (typeNumber)
                    requiredFields.add(modelNumber);
                message = prepareMessages(checkEmptyFields(requiredFields, requiredSelections), true);
                if (!TextUtils.isEmpty(message)) {
                    toastMessage = message.concat(" ").concat(getResources().getString(R.string.emptyLabel));
                    showToast(toastMessage);
                }
                break;
            case 3:
                requiredFields = new ArrayList<>();
                requiredSelections = new ArrayList<>();
                requiredSelections.add(deviceType);
                requiredSelections.add(vendors);
                requiredSelections.add(formFactor);
                requiredFields.add(customerName);
                requiredFields.add(datacenterName);
                requiredFields.add(rack);
                requiredFields.add(deviceSlot);
                requiredFields.add(serialNumber);
                requiredFields.add(deviceModel);
                requiredFields.add(enclosureSn);
                if (typeNumber)
                    requiredFields.add(modelNumber);
                message = prepareMessages(checkEmptyFields(requiredFields, requiredSelections), false);
                if (!TextUtils.isEmpty(message)) {
                    toastMessage = message.concat(" ").concat(getResources().getString(R.string.emptyLabel));
                    showToast(toastMessage);
                }
                break;
            case 4:
                requiredFields = new ArrayList<>();
                requiredSelections = new ArrayList<>();
                requiredSelections.add(deviceType);
                requiredSelections.add(vendors);
                requiredFields.add(customerName);
                requiredFields.add(datacenterName);
                requiredFields.add(rack);
                requiredFields.add(serialNumber);
                requiredFields.add(deviceModel);
                if (typeNumber)
                    requiredFields.add(modelNumber);
                message = prepareMessages(checkEmptyFields(requiredFields, requiredSelections), true);
                if (!TextUtils.isEmpty(message)) {
                    toastMessage = message.concat(" ").concat(getResources().getString(R.string.emptyLabel));
                    showToast(toastMessage);
                }
                break;
            case 5:
                requiredFields = new ArrayList<>();
                requiredSelections = new ArrayList<>();
                requiredSelections.add(deviceType);
                requiredSelections.add(vendors);
                requiredFields.add(customerName);
                requiredFields.add(datacenterName);
                requiredFields.add(rack);
                requiredFields.add(serialNumber);
                requiredFields.add(enclosureSn);
                requiredFields.add(enclosureModel);
                requiredFields.add(deviceModel);
                if (typeNumber)
                    requiredFields.add(modelNumber);
                message = prepareMessages(checkEmptyFields(requiredFields, requiredSelections), true);
                if (!TextUtils.isEmpty(message)) {
                    toastMessage = message.concat(" ").concat(getResources().getString(R.string.emptyLabel));
                    showToast(toastMessage);
                }
                break;


        }
    }

    private String prepareMessages(ArrayList<String> strings, boolean rackPositionRequired) {
        String message = getResources().getString(R.string.emptyString);
        StringBuilder tempString = new StringBuilder();
        for (int stringIndex = 0; stringIndex < strings.size(); stringIndex++) {
            tempString.append(strings.get(stringIndex)).append(", ");
        }

        if (rackPositionRequired) {
            //check if empty
            if (TextUtils.isEmpty(rackStart.getText().toString()) || TextUtils.isEmpty(rackEnd.getText().toString())) {
                tempString.append(getResources().getString(R.string.rack_position));
            }

        }

        message = tempString.toString();

        return message;
    }


}