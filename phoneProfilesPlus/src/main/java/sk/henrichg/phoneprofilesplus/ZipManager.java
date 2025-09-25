package sk.henrichg.phoneprofilesplus;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class ZipManager {
	private static final int BUFFER = 80000;

	/** @noinspection BlockingMethodInNonBlockingContext*/
	public boolean zip(String[] _files, String zipFileName) {
		try {
			BufferedInputStream origin;
			FileOutputStream dest = new FileOutputStream(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			byte[] data = new byte[BUFFER];

			//noinspection ForLoopReplaceableByForEach
			for (int i = 0; i < _files.length; i++) {
				Log.v("Compress", "Adding: " + _files[i]);
				FileInputStream fi = new FileInputStream(_files[i]);
				origin = new BufferedInputStream(fi, BUFFER);

				ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
				out.putNextEntry(entry);
				int count;

				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}

			out.close();

			return true;
		} catch (Exception e) {
			PPApplicationStatic.logException("ZipManager.zip", Log.getStackTraceString(e), false);
			return false;
		}
	}

	/** @noinspection BlockingMethodInNonBlockingContext*/
	public boolean unzip(String _zipFile, String _targetLocation) {
		//create target location folder if not exist
		dirChecker(_targetLocation);
		
		try {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {

				//create dir if required while unzipping
				if (ze.isDirectory()) {
					dirChecker(ze.getName());
				} else {
					FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
			return true;
		} catch (Exception e) {
			PPApplicationStatic.logException("ZipManager.unzip", Log.getStackTraceString(e), false);
			return false;
		}
	}

	private void dirChecker(String dir) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			//noinspection ResultOfMethodCallIgnored
			f.mkdirs();
		}
	}

}
