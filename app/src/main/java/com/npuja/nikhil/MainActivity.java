package com.npuja.nikhil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.widget.*;
import android.view.View;
import android.view.View.*;
import java.io.*;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;


import android.view.KeyEvent; 
import android.view.inputmethod.EditorInfo; 
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class MainActivity extends Activity {
  
  
  private TextView terminalView;
private EditText inputView;
private ScrollView scrollView;

  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
  
setContentView(R.layout.activity_main);


terminalView = findViewById(R.id.terminalView);
    inputView = findViewById(R.id.inputView);
    scrollView = findViewById(R.id.scrollView);
    
    File homeDir = new File(getFilesDir(), "home");
if (!homeDir.exists()) {
    homeDir.mkdirs(); 
unzipAssetToFiles("Boot.zip");
copyBinaryFromAssets("profile", "/data/data/com.npuja.nikhil/files/");
copyBinaryFromAssets("motd.sh", "/data/data/com.npuja.nikhil/files/home/");
copyBinaryFromAssets("jvm", "/data/data/com.npuja.nikhil/files/home/");
}
    
    
    
/*copyBinaryFromAssets("hello");
copyBinaryFromAssets("njvm");
copyBinaryFromAssets("ser");
copyBinaryFromAssets("profile");

// aapt2 run
copyBinaryFromAssets("aapt2");
copyBinaryFromAssets("motd.sh");
copyBinaryFromAssets("fl");
copyBinaryFromAssets("apt");
copyBinaryFromAssets("nano");

//c++ run
copyBinaryFromAssets("libc++_shared.so");

//bash run
copyBinaryFromAssets("bash");
copyBinaryFromAssets("libandroid-support.so");
copyBinaryFromAssets("libreadline.so.8");
copyBinaryFromAssets("libiconv.so");
copyBinaryFromAssets("libreadline.so");
copyBinaryFromAssets("libncursesw.so.6");


copyAsset("bashrc", ".profile");

unzipAssetToFiles("Boot.zip");
//unzipAssetToFiles("aapt2lib.zip");

//JAVA
    copyBinaryFromAssets("libandroid-shmem.so");
    copyBinaryFromAssets("libandroid-spawn.so");
    copyBinaryFromAssets("libz.so.1");

*/
NativePTY nativePTY = new NativePTY();
int fd = nativePTY.startShell("/data/data/com.npuja.nikhil/files/usr/bin/bash");

        new Thread(() -> {
    while (true) {
        String output = nativePTY.readFromShell();
        if (!output.isEmpty()) {
            appendToTerminal(output);
        }
    }
}).start();


findViewById(R.id.btnCtrlC).setOnClickListener(v -> {
    nativePTY.writeToShell("\u0003");
   // appendToTerminal("cancel");
});



 inputView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {

            String input = inputView.getText().toString();
            
           
            
            
            inputView.setText("");
           // appendToTerminal("\n$ " + input + "\n");

            
                nativePTY.writeToShell(input);
            

            return true;
        }
        return false;
    });
  
}




//output
private void appendToTerminal(String text) {
    runOnUiThread(() -> {
        terminalView.append(ANSIParser.parse(text));
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    });
}

//Copy Asset
private void copyBinaryFromAssets(String filename , String folder) {
    File outFile = new File(folder, filename);
    if (outFile.exists()) return;

    try (InputStream in = getAssets().open(filename);
         FileOutputStream out = new FileOutputStream(outFile)) {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        outFile.setExecutable(true);
    } catch (IOException e) {
       // appendToTerminal("Binary copy error: " + e.getMessage() + "\n");
    }
}
//Bashrc copy
private void copyAsset(String assetName, String outName) {
    File outFile = new File(getFilesDir(), outName);
    if (outFile.exists()) return;

    try (InputStream in = getAssets().open(assetName);
         FileOutputStream out = new FileOutputStream(outFile)) {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        outFile.setExecutable(true);
        outFile.setReadable(true);
    } catch (IOException e) {
        appendToTerminal("Copy error: " + e.getMessage() + "\n");
    }
}


// jvm
public void unzipAssetToFiles(String assetZipName) {
    File outputDir = getFilesDir();
    File checkFile = new File(outputDir, "usr/java-17-openjdk");

    if (checkFile.exists()) {
        // Already extracted, skip
        return;
    }

    try {
        InputStream is = getAssets().open(assetZipName);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[4096];

        while ((ze = zis.getNextEntry()) != null) {
            File outFile = new File(outputDir, ze.getName());

            // নিরাপত্তা: ফাইল পাথ validate
            String canonicalPath = outFile.getCanonicalPath();
            String canonicalOutputDir = outputDir.getCanonicalPath();
            if (!canonicalPath.startsWith(canonicalOutputDir)) {
                throw new IOException("Entry outside target dir: " + ze.getName());
            }

            if (ze.isDirectory()) {
                if (!outFile.exists()) {
                    boolean created = outFile.mkdirs();
                    if (!created) {
                        Log.e("UNZIP", "Failed to create directory: " + outFile.getAbsolutePath());
                    }
                }
            } else {
                // Ensure parent folders exist
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    boolean created = parent.mkdirs();
                    if (!created) {
                        Log.e("UNZIP", "Failed to create parent directories for: " + outFile.getAbsolutePath());
                    }
                }

                // Write the file
                FileOutputStream fos = new FileOutputStream(outFile);
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.close();

                // ✅ Make executable if under /bin/ or /lib/
                if (ze.getName().contains("/bin/")) {
                    outFile.setExecutable(true); // executable for all users
                    Log.d("UNZIP", "Executable set: " + ze.getName());
                }
            }

            zis.closeEntry();
        }

        zis.close();
        is.close();

        Log.d("UNZIP", "✅ Extraction complete: " + outputDir.getAbsolutePath());

    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
