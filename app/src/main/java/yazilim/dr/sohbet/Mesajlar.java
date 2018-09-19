package yazilim.dr.sohbet;

/**
 * Created by ramazan on 9/14/2018.
 */

public class Mesajlar {

    private String mesaj,type,from;
    private long time;
    private boolean seen;

    public Mesajlar(String mesaj, String type, long time, boolean seen) {
        this.mesaj = mesaj;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Mesajlar() {
    }
}
