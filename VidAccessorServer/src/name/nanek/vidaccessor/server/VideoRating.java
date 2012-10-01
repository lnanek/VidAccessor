package name.nanek.vidaccessor.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * @author lnanek@gmail.com
 * 
 */
@PersistenceCapable
public class VideoRating {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String videoId;

	@Persistent
	private int rating;

	public VideoRating(String videoId, int rating) {
		this.videoId = videoId;
		this.rating = rating;
	}

	public Key getKey() {
		return key;
	}

	public String getVideoId() {
		return videoId;
	}

	public int getRating() {
		return rating;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
}
