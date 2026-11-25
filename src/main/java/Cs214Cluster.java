import java.util.ArrayList;

public class Cs214Cluster implements Comparable<Cs214Cluster>{
    // centroid array and song array fields.
    private final ArrayList<Double> initialCentroid = new ArrayList<>();

    private ArrayList<Double> centroid = new ArrayList<>();

    private ArrayList<String> songs = new ArrayList<>();

    private String clusterUserChoiceSongName;

    
    public Cs214Cluster(String clusterUserChoiceSongName){
        this.clusterUserChoiceSongName = clusterUserChoiceSongName;
    }
    
    @Override
    public int compareTo(Cs214Cluster other) {
        return this.clusterUserChoiceSongName.compareTo(other.clusterUserChoiceSongName);
    }
    
    public String getClusterUserChoiceSongName() {
        return clusterUserChoiceSongName;
    }

    public ArrayList<Double> getInitialCentroid() {
        return initialCentroid;
    }

    public ArrayList<Double> getCentroid() {
        return centroid;
    }

    public void setCentroid(ArrayList<Double> centroid) {
        this.centroid = centroid;
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

}
