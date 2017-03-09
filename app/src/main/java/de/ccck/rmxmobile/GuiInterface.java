package de.ccck.rmxmobile;

import de.tbjv.rmxmc2.activity.ControllerActivity;

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
        ControllerActivity.updateTrainSpeed(trainNumber);
    }

    /**
     * Fahrtrichtung des Zuges setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainDirection(int trainNumber) {
       ControllerActivity.updateTrainDirection(trainNumber);
    }

    /**
     * Zugfunktionen Licht bis F7 setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainMode0to7(int trainNumber) {
        ControllerActivity.updateTrainMode0to7(trainNumber);
    }

    /**
     * Zugfunktionen F8 bis F15 setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainMode8to15(int trainNumber) {
        ControllerActivity.updateTrainMode8to15(trainNumber);
    }

    /**
     * Zugfunktion F16 setzen.
     *
     * @param trainNumber
     *            - int - Die Lok-Nr. Identifier.
     */
    public static synchronized void setTrainMode16to23(int trainNumber) {
        ControllerActivity.updateTrainMode16to23(trainNumber);
    }

    /**
     * Methode zum Aktualisieren des TrainSpinner
     */
    public static synchronized void updateTrainList() {
        ControllerActivity.updateTrainSelector();
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
