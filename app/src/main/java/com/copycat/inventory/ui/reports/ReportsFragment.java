package com.copycat.inventory.ui.reports;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.copycat.inventory.Constants;
import com.copycat.inventory.InventoryAdapter;
import com.copycat.inventory.InventoryData;
import com.copycat.inventory.MainActivity;
import com.copycat.inventory.R;
import com.copycat.inventory.SystemInventory;
import com.copycat.inventory.databinding.FragmentReportsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportsFragment extends Fragment implements View.OnClickListener {

    private FragmentReportsBinding binding;
    private ImageButton cloudDownload;
    private String selectedCustomer;
    private ProgressDialog progressDialog;
    private HSSFWorkbook hssfWorkbook;
    private ArrayList<InventoryData> inventoryDataArrayList;
    private InventoryAdapter inventoryAdapter;
    private FloatingActionButton exportReportButton;
    private final ActivityResultLauncher<Intent> fileWrite = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                ParcelFileDescriptor descriptor = null;
                try {
                    assert intent != null;
                    if (intent.getData()!=null)
                    {
                        descriptor = getContext().getContentResolver().openFileDescriptor(intent.getData(), "rw");
                    }

                } catch (FileNotFoundException | NullPointerException e) {
                    e.printStackTrace();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(descriptor.getFileDescriptor());
                if (hssfWorkbook != null) {
                    try {
                        hssfWorkbook.write(fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ReportsViewModel reportsViewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        binding = FragmentReportsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        selectedCustomer = null;
        AutoCompleteTextView customersTextView = root.findViewById(R.id.report_customers);
        ArrayAdapter<String> customerAdapters = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, MainActivity.listedCustomers);
        customersTextView.setAdapter(customerAdapters);
        customersTextView.setThreshold(2);
        customerAdapters.notifyDataSetChanged();
        RecyclerView recyclerView = root.findViewById(R.id.customerInventory);
        cloudDownload = root.findViewById(R.id.downloadInventory);
        cloudDownload.setOnClickListener(this);
        exportReportButton = root.findViewById(R.id.exportButton);
        exportReportButton.setOnClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(getResources().getString(R.string.app_name));
        progressDialog.setIcon(R.mipmap.ic_progress);
        customersTextView.setOnItemClickListener((parent, view, position, id) -> selectedCustomer = parent.getItemAtPosition(position).toString());

        inventoryDataArrayList = new ArrayList<>();
        inventoryAdapter = new InventoryAdapter(inventoryDataArrayList, getContext());
        recyclerView.setAdapter(inventoryAdapter);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {

        if (v == cloudDownload) {
            if (TextUtils.isEmpty(selectedCustomer) || selectedCustomer == null) {
                String message = getString(R.string.customer_missing);
                setProgressDialogMessage(message, true);
            } else {
                getCustomerInventory(selectedCustomer);
            }
        }
        if (v == exportReportButton) {
            try {
                if (!checkPermission()) {
                    requestStoragePermissions();
                    generateCustomerReport();

                } else {

                    generateCustomerReport();
                }
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void getCustomerInventory(String customerName) {

        inventoryDataArrayList.clear();
        String message = getString(R.string.inventoryLoad).concat(customerName);
        setProgressDialogMessage(message, true);
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
        Query dbQuery = dbReference.orderByChild(Constants.CUSTOMER_NAMES).equalTo(customerName);
        dbQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SystemInventory systemInventory = dataSnapshot.getValue(SystemInventory.class);

                    if (systemInventory != null) {
                        checkNullFields(systemInventory);
                        InventoryData inventoryData = new InventoryData();
                        inventoryData.setDataCenter(systemInventory.getDataCenter());
                        inventoryData.setRackName(systemInventory.getRackName());
                        inventoryData.setDeviceType(systemInventory.getDeviceType());
                        inventoryData.setDeviceFormFactor(systemInventory.getDeviceFormFactor());
                        inventoryData.setDeviceManufacturer(systemInventory.getDeviceManufacturer());
                        inventoryData.setChassisSerial(systemInventory.getChassisSerial());
                        inventoryData.setChassisModel(systemInventory.getChassisModel());
                        if ((!systemInventory.getDeviceFormFactor().equalsIgnoreCase("Blade")) || (!systemInventory.getDeviceType().equalsIgnoreCase("I/O Module"))) {
                            inventoryData.setServerSlot(getString(R.string.unavailable));
                        } else {
                            inventoryData.setServerSlot(String.valueOf(systemInventory.getServerSlot()));
                        }
                        inventoryData.setDeviceSerial(systemInventory.getDeviceSerial());
                        inventoryData.setDeviceModel(systemInventory.getDeviceModel());
                        inventoryData.setDeviceModelNumber(systemInventory.getDeviceModelNumber());
                        inventoryData.setRackPosition(systemInventory.getRackPosition());
                        inventoryDataArrayList.add(inventoryData);
                        inventoryAdapter.notifyItemInserted((inventoryDataArrayList.size() - 1));
                        inventoryAdapter.notifyDataSetChanged();

                    } else {
                        Log.d("InventoryError", "Null");
                    }
                }

                if (!snapshot.exists()) {
                    String message = getResources().getString(R.string.inventoryFail).concat(" ").concat(selectedCustomer);
                    setProgressDialogMessage(message, true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                String message = getString(R.string.dbError).concat(error.getMessage());

                setProgressDialogMessage(message, false);
            }
        });


    }

    private void setProgressDialogMessage(String message, boolean timed) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog.setMessage(message);

        if (timed) {
            progressDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(() -> progressDialog.dismiss(), 2000);
        } else {
            progressDialog.show();
        }
    }

    private void checkNullFields(SystemInventory systemInventory) {
        if (TextUtils.isEmpty(systemInventory.getDeviceModelNumber()) || systemInventory.getDeviceModelNumber() == null) {
            systemInventory.setDeviceModelNumber(getResources().getString(R.string.unavailable));
        }
        if (TextUtils.isEmpty(systemInventory.getDeviceFormFactor()) || systemInventory.getDeviceFormFactor() == null) {
            systemInventory.setDeviceFormFactor(getResources().getString(R.string.unavailable));
        }
        if (TextUtils.isEmpty(systemInventory.getChassisSerial()) || systemInventory.getChassisSerial() == null) {
            systemInventory.setChassisSerial(getResources().getString(R.string.unavailable));
        }
        if (TextUtils.isEmpty(systemInventory.getChassisModel()) || systemInventory.getChassisModel() == null) {
            systemInventory.setChassisModel(getResources().getString(R.string.unavailable));
        }
        if (TextUtils.isEmpty(systemInventory.getDeviceSerial()) || systemInventory.getDeviceSerial() == null) {
            systemInventory.setDeviceSerial(getResources().getString(R.string.unavailable));
        }
        if (TextUtils.isEmpty(systemInventory.getRackPosition()) || systemInventory.getRackPosition() == null) {
            systemInventory.setRackPosition(getResources().getString(R.string.unavailable));
        }
        if (TextUtils.isEmpty(String.valueOf(systemInventory.getServerSlot())) || systemInventory.getServerSlot() == 0) {
            systemInventory.setServerSlot(0L);
        }

        if (systemInventory.getDeviceFormFactor().equalsIgnoreCase("Chassis")) {
            systemInventory.setDeviceModel(getResources().getString(R.string.unavailable));
            systemInventory.setDeviceSerial(getResources().getString(R.string.unavailable));

        }


    }

    private void generateCustomerReport() throws NullPointerException, IOException {
        String sheetName;
        hssfWorkbook = new HSSFWorkbook();


        if (selectedCustomer != null) {
            sheetName = selectedCustomer;
        } else {
            sheetName = getString(R.string.emptySheet);
        }


        HSSFSheet inventorySheet = hssfWorkbook.createSheet(selectedCustomer);
        HSSFCellStyle headerCellStyle = hssfWorkbook.createCellStyle();
        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THICK);
        headerCellStyle.setBorderRight(HSSFCellStyle.BORDER_THICK);
        headerCellStyle.setBorderTop(HSSFCellStyle.BORDER_THICK);
        headerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THICK);


        String[] headers = new String[]{getString(R.string.data_center)
                , getString(R.string.rack_number), getString(R.string.system_type),
                getString(R.string.form_factor), getString(R.string.device_vendor),
                getString(R.string.enclosure_serial), getString(R.string.enclosure_model),
                getString(R.string.server_slot), getString(R.string.system_serial), getString(R.string.rack_position),
                getString(R.string.system_model), getString(R.string.system_machine_product)
        };

        Row row = inventorySheet.createRow(0);
        Cell cell;
        for (int i = 0; i < headers.length; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue(headers[i]);
        }

        HSSFCellStyle dataCellStyle = hssfWorkbook.createCellStyle();
        dataCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        dataCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        dataCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

        for (int i = 0; i < inventoryDataArrayList.size(); i++) {
            Row rowData = inventorySheet.createRow(i + 1);
            cell = rowData.createCell(0);
            cell.setCellValue(inventoryDataArrayList.get(i).getDataCenter());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(1);
            cell.setCellValue(inventoryDataArrayList.get(i).getRackName());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(2);
            cell.setCellValue(inventoryDataArrayList.get(i).getDeviceType());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(3);
            cell.setCellValue(inventoryDataArrayList.get(i).getDeviceFormFactor());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(4);
            cell.setCellValue(inventoryDataArrayList.get(i).getDeviceManufacturer());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(5);
            cell.setCellValue(inventoryDataArrayList.get(i).getChassisSerial());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(6);
            cell.setCellValue(inventoryDataArrayList.get(i).getChassisModel());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(7);
            cell.setCellValue(inventoryDataArrayList.get(i).getServerSlot());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(8);
            cell.setCellValue(inventoryDataArrayList.get(i).getDeviceSerial());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(9);
            cell.setCellValue(inventoryDataArrayList.get(i).getRackPosition());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(10);
            cell.setCellValue(inventoryDataArrayList.get(i).getDeviceModel());
            cell.setCellStyle(dataCellStyle);

            cell = rowData.createCell(11);
            cell.setCellValue(inventoryDataArrayList.get(i).getDeviceModelNumber());
            cell.setCellStyle(dataCellStyle);

        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault());
        Date date = new Date();
        String fileName = sheetName.concat("_").concat(simpleDateFormat.format(date)).concat(".xls");
        createSpreadsheet(fileName);


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String storagePath = null;
        hssfWorkbook = null;

    }

    private boolean checkPermission() {
        boolean permission;

        boolean write = (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean read = (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        permission = read && write;

        return permission;
    }

    private void requestStoragePermissions() {

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 181);
    }

    private void createSpreadsheet(String fileName) {
        Intent docCreate = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        docCreate.addCategory(Intent.CATEGORY_OPENABLE);
        docCreate.setType("application/vnd.ms-excel");
        docCreate.putExtra(Intent.EXTRA_TITLE, fileName);
        fileWrite.launch(docCreate);
    }

}