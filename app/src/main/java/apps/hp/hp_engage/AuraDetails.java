package apps.hp.hp_engage;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.aurasma.aurasmasdk.Aura;
import com.aurasma.aurasmasdk.util.LatLong;

/**
 * Created by bernajua on 10/14/2015.
 */
public class AuraDetails implements Parcelable {

    private static final String TAG = "AuraDetails";
    public Aura aura;
    public Bitmap auraImage;
    public Bitmap largeAuraImage;
    public String booth = "Booth #";
    public Double lat;
    public Double lon;

    protected AuraDetails(Parcel in) {
        auraImage = in.readParcelable(Bitmap.class.getClassLoader());
        largeAuraImage = in.readParcelable(Bitmap.class.getClassLoader());
        booth = in.readString();
        try{
        lat = in.readDouble();
        lon = in.readDouble();}
        catch(Exception e){

        }
    }

    public AuraDetails()
    {

    }

    public static final Creator<AuraDetails> CREATOR = new Creator<AuraDetails>() {
        @Override
        public AuraDetails createFromParcel(Parcel in) {
            return new AuraDetails(in);
        }

        @Override
        public AuraDetails[] newArray(int size) {
            return new AuraDetails[size];
        }
    };

    public void setAura(Aura aura) {
        this.aura = aura;

        LatLong  ll = aura.getLocation();

        try {
            lat = ll.getLatitude();
            lon = ll.getLongitude();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Location is null");
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(auraImage, flags);
        dest.writeParcelable(largeAuraImage, flags);
        dest.writeString(booth);
        try {
            dest.writeDouble(lat);
            dest.writeDouble(lon);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG,"Location is null");
        }
    }


}


