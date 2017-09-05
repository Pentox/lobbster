package olbot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImprovedString {

	private String content = "";
	public List<String> words = new LinkedList<String>();
	public List<String> lines = new LinkedList<String>();

	public ImprovedString (String content) {
		this.content = content;
		words = Arrays.asList(this.content.split(" "));
		lines = Arrays.asList(this.content.split("\n"));
	}

	public String getWord (int index) {
		return words.get(index);
	}

	public String getLine (int index) {
		return lines.get(index);
	}

	public String getLine (int index, int ignore) {
		return lines.get(index).substring(ignore);
	}

	public String getLine (int index, int ignore, String pattern) {
		String[] split = lines.get(index).split(pattern);
		String ret = "";
		for (int i = ignore; i < split.length; i++) {
			ret += ret.isEmpty() ? split[i] : pattern + split[i];
		}
		return ret;
	}

	public String getContent (int ignore, String pattern) {
		String[] split = content.split(pattern);
		String ret = "";
		for (int i = ignore; i < split.length; i++) {
			ret += ret.isEmpty() ? split[i] : pattern + split[i];
		}
		return ret;
	}

	public String getContent (int ignore) {
		return content.substring(ignore);
	}

	public String getContent() {
		return content;
	}
}
