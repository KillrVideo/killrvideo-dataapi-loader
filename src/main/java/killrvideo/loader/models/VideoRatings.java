package killrvideo.loader.models;

public class VideoRatings {
	private String videoid;
	private int ratingCounter;
	private int ratingTotal;
	
	public String getVideoid() {
		return videoid;
	}
	public void setVideoid(String videoid) {
		this.videoid = videoid;
	}
	public int getRatingCounter() {
		return ratingCounter;
	}
	public void setRatingCounter(int ratingCounter) {
		this.ratingCounter = ratingCounter;
	}
	public int getRatingTotal() {
		return ratingTotal;
	}
	public void setRatingTotal(int ratingTotal) {
		this.ratingTotal = ratingTotal;
	}
}
