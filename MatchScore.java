package persistent;

public class MatchScore {

	private int score;
	private int additional;
	private String URL;
	
	public MatchScore(String URL, int score){
		this.setScore(score);
		this.setURL(URL);
	}
	
	public MatchScore(String URL, int score, int additional){
		this.setScore(score);
		this.setURL(URL);
		this.additional = additional;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public int getAdditional() {
		return additional;
	}

	public void setAdditional(int additional) {
		this.additional = additional;
	}
	
}
