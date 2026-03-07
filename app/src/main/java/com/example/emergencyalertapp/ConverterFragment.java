package com.example.emergencyalertapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ConverterFragment extends Fragment {

    private AutoCompleteTextView spinner;
    private TextInputEditText etInput;
    private TextView tvResult;
    private MaterialButton btnConvert;

    // Fixed exchange rates/values
    private final double USD_TO_INR = 83.0;
    private final double GALLON_TO_LITER = 3.78541;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_converter, container, false);

        spinner = v.findViewById(R.id.conversionSpinner);
        etInput = v.findViewById(R.id.etInput);
        tvResult = v.findViewById(R.id.tvResult);
        btnConvert = v.findViewById(R.id.btnConvert);

        // Define options
        String[] options = {
                "Celsius to Fahrenheit",
                "Fahrenheit to Celsius",
                "USD to INR",
                "INR to USD",
                "Liters to Gallons",
                "Gallons to Liters"
        };

        // Set adapter for the Material Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, options);
        spinner.setAdapter(adapter);

        btnConvert.setOnClickListener(view -> performConversion());

        return v;
    }

    private void performConversion() {
        String inputStr = etInput.getText().toString().trim();
        if (inputStr.isEmpty()) {
            etInput.setError("Please enter a value");
            return;
        }

        double inputValue;
        try {
            inputValue = Double.parseDouble(inputStr);
        } catch (NumberFormatException e) {
            etInput.setError("Invalid number");
            return;
        }

        String selectedOption = spinner.getText().toString();
        if (selectedOption.isEmpty()) {
            Toast.makeText(getContext(), "Please select a conversion type", Toast.LENGTH_SHORT).show();
            return;
        }

        double result = 0;
        String unit = "";

        switch (selectedOption) {
            case "Celsius to Fahrenheit":
                result = (inputValue * 9/5) + 32;
                unit = " °F";
                break;
            case "Fahrenheit to Celsius":
                result = (inputValue - 32) * 5/9;
                unit = " °C";
                break;
            case "USD to INR":
                result = inputValue * USD_TO_INR;
                unit = " INR";
                break;
            case "INR to USD":
                result = inputValue / USD_TO_INR;
                unit = " USD";
                break;
            case "Liters to Gallons":
                result = inputValue / GALLON_TO_LITER;
                unit = " Gallons";
                break;
            case "Gallons to Liters":
                result = inputValue * GALLON_TO_LITER;
                unit = " Liters";
                break;
        }

        tvResult.setText(String.format("Result: %.2f%s", result, unit));
    }
}