package efc.SensorReaders;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RpmSensorValue {
    @SerializedName(value = "Value") @Expose private int value;
    @SerializedName(value = "TimeStamp") @Expose private String timeStamp;
    @SerializedName(value = "SensoriID") @Expose private int sensoriId;
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public void setTimeStamp(String timestamp) {
        this.timeStamp = timestamp;
    }
    
    public void setSensoriId(int sensoriId) {
        this.sensoriId = sensoriId;
    }
}
