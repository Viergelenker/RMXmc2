package de.ccck.rmxmobile;

import de.tbjv.rmxmc2.activity.ControllerActivity;
import de.tbjv.rmxmc2.controller.TrainControl;

/**
 * Bietet eine Schnittstelle um der Funktionen der grafischen Oberflaeche des
 * Clients zu steuern.
 */
public class GuiInterface {
    /**
     * Informiert die GUI, dass sich die Geschwindigkeit der Lok gaendert hat.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainSpeed(int trainNumber) {
        // TrainControllerViewCollection.initializeSpeed(trainNumber);
    }

    /**
     * Fahrtrichtung des Zuges setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainDirection(int trainNumber) {
        // TrainControllerViewCollection.initializeDirection(trainNumber);
    }

    /**
     * Zugfunktionen Licht bis F7 setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainMode0to7(int trainNumber) {
        // TrainControllerViewCollection.initializef0tof7(trainNumber);
    }

    /**
     * Zugfunktionen F8 bis F15 setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainMode8to15(int trainNumber) {
        // TrainControllerViewCollection.initializef8tof15(trainNumber);
    }

    /**
     * Zugfunktion F16 setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainMode16to23(int trainNumber) {
        // TrainControllerViewCollection.initializef16to23(trainNumber);
    }

    /**
     * Methode zum Aktualisieren des TrainSpinner
     */
    public static synchronized void updateTrainList() {

        TrainControl.initializeTrainSelector();
    }

    /**
     * Aktualisiert das Verbindungsstatus-Label
     *
     * @param connectionStatus
     *            - int - 1 = "getrennt" / 2 = "verbunden"
     */
    public static synchronized void changeConnectionStatus(int connectionStatus) {
        // ControllerViewCollection.updateConnectionStatus(connectionStatus);
    }

    /**
     * Aktualisiert das Energie-Label
     *
     * @param energyOn
     *            - boolean - true = "Energie an" / false = "Energie aus"
     */
    public static synchronized void changeEnergyStatus(boolean energyOn) {
        // ControllerViewCollection.updateEnergyButton(energyOn);
    }

    /**
     * Aktualisiert den angegebenen Bus mit den uebermittelten Inhalten.
     *
     * @param rmx
     *            - byte - RMX-Bus
     * @param key
     *            - String - RMX-Adresse
     */
    public static synchronized void updateBusAdress(byte rmx, String key) {
        // SwitchBoardViewCollectionOld.updateBusAdress(rmx, key);
    }

    /**
     * Aktualisiert alle Gleisplanelemente, deren Bus- und RMX-Adresse
     * mit den übermittelten Werten übereinstimmen.
     *
     * @param rmx - byte - RMX-Bus
     * @param address - String - RMX-Adresse
     * @since AAST-Version
     */
    public static synchronized void updateTrackLayoutElements(byte rmx, String address) {
        // SwitchBoardViewCollection.updateTrackLayoutElements(rmx, address);
    }

}
