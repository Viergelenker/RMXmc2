package de.ccck.rmxmobile.data_management;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * TrainDepotMap enthaellt alle Methoden zur Verwaltung aller Zugobjekte.
 * 
 * @author Kientzle Claus, Coels Corinna
 */
public class TrainDepotMap {

	private static TrainDepotMap instance = null;

	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, TrainObject> TrainDepotMap = new HashMap<Integer, TrainObject>();

	private TrainDepotMap() {
	}

	protected static synchronized TrainDepotMap getTrainDepot() {
		if (instance == null) {
			instance = new TrainDepotMap();
		}
		return instance;
	}

	protected synchronized void addTrain(int trainNumber, int adrShort, byte opmode, byte rmxChannel, String trainName,
			byte modeF0F7, byte modeF8F15, byte modeF16F23, byte direction, int runningNotch) {
		TrainObject stopToAdd = new TrainObject();
		stopToAdd.setTrainNumber(trainNumber);
		stopToAdd.setAdrShort(adrShort);
		stopToAdd.setOpmode(opmode);
		stopToAdd.setRmxChannel(rmxChannel);
		stopToAdd.setTrainName(trainName);
		stopToAdd.setModeF0F7(modeF0F7);
		stopToAdd.setModeF8F15(modeF8F15);
		stopToAdd.setModeF16F23(modeF16F23);
		stopToAdd.setDirection(direction);
		stopToAdd.setMaxRunningNotch(runningNotch);
		int key = trainNumber;
		TrainDepotMap.put(key, stopToAdd);
	}

	protected synchronized TrainObject getTrainMapEntry(int key) {
		return TrainDepotMap.get(key);
	}

	protected synchronized ArrayList<String> generateTrainNameList() {
		ArrayList<String> TrainNameList = new ArrayList<String>();

		ArrayList<Integer> sortedList = new ArrayList<Integer>();
		for (Integer key : TrainDepotMap.keySet()) {
			sortedList.add(key);
		}
		Collections.sort(sortedList);
		for (int i = 0; i < sortedList.size(); i++) {
			TrainNameList.add(TrainDepotMap.get(sortedList.get(i)).getTrainNumber() + ": "
					+ TrainDepotMap.get(sortedList.get(i)).getTrainName());
		}
		return TrainNameList;
	}

	protected synchronized boolean isTrainExisting(int key) {
		return TrainDepotMap.containsKey(key);
	}

	protected synchronized void removeTrain(int key) {
		TrainDepotMap.remove(key);
	}

	protected synchronized void clearTrain() {
		TrainDepotMap.clear();
	}
}
