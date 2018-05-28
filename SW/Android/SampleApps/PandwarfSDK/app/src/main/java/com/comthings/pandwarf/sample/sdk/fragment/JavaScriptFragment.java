package com.comthings.pandwarf.sample.sdk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.gollum.api.gollumandroidlib.GollumException;
import com.comthings.gollum.api.gollumandroidlib.callback.GollumCallbackGetInteger;
import com.comthings.gollum.api.gollumandroidlib.events.JavaScriptConsoleLogEvent;
import com.comthings.pandwarf.sample.sdk.R;
import com.comthings.pandwarf.sample.sdk.utils.FilePath;
import com.pddstudio.highlightjs.HighlightJsView;
import com.pddstudio.highlightjs.models.Language;
import com.pddstudio.highlightjs.models.Theme;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class JavaScriptFragment extends Fragment implements View.OnClickListener {
	public static final String MIME_TYPE_JAVASCRIPT = "application/octet-stream";
	static final int SELECT_JS_FILE_REQ = 1;
	static private final String TAG = "JSFrag";
	private static final int DONGLE_ID = 0;
	static final int MAX_JS_FILE_SIZE_BYTE = 10000;
	private Activity activity;
	static TextView mTextViewConsoleLogJS;
	private HighlightJsView mTextViewJsFileContent;
	static ScrollView mScrollViewConsoleJS;
	private Button mButtonJsSdcardScriptParse;
	private ToggleButton mButtonJsRunScript;

	/**
	 * Class for storing the currently opened JS file
	 */
	class GollumJavaScriptFile {
		String absoluteGollumJsPath = null; // jsFile.getAbsolutePath()
		File jsFile;
		String fileContent;// String storing the currently opened data from jsFile
		String fileName; // String storing the currently opened filename
	}

	;
	GollumJavaScriptFile gollumJavaScriptFile = new GollumJavaScriptFile();

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Register to console log JS events
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_java_script, container, false);
		activity = getActivity();

		mButtonJsSdcardScriptParse = (Button) v.findViewById(R.id.js_sdcard_scripts_parse_button);
		mButtonJsSdcardScriptParse.setOnClickListener(this);

		mButtonJsRunScript = (ToggleButton) v.findViewById(R.id.run_js_script_button);
		mButtonJsRunScript.setOnClickListener(this);

		mTextViewJsFileContent = (HighlightJsView) v.findViewById(R.id.textViewJsFileContent);
		mTextViewJsFileContent.setTheme(Theme.ARDUINO_LIGHT);
		mTextViewJsFileContent.setHighlightLanguage(Language.JAVA_SCRIPT);
		mTextViewJsFileContent.getSettings().setTextSize(WebSettings.TextSize.SMALLEST);

		mTextViewConsoleLogJS = (TextView) v.findViewById(R.id.textView_js_gollum_print);

		mScrollViewConsoleJS = (ScrollView) v.findViewById(R.id.scrollView_JS_log);

		return v;
	}

	public void onClick(View v) {
		if (v == mButtonJsSdcardScriptParse) {
			Log.d(TAG, "Launching Scripts from internal storage");
			displayListOfJsFilesOnStorage();
		} else if (v == mButtonJsRunScript) {

			if (getJsFileContent().isEmpty()) {
				//if JS buffer is empty, disable Run button
				mButtonJsRunScript.setChecked(false);
				return;
			}

			// Start the magic
			GollumDongle.getInstance(getActivity()).sendJsFile(DONGLE_ID, gollumJavaScriptFile.absoluteGollumJsPath, new GollumCallbackGetInteger() {
				@Override
				public void done(int result, GollumException e) {
					if (result < 0) {
						TastyToast.makeText(getActivity(), "error", TastyToast.LENGTH_LONG, TastyToast.ERROR);
						mButtonJsRunScript.setChecked(false);
					} else {
						TastyToast.makeText(getActivity().getApplicationContext(), "success", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
						mButtonJsRunScript.setChecked(false);
					}
				}
			});
		}
	}

	private void displayListOfJsFilesOnStorage() {
		Log.d(TAG, "displayListOfJsFilesOnStorage()");

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType(MIME_TYPE_JAVASCRIPT);

		if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivityForResult(Intent.createChooser(intent, "Select Javascript"), SELECT_JS_FILE_REQ);
		}
	}

	/**
	 * Set content of javascript file on the webview
	 */
	private void setJavaScriptOnWebView() {
		int fileContentLength = gollumJavaScriptFile.fileContent.length();

		if (fileContentLength < MAX_JS_FILE_SIZE_BYTE) {
			setJsFileContent(gollumJavaScriptFile.fileContent);
		} else {
			TastyToast.makeText(getContext(), "file too big" + ": " + fileContentLength + ", max: " + MAX_JS_FILE_SIZE_BYTE + " bytes",
					TastyToast.LENGTH_LONG, TastyToast.ERROR);
		}
	}

	/**
	 * Called when intent finishes, normaly with filename in data parameter
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult, requestCode: " + requestCode + ", resultCode: " + resultCode);
		if (resultCode != Activity.RESULT_OK) {
			TastyToast.makeText(getContext(), getString(R.string.msg_error_incorrect_file_picker) + ", resultCode: " + resultCode, TastyToast.LENGTH_SHORT, TastyToast.ERROR);
			return;
		}

		if (requestCode == SELECT_JS_FILE_REQ) {
			Uri uri = data.getData();

			String fileName = FilePath.getPath(activity.getApplicationContext(), uri);

			if (fileName == null) {
				TastyToast.makeText(getContext(), "Null File Name, abort loading", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
				return;
			}

			gollumJavaScriptFile.jsFile = new File(fileName);
			gollumJavaScriptFile.fileName = fileName;

			gollumJavaScriptFile.absoluteGollumJsPath = gollumJavaScriptFile.jsFile.getAbsolutePath();

			// Display file content
			try {
				gollumJavaScriptFile.fileContent = FilePath.getFileContents(gollumJavaScriptFile.jsFile);
				setJavaScriptOnWebView();
			} catch (IOException e) {
				Log.e(TAG, "IOException: " + e.toString());
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "Incorrect onActivityResult requestCode: " + requestCode);
		}
	}

	/**
	 * @param fileContent
	 */
	private void setJsFileContent(String fileContent) {
		// Display content in HighlightJS
		mTextViewJsFileContent.setSource(fileContent);

		// Save the content
		gollumJavaScriptFile.fileContent = fileContent;

		// Write content to file for coherency
		if (gollumJavaScriptFile.jsFile != null) {
			FilePath.writeToFile(gollumJavaScriptFile.jsFile, fileContent);
		}
	}

	/**
	 * @return
	 */
	private String getJsFileContent() {
		if (gollumJavaScriptFile.fileContent == null) {
			return "";
		}

		return gollumJavaScriptFile.fileContent;
	}

	// EventBus
	// This method will be called when a JavaScriptConsoleLogEvent is posted
	public void onEventMainThread(JavaScriptConsoleLogEvent event) {
		log(event.getStringToPrint());
	}

	public void log(final String text) {
		//Print now
		mTextViewConsoleLogJS.append(text + "\n");
		//Handle AutoScroll
		scrollConsoleToBottom();
	}

	public void scrollConsoleToBottom() {
		if (mScrollViewConsoleJS == null) {
			return;
		}

		mScrollViewConsoleJS.post(new Runnable() {
			public void run() {
				mScrollViewConsoleJS.fullScroll(View.FOCUS_DOWN);
			}
		});
	}
}