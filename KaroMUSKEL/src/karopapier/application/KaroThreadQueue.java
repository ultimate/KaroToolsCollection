package karopapier.application;

import karopapier.gui.ProgressFrame;

public class KaroThreadQueue extends ThreadQueue {
	
	private ProgressFrame progress;
	private int expected;
	private int done;

	public KaroThreadQueue(int max, boolean debugEnabled, ProgressFrame progress, int expected) {
		super(max, debugEnabled);
		this.progress = progress;
		this.expected = expected;
		this.done = 0;
	}

	public KaroThreadQueue(int max, ProgressFrame progress, int expected) {
		super(max);
		this.progress = progress;
		this.expected = expected;
		this.done = 0;
	}
	
	public void notifyFinished(QueuableThread th){
		super.notifyFinished(th);
		done++;
		progress.setProgress((int)Math.round((double)done*100.0/(double)expected));
	}
}
