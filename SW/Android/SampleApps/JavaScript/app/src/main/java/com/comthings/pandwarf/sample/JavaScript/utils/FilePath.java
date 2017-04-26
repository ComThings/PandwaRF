package com.comthings.pandwarf.sample.JavaScript.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FilePath {
	// http://www.limbaniandroid.com/2014/03/how-to-get-absolute-path-when-select.html

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileContents(final File file) throws IOException {
		final InputStream inputStream = new FileInputStream(file);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		final StringBuilder stringBuilder = new StringBuilder();

		boolean done = false;

		while (!done) {
			final String line = reader.readLine();
			done = (line == null);

			if (line != null) {
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}
		}

		reader.close();
		inputStream.close();

		return stringBuilder.toString();
	}

	/**
	 * @param aFile
	 * @param aNewText
	 */
	public static void writeToFile(File aFile, String aNewText) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(aFile.getPath()));
			bufferedWriter.write(aNewText);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param file
	 * @param data
	 */
	public static void writeBinaryToFile(final File file, String data) {
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			outputStream.write(data.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method for return file path of chosen file
	 *
	 * @param context
	 * @param uri
	 * @return path of the selected chosen file from file chooser
	 */
	public static String getPath(final Context context, final Uri uri) {
		//check here to KITKAT or new version
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				} else if ("home".equalsIgnoreCase(type)) {
					String envFileType;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						// DIRECTORY_DOCUMENTS is available only on API > 19
						//Document is found in external Storage in sdcard/Documents/Gollum
						envFileType = Environment.DIRECTORY_DOCUMENTS;
					} else {
						// Backup plan: found in /Downloads
						envFileType = Environment.DIRECTORY_DOWNLOADS;
					}

					return Environment.getExternalStoragePublicDirectory(envFileType) + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
}