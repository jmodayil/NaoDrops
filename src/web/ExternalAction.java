package web;

public class ExternalAction {
	private PluggableGuiWebserver webserver;
	private ButtonPlugin bp;
	private int action = 0;

	public int getAction() {
		String[] chosen = bp.getClicks();
		// System.out.println(chosen.length);
		for (String lastClick : chosen) {
			System.out.println(" SD:" + lastClick + "|");

			if (lastClick.equals("Dorothea")) {
				System.out.println("Doro");
				action = 2;
//				robot.sendAction(50, 50);
			} else if (lastClick.equals("Freund")) {
				System.out.println("Freund");
				action =  1;
//				robot.sendAction(150, 150);
			} else if (lastClick.equals("Anna")) {
				System.out.println("Anna");
				action =  0;
//				robot.sendAction(-550, 550);
			}
		}
		return action;
	}

	public ExternalAction() {
		this.bp = new ButtonPlugin(new String[] { "Dorothea", "Freund",
				"Anna"});
		this.webserver = new PluggableGuiWebserver(8080, bp);
	}

	public static void main(String[] args) {
		ExternalAction test = new ExternalAction();
		System.out.println("Hallo!");
		while (true) {
			System.out.println(test.getAction());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
