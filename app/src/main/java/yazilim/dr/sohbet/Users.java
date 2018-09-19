package yazilim.dr.sohbet;

/**
 * Created by ramazan on 9/3/2018.
 */

public class Users {
    public String isim;
    public String resim;
    public String durum;
    public String kucuk_resim;



    public Users(){

    }
    public Users(String isim, String resim, String durum, String kucuk_resim) {
        this.isim = isim;
        this.resim = resim;
        this.durum = durum;
        this.kucuk_resim = kucuk_resim;
    }

    public String getIsim() {
        return isim;
    }

    public void setIsim(String isim) {
        this.isim = isim;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    public String getKucuk_resim() {
        return kucuk_resim;
    }

    public void setKucuk_resim(String kucuk_resim) {
        this.kucuk_resim = kucuk_resim;
    }
}
