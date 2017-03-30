package de.ccck.rmxmobile.data_management;

import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * SharedPrefIO enthaellt alle Methoden um Konfigurationen in einem externen
 * Dokument zu speichern und dieses zu verwalten.
 * 
 * @author Kientzle Claus
 */
public class SharedPrefIO {

	private Context context;

	@SuppressLint("StaticFieldLeak")
	private static SharedPrefIO SharedPrefIO;

	private SharedPrefIO(Context context) {
		this.context = context;
	}

	protected static synchronized SharedPrefIO getSharedPrefIO(Context context) {
		if (SharedPrefIO == null) {
			SharedPrefIO = new SharedPrefIO(context);
		}
		return SharedPrefIO;
	}

	protected void save(String accountname) {
		int i = 1;
		boolean found = false;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		Map<String, ?> keys = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			if (entry.getKey().contains("name")
					&& entry.getValue().toString().equals(accountname)) {
				String[] splitResult = entry.getKey().split("//");
				i = Integer.parseInt(splitResult[1]);
				found = true;
			}
		}
		if (!found) {
			while (sharedPreferences.contains("name//" + i)) {
				i++;
			}
		}
		editor.putString("name//" + i, ConfigObject.getConfigObject()
				.getAccountName());
		editor.putString("IP//" + i, ConfigObject.getConfigObject()
				.getIpAdress());
		editor.putString("Port//" + i, ConfigObject.getConfigObject().getPort());
		editor.putInt("Theme//" + i, ConfigObject.getConfigObject().getTheme());
		if (editor.commit()) {
			// Done within the AddProfileActivity and MainActivity
			/*Toast.makeText(
					context,
					"Der Datensatz mit dem Schlüssel: " + accountname
							+ " wurde erfolgreich gespeichert",
					Toast.LENGTH_SHORT).show();*/
		} else {
			Toast.makeText(
					context,
					"Der Datensatz mit dem Schlüssel: " + accountname
							+ " konnte nicht gespeichert werden",
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void saveLastID(int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("LastID", value);
		editor.apply();
	}

	protected int loadLastID() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		return sharedPreferences.getInt("LastID", 0);
	}

	protected void saveLastTheme() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("LastTheme", ConfigObject.getConfigObject().getTheme());
		editor.apply();
	}

	protected void loadLastTheme() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		ConfigObject.getConfigObject().setTheme(
				sharedPreferences.getInt("LastTheme", 0));
	}

	protected void load(String accountname) {
		boolean found = false;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		Map<String, ?> keys = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			if (entry.getKey().contains("name")
					&& entry.getValue().toString().equals(accountname)) {
				String[] splitResult = entry.getKey().split("//");
				ConfigObject.getConfigObject().setAccountName(
						sharedPreferences.getString("name//" + splitResult[1],
								null));
				ConfigObject.getConfigObject().setIpAdress(
						sharedPreferences.getString("IP//" + splitResult[1],
								null));
				ConfigObject.getConfigObject().setPort(
						sharedPreferences.getString("Port//" + splitResult[1],
								null));
				ConfigObject.getConfigObject()
						.setTheme(
								sharedPreferences.getInt("Theme//"
										+ splitResult[1], 0));
				found = true;
			}
		}
		if (!found) {
			Toast.makeText(
					context,
					"Der Datensatz mit dem Schlüssel: " + accountname
							+ " ist nicht vorhanden", Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected void delete(String accountname) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		Map<String, ?> keys = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			if (entry.getKey().contains("name")
					&& entry.getValue().toString().equals(accountname)) {
				String[] splitResult = entry.getKey().split("//");
				editor.remove("name//" + splitResult[1]);
				editor.remove("IP//" + splitResult[1]);
				editor.remove("Port//" + splitResult[1]);
				editor.remove("Theme//" + splitResult[1]);
				if (editor.commit()) {
					// Done within the MainActivity
					/*Toast.makeText(
							context,
							"Der Datensatz mit dem Schlüssel: " + accountname
									+ " wurde gelöscht",
							Toast.LENGTH_SHORT).show();*/
					ConfigObject.getConfigObject().setAccountName("");
					ConfigObject.getConfigObject().setIpAdress("");
					ConfigObject.getConfigObject().setPort("");
				} else {
					Toast.makeText(
							context,
							"Der Datensatz mit dem Schlüssel: " + accountname
									+ " konnte nicht gelöscht werden",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	protected ArrayList<String> generateConfigNameList() {
		ArrayList<String> al = new ArrayList<String>();
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		Map<String, ?> keys = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			if (entry.getKey().contains("name")) {
				al.add(sharedPreferences.getString(entry.getKey(), null));
			}
		}
		return al;
	}

	protected boolean checkSingularity(String input) {
		boolean unique = false;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"ConfigObjectPref", Context.MODE_PRIVATE);
		Map<String, ?> keys = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			if (entry.getKey().contains("name")
					&& entry.getValue().equals(input)) {
				unique = true;
			}
		}
		return unique;
	}
}
