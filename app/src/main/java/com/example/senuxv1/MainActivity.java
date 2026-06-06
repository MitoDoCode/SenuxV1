package com.example.senuxv1;

import android.os.Bundle;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.view.View;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.ScrollView;
import android.widget.AdapterView;

import androidx.core.splashscreen.SplashScreen;


public class MainActivity extends AppCompatActivity {

    private EditText commandInput;
    private Button goButton, saveButton;
    private Spinner savedSpinner;
    private TextView outputText;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> savedCommandsList;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ui stuff
        commandInput = findViewById(R.id.pt_TextInput);
        goButton = findViewById(R.id.btn_Go);
        saveButton = findViewById(R.id.btn_Save);
        savedSpinner = findViewById(R.id.spin_SavedDropdown);
        outputText = findViewById(R.id.tv_Output);

        // Load saved commands
        prefs = getSharedPreferences("CommandPrefs", MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("commands", new HashSet<>());
        savedCommandsList = new ArrayList<>(savedSet);
        Collections.sort(savedCommandsList);

        //Spinner
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, savedCommandsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        savedSpinner.setAdapter(adapter);

        // When an item is selected put it into the EditText
        savedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < savedCommandsList.size()) {
                    commandInput.setText(savedCommandsList.get(position));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save command btn
        saveButton.setOnClickListener(v -> {
            String cmd = commandInput.getText().toString().trim();
            if (cmd.isEmpty()) {
                Toast.makeText(this, "Enter a command", Toast.LENGTH_SHORT).show();
                return;
            }
            if (savedCommandsList.contains(cmd)) {
                Toast.makeText(this, "Already saved", Toast.LENGTH_SHORT).show();
                return;
            }
            savedCommandsList.add(cmd);
            Collections.sort(savedCommandsList);
            adapter.notifyDataSetChanged();
                //  SharedPreferences
            Set<String> newSet = new HashSet<>(savedCommandsList);
            prefs.edit().putStringSet("commands", newSet).apply();
            Toast.makeText(this, "Command saved", Toast.LENGTH_SHORT).show();
        });

        // Go button send command to Arch Linux server
        goButton.setOnClickListener(v -> {
            String cmd = commandInput.getText().toString().trim();
            if (cmd.isEmpty()) {
                Toast.makeText(this, "Type a command", Toast.LENGTH_SHORT).show();
                return;
            }
            outputText.append("> " + cmd + "\n");
            sendCommand(cmd);
        });
    }

    private void sendCommand(String command) {
        ShellApi api = RetrofitClient.getInstance();
        Call<CommandResponse> call = api.execCommand(new CommandRequest(command));
        call.enqueue(new Callback<CommandResponse>() {
            @Override
            public void onResponse(Call<CommandResponse> call, Response<CommandResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String output = response.body().getOutput();
                    outputText.append(output + "\n");
                } else {
                    outputText.append("Error: " + response.code() + "\n");
                }
                // Auto-scroll to bottom
                ScrollView scrollView = findViewById(R.id.scrollOutput);
                if (scrollView != null) {
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                }
            }

            @Override
            public void onFailure(Call<CommandResponse> call, Throwable t) {
                outputText.append("Network error: " + t.getMessage() + "\n");
            }
        });
    }
}