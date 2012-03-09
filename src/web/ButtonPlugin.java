package web;

import java.util.ArrayList;

public class ButtonPlugin implements WebserverPlugin {
	private ArrayList<String> storage;
	private String[] buttons;
	private String page;

	ButtonPlugin(String[] buttons) {
		this.buttons = buttons;
		this.storage = new ArrayList<String>();
		this.page = "<html><body><FORM> ";
		for (String name : buttons) {
			page += "<input type=\"submit\" name=\"B\" style=\"font-size:300%\" value=\""
					+ name + "\"/>";
		}
		page += "</form></body></html>";
		System.err.println("Buttons ready");
	}

	public String serveOnRequest(String in) {
		if (in.length() > 3) {
			String buttonname = new String(in.substring(3));
			System.err.println("|" + buttonname + "|" + storage.size());
			storage.add(buttonname);
		}
		return page;
	}

	public String[] getClicks() {
		String[] vals = this.storage.toArray(new String[this.storage.size()]);
		this.storage.clear();
		return vals;
	}
}
