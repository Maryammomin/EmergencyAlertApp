package com.example.emergencyalertapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class CalculatorFragment extends Fragment {

    private TextView tvResult;
    private String currentInput = "";
    private double firstNumber = 0;
    private String operator = "";
    private boolean isOperatorPressed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calculator, container, false);

        tvResult = v.findViewById(R.id.tvResult);

        // Number Buttons Array
        int[] numberButtons = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};

        View.OnClickListener numberClickListener = view -> {
            MaterialButton b = (MaterialButton) view;
            if (currentInput.equals("0")) currentInput = "";
            currentInput += b.getText().toString();
            tvResult.setText(currentInput);
        };

        for (int id : numberButtons) {
            v.findViewById(id).setOnClickListener(numberClickListener);
        }

        v.findViewById(R.id.btnDot).setOnClickListener(view -> {
            if (!currentInput.contains(".")) {
                if (currentInput.isEmpty()) currentInput = "0";
                currentInput += ".";
                tvResult.setText(currentInput);
            }
        });

        v.findViewById(R.id.btnPlus).setOnClickListener(view -> setOperator("+"));
        v.findViewById(R.id.btnMinus).setOnClickListener(view -> setOperator("-"));
        v.findViewById(R.id.btnMultiply).setOnClickListener(view -> setOperator("*"));
        v.findViewById(R.id.btnDivide).setOnClickListener(view -> setOperator("/"));

        v.findViewById(R.id.btnEqual).setOnClickListener(view -> calculateResult());

        v.findViewById(R.id.btnClear).setOnClickListener(view -> {
            currentInput = "";
            firstNumber = 0;
            operator = "";
            isOperatorPressed = false;
            tvResult.setText("0");
        });

        return v;
    }

    private void setOperator(String op) {
        if (!currentInput.isEmpty()) {
            firstNumber = Double.parseDouble(currentInput);
            operator = op;
            currentInput = "";
            isOperatorPressed = true;
        }
    }

    private void calculateResult() {
        if (isOperatorPressed && !currentInput.isEmpty()) {
            double secondNumber = Double.parseDouble(currentInput);
            double result = 0;

            switch (operator) {
                case "+": result = firstNumber + secondNumber; break;
                case "-": result = firstNumber - secondNumber; break;
                case "*": result = firstNumber * secondNumber; break;
                case "/":
                    if (secondNumber == 0) {
                        Toast.makeText(getContext(), "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    result = firstNumber / secondNumber;
                    break;
            }

            tvResult.setText(String.valueOf(result));
            currentInput = String.valueOf(result);
            isOperatorPressed = false;
        }
    }
}