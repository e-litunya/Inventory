package com.copycat.inventory.ui.search;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.copycat.inventory.R;
import com.copycat.inventory.SystemInventory;
import com.copycat.inventory.databinding.FragmentSearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SearchFragment extends Fragment implements View.OnClickListener {

    private SearchViewModel searchViewModel;
    private FragmentSearchBinding binding;
    private EditText serialNumber;
    private TextView customer,datacenter,rack,deviceType,engineer,
            formFactor,vendor,chassisSerial,chassisModel,
            chassisSlot,deviceID,deviceModel,deviceNumber,rackPosition;
    private ImageButton imageButton;
    private ProgressDialog progressDialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        serialNumber=root.findViewById(R.id.search_serial);
        customer=root.findViewById(R.id.search_customer);
        datacenter=root.findViewById(R.id.search_datacenter);
        rack=root.findViewById(R.id.search_rack);
        deviceType=root.findViewById(R.id.search_system_type);
        formFactor=root.findViewById(R.id.search_formFactor);
        vendor=root.findViewById(R.id.search_vendor);
        chassisSerial=root.findViewById(R.id.search_encSerial);
        chassisModel=root.findViewById(R.id.search_encModel);
        chassisSlot=root.findViewById(R.id.search_slot);
        deviceID=root.findViewById(R.id.search_id);
        deviceModel=root.findViewById(R.id.search_model);
        deviceNumber=root.findViewById(R.id.search_modelNumber);
        rackPosition=root.findViewById(R.id.search_rackPosition);
        imageButton=root.findViewById(R.id.search_button);
        engineer=root.findViewById(R.id.search_engineer);
        imageButton.setOnClickListener(this);
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.AppName);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if (v==imageButton)
        {
            if (!TextUtils.isEmpty(serialNumber.getText().toString()))
            {
                clearText();
                searchAndUpdateView(serialNumber.getText().toString());
            }
            else
            {
                Toast.makeText(getContext(),R.string.emptySerial,Toast.LENGTH_LONG).show();
            }

        }
    }

    private void searchAndUpdateView(String deviceSerialNumber)
    {
        setProgressDialogMessage(getResources().getString(R.string.searchStart));
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.orderByChild("deviceSerial").equalTo(deviceSerialNumber);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    SystemInventory systemInventory=dataSnapshot.getValue(SystemInventory.class);

                    if (systemInventory!=null)
                    {
                        customer.setText(systemInventory.getCustomerName());
                        datacenter.setText(systemInventory.getDataCenter());
                        rack.setText(systemInventory.getRackName());
                        deviceType.setText(systemInventory.getDeviceType());
                        formFactor.setText(systemInventory.getDeviceFormFactor());
                        vendor.setText(systemInventory.getDeviceManufacturer());
                        deviceID.setText(systemInventory.getDeviceSerial());
                        deviceModel.setText(systemInventory.getDeviceModel());
                        deviceNumber.setText(systemInventory.getDeviceModelNumber());
                        rackPosition.setText(systemInventory.getRackPosition());
                        engineer.setText(systemInventory.getUserID());
                        try {

                            if ((!checkConverged(systemInventory.getDeviceFormFactor(),systemInventory.getDeviceType()))||(systemInventory.getDeviceModelNumber()==null))
                            {
                                chassisModel.setText(getResources().getString(R.string.unavailable));
                                chassisSerial.setText(getResources().getString(R.string.unavailable));
                                chassisSlot.setText(getResources().getString(R.string.unavailable));
                                deviceNumber.setText(getResources().getString(R.string.unavailable));
                            }
                            else
                            {
                                chassisModel.setText(systemInventory.getChassisModel());
                                chassisSerial.setText(systemInventory.getChassisSerial());
                                chassisSlot.setText(systemInventory.getServerSlot());
                                deviceNumber.setText(systemInventory.getDeviceModelNumber());
                            }


                        }catch (NullPointerException e)
                        {
                            e.printStackTrace();
                        }
                    }



                }
                if (!snapshot.exists())
                {
                    String message= getResources().getString(R.string.noRecord)+serialNumber.getText().toString();
                    setProgressDialogMessage(message);
                    //Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                String message=getResources().getString(R.string.errorLabel)+error.getMessage();
                setProgressDialogMessage(message);

            }
        });


    }

    private boolean checkConverged(String deviceForm, String deviceType) throws NullPointerException
    {
        boolean converged;

        if (deviceForm.equalsIgnoreCase("Blade"))
        {
            converged=true;
        }
        else if (deviceType.equalsIgnoreCase("I/O Module"))
        {
            converged=true;
        }
        else
        {
            converged=false;
        }

        return converged;
    }

    private void setProgressDialogMessage(String message)
    {
        if (progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.show();
        try {

            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            },2000);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    private void clearText()
    {
        String emptyText=getResources().getString(R.string.emptyString);
        customer.setText(emptyText);
        datacenter.setText(emptyText);
        rack.setText(emptyText);
        deviceType.setText(emptyText);
        formFactor.setText(emptyText);
        vendor.setText(emptyText);
        chassisSerial.setText(emptyText);
        chassisModel.setText(emptyText);
        chassisSlot.setText(emptyText);
        deviceID.setText(emptyText);
        rackPosition.setText(emptyText);
        deviceModel.setText(emptyText);
        deviceNumber.setText(emptyText);
        engineer.setText(emptyText);



    }
}